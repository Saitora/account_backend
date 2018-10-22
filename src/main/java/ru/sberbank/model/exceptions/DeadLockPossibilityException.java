package ru.sberbank.model.exceptions;

public class DeadLockPossibilityException extends AccountingException {

    public DeadLockPossibilityException() {
        super("possible dead lock!");
    }

}
