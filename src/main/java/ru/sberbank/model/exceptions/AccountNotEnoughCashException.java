package ru.sberbank.model.exceptions;

public class AccountNotEnoughCashException extends AccountingException {

    public AccountNotEnoughCashException() {
        super("not enough cash!");
    }

}
