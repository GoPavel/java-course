package ru.ifmo.rain.golovin.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

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

    private static class DataWrapper {
        private String url;
        private Document document;

        public DataWrapper(String url) {
            this.url = url;
        }

        public DataWrapper(String url, Document document) {
            this.url = url;
            this.document = document;
        }

        public String getUrl() {
            return url;
        }
    }

    private class TaskLoading implements Runnable {
        private final String url;
        private final String host;
        private final Phaser phaser;
        private final BlockingQueue<DataWrapper> dataQueue;
        private final ConcurrentMap<String, IOException> errors;
        private final ConcurrentMap<String, Semaphore> hostSemaphore;
        private final ConcurrentSkipListSet<String> notesOfUrl;
        private int depth;

        private TaskLoading(String url,
                            Phaser phaser,
                            BlockingQueue<DataWrapper> dataQueue,
                            ConcurrentMap<String, IOException> errors,
                            ConcurrentMap<String, Semaphore> hostSemaphore,
                            ConcurrentSkipListSet<String> notesOfUrl,
                            int depth) {
            this.url = url;
            this.host = getHost(url);
            this.phaser = phaser;
            this.dataQueue = dataQueue;
            this.errors = errors;
            this.hostSemaphore = hostSemaphore;
            this.notesOfUrl = notesOfUrl;
            this.depth = depth;
        }


        @Override
        public void run() {
            try {
                Semaphore semaphore = hostSemaphore.get(host);
                if (semaphore == null) {
                    semaphore = new Semaphore(perHost);
                    hostSemaphore.put(host, semaphore);
                }

                semaphore.acquire();
                Document document = null;
                try {
                    document = downloader.download(url);
                } catch (IOException e) {
                    errors.put(url, e);
                }
                semaphore.release();

                if (depth != 0 && document != null) {
                    extractPool.submit(new TaskExtractLink(url, phaser, dataQueue,
                            hostSemaphore, errors, notesOfUrl, depth, document));
                }

                dataQueue.add(new DataWrapper(url));

                phaser.arrive();
            } catch (InterruptedException e) {
            }
        }
    }

    private class TaskExtractLink implements Runnable {
        private final String url;
        private final Phaser phaser;
        private final BlockingQueue<DataWrapper> dataQueue;
        private final ConcurrentMap<String, Semaphore> hostSemaphore;
        private final ConcurrentMap<String, IOException> errors;
        private final ConcurrentSkipListSet<String> notesOfUrl;
        private int depth;
        private final Document document;

        public TaskExtractLink(String url,
                               Phaser phaser,
                               BlockingQueue<DataWrapper> dataQueue,
                               ConcurrentMap<String, Semaphore> hostSemaphore,
                               ConcurrentMap<String, IOException> errors,
                               ConcurrentSkipListSet<String> notesOfUrl,
                               int depth,
                               Document document) {
            this.url = url;
            this.phaser = phaser;
            phaser.register();
            this.dataQueue = dataQueue;
            this.hostSemaphore = hostSemaphore;
            this.errors = errors;
            this.notesOfUrl = notesOfUrl;
            this.depth = depth;
            this.document = document;
        }

        @Override
        public void run() {
            List<String> urls = null;
            try {
                urls = document.extractLinks();
            } catch (IOException e) {
                errors.put(url, e);
            }

            if (urls != null) {
                for (String url : urls) {
                    if (notesOfUrl.add(url)) {
                        downloadPool.submit(new TaskLoading(url, phaser, dataQueue,
                                errors, hostSemaphore, notesOfUrl, depth - 1));
                    }
                }
            }

            phaser.arrive();
        }
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloadPool = Executors.newFixedThreadPool(downloaders);
        this.extractPool = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
        this.downloader = downloader;
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
        BlockingQueue<DataWrapper> result = new LinkedBlockingDeque<>(); // for result
        ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();
        ConcurrentMap<String, Semaphore> hostSemaphore = new ConcurrentHashMap<>(); // check per host limit
        ConcurrentSkipListSet<String> notes = new ConcurrentSkipListSet<>(); // for unique check

        Phaser phaser = new Phaser();

        downloadPool.submit(new TaskLoading(url, phaser, result,
                errors, hostSemaphore, notes, depth));

        phaser.arriveAndAwaitAdvance();

        return createResult(result, errors);
    }

    private Result createResult(BlockingQueue<DataWrapper> data, Map<String, IOException> errors) {
        return new Result(data.stream().map(DataWrapper::getUrl).collect(Collectors.toList()), errors);
    }

    /**
     * Close all supporting thread.
     */
    @Override
    public void close() {
        downloadPool.shutdown();
        extractPool.shutdown();
    }

    /**
     * Run downloading.
     *
     * @param args = url [downloads [extractors [perHost]]]
     */
    public static void main(String[] args) {
        int defaultDownloads = 10;
        int defaultExtractors = 10;
        int defaultPerHost = 10;
        int defaultDepth = 1;

        if (args.length > 5) {
            System.out.println("Too many arguments.");
            return;
        }
        if (args.length < 1) {
            System.out.println("Too few arguments.");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.out.println("Expected non null argument.");
                return;
            }
        }
        String url = args[0];
        int downloads;
        int extractors;
        int perHost;
        int depth;
        try {
            downloads = args.length > 1 ? Integer.parseInt(args[1]) : defaultDownloads;
            extractors = args.length > 2 ? Integer.parseInt(args[2]) : defaultExtractors;
            perHost = args.length > 3 ? Integer.parseInt(args[3]) : defaultPerHost;
            depth = args.length > 4 ? Integer.parseInt(args[4]) : defaultDepth;
        } catch (NumberFormatException e) {
            error("Can't parse some integer argument", e);
            return;
        }

        WebCrawler crawler;
        try {
            crawler = new WebCrawler(new CachingDownloader(), downloads, extractors, perHost);
        } catch (IOException e) {
            error("Problem with creating Downloader:", e);
            return;
        }
        crawler.download(url, depth);
    }

    private static void error(String msg, Exception e) {
        System.out.println(msg);
        System.out.println("Exception's message: " + e.getMessage());
    }
}

/*
 May be use ForkJoinPool.
 */
