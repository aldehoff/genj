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

import javax.help.*;
import javax.swing.JFrame;

import java.net.*;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.*;

import genj.util.Registry;

/**
 * A bridge to javax Help System
 */
class HelpBridgeImpl implements HelpBridge {

  /** javax help bootstrap */
  private HelpBroker broker;

  /**
   * Notification that the registry is closing
   */
  public void close(Registry registry) {

    if (broker==null)
      return;

    registry.put("help",broker.getSize());
    registry.put("help",broker.getLocation());
  }
  
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
   * Opens the help
   */
  public void open(Registry registry) {

    try {
      
      String file = calcHelpBase() + "/helpset.xml";
      System.out.println("[Debug]Using help in " + file );
      URL url = new URL("file","", file);
      HelpSet hs = new HelpSet(null,url);

      JHelpContentViewer content = new JHelpContentViewer(hs);
      content.getModel().setCurrentID(hs.getHomeID());
      JHelpNavigator nav = (JHelpNavigator) hs.getNavigatorView("TOC").createNavigator(content.getModel());

      JFrame frame = App.getInstance().createFrame("foo",null,"help",new Dimension(320,256));
      Container c = frame.getContentPane();
      c.setLayout(new BorderLayout());
      c.add(content, BorderLayout.CENTER);
      c.add(nav    , BorderLayout.WEST);
      
      frame.pack();
      frame.show();

    } catch (Throwable t) {
    }
    
  }
}
