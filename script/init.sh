#!/usr/bin/env bash

base=$(pwd)
if [[ -L $0 ]] ; then
    path=$(readlink "$0")
else
    path=$0
fi
cd $(dirname "$path")

tar -xzvf jre.tar.gz
