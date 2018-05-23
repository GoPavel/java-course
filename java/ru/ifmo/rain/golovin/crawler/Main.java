package ru.ifmo.rain.golovin.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Main {

    static public class BookAdder implements BiConsumer<String, String> {
        private final Set<String> books;
        private final String INFO_BEGIN = "<div id=\"bibliographic_record\">";
        private final String INFO_END = "</div>";

        public BookAdder(final Set<String> storage) {
            books = storage;
        }

        private static boolean checkPage(final String clean) {
            if (clean.contains("fizika_0\"")
                    || clean.contains("matematika_0\"")
                    || clean.contains("informatika_0\"")) {
                for (int i = 2014; i <= 2018; i++) {
                    if (clean.contains("<dt>Год:</dt><dd>" + Integer.toString(i) + "</dd>")) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void accept(String url, String page) {
            page = page.replaceAll(">\\p{javaWhitespace}+<", "><");
            if (checkPage(page)) {
                final int beg = page.indexOf(INFO_BEGIN);
                final int end = page.indexOf(INFO_END, beg);
                books.add(page.substring(beg + INFO_BEGIN.length(), end).trim());
            }
        }
    }

    static public class BookPredicate implements Predicate<String> {
        @Override
        public boolean test(String url) {
            return url.contains("e.lanbook.com/book/")
                    || url.contains("e.lanbook.com/books/917")
                    || url.contains("e.lanbook.com/books/918")
                    || url.contains("e.lanbook.com/books/1537")
                    || url.endsWith("e.lanbook.com/books");
        }
    }

    public static void main(String[] args) throws IOException {
//        WebCrawler crawler = new WebCrawler(new CachingDownloader(), 1, 1, 1);
//        Result res = crawler.download("http://www.ifmo.ru/ru/",  2);
//        System.out.println(">>>>>>>>>>>>\n" + res.getDownloaded().toString());
//        crawler.close();
//        Downloader downloader = new MyDownloader();
//        Document document = downloader.download("https://www.kgeorgiy.info/");
//        List<String> list = document.extractLinks();
//        System.out.println(list);
        final String host;
        try {
            host = URLUtils.getHost("https://e.lanbook.com");
        } catch (MalformedURLException ignored) {
            System.err.println(ignored.getMessage());
            return;
        }

        final Set<String> books = ConcurrentHashMap.newKeySet();
        final Predicate<String> bookPage = new BookPredicate();
        final Predicate<String> isBook = link -> link.contains("e.lanbook.com/book/");
        final BiConsumer<String, String> handler = new BookAdder(books);
        final Downloader downloader;
        try {
            downloader = new MyDownloader("tempPages", host, bookPage, isBook , handler);
        } catch (IOException e) {
            System.err.println("Can't create downloader: " + e.getMessage());
            return;
        }
        try (final Crawler crawler = new WebCrawler(downloader, 10, 10, 400)) {
            crawler.download("https://e.lanbook.com/books", 30);
            Path path = Paths.get("/media/pavel/DATA/java-course/books.txt");
            try (final BufferedWriter out = new BufferedWriter(Files.newBufferedWriter(path))){
                books.forEach(book -> {
                    try {
                        out.write(book + System.lineSeparator());
                    } catch (IOException e) {
                        System.err.println("Unable to write book url");
                    }
                });
            } catch (IOException e) {
                System.err.println("Unable to create output file " + e.getMessage());
            }
        }
    }
}
