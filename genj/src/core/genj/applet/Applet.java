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

import genj.Version;
import genj.gedcom.Gedcom;
import genj.io.GedcomReader;
import genj.util.Origin;
import genj.util.Registry;
import genj.view.ViewManager;
import genj.window.DefaultWindowManager;

import java.awt.BorderLayout;
import java.net.URL;

/**
 * THE GenJ Applet
 */
public class Applet extends java.applet.Applet {

  /** views we offer */
  static final private String[] FACTORIES = new String[]{
    "genj.table.TableViewFactory",
    "genj.tree.TreeViewFactory",
    "genj.timeline.TimelineViewFactory",
    "genj.edit.EditViewFactory",
    //"genj.report.ReportViewFactory",
    "genj.nav.NavigatorViewFactory",
    "genj.entity.EntityViewFactory", 
    "genj.search.SearchViewFactory" 
  };


  /** whether we're initialized */
  private boolean isInitialized = false;

  /**
   * @see java.applet.Applet#init()
   */
  public void init() {
    
    // work to do?
    if (isInitialized)
      return;
    isInitialized = true;

    // open registry
    Registry registry = new Registry();

    // load gedcom
    Gedcom gedcom;
    try {
      URL url = new URL("file:/d:/nils/priv.java/workspace/GenJ-HEAD/gedcom/example.ged");
      Origin origin = Origin.create(url);
      GedcomReader reader = new GedcomReader(origin);
      gedcom = reader.read();
    } catch (Throwable t) {
      t.printStackTrace();
      return;
    }
    
    // prepare window manager
    ViewManager vmanager = new ViewManager(
      registry, 
      null, 
      new DefaultWindowManager(registry), 
      FACTORIES
    );

    // add center
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new ControlCenter(vmanager, gedcom));
    
    // done
  }
  
  /**
   * @see java.applet.Applet#start()
   */
  public void start() {
  }

  /**
   * @see java.applet.Applet#stop()
   */
  public void stop() {
  }
  
  /**
   * @see java.applet.Applet#getAppletInfo()
   */
  public String getAppletInfo() {
    return "GenealogyJ v"+Version.getInstance();
  }


} //Applet
