#!/usr/bin/env bash

base=$(pwd)
if [[ -L $0 ]] ; then
    path=$(readlink "$0")
else
    path=$0
fi
cd $(dirname "$path")

log_url_debug='http://localhost:8080/log/api_request'
log_url_release='http://prometheus-1151.appspot.com/log/api_request'
log_url="$log_url_release"

if [ -z $poseidon_url ] || [ -z $poseidon_host ]
then
    echo 'poseidon_url or poseidon_host is not defined'
    exit 1
else
    target_url=${poseidon_url}
    target_host=${poseidon_host}
fi

host_name=`hostname`
https_transfer_time='['
http2_transfer_time='['
trace_route=`traceroute "$target_host" | tr '\n' ';'`

JAVA_HOME='./jre1.8.0_51'
JAVA_CMD="$JAVA_HOME/bin/java"
#JAVA_CMD='java'

END=20
for i in $(seq ${END})
do
    data=`"$JAVA_CMD" -Xbootclasspath/p:./lib/alpn-boot-8.1.4.v20150727.jar -jar poseidon-all-1.0-SNAPSHOT.jar http2.client.ClientMain| grep 'connection duration'| grep -o '([0-9]\{1,\})$'|grep -o '[0-9]\{1,\}'`
    https_data=`echo "$data"|tr ' ' '\n'| head -n1| tail -n1`
    http2_data=`echo "$data"|tr ' ' '\n'| head -n2| tail -n1`
    separator=', '
    if [ ${i} -eq ${END} ]; then
        separator=''
    fi
    https_transfer_time=${https_transfer_time}${https_data}${separator}
    http2_transfer_time=${http2_transfer_time}${http2_data}${separator}
done
https_transfer_time=${https_transfer_time}']'
http2_transfer_time=${http2_transfer_time}']'


post_data="{  \"host_name\": \"$host_name\", \"target_url\": \"$target_url\",  \"https_transfer_time\": $https_transfer_time, \"http2_transfer_time\": $http2_transfer_time,  \"trace_route\": \"$trace_route\"}"
echo "$post_data"
wget -qO- --header='content-type: application/json' "$log_url" --post-data="$post_data"
