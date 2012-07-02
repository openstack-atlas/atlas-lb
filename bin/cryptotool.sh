#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

home=`cd "$bin/..";pwd`

pushd .
cd $home

java -jar core-tools/core-crypto-tool/target/core-crypto-tool-1.1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2

popd > /dev/null
