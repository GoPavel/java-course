#!/bin/bash

# execure from java-course/scripts

rm ../out -rf
mkdir ../out 2> /dev/null

javac \
    -d ../out \
    -cp \
        ../artifacts/IterativeParallelismTest.jar:`
        `../lib/hamcrest-core-1.3.jar:`
        `../lib/junit-4.11.jar:`
        `../lib/jsoup-1.8.1.jar:`
        `../lib/quickcheck-0.6.jar: \
    -Xlint ../java/ru/ifmo/rain/golovin/concurrent/ListIPImpl.java

java -cp \
        ../artifacts/IterativeParallelismTest.jar:`
        `../lib/hamcrest-core-1.3.jar:`
        `../lib/junit-4.11.jar:`
        `../lib/jsoup-1.8.1.jar:`
        `../lib/quickcheck-0.6.jar:`
        `../out \
    info.kgeorgiy.java.advanced.concurrent.Tester list ru.ifmo.rain.golovin.concurrent.ListIPImpl $1
