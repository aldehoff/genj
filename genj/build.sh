#!/bin/sh
#

export CLASSPATH=$CLASSPATH:$ANT_HOME/lib/ant.jar:$ANT_HOME/ant.jar
export PATH=$JAVA_HOME/bin
java org.apache.tools.ant.Main $1 $2 $3

