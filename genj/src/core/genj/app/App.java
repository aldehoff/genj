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
import genj.lnf.LnFBridge;
import genj.util.*;
import genj.util.AreaInScreen;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.util.Resources;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
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
  private Hashtable openFrames = new Hashtable();
  private static App instance;
  /*package*/ final static Resources resources = new Resources("genj.app");

  /**
   * Application Constructor
   */
  private App() {

    // Startup Information
    Debug.log(Debug.INFO, App.class, "GenJ App - Version "+Version.getInstance()+" - "+new Date());
    String log = EnvironmentChecker.getProperty(
      this, new String[]{ "genj.debug.file" }, null, "choose log-file"
    );
    if (log!=null) Debug.setFile(new File(log));
    EnvironmentChecker.log();
    
    // init our data
    registry = new Registry("genj");
    
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
    JFrame frame = createFrame(resources.getString("app.title"),Images.imgGedcom,"main", new Dimension(256,180));

    // Create the desktop
    ControlCenter center = new ControlCenter(frame,registry);
    frame.getContentPane().add(center);

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
    // Flush Debug
    Debug.flush();
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
      Debug.log(Debug.ERROR, App.class, "Cannot instantiate App", err);
      Debug.flush();
      System.exit(1);
    }
  }

  /**
   * Returns a previously opened Frame by key
   */
  public JFrame getFrame(String key) {
    return (JFrame)openFrames.get(FRAME_KEY_PREFIX+key);
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
    return new App.Frame(title,image,key,dimension);
  }

  /**
   * Our own frame
   */
  public class Frame extends JFrame {
    
    private String savedKey;
    private Dimension savedDimension;
    
    /**
     * Constructor
     */
    protected Frame(String title, ImgIcon image, String key, Dimension dimension) {

      // 1st remember
      savedKey = FRAME_KEY_PREFIX+key;
      savedDimension = dimension;
      
      // 2nd modify the frame's behavior
      setTitle(title);
      if (image!=null) setIconImage(image.getImage());
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      
      // 3rd remember
      openFrames.put(savedKey,this);

      // done
    }
    
    /**
     * @see java.awt.Window#dispose()
     */
    public void dispose() {
      registry.put(savedKey,getBounds());
      openFrames.remove(savedKey);
      super.dispose();
    }

    /**
     * @see java.awt.Window#pack()
     */    
    public void pack() {
      
      Rectangle box = registry.get(savedKey,(Rectangle)null);
      if ((box==null)&&(savedDimension!=null)) 
        box = new Rectangle(0,0,savedDimension.width,savedDimension.height);
      if (box==null) {
        super.pack();
      } else {
        setBounds(new AreaInScreen(box));
      }
      invalidate();
      validate();
      doLayout();
    }

  } // Frame

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
