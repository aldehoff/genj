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
import genj.print.PrintProperties;
import genj.print.PrintRenderer;
import genj.print.Printer;
import genj.util.*;
import genj.util.swing.ButtonHelper;
import awtx.*;

/**
 * A broker for Views into Gedcom information
 */
class ViewBridge {
  
  /** instance */
  private static ViewBridge instance;

  /** the views we have */
  static private final Object modes[][] = {
    { "genj.edit.EditView"        ,"edit"     ,Images.imgNewEdit     , new Dimension(256,480) },
    { "genj.table.TableView"      ,"table"    ,Images.imgNewTable    , new Dimension(480,320) },
    { "genj.tree.TreeView"        ,"tree"     ,Images.imgNewTree     , new Dimension(480,480) },
    { "genj.timeline.TimelineView","timeline" ,Images.imgNewTimeline , new Dimension(480,256) },
    { "genj.report.ReportView"    ,"report"   ,Images.imgNewReport   , new Dimension(480,320) },
    { "genj.nav.NavigatorView"    ,"navigator",Images.imgNewNavigator, new Dimension(140,200) }
  };

  /** the known views' ids */
  static final int
    EDIT     = 0,
    TABLE    = 1,
    TREE     = 2,
    TIMELINE = 3,
    REPORT   = 4,
    NAVIGATOR= 5;
    
  /**
   * Singleton access
   */
  public static ViewBridge getInstance() {
    if (instance==null) instance = new ViewBridge();
    return instance;
  }

  /**
   * Helper that instantiates a View
   * @param classname fully specified classname of view with
   *        constructor View(Gedcom,Registry,frame)
   * @param name logical name
   * @param image represented image
   * @param gedcom Gedcom to be viewed
   */
  private Component createView(String classname, Gedcom gedcom, Registry registry, Frame frame) {
    
    // Resolve class for viewing component
    Class c;
    try {
      c = Class.forName(classname);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Couldn't find viewing class "+classname);
    }

    java.lang.reflect.Constructor constructor;
    try {
      constructor = c.getConstructor(new Class[]{ Gedcom.class, Registry.class, Frame.class});
    } catch (Exception e) {
      throw new IllegalArgumentException("Couldn't find constructor for viewing class "+classname);
    }

    // Create viewing component
    Component view;
    try {
      view = (Component) constructor.newInstance(new Object[]{gedcom,registry,frame});
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Couldn't instantiate viewing component of class "+classname);
    }
    
    // Add special view components to content
    if (view instanceof awtx.Scrollpane) {

      awtx.Scrollpane scroll = (awtx.Scrollpane)view;
      ButtonHelper bh = new ButtonHelper().setResources(App.resources).setInsets(0);

      // A button for editing the View's settings
      if (ViewEditor.getViewInfo(view)!=null) {
        final Component _view = view;
        final Frame _frame = frame;
        bh.setListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ViewEditor.startEditing(_view,_frame.getTitle());
          }
        });
        scroll.add2Edge(bh.setImage(Images.imgSettings).setAction("VIEWEDIT").setTip("cc.tip.settings").create());
        _frame.addWindowListener(new WindowAdapter() {
          public void windowClosed(WindowEvent e) {
            ViewEditor.stopEditing(_view);
          }
        });
      }

      // And a print button in case a PrintRenderer is existing
      try {
        final PrintRenderer _renderer = (PrintRenderer)Class.forName(view.getClass().getName()+"PrintRenderer").newInstance();
        _renderer.setView(view);
        final Frame _frame = frame;
        bh.setListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Printer.print(_frame, _renderer, new PrintProperties(_frame.getTitle()));
          }
        });
        scroll.add2Edge(bh.setImage(Images.imgPrint).setAction("PRINT").setTip("cc.tip.print").create());
      } catch (Throwable t) {
        // won't support printing
      }
    }

    // Done
    return view;
  }

  /**
   * Closes all views on given Gedcom
   */
  public void closeAll(Gedcom gedcom) {
    
    // the view looking on that gedcom
    Vector views = new Vector();
    
    // search through frames
    Hashtable frames = App.getInstance().getFrames();
    Enumeration vs = frames.keys();
    while (vs.hasMoreElements()) {
      String key = (String)vs.nextElement();
      if (key.startsWith(gedcom.getName())) views.add(frames.get(key));
    }

    // close those views
    vs = views.elements();
    while (vs.hasMoreElements()) {
      ((JFrame)vs.nextElement()).dispose();
    }    
    
    // done
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
      if (App.getInstance().getFrame(name+"."+nameOfView+"."+number)==null) {
        break;
      }
    }

    // Return calculated view
    return new Registry(Registry.getRegistry(name), nameOfView+"."+number);
  }
  
  /**
   * Opens a view on a gedcom file
   */
  public JFrame open(int mode, Gedcom gedcom) {
    
    try {
      
      // get the meta-information      
      String  classname = (String)modes[mode][0];
      String  name = (String)modes[mode][1];
      ImgIcon image = (ImgIcon)modes[mode][2];
      Dimension dimension = (Dimension)modes[mode][3];
      
      // create a registry for that view
      Registry registry = getRegistryFor(gedcom, name);

      // Create an enclosing Frame
      JFrame frame = App.getInstance().createFrame(
        gedcom.getName()+" - "+App.resources.getString("cc.view."+name)+" ("+registry.getViewSuffix()+")",
        image,
        registry.getName() + "." + registry.getView(),
        dimension
      );
      
      // Create the viewing component
      Component view = createView(classname, gedcom, registry, frame);
      frame.getContentPane().add(view);
      
      // Done
      return frame;

    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }

    return null;
    // Done
  }
}
