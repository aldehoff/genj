#!/bin/sh
# Modifiy the two environment variables to meet your needs
GENJ_INSTALL_DIR=~/gj
GENJ_CONSOLE_INSTALL_DIR=~/gj-console
java -classpath $GENJ_INSTALL_DIR/lib/genj.jar:$GENJ_CONSOLE_INSTALL_DIR/lib/console.jar com.sadinoff.genj.console.Console "$@"
