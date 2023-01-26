package org.example;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class EventHandler extends Thread {
    private final EventsQueue<Event> eventsQueue;
    private final int threadNumber;
    static AtomicBoolean isPoisonFound = null;
    private List<AtomicInteger> references;

    public EventHandler(EventsQueue<Event> eventsQueue, int threadNumber, AtomicBoolean isPoisonFound, List<AtomicInteger> references) {
        this.eventsQueue = eventsQueue;
        this.threadNumber = threadNumber;
        EventHandler.isPoisonFound = isPoisonFound;
        this.references = references;
    }

    @Override
    public void run() {
        while (!isPoisonFound.get()) {
            try {
                Event event = SideStuff.peekPoll(eventsQueue, this);
                if (event != null) {
                   System.out.printf(String.format("Event message: %s processed by thread %d%n", event.getMessage(), threadNumber));
                    references.get(threadNumber).set(0);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }

    @Override
    public int hashCode() {
        int prime = 31;
        return prime + ((this.getName() == null) ? 0 : this.getName().hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EventHandler)) {
            return false;
        }
        EventHandler eventHandler = new EventHandler(eventsQueue, threadNumber, new AtomicBoolean(false), references);
        return (eventHandler.getName().equals(this.getName()));
    }

    public int getThreadNumber() {
        return threadNumber;
    }
}
