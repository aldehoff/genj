@echo off
echo /**
echo  * GenJ - Start Script
echo  * 
echo  * If something goes wrong now you probably have an
echo  * incomplete CLASSPATH. Either copy swing.jar to
echo  * the ./lib directory or specify its location via
echo  *   set CLASSPATH=c:\swing\swing.jar
echo  * before starting this script.
echo  */

set LANG=./lib/genj_de.zip;./lib/genj_en.zip;./lib/genj_fr.zip
set CLASSPATH=./lib/genj.jar;./lib/swing.jar;./lib/jhbasic.jar;%LANG%;%CLASSPATH%
java genj.app.App

