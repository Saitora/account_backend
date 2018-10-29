package ru.sberbank;

import org.json.JSONObject;
import ru.sberbank.model.OperationsManager;
import ru.sberbank.model.helpers.HibernateUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ServletOperation extends HttpServlet {

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

    //Get
    private static final String METHOD_GET_ALL_OPERATIONS = "get_all_operations";
    private static final String METHOD_GET_OPERATIONS_AMNT = "get_operations_amnt";
    private static final String METHOD_GET_OPERATIONS_PAGE = "get_operations_page";

    private static final OperationsManager opManager = new OperationsManager(HibernateUtil.getSessionFactory());

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
                    case METHOD_GET_ALL_OPERATIONS:
                        response = processGetAllOperations(req);
                        break;
                    case METHOD_GET_OPERATIONS_AMNT:
                        response = processGetOperationsAmount();
                        break;
                    case METHOD_GET_OPERATIONS_PAGE:
                        response = processGetOperationsPage(req);
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

    private JSONObject processGetAllOperations(HttpServletRequest req) {
        JSONObject json = new JSONObject();
        List list = opManager.getAllOperations();
        long count = opManager.countOperations();
        json.put("amount", count);
        json.put("items", list);
        addStatusMessage(json,
                ServletAccount.ERROR_STATUS.OK.getValue(),
                ServletAccount.ERROR_STATUS.OK.getDescription()
        );
        return json;
    }

    private JSONObject processGetOperationsAmount() {
        JSONObject json = new JSONObject();
        long count = opManager.countOperations();
        json.put("amount", count);
        addStatusMessage(json,
                ServletAccount.ERROR_STATUS.OK.getValue(),
                ServletAccount.ERROR_STATUS.OK.getDescription()
        );
        return json;
    }

    private JSONObject processGetOperationsPage(HttpServletRequest req) {
        JSONObject json = new JSONObject();
        try {
            int pageSize = Integer.valueOf(req.getParameter("page_size"));
            int pageNum = Integer.valueOf(req.getParameter("page_num"));
            List list = opManager.getOperationsPage(pageSize, pageNum);
            long count = opManager.countOperations();
            json.put("page_size", pageSize);
            json.put("page_num", pageNum);
            json.put("amount_on_page", list.size());
            json.put("amount", count);
            json.put("items", list);
            addStatusMessage(json,
                    ServletAccount.ERROR_STATUS.OK.getValue(),
                    ServletAccount.ERROR_STATUS.OK.getDescription()
            );
        } catch (Exception e) {
            addStatusMessage(
                    json,
                    ServletAccount.ERROR_STATUS.ERROR.getValue(),
                    e.getMessage()
            );
        }
        return json;
    }

    private JSONObject processWrongMethod(String method) {
        JSONObject json = new JSONObject();
        addStatusMessage(json,
                ServletAccount.ERROR_STATUS.WRONG_METHOD.getValue(),
                ServletAccount.ERROR_STATUS.WRONG_METHOD.getDescription()
        );
        return json;
    }

    private void addStatusMessage(JSONObject json, int code, String message ) {
        json.put("status", code);
        json.put("error_message", message);
    }

}
