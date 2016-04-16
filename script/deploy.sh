#!/usr/bin/env bash
base=$(pwd)
if [[ -L $0 ]] ; then
    path=$(readlink "$0")
else
    path=$0
fi
cd $(dirname "$path")

if [ ! -d '../planet_lab/lib' ]; then
    mkdir ../planet_lab/lib
fi
if [ -d '../planet_lab/jre1.8.0_51' ]; then
    rm -rf ../planet_lab/jre1.8.0_51
fi
cp ../lib/alpn-boot-8.1.4.v20150727.jar ../planet_lab/lib
cp ../build/libs/poseidon-all-1.0-SNAPSHOT.jar ../planet_lab
cp run.sh ../planet_lab
cp init.sh ../planet_lab
