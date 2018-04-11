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
import java.nio.file.FileVisitor;
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

/**
 * Implementation of {@link JarImpler}.
 *
 * This class produces code implementing class or interface and creates jar file containing it.
 *
 * @author  Golovin Pavel
 */
public class Implementor implements JarImpler {

    /**
     * Create new instance.
     */
    public Implementor() { }

    /**
     * Generate name of implementation of given class.
     *
     * @param aClass reflection of implementing class.
     * @return string representation name of implementing class.
     */
    private String getClassName(Class<?> aClass) {
        return aClass.getSimpleName() + "Impl";
    }

    /**
     * Generate code for return default value by given type.
     *
     * @param aClass reflection of type of default value.
     * @return string representation of value.
     */
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

    /**
     * Constants for generating code of implementation. It equals to four space.
     */
    private final static String TAB = "    ";

    /**
     * Constant for generating code of implementation. it equals to one space.
     */
    private final static String SPC = " ";

    /**
     * Constant for generating code of implementation. It equals to <code>System.lineSeparator</code>.
     */
    private final static String NEWLINE = System.lineSeparator();

    /**
     * Constant for generating code of implementation. It equals to one comma.
     */
    private final static String COMMA = ",";

    /**
     * Constant for generating code of implementation. It equals to one semicolon.
     */
    private final static String SEMI = ";";

    /**
     * Constant for generating code of implementation. It equals to one left round bracket.
     */
    private final static String BRl = "(";

    /**
     * Constant for generating code of implementation. It equals to one right round bracket.
     */
    private final static String BRr = ")";

    /**
     * Constant for generating code of implementation. It equals to one left curly bracket.
     */
    private final static String CBRl = "{";

    /**
     * Constant for generating code of implementation. It equal one right curly bracket.
     */
    private final static String CBRr = "}";

    /**
     * Generate the first part of <code>aClass</code>'s implementation. It contains:
     * <ul>
     *     <li>package's declaration</li>
     *     <li>class` declaration</li>
     * </ul>
     *
     * @param aClass reflection of implementing class.
     * @return string representation of the the first part of generating code.
     */
    private String genHead(Class<?> aClass) {
        StringBuilder result = new StringBuilder();
        if (aClass.getPackage() != null) {
            result.append("package" + SPC + aClass.getPackage().getName() + SEMI + NEWLINE + NEWLINE);
        }
        result.append("public" + SPC + "class" + SPC + getClassName(aClass) + SPC +
                (aClass.isInterface() ? "implements" : "extends") + SPC + aClass.getSimpleName() + SPC + CBRl + NEWLINE);
        return result.toString();
    }

    /**
     * Generate last part of <code>aClass</code>'s implementation. Example, last back curly bracket.
     *
     * @param aClass reflection of implementing class.
     * @return string representation of last part of generating code.
     */
    private String genTail(Class<?> aClass) { // must be consistent with genHead()
        return CBRr;
    }

    /**
     * add reflection of abstract methods form array to set.
     *
     * @param methods array of reflection of method.
     * @param storage set, witch collected abstract methods form <code>methods</code>
     */
    private void addToMethodStorage(Method[] methods, Set<Method> storage) {
        Arrays.stream(methods)
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .collect(Collectors.toCollection(() -> storage));
    }

    /**
     * Generate implementation of given class's method.
     *
     * @param aClass reflection of implementing class.
     * @return string representation code of <code>aClass</code>'s abstract methods.
     */
    private String genAbstractMethods(Class<?> aClass) {
        StringBuilder result = new StringBuilder();

        Set<Method> methods = new TreeSet<>(Comparator.comparingInt(
                method -> (method.getName() + Arrays.toString(method.getParameterTypes())).hashCode()));

        addToMethodStorage(aClass.getMethods(), methods);

        if (!aClass.isInterface()) {
            for (Class<?> token = aClass; token != null; token = token.getSuperclass()) {
                addToMethodStorage(token.getDeclaredMethods(), methods);
            }
        }

        for (Method method : methods) {
            result.append(genExecutable(method, method.getReturnType().getCanonicalName(), method.getName()));
            result.append(TAB + TAB + "return" + getDefaultValue(method.getReturnType()) + SEMI + NEWLINE);
            result.append(TAB + CBRr + NEWLINE);
            result.append(NEWLINE);
        }

        return result.toString();
    }

    /**
     *  Generate implementation of given class' constructors.
     *
     * @param aClass reflection of implementing class.
     * @return string representation code of <code>aClass</code>'s constructors.
     */
    private String genConstructors(Class<?> aClass) {
        StringBuilder result = new StringBuilder();
        if (!aClass.isInterface()) {
            for (Constructor constructor : aClass.getDeclaredConstructors()) {
                if (!Modifier.isPrivate(constructor.getModifiers())) {
                    result.append(genExecutable(constructor, "", getClassName(aClass)));
                    result.append(TAB + TAB + "super");
                    result.append(Arrays.stream(constructor.getParameters()).map(Parameter::getName)
                            .collect(Collectors.joining(COMMA + SPC, BRl, BRr)) + SEMI + NEWLINE);
                    result.append(TAB + CBRr + NEWLINE);
                    result.append(NEWLINE);
                }
            }
        }
        return result.toString();
    }


    /**
     * Generate implementation of given class` constructor or method.
     *
     * @param func reflection of method or constructor.
     * @param returnType string representation of <code>func</code>'s return type. Example, for constructor it's must be empty.
     * @param funcName string representation of <code>func</code>`s name.
     * @return string representation of this <code>func</code>.
     */
    private String genExecutable(Executable func, String returnType, String funcName) {
        StringBuilder result = new StringBuilder();

        result.append(TAB + Modifier.toString(func.getModifiers() & ~(Modifier.ABSTRACT|Modifier.TRANSIENT|Modifier.VOLATILE)) + SPC);
        result.append(returnType + SPC);
        result.append(funcName);
        result.append(genParametersExecutable(func) + SPC);
        result.append(genExceptionExecutable(func) + CBRl + NEWLINE);
        return result.toString();
    }

    /**
     * Generate implementation of executable object's parameters.
     *
     * @param func executable object.
     * @return string representation of <code>func</code>'s parameters.
     */
    private String genParametersExecutable(Executable func) {
        return Arrays.stream(func.getParameters())
                .map(parameter -> parameter.getType().getCanonicalName() + SPC + parameter.getName())
                .collect(Collectors.joining(COMMA + SPC, BRl, BRr));
    }

    /**
     * Generate implement of specifying exceptions for executable object.
     *
     * @param func executable object.
     * @return string representation of specifying exceptions thrown by <code>func</code>.
     */
    private String genExceptionExecutable(Executable func) {
        if (func.getExceptionTypes().length == 0)
            return "";
        else
            return Arrays.stream(func.getExceptionTypes())
                    .map(Class::getCanonicalName)
                    .collect(Collectors.joining(COMMA + SPC, "throws" + SPC, SPC));
    }

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
                code.write(genHead(token) + NEWLINE);
                if (!token.isInterface()) {
                    String stringOfConstructor = genConstructors(token);
                    if (stringOfConstructor.isEmpty())
                        throw new ImplerException("Couldn't access constructors of super class");
                    code.write(stringOfConstructor);
                }
                code.write(genAbstractMethods(token));
                code.write(genTail(token));
            } catch (IOException e) {
                throw new ImplerException("Problems with writing file.", e);
            }
        } catch (IOException e) {
            throw new ImplerException("Problems with creating or closing generated file.", e);
        }
    }

    /**
     * Resolve path file: convernt package to path and append extension;
     *
     * @param path path file with code
     * @param aClass reflection of implementing class.
     * @param end string representation of extension.
     * @return path to file
     */
    private Path resolveFilePath(Path path, Class<?> aClass, String end) {
        return path.resolve(aClass.getPackage().getName().replace('.', File.separatorChar)).resolve(getClassName(aClass) + end);
    }

    /**
     * Crete file for code implementation and directories on path to file.
     *
     * @param token reflection of implementing class.
     * @param path path to implementing class.
     * @return writer on new file.
     * @throws IOException if occurred problem with create file or directories.
     */
    private BufferedWriter createFile(Class<?> token, Path path) throws IOException {
        Path pathFile = resolveFilePath(path, token, ".java");
        if (pathFile.getParent() != null) {
            Files.createDirectories(pathFile.getParent());
        }
        Files.deleteIfExists(pathFile);
        Files.createFile(pathFile);
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFile.toString()), "UTF8"));
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (token == null || jarFile == null) {
            throw new ImplerException("Require not null argument.");
        }

        if (token.isPrimitive() || token.isArray() || Modifier.isFinal(token.getModifiers())
                || token == Enum.class) {
            throw new ImplerException("Incorrect type");
        }

        if (jarFile.getParent() != null) {
            try {
                Files.createDirectories(jarFile.getParent());
            } catch (IOException e) {
                throw new ImplerException("Problems with creating directories for temp files.", e);
            }
        }

        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "temp");
        } catch (IOException e) {
            throw new ImplerException("Problems with creating temp directories.", e);
        }

        implement(token, tempDir);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Problem with the compiler.");
        }

        String[] args = new String[] { "-cp",
            tempDir.toString() + File.pathSeparator + System.getProperty("java.class.path"),
            resolveFilePath(tempDir, token, ".java").toString() };

        if (compiler.run(null, null, null, args) != 0) {
            throw new ImplerException("Problem with compiling generative file.");
        }

        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Golovin Pavel");
        try (JarOutputStream jarStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            try {
                jarStream.putNextEntry(new ZipEntry(token.getName().replace('.', '/') + "Impl.class"));
                Files.copy(resolveFilePath(tempDir, token, ".class"), jarStream);
            }   catch (IOException e) {
                throw new ImplerException("Problem with writing to jar-file");
            }
        } catch (IOException e) {
            throw new ImplerException("Problem with creating or closing jar-file", e);
        } finally {
            try {
                clean(tempDir);
            } catch (IOException e) {
                error("Problem with delete temp files.", e);
            }
        }
    }

    /**
     * Remove all file and directories from given file subtree.
     *
     * @param dir path root removed subtree.
     * @throws IOException if an I/O error occurs.
     */
    private void clean(Path dir) throws IOException {
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
        Files.walkFileTree(dir, visitor);
    }

    /**
     * Gets console argument and executes implementation.
     * Two mode is possible:
     *  <ul>
     *  <li> 2 arguments: <tt>className rootPath</tt> - call {@link #implement(Class, Path)} with given arguments.
     *      It's generate implementation of class or interface</li>
     *  <li> 3 arguments: <tt>-jar className jarPath</tt> - call {@link #implementJar(Class, Path)} with two last arguments.
     *      It's generate jar file with implementation of class or interface. </li>
     *  </ul>
     *  If arguments are incorrect or an error occurs during implementation returns message with information about error.
     *
     * @param args arguments for running an application.
     */
    public static void main(String[] args) {
        if (args == null || (args.length != 2 && args.length != 3)) {
            System.out.println("Two or three arguments expected.");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.out.println("expected non-null arguments.");
                return;
            }
        }
        JarImpler implementor = new Implementor();
        try {
            if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else if (!args[0].equals("-jar")) {
                System.out.println("\'" + args[0] + "\'" + " -- unknown argument, -jar expected.");
            } else {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
        } catch (InvalidPathException e) {
            error("Invalid path in the second argument.", e);
        } catch (ClassNotFoundException e) {
            error("Invalid class in the first argument.", e);
        } catch (ImplerException e) {
            error("An error occurred during implementation.", e);
        }
    }

    /**
     * Print in stdout custom message and exception's message.
     *
     * @param msg custom message
     * @param e exception
     */
    private static void error(String msg, Exception e) {
        System.out.println(msg);
        System.out.println("Exception's message: " + e.getMessage());
    }
}
