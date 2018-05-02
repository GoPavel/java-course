package ru.ifmo.rain.golovin.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.ReplayDownloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.List;

import static info.kgeorgiy.java.advanced.crawler.CrawlerEasyTest.checkResult;

public class Main {
    public static void main(String[] args) throws IOException{
//        WebCrawler crawler = new WebCrawler(new CachingDownloader(), 1, 1, 1);
//        Result res = crawler.download("https://docs.oracle.com/javase/10/docs/api/java/util/concurrent/LinkedBlockingQueue.html#put(E)",  1);
//        System.out.println(">>>>>>>>>>>>\n" + res.getDownloaded().toString());
//        crawler.close();
        CachingDownloader downloader = new CachingDownloader();
        Document document = downloader.download("https://www.kgeorgiy.info/");
        List<String> list = document.extractLinks();
        System.out.println(list);
    }
}
