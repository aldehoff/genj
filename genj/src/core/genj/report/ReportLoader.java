/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.report;

import genj.util.Debug;
import genj.util.EnvironmentChecker;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * ClassLoad for Reports
 */
/*package*/ class ReportLoader {

  /** reports we have */
  private List instances = new ArrayList(10);
  
  /** report files */
  private List reports = new ArrayList(10);
  
  /** classpath */
  private List classpath = new ArrayList(10);
  
  /** whether reports are in classpath */
  private boolean isReportsInClasspath = false;
  
  /** a singleton */
  private static ReportLoader singleton;
  
  /**
   * Clears the report loader's state effectively forcing a reload
   */
  /*package*/ static void clear() {
    singleton = null;
  }
  
  /** 
   * Access
   */
  /*package*/ static ReportLoader getInstance() {
    
    // not known yet?
    if (singleton==null)
      singleton = new ReportLoader();
      
    // done
    return singleton;
      
  }

  /**
   * Constructor
   */
  private ReportLoader() {

    // where are the reports 
    String dir = EnvironmentChecker.getProperty(
      ReportLoader.class,
      new String[]{ "genj.report.dir", "user.dir/report"},
      "./report",
      "find report class-files"
    );
    File base = new File(dir);
    
    Debug.log(Debug.INFO, ReportLoader.class,"Reading reports from "+base);
      
    // parse report directory
    try {
      classpath.add(base.toURL());
    } catch (MalformedURLException e) {
      // n/a
    }
    parseDir(base, null);
    
    // Prepare classloader
    URLClassLoader cl = new URLClassLoader((URL[])classpath.toArray(new URL[classpath.size()]));
    
    // Load reports
    Iterator rs = reports.iterator();
    while (rs.hasNext()) {
      String rname = rs.next().toString(); 
      try {
        Report r = (Report)cl.loadClass(rname).newInstance();
        if (!isReportsInClasspath&&r.getClass().getClassLoader()!=cl) {
          Debug.log(Debug.WARNING, this, "Reports are in classpath and can't be reloaded");
          isReportsInClasspath = true;
        }
        instances.add(r);
      } catch (Throwable t) {
        Debug.log(Debug.WARNING, this, "Failed to instantiate "+rname, t);
      }
    }
    
    // sort 'em
    Collections.sort(instances, new Comparator() { 
      public int compare(Object a, Object b) {
        return ((Report)a).getName().compareTo(((Report)b).getName());
      }
    });
    
    // done
  }
  
  /**
   * Parse directory for lib- and report files
   */
  private void parseDir(File dir, String pkg) { 

    // loop files and directories
    String[] files = dir.list();
    for (int i=0;i<files.length;i++) {
      File file = new File(dir, files[i]);
      
      // dir?
      if (file.isDirectory()) {
        parseDir(file, (pkg==null?"":pkg+".")+file.getName());
        continue;
      }

      // report class file?
      String report = isReport(file, pkg);
      if (report!=null) {
        reports.add(report);
        continue;
      } 
      
      // library?
      if (isLibrary(file)) {
        try {
          Debug.log(Debug.INFO, this, "report library "+file.toURL());
          classpath.add(file.toURL());
        } catch (MalformedURLException e) {
          // n/a
        }
      }
      
      // next 
    }
      
    // done
  }
  
  /**
   * Criteria for library
   */
  private boolean isLibrary(File file) {
    return 
      !file.isDirectory() &&
      (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"));
  }
  
  /**
   * Criteria for report
   */
  private String isReport(File file, String pkg) {
    if ( (pkg!=null&&pkg.startsWith("genj")) || 
         file.isDirectory() ||
         !file.getName().endsWith(".class") ||
         !file.getName().startsWith("Report") ||
         file.getName().indexOf("$")>0 )
      return null;
    String name = file.getName();
    return (pkg==null?"":pkg+".") + name.substring(0, name.length()-".class".length());
  }

  /**
   * Which reports do we have
   */
  /*package*/ Report[] getReports() {
    return (Report[])instances.toArray(new Report[instances.size()]);
  }

  /**
   * Save options of all reports
   */
  /*package*/ void saveOptions() {
    Report[] rs = getReports();
    for (int r=0;r<rs.length;r++)
      rs[r].saveOptions();
  }
  
  /**
   * Whether reports are in classpath
   */
  /*package*/ boolean isReportsInClasspath() {
    return isReportsInClasspath;
  }

} //ReportLoader
