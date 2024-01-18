/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even 
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.report.Report;
import genj.report.ReportLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sun.tools.javac.Main;

/**
 * GenJ - ReportRecompile - recompile all reports
 */
public class ReportRecompile extends Report {

  private final static String[] OPTIONS = {
      "-g", 
      "-nowarn",
      "-source", "1.4", 
      "-target", "1.4",
      "-encoding", "utf8",
  };
  
  /**
   * main entry to the reports
   */
  public void start(Gedcom gedcom) {
    
    // prepare args
    List args = new ArrayList();
    
    for (int i = 0; i < OPTIONS.length; i++) 
      args.add(OPTIONS[i]);

    File reports = ReportLoader.getReportDirectory();
    args.add("-d");
    args.add(reports.getAbsolutePath());
    
    // collect all .java files
    int sources = findSources(reports, args);
    if (sources==0) {
      println(translate("nosources", reports));
      return;
    }
    
    // do the compile
    int rc;
    try {
      rc =Main.compile((String[])args.toArray(new String[args.size()]), out);
    } catch (NoClassDefFoundError e) {
      println(translate("javac.jre", System.getProperty("java.home")));
      return;
    }
    
    // done
    if (rc==0)
      println(translate("javac.success", new Object[]{ ""+sources, reports}));
    else {
      println("---");
      println(translate("javac.error"));
    }
    
  }
  
  /**
   * Look for source files recursively
   */
  private int findSources(File dir, List args) {
    File[] files = dir.listFiles();
    if (files==null||files.length==0)
      return 0;
    int found = 0;
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      if (file.isDirectory()) {
        found += findSources(file, args);
      } else if (file.getName().endsWith(".java")) {
        args.add(file.toString());
        found++;
      }
    }
    return found;
  }
  
}

/*********************************************************************************************************************
author               = Nils Meier
version              = 1.0
category             = Utilities
updated              = $Date: 2007-01-29 08:35:33 $

name                 = Recompile Reports
name.de              = Reports rekompilieren 
name.fr              = Compilation des Rapports

info = This report recompiles all reports in GenJ's report directory (./report). After a restart of GenJ 
 or by pressing the reload button in ReportView recompiled reports can be reloaded into GenJ.\n\n
 For the compilation to succeed GenJ needs to be run inside a JDK's Java Virtual Machine, not in 
 a JRE's Java Virtual Machine.
 
info.de = Dieser Report kompiliert alle Reports im GenJ Reportverzeichnis (./report). Nach Neustart von 
 GenJ oder einem Klick auf den Schalter 'Neu Einladen' in ReportView können kompilierte Reports neu eingeladen werden.\n\n
 Die Kompilierung funktioniert nur, wenn GenJ in einer JDK Java Virtual Machine, nicht in einer JRE JVM, läuft.

info.fr =  <h1><center>Outils de Compilation des Rapports</center></h1><br>
 <div style="margin-left: 5px; text-align:justify; 
 margin-right: 10px; font-style: normal; color: #000000">
 Ce rapport vous sert à recompiler tous les rapports qui se trouvent 
 dans le répertoire des rapports de GenJ (./report).<br>
 Aprés un redémarrage de GenJ ou aprés avoir cliqué sur le bouton de 
 rechargement des Scripts, tous les rapports nouvellement compilés 
 pourront être utilisés par GenJ.<br>
 <br>
 Pour que cela fonctionne, il vous faut cependant que GenJ tourne 
 à l'intérieur du Machine Virtuelle Java JDK, et non une Machine 
 Virtuelle Java JRE. Pour faire simple, il faut que le programme 
 Java que vous avez téléchargé sur le site de Sun commence par "jdk", 
 et non par "jre". C'est tout.<br>
 <br>
 Vous pouvez trouver tout ce qu'il vous faut ici, sauf si le lien 
 a changé&nbsp;:<br>
 <a href="http://java.sun.com/javase/downloads/index.jsp" target="_blank">Site de Sun</a>.
 </div>
 <br>

nosources    = No sources in {0}
nosources.de = Keine Quelldateien in {0}
nosources.fr = Il n'y a pas de fichier Source dans {0}

javac.success    = {0} Sources (*.java) compiled into {1} - to activate press 'Reload report classes'
javac.success.de = {0} Quelldateien (*.java) kompiliert nach {1} - zum Aktivieren 'Neu Einladen' klicken 
javac.success.fr = {0} Les Sources (*.java) ont été compilées dans 
 le répertoire {1} - Pour que cette compilation soit prise en 
 considération, veuillez cliquer sur le bouton 'Recharger les Scripts de 
 lancement des Rapports' (en bas de la fenêtre, le 4ème à partir de la gauche)

javac.error    = Compilation failed - check compiler output above
javac.error.de = Kompilierung fehlgeschlagen - bitte Fehlermeldung(en) oben konsultieren
javac.error.fr = La compilation a échouée - Vérifiez les messages de 
 sortie du compilateur ci-dessus.

javac.jre    = Reports can't be recompiled - make sure GenJ is run inside a JDK's Java Virtual Machine (java.home={0}) 
javac.jre.de = Reports können nicht kompiliert werden - GenJ muß in einer JDK Java Virtual Machine ablaufen (java.home={0}) 
javac.jre.fr = \nLes Rapports n'ont pas pu etre compilés.\n
 Assurez-vous que GenJ est lancé par l'intermédiaire de la machine 
 virtuelle Java JDK (JDK et non JRE).\n\n     Pour information : (java.home={0}) 

*********************************************************************************************************************/
