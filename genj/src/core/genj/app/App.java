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

import javax.swing.*;

import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.net.*;

import genj.gedcom.*;
import genj.util.*;
import genj.lnf.LnFBridge;

/**
 * Main Class for GenJ Application
 */
public class App {

  /** constants */
  private final static String SWING_RESOURCES_KEY_PREFIX = "swing.";

  /** members */
  private static boolean doDisclaimer=false;
  private Registry registry = Registry.getRegistry("genj");
  private JFrame frame;
  private ControlCenter center;
  private Hashtable openFrames = new Hashtable();
  private static App instance;

  final static Resources resources = new Resources("genj.app");

  /**
   * Application Constructor
   */
  private App() {

    // Startup Information
    String cpath = System.getProperty("java.class.path");
    StringTokenizer tokens = new StringTokenizer(
      cpath,
      System.getProperty("path.separator"),
      false
    );
    while (tokens.hasMoreTokens())
      System.out.println("[Debug]Classpath contains "+tokens.nextToken());

    // Make sure that Swing shows our localized texts
    Enumeration keys = resources.getKeys();
    while (keys.hasMoreElements()) {
      String key = (String)keys.nextElement();
      if (key.indexOf(SWING_RESOURCES_KEY_PREFIX)==0) {
        UIManager.put(
          key.substring(SWING_RESOURCES_KEY_PREFIX.length()),
          resources.getString(key)
        );
      }
    }
    
    // Set the Look&Feel
    LnFBridge.LnF lnf = LnFBridge.getInstance().getLnF(registry.get("lnf", (String)null));
    if (lnf!=null) {
      lnf.apply(lnf.getTheme(registry.get("lnf.theme", (String)null)), new Vector());
    }

    // Disclaimer
    if (registry.get("disclaimer",0)==0) {

      registry.put("disclaimer",1);

      JTextPane tpane = new JTextPane();
      tpane.setText(resources.getString("app.disclaimer"));
      JScrollPane spane = new JScrollPane(tpane) {
        public Dimension getPreferredSize() {
          return new Dimension(256,128);
        }
      };

      JOptionPane.showMessageDialog(null,spane,"Disclaimer",JOptionPane.INFORMATION_MESSAGE);

    }

    // Create frame
    frame = createFrame(resources.getString("app.title"),Images.imgGedcom,"main", null);

    // Create the desktop
    center = new ControlCenter(frame,registry);
    frame.getContentPane().add(center);

    // Menu-Bar
    frame.setJMenuBar(center.getMenu());

    // Show it
    frame.pack();
    frame.show();

    // Done
  }
  
  /**
   * Singleton access
   */
  public static App getInstance() {
    return instance;
  }
  
  /**
   * Shutdown
   */
  public void shutdown() {
    
    // close all frames we know
    Enumeration e = App.getInstance().getFrames().elements();
    while (e.hasMoreElements()) ((JFrame)e.nextElement()).dispose();
    // Store registry 
    Registry.saveToDisk();      
    // exit
    System.exit(0);
  }

  /**
   * Main of app
   */
  public static void main(java.lang.String[] args) {

    // Startup and catch Swing missing
    try {
      instance = new App();
    } catch (NoClassDefFoundError err) {
      System.out.println("Error: Swing is missing - please install v1.1 and put swing.jar in your CLASSPATH!");
      err.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Returns a previously opened Frame by key
   */
  public JFrame getFrame(String key) {
    return (JFrame)openFrames.get(key);
  }
  
  /**
   * Returns all know JFrames that have been opened
   */
  public Hashtable getFrames() {
    return openFrames;
  }

  /**
   * Creates a Frame which remembers it's position from last time
   */
  public JFrame createFrame(String title, ImgIcon image, final String key, final Dimension dimension) {

    final String resource = "frame."+key;

    // Create the frame
    JFrame frame = new JFrame(title) {
      // LCD
      /** Disposes of this frame */
      public void dispose() {
        registry.put(resource,getBounds());
        openFrames.remove(key);
        super.dispose();
      }
      /** Packs this frame to optimal/remembered size */
      public void pack() {
        Rectangle box = registry.get(resource,(Rectangle)null);
        if ((box==null)&&(dimension!=null)) box = new Rectangle(0,0,dimension.width,dimension.height);
        if (box==null) {
          super.pack();
        } else {
          setBounds(new AreaInScreen(box));
        }
        invalidate();
        validate();
        doLayout();
      }
      // EOC
    };

    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    if (image!=null) {
      frame.setIconImage(image.getImage());
    }

    openFrames.put(key,frame);

    // Done
    return frame;
  }

  /**
   * Sets the LookAndFeel
   */
  public void setLnF(LnFBridge.LnF lnf, LnFBridge.Theme theme) {
    
    // collect frames we know about
    Vector uis = new Vector();
    Enumeration frames = getFrames().elements();
    while (frames.hasMoreElements()) uis.add(frames.nextElement());
    
    // set it!
    if (lnf.apply(theme, uis)) {
      registry.put("lnf", lnf.getName());
      if (theme!=null) registry.put("lnf.theme", theme.getName());
    }
    
    // remember
  }


}
