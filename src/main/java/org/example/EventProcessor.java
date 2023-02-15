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
        Event event;
        while (!isPoisonFound.get()) {
            try {
                event = Helper.peekPoll(queue, this);
                print(event);
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }

    private synchronized void print(Event event) {
        if (event != null) {
            System.out.printf("Thread %s successfully finished %s with key %d%n", this.getName(), event.getMessage(), event.getId());
        }
    }


}
