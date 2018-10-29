package ru.sberbank;

import org.hibernate.HibernateException;
import org.json.JSONObject;
import ru.sberbank.model.AccountsEntity;
import ru.sberbank.model.AccountsManager;
import ru.sberbank.model.exceptions.AccountingException;
import ru.sberbank.model.helpers.HibernateUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class ServletAccount extends HttpServlet {

    enum ERROR_STATUS {
        OK(0, ""),
        WRONG_METHOD(1, "wrong method"),
        WRONG_PARAMETERS(2, "wrong parameters"),
        ERROR(3, "server error");

        private final int value;
        private final String description;

        ERROR_STATUS(final int newValue, final String description) {
            this.value = newValue;
            this.description = description;
        }

        public int getValue() { return value; }
        public String getDescription() { return description; }
    }

    //Post
    private static final String METHOD_CREATE = "create";
    private static final String METHOD_ADD_CASH = "add";
    private static final String METHOD_GET_CASH = "get";
    private static final String METHOD_TRANSFER_CASH = "transfer";
    private static final String METHOD_DELETE = "delete";

    //Get
    private static final String METHOD_GET_ALL_ACCOUNTS = "get_all_accounts";
    private static final String METHOD_GET_ACCOUNTS_AMNT = "get_acc_amnt";
    private static final String METHOD_GET_ACCOUNTS_PAGE = "get_acc_page";

    private static final AccountsManager accManager = new AccountsManager(HibernateUtil.getSessionFactory());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        String method = req.getParameter("method");
        JSONObject response = null;
        try {
            if (method == null) {
                response = processWrongMethod(method);
            } else {
                switch (method) {
                    case METHOD_GET_ALL_ACCOUNTS:
                        response = processGetAllAccounts(req);
                        break;
                    case METHOD_GET_ACCOUNTS_AMNT:
                        response = processGetAccountsAmount();
                        break;
                    case METHOD_GET_ACCOUNTS_PAGE:
                        response = processGetAccountsPage(req);
                        break;
                    default:
                        response = processWrongMethod(method);
                        break;
                }
            }
        } catch (Exception e) {

        }
        PrintWriter out = resp.getWriter();
        out.print(response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        String method = req.getParameter("method");
        JSONObject response = null;
        try {
            if (method == null) {
                response = processWrongMethod(method);
            } else {
                switch (method) {
                    case METHOD_CREATE:
                        response = processCreateMethod(req);
                        break;
                    case METHOD_ADD_CASH:
                        response = processAddCashMethod(req);
                        break;
                    case METHOD_GET_CASH:
                        response = processGetCashMethod(req);
                        break;
                    case METHOD_TRANSFER_CASH:
                        response = processTransferCashMethod(req);
                        break;
                    case METHOD_DELETE:
                        response = processDeleteMethod(req);
                        break;
                    default:
                        response = processWrongMethod(method);
                        break;
                }
            }
        } catch (Exception e) {

        }
        PrintWriter out = resp.getWriter();
        out.print(response);
    }

    private JSONObject processCreateMethod(HttpServletRequest req) {
        JSONObject json = new JSONObject();
        try {
            AccountsEntity account = accManager.createAccount(req.getParameter("account_num"));
            json.put("account_number", account.getAccountNumber());
            addStatusMessage(json,
                    ERROR_STATUS.OK.getValue(),
                    ERROR_STATUS.OK.getDescription()
            );
        } catch (Exception e) {
            addStatusMessage(
                    json,
                    ERROR_STATUS.ERROR.getValue(),
                    Arrays.toString(e.getStackTrace())
            );
        }
        return json;
    }

    private JSONObject processAddCashMethod(HttpServletRequest req) {
        JSONObject json = new JSONObject();
        try {
            String accountNum = req.getParameter("account_num");
            BigDecimal cash = new BigDecimal(req.getParameter("cash"));
            accManager.addCash(accountNum, cash);
            addStatusMessage(json,
                    ERROR_STATUS.OK.getValue(),
                    ERROR_STATUS.OK.getDescription()
            );
        } catch (Exception e) {
            addStatusMessage(
                    json,
                    ERROR_STATUS.ERROR.getValue(),
                    Arrays.toString(e.getStackTrace())
            );
        }
        return json;
    }

    private JSONObject processGetCashMethod(HttpServletRequest req) {
        JSONObject json = new JSONObject();
        try {
            String accountNum = req.getParameter("account_num");
            BigDecimal cash = new BigDecimal(req.getParameter("cash"));
            accManager.getCash(accountNum, cash);
            addStatusMessage(json,
                    ERROR_STATUS.OK.getValue(),
                    ERROR_STATUS.OK.getDescription()
            );
        } catch (AccountingException e) {
            addStatusMessage(
                    json,
                    ERROR_STATUS.ERROR.getValue(),
                    e.getMessage()
            );
        } catch (Exception e) {
            addStatusMessage(
                    json,
                    ERROR_STATUS.ERROR.getValue(),
                    Arrays.toString(e.getStackTrace())
            );
        }
        return json;
    }

    private JSONObject processTransferCashMethod(HttpServletRequest req) {
        JSONObject json = new JSONObject();
        try {
            String fromAccountNum = req.getParameter("from_account_num");
            String toAccountNum = req.getParameter("to_account_num");
            BigDecimal cash = new BigDecimal(req.getParameter("cash"));
            accManager.transferCash(fromAccountNum, toAccountNum, cash);
            addStatusMessage(json,
                    ERROR_STATUS.OK.getValue(),
                    ERROR_STATUS.OK.getDescription()
            );
        } catch (Exception e) {
            addStatusMessage(
                    json,
                    ERROR_STATUS.ERROR.getValue(),
                    e.getMessage()
            );
        }
        return json;
    }

    private JSONObject processWrongMethod(String method) {
        JSONObject json = new JSONObject();
        addStatusMessage(json,
                ERROR_STATUS.WRONG_METHOD.getValue(),
                ERROR_STATUS.WRONG_METHOD.getDescription()
        );
        return json;
    }

    private JSONObject processGetAllAccounts(HttpServletRequest req) {
        JSONObject json = new JSONObject();
        List list = accManager.getAllAccounts();
        long count = accManager.countAccounts(null);
        json.put("amount", count);
        json.put("items", list);
        addStatusMessage(json,
                ERROR_STATUS.OK.getValue(),
                ERROR_STATUS.OK.getDescription()
        );
        return json;
    }

    private JSONObject processGetAccountsAmount() {
        JSONObject json = new JSONObject();
        long count = accManager.countAccounts(null);
        json.put("amount", count);
        addStatusMessage(json,
                ERROR_STATUS.OK.getValue(),
                ERROR_STATUS.OK.getDescription()
        );
        return json;
    }

    private JSONObject processGetAccountsPage(HttpServletRequest req) {
        JSONObject json = new JSONObject();
        try {
            int pageSize = Integer.valueOf(req.getParameter("page_size"));
            int pageNum = Integer.valueOf(req.getParameter("page_num"));
            String accountNumFilter = req.getParameter("account_num_filter");
            System.out.println(accountNumFilter);
            List list = accManager.getAccountPage(pageSize, pageNum, accountNumFilter);
            long count = accManager.countAccounts(accountNumFilter);
            json.put("page_size", pageSize);
            json.put("page_num", pageNum);
            json.put("amount_on_page", list.size());
            json.put("amount", count);
            json.put("items", list);
            addStatusMessage(json,
                    ERROR_STATUS.OK.getValue(),
                    ERROR_STATUS.OK.getDescription()
            );
        } catch (Exception e) {
            addStatusMessage(
                    json,
                    ERROR_STATUS.ERROR.getValue(),
                    e.getMessage()
            );
        }
        return json;
    }

    private JSONObject processDeleteMethod(HttpServletRequest req) {
        JSONObject json = new JSONObject();
        try {
            String accountNum = req.getParameter("account_num");
            accManager.deleteAccount(accountNum);
            addStatusMessage(json,
                    ERROR_STATUS.OK.getValue(),
                    ERROR_STATUS.OK.getDescription()
            );
        } catch (HibernateException e) {
            addStatusMessage(
                    json,
                    ERROR_STATUS.ERROR.getValue(),
                    e.getMessage()
            );
        } catch (Exception e) {
            addStatusMessage(
                    json,
                    ERROR_STATUS.ERROR.getValue(),
                    Arrays.toString(e.getStackTrace())
            );
        }
        return json;
    }

    private void addStatusMessage(JSONObject json, int code, String message ) {
        json.put("status", code);
        json.put("error_message", message);
    }

}
