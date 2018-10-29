package ru.sberbank.model.helpers;

import org.json.JSONObject;

public class JSONResponse extends JSONObject {

    private int errorCode = 0;
    private String errorMessage = "";

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public JSONResponse generateJSON() {
        this.put("error", errorCode);
        this.put("error_message", errorMessage);
        return this;
    }
}
