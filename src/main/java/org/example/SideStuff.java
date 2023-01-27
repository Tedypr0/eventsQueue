package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SideStuff {
    private static final int THREAD_NUMBER = 4;
    public static final String POISON_MESSAGE = "!@#$%^&*()POISON_PILL";
    static AtomicBoolean isPoisonFound = new AtomicBoolean(false);
    static List<AtomicInteger> threadReferences = new ArrayList<>(THREAD_NUMBER);
    List<EventHandler> threads = new ArrayList<>();
    EventsQueue<Event> eventsQueue = new EventsQueue<>(30);

    public void eventCreation() throws InterruptedException {
        for (int j = 0; j < 10000; j++) {
            for (int i = 0; i < 5; i++) {
                Event event = new Event(String.format("Event %d", i));
                eventsQueue.add(event);
            }
        }

        Event endEvent = new Event(POISON_MESSAGE);
        eventsQueue.add(endEvent);
    }

    public void eventReferencesCreationAndThreadStartup() {
        for (int i = 0; i < THREAD_NUMBER; i++) {
            threadReferences.add(new AtomicInteger(0));
        }

        for (int i = 0; i < THREAD_NUMBER; i++) {
            EventHandler eventHandler = new EventHandler(eventsQueue, i, isPoisonFound, threadReferences);
            threads.add(eventHandler);
            threads.get(i).start();
        }
    }

    public static synchronized Event peekPoll(EventsQueue<Event> queue, EventHandler currentThread) throws InterruptedException {
        Event peekedEvent = queue.peek();
        if (peekedEvent.getMessage().equals(POISON_MESSAGE)) {
            isPoisonFound.set(true);
        } else {
            do {
                boolean isNextEventFound = false;
                boolean isPeekEventFound = false;
                Event savedEvent = null;
                for (Event event : queue.getUniqueValues()) {
                    for (AtomicInteger threadReference : threadReferences) {
                        if (threadReference.get() == peekedEvent.hashCode() && !isPeekEventFound) {
                            isPeekEventFound = true;
                        }
                        if (threadReference.get() == event.hashCode()) {
                            isNextEventFound = true;
                        } else {
                            savedEvent = event;
                        }
                    }
                    //This is dumb. Must be changed.
                    if (!isPeekEventFound) {
                        Event eventToProcess = queue.poll();
                        //  System.out.printf(String.format("Thread: %s has polled task: %s%n", currentThread.getThreadNumber(), eventToProcess.getMessage()));
                        threadReferences.get(currentThread.getThreadNumber()).set(eventToProcess.hashCode());
                        return eventToProcess;
                    } else if (!isNextEventFound) {
                        Event eventToProcess = queue.removeSpecificElement(savedEvent);
                        //   System.out.printf(String.format("Thread: %s has removed specific task: %s%n", currentThread.getThreadNumber(), eventToProcess.getMessage()));
                        threadReferences.get(currentThread.getThreadNumber()).set(eventToProcess.hashCode());
                        return eventToProcess;
                    }
                }
            } while (!queue.peek().getMessage().equals(POISON_MESSAGE));

        }
        return null;
    }
}
