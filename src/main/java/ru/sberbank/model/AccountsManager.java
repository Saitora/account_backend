package ru.sberbank.model;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import ru.sberbank.model.exceptions.AccountNotEnoughCashException;
import ru.sberbank.model.exceptions.DeadLockPossibilityException;
import ru.sberbank.model.helpers.CustomLock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountsManager {

    private final SessionFactory sessions;
    private static final Map<String, CustomLock> locks = new HashMap<>();

    public AccountsManager(SessionFactory sessions) {
        this.sessions = sessions;
    }

    public List<AccountsEntity> getAllAccounts() {
        List result = null;
        Session s = sessions.openSession();
        result = s.createQuery("SELECT A FROM AccountsEntity A").list();
        s.close();
        return result;
    }

    public List getAccountPage(int pageSize, int pageNum) {
        List result = null;
        Session s = sessions.openSession();
        Query q = s.createQuery("SELECT A FROM AccountsEntity A");
        q.setFirstResult(pageSize * pageNum);
        q.setMaxResults(pageSize);
        result = q.list();
        s.close();
        return result;
    }

    public long countAccounts() {
        Session s = sessions.openSession();
        long count = (Long) s.createQuery("SELECT count(A) FROM AccountsEntity A").getSingleResult();
        s.close();
        return count;
    }

    public AccountsEntity createAccount() {
        AccountsEntity account = new AccountsEntity();
        Session s = sessions.openSession();
        Transaction t = s.beginTransaction();
        s.save(account);
        t.commit();
        s.refresh(account);
        s.close();
        return account;
    }

    public void addCash(String accountNumber, BigDecimal cash) {
        Session s = sessions.openSession();
        Transaction t = s.beginTransaction();
        CustomLock lock = getAccountLock(accountNumber);
        try {
            synchronized (lock) {
                Query q = s.createQuery("SELECT A FROM AccountsEntity A WHERE A.accountNumber = :accnum")
                        .setParameter("accnum", accountNumber);
                AccountsEntity account = (AccountsEntity) q.getSingleResult();
                account.setCash(account.getCash().add(cash));
            }
            t.commit();
        } catch (HibernateException e) {
            t.rollback();
            throw e;
        } finally {
            freeLock(lock);
            s.close();
        }
    }

    public void getCash(String accountNumber, BigDecimal cash) throws AccountNotEnoughCashException {
        Session s = sessions.openSession();
        Transaction t = s.beginTransaction();
        CustomLock lock = getAccountLock(accountNumber);
        try {
            synchronized (lock) {
                Query q = s.createQuery("SELECT A FROM AccountsEntity A WHERE A.accountNumber = :accnum")
                        .setParameter("accnum", accountNumber);
                AccountsEntity account = (AccountsEntity) q.getSingleResult();
                if (cash.compareTo(account.getCash()) > 0) {
                    throw new AccountNotEnoughCashException();
                }
                account.setCash(account.getCash().subtract(cash));
            }
            t.commit();
        } catch (HibernateException e) {
            t.rollback();
            throw e;
        } finally {
            freeLock(lock);
            s.close();
        }
    }

    public void transferCash(String fromAccount, String toAccount, BigDecimal cash) throws AccountNotEnoughCashException, DeadLockPossibilityException {
        synchronized (AccountsManager.class) {
            Session s = sessions.openSession();
            Transaction t = s.beginTransaction();
            CustomLock fromLock = getAccountLock(fromAccount);
            CustomLock toLock = getAccountLock(toAccount);
            try {
                Query q = s.createQuery("SELECT A FROM AccountsEntity A WHERE A.accountNumber = :accnum")
                        .setParameter("accnum", fromAccount);
                AccountsEntity fromAccountEntity = null;
                synchronized (fromLock) {
                    fromAccountEntity = (AccountsEntity) q.getSingleResult();
                    if (cash.compareTo(fromAccountEntity.getCash()) > 0) {
                        throw new AccountNotEnoughCashException();
                    }
                    fromAccountEntity.setCash(fromAccountEntity.getCash().subtract(cash));
                    if (toAccount.equals("30000000000000000000")) {
                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                synchronized (toLock) {
                    q.setParameter("accnum", toAccount);
                    AccountsEntity toAccountEntity = (AccountsEntity) q.getSingleResult();
                    toAccountEntity.setCash(toAccountEntity.getCash().add(cash));
                }
                t.commit();

            } catch (HibernateException e) {
                t.rollback();
                throw e;
            } finally {
                freeLock(fromLock);
                freeLock(toLock);
                s.close();
            }
        }
    }

    private static synchronized void freeLock(CustomLock lock) {
        lock.decCounter();
        if (lock.getCounter() == 0) {
            locks.remove(lock.getKey());
        }
    }

    private static synchronized CustomLock getAccountLock(String accountNum) {
        CustomLock lock = null;
        if (locks.containsKey(accountNum)) {
            lock = locks.get(accountNum);
        } else {
            lock = new CustomLock(accountNum);
            locks.put(accountNum, lock);
        }
        lock.incCounter();
        return lock;
    }

}
