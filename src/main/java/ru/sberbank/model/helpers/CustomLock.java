package ru.sberbank.model.helpers;

import java.util.concurrent.locks.ReentrantLock;

public class CustomLock extends ReentrantLock {

    private int counter = 0;
    private String key = null;

    public CustomLock(String key) {
        super();
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public int getCounter() {
        return counter;
    }

    public void incCounter() {
        counter++;
    }

    public void decCounter() {
        counter--;
    }

}
