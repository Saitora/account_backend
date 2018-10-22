import ru.sberbank.model.AccountsManager;
import ru.sberbank.model.helpers.HibernateUtil;

public class Main {

    public static void main(final String[] args) throws Exception {
        HibernateUtil.buildSessionFactory();
        AccountsManager manager = new AccountsManager(HibernateUtil.getSessionFactory());
        System.out.println(manager.countAccounts());
        System.out.println(manager.getAllAccounts());
        HibernateUtil.closeSessionFactory();
    }
}