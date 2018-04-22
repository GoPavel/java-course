package ru.ifmo.rain.golovin.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private class TaskLoading implements Callable<Result> {

        String url;

        public TaskLoading(String url,
                           Phaser isDone,
                           BlockingQueue resultQueue,
                           ConcurrentMap<String, Semaphore> hostSemaphore,
                           ConcurrentSkipListSet notes) {
        } //TODO

        public TaskLoading(String urlP) {
            url = urlP;
        }

        @Override
        public Result call() {
            Map<String, IOException> errors;

            try {
                Document doc = downloader.download(url);
            } catch (IOException e) {
//                errors.add()
            }
        }
    }

    private static class TaskExtractLink implements Runnable {

        public TaskExtractLink(String url,
                               Phaser isDone,
                               BlockingQueue resultQueue,
                               ConcurrentMap<String, Semaphore> hostSemaphore,
                               ConcurrentSkipListSet notes) {
        } //TODO

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
        ConcurrentSkipListSet<String> notes = new ConcurrentSkipListSet<>();

        Phaser isDone = new Phaser(); // for wait all recur thread before return;


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
