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

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * ClassLoad for Reports
 */
public class ReportLoader extends ClassLoader {

  private File basedir;
  private Hashtable cache = new Hashtable();
  private Vector reports = new Vector(10);

  /**
   * Constructor
   */
  public ReportLoader(File basedir) {

    // Remember
    this.basedir = basedir;

    // Load reports
    loadReports();

  }

  /**
   * Helper that loads a class from File
   */
  private Class loadClass(File file) {

    // Look for filename
    if ( !file.exists()) {
      return null;
    }

    // Try to open file
    FileInputStream in;
    try {
      in = new FileInputStream(file);
    } catch (FileNotFoundException ex) {
      return null;
    }

    // Try to read form file
    byte classdata[] = new byte[(int)file.length()];
    try {
      if (classdata.length != in.read(classdata) ) {
        return null;
      }
      // .. and close
      in.close();
    } catch (IOException ex) {
      System.out.println("File "+file+" couldn't be loaded :(");
    }

    // Construct class
    Class c;
    try {
      c = defineClass(null,classdata,0,classdata.length);
    } catch (ClassFormatError err) {
      System.out.println("File "+file+" isn't a valid class-file :)");
      return null;
    }

    // Resolve it
    try {
      resolveClass(c);
    } catch (IncompatibleClassChangeError err) {
      System.out.println("File "+file+" is incompatible - don't ask me what that means :(");
      return null;
    }

    // Remember in cache
    cache.put(c.getName(),c);

    // Done
    return c;
  }

  /**
   * Load a class
   * @exception ClassNotFoundException in case Class couldn't be loaded
   */
  protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {

    // Check if already in cache
    Class c = (Class)cache.get(name);
    if (c!=null) {
      System.out.println("Found "+c);
      return c;
    }

		// Maybe super can load it?
		c = super.loadClass(name, resolve);
		if (c!=null) {
		  return c;
		}

    // Load from reports-directory
    File file = new File(basedir,name.replace('.',File.separatorChar)+".class");
    c = loadClass(file);
    if (c!=null) {
      return c;
    }

    // Look in default ClassLoader
    return Class.forName(name);
  }

  /**
   * Helper that loads a report from File
   */
  private void loadReport(File file) {

    // Named as "Report" ?
    if ( !file.getName().startsWith("Report") ) {
      return;
    }

    // Load class
    Class c = loadClass(file);
    if (c==null) {
      return;
    }

    // Remember as report ?
    Object o;
    try {
      o = c.newInstance();
    } catch (Exception ex) {
      return;
    }

    if (!(o instanceof Report)) {
      return;
    }

    reports.addElement(c);

    // Done
  }

  /**
   * Load available reports from basedir
   */
  public boolean loadReports() {

    // Directory to look in ?
    if (!basedir.isDirectory()) {
      return false;
    }

    // Look for class-files
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
      return (name.indexOf(".class")>0);
      }
    };

    try {

      String[] files = basedir.list(filter);

      // Create classes for files
      Class report;

      File file;
      for (int i=0;i<files.length;i++) {
        file = new File(files[i]);
        if (!file.isDirectory()) {
          loadReport(new File(basedir,file.getName()));
        }
      }

    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      return false;
    }

    return true;
  }

  /**
   * Which reports ?
   */
  public Report[] getReports() {
    Report result[] = new Report[reports.size()];
    Class c;
    for (int i=0;i<reports.size();i++) {
      c = (Class)reports.elementAt(i);
      try {
        result[i]=(Report)c.newInstance();
      } catch (Exception e) {
      }
    }
    return result;
  }

}
