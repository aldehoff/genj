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
import genj.option.Option;
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

import javax.swing.UIManager;

/**
 * Main Class for GenJ Application
 */
public class App {
  
  /** constants */
  private final static String SWING_RESOURCES_KEY_PREFIX = "swing.";
  private final static String FRAME_KEY_PREFIX = "frame.";

  /**
   * Main of app
   */
  public static void main(java.lang.String[] args) {

    // Startup and catch Swing missing
    try {
      init();
    } catch (Throwable t) {
      Debug.log(Debug.ERROR, App.class, "Cannot instantiate App", t);
      Debug.flush();
      System.exit(1);
    }
  }
  
  /**
   * main
   */
  private static void init() {

    // Startup Information
    Debug.log(Debug.INFO, App.class, "GenJ App - Version "+Version.getInstance()+" - "+new Date());
    String log = EnvironmentChecker.getProperty(App.class, new String[]{"genj.debug.file", "user.home/.genj/genj.log"}, "", "choose log-file");
    if (log.length()>0) 
      Debug.setFile(new File(log));
    EnvironmentChecker.log();
    
    // check VM version
    if (!EnvironmentChecker.isJava14(App.class)) {
      if (EnvironmentChecker.getProperty(App.class, "genj.forcevm", null, "Check force of VM")==null) {
        Debug.log(Debug.ERROR, App.class, "Need Java 1.4 to run GenJ");
        System.exit(1);
        return;
      }
    }
    
    // init our data
    final Registry registry = new Registry("genj");
    
    // initialize options
    Option.restoreAll(registry);

    // get app resources now
    Resources resources = Resources.get(App.class);

    // Make sure that Swing shows our localized texts
    initSwing(resources);

    // create window manager
    final WindowManager winMgr = new DefaultWindowManager(new Registry(registry, "window"));
    
    // Disclaimer - check version and registry value
    String version = Version.getInstance().toString();
    if (!version.equals(registry.get("disclaimer",""))) {
      // keep it      
      registry.put("disclaimer", version);
      // show disclaimer
      winMgr.openDialog("disclaimer", "Disclaimer", WindowManager.IMG_INFORMATION, resources.getString("app.disclaimer"), CloseWindow.OK(), null);    
    }
    
    // setup control center
    final ControlCenter center = new ControlCenter(registry, winMgr);
    
    Runnable onClosing = new Runnable() {
      public void run() {
        center.getExitAction().trigger();
      }
    };
    Runnable onClose = new Runnable() {
      public void run() {
        // take a snapshot of view configuration
        center.snapshot();
        // close all frames we know
        winMgr.closeAll();
        // persist options
        Option.persistAll(registry);
        // Store registry 
        Registry.persist();      
        // Flush Debug
        Debug.flush();
        // exit - open for discussion: instead of exit() we could
        // make sure that all threads terminate nicely and the vm
        // shuts down by itself PENDING
        System.exit(0);
      }
    };
    winMgr.openFrame("cc", resources.getString("app.title"), Gedcom.getImage(), center, center.getMenuBar(), onClosing, onClose);

    // done with this thread
  }

  /**
   * Initialize Swing resources
   */  
  private static void initSwing(Resources resources) {
    
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

} //App
