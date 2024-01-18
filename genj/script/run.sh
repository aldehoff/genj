#!/bin/sh

# GenJ has to be run from inside it's installation directory
# so change into directory where this script was started
cd `dirname $0`

# check the script for being a symbolik link we can follow
SCRIPT=`basename $0`
while [ -h "$SCRIPT" ]; do
 SCRIPT=`ls -l $SCRIPT | grep -o '[/.[:alnum:]]*$'`
 echo "*** INFO: Following symlink $SCRIPT"
 cd `dirname $SCRIPT`
 SCRIPT=`basename $SCRIPT` done

# final check if the GenJ main archive is right here
if [ ! -e "./run.jar"  ]; then
 echo "*** ERROR: Missing GenJ resource(s) in "`pwd`
 exit 1
fi

echo "*** INFO: Running GenJ from"`pwd`

# find java
JAVA=$JAVA_HOME/bin/java
if [ ! -x "$JAVA" ]; then
 JAVA=`which java`
 if [ $? -eq 1 ]; then
  echo "*** ERROR: Can't find java executable"
  exit 1
 fi
fi

# run it
CMD="$JAVA -jar run.jar"

echo "*** INFO: Executing '$CMD'"

# end
