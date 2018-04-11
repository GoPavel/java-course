package ru.ifmo.rain.golovin.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ParallelMapperImpl implements ParallelMapper {

    private static class ThreadSafetyQueue {
        private LinkedList<Task> taskList = new LinkedList<Task>();

        public synchronized void push(Task task) {
            taskList.push(task);
            notify();
        }

        public synchronized Task pop() {
            return taskList.pop();
        }

        public synchronized boolean isEmpty() {
            return taskList.isEmpty();
        }
    }

    private static class Counter {
        private int val;

        public Counter (int valP) {
            val = valP;
        }

        public synchronized int get() {
            return val;
        }

        public synchronized void dec() {
            val--;
            if (val <= 0) {
                notify();
            }
        }

    }

    private static class Task<T, R> {
        private final Function <? super T, ? extends R> f;
        private final Counter counter;
        private final List<R> pullResult;
        private final int indexInPull;
        private final T arg;

        public Task(Function <? super T, ? extends R> fP, Counter counterP, List<R> pullResultP, int indexInPullP, T argP) {
            f = fP;
            counter = counterP;
            pullResult = pullResultP;
            indexInPull = indexInPullP;
            arg = argP;
        }

        public void doIt() {
            pullResult.set(indexInPull, f.apply(arg));
        }
    }

    private class Worker implements Runnable {
//        public Worker()

        public void run() {
            while (true) {
                Task myTask;
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) { }
                    }
                    myTask = queue.pop();
                }

                myTask.doIt();

                myTask.counter.dec();

            }
        }
    }

    private ArrayList<Thread> arrayOfThread;

    private ThreadSafetyQueue queue;

    private int cntThreads;

    public int getCountThread() {
        return cntThreads;
    }

    public ParallelMapperImpl(int threads) {
        cntThreads = threads;
        queue = new ThreadSafetyQueue();
        arrayOfThread = new ArrayList<>(threads);
        for (int i = 0; i < cntThreads; ++i) {
            Thread thread = new Thread(new Worker());
            thread.start();
            arrayOfThread.add(thread);
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R>
    List<R> map(Function<? super T, ? extends R> map, List<? extends T> args)
            throws InterruptedException {
        Counter counter = new Counter(args.size());

        List<R> results = new ArrayList<>(Collections.nCopies(args.size(), null));

        for (int indexTask = 0; indexTask < args.size(); ++indexTask) {
            queue.push(new Task<T, R>(map, counter, results, indexTask, args.get(indexTask)));
        }

        synchronized (counter) {
            while (counter.get() > 0) {
                counter.wait();
            }
        }

        return results;
    }

    /** Stops all threads. All unfinished mappings leave in undefined state. */
    @Override
    public void close() {
        for (int i = 0; i < cntThreads; ++i) {
            arrayOfThread.get(i).interrupt();
        }
    }

}
