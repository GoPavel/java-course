package ru.ifmo.rain.golovin.walk;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {
    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Too few args");
        } else if (args.length > 2) {
            System.out.println("Too many args");
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF8"))) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF8"))) {
                    for (String pathname = reader.readLine(); pathname != null; pathname = reader.readLine()) {
                        try {
                            Path path = Paths.get(pathname);
                            Files.walkFileTree(path, new FileVisitorHash(writer));
                        }
                        catch (InvalidPathException e) {
                            writer.write(String.format("%08X", 0).toLowerCase() + " " + pathname);
                            System.out.println("Invalid path \"" + pathname + "\"");
                        }
                    }
                } catch (IOException ignore) {
                    System.out.println("Output file \""+ args[1] +"\" open error");
                }
            } catch (IOException ignore) {
                System.out.println("Input file \""+ args[0] + "\" open error");
            }
        }
    }
}
