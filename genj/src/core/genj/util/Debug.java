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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Central Debugging class
 */
public class Debug {
  
  /** the output */
  private static PrintStream out = System.out;
  
  /** a mutex */
  private final static Object mutex = new Object();
  
  /** levels */
  public final static int
    INFO = 0,
    WARNING = 1,
    ERROR = 2,
    STACK = 3;
    
  /** level messages */
  private final static String[] LEVELS = {
    "INFO", "WARNING", "ERROR", "STACKTRACE"
  };
  
  /**
   * Sets the PrintStream to use
   */
  public static void setFile(File file) {
    
    // try to open a file for debugging
    try {
      out = new PrintStream(new FileOutputStream(file, true));
    } catch (Throwable t) {
      log(ERROR, Debug.class, "Failed to route debug log to "+file.getAbsoluteFile(), t);
    }
    
    // done
  }
  
  /**
   * Flush
   */
  public static void flush() {
    out.flush();
  }
  
  /**
   * Log
   *  WARNING:type:message
   */
  public static void log(int level, Object source, String msg) {
    synchronized (mutex) {
      Class type = source instanceof Class ? (Class)source : source.getClass();
      out.println(LEVELS[level]+':'+type.getName()+':'+msg);
      if ((out!=System.out)&&out.checkError()) {
        out = System.out;
        log(ERROR, Debug.class, "Problems sending debugging to log - switching to System.out");
        log(level, source, msg);
      }
    }
  }
  
  /**
   * Log
   *  WARNING:type:message
   *  STACKTRACE:exception:message
   *  ...
   */
  public static void log(int level, Object source, String msg, Throwable t) {
    synchronized (mutex) {
      log(level,source,msg);
      log(STACK,t,t.getMessage());
      t.printStackTrace(out);
    }
  }
  
  /**
   * Log
   */
  public static void log(int level, Object source, Throwable t) {
    log(level, source, "Unexpected Throwable or Exception", t);
  }
  
}
