#!/bin/bash
./gradlew build
cd build/classes/java/main
java net.koonts.Main $1
cd ../../../..