#!/bin/sh
#

export CLASSPATH=$CLASSPATH:$ANT_HOME/lib/ant.jar:$ANT_HOME/ant.jar

$JAVA_HOME/bin/java -Dant.home=$ANT_HOME org.apache.tools.ant.Main $1 $2 $3

