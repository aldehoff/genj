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

import genj.util.ActionDelegate;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * A bridge to javax Help System
 */
class HelpWidget extends JPanel {

  /**
   * Calculate help-directory location 'help'
   */
  private String calcHelpBase() {
    
    // First we look in "genj.help.dir"
    String dir = System.getProperty("genj.help.dir");
    if ((dir==null)||(!new File(dir).exists())) {
      // .. otherwise we'll use "user.dir"/help
      dir = System.getProperty("user.dir")+"/help";
    }
    
    // Then we check for local language
    String local = dir+"/"+System.getProperty("user.language");
    if (new File(dir).exists()) {
      return local;
    }
    
    // ... otherwise en language
    return dir+"/en";
    
  }

  /**
   * Constructor
   */
  public HelpWidget(JFrame frame) {
    
    // simple layout
    super(new BorderLayout());
    
    // create center component
    JComponent pCenter = getContent();
    if (pCenter==null) {
      pCenter = new JLabel(
        App.resources.getString("cc.help.help_file_missing"),
        (Icon)UIManager.get( "OptionPane.errorIcon"),
        SwingConstants.CENTER
      );
    }
    
    // create south component
    JPanel pSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton bExit = new JButton(App.resources.getString("view.close"));
    bExit.setActionCommand("dispose");
    bExit.addActionListener(new ActionDelegate(frame));
    pSouth.add(bExit);
    
    // layout
    add(pCenter, BorderLayout.CENTER);    
    add(pSouth , BorderLayout.SOUTH );    
    
    // done
  }

  /**
   * Initialization of help
   */
  private JComponent getContent() {
    
    // Creat a URL to help-set
    URL url;
    try {
      // Open the Help Set        
      String file = calcHelpBase() + "/helpset.xml";
      System.out.println("[Debug]Using help in " + file );
      url = new URL("file","", file);
    } catch (MalformedURLException mue) {
      System.out.println("[Debug]... malformed");
      return null;
    }
      
    // Load an init through bridge
    try {
      Bridge bridge = (Bridge)Class.forName(Bridge.class.getName()+"Impl").newInstance();
      return bridge.init(url);
    } catch (Throwable t) {
      System.out.println("[Debug]... exception "+t.getMessage());
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
