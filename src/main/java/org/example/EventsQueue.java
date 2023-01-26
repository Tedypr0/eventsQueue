package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map.Entry;

class EventsQueue<T> {
    private final ConcurrentHashMap<T, T> uniqueValuesMap;
    private Map<T, T> queue = new LinkedHashMap<>();
    private int size;
    private final int capacity;

    public EventsQueue(int capacity) {
        this.capacity = capacity;
        this.uniqueValuesMap = new ConcurrentHashMap<>();
        queue = Collections.synchronizedMap(queue);
    }

    public synchronized void add(T element) throws InterruptedException {
        while (queue.size() == capacity) {
            wait();
        }
        uniqueValuesMap.put(element, element);
        queue.put(element, element);
        size++;
        notifyAll();
    }

    public synchronized T poll() throws InterruptedException {
        while (queue.size() == 0) {
            wait();
        }
        Entry<T, T> tempEntry = getFirstEntry();
        if (tempEntry != null) {
            T elementToRemove = queue.get(tempEntry.getKey());
            queue.remove(tempEntry.getKey());
            if (!queue.containsKey(tempEntry.getKey())) {
                uniqueValuesMap.remove(tempEntry.getKey());
            }
            size--;
            notify();
            return elementToRemove;
        }
        return null;
    }

    public synchronized T peek() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        Entry<T, T> entry = getFirstEntry();
        if (entry != null) {
            return queue.get(entry.getKey());
        }
        return null;
    }

    public int size() {
        return size;
    }

    public synchronized Collection<T> getUniqueValues() {
        return uniqueValuesMap.values();
    }

    private Entry<T, T> getFirstEntry() {
        for (Entry<T, T> entry : queue.entrySet()) {
            return entry;
        }
        return null;
    }

    public synchronized T removeSpecificElement(T elementToRemove) {
        queue.remove(elementToRemove);
        if (!queue.containsKey(elementToRemove)) {
            uniqueValuesMap.remove(elementToRemove);
        }
        return elementToRemove;
    }
}