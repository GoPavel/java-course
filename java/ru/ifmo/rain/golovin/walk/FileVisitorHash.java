package ru.ifmo.rain.golovin.walk;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileVisitorHash extends SimpleFileVisitor<Path> {
    BufferedWriter writer;
    int sizeBuffer = 1024;
    byte[] buffer  = new byte[sizeBuffer];


    public FileVisitorHash(BufferedWriter bufferedWriter) {
        writer = bufferedWriter;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        int acc = 0x811c9dc5;
        try (FileInputStream reader = new FileInputStream(file.toString())) {
            for (int cntReaded = reader.read(buffer, 0, sizeBuffer); cntReaded > 0; cntReaded = reader.read(buffer, 0, sizeBuffer)) {
                for (int i = 0; i < cntReaded; ++i) {
                    acc = (acc * 0x01000193) ^ (buffer[i] & 0xff);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Path: \"" + file.toString() + "\" is not found");
            acc = 0;
        } catch (IOException ignore) {
            acc = 0;
        } finally {
            try {
                writer.write(String.format("%08X", acc).toLowerCase() + " " + file.toString());
                writer.newLine();
            } catch (IOException e) {
                System.out.println("Can't write in output file"
                        + "\"" + String.format("%08X", acc).toLowerCase() + " " + file.toString() + "\"");
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        try {
            writer.write(String.format("%08X", 0).toLowerCase() + " " + file.toString());
            writer.newLine();
            System.out.println("Open or read error with file \"" + file.toString() + "\"");
        } catch (IOException ignore) {

        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        if (exc == null)
            return FileVisitResult.CONTINUE;
        else {
            System.out.println("I\\O error with directory");
            return FileVisitResult.CONTINUE;
        }
    }
}