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
import genj.io.FileAssociation;
import genj.lnf.LnFBridge;
import genj.renderer.BlueprintManager;
import genj.util.Debug;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.window.DefaultWindowManager;
import genj.window.WindowManager;

import java.awt.Dimension;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.UIManager;

/**
 * Main Class for GenJ Application
 */
public class App {
  
  /** constants */
  private final static String SWING_RESOURCES_KEY_PREFIX = "swing.";
  private final static String FRAME_KEY_PREFIX = "frame.";

  /** members */
  private Registry registry;
  private static App instance;
  private Resources resources;
  private WindowManager winMgr;

  /**
   * Application Constructor
   */
  private App() {

    // Startup Information
    Debug.log(Debug.INFO, App.class, "GenJ App - Version "+Version.getInstance()+" - "+new Date());
    String log = EnvironmentChecker.getProperty(this, new String[]{"genj.debug.file", "user.home"}, null, "choose log-file");
    if (log!=null) {
      File file = new File(log);
      if (file.isDirectory()) file = new File(file, "genj.log");
      Debug.setFile(file);
    }
    EnvironmentChecker.log();
    
    // init our data
    registry = new Registry("genj");
    
    // Check language
    String lang = getLanguage();
    if (lang!=null) try {
      Debug.log(Debug.INFO, this, "Switching language to "+lang);
      System.setProperty("user.language", lang);
    } catch (Throwable t) {}

    // Make sure that Swing shows our localized texts
    resources = Resources.get(this);
    
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

    // create window manager
    winMgr = new DefaultWindowManager(new Registry(registry, "window"));
// testing plugability of other window managers (will use for applet, too)    
//    winMgr = new LightweightWindowManager(new Registry(registry, "window"));
    
    // Set the Look&Feel
    LnFBridge.LnF lnf = LnFBridge.getInstance().getLnF(registry.get("lnf", (String)null));
    if (lnf!=null) {
      lnf.apply(lnf.getTheme(registry.get("lnf.theme", (String)null)), new Vector());
    }
    
    // Load file associations
    FileAssociation.read(registry);

    // Disclaimer - check version and registry value
    String version = Version.getInstance().toString();
    if (!version.equals(registry.get("disclaimer",""))) {
      // keep it      
      registry.put("disclaimer", version);
      // show disclaimer
      winMgr.openDialog("disclaimer", WindowManager.IMG_INFORMATION, resources.getString("app.disclaimer"), WindowManager.OPTIONS_OK, null);    
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
        // remember file associations
        FileAssociation.write(registry);
        // tell BlueprintManager
        BlueprintManager.getInstance().shutdown();
        // close all frames we know
        winMgr.closeAllFrames();
        // Store registry 
        Registry.saveToDisk();      
        // Flush Debug
        Debug.flush();
        // exit
        System.exit(0);
      }
    };
    winMgr.openFrame("cc", resources.getString("app.title"), Gedcom.getImage(), new Dimension(280,180), center, center.getMenuBar(), onClosing, onClose);

    // Done
  }
  
  /**
   * Singleton access
   */
  public static App getInstance() {
    return instance;
  }
  
  /**
   * Main of app
   */
  public static void main(java.lang.String[] args) {

    // Startup and catch Swing missing
    try {
      instance = new App();
    } catch (Throwable t) {
      Debug.log(Debug.ERROR, App.class, "Cannot instantiate App", t);
      Debug.flush();
      System.exit(1);
    }
  }
  
  /**
   * Sets the language
   */
  public void setLanguage(String lang) {
    if (lang!=null)
      registry.put("language", lang);
  }

  /**
   * Gets the language
   */
  public String getLanguage() {
    return registry.get("language", (String)null);
  }

  /**
   * Sets the LookAndFeel
   */
  public void setLnF(LnFBridge.LnF lnf, LnFBridge.Theme theme) {
    
    // collect root elements we know about
    List roots = winMgr.getRootComponents();
    
    // set it!
    if (lnf.apply(theme, roots)) {
      registry.put("lnf", lnf.getName());
      if (theme!=null) registry.put("lnf.theme", theme.getName());
    }
    
    // remember
  }
  
} //App
