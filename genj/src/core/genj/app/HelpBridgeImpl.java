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
   * Calculate help-directory location
   */
  private String calcHelp(boolean useEn) {

    return
      System.getProperty("user.dir")
        + "/help/"
        + (useEn?"en":Locale.getDefault().getLanguage())
        + "/helpset.xml";

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
      file = calcHelp(false);
      if (!new File(file).exists()) {
        System.out.println("Tried to find help in " + file );
        file = calcHelp(true);
        if (!new File(file).exists()) {
          System.out.println("Tried to find help in " + file + " ... sorry :(" );
          return;
        }
      }

      System.out.println("Using help in " + file );
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
