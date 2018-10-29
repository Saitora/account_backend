package ru.sberbank.model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;

public class OperationsManager {

    private final SessionFactory sessions;

    public OperationsManager(SessionFactory sessions) {
        this.sessions = sessions;
    }

    public void createFromOperation(int accountId, String operationType, BigDecimal value) {
        OperationsEntity operation = new OperationsEntity();
        operation.setFromAccountId(accountId);
        operation.setValue(value);
        operation.setOperationType(operationType);
        Session s = sessions.openSession();
        Transaction t = s.beginTransaction();
        s.save(operation);
        t.commit();
        s.close();
    }

    public void createToOperation(int accountId, String operationType, BigDecimal value) {
        OperationsEntity operation = new OperationsEntity();
        operation.setToAccountId(accountId);
        operation.setValue(value);
        operation.setOperationType(operationType);
        Session s = sessions.openSession();
        Transaction t = s.beginTransaction();
        s.save(operation);
        t.commit();
        s.close();
    }

    public void createTransferOperation(int fromAccountId, int toAccountId, String operationType, BigDecimal value) {
        OperationsEntity operation = new OperationsEntity();
        operation.setFromAccountId(fromAccountId);
        operation.setToAccountId(toAccountId);
        operation.setValue(value);
        operation.setOperationType(operationType);
        Session s = sessions.openSession();
        Transaction t = s.beginTransaction();
        s.save(operation);
        t.commit();
        s.close();
    }

    public long countOperations() {
        Session s = sessions.openSession();
        long count = (Long) s.createQuery("SELECT count(O) FROM OperationsEntity O").getSingleResult();
        s.close();
        return count;
    }

    public List<OperationsEntity> getAllOperations() {
        List result = null;
        Session s = sessions.openSession();
        result = s.createQuery("SELECT O FROM OperationsEntity O ORDER BY O.created desc").list();
        s.close();
        return result;
    }

    public List getOperationsPage(int pageSize, int pageNum) {
        List result = null;
        Session s = sessions.openSession();
        Query q = s.createQuery("SELECT O FROM OperationsEntity O ORDER BY O.created desc");
        q.setFirstResult(pageSize * pageNum);
        q.setMaxResults(pageSize);
        result = q.list();
        s.close();
        return result;
    }

    public List<OperationsEntity> getAllOperationsForAccount(int accountId) {
        List result = null;
        Session s = sessions.openSession();
        result = s.createQuery("SELECT O FROM OperationsEntity O WHERE O.fromAccountId = :accountId ORDER BY O.created desc")
                .setParameter("accountId", accountId)
                .list();
        s.close();
        return result;
    }

    public List getOperationsPageForAccount(int pageSize, int pageNum, int accountId) {
        List result = null;
        Session s = sessions.openSession();
        Query q = s.createQuery("SELECT O FROM OperationsEntity O WHERE O.fromAccountId = :accountId ORDER BY O.created desc")
                .setParameter("accountId", accountId);
        q.setFirstResult(pageSize * pageNum);
        q.setMaxResults(pageSize);
        result = q.list();
        s.close();
        return result;
    }

}
