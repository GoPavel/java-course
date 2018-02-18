package ru.ifmo.rain.golovin.walk;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {
    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),  "UTF8"))) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF8"))) {
                for (String pathname = reader.readLine(); pathname != null; pathname = reader.readLine()) {
                    Files.walkFileTree(Paths.get(pathname), new FileVisitorHash(pathname, writer));
                }
            }
            catch (IOException ignore) {
                System.out.print("Output file open error");
            }
        }
        catch (IOException ignore) {
            System.out.println("Input file open error");
        }
    }
}
