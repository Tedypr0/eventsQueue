package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UniqueEventsQueue<T> {
    /*
     * The idea behind eventsInQueueMap is to mirror all events that are in the workerQueue,
     * so we know when an Event has been accepted by a Thread, so we can add an Event with the same key to the queue.
     */
    private final Map<Integer, Queue<T>> storageMap = new ConcurrentHashMap<>();
    private final Map<Integer, T> eventsInQueueMap = new ConcurrentHashMap<>();
    private final Queue<T> workerQueue;
    private volatile int size;

    public UniqueEventsQueue(Queue<T> workerQueue) {
        this.workerQueue = workerQueue;
    }

    /*
     *  Check if an Event is inside the queue. If it IS add it to the storage, if not add it to the workerQueue and eventsInQueueMap.
     */

    public synchronized void add(T element) {
        if (element.hashCode() == Integer.MAX_VALUE) {
            Queue<T> poisonQueue = new ConcurrentLinkedQueue<>();
            poisonQueue.add(element);
            storageMap.put(element.hashCode(), poisonQueue);
            size++;
            notifyAll();
            return;
        }

        if (eventsInQueueMap.containsKey(element.hashCode())) {
            if (storageMap.containsKey(element.hashCode())) {
                Queue<T> list = storageMap.get(element.hashCode());
                list.add(element);
                storageMap.put(element.hashCode(), list);
            } else {
                Queue<T> list = new ConcurrentLinkedQueue<>();
                list.add(element);
                storageMap.put(element.hashCode(), list);
            }
            size++;
            notifyAll();
        } else {
            eventsInQueueMap.put(element.hashCode(), element);
            workerQueue.add(element);
        }

    }

    public synchronized T poll() throws InterruptedException {
        while (workerQueue.isEmpty()) {
            wait();
        }

        T element = workerQueue.peek();

        if (size == 0 && storageMap.containsKey(Integer.MAX_VALUE)) {
            workerQueue.add(storageMap.get(Integer.MAX_VALUE).peek());
        } else {

            if (element != null) {
                Queue<T> queueToCheck = storageMap.get(element.hashCode());
                if (queueToCheck != null && !queueToCheck.isEmpty()) {
                    T elementFromStorage = queueToCheck.poll();
                    workerQueue.add(elementFromStorage);
                } else {
                    eventsInQueueMap.remove(element.hashCode());
                }
            }
            size--;
            notify();

            return workerQueue.poll();
        }

        // Will return null every time the workerQueue has the last element.
        return null;
    }

    public synchronized T peek() throws InterruptedException {
        while (workerQueue.isEmpty()) {
            wait();
        }
        return workerQueue.peek();
    }
}