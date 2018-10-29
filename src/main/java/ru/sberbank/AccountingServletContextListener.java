package ru.sberbank;

import ru.sberbank.model.helpers.HibernateUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.TimeZone;

public class AccountingServletContextListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        HibernateUtil.closeSessionFactory();
    }

    //Run this before web application is started
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        HibernateUtil.buildSessionFactory();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+6"));
    }

}