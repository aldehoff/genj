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

import genj.util.Debug;
import genj.util.EnvironmentChecker;
import genj.util.Resources;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.io.File;
import java.net.URL;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A bridge to javax Help System
 */
class HelpWidget extends JPanel {

  /**
   * Calculate help-directory location 'help'
   */
  private String calcHelpBase() {
    
    // First we look in "genj.help.dir"
    String dir = EnvironmentChecker.getProperty(
      this,
      new String[]{ "genj.help.dir", "user.dir/help"},
      ".",
      "read help"
    );
    
    // Then we check for local language
    String local = dir+"/"+System.getProperty("user.language");
    if (new File(local).exists()) {
      return local;
    }
    
    // ... otherwise fallback to 'en' language
    return dir+"/en";
    
  }

  /**
   * Constructor
   */
  public HelpWidget() {
    
    // simple layout
    super(new BorderLayout());
    
    // create center component
    JComponent pCenter = getContent();
    if (pCenter==null) {
      pCenter = new JLabel(
        Resources.get(this).getString("cc.help.help_file_missing"),
        WindowManager.IMG_ERROR,
        SwingConstants.CENTER
      );
    }
    
    // layout
    add(pCenter, BorderLayout.CENTER);    
    
    // done
  }
  
  /**
   * Initialization of help
   */
  private JComponent getContent() {
    
    // Open the Help Set        
    String file = calcHelpBase() + "/helpset.xml";
    Debug.log(Debug.INFO, this,"Trying to use help in " + file );
    // safety check
    if (!new File(file).exists()) {
      Debug.log(Debug.WARNING, this,"No help found");
      return null;
    }

    // Load and init through bridge
    try {
      Bridge bridge = (Bridge)Class.forName(Bridge.class.getName()+"Impl").newInstance();
      return bridge.init(new URL("file","", file));
    } catch (Exception e) {
      Debug.log(Debug.WARNING, this,"Problem reading help",e);
      return null;
    }
    
    // done    
  }

  /**
   * A bridge to the JavaHelp classes that might not be
   * there during runtime  
   */
  /*package*/ interface Bridge {
    public JComponent init(URL url) throws Exception;
  }
  
  /**
   * Impl
   */
  /*package*/ static class BridgeImpl implements Bridge {
    public JComponent init(URL url) throws Exception {
      return new JHelp(new HelpSet(null,url));
    }
  }
  
}
