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
import genj.util.Debug;
import java.io.*;
import java.net.*;

/**
 * ClassLoad for Reports
 */
public class ReportLoader extends ClassLoader {

  private File basedir;
  private Hashtable cache = new Hashtable();
  private Vector reports = new Vector(10);
  private static boolean warnedAboutClasspath = false;

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
      Debug.log(Debug.WARNING, this,"File "+file+" couldn't be loaded :(");
    }

    // Get the class
    try {
      // transform bytes into class
      Class c = defineClass(null,classdata,0,classdata.length);
      // resolve (link) it
      resolveClass(c);
      // Remember in cache
      cache.put(c.getName(),c);
      // done
      return c;
    } catch (ClassFormatError err) {
      Debug.log(Debug.WARNING, this,"File "+file+" isn't a valid class-file :(");
    } catch (IncompatibleClassChangeError err) {
      Debug.log(Debug.WARNING, this, err);
    } catch (Throwable t) {
      Debug.log(Debug.ERROR, this,"Class in "+file.getName()+" couldn't be resolved",t);
    }

    // Done (failed)
    return null;
  }

  /**
   * Overriden class loading
   * @exception ClassNotFoundException in case Class couldn't be loaded
   */
  protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {

    // Check if already in cache
    Class c = (Class)cache.get(name);
    if (c!=null) {
      return c;
    }

    // Maybe super can load it?
    try {
      c = super.loadClass(name, resolve);
      if (c!=null) return c;
    } catch (Throwable t) {
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

    // Has to be a file
    if (file.isDirectory()) {
      return;
    }
    
    // Classfile that is
    if (file.getName().indexOf(".class")<0) {
      return;
    }

    // Named as "ReportXYZ.class" ?
    if ( !file.getName().startsWith("Report") ) {
      return;
    }

    // Please no inner type!
    if ( file.getName().indexOf("$")>0 ) {
      return;
    }
    
    // Getting the report type
    Class type;
              
    try {
                    
      // If the report can be loaded by the default
      // classloader then we fall back on that one
      String name = file.getAbsoluteFile().getName().replace(File.separatorChar,'.');
      name = name.substring(0,name.lastIndexOf(".class"));
      type = super.loadClass(name, true);

      if (!warnedAboutClasspath) {
        warnedAboutClasspath = true;
        Debug.log(Debug.INFO, this, "Reports are in the Classpath and can't be reloaded");
      }
      
    } catch (Throwable t) {

      type = loadClass(file);
    
    }

    if (type==null) {
      return;
    }
        
    // Getting an instance
    Object instance;
    try {
      instance = type.newInstance();
    } catch (Throwable t) {
      Debug.log(Debug.WARNING, this,"Couldn't load report "+file, t);
      return;
    }

    // Remember as report?
    if (!(instance instanceof Report)) {
      return;
    }

    // Remember!
    reports.addElement(type);

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
    try {

      String[] files = basedir.list();

      // Create classes for files
      Class report;

      File file;
      for (int i=0;i<files.length;i++) {
        file = new File(files[i]);
        loadReport(new File(basedir,file.getName()));
      }

    } catch (Exception ex) {
      Debug.log(Debug.WARNING, this,ex);
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
