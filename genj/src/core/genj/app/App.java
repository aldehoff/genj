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

  final static Resources resources = new Resources("genj.app");

  /**
   * Listening to Window Events
   */
  private class AppWindowListener implements WindowListener {

    /**
     * Invoked when a window is activated.
     */
    public void windowActivated(WindowEvent e) {
    }

    /**
     * Invoked when a window has been closed.
     */
    public void windowClosed(WindowEvent e) {

      // Store registry so that current settings
      // are available next time
      Registry.saveToDisk();

      // Current frame ?
      if (e.getSource() == frame){
        System.exit(0);
      }
  
    // Done
    }

    /**
     * Invoked when a window is in the process of being closed.
     * The close operation can be overridden at this point.
     */
    public void windowClosing(WindowEvent e) {

      // Remember position
      registry.put("frame",frame.getBounds());
      center.close();

      // Done
    }

    /**
     * Invoked when a window is deactivated.
     */
    public void windowDeactivated(WindowEvent e) {
    }

    /**
     * Invoked when a window is de-iconified.
     */
    public void windowDeiconified(WindowEvent e) {
    }

    /**
     * Invoked when a window is iconified.
     */
    public void windowIconified(WindowEvent e) {
    }

    /**
     * Invoked when a window has been opened.
     */
    public void windowOpened(WindowEvent e) {
    }

  }

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
//    UIManager.put("OptionPane.yesButtonText", "Natuerlich");

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

    // Make ToolTip HeavyWeight
    //ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

    // Create frame
    frame = new JFrame(resources.getString("app.title"));
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.setIconImage(Images.imgGedcom.getImage());

    // Create the desktop
    center = new ControlCenter(frame,registry);
    frame.getContentPane().add(center);

    // Menu-Bar
    frame.setJMenuBar(center.getMenu());

    // Handler
    frame.addWindowListener(new AppWindowListener());

    // Show it

    Rectangle saved = registry.get("frame",(Rectangle)null);
    if (saved!=null) {
      frame.setBounds(saved);
    } else {
      frame.pack();
    }
    frame.show();

    // Done
  }

  /**
   * Main of app
   */
  public static void main(java.lang.String[] args) {

    // Startup and catch Swing missing
    try {
      new App();
    } catch (NoClassDefFoundError err) {
      System.out.println("Error: Swing is missing - please install v1.1 and put swing.jar in your CLASSPATH!");
      err.printStackTrace();
      System.exit(1);
    }
  }

}
