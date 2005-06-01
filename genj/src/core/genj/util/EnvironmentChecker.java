package genj.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvironmentChecker {

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
    return version.startsWith("1.4") || version.startsWith("1.5");
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
        Debug.log(Debug.INFO, EnvironmentChecker.class, SYSTEM_PROPERTIES[i] + " = "+System.getProperty(SYSTEM_PROPERTIES[i]));
      }

      // check classpath
      String cpath = System.getProperty("java.class.path");
      StringTokenizer tokens = new StringTokenizer(cpath,System.getProperty("path.separator"),false);
      while (tokens.hasMoreTokens()) {
        String entry = tokens.nextToken();
        String stat = checkClasspathEntry(entry) ? " (does exist)" : "";
        Debug.log(Debug.INFO, EnvironmentChecker.class, "Classpath = "+entry+stat);
      }
      
      // check classloaders
      ClassLoader cl = EnvironmentChecker.class.getClassLoader();
      while (cl!=null) {
        if (cl instanceof URLClassLoader) {
          Debug.log(Debug.INFO, EnvironmentChecker.class, "URLClassloader "+cl + Arrays.asList(((URLClassLoader)cl).getURLs()));
        } else {
          Debug.log(Debug.INFO, EnvironmentChecker.class, "Classloader "+cl);
        }
        cl = cl.getParent();
      }

      // DONE
    } catch (Throwable t) {
      Debug.log(Debug.INFO, EnvironmentChecker.class, "Couldn't test for system properties");
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
          Debug.log(Debug.INFO, receipient, "Using system-property "+key+'='+val+" ("+msg+')');
          return val+postfix;
        }
        // next one
        Debug.log(Debug.INFO, receipient, "Tried system-property "+key+" ("+msg+')');
      }
    } catch (Throwable t) {
      Debug.log(Debug.WARNING, receipient, "Couldn't access system-properties", t);
    }
    // fallback
    if (fallback!=null)
      Debug.log(Debug.INFO, receipient, "Using fallback for system-property "+key+'='+fallback+" ("+msg+')');
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

    File user_home_genj;
    File home = new File(System.getProperty("user.home"));
    File dotgenj = new File(home, ".genj");
    File appdata = new File(home, "Application Data");
    if (!isWindows() || dotgenj.isDirectory() || !appdata.isDirectory())
      user_home_genj = dotgenj;
    else
      user_home_genj = new File(appdata, "GenJ");
    
    System.setProperty("user.home.genj", user_home_genj.getAbsolutePath());
    
  }
  
  /**
   * "all.home.genj" the genj application data directory ("C:/Documents and Settings/All Users/Application Data/genj" windows only)
   */
  static {
    
    if (isWindows()) {
      File app_data = new File(System.getProperty("all.home"), "Application Data");
      if (app_data.isDirectory())
        System.setProperty("all.home.genj", new File(app_data, "GenJ").getAbsolutePath());
    }
    
  }
  
}
