package ru.sberbank.model.helpers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    // this is called by the servlet context listener
    public static void buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure();

            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void closeSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static Session getSession() {
        return sessionFactory.openSession();
    }

/*    // check if the entity user already exist in the database
    public static User getUser(String email) {
        Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();
        User user = (User)session.createQuery("from User c where c.emailAddress like :email").setParameter("email", email).uniqueResult();
        session.getTransaction().commit();
        return user;
    }

    // if getUser returns null, insert this user in the database
    public static void insertUser(User user) {
        Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();
        session.save(user);
        session.getTransaction().commit();
    }*/

}