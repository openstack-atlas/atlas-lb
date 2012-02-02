#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

home=`cd "$bin/..";pwd`

pushd .
cd $home

java -jar server/target/atlas-1.1.0-SNAPSHOT.jar start

popd