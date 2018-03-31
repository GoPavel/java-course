package ru.ifmo.rain.golovin.implementor;

import java.util.Arrays;

public interface A {
    Integer a = null;

    Arrays func(Integer[] a);

    void foo();

    Integer bar(String a, Double b);

    String foobar(String a, int  b, double c, char d);

    default Void foobaz() {
        return null;
    }

    int foooo(int val);
}
