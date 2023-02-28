package org.example;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.Helper.POISON_MESSAGE;

public class EventProcessor extends Thread {
    private final AtomicBoolean isPoisonFound;
    private final UniqueEventsQueue<Event> queue;

    // These keys are going to lock Queues instead of an Event.
    private final Map<Integer, ReentrantLock> keys;

    public EventProcessor(AtomicBoolean isPoisonFound, UniqueEventsQueue<Event> queue, Map<Integer, ReentrantLock> keys) {
        this.isPoisonFound = isPoisonFound;
        this.queue = queue;
        this.keys = keys;
    }

    @Override
    public void run() {
        while (!isPoisonFound.get()) {
            /* @THOUGHTS
             * Some nested while loop which checks if the queue we acquired is empty. If it is break the loop. (If we won't delete the queues from the other DS).
             * After that have some logic to check if we have a queue which isn't worked on. If there is, acquire it and
             * poll until it's empty. If there isn't one wait until there is one and acquire it. Of course if we have a Queue with POISONPILL, break the thread loops.
             *
             * @PROBLEM How to get an available queue without iteration and at the same time add elements from another thread.
             * @CONCRETE THOUGHTS Keep track of Events in (ArrayList maybe or Queue), which haven't been acquired yet and get Queues
             * from there to poll from.
             *
             * If we use a Queue to keep track of Events it's going to be difficult to add elements. Because we have to iterate
             * the first Queue and acquire the ref to the Queue we need and add elements then (STUPID). Perhaps in another DS keep ref to the Queue we want to add.
             *
             * Set/Map to keep track of Elements workerQueue. workerQueue is going to just keep refs to Queue<Queue<Event> .. NO
             * Queue<Queue<Event> poll is going to remove the queue and when we want to add an Event with the same key, like the queue we pulled from... what? We create a new Queue? DUMB.
             *
             */


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
