package org.example;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
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
                /* key might be accessed by multiple threads, but a thread will lock this key and
                 * when it gets polled, other threads will see a different object when they call peek()
                 */
                int key = queue.peek().getId();
                ReentrantLock lock = keys.computeIfAbsent(key, k -> new ReentrantLock());
                lock.lock();
                try {
                    if (queue.peek().getMessage().equals(POISON_MESSAGE)) {
                        isPoisonFound.set(true);
                    } else {
                        Event event = queue.poll();
                        if(event==null){
                            return;
                        }
                        if (event.hashCode() != Integer.MAX_VALUE) {
                           // System.out.println();
                            System.out.println(event.getMessage());
                        }

                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }
}
