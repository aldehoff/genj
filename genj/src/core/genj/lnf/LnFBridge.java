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
package genj.lnf;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import genj.util.Registry;

/**
 * A Bridge to Look&Feels
 */
public class LnFBridge {
  
  /** singleton instance */
  static private LnFBridge instance;
  
  /** constants */
  static private final String 
    LNF_PROPERTIES = "lnf.properties",
    LNF_DIR        = "./lnf";

  /** Look&Feels */
  private LnF[] lnfs = new LnF[0];
  
  /**
   * Accessor - singleton
   */
  public static LnFBridge getInstance() {
    if (instance==null) instance=new LnFBridge();
    return instance;
  }
  
  /** 
   * Constructor
   */
  private LnFBridge() {
    readDescriptor();
  }
  
  /**
   * Sets a certain LnF
   */
  public void setLnF(LnF lnf, LnF.Theme theme, final Vector rootComponents) {
    
    // try to load LnF
    String type = lnf.getType();
    String archive = lnf.getArchive();
    String prefix = "[Debug]Look and feel #"+lnf+" of type "+type;
    
    // Apply theme(?)
    if (lnf.getThemeKey()!=null) {
      if (theme==null) theme=lnf.getThemes()[0];
      try {
        System.setProperty(lnf.getThemeKey(), getLnFDir() + theme.getPack());
      } catch (ArrayIndexOutOfBoundsException aioobe) {
      }
    }

    // Load and apply L&F
    try {
      
      ClassLoader cl = getClass().getClassLoader();
      if (archive!=null) {
        URL urlArchive = new URL("file", "", new File(getLnFDir(), archive).getAbsolutePath());
        cl = new URLClassLoader(new URL[]{urlArchive},cl);
        UIManager.getLookAndFeelDefaults().put("ClassLoader",cl);
        UIManager.getDefaults().put("ClassLoader",cl);
      } 
      UIManager.setLookAndFeel((LookAndFeel)cl.loadClass(type).newInstance());
    } catch (ClassNotFoundException cnfe) {
      System.out.println(prefix+" is not accessible (ClassNotFoundException)");
    } catch (ClassCastException cce) {
      System.out.println(prefix+" is not a valid LookAndFeel (ClassCastException)");
    } catch (MalformedURLException mue) {
      System.out.println(prefix+" doesn't point to a valid archive (MalformedURLException)");
    } catch (UnsupportedLookAndFeelException e) {
      System.out.println(prefix+" is not supported on this platform (UnsupportedLookAndFeelException)");
    } catch (Throwable t) {
      System.out.println(prefix+" couldn't be set ("+t.getClass()+")");
      return;
    }

    // reflect it    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Enumeration e = rootComponents.elements();
        while (e.hasMoreElements()) SwingUtilities.updateComponentTreeUI((Component)e.nextElement());
      }
    });
    
    // done
  }
  
  /**
   * LnFs
   */
  public LnF[] getLnFs() {
    return lnfs;
  }
  
  /**
   * Directory of LnF
   */
  private String getLnFDir() {
    String dir = System.getProperty("genj.lnf.dir");
    if (dir==null) dir = LNF_DIR;
    return dir;
  }
  
  /** 
   * Read lnf descriptor
   */
  private void readDescriptor() {
    
    try {
      
      // get the number of configured lnfs
      Registry r = new Registry(new FileInputStream(new File(getLnFDir(), LNF_PROPERTIES)));
      int num = r.get("lnf.count",-1);
      if (num<0) return;
      
      // create a LnF for each
      LnF[] tmp = new LnF[num];
      for (int d=0; d<tmp.length; d++) {
        tmp[d]=new LnF(new Registry(r, "lnf."+(d+1)));
      }   

      // keep      
      lnfs=tmp;   
      
    } catch (IOException ioe) {
    }
  }

  /**
   * A LnF
   */
  public class LnF {
    
    /** members */
    private String name,type,archive,themekey;
    private Theme[] themes;
    
    /**
     * Constructor
     */
    protected LnF(Registry registry) {
      // members
      name = registry.get("name","?");
      type = registry.get("type","?");
      archive = registry.get("jar",(String)null);
      themekey = registry.get("themes.key", (String)null);
      
      // themes
      String[] ts = registry.get("themes",new String[0]);
      themes = new Theme[ts.length];
      for (int t=0; t<ts.length; t++) {
        themes[t] = new Theme(ts[t]);
      }
      
      // done
    }
    
    /**
     * String
     */
    public String toString() {
      return getName();
    }
    
    /**
     * Themes
     */
    public Theme[] getThemes() {
      return themes;
    }
    
    /**
     * ThemeKey
     */
    public String getThemeKey() {
      return themekey;
    }
    
    /**
     * Name
     */
    public String getName() {
      return name;
    }
    
    /**
     * Type
     */
    public String getType() {
      return type;
    }
    
    /**
     * Archive
     */
    public String getArchive() {
      return archive;
    }
    
    
    /**
     * A Theme
     */
    public class Theme {
      
      /** the archive of the theme */
      private String pack;
      
      /** 
       * Constructor
       */
      protected Theme(String setPack) {
        pack=setPack;
      }
      
      /**
       * String
       */
      public String toString() {
        return archive;
      }
      
      /**
       * Pack
       */
      public String getPack() {
        return pack;
      }
      
    } // Theme
    
  } // LnF
  
}
