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
    int rc =Main.compile((String[])args.toArray(new String[args.size()]), out);
    
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
updated              = $Date: 2007-01-29 04:51:54 $

name                 = Recompile Reports
name.de              = Reports rekompilieren 

info = This report recompiles all reports in ./report. After restart of GenJ or by pressing the reload button in ReportView they are reloaded into GenJ.
info.de = Dieser Report kompiliert alle Reports in ./report. Nach Neustart von GenJ oder einem Klick auf den Schalter fuer 'Neu Einladen' in ReportView werden sie neu eingeladen.

nosources = No sources in {0}
nosources.de = Keine Quelldateien in {0}

javac.success = {0} Sources (*.java) compiled into {1} - to activate press 'Reload report classes'
javac.success.de = {0} Quelldateien (*.java) kompiliert nach {1} - zum Aktivieren 'Neu Einladen' klicken 

javac.error = Compilation failed - check compiler output above
javac.error.de = Kompilierung fehlgeschlagen - bitte Fehlermeldung(en) oben konsultieren

*********************************************************************************************************************/
