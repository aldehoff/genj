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

/**
 * A broker for Views into Gedcom information
 */
public class ViewBridge {
  
  /** instance */
  private static ViewBridge instance;
  
  /** descriptors of views */
  static final private Descriptor[] descriptors = new Descriptor[]{
    new Descriptor("genj.table.TableView"      ,"table"    ,Images.imgNewTable    , new Dimension(480,320)),
    new Descriptor("genj.tree.TreeView"        ,"tree"     ,Images.imgNewTree     , new Dimension(480,480)),
    new Descriptor("genj.timeline.TimelineView","timeline" ,Images.imgNewTimeline , new Dimension(480,256)),
    new Descriptor("genj.edit.EditView"        ,"edit"     ,Images.imgNewEdit     , new Dimension(256,480)),
    new Descriptor("genj.report.ReportView"    ,"report"   ,Images.imgNewReport   , new Dimension(480,320)),
    new Descriptor("genj.nav.NavigatorView"    ,"navigator",Images.imgNewNavigator, new Dimension(140,200))
  };

  /**
   * Singleton access
   */
  public static ViewBridge getInstance() {
    if (instance==null) instance = new ViewBridge();
    return instance;
  }
  
  /**
   * Returns all known descriptors
   */
  public Descriptor[] getDescriptors() {
    return descriptors;
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
        scroll.add2Edge(bh.create(new ActionStartEdit(view,frame.getTitle())));
        frame.addWindowListener(new ActionDelegate.WindowClosedRouter(new ActionStopEdit(view)));
      }

      // And a print button in case a PrintRenderer is existing
      try {
        PrintRenderer renderer = (PrintRenderer)Class.forName(view.getClass().getName()+"PrintRenderer").newInstance();
        renderer.setView(view);
        scroll.add2Edge(bh.create(new ActionPrint(frame,renderer)));
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
  public JFrame open(Descriptor descriptor, Gedcom gedcom) {
    
    try {
      
      // create a registry for that view
      Registry registry = getRegistryFor(gedcom, descriptor.key);

      // Create an enclosing Frame
      JFrame frame = App.getInstance().createFrame(
        gedcom.getName()+" - "+App.resources.getString("cc.view."+descriptor.key)+" ("+registry.getViewSuffix()+")",
        descriptor.img,
        registry.getName() + "." + registry.getView(),
        descriptor.dim
      );
      
      // Create the viewing component
      Component view = createView(descriptor.type, gedcom, registry, frame);
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
  
  /**
   * A descriptor of a View
   */  
  public static class Descriptor {
    public String type;
    public String key;
    public ImgIcon img;
    public Dimension dim;
    /*package*/ Descriptor(String t, String k, ImgIcon i, Dimension d) {
      type=t;key=k;img=i;dim=d;
    }
  } //Descriptor
  

  /**
   * Action - View Edit
   */
  private class ActionStartEdit extends ActionDelegate {
    /** a view */
    private Component view;
    /** a title */
    private String title;
    /** constructor */
    protected ActionStartEdit(Component v, String t) {
      super.setImage(Images.imgSettings).setTip("cc.tip.settings");
      view=v;
      title=t;
    }
    /** run */
    protected void run() {
      ViewEditor.startEditing(view,title);
    }
  } //ActionViewEdit

  /**
   * Action - Stop Edit
   */
  private class ActionStopEdit extends ActionDelegate {
    /** a view */
    private Component view;
    /** constructor */
    protected ActionStopEdit(Component v) {
      view=v;
    }
    /** run */
    protected void run() {
      ViewEditor.stopEditing(view);
    }
  } //ActionViewEdit
  
  /**
   * Action - Print
   */
  private class ActionPrint extends ActionDelegate {
    /** a frame */
    private Frame frame;
    /** a renderer */
    private PrintRenderer renderer;
    /** constructor */
    protected ActionPrint(Frame f, PrintRenderer r) {
      super.setImage(Images.imgPrint).setTip("cc.tip.print");
      frame=f;
      renderer=r;
    }
    /** run */
    protected void run() {
      Printer.print(frame, renderer, new PrintProperties(frame.getTitle()));
    }
  }
}
