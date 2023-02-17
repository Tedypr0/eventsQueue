package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UniqueEventsQueue<T> {
    private final Map<Integer, Queue<T>> storageMap = new ConcurrentHashMap<>();
    private final Map<Integer, T> eventsInQueueMap = new ConcurrentHashMap<>();
    private final Queue<T> workerQueue = new ConcurrentLinkedQueue<>();
    private int size;

    public synchronized void add(T element) {
        if (element.hashCode() == Integer.MAX_VALUE) {
            Queue<T> finalElement = new ConcurrentLinkedQueue<>();
            finalElement.add(element);
            storageMap.put(element.hashCode(), finalElement);
        } else {
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
            } else {
                eventsInQueueMap.put(element.hashCode(), element);
                workerQueue.add(element);
            }
        }
        size++;
        notifyAll();
    }

    public synchronized T poll() throws InterruptedException {
        while (workerQueue.isEmpty()) {
            wait();
        }
        // If hashmap has only POISON element. Add it to queue.

        T element = workerQueue.peek();
        if (element != null) {
            if (storageMap.size() == 1 && storageMap.containsKey(Integer.MAX_VALUE)) {
                Queue<T> poisonEventQueue = storageMap.remove(Integer.MAX_VALUE);
                T poisonEvent = poisonEventQueue.poll();
                workerQueue.add(poisonEvent);
                size--;
                notify();
                return poisonEvent;
            } else {
                boolean doesStorageMapContainKey = storageMap.containsKey(element.hashCode());
                Queue<T> queueToCheck = storageMap.get(element.hashCode());
                if (doesStorageMapContainKey && !queueToCheck.isEmpty()) {
                    Queue<T> events = storageMap.get(element.hashCode());
                    T elementFromStorage = events.poll();
                    workerQueue.add(elementFromStorage);
                    if (events.size() == 0 && elementFromStorage!=null) {
                        storageMap.remove(elementFromStorage.hashCode());
                    }
                    size--;
                    notify();
                    return workerQueue.poll();
                }

            }
            size--;
            notify();
            return workerQueue.poll();
        }

        return null;
    }

    public synchronized T peek() throws InterruptedException {
        while (workerQueue.isEmpty()) {
            wait();
        }
        return workerQueue.peek();
    }
}