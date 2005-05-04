#!/bin/sh
#

export CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar:./contrib/ant/ant.jar

$JAVA_HOME/bin/java org.apache.tools.ant.Main $1 $2 $3

