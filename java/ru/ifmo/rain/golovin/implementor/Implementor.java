package ru.ifmo.rain.golovin.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class Implementor implements JarImpler {

    private String getClassName(Class<?> aClass) {
        return aClass.getSimpleName() + "Impl";
    }

    private String getDefaultValue(Class<?> aClass) {
        if (aClass.equals(boolean.class)) {
            return " false";
        } else if (aClass.equals(void.class)) {
            return "";
        } else if (aClass.isPrimitive()) {
            return " 0";
        } else {
            return " null";
        }
    }

    private final static String TAP = "    ";
    private final static String SPC = " ";
    private final static String ESC = System.lineSeparator();
    private final static String COMMA = ",";
    private final static String SEMI = ";";
    private final static String BRl = "(";
    private final static String BRr = ")";
    private final static String CBRl = "{";
    private final static String CBRr = "}";

    private String genHead(Class<?> aClass) {
        StringBuilder result = new StringBuilder();
        if (aClass.getPackage() != null) {
            result.append("package" + SPC + aClass.getPackage().getName() + SEMI + ESC + ESC);
        }
        result.append("public" + SPC + "class" + SPC + getClassName(aClass) + SPC +
                (aClass.isInterface() ? "implements" : "extends") + SPC + aClass.getSimpleName() + SPC + CBRl + ESC);
        return result.toString();
    }

    private String genTail(Class<?> aClass) { // must be consistent with genHead()
        return CBRr;
    }

    private void addToMethodStorage(Method[] methods, Set<Method> storage) {
        Arrays.stream(methods)
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .collect(Collectors.toCollection(() -> storage));
    }

    private String genAbstractMethods(Class<?> aClass) {
        StringBuilder result = new StringBuilder();

        Set<Method> methods = new TreeSet<>(Comparator.comparingInt(
                method -> (method.getName() + Arrays.toString(method.getParameterTypes())).hashCode()));

        addToMethodStorage(aClass.getMethods(), methods);
        for (Class<?> token = aClass; token != null; token = token.getSuperclass()) {
            addToMethodStorage(token.getDeclaredMethods(), methods);
        }

        for (Method method : methods) {
            result.append(genExecutable(method, method.getReturnType().getCanonicalName(), method.getName()));
            result.append(TAP + TAP + "return" + getDefaultValue(method.getReturnType()) + SEMI + ESC);
            result.append(TAP + CBRr + ESC);
            result.append(ESC);
        }

        return result.toString();
    }

    private String genConstructors(Class<?> aClass) {
        StringBuilder result = new StringBuilder();
        if (!aClass.isInterface()) {
            for(Constructor constructor: aClass.getDeclaredConstructors()) {
                if (!Modifier.isPrivate(constructor.getModifiers())) {
                    result.append(genExecutable(constructor, "", getClassName(aClass)));
                    result.append(TAP + TAP + "super");
                    result.append(Arrays.stream(constructor.getParameters()).map(Parameter::getName)
                            .collect(Collectors.joining(COMMA + SPC, BRl, BRr)) + SEMI + ESC);
                    result.append(TAP + CBRr + ESC);
                    result.append(ESC);
                }
            }
        }
        return result.toString();
    }


    private String genExecutable(Executable func, String returnType, String funcName) {
        StringBuilder result = new StringBuilder();

        result.append(TAP + Modifier.toString(func.getModifiers()).replaceAll("abstract|transient|volatile|native", ""));
        result.append(returnType + SPC);
        result.append(funcName);
        result.append(genParametersExecutable(func) + SPC);
        result.append(genExceptionExecutable(func) + CBRl + ESC);
        return result.toString();
    }

    private String genParametersExecutable(Executable func) {
        return Arrays.stream(func.getParameters())
                .map(parameter -> parameter.getType().getCanonicalName() + SPC + parameter.getName())
                .collect(Collectors.joining(COMMA + SPC, BRl, BRr));
    }

    private String genExceptionExecutable(Executable func) {
        if (func.getExceptionTypes().length == 0)
            return "";
        else
            return Arrays.stream(func.getExceptionTypes())
                    .map(Class::getCanonicalName)
                    .collect(Collectors.joining(COMMA + SPC, "throws" + SPC, SPC));
    }

    /**
     * Produces code implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to <tt>$root/java/util/ListImpl.java</tt>
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     *                                                                 generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Require not null argument.");
        }

        if (token.isPrimitive() || token.isArray() || Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
            throw new ImplerException("Incorrect type");
        }

        try (BufferedWriter code = createFile(token, root)) {
            try {
                code.write(genHead(token) + ESC);
                if (!token.isInterface()) {
                    if (genConstructors(token).isEmpty())
                        throw new ImplerException("Couldn't access constructors of super class");
                    code.write(genConstructors(token));
                }
                code.write(genAbstractMethods(token));
                code.write(genTail(token));
            } catch (IOException e) {
                throw new ImplerException("Problems with writing file.", e);
            }
        } catch (IOException e) {
            throw new ImplerException("Problems with create or close.", e);
        }
    }

    private Path resolveFilePath(Path path, Class<?> aClass, String end) {
        return path.resolve(aClass.getPackage().getName().replace('.', File.separatorChar)).resolve(getClassName(aClass) + end);
    }

    private BufferedWriter createFile(Class<?> token, Path path) throws IOException {
        Path pathFile = resolveFilePath(path, token, ".java"); //  path.resolve(token.getCanonicalName().replace('.', File.separatorChar) + "Impl.java");
        Files.createDirectories(Objects.requireNonNull(pathFile.getParent()));
        Files.deleteIfExists(pathFile); //TODO
        Files.createFile(pathFile);
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFile.toString()), "UTF8"));
    }


    /**
     * Produces <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     *
     * @param token type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException when implementation cannot be generated.@Override

    */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (token == null || jarFile == null) {
            throw new ImplerException("Require not null argument.");
        }

//        if (token.isPrimitive() || token.isArray() || Modifier.isFinal(token.getModifiers())
//                || token == Enum.class) {
//            throw new ImplerException("Incorrect type");
//        }

        if (jarFile.getParent() != null) {
            try {
                Files.createDirectories(jarFile.getParent());
            } catch (IOException e) {
                throw new ImplerException("Problems with create directories for temp files.", e);
            }
        }

        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "temp");
        }
        catch (IOException e) {
            throw new ImplerException("Problems with create temp directories.", e);
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String[] args = new String[3];
        args[0] = "-cp";
        args[1] = tempDir.toString() + File.pathSeparator + System.getProperty("java.class.path");
        args[2] = resolveFilePath(tempDir, token, ".java").toString();

        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Golovin Pavel");
        try (JarOutputStream jarStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            implement(token, tempDir);
            if (compiler.run(null, null, null, args) != 0) {
                throw new ImplerException("Problem with compile generative file.");
            }
            try {
                jarStream.putNextEntry(new ZipEntry(token.getName().replace('.', '/') + "Impl.class"));
                Files.copy(resolveFilePath(tempDir, token, ".class"), jarStream);
            } catch (IOException e) {
                throw new ImplerException("Problem with write to jar-file");
            }
        } catch(IOException e) {
            throw new ImplerException("Problem with create jar-file" , e);
        }
        finally {
            try {
                clean(tempDir);
            } catch (IOException e) {
                error("Problem with detele temp files.", e);
            }
        }
    }

    private void clean(Path dir) throws IOException {
        Files.walkFileTree(dir, new Cleaner());
    }

    private static class Cleaner extends SimpleFileVisitor<Path> {
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
    }

    public static void main(String[] args) {
        if (args == null || (args.length != 2 && args.length != 3)) {
            System.out.println("Two or three arguments expected.");
            return;
        }
        for (String arg: args) {
            if (arg == null) {
                System.out.println("expected non-null arguments.");
            }
        }
        JarImpler implementor = new Implementor();
        try {
            if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
        } catch (InvalidPathException e) {
            error("Invalid path in the second argument.", e);
        } catch (ClassNotFoundException e) {
            error("Invalid class in first argument.", e);
        } catch (ImplerException e) {
            error("An error occurred during implementation.", e);
        }
    }

    private static void error(String msg, Exception e) {
        System.out.println(msg);
        System.out.println("Exception's message: " + e.getMessage());
    }
}