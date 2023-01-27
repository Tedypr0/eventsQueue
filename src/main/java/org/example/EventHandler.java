package org.example;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class EventHandler extends Thread {
    private final EventsQueue<Event> eventsQueue;
    private final int threadNumber;
    static AtomicBoolean isPoisonFound = null;
    private final List<AtomicInteger> references;
    private final SideStuff sideStuffLogic;

    public EventHandler(EventsQueue<Event> eventsQueue, int threadNumber, AtomicBoolean isPoisonFound, List<AtomicInteger> references, SideStuff sideStuffLogic) {
        this.eventsQueue = eventsQueue;
        this.threadNumber = threadNumber;
        EventHandler.isPoisonFound = isPoisonFound;
        this.references = references;
        this.sideStuffLogic = sideStuffLogic;
    }

    @Override
    public void run() {
        while (!isPoisonFound.get()) {
            try {
                Event event = sideStuffLogic.peekPoll(eventsQueue, this);
                if (event != null) {
                    System.out.printf(String.format("Event message: %s processed by thread %d%n", event.getMessage(), threadNumber));
                    references.get(threadNumber).set(0);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }

    public int getThreadNumber() {
        return threadNumber;
    }
}
