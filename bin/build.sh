#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

home=`cd "$bin/..";pwd`

pushd .
cd $home

mvn clean install

popd