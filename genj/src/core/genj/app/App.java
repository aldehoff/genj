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
import genj.util.Debug;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.window.CloseWindow;
import genj.window.DefaultWindowManager;
import genj.window.WindowManager;

import java.io.File;
import java.util.Date;
import java.util.Iterator;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main Class for GenJ Application
 */
public class App implements Runnable {
  
  /** constants */
  private final static String SWING_RESOURCES_KEY_PREFIX = "swing.";

  /**
   * GenJ Main Method
   */
  public static void main(java.lang.String[] args) {
    new App().run();
  }
  
  /**
   * GenJ Main Method
   */
  public void run() {

    // Catch anything that might happen
    try {
      
      // Startup Information
      Debug.log(Debug.INFO, App.class, "GenJ App - Build "+Version.getInstance().getBuildString()+" started at "+new Date());
      
      // init our data
      Registry registry = new Registry("genj");
      
      // initialize options
      OptionProvider.restoreAll(registry);

      // Setup Log
      String log = EnvironmentChecker.getProperty(App.class, new String[]{"genj.debug.file", "user.home/.genj/genj.log"}, "", "choose log-file");
      if (log.length()>0) {
        File file = new File(log);
        if (file.exists()&&file.length()>Options.getInstance().getMaxLogSizeKB()*1024)
          file.delete();
        Debug.setFile(file);
      }
      EnvironmentChecker.log();
      
      // check VM version
      if (!EnvironmentChecker.isJava14(App.class)) {
        if (EnvironmentChecker.getProperty(App.class, "genj.forcevm", null, "Check force of VM")==null) {
          Debug.log(Debug.ERROR, App.class, "Need Java 1.4 to run GenJ");
          System.exit(1);
          return;
        }
      }
      
      // Startup the UI
      SwingUtilities.invokeLater(new Startup(registry));
      
      // Hook into Shutdown
      Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown(registry)));

      // Done
      
    } catch (Throwable t) {
      Debug.log(Debug.ERROR, App.class, "Cannot instantiate App", t);
      Debug.flush();
      System.exit(1);
    }
  }
  
  /**
   * Our startup code
   */
  private static class Startup implements Runnable {
    
    private Registry registry;
    
    /**
     * Constructor
     */
    private Startup(Registry registry) {
      this.registry = registry;
    }
    
    /**
     * Constructor
     */
    public void run() {
      
      Debug.log(Debug.INFO, this, "Startup");
      
      // get app resources now
      Resources resources = Resources.get(App.class);

      // Make sure that Swing shows our localized texts
      initSwing(resources);

      // create window manager
      WindowManager winMgr = new DefaultWindowManager(new Registry(registry, "window"));
      
      // Disclaimer - check version and registry value
      String version = Version.getInstance().getVersionString();
      if (!version.equals(registry.get("disclaimer",""))) {
        // keep it      
        registry.put("disclaimer", version);
        // show disclaimer
        winMgr.openDialog("disclaimer", "Disclaimer", WindowManager.IMG_INFORMATION, resources.getString("app.disclaimer"), CloseWindow.OK(), null);    
      }
      
      // setup control center
      ControlCenter center = new ControlCenter(registry, winMgr);

      // show it
      winMgr.openFrame("cc", resources.getString("app.title"), Gedcom.getImage(), center, center.getMenuBar(), center.getExitAction());

      // done
      Debug.log(Debug.INFO, this, "/Startup");
    }
    
    /**
     * Initialize Swing resources
     */  
    private static void initSwing(Resources resources) {
      
      // make some adjustment for MacOS
      //  switch menubar to be mac compatible - one on the top
      // don't know how to set dock programmatically -Xdock:name="JUnit on Mac OS X"
      System.setProperty("com.apple.macos.useScreenMenuBar", "true");
      
      // set swing resource strings (ok, cancel, etc.)
      Iterator keys = resources.getKeys();
      while (keys.hasNext()) {
        String key = (String)keys.next();
        if (key.indexOf(SWING_RESOURCES_KEY_PREFIX)==0) {
          UIManager.put(
            key.substring(SWING_RESOURCES_KEY_PREFIX.length()),
            resources.getString(key)
          );
        }
      }
      
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
      Debug.log(Debug.INFO, this, "Shutdown");
	    // persist options
	    OptionProvider.persistAll(registry);
	    // Store registry 
	    Registry.persist();      
	    // done
      Debug.log(Debug.INFO, this, "/Shutdown");
	    // Flush Debug
	    Debug.flush();
    }
  } //Shutdown
  
} //App
