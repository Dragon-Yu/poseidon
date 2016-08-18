#!/usr/bin/env bash

base=$(pwd)
if [[ -L $0 ]] ; then
    path=$(readlink "$0")
else
    path=$0
fi
cd $(dirname "$path")

JAVA_HOME='./jre1.8.0_51'
JAVA_CMD="$JAVA_HOME/bin/java"

if [ 'lixuebingdeMacBook-Pro.local' = `hostname` ]
then
    JAVA_CMD='java'
fi

if [ -z $times ]
then
    times=1
fi

for i in $(seq ${times})
do
    ${JAVA_CMD} -Xbootclasspath/p:./lib/alpn-boot-8.1.4.v20150727.jar -cp poseidon-all-1.0-SNAPSHOT.jar main.MultiConnMain
done
