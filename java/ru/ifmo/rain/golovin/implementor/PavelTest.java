package ru.ifmo.rain.golovin.implementor;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface PavelTest {
    int hello();
    default void defaultMethod() {
        System.out.println("defaultMethod");
    }
}
