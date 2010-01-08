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

import genj.lnf.LnF;
import genj.option.Option;
import genj.option.OptionProvider;
import genj.option.OptionUI;
import genj.option.OptionsWidget;
import genj.option.PropertyOption;
import genj.util.EnvironmentChecker;
import genj.util.Resources;
import genj.util.swing.Action2;

import java.awt.Desktop;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * Application options
 */
public class Options extends OptionProvider {

  /** constants */
  private final static String SWING_RESOURCES_KEY_PREFIX = "swing.";

  /** singleton */
  private final static Options instance = new Options();

  /** resources */
  private Resources resources;

  /** maximum log file size */
  private int maxLogSizeKB = 128;

  /** the current looknfeel */
  private int lookAndFeel = -1;

  /** the current language code */
  private int language = -1;

  /** restore views during startup */
  public boolean isRestoreViews = true;
  
  /** write BOM on save */
  public boolean isWriteBOM = false;

  /** all available language codes */
  private static String[] languages;

  /** all available language codes */
  private final static String[] codes = findCodes();

  private static String[] findCodes() {

    // Check available language libraries
    // prepare result with default "en"
    TreeSet result = new TreeSet();
    result.add("en");

    // look for development mode -Dgenj.language.dir or in  ./language/xy (except 'CVS')
    File[] dirs = new File(EnvironmentChecker.getProperty(Options.class, "genj.language.dir", "./language", "Dev-time language directory switch")).listFiles();
    if (dirs!=null) {
      for (int i = 0; i < dirs.length; i++) {
        String dir = dirs[i].getName();
        if (!"CVS".equals(dir))
          result.add(dir);
      }
    }

    // look for language libraries (./lib/genj_pt_BR.jar)
    File[] libs = new File("./lib").listFiles();
    if (libs!=null)
      for (int l=0;l<libs.length;l++) {
        File lib = libs[l];
        if (!lib.isFile()) continue;
        String name = lib.getName();
        if (!name.startsWith("genj_")) continue;
        if (!name.endsWith  (".jar" )) continue;
        result.add(name.substring(5, name.length()-4));
      }

    // done
    return (String[])result.toArray(new String[result.size()]);
  }

  /**
   * Instance access
   */
  public static Options getInstance() {
    return instance;
  }

  /**
   * Getter - looknfeel
   */
  public int getLookAndFeel() {
    // this is invoked once on option introspection
    if (lookAndFeel<0)
      setLookAndFeel(0);
    return lookAndFeel;
  }

  /**
   * Setter - looknfeel
   */
  public void setLookAndFeel(int set) {

    // Check against available LnFs
    LnF[] lnfs = LnF.getLnFs();
    if (set<0||set>lnfs.length-1)
      set = 0;

    // set it - 20091134 making this a restart only change
    lnfs[set].apply(null);

    // remember for restart
    lookAndFeel = set;

    // done
  }

  /**
   * Getter - looknfeels
   */
  public LnF[] getLookAndFeels() {
    return LnF.getLnFs();
  }

  /**
   * Setter - language
   */
  public void setLanguage(int language) {

    // set locale if applicable
    if (language>=0&&language<codes.length) {
      String lang = codes[language];
      if (lang.length()>0) {
        App.LOG.info("Switching language to "+lang);
        String country = Locale.getDefault().getCountry();
        int i = lang.indexOf('_');
        if (i>0) {
          country = lang.substring(i+1);
          lang = lang.substring(0, i);
        }
        try {
          Locale.setDefault(new Locale(lang,country));
        } catch (Throwable t) {}
      }
    }

    // remember
    this.language = language;

    // set swing resource strings (ok, cancel, etc.)
    Resources resources = Resources.get(this);
    Iterator keys = resources.getKeys().iterator();
    while (keys.hasNext()) {
      String key = (String)keys.next();
      if (key.indexOf(SWING_RESOURCES_KEY_PREFIX)==0) {
        UIManager.put(
          key.substring(SWING_RESOURCES_KEY_PREFIX.length()),
          resources.getString(key)
        );
      }
    }

    // done
  }

  /**
   * Getter - language
   */
  public int getLanguage() {
    return language;
  }

  /**
   * Getter - languages
   */
  public String[] getLanguages() {
    // not known yet?
    if (languages==null) {

      Resources resources = getResources();

      // init 'em
      languages = new String[codes.length];
      for (int i=0;i<languages.length;i++) {
        String language = resources.getString("option.language."+codes[i], false);
        languages[i] = language!=null ? language : codes[i];
      }
    }
    // done
    return languages;
  }

  /**
   * Getter - maximum log size
   */
  public int getMaxLogSizeKB() {
    return maxLogSizeKB;
  }

  /**
   * Setter - maximum log size
   */
  public void setMaxLogSizeKB(int set) {
    maxLogSizeKB = Math.max(128, set);
  }

  /**
   * Getter - http proxy
   */
  public String getHttpProxy() {
    String host = System.getProperty("http.proxyHost");
    String port = System.getProperty("http.proxyPort");
    if (host==null)
      return "";
    return port!=null&&port.length()>0 ? host+":"+port : host;
  }

  /**
   * Setter - http proxy
   */
  public void setHttpProxy(String set) {
    int colon = set.indexOf(":");
    String port = colon>=0 ? set.substring(colon+1) : "";
    String host = colon>=0 ? set.substring(0,colon) : set;
    if (host.length()==0) port = "";
    System.setProperty("http.proxyHost", host);
    System.setProperty("http.proxyPort", port);
  }

  /**
   * Lazy resources
   */
  private Resources getResources() {
    if (resources==null)
      resources = Resources.get(this);
    return resources;
  }

  /**
   * Provider callback
   */
  public List getOptions() {
    // bean property options of instance
    List result = PropertyOption.introspect(instance);
    // add an otion for user.home.dir
    result.add(new UserHomeGenJOption());
    // done
    return result;
  }

  private static class UserHomeGenJOption extends Option implements OptionUI {

    public String getName() {
      return getInstance().getResources().getString("option.userhomegenj.name");
    }

    public String getToolTip() {
      return getInstance().getResources().getString("option.userhomegenj.name.tip", false);
    }

    public OptionUI getUI(OptionsWidget widget) {
      return this;
    }

    public void persist() {
      // we're not storing anything
    }

    public void restore() {
      // no state to restore
    }

    public void endRepresentation() {
    }

    public JComponent getComponentRepresentation() {
      JButton button = new JButton(new Open());
      // small guy
      button.setMargin(new Insets(2,2,2,2));
      // done
      return button;
    }

    public String getTextRepresentation() {
      // none
      return null;
    }

    /**
     * Action for UI
     */
    private class Open extends Action2 {
      Open() {
        setText("...");
      }
      public void actionPerformed(ActionEvent event) {
        File user_home_genj = new File(EnvironmentChecker.getProperty(UserHomeGenJOption.this, "user.home.genj", null, "trying to open user.home.genj")) ;
        try {
          Desktop.getDesktop().open(user_home_genj);
        } catch (IOException e) {
          Logger.getLogger("genj.io").log(Level.INFO, "can't open user.home.genj", e);
        }
      }
    }
  }

} //Options
