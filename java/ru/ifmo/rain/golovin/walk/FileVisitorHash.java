package ru.ifmo.rain.golovin.walk;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;

public class FileVisitorHash extends SimpleFileVisitor<Path> {
    BufferedWriter writer;

    public FileVisitorHash(String pathnameStart, BufferedWriter bufferedWriter) {
        writer = bufferedWriter;
    }
    //TODO
}