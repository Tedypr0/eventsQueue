package org.example;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.Helper.POISON_MESSAGE;

public class EventProcessor extends Thread {
    private final AtomicBoolean isPoisonFound;
    private final UniqueEventsQueue<Event> queue;
    private final Map<Integer, ReentrantLock> keys;
    public EventProcessor(AtomicBoolean isPoisonFound, UniqueEventsQueue<Event> queue, Map<Integer, ReentrantLock> keys) {
        this.isPoisonFound = isPoisonFound;
        this.queue = queue;
        this.keys = keys;
    }

    @Override
    public void run() {
        while (!isPoisonFound.get()) {
            try {
                ReentrantLock lock = null;
                int key = 0;
                isPoisonFound.set(queue.peek().getMessage().equals(POISON_MESSAGE));
                Event event;
                try {
                    /*
                     * The idea of the synchronized block is to verify that locking events will be done only by 1 thread,
                     * at a time. When an Event is polled, another thread can poll too if the lock is not being used.
                     */
                    key = queue.peek().getId();
                    lock = keys.computeIfAbsent(key, k -> new ReentrantLock());
                    lock.lock();
                    System.out.printf("%s locked key: %d%n", this.getName(), key);

                    queue.poll();
                } finally {
                    assert lock != null;
                   System.out.printf("%s unlocked key: %d%n", this.getName(), key);
                    lock.unlock();

                }
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }
}
