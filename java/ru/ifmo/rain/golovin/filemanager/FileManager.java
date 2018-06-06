package ru.ifmo.rain.golovin.filemanager;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FileManager {

    private static final String helpString =
            "dir -- print current directory\n" +
                    "rm <file> -- remove file\n" +
                    "cd <dir> -- descend to directory\n" +
                    "create <file> -- create file\n" +
                    "mkdir <dir> -- make directory\n" +
                    "rmdir <dir> -- remove directory (recursively) \n" +
                    "help -- print command\n";

    private static Path currentDir;

    public static void main(String[] args) {

        Map<String, Consumer<List<String>>> commands = new HashMap<>();
        commands.put("help", (commandArgs) -> System.out.println(helpString));
        commands.put("dir", FileManager::printDirectory);
        commands.put("cd", FileManager::changeDirectory);
        commands.put("mkdir", FileManager::createDirectory);
        commands.put("create", FileManager::createFile);
        commands.put("rm", FileManager::removeFile);
        commands.put("rmdir", FileManager::removeFolder);

        currentDir = Paths.get(System.getProperty("user.dir"));

        System.out.println("Hi, I'm file manager.");
        System.out.println(helpString);

        while (true) {
            String commandArgs = System.console().readLine();
            if (commandArgs == null) {
                break;
            }

            List<String> words = new LinkedList<>(Arrays.asList(commandArgs.split("\\s")))
                    .stream()
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (words.size() < 1) {
                System.out.println("Incorrect command.");
                continue;
            }

            Consumer<List<String>> consumer = commands.get(words.get(0));
            if (consumer == null) {
                System.out.println("Unknown command: " + words.get(0));
                continue;
            }

            consumer.accept(words.subList(1, words.size()));
        }
    }

    private static void printDirectory(List<String> args) {
        if (!checkSize(args, 0)) {
            return;
        }

        System.out.println(currentDir.toAbsolutePath());
    }

    private static void changeDirectory(List<String> args) {
        if (!checkSize(args, 1)) {
            return;
        }

        Path tempPath = currentDir.resolve(args.get(0));

        try {
            if (!Files.isDirectory(tempPath)) {
                System.out.println("It isn't directory.");
            } else {
                currentDir = tempPath.normalize();
            }
        } catch (SecurityException e) {
            error("Can't access to " + args.get(0), e);
        }
    }

    private static void createDirectory(List<String> args) {
        if (!checkSize(args, 1)) {
            return;
        }

        Path tempPath = currentDir.resolve(args.get(0));
        try {
            Files.createDirectory(tempPath);
        } catch (FileAlreadyExistsException e) {
            error("File already exist.", e);
        } catch (SecurityException e) {
            error("Can't access to " + args.get(0), e);
        } catch (IOException e) {
            error("I/O error occurs.", e);
        }
    }

    private static void createFile(List<String> args) {
        if (!checkSize(args, 1)) {
            return;
        }

        Path tempPath = currentDir.resolve(args.get(0));
        try {
            Files.createFile(tempPath);
        } catch (FileAlreadyExistsException e) {
            error("File already exist.", e);
        } catch (SecurityException e) {
            error("Can't access to " + args.get(0), e);
        } catch (IOException e) {
            error("I/O error occurs.", e);
        }
    }

    private static void removeFile(List<String> args) {
        if (!checkSize(args, 1)) {
            return;
        }

        Path tempPath = currentDir.resolve(args.get(0));
        if (Files.isDirectory(tempPath)) {
            System.out.println("It's directory.");
            return;
        }
        try {
            Files.delete(tempPath);
        } catch (NoSuchFileException e) {
            error("File isn't exist.", e);
        } catch (SecurityException e) {
            error("Can't access to " + args.get(0), e);
        } catch (IOException e) {
            error("I/O error occurs.", e);
        }
    }

    private static void removeFolder(List<String> args) {
        if (!checkSize(args, 1)) {
            return;
        }

        Path tempPath = currentDir.resolve(args.get(0));

        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(tempPath, visitor);
        } catch (IOException e) {
            error("I/O error occurs.", e);
        }
    }

    private static boolean checkSize(List<String> args, int cnt) {
        if ((cnt == 0 && args != null && args.size() > 1) ||
                (cnt != 0 && args != null && args.size() > cnt)) {
            System.out.println("too many argument.");
            return false;
        }

        if (cnt != 0 && (args == null || args.size() < cnt)) {
            System.out.println("too few arguments.");
            return false;
        }

        return true;
    }

    private static void error(String msg, Exception e) {
        System.out.println(msg);
//        System.out.println(e.getMessage());
    }

}
