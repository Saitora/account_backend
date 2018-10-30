package ru.sberbank.json;

import org.json.JSONObject;

public class JSONResponse extends JSONObject {

    public JSONResponse setErrorCode(int errorCode) {
        this.put("error", errorCode);
        return this;
    }

    public JSONResponse setErrorMessage(String errorMessage) {
        this.put("error_message", errorMessage);
        return this;
    }

}