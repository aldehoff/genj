@echo off

echo /**
echo  * GenJ - Start Script
echo  * 
echo  * GenealogyJ requires you to have a Java environment of version
echo  * 1.3 or higher. Please run 'java -version' in case you're unsure
echo  * of what's installed on your machine.
echo  *
echo  * If you encounter runtime problems open genj.log and send it
echo  * as an attachement to genj-developer@lists.sourceforge.net.
echo  * Also include a description of the problem.
echo  * 
echo  */

java -Dgenj.debug.file=.\genj.log -jar .\lib\genj.jar


