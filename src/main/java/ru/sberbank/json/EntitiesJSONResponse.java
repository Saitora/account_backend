package ru.sberbank.json;

import java.util.List;

public class EntitiesJSONResponse extends JSONResponse {

    public EntitiesJSONResponse setPageSize(int pageSize) {
        this.put("page_size", pageSize);
        return this;
    }

    public EntitiesJSONResponse setPageNum(int pageNum) {
        this.put("page_num", pageNum);
        return this;
    }

    public EntitiesJSONResponse setAmountOnPage(int amountOnPage) {
        this.put("amount_on_page", amountOnPage);
        return this;
    }

    public EntitiesJSONResponse setAmount(long amount) {
        this.put("amount", amount);
        return this;
    }

    public EntitiesJSONResponse setEntitiesList(List entitiesList) {
        this.put("items", entitiesList);
        return this;
    }

}
