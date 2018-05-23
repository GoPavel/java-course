package ru.ifmo.rain.golovin.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        WebCrawler crawler = new WebCrawler(new CachingDownloader(), 1, 1, 1);
        Result res = crawler.download("http://www.ifmo.ru/ru/",  2);
        System.out.println(">>>>>>>>>>>>\n" + res.getDownloaded().toString());
        crawler.close();
//        Downloader downloader = new MyDownloader();
//        Document document = downloader.download("https://www.kgeorgiy.info/");
//        List<String> list = document.extractLinks();
//        System.out.println(list);
    }
}
