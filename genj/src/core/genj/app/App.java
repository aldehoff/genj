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
package genj.app;

import genj.Version;
import genj.gedcom.Gedcom;
import genj.option.OptionProvider;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.window.DefaultWindowManager;
import genj.window.WindowManager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import javax.swing.SwingUtilities;

/**
 * Main Class for GenJ Application
 */
public class App {
  
  /*package*/ static Logger LOG = Logger.getLogger("genj");
  
  /**
   * GenJ Main Method
   */
  public static void main(java.lang.String[] args) {

    // Catch anything that might happen
    try {
      
      // prepare some basic logging for now
      Formatter formatter = new LogFormatter();
      Logger root = Logger.getLogger("");
      Handler[] handlers = root.getHandlers();
      for (int i=0;i<handlers.length;i++) root.removeHandler(handlers[i]);
      root.addHandler(new FlushingHandler(new StreamHandler(System.out, formatter)));
      System.setOut(new PrintStream(new LogOutputStream(Level.INFO, "System", "out")));
      System.setErr(new PrintStream(new LogOutputStream(Level.WARNING, "System", "err")));
      
      // create our home directory
      File home = new File(EnvironmentChecker.getProperty(App.class, "user.home.genj", null, "determining home directory"));
      home.mkdirs();
      if (!home.exists()||!home.isDirectory()) 
        throw new IOException("Can't initialize home directoy "+home);
      
      // init our data
      Registry registry = new Registry("genj");
      
      // initialize options first
      OptionProvider.getAllOptions(registry);

      // Setup File Logging and check environment
      Handler handler = new FileHandler(new File(home, "genj.log").getAbsolutePath(), Options.getInstance().getMaxLogSizeKB()*1024, 1, true);
      handler.setFormatter(formatter);
      LOG.addHandler(handler);
      
      // Startup Information
      LOG.info("version = "+Version.getInstance().getBuildString());
      LOG.info("date = "+new Date());
      EnvironmentChecker.log();
      
      // patch up GenJ for Mac if applicable
      if (EnvironmentChecker.isMac()) {
        LOG.info("Setting up MacOs adjustments");
        System.setProperty("apple.laf.useScreenMenuBar","true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name","GenealogyJ");
      }
      
      // check VM version
      if (!EnvironmentChecker.isJava14(App.class)) {
        if (EnvironmentChecker.getProperty(App.class, "genj.forcevm", null, "Check force of VM")==null) {
          LOG.severe("Need Java 1.4 to run GenJ");
          System.exit(1);
          return;
        }
      }
      
      // Startup the UI
      SwingUtilities.invokeLater(new Startup(registry, args));
      
      // Hook into Shutdown
      Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown(registry)));

      // Done
      
    } catch (Throwable t) {
      LOG.log(Level.SEVERE, "Cannot instantiate App", t);
      System.exit(1);
    }
  }
  
  /**
   * Our startup code
   */
  private static class Startup implements Runnable {
    
    private Registry registry;
    private String[] args;
    
    /**
     * Constructor
     */
    private Startup(Registry registry, String[] args) {
      this.registry = registry;
      this.args = args;
    }
    
    /**
     * Constructor
     */
    public void run() {
      
      LOG.info("Startup");
      
      // get app resources now
      Resources resources = Resources.get(App.class);

      // create window manager
      WindowManager winMgr = new DefaultWindowManager(new Registry(registry, "window"), Gedcom.getImage());
      
      // Disclaimer - check version and registry value
      String version = Version.getInstance().getVersionString();
      if (!version.equals(registry.get("disclaimer",""))) {
        // keep it      
        registry.put("disclaimer", version);
        // show disclaimer
        winMgr.openDialog("disclaimer", "Disclaimer", WindowManager.INFORMATION_MESSAGE, resources.getString("app.disclaimer"), Action2.okOnly(), null);    
      }
      
      // setup control center
      ControlCenter center = new ControlCenter(registry, winMgr, args);

      // show it
      winMgr.openFrame("cc", resources.getString("app.title"), Gedcom.getImage(), center, center.getMenuBar(), center.getExitAction());

      // done
      LOG.info("/Startup");
    }
        
  } //Startup

  /**
   * Our shutdown code
   */
  private static class Shutdown implements Runnable {
    
    private Registry registry;
    
    /**
     * Constructor
     */
    private Shutdown(Registry registry) {
      this.registry = registry;
    }
    /**
     * do the shutdown
     */
    public void run() {
      LOG.info("Shutdown");
	    // persist options
	    OptionProvider.persistAll(registry);
	    // Store registry 
	    Registry.persist();      
	    // done
      LOG.info("/Shutdown");
    }
  } //Shutdown

  /**
   * a log handler that flushes on publish
   */
  private static class FlushingHandler extends Handler {
    private Handler wrapped;
    private FlushingHandler(Handler wrapped) {
      this.wrapped = wrapped;
    }
    public void publish(LogRecord record) {
      wrapped.publish(record);
      flush();
    }
    public void flush() {
      wrapped.flush();
    }
    public void close() throws SecurityException {
      wrapped.close();
    }
  }
  
  /**
   * Our own log format
   */
  private static class LogFormatter extends Formatter {
    public String format(LogRecord record) {
      StringBuffer result = new StringBuffer(80);
      result.append(record.getLevel());
      result.append(":");
      result.append(record.getSourceClassName());
      result.append(".");
      result.append(record.getSourceMethodName());
      result.append(":");
      String msg = record.getMessage();
      Object[] parms = record.getParameters();
      if (parms==null||parms.length==0)
        result.append(record.getMessage());
      else 
        result.append(MessageFormat.format(msg, parms));
      result.append(":");
      Throwable t =record.getThrown();
      if (t!=null)  {
        result.append(t);
        result.append(" in ");
        
        StackTraceElement trace[] = t.getStackTrace();
        if (trace.length>0) {
          result.append(trace[0].getClassName());
          result.append(".");
          result.append(trace[0].getMethodName());
          if (t.getMessage()!=null) {
            result.append(", message=");
            result.append(t.getMessage());
          }
          if (trace[0].getLineNumber()>0) {
            result.append(", line=");
            result.append(trace[0].getLineNumber());
          }
        }
      }
      result.append(":");
      result.append(System.getProperty("line.separator"));
      return result.toString();
    }
  }
  
  /**
   * Our STDOUT/ STDERR log outputstream
   */
  private static class LogOutputStream extends OutputStream {
    
    private char[] buffer = new char[256];
    private int size = 0;
    private Level level;
    private String sourceClass, sourceMethod;
    
    /**
     * Constructor
     */
    public LogOutputStream(Level level, String sourceClass, String sourceMethod) {
      this.level = level;
      this.sourceClass = sourceClass;
      this.sourceMethod = sourceMethod;
    }
    
    /**
     * collect up to limit characters 
     */
    public void write(int b) throws IOException {
      if (b!='\n') {
       buffer[size++] = (char)b;
       if (size<buffer.length) 
         return;
      }
      flush();
    }

    /**
     * 
     */
    public void flush() throws IOException {
      if (size>0) {
        LOG.logp(level, sourceClass, sourceMethod, String.valueOf(buffer, 0, size).trim());
        size = 0;
      }
    }
  }
  
} //App
