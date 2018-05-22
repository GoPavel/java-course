#!/usr/bin/env bash

rm ../out -rf
mkdir ../out 2> /dev/null

javac \
    -d ../out \
    -cp \
        ../artifacts/HelloUDPTest.jar:`
        `../lib/hamcrest-core-1.3.jar:`
        `../lib/junit-4.11.jar:`
        `../lib/jsoup-1.8.1.jar:`
        `../lib/quickcheck-0.6.jar: \
     ../java/ru/ifmo/rain/golovin/nets/HelloUDPClient.java

java -cp \
        ../artifacts/HelloUDPTest.jar:`
        `../lib/hamcrest-core-1.3.jar:`
        `../lib/junit-4.11.jar:`
        `../lib/jsoup-1.8.1.jar:`
        `../lib/quickcheck-0.6.jar:`
        `../out \
    ru.ifmo.rain.golovin.nets.HelloUDPClient $@
