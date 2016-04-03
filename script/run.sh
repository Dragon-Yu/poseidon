#!/usr/bin/env bash

java -Xbootclasspath/p:./lib/alpn-boot-8.1.4.v20150727.jar -cp -jar poseidon-all-1.0-SNAPSHOT.jar http2.client.ClientMain
