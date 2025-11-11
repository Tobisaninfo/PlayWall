#!/usr/bin/env bash

JAR_NAME=$1

java -XX:AOTCacheOutput=PlayWallServerAOT -Dspring.main.web-application-type=NONE -jar "$JAR_NAME" &
sleep 15
cp PlayWallServerAOT ../../PlayWallClient/target/build/server/
