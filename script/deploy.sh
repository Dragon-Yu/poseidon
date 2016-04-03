#!/usr/bin/env bash

if [ ! -d '../planet_lab/lib' ]; then
    mkdir ../planet_lab/lib
fi
cp ../lib/alpn-boot-8.1.4.v20150727.jar ../planet_lab/lib
cp ../build/libs/poseidon-all-1.0-SNAPSHOT.jar ../planet_lab
cp run.sh ../planet_lab
cp init.sh ../planet_lab
