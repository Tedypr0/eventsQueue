package org.example;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.Helper.POISON_MESSAGE;

public class EventProcessor extends Thread {
    private final AtomicBoolean isPoisonFound;
    private final UniqueEventsQueue<Event> queue;
    private final ReentrantLock lock;
    private final Map<Integer, ReentrantLock> keys;

    public EventProcessor(AtomicBoolean isPoisonFound, UniqueEventsQueue<Event> queue, ReentrantLock lock, Map<Integer, ReentrantLock> keys) {
        this.isPoisonFound = isPoisonFound;
        this.queue = queue;
        this.lock = lock;
        this.keys = keys;
    }

    @Override
    public void run() {
        while (!isPoisonFound.get()) {
            try {
                synchronized (lock) {
                    int key = queue.peek().getId();
                    ReentrantLock lock = keys.computeIfAbsent(key, k -> new ReentrantLock());
                    lock.lock();
                    try {
                        if (queue.peek().getMessage().equals(POISON_MESSAGE)) {
                            isPoisonFound.set(true);

                        } else {

                            Event event = queue.poll();
                            if (event.hashCode() != Integer.MAX_VALUE) {
                                System.out.println();
                                System.out.println(event.getMessage());
                            }

                        }
                    } finally {
                        lock.unlock();

                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }
}
