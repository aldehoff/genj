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
import java.security.acl.LastOwnerException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import genj.util.Debug;
import genj.util.EnvironmentChecker;
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
  
  /** last */
  private static LnF lastLnF;
  
  /** dir */
  private static String dir;
  
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
   * Resolves a LnF
   */
  public LnF getLnF(String lnfName) {
    
    // search for lnf
    for (int i=0; i<lnfs.length; i++) {
      if (lnfs[i].getName().equals(lnfName)) {
        return lnfs[i];
      }
    }
    return null;
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
  private static String getLnFDir() {
    if (dir==null) {
      dir = EnvironmentChecker.getProperty(
        LnFBridge.class,
        new String[]{ "genj.lnf.dir"},
        LNF_DIR,
        "read lnf.properties"
      );
    }
    return dir;
  }
  
  /** 
   * last LnF
   */
  public LnF getLastLnF() {
    return lastLnF;
  }
  
  /**
   * A LnF
   */
  public static class LnF {
    
    /** members */
    private String name,type,archive;
    private Theme[] themes;
    private ClassLoader cl;
    private Theme lastTheme;
    private LookAndFeel instance;
    
    /**
     * Constructor
     */
    protected LnF(Registry registry) {
      // members
      name = registry.get("name","?");
      type = registry.get("type","?");
      archive = registry.get("jar",(String)null);
      
      // themes
      String[] ts = registry.get("themes",new String[0]);
      themes = new Theme[ts.length];
      for (int t=0; t<ts.length; t++) {
        themes[t] = new Theme(ts[t]);
      }
      
      // done
    }
    
    /**
     * Type
     */
    private LookAndFeel getInstance() throws Exception {
      
      // create an instance once
      if (instance==null) {
        instance = (LookAndFeel)cl.loadClass(type).newInstance();      
      } 
      
      // Reset Metal's current theme (some L&Fs change it)
      if (instance.getClass()==javax.swing.plaf.metal.MetalLookAndFeel.class) {
        ((javax.swing.plaf.metal.MetalLookAndFeel)instance).setCurrentTheme(
          new javax.swing.plaf.metal.DefaultMetalTheme()
        );
      }
      
      // here it is
      return instance;
    }
    
    /**
     * Classloader
     */
    private ClassLoader getCL() throws MalformedURLException {
      if (cl!=null) return cl;
      if (archive==null) {
        cl = getClass().getClassLoader();
      } else {
        URL urlArchive = new URL("file", "", new File(getLnFDir(), archive).getAbsolutePath());
        cl = new URLClassLoader(new URL[]{urlArchive});
      }
      return cl;
    }
    
    /** 
     * last Theme
     */
    public Theme getLastTheme() {
      if ((lastTheme==null)&&(themes.length>0)) lastTheme=themes[0];
      return lastTheme;
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
     * Resolves a Theme by name
     */
    public Theme getTheme(String theme) {
      
      // search for theme
      for (int i=0; i<themes.length; i++) {
        if (themes[i].getName().equals(theme)) {
          return themes[i];
        }
      }
      return null;
    }
  
    /**
     * Applies the LnF (with given Theme)
     */
    public boolean apply(Theme theme, final Vector rootComponents) {
      
      // try to load LnF
      String prefix = "Look and feel #"+this+" of type "+type;
      
      // Load and apply L&F
      try {
        
        UIManager.getLookAndFeelDefaults().put("ClassLoader",getCL());
        UIManager.getDefaults().put("ClassLoader",getCL());
        
        LookAndFeel lookAndFeel = getInstance();
        if (theme!=null) theme.apply(lookAndFeel);
        UIManager.setLookAndFeel(lookAndFeel);
        
      } catch (ClassNotFoundException cnfe) {
        Debug.log(Debug.WARNING, this,prefix+" is not accessible (ClassNotFoundException)");
        return false;
      } catch (ClassCastException cce) {
        Debug.log(Debug.WARNING, this,prefix+" is not a valid LookAndFeel (ClassCastException)");
        return false;
      } catch (MalformedURLException mue) {
        Debug.log(Debug.WARNING, this,prefix+" doesn't point to a valid archive (MalformedURLException)");
        return false;
      } catch (UnsupportedLookAndFeelException e) {
        Debug.log(Debug.WARNING, this,prefix+" is not supported on this platform (UnsupportedLookAndFeelException)");
        return false;
      } catch (Throwable t) {
        Debug.log(Debug.WARNING, this,prefix+" couldn't be set ("+t.getClass()+")");
        return false;
      }
      
      // reflect it    
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          Enumeration e = rootComponents.elements();
          while (e.hasMoreElements()) SwingUtilities.updateComponentTreeUI((Component)e.nextElement());
        }
      });
      
      // remember
      lastLnF = this;
      lastTheme = theme;
      
      // done
      return true;
    }

  } // LnF
    
  /**
   * A Theme
   */
  public static class Theme {
    
    /** the name of the theme */
    private String name;
    
    /** 
     * Constructor
     */
    protected Theme(String setName) {
      name=setName;
    }
    
    /**
     * String
     */
    public String toString() {
      return getName();
    }
    
    /**
     * Name
     */
    public String getName() {
      return name;
    }
    
    /**
     * Returns the archive
     */
    public String getArchive() {
      return new File(getLnFDir(), getName()).getAbsolutePath();
    }

    /**
     * Applies a theme 
     */
    protected void apply(LookAndFeel lnf) throws Exception {
      
      // This is a hack for www.lfprod.com's SkinLookAndFeel ONLY right now
      UIManager.put(
        "SkinLookAndFeel.Skin", 
        lnf.getClass().getMethod("loadThemePack", new Class[]{String.class}).invoke(lnf,new Object[]{getArchive()})
      );
      
      // Done
    }
  
    
  } // Theme
    
}
