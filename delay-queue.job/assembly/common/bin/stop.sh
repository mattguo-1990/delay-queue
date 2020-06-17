#!/bin/bash

cd $(dirname $0)

source ./init.sh
source /home/publish_product/server_java/dolphin_excutor/shell/control_shell.sh

STOP ${S_APP_BASE_DIR} ${APP_NAME}
