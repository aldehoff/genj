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
package genj.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for interacting with environment/system settings/parameters
 */
public class EnvironmentChecker {
  
  private static Logger LOG = Logger.getLogger("genj.util");

  private final static String[] SYSTEM_PROPERTIES = {
        "java.vendor", "java.vendor.url",
        "java.version", "java.class.version",
        "os.name", "os.arch", "os.version",
        "browser", "browser.vendor", "browser.version",
        "user.name", "user.dir", "user.home", "all.home", "user.home.genj", "all.home.genj"
  };
  
  /**
   * Check for Java 1.4 and higher
   */
  public static boolean isJava14(Object receipient) {
    String version = getProperty(receipient, "java.version", "", "Checking Java VM version");
    // o.k. this should be more flexible 8)
    return version.startsWith("1.4") || version.startsWith("1.5")  || version.startsWith("1.6");
  }
  
  /**
   * Check for Mac
   */
  public static boolean isMac() {
    return System.getProperty("mrj.version")!=null;
  }
  
  /**
   * Check for Windows
   */
  public static boolean isWindows() {
    return System.getProperty("os.name").indexOf("Windows")>-1;
  }

  /**
   * Check the environment
   */
  public static void log() {
    
    // Go through system properties
    try {
      for (int i=0; i<SYSTEM_PROPERTIES.length; i++) {
        LOG.info(SYSTEM_PROPERTIES[i] + " = "+System.getProperty(SYSTEM_PROPERTIES[i]));
      }

      // check classpath
      String cpath = System.getProperty("java.class.path");
      StringTokenizer tokens = new StringTokenizer(cpath,System.getProperty("path.separator"),false);
      while (tokens.hasMoreTokens()) {
        String entry = tokens.nextToken();
        String stat = checkClasspathEntry(entry) ? " (does exist)" : "";
        LOG.info("Classpath = "+entry+stat);
      }
      
      // check classloaders
      ClassLoader cl = EnvironmentChecker.class.getClassLoader();
      while (cl!=null) {
        if (cl instanceof URLClassLoader) {
          LOG.info("URLClassloader "+cl + Arrays.asList(((URLClassLoader)cl).getURLs()));
        } else {
          LOG.info("Classloader "+cl);
        }
        cl = cl.getParent();
      }
      
      // check memory
      Runtime r = Runtime.getRuntime();
      LOG.log(Level.INFO, "Memory Max={0}/Total={1}/Free={2}", new Long[]{ new Long(r.maxMemory()), new Long(r.totalMemory()), new Long(r.freeMemory()) });

      // DONE
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "Couldn't test for system properties", t);
    }
  }

  /**
   * check individual classpath entry
   */
  private static boolean checkClasspathEntry(String entry) {
    try {
      return new File(entry).exists();
    } catch (Throwable t) {
    }
    return false;
  }

  /**
   * Returns a (system) property
   */
  public static String getProperty(Object receipient, String key, String fallback, String msg) {
    return getProperty(receipient, new String[]{key}, fallback, msg);
  }

  /**
   * Returns a (system) property
   */
  public static String getProperty(Object receipient, String[] keys, String fallback, String msg) {
    // see if one key fits
    String key = null, val, postfix;
    try {
      for (int i=0; i<keys.length; i++) {
        // get the key
        key = keys[i];
        // there might be a prefix in there
        int pf = key.indexOf('/');
        if (pf<0) pf = key.length();
        postfix = key.substring(pf);
        key = key.substring(0,pf);
        // ask the System
        val = System.getProperty(key);
        // found it ?
        if (val!=null) {
          LOG.fine("Using system-property "+key+'='+val+" ("+msg+')');
          return val+postfix;
        }
      }
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "Couldn't access system properties", t);
    }
    // fallback
    if (fallback!=null)
      LOG.info("Using fallback for system-property "+key+'='+fallback+" ("+msg+')');
    return fallback;
  }

  /**
   * all.home - the shared home directory of all users (windows only)
   */
  static {
    
    // check the registry - this is windows only 
    if (isWindows()) {
      
      String QUERY = "reg query \"HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\ProfileList\"";
      Pattern PATTERN  = Pattern.compile(".*AllUsersProfile\tREG_SZ\t(.*)");
      String value = null;
      try {
        Process process = Runtime.getRuntime().exec(QUERY);
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while (true) {
          String line = in.readLine();
          if (line==null) break;
          Matcher match = PATTERN.matcher(line);
          if (match.matches()) {
            File home = new File(new File(System.getProperty("user.home")).getParent(), match.group(1));
            if (home.isDirectory())
              System.setProperty("all.home", home.getAbsolutePath());
            break;
          }
        }
        in.close();
      } catch (Throwable t) {
      }
    }
    // done
  }
  
  /**
   * "user.home.genj" the genj application data directory ("C:/Documents and Settings/$USER/Application Data/genj" on windows, "~/.genj" otherwise)
   */
  static {

    try {
      File user_home_genj;
      File home = new File(System.getProperty("user.home"));
      File dotgenj = new File(home, ".genj");
      File appdata = new File(home, "Application Data");
      if (!isWindows() || dotgenj.isDirectory() || !appdata.isDirectory())
        user_home_genj = dotgenj;
      else
        user_home_genj = new File(appdata, "GenJ");
      
      System.setProperty("user.home.genj", user_home_genj.getAbsolutePath());

    } catch (Throwable t) {
      // ignore if we can't access system properties
    }
    
  }
  
  /**
   * "all.home.genj" the genj application data directory ("C:/Documents and Settings/All Users/Application Data/genj" windows only)
   */
  static {

    try {
      if (isWindows()) {
        File app_data = new File(System.getProperty("all.home"), "Application Data");
        if (app_data.isDirectory())
          System.setProperty("all.home.genj", new File(app_data, "GenJ").getAbsolutePath());
      }
    } catch (Throwable t) {
      // ignore if we can't access system properties
    }
    
  }
  
}
