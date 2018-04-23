package ru.ifmo.rain.golovin.crawler;

public class CounterLock {

    private Integer val;

    public CounterLock(int i) {
        val = i;
    }

    public synchronized void inc() {
        val++;
    }

    public synchronized void dec() {
        val--;
        if (val == 0) {
            notifyAll();
        }
    }

    public void awaitZero() throws InterruptedException {
        val.wait();
    }

}
