package org.example;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class EventProcessor extends Thread {
    private final AtomicBoolean isPoisonFound;
    private final UniqueEventsQueue<Event> queue;

    public EventProcessor(AtomicBoolean isPoisonFound, UniqueEventsQueue<Event> queue) {
        this.isPoisonFound = isPoisonFound;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (!isPoisonFound.get()) {
            try {
                Helper.peekPoll(queue);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }
}
