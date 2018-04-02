#!/bin/bash

javadoc \
    -link https://docs.oracle.com/javase/8/docs/api/ \
    -private \
    -d ./../javaDocJarImplementor \
    -cp ../artifacts/JarImplementorTest.jar:`
        `../lib/hamcrest-core-1.3.jar:`
        `../lib/junit-4.11.jar:`
        `../lib/jsoup-1.8.1.jar:`
        `../lib/quickcheck-0.6.jar:\
     ../java/ru/ifmo/rain/golovin/implementor/Implementor.java \
     ../java/ru/ifmo/rain/golovin/implementor/package-info.java \
     ../java/info/kgeorgiy/java/advanced/implementor/Impler.java \
     ../java/info/kgeorgiy/java/advanced/implementor/JarImpler.java \
     ../java/info/kgeorgiy/java/advanced/implementor/ImplerException.java
