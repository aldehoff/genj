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
import java.net.*;
import java.util.*;
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
   * Whether a directory with that name exists
   */
  private boolean exists(String dir) {
    return new File(dir).exists();
  }

  /**
   * Calculate help-directory location 'help'
   */
  private String calcHelpBase() {
    
    // First we look in "genj.help.dir"
    String dir = System.getProperty("genj.help.dir");
    if ((dir==null)||(!exists(dir))) {
      // .. otherwise we'll use "user.dir"/help
      dir = System.getProperty("user.dir")+"/help";
    }
    
    // Then we check for local language
    String local = dir+"/"+System.getProperty("user.language");
    if (exists(local)) {
      return local;
    }
    
    // ... otherwise en language
    return dir+"/en";
    
  }
  


  /**
   * Opens the help
   */
  public void open(Registry registry) {

    if (broker!=null) {
      broker.setSize(broker.getSize());
      broker.setLocation(broker.getLocation());
      broker.setDisplayed(true);
      return;
    }

    HelpSet hs=null;

    String file;
    try {
      file = calcHelpBase() + "/helpset.xml";
      System.out.println("[Debug]Using help in " + file );
      URL url = new URL("file","", file);
      hs = new HelpSet(null,url);

    } catch (Exception e1) {
    }

    broker = hs.createHelpBroker();
    broker.setSize(registry.get("help",new java.awt.Dimension(640,480)));
    broker.setLocation(registry.get("help",new java.awt.Point(0,0)));
    broker.setDisplayed(true);
  }
}
