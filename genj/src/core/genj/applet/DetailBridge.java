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
package genj.applet;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

import java.net.*;

import genj.gedcom.*;

/**
 * GenJ Applet bridge to browser
 */
public class DetailBridge implements ActionListener {

  /** the applet's context */
  private AppletContext ctx;

  /** the url we came from */
  private String url;

  /** the gedcom we're looking at */
  private Gedcom gedcom;

  /**
   * Constructor
   */
  public DetailBridge(Applet applet, String pUrl, Gedcom pGedcom) {

    // Remember the context
    ctx = applet.getAppletContext();

    // .. and the gedcom
    gedcom = pGedcom;

    // Check the url
    url = pUrl;

    if (!url.endsWith("/")) {
      url = url + "/";
    }
    if (url.indexOf(':')<0) {
      String base = applet.getDocumentBase().toString();
      int p = base.lastIndexOf('/');
      url = base.substring(0,p+1)+url;
    }

    // Done
  }

  /**
   * Notification about action performed - triggers detail
   */
  public void actionPerformed(ActionEvent e) {

    // What's the entity we're looking at?
    Entity last = gedcom.getLastEntity();
    if (last==null) {
      return;
    }

    String id = last.getId();
    String doc = url+id+".html";

    try {
      ctx.showDocument(new URL(doc),"_detail");
      System.out.println("Showing detail document "+doc);
    } catch (Exception ex) {
      System.out.println("Couldn't show detail document "+doc+"["+ex.getMessage()+"]");
    }

  }
}
