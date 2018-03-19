package ru.ifmo.rain.golovin.implementor;

import java.nio.file.Paths;

public class ImplTester {
    private static Implementor hero = new Implementor();

    public static void main(String[] args) {
        try {
            hero.implement(A.class, Paths.get("."));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
