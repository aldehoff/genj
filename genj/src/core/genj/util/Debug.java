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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Central Debugging class
 */
public class Debug {
  
  /** the intial buffering we do */
  private final static int BUFFER_SIZE = 512;
  
  /** an initial buffered output */
  private static ByteArrayOutputStream buffer = new ByteArrayOutputStream(BUFFER_SIZE);
  
  /** the output */
  private static PrintStream out = new PrintStream(buffer);
  
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
      setOutput(new PrintStream(new FileOutputStream(file.getAbsolutePath(), true)));
    } catch (Throwable t) {
      log(ERROR, Debug.class, "Failed to route debug log to "+file.getAbsoluteFile(), t);
    }
  }
  
  /**
   * Sets the PrintStream to use
   */
  public static void setOutput(PrintStream p) {
    
    synchronized (mutex) {

      // keep it    
      out = p;
      
      // something from buffer?
      if (buffer!=null) try {
        buffer.flush();
        out.write(buffer.toByteArray());
        buffer.close();
        buffer=null;
      } catch (IOException e) {
      }
      
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
   *  LEVEL:type:throwable:tmessage:message
   */
  public static void log(int level, Object source, String msg) {
    log(level, source, msg, null);
  }
  
  /**
   * Log
   *  LEVEL:type:throwable:tmessage:message
   *  ...
   */
  public static void log(int level, Object source, String msg, Throwable t) {
    
    synchronized (mutex) {
      
      // log what we need
      Class type = source instanceof Class ? (Class)source : source.getClass();
      
      StringBuffer buf = new StringBuffer(120);
      
      buf.append(LEVELS[level]);
      buf.append(':');
      buf.append(type.getName());
      buf.append(':');
      if (t!=null) buf.append(t.getClass().getName());
      buf.append(':');
      if (t!=null) buf.append(t.getMessage()!=null?t.getMessage():"");
      buf.append(':');
      if (msg!=null) buf.append(msg);
      
      out.println(buf.toString());
      if (level==ERROR) {
        t.printStackTrace(out);
      }
      
      if ((out!=System.out)&&out.checkError()) {
        out = System.out;
        log(ERROR, Debug.class, "Problems sending debugging to log - switching to System.out");
        log(level, source, msg, t);
      }
      
      // check buffer state
      if ((buffer!=null)&&(buffer.size()>BUFFER_SIZE)) {
        setOutput(System.out);
      }
    }
  
  }
  
  /**
   * Log
   */
  public static void log(int level, Object source, Throwable t) {
    log(level, source, "Unexpected Throwable or Exception", t);
  }
  
}
