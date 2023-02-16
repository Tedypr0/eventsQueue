package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class Helper {
    private static final String POISON_MESSAGE = "POISON_MESSAGE";
    private static final int THREAD_NUMBER = 4;
    private static AtomicBoolean isPoisonFound = null;
    private static UniqueEventsQueue<Event> queue = null;
    private static final List<EventProcessor> threads = new ArrayList<>();
    private static final Map<Integer, ReentrantLock> keys = new ConcurrentHashMap<>();

    public Helper() {
        isPoisonFound = new AtomicBoolean(false);
        queue = new UniqueEventsQueue<>();
    }

    public void eventCreation() {
        int i = 0;
        for (int j = 0; j < 100; j++) {
            if (j % 5 == 0) {
                i++;
            }
            queue.add(new Event(i, String.format("Event %d %d", i, j)));
        }
        queue.add(new Event(Integer.MAX_VALUE, POISON_MESSAGE));
    }

    public void threadCreation() {
        for (int i = 0; i < THREAD_NUMBER; i++) {
            threads.add(new EventProcessor(isPoisonFound, queue));
            threads.get(i).start();
        }
    }

    public static synchronized void peekPoll(UniqueEventsQueue<Event> queue, EventProcessor currentThread) throws InterruptedException {
        int key = queue.peek().getId();
//         System.out.printf("Checking if thread %s can access %s with key %d%n", currentThread.getName(), queue.peek().getMessage(),key);
//        if(keys.containsKey(key)){
//            System.out.printf("%s cannot work with event %s, because another thread is working with it already! KEY: %d%n", currentThread.getName(), queue.peek().getMessage(), key);
//        }else{
//            System.out.printf("%s took event %s!%n", currentThread.getName(), queue.peek().getMessage());
//        }
        ReentrantLock lock = keys.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            if (queue.peek().getMessage().equals(POISON_MESSAGE)) {
                isPoisonFound.set(true);
           //     System.out.println("IsPoisonFound has been set to TRUE!");
            } else {
                System.out.println();
                Event peek = queue.peek();
                System.out.println(peek.getMessage());
                queue.poll();
            }
        } finally {
            lock.unlock();
         //   System.out.printf("Thread %s unlocked key %d!%n", currentThread.getName(), key);
        }
    }


}
