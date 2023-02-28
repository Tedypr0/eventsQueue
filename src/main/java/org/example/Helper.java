package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Helper {
    static final String POISON_MESSAGE = "POISON_MESSAGE";
    private static final int THREAD_NUMBER = 4;
    private static AtomicBoolean isPoisonFound = null;
    private static UniqueEventsQueue<Event> queue = null;
    private final List<EventProcessor> threads = new ArrayList<>();
    private static final Map<Integer, ReentrantLock> keys = new ConcurrentHashMap<>();

    public Helper() {
        isPoisonFound = new AtomicBoolean(false);
        Queue<Event> workerQueue = new ConcurrentLinkedQueue<>();
        queue = new UniqueEventsQueue<>(workerQueue);
    }

    public void eventCreation() {
            for (int j = 0; j <= 1000; j++) {
                queue.add(new Event(0, String.format("Event %d %d", 0, j)));
            }

            for(int i = 1; i< 5; i++){
                for (int j = 0; j< 100; j++){
                    queue.add(new Event(i, String.format("Event %d %d", i, j)));
                }
            }
        queue.add(new Event(Integer.MAX_VALUE, POISON_MESSAGE));
    }

    public void threadCreation() {
        for (int i = 0; i < THREAD_NUMBER; i++) {
            threads.add(new EventProcessor(isPoisonFound, queue, keys));
            threads.get(i).start();
        }
    }
}
