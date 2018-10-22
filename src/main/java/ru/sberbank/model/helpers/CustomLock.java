package ru.sberbank.model.helpers;

public class CustomLock {

    private int counter = 0;
    private String key = null;

    public CustomLock(String key) {
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
