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

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.util.*;
import java.awt.event.*;

import genj.gedcom.*;
import genj.util.*;
import awtx.*;

/**
 * A View is a Dialog into the Gedcom information
 */
class View extends JFrame {

  /** static keeping track of things */
  static private Hashtable views = new Hashtable();
  static private View lastActive = null;

  /** the views we have */
  static private final Object modes[][] = {
    { "genj.edit.EditView"        ,"edit"    ,Images.imgNewEdit     },
    { "genj.table.TableView"      ,"table"   ,Images.imgNewTable    },
    { "genj.tree.TreeView"        ,"tree"    ,Images.imgNewTree     },
    { "genj.timeline.TimelineView","timeline",Images.imgNewTimeline },
    { "genj.report.ReportView"    ,"report"  ,Images.imgNewReport   }
  };

  /** the known views' ids */
  static final int
    EDIT     = 0,
    TABLE    = 1,
    TREE     = 2,
    TIMELINE = 3,
    REPORT   = 4;

  /** members */
  private Registry registry;
  private Component component;

  /**
   * Constructor for given args
   * @param classname fully specified classname of view with
   *        constructor View(Gedcom,Registry,frame)
   * @param name logical name
   * @param image represented image
   * @param gedcom Gedcom to be viewed
   */
  private View(String classname, String name, ImgIcon image, Gedcom gedcom) {

    // Creat a view on registry and remember ControlCenter
    registry = getRegistryFor(gedcom,name);

    // Preset the position of this view - this is in case the view component
    // needs a decent position of this view's frame (e.g. Tree) 11/26/2000
    Point p = registry.get("frame",(Point)null);
    if (p!=null)
      setLocation(p);

    // Resolve class for viewing component
    Class c;
    try {
      c = Class.forName(classname);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Couldn't find viewing class "+classname);
    }

    Class ptypes[] = new Class[]{
      Gedcom.class,
      Registry.class,
      Frame.class
    };

    java.lang.reflect.Constructor constructor;
    try {
      constructor = c.getConstructor(ptypes);
    } catch (Exception e) {
      throw new IllegalArgumentException("Couldn't find constructor for viewing class "+classname);
    }

    // Preset behaviour
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setIconImage(image.getImage());
    setTitle(
      gedcom.getName()+" - "+App.resources.getString("cc.view."+name)+" ("+registry.getViewSuffix()+")"
    );
    enableEvents(FocusEvent.FOCUS_GAINED);

    // Create viewing component
    try {
      Object params[] = new Object[]{
      gedcom,
      registry,
      this
      };

      component = (Component) constructor.newInstance(params);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Couldn't instantiate viewing component of class "+classname);
    }

    lastActive = this;

    // Remember view as being opened e.g. "nils.ged.table"
    views.put(registry.getName()+"."+registry.getView(),this);

    // Layout
    this.getContentPane().add(component);

    // Show in editor
    ViewEditor.edit(component,getTitle());

    // Done
  }

  /**
   * Closes all views
   */
  static void closeAll() {

    Enumeration oviews = views.elements();

    while (oviews.hasMoreElements()) {
      ((View)oviews.nextElement()).dispose();
    }

    lastActive=null;
  }

  /**
   * Closes all views on given Gedcom
   */
  static void closeAll(Gedcom gedcom) {

    lastActive=null;

    Enumeration vs = views.keys();

    while (vs.hasMoreElements()) {

      String key = (String)vs.nextElement();

      if (key.startsWith(gedcom.getName()))
      ((View)views.get(key)).dispose();
    }
  }

  /**
   * Closing
   */
  public void dispose() {

    // Remember box
    registry.put("frame",getBounds());

    // View isn't open anymore
    views.remove(registry.getName()+"."+registry.getView());

    if (lastActive==this) {
      lastActive=null;
    }

    // .. in editor, too
    ViewEditor.dontEdit(component);

    // Delegate
    super.dispose();
  }

  /**
   * Returns the component inside this View
   */
  public Component getContent() {
    return component;
  }

  /**
   * Returns the last active View
   */
  public static View getLastActive() {
    return lastActive;
  }

  /**
   * Helper that returns the next logical registry-view
   * for given gedcom and name of view
   */
  private Registry getRegistryFor(Gedcom gedcom, String nameOfView) {

    // Check which iteration number is available next
    String name = gedcom.getOrigin().getFileName();
    int number;
    for (number=1;;number++) {
      if (!views.containsKey(name+"."+nameOfView+"."+number)) {
        break;
      }
    }

    // Return calculated view
    return new Registry(Registry.getRegistry(name), nameOfView+"."+number);
  }

  /**
   * Opens a view on a gedcom file
   */
  static View open(int mode, Gedcom gedcom) {

    try {
      // Create the viewing component
      View view = new View(
        (String) modes[mode][0],
        (String) modes[mode][1],
        (ImgIcon)modes[mode][2],
        gedcom
      );

      // Done
      return view;

    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }

    return null;
    // Done
  }

  /**
   * Layout
   */
  public void pack() {

    // Try to find out box
    Rectangle box = registry.get("frame",(Rectangle)null);
    if (box!=null) {
      setBounds(new AreaInScreen(box));
    } else {
      setSize(new Dimension(480,320));
    }
  }

  /**
   * Capture focus events
   */
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);

    if  (e.getID()==WindowEvent.WINDOW_ACTIVATED) {
      ViewEditor.edit(component,getTitle());
      lastActive = this;
    }
  }
}
