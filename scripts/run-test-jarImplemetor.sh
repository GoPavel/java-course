#!/bin/bash

# execure from java-course/scripts

rm ../out -rf
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

java -cp \
        ../artifacts/JarImplementorTest.jar:`
        `../lib/hamcrest-core-1.3.jar:`
        `../lib/junit-4.11.jar:`
        `../lib/jsoup-1.8.1.jar:`
        `../lib/quickcheck-0.6.jar:`
        `../out \
    info.kgeorgiy.java.advanced.implementor.Tester jar-class ru.ifmo.rain.golovin.implementor.Implementor
