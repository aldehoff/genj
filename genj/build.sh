#!/bin/sh
#

export CLASSPATH=$CLASSPATH:$ANT_HOME/lib/ant.jar

if [[ $1 = "" ]] ; then

	$JAVA_HOME/bin/java -Dant.home=$ANT_HOME org.apache.tools.ant.Main clean
	$JAVA_HOME/bin/java -Dant.home=$ANT_HOME org.apache.tools.ant.Main dist

elif [[ $1 = "clean" ]] ; then
	echo "We clean everything"
	$JAVA_HOME/bin/java -Dant.home=$ANT_HOME org.apache.tools.ant.Main $1

elif [[ $1 = "init" ]] ; then
	echo "We make the init directories"
	$JAVA_HOME/bin/java -Dant.home=$ANT_HOME org.apache.tools.ant.Main $1
	
elif [[ $1 = "version" ]] ; then
	echo "We establish the version number"
	$JAVA_HOME/bin/java -Dant.home=$ANT_HOME org.apache.tools.ant.Main $1

elif [[ $1 = "dist" ]] ; then
	echo "We make the distribution package"
	$JAVA_HOME/bin/java -Dant.home=$ANT_HOME org.apache.tools.ant.Main $1

elif [[ $1 = "javadoc" ]] ; then
	echo "We create the API"
	$JAVA_HOME/bin/java -Dant.home=$ANT_HOME org.apache.tools.ant.Main $1

elif [[ $1 = "run" ]] ; then
	echo "We build Genj to test a run"
	$JAVA_HOME/bin/java -Dant.home=$ANT_HOME org.apache.tools.ant.Main $1

else
	echo ""
	echo "Wrong parameter - Please check again a valid parameter."
	echo ""
	break

fi
