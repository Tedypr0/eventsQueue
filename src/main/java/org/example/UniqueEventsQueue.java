package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UniqueEventsQueue<T> {
    private final Queue<T> queue;
    private int size;

    public UniqueEventsQueue() {
        queue = new ConcurrentLinkedQueue<>();
    }

    public synchronized void add(T element) {
        queue.add(element);
        size++;
        notifyAll();
    }

    public synchronized T poll() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        size--;
        notify();
        return queue.poll();
    }

    public synchronized T peek() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.peek();
    }

}