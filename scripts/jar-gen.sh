#!/bin/bash

# execure from java-course/scripts

mkdir ../out 2> /dev/null

javac \
    -d ../out \
    -cp \
        ../artifacts/JarImplementorTest.jar:`
        `../lib/hamcrest-core-1.3.jar:`
        `../lib/junit-4.11.jar:`
        `../lib/jsoup-1.8.1.jar:`
        `../lib/quickcheck-0.6.jar: \
    -Xlint ../java/ru/ifmo/rain/golovin/implementor/Implementor.java

cd ../out

jar xf ../artifacts/JarImplementorTest.jar \
        info/kgeorgiy/java/advanced/implementor/Impler.class \
        info/kgeorgiy/java/advanced/implementor/JarImpler.class \
        info/kgeorgiy/java/advanced/implementor/ImplerException.class

mkdir ../jar-files 2> /dev/null

jar cfm ../jar-files/Implementor.jar ../java/ru/ifmo/rain/golovin/implementor/Manifest.txt \
    ru/ifmo/rain/golovin/implementor/*.class \
    info/kgeorgiy/java/advanced/implementor/*.class
