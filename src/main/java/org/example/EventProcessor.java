package org.example;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.Helper.POISON_MESSAGE;

public class EventProcessor extends Thread {
    private final AtomicBoolean isPoisonFound;
    private final UniqueEventsQueue<Event> queue;
    private final Map<Integer, ReentrantLock> keys;
    private final Object syncLock;
    public EventProcessor(AtomicBoolean isPoisonFound, UniqueEventsQueue<Event> queue, Map<Integer, ReentrantLock> keys, Object syncLock) {
        this.isPoisonFound = isPoisonFound;
        this.queue = queue;
        this.keys = keys;
        this.syncLock = syncLock;
    }

    @Override
    public void run() {
        while (!isPoisonFound.get()) {
            try {
                /* key might be accessed by multiple threads, but a thread will lock this key and
                 * when it gets polled, other threads will see a different object when they call peek()
                 */
                ReentrantLock lock = null;
                int key = 0;
                Event event;
                try {
                    synchronized (syncLock) {
                        key = queue.peek().getId();
                        lock = keys.computeIfAbsent(key, k -> new ReentrantLock());
                        lock.lock();
                        System.out.printf("%s locked key: %d%n", this.getName(), key);
                        isPoisonFound.set(queue.peek().getMessage().equals(POISON_MESSAGE));
                        event = queue.poll();
                    }
                    if (event != null) {
                        boolean isPoisonFound = event.hashCode() == Integer.MAX_VALUE;
                        if (!isPoisonFound) {
                            System.out.printf("%s processed %s with key %d%n", this.getName(), event.getMessage(), event.getId());
                        }
                    }
                } finally {
                    assert lock != null;
                    lock.unlock();
                    System.out.printf("%s unlocked key: %d%n",this.getName(), key);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }
}
