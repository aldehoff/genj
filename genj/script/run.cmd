@echo off


echo /**
echo  * GenJ - Start Script
echo  * 
echo  * If something goes wrong now you probably have an incomplete 
echo  * CLASSPATH. GenJ requires genj.jar which is assumed to reside 
echo  * in ./lib at this point. If you're JDK 1.1.x you'll also have
echo  * to provide swing.jar which is expected in ./lib, too.
echo  *
echo  * If you encounter runtime problems open genj.log and send it
echo  * as an attachement to genj-developer@lists.sourceforge.net.
echo  * Also include a description of the problem.
echo  * 
echo  */

set LANG=.\lib\genj_de.zip;.\lib\genj_en.zip;.\lib\genj_fr.zip;.\lib\genj_it.zip;.\lib\genj_es.zip
set CLASSPATH=.\lib\genj.jar;.\lib\swing.jar;.\lib\jhbasic.jar;%LANG%;%CLASSPATH%

java -Dgenj.debug.file=.\genj.log genj.app.App


