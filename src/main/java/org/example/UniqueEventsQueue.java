package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UniqueEventsQueue<T> {
    private final Map<Integer, Queue<T>> storageMap = new ConcurrentHashMap<>();
    private final Map<Integer, T> eventsInQueueMap = new ConcurrentHashMap<>();
    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private int size;

    public synchronized void add(T element) {
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
            queue.add(element);
        }
        size++;
        notifyAll();
    }

    public synchronized T poll() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }

        T element = queue.poll();
        if (element != null) {
            if (storageMap.containsKey(element.hashCode())) {
                Queue<T> events = storageMap.get(element.hashCode());
                queue.add(events.poll());
            }
            eventsInQueueMap.remove(element.hashCode());
            size--;
            notify();
            return element;
        }
        return null;
    }

    public synchronized T peek() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.peek();
    }
}