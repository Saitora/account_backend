package ru.sberbank.model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

public class ListOfValuesManager {

    private final SessionFactory sessions;

    public ListOfValuesManager(SessionFactory sessions) {
        this.sessions = sessions;
    }

    // return "" if no value was found
    public String lookupValue(String type, String code) {
        String result = "";
        Session s = sessions.openSession();
        Query q = s.createQuery("SELECT L.displayValue FROM ListOfValuesEntity L WHERE L.type = :type AND L.code = :code");
        q.setParameter("type", type);
        q.setParameter("code", code);
        try {
            result = (String) q.getSingleResult();
        } catch (Exception e) {
            // nope
        }
        s.close();
        return result;
    }

}
