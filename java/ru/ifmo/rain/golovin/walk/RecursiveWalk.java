package ru.ifmo.rain.golovin.walk;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {
    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
                System.out.println("Invalid input parameter.");
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF8"))) {
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF8"))) {
                        for (String pathname = reader.readLine(); pathname != null; pathname = reader.readLine()) {
                            try {
                                Path path = Paths.get(pathname);
                                Files.walkFileTree(path, new FileVisitorHash(writer, path.toString()));
                            } catch (InvalidPathException e) {
                                writer.write(String.format("%08X", 0).toLowerCase() + " " + pathname);
                                System.out.println("Invalid path exception during access to starting path for walking. Assumed that it's a file and wrote null-hash for this path.");
                                System.out.println("Exception message:"  + "\"" + e.getMessage() + "\"");
                            } catch (SecurityException e) {
                                writer.write(String.format("%08X", 0).toLowerCase() + " " + pathname);
                                System.out.println("Security exception during access to starting path for walking. Assumed that it's a file and wrote null-hash for this path.");
                                System.out.println("Exception message:"  + "\"" + e.getMessage() + "\"");
                            } catch (IOException e) {
                                System.out.println("I/O exception during walk. message: " + "\"" + e.getMessage() + "\"");
                            }
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Output file not found. Caught file not found exception with message: " + "\"" + e.getMessage() + "\"");
                    } catch (SecurityException e) {
                        System.out.println("Security exception during open output file. message: " + "\"" + e.getMessage() + "\"");
                    } catch (UnsupportedEncodingException e) {
                        System.out.println("Output file is not supported charset \"UTF8\". Caught unsupported encoding exception with message: " + "\"" + e.getMessage() + "\"");
                    } catch (IOException e) {
                        System.out.println("I/O exception during close output file. message: " + "\"" + e.getMessage() + "\"");
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Input file not found. Caught file not found exception with message: " + "\"" + e.getMessage() + "\"");
                } catch (SecurityException e) {
                    System.out.println("Security exception during open input file. message: " + "\"" + e.getMessage() + "\"");
                } catch (UnsupportedEncodingException e) {
                    System.out.println("Input file is not supported charset \"UTF8\". Caught unsupported encoding exception with message: " + "\"" + e.getMessage() + "\"");
                } catch (IOException e) {
                    System.out.println("I/O exception during close input file. message: " + "\"" + e.getMessage() + "\"");
                }
            }
        } catch (Error e) {
            System.out.println("Fatal error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Runtime exception: " + e.getMessage());
        }
    }
}
