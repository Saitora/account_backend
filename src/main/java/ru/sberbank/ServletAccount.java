package ru.sberbank;

import org.hibernate.HibernateException;
import org.json.JSONObject;
import ru.sberbank.json.EntitiesJSONResponse;
import ru.sberbank.json.JSONResponse;
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
        WRONG_METHOD(1, "method not supported"),
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
        JSONResponse json = new JSONResponse();
        try {
            AccountsEntity account = accManager.createAccount(req.getParameter("account_num"));
            json.setErrorCode(ERROR_STATUS.OK.getValue())
                    .setErrorMessage(ERROR_STATUS.OK.getDescription())
                    .put("account_number", account.getAccountNumber());
        } catch (Exception e) {
            json.setErrorCode(ERROR_STATUS.ERROR.getValue())
                    .setErrorMessage(Arrays.toString(e.getStackTrace()));
        }
        return json;
    }

    private JSONObject processAddCashMethod(HttpServletRequest req) {
        JSONResponse json = new JSONResponse();
        try {
            String accountNum = req.getParameter("account_num");
            BigDecimal cash = new BigDecimal(req.getParameter("cash"));
            accManager.addCash(accountNum, cash);
            json.setErrorCode(ERROR_STATUS.OK.getValue())
                    .setErrorMessage(ERROR_STATUS.OK.getDescription());
        } catch (Exception e) {
            json.setErrorCode(ERROR_STATUS.ERROR.getValue())
                    .setErrorMessage(Arrays.toString(e.getStackTrace()));
        }
        return json;
    }

    private JSONObject processGetCashMethod(HttpServletRequest req) {
        JSONResponse json = new JSONResponse();
        try {
            String accountNum = req.getParameter("account_num");
            BigDecimal cash = new BigDecimal(req.getParameter("cash"));
            accManager.getCash(accountNum, cash);
            json.setErrorCode(ERROR_STATUS.OK.getValue())
                    .setErrorMessage(ERROR_STATUS.OK.getDescription());
        } catch (AccountingException e) {
            json.setErrorCode(ERROR_STATUS.ERROR.getValue())
                    .setErrorMessage(e.getMessage());
        } catch (Exception e) {
            json.setErrorCode(ERROR_STATUS.ERROR.getValue())
                    .setErrorMessage(Arrays.toString(e.getStackTrace()));
        }
        return json;
    }

    private JSONObject processTransferCashMethod(HttpServletRequest req) {
        JSONResponse json = new JSONResponse();
        try {
            String fromAccountNum = req.getParameter("from_account_num");
            String toAccountNum = req.getParameter("to_account_num");
            BigDecimal cash = new BigDecimal(req.getParameter("cash"));
            accManager.transferCash(fromAccountNum, toAccountNum, cash);
            json.setErrorCode(ERROR_STATUS.OK.getValue())
                    .setErrorMessage(ERROR_STATUS.OK.getDescription());
        } catch (Exception e) {
            json.setErrorCode(ERROR_STATUS.ERROR.getValue())
                    .setErrorMessage(e.getMessage());
        }
        return json;
    }

    private JSONObject processWrongMethod(String method) {
        JSONResponse json = new JSONResponse();
        json.setErrorCode(ERROR_STATUS.WRONG_METHOD.getValue())
                .setErrorMessage(ERROR_STATUS.WRONG_METHOD.getDescription());
        return json;
    }

    private JSONObject processGetAllAccounts(HttpServletRequest req) {
        EntitiesJSONResponse json = new EntitiesJSONResponse();
        List list = accManager.getAllAccounts();
        long count = accManager.countAccounts(null);
        json.setAmount(count)
                .setEntitiesList(list)
                .setErrorCode(ERROR_STATUS.OK.getValue())
                .setErrorMessage(ERROR_STATUS.OK.getDescription());
        return json;
    }

    private JSONObject processGetAccountsAmount() {
        EntitiesJSONResponse json = new EntitiesJSONResponse();
        long count = accManager.countAccounts(null);
        json.setAmount(count)
                .setErrorCode(ERROR_STATUS.OK.getValue())
                .setErrorMessage(ERROR_STATUS.OK.getDescription());
        return json;
    }

    private JSONObject processGetAccountsPage(HttpServletRequest req) {
        EntitiesJSONResponse json = new EntitiesJSONResponse();
        try {
            int pageSize = Integer.valueOf(req.getParameter("page_size"));
            int pageNum = Integer.valueOf(req.getParameter("page_num"));
            String accountNumFilter = req.getParameter("account_num_filter");
            List list = accManager.getAccountPage(pageSize, pageNum, accountNumFilter);
            long count = accManager.countAccounts(accountNumFilter);
            json.setPageSize(pageSize)
                    .setPageNum(pageNum)
                    .setAmountOnPage(list.size())
                    .setAmount(count)
                    .setEntitiesList(list)
                    .setErrorCode(ERROR_STATUS.OK.getValue())
                    .setErrorMessage(ERROR_STATUS.OK.getDescription());
        } catch (Exception e) {
            json.setErrorCode(ERROR_STATUS.ERROR.getValue())
                    .setErrorMessage(e.getMessage());
        }
        return json;
    }

    private JSONObject processDeleteMethod(HttpServletRequest req) {
        JSONResponse json = new JSONResponse();
        try {
            String accountNum = req.getParameter("account_num");
            accManager.deleteAccount(accountNum);
            json.setErrorCode(ERROR_STATUS.OK.getValue())
                    .setErrorMessage(ERROR_STATUS.OK.getDescription());
        } catch (HibernateException e) {
            json.setErrorCode(ERROR_STATUS.ERROR.getValue())
                    .setErrorMessage(e.getMessage());
        } catch (Exception e) {
            json.setErrorCode(ERROR_STATUS.ERROR.getValue())
                    .setErrorMessage(Arrays.toString(e.getStackTrace()));
        }
        return json;
    }

}
