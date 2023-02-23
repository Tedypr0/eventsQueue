package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class Helper {
    static final String POISON_MESSAGE = "POISON_MESSAGE";
    private static final int THREAD_NUMBER = 4;
    private static AtomicBoolean isPoisonFound = null;
    private static UniqueEventsQueue<Event> queue = null;
    private final List<EventProcessor> threads = new ArrayList<>();
    private static final Map<Integer, ReentrantLock> keys = new ConcurrentHashMap<>();
    private static final Object syncLock = new Object();

    public Helper() {
        isPoisonFound = new AtomicBoolean(false);
        Queue<Event> workerQueue = new ConcurrentLinkedQueue<>();
        queue = new UniqueEventsQueue<>(workerQueue);
    }

    public void eventCreation() {
        for (int i = 0; i <= 3; i++) {
            for (int j = 0; j <= 3; j++) {
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

    public static void peekPoll(EventProcessor thread) throws InterruptedException{
        ReentrantLock lock = null;
        int key = 0;
        Event event;
        try {
            synchronized (syncLock) {
                key = queue.peek().getId();
                lock = keys.computeIfAbsent(key, k -> new ReentrantLock());
                lock.lock();
                System.out.printf("%s locked key: %d%n", thread.getName(), key);
                isPoisonFound.set(queue.peek().getMessage().equals(POISON_MESSAGE));
                event = queue.poll();
            }
                if (event != null) {
                    boolean isPoisonFound = event.hashCode() == Integer.MAX_VALUE;
                    if (!isPoisonFound) {
                        System.out.printf("%s processed %s with key %d%n", thread.getName(), event.getMessage(), event.getId());
                    }
                }

        } finally {
            assert lock != null;
            lock.unlock();
            System.out.printf("%s unlocked key: %d%n",thread.getName(), key);
        }
    }
}
