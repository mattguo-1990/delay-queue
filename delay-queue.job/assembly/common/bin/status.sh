#!/bin/bash


cd $(dirname $0)

source ./init.sh

PIDS=$(ps -ef | grep java | grep "$APP_NAME" | grep -v grep | awk '{print $2}')
if [ -z "$PIDS" ]; then
    echo "The $APP_NAME stopped!"
    exit 0
else
	echo "The $APP_NAME running!"
	exit 0
fi
