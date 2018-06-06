#!/bin/bash

# execure from java-course/scripts

rm ../out -rf
mkdir ../out # 2> /dev/null


javac -d ../out ../java/ru/ifmo/rain/golovin/filemanager/FileManager.java

java -cp ../out ru.ifmo.rain.golovin.filemanager.FileManager