package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int THREAD_NUMBER = 4;
    public static final String POISON_MESSAGE = "!@#$%^&*()POISON_PILL";
    static AtomicBoolean isPoisonFound = new AtomicBoolean(false);
    static List<AtomicInteger> threadReferences = new ArrayList<>(THREAD_NUMBER);

    public static void main(String[] args) throws InterruptedException {
        EventsQueue<Event> eventsQueue = new EventsQueue<>(30);
        List<EventHandler> threads = new ArrayList<>();
        for (int i = 0; i < THREAD_NUMBER; i++) {
            threadReferences.add(new AtomicInteger(0));
        }

        for (int i = 0; i < THREAD_NUMBER; i++) {
            EventHandler eventHandler = new EventHandler(eventsQueue, i, isPoisonFound, threadReferences);

            threads.add(eventHandler);
            threads.get(i).start();
        }
        for(int j = 0; j< 5; j++) {
            for (int i = 0; i < 5; i++) {
                Event event = new Event(String.format("Event %d", i));
                eventsQueue.add(event);
            }
        }

        Event endEvent = new Event(POISON_MESSAGE);
        eventsQueue.add(endEvent);
       Thread.sleep(1000);
        System.out.println();
    }

    public static synchronized Event peekPoll(EventsQueue<Event> queue, EventHandler currentThread) throws InterruptedException {
        if (queue.peek().getMessage().equals(Main.POISON_MESSAGE)) {
            isPoisonFound.set(true);
        } else {
            boolean isFound = false;
            for (AtomicInteger threadReference : threadReferences) {
                if (threadReference.get() == queue.peek().hashCode()) {
                    isFound = true;
                }
            }
            if (!isFound) {
                Event eventToProcess = queue.poll();
                System.out.printf(String.format("Thread: %s has polled task: %s%n",currentThread.getThreadNumber(), eventToProcess.getMessage()));
                threadReferences.get(currentThread.getThreadNumber()).set(eventToProcess.hashCode());
                return eventToProcess;
            }
        }
        return null;
    }
}