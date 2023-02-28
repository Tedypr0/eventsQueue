package org.example;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;;
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
                Event event = queue.poll();
                try {
                    isPoisonFound.set(event.getMessage().equals(POISON_MESSAGE));
                    key = event.getId();
                    lock = keys.computeIfAbsent(key, k -> new ReentrantLock());
                    lock.lock();
                    System.out.printf("%s locked key: %d%n", this.getName(), key);
                } finally {
                    assert lock != null;
                    /* Sleep is for testing purposes. We may have Events with different processing times.
                     * With sleep() we can check if all of our threads will get an event to work with from the queue.
                     */

                    sleep(50);
                    System.out.printf("%s unlocked key: %d%n", this.getName(), key);
                    lock.unlock();

                }
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }
}
