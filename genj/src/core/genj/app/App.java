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
import genj.util.ActionDelegate;
import genj.util.AreaInScreen;
import genj.util.Debug;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.ScreenResolutionScale;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

    // lookup resources
    Resources resources = Resources.get(this);
    
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
    
    // Load file associations
    FileAssociation.read(registry);

    // Disclaimer
    String version = Version.getInstance().toString();
    if (!version.equals(registry.get("disclaimer",""))) {

      registry.put("disclaimer", version);

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
    JFrame frame = createFrame(resources.getString("app.title"),Gedcom.getImage(),"main", new Dimension(280,180));

    // Create the desktop
    ControlCenter center = new ControlCenter(frame,registry);
    frame.getContentPane().add(center);

    // Show it
    frame.pack();
    frame.show();

    // Done
  }
  
  /**
   * Shutdown
   */
  public void shutdown() {
    // remember file associations
    FileAssociation.write(registry);
    // tell BlueprintManager
    BlueprintManager.getInstance().shutdown();
    // close all frames we know
    Enumeration e = openFrames.elements();
    while (e.hasMoreElements()) ((JFrame)e.nextElement()).dispose();
    // Store registry 
    Registry.saveToDisk();      
    // Flush Debug
    Debug.flush();
    // exit
    System.exit(0);
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
    } catch (NoClassDefFoundError err) {
      Debug.log(Debug.ERROR, App.class, "Cannot instantiate App", err);
      Debug.flush();
      System.exit(1);
    }
  }
  
  /** 
   * Accessor - DPI
   */  
  public Point getDPI() {
    return registry.get("dpi",ScreenResolutionScale.getSystemDPI()); 
  }
  
  /** 
   * Accessor - DPI
   */  
  public void setDPI(Point dpi) {
    registry.put("dpi",dpi); 
  }
  
  /**
   * Gets the registry
   */
  public Registry getRegistry() {
    return registry;
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
    
    // collect frames we know about
    Vector uis = new Vector();
    Enumeration frames = openFrames.elements();
    while (frames.hasMoreElements()) uis.add(frames.nextElement());
    
    // set it!
    if (lnf.apply(theme, uis)) {
      registry.put("lnf", lnf.getName());
      if (theme!=null) registry.put("lnf.theme", theme.getName());
    }
    
    // remember
  }

  /**
   * Returns a previously opened Frame by key
   */
  public JFrame getFrame(String key) {
    return (JFrame)openFrames.get(FRAME_KEY_PREFIX+key);
  }
  
  /**
   * Creates a Frame which remembers it's position from last time
   */
  public Frame createFrame(String title, ImageIcon image, String key, Dimension dimension) {
    return new App.Frame(title, image, key, dimension);
  }

  /**
   * Creates a Dialog which remembers it's position from last time
   * It's modal by default and contains OK and Cancel buttons
   */
  public Dialog createDialog(String title, String key, Dimension dimension, JComponent owner, JComponent content, String[] choices) {
    // find owner
    JFrame frame = null;
    Component cursor = owner;
    while (true) {
      // .. found it?
      if (cursor instanceof JFrame) {
        frame = (JFrame) cursor;
        break;
      }
      // .. look up!
      cursor = cursor.getParent();
      // .. depleted?
      if (cursor==null) break;
    }
    // create it
    return new App.Dialog(title, key, dimension, frame, content, choices);
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
    protected Frame(String title, ImageIcon image, String key, Dimension dimension) {

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
   * Our own dialog
   */
  public class Dialog extends JDialog {
    
    /** a key for remembering dialog characteristics */
    private String savedKey;
    
    /** dimensions used in pack() */
    private Dimension savedDimension;
    
    /** the choice made to finish the Dialog */
    private int choice = -1;
    
    /**
     * Constructor
     */
    protected Dialog(String title, String key, Dimension dimension, JFrame owner, JComponent content, String[] choices) {
      super(owner);
      
      // 1st remember
      savedKey = FRAME_KEY_PREFIX+key;
      savedDimension = dimension;
      
      // 2nd modify the frame's behavior
      setTitle(title);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      
      // 3rd add the content
      Container c = getContentPane();
      c.setLayout(new BorderLayout());
      c.add(content, BorderLayout.CENTER);
      c.add(createButtons(choices), BorderLayout.SOUTH); 
      
      // 4th defaults
      setModal(true);
      
      // done
    }
    
    /**
     * @see java.awt.Window#dispose()
     */
    public void dispose() {
      registry.put(savedKey,getBounds());
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
    
    /**
     * Packs and shows the dialog
     */
    public int packAndShow() {
      pack();
      show();
      return getChoice();
    }
    
    /**
     * The index of the choice made by the user     */
    public int getChoice() {
      return choice;
    }

    /**
     * Create Dialog buttons     */
    private JPanel createButtons(String[] choices) {
      // none?
      if (choices==null) {
        choices = new String[] { UIManager.getString("OptionPane.okButtonText")};
      }
      // loop
      JPanel result = new JPanel();
      result.setLayout(new FlowLayout(FlowLayout.RIGHT));
      ButtonHelper bh = new ButtonHelper().setContainer(result);
      for (int i=0; i<choices.length; i++) {
        result.add(bh.create(new Choice(choices[i],i)));
      }
      // done
      return result;
    }
    
    /**
     * Action     */
    private class Choice extends ActionDelegate {
      /** the index of this choice */
      private int index;
      /**
       * Constructor       */
      private Choice(String txt, int inDex) {
        super.setText(txt);
        index = inDex;
      }
      /**
       * @see genj.util.ActionDelegate#execute()
       */
      protected void execute() {
        choice = index;
        dispose();
      }
    } //Choice 

  } // Dialog

} //App
