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
    String namePathOfWriter;
    int sizeBuffer = 1024;
    byte[] buffer = new byte[sizeBuffer];


    public FileVisitorHash(BufferedWriter writer, String namePathOfWriter) {
        this.writer = writer;
        this.namePathOfWriter = namePathOfWriter;
    }

    private void writeHash(int hash, Path path) {
        String text = String.format("%08X", hash).toLowerCase() + " " + path.toString();
        try {
            writer.write(text);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("I/O exception during write to output file. message: " + "\"" + e.getMessage() + "\"");
            System.out.println("Unwritten text: " + "\"" + text + "\"");
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        int acc = 0x811c9dc5;
        try (FileInputStream reader = new FileInputStream(file.toString())) {
            try {
                for (int cntReaded = reader.read(buffer, 0, sizeBuffer); cntReaded > 0; cntReaded = reader.read(buffer, 0, sizeBuffer)) {
                    for (int i = 0; i < cntReaded; ++i) {
                        acc = (acc * 0x01000193) ^ (buffer[i] & 0xff);
                    }
                }
            } catch (IOException e) {
                System.out.println("I/O exception during read file when walk in path " + "\"" + namePathOfWriter + "\"");
                System.out.println("Exception message: " + "\"" + e.getMessage() + "\"");
                acc = 0;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found exception during open file when walk in path " + "\"" + namePathOfWriter + "\"");
            System.out.println("Exception message: " + "\"" + e.getMessage() + "\"");
            acc = 0;
        } catch (SecurityException e) {
            System.out.println("Security exception during open file when walk in path " + "\"" + namePathOfWriter + "\"");
            System.out.println("Exception message: " + "\"" + e.getMessage() + "\"");
            acc = 0;
        } catch (IOException e) {
            System.out.println("I/O exception during close file when walk in path " + "\"" + namePathOfWriter + "\"");
            System.out.println("Exception message: " + "\"" + e.getMessage() + "\"");
            acc = 0;
        } finally {
            writeHash(acc, file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        writeHash(0, file);
        System.out.println("File " + "\"" + file.toString() + "\" " + "couldn't be visit with I/O exception " + "\"" + exc.getClass().getCanonicalName() + "\" " +
                "when walk in path " + "\"" + namePathOfWriter + "\".");
        System.out.println("Exception message: " + exc.getMessage());
        return FileVisitResult.CONTINUE;
    }

    /*
     * Note: Как можно обрабытывать исключения, которые нам приходят как параметры? Нужно ли пытаться найти динамический тип?
     * Ведь специфицировать статический исключения при передачи его как параметр невозможно.
     * if (instanceof)
     */

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        if (exc == null)
            return FileVisitResult.CONTINUE;
        else {
            System.out.println("I/O exception " + "\"" + exc.getClass().getCanonicalName() + "\" " +
                    "when walk in path " + "\"" + namePathOfWriter + "\" and iteration completed prematurely.");
            System.out.println("Exception message: " + "\"" + exc.getMessage() + "\"");
            return FileVisitResult.CONTINUE;
        }
    }
}