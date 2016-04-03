#!/usr/bin/env bash

JAVA_HOME='jre1.8.0_51'
JAVA_CMD="$JAVA_HOME/bin/java"
exec "$JAVA_CMD" -Xbootclasspath/p:./lib/alpn-boot-8.1.4.v20150727.jar -jar poseidon-all-1.0-SNAPSHOT.jar http2.client.ClientMain
