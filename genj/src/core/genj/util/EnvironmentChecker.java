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
  
}
