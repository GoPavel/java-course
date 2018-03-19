package ru.ifmo.rain.golovin.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//public class Implementor implements Impler {
//
//    private BufferedWriter textClassWriter = null;
//    private String className = null;
//
//    private void error(Exception e) {
//        System.out.println(e.getClass().getCanonicalName() + ": " + e.getMessage());
//    }
//
//    private BufferedWriter makeJavaFile(Path path) throws IOException{
//        Path pathOfClass;
//        pathOfClass = Paths.get(path.toString(), className + ".java");
//        Files.deleteIfExists(pathOfClass); // TODO
//        Files.createFile(pathOfClass);
//        textClassWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathOfClass.toString()), "UTF8"));
//        return textClassWriter;
//    }
//
//    private void write(String str) throws IOException {
//        textClassWriter.write(str);
//    }
//
//    @Override
//    public void implement(Class<?> aClass, Path path) throws ImplerException {
//        try {
//            className = aClass.getSimpleName();
//            textClassWriter = makeJavaFile(path);
//            write("// This class was auto-generated.");
//            textClassWriter.newLine();
//            write( "public");
//            textClassWriter.close();
//        } catch (IOException e) {
//            error(e);
//        }
//    }
//}

public class Implementor implements Impler {

    /*
    other method
     */

    private void createFile() {} //TODO

    /*
    Pull constant for simply output.
     */
    private static String TAP = "    ";
    private static String SPC = " ";

    /*
    Pull method for draw code and put to output stream.

    IOException must catch in other method
     */
    private void drawMethod() {} //TODO
    private void drawConstructor() {} //TODO
    private void drawHead() {} //TODO
    private void drawTail() {} // must be consistent with drawHead() TODO

    /*
    pull method for output
     */
    private void write() {} //TODO
    private void writeln() {} // TODO

    /*
    public method should be hard and heavy
     */

    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        //TODO
    }

    public static void main(String[] args) {
        //TODO
    }
}
