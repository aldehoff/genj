#!/bin/sh

cd `dirname $0`

if [ -w "./run.jar" ]; then
  java -jar run.jar
else
  echo "*** ERROR: Missing GenJ resource(s) in "`dirname $0`
fi

