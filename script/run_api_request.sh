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

if [ -z $times ]
then
    times=20
fi

for i in $(seq ${times})
do
    ${JAVA_CMD} -Xbootclasspath/p:./lib/alpn-boot-8.1.4.v20150727.jar -jar poseidon-all-1.0-SNAPSHOT.jar http2.client.ClientMain
done
