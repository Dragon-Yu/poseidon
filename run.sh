#!/usr/bin/env bash

jdk8=''
build=''

while [[ $# > 0 ]]
do
    key="$1"
    case ${key} in
        -8|--jdk8)
        jdk8='true'
        ;;
        -b|--build)
        build='true'
    esac
    shift
done

if [ -n "$build" ];
then
    ./gradlew build
fi

if [ -z "$jdk8" ];
then
    java -Xbootclasspath/p:lib/alpn-boot-7.1.3.v20150130.jar -cp ./lib/log4j-core-2.5.jar:./lib/poseidon.jar http2.client.ClientMain
else
    java -Xbootclasspath/p:lib/alpn-boot-8.1.4.v20150727.jar -cp ./lib/log4j-core-2.5.jar:./lib/poseidon.jar http2.client.ClientMain
fi
