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
public class ReportLoader {

  /** reports we have */
  private List instances = new ArrayList(10);

  /**
   * Constructor
   */
  public ReportLoader(File basedir) {
    
    // load reports
    if (!basedir.isDirectory()) {
      return;
    }
    
    // Look into report directory
    List 
      reports = new ArrayList(),
      classpath = new ArrayList();

    try {      
      classpath.add(basedir.toURL());
      
      String[] files = basedir.list();
      for (int i=0;i<files.length;i++) {
        File file = new File(basedir, files[i]);
        String report = isReport(file);
        if (report!=null) reports.add(report);
        if (isLibrary(file)) {
          Debug.log(Debug.INFO, this, "report library "+file.toURL());
          classpath.add(file.toURL());
        } 
      }
      
    } catch (MalformedURLException e) {
      // n/a
    }
    
    // Prepare classloader
    URLClassLoader cl = new URLClassLoader((URL[])classpath.toArray(new URL[classpath.size()]));
    
    // Load reports
    Iterator rs = reports.iterator();
    while (rs.hasNext()) {
      String rname = rs.next().toString(); 
      try {
        Report r = (Report)cl.loadClass(rname).newInstance();
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
  private String isReport(File file) {
    if ( file.isDirectory() ||
         !file.getName().endsWith(".class") ||
         !file.getName().startsWith("Report") ||
         file.getName().indexOf("$")>0 )
      return null;
    String name = file.getName();
    return name.substring(0, name.length()-".class".length());
  }

  /**
   * Which reports do we have
   */
  public Report[] getReports() {
    return (Report[])instances.toArray(new Report[instances.size()]);
  }

} //ReportLoader
