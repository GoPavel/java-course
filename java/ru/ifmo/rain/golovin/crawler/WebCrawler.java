package ru.ifmo.rain.golovin.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WebCrawler implements Crawler {

    private final ExecutorService downloadPool;

    private final ExecutorService extractPool;

    private final Downloader downloader;

    private int perHost;

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

    private static String getHost(String url, ConcurrentMap<String, IOException> errors) {
        try {
            return URLUtils.getHost(url);
        } catch (MalformedURLException e) {
            errors.put(url, e);
            return null;
        }
    }


    private class TaskLoading implements Runnable {
        private final Phaser phaser;
        private final BlockingQueue<DataWrapper> dataQueue;
        private final ConcurrentMap<String, IOException> errors;
        private final ConcurrentSkipListSet<String> notesOfUrl;
        private final int depth;
        private final ConcurrentMap<String, AtomicInteger> counterPerHost;
        private final ConcurrentMap<String, BlockingQueue<String>> tasksPerHost;
        private String host;

        private TaskLoading(String url,
                            Phaser phaser,
                            BlockingQueue<DataWrapper> dataQueue,
                            ConcurrentMap<String, IOException> errors,
                            ConcurrentSkipListSet<String> notesOfUrl,
                            int depth,
                            ConcurrentMap<String, AtomicInteger> counterPerHost,
                            ConcurrentMap<String, BlockingQueue<String>> tasksPerHost) {
            this.phaser = phaser;
            phaser.register();
            this.dataQueue = dataQueue;
            this.errors = errors;
            this.notesOfUrl = notesOfUrl;
            this.depth = depth;
            this.counterPerHost = counterPerHost;
            this.tasksPerHost = tasksPerHost;
            this.host = getHost(url, errors);
        }

        @Override
        public void run() {
            try {
                if (host != null) {
                    BlockingQueue<String> queueTasks = tasksPerHost.getOrDefault(host, null);
                    while (!queueTasks.isEmpty()) {
                        String url = queueTasks.remove();

                        Document document = null;
                        try {
                            document = downloader.download(url);
                        } catch (IOException e) {
                            errors.put(url, e);
                        }

                        if (depth > 1 && document != null) {
                            extractPool.submit(new TaskExtractLink(url, phaser, dataQueue,
                                    errors, notesOfUrl, depth, document, counterPerHost, tasksPerHost));
                        }
                        if (document != null)
                            dataQueue.add(new DataWrapper(url));

                    }
                }
            } catch (NoSuchElementException e) {

            } finally {
                phaser.arrive();
                counterPerHost.get(host).decrementAndGet();
            }

        }
    }

    private class TaskExtractLink implements Runnable {
        private final String url;
        private final Phaser phaser;
        private final BlockingQueue<DataWrapper> dataQueue;
        private final ConcurrentMap<String, IOException> errors;
        private final ConcurrentSkipListSet<String> notesOfUrl;
        private int depth;
        private final Document document;
        private final ConcurrentMap<String, AtomicInteger> counterPerHost;
        private final ConcurrentMap<String, BlockingQueue<String>> tasksPerHost;

        public TaskExtractLink(String url,
                               Phaser phaser,
                               BlockingQueue<DataWrapper> dataQueue,
                               ConcurrentMap<String, IOException> errors,
                               ConcurrentSkipListSet<String> notesOfUrl,
                               int depth,
                               Document document,
                               ConcurrentMap<String, AtomicInteger> counterPerHost,
                               ConcurrentMap<String, BlockingQueue<String>> tasksPerHost) {
            this.url = url;
            this.phaser = phaser;
            phaser.register();
            this.dataQueue = dataQueue;
            this.errors = errors;
            this.notesOfUrl = notesOfUrl;
            this.depth = depth;
            this.document = document;
            this.counterPerHost = counterPerHost;
            this.tasksPerHost = tasksPerHost;
        }

        @Override
        public void run() {
            try {
                List<String> urls = document.extractLinks();

                if (urls != null) {
                    for (String url : urls) {
                        addDownloadTask(url, phaser, dataQueue, errors, notesOfUrl, depth, counterPerHost, tasksPerHost);
                    }
                }
            } catch (IOException e) {
                errors.put(url, e);
            } finally {
                phaser.arrive();
            }
        }
    }

    private void addDownloadTask(String url,
                                 Phaser phaser,
                                 BlockingQueue<DataWrapper> dataQueue,
                                 ConcurrentMap<String, IOException> errors,
                                 ConcurrentSkipListSet<String> notesOfUrl,
                                 int depth,
                                 ConcurrentMap<String, AtomicInteger> counterPerHost,
                                 ConcurrentMap<String, BlockingQueue<String>> tasksPerHost) {
        if (notesOfUrl.add(url)) {

            String host = getHost(url, errors);

            if (host != null) {
                tasksPerHost.putIfAbsent(host, new LinkedBlockingQueue<>());
                counterPerHost.putIfAbsent(host, new AtomicInteger(0));

                BlockingQueue<String> tasks = tasksPerHost.get(host);
                AtomicInteger counter = counterPerHost.get(host);

                tasks.add(url);
                if (counter.addAndGet(1) - 1 >= perHost) {
                    // not download
                    counter.decrementAndGet();
                } else {
                    // download
                    downloadPool.submit(new TaskLoading(url, phaser, dataQueue,
                            errors, notesOfUrl, depth - 1, counterPerHost, tasksPerHost));
                }
            }
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
        ConcurrentSkipListSet<String> notesOfUrl = new ConcurrentSkipListSet<>(); // for unique check

        ConcurrentMap<String, AtomicInteger> counterPerHost = new ConcurrentHashMap<>();
        ConcurrentMap<String, BlockingQueue<String>> tasksPerHost = new ConcurrentHashMap<>();

        Phaser phaser = new Phaser(1);

        addDownloadTask(url, phaser, result, errors, notesOfUrl, depth + 1, counterPerHost, tasksPerHost);

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