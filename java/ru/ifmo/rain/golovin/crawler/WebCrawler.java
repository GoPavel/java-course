package ru.ifmo.rain.golovin.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;

public class WebCrawler implements Crawler {

    private final ExecutorService downloadPool;

    private final ExecutorService extractPool;

    private final Downloader downloader;

    private int perHost;

    private String getHost(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private class TaskLoading implements Runnable {

        String url;
        String host;
        CounterLock counterLock;
        BlockingQueue<Result> resultQueue;
        ConcurrentMap<String, Semaphore> hostSemaphore;
        ConcurrentSkipListSet notes;
        int depth;


        public TaskLoading(String urlP,
                           CounterLock counterLockP,
                           BlockingQueue<Result> resultQueueP,
                           ConcurrentMap<String, Semaphore> hostSemaphoreP,
                           ConcurrentSkipListSet notesP,
                           int depthP) {
            url = urlP;
            host = getHost(url);
            counterLock = counterLockP;
            resultQueue = resultQueueP;
            hostSemaphore = hostSemaphoreP;
            notes = notesP;
            depth = depthP;
        }


        @Override
        public void run() {
            final Map<String, IOException> errors = new HashMap<>();
            try {
                Document doc = downloader.download(url);
            } catch (IOException e) {
                errors.put(url, e);
            }

            Future future = extractPool.submit(new TaskExtractLink(url, counterLock, hostSemaphore, notes, depth));
            try {
                future.get();
            } catch (Exception e) {
                //TODO
            }

//DEADLOCK
            counterLock.dec();
        }
    }

    private static class TaskExtractLink implements Runnable {

        public TaskExtractLink(String url,
                               CounterLock counterLock,
                               ConcurrentMap<String, Semaphore> hostSemaphore,
                               ConcurrentSkipListSet notes,
                               int depthP) {
            isDone.();

        //TODO
        }

        @Override
        public void run() {
        } //TODO
    }

    public WebCrawler(Downloader downloaderP, int downloadersP, int extractorsP, int perHostP) {
        downloadPool = Executors.newFixedThreadPool(downloadersP);
        extractPool = Executors.newFixedThreadPool(extractorsP);
        perHost = perHostP;
        downloader = downloaderP;
    }

    /**
     * Recursive download site.
     *
     * @param url   address of site
     * @param depth level of recursion
     * @return List of downloaded sites {@link Result#downloaded} and errors {@link Result#errors}
     */
    @Override
    public Result download(String url, int depth) {
        BlockingQueue<Result> result = new LinkedBlockingDeque<>(); // for result
        ConcurrentMap<String, Semaphore> hostSemaphore = new ConcurrentHashMap<>(); // check per host limit
        ConcurrentSkipListSet<String> notes = new ConcurrentSkipListSet<>(); // for unique check

        CounterLock counterLock = new CounterLock(1); // for wait all recur thread before return;

        Future<Result> res = downloadPool.submit(new TaskLoading(url, counterLock, result, hostSemaphore, notes, depth));

        try {
            counterLock.awaitZero();
        } catch (InterruptedException e) {
            //TODO
        }
        //TODO
    }

    /**
     * Close all supporting thread.
     */
    @Override
    public void close() {
        //TODO
    }

    /**
     * Run downloading.
     *
     * @param args = url [downloads [extractors [perHost]]]
     */
    public static void main(String[] args) {

    }
}

/*
 May be use ForkJoinPool.
 */
