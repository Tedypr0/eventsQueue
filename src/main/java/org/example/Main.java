package org.example;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Helper helper = new Helper();
        helper.eventCreation();
        Thread.sleep(500);
        helper.threadCreation();
    }
}
