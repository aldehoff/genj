@echo off
setlocal

set classpath=%ANT_HOME%/lib/ant.jar;%CLASSPATH%

"%JAVA_HOME%\bin\java.exe" org.apache.tools.ant.Main %1 %2 %3 %4

:end
endlocal

