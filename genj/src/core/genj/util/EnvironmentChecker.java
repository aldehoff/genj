package genj.util;

import java.io.File;
import java.util.StringTokenizer;

public class EnvironmentChecker {

  private final static String[] SYSTEM_PROPERTIES = {
        "java.vendor", "java.vendor.url",
        "java.version", "java.class.version",
        "os.name", "os.arch", "os.version",
        "browser", "browser.vendor", "browser.version"
  };

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
      StringTokenizer tokens = new StringTokenizer(
        cpath,
        System.getProperty("path.separator"),
        false
      );
      
      while (tokens.hasMoreTokens()) {
        String entry = tokens.nextToken();
        String stat = checkClasspathEntry(entry) ? " (does exist)" : "";
        Debug.log(Debug.INFO, EnvironmentChecker.class, "Classpath = "+entry+stat);
      }

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
    Debug.log(Debug.INFO, receipient, "Using fallback for system-property "+key+'='+fallback+" ("+msg+')');
    return fallback;
  }

  
}
