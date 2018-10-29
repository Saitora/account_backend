package ru.sberbank.model;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import ru.sberbank.model.exceptions.AccountNotEnoughCashException;
import ru.sberbank.model.helpers.CustomLock;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountsManager {

    private final SessionFactory sessions;
    private static final Map<String, CustomLock> locks = new HashMap<>();

    enum OPERATION_TYPES {
        get("GET"),
        put("PUT");

        private final String value;

        OPERATION_TYPES(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static final String OPERATION_TYPES_LOV = "OPERATION_TYPE";

    public AccountsManager(SessionFactory sessions) {
        this.sessions = sessions;
    }

    public void deleteAccount(String accountNumber) {
        Session s = sessions.openSession();
        Transaction t = s.beginTransaction();
        CustomLock lock = getAccountLock(accountNumber);
        try {
            lock.lock();
            AccountsEntity account = new AccountsEntity();
            Query q = s.createQuery("SELECT A FROM AccountsEntity A WHERE A.accountNumber = :accnum");
            account = (AccountsEntity) q.setParameter("accnum", accountNumber).getSingleResult();
            s.remove(account);
            t.commit();
        } catch (HibernateException e) {
            t.rollback();
            throw e;
        } finally {
            s.close();
            freeLock(lock);
        }
    }

    public List<AccountsEntity> getAllAccounts() {
        List result = null;
        Session s = sessions.openSession();
        result = s.createQuery("SELECT A FROM AccountsEntity A ORDER BY A.created DESC").list();
        s.close();
        return result;
    }

    public List getAccountPage(int pageSize, int pageNum, String accountNumFilter) {
        List result = null;
        Session s = sessions.openSession();
        Query q = null;
        if (accountNumFilter == null) {
            q = s.createQuery("SELECT A FROM AccountsEntity A ORDER BY A.created DESC");
        } else {
            q = s.createQuery("SELECT A FROM AccountsEntity A WHERE A.accountNumber like :account_num ORDER BY A.created DESC");
            q.setParameter("account_num", "%" + accountNumFilter + '%');
        }
        q.setFirstResult(pageSize * pageNum);
        q.setMaxResults(pageSize);
        result = q.list();
        s.close();
        return result;
    }

    public long countAccounts(String accountNumFilter) {
        Session s = sessions.openSession();
        long count = 0;
        Query q = null;
        if (accountNumFilter == null) {
            q = s.createQuery("SELECT count(A) FROM AccountsEntity A");
        } else {
            q = s.createQuery("SELECT count(A) FROM AccountsEntity A WHERE A.accountNumber like :account_num");
            q.setParameter("account_num", "%" + accountNumFilter + '%');
        }
        count = (Long) q.getSingleResult();
        s.close();
        return count;
    }

    public AccountsEntity createAccount(String accountNumber) {
        AccountsEntity account = new AccountsEntity();
        Session s = sessions.openSession();
        Transaction t = s.beginTransaction();
        account.setAccountNumber(accountNumber);
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
            lock.lock();
            Query q = s.createQuery("SELECT A FROM AccountsEntity A WHERE A.accountNumber = :accnum")
                    .setParameter("accnum", accountNumber);
            AccountsEntity account = (AccountsEntity) q.getSingleResult();
            account.setCash(account.getCash().add(cash));
            createOperation(account, null, OPERATION_TYPES.put, cash);
            t.commit();
        } catch (HibernateException e) {
            t.rollback();
            throw e;
        } finally {
            s.close();
            freeLock(lock);
        }
    }

    public void getCash(String accountNumber, BigDecimal cash) throws AccountNotEnoughCashException {
        Session s = sessions.openSession();
        Transaction t = s.beginTransaction();
        CustomLock lock = getAccountLock(accountNumber);
        try {
            lock.lock();
            Query q = s.createQuery("SELECT A FROM AccountsEntity A WHERE A.accountNumber = :accnum")
                    .setParameter("accnum", accountNumber);
            AccountsEntity account = (AccountsEntity) q.getSingleResult();
            if (cash.compareTo(account.getCash()) > 0) {
                throw new AccountNotEnoughCashException();
            }
            account.setCash(account.getCash().subtract(cash));
            createOperation(account, null, OPERATION_TYPES.get, cash);
            t.commit();
        } catch (HibernateException e) {
            t.rollback();
            throw e;
        } finally {
            s.close();
            freeLock(lock);
        }
    }

    public void transferCash(String fromAccount, String toAccount, BigDecimal cash) throws AccountNotEnoughCashException {
        Session s = sessions.openSession();
        Transaction t = s.beginTransaction();
        CustomLock fromLock = getAccountLock(fromAccount);
        CustomLock toLock = getAccountLock(toAccount);
        try {
            fromLock.lock();
            toLock.lock();
            Query q = s.createQuery("SELECT A FROM AccountsEntity A WHERE A.accountNumber = :accnum")
                    .setParameter("accnum", fromAccount);
            AccountsEntity fromAccountEntity = null;
            fromAccountEntity = (AccountsEntity) q.getSingleResult();
            if (cash.compareTo(fromAccountEntity.getCash()) > 0) {
                throw new AccountNotEnoughCashException();
            }
            fromAccountEntity.setCash(fromAccountEntity.getCash().subtract(cash));
            q.setParameter("accnum", toAccount);
            AccountsEntity toAccountEntity = (AccountsEntity) q.getSingleResult();
            toAccountEntity.setCash(toAccountEntity.getCash().add(cash));
            createOperation(fromAccountEntity, toAccountEntity, OPERATION_TYPES.get, cash);
            createOperation(toAccountEntity, fromAccountEntity, OPERATION_TYPES.put, cash);
            t.commit();
        } catch (HibernateException e) {
            t.rollback();
            throw e;
        } finally {
            s.close();
            freeLock(fromLock);
            freeLock(toLock);
        }
    }

    private void createOperation(AccountsEntity fromAccount, AccountsEntity toAccount, OPERATION_TYPES operationType, BigDecimal value) {
        OperationsManager manager = new OperationsManager(sessions);
        ListOfValuesManager lovManager = new ListOfValuesManager(sessions);
        String opType = lovManager.lookupValue(OPERATION_TYPES_LOV, operationType.getValue());
        if ( (fromAccount == null) && (toAccount == null)) {
            throw new IllegalArgumentException();
        }
        if (fromAccount != null) {
            if (toAccount != null) {
                manager.createTransferOperation(fromAccount.getId(), toAccount.getId(), opType, value);
            } else {
                manager.createFromOperation(fromAccount.getId(), opType, value);
            }
        } else {
            manager.createToOperation(toAccount.getId(), opType, value);
        }
    }

    private static synchronized void freeLock(CustomLock lock) {
        lock.unlock();
        lock.decCounter();
        if (lock.getCounter() == 0) {
            locks.remove(lock.getKey());
        }
        System.out.println("Free: " + locks.toString());
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
        System.out.println("Get: " + locks.toString());
        return lock;
    }

}
