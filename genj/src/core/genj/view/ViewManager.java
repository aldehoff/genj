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
package genj.view;

import genj.app.App;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.print.PrintProperties;
import genj.print.PrintRenderer;
import genj.print.Printer;
import genj.util.ActionDelegate;
import genj.util.ImgIcon;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImgIconConverter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

/**
 * A bridge to open/manage Views
 */
public class ViewManager {

  /** instance */
  private static ViewManager instance;

  /** resources */
  public final static Resources resources = new Resources("genj.view");

  /** descriptors of views */
  static final private Descriptor[] descriptors = new Descriptor[]{
    new Descriptor("genj.table.TableViewFactory"      ,"table"    ,Images.imgTable    , new Dimension(480,320)),
    new Descriptor("genj.tree.TreeViewFactory"        ,"tree"     ,Images.imgTree     , new Dimension(480,480)),
    new Descriptor("genj.timeline.TimelineViewFactory","timeline" ,Images.imgTimeline , new Dimension(480,256)),
    new Descriptor("genj.edit.EditViewFactory"        ,"edit"     ,Images.imgEdit     , new Dimension(256,480)),
    new Descriptor("genj.report.ReportViewFactory"    ,"report"   ,Images.imgReport   , new Dimension(480,320)),
    new Descriptor("genj.nav.NavigatorViewFactory"    ,"navigator",Images.imgNavigator, new Dimension(140,200)),
    new Descriptor("genj.entity.EntityViewFactory"    ,"entity"   ,Images.imgEntity   , new Dimension(320,320))
  };
  
  /** open views */
  private List viewWidgets = new LinkedList();
  
  /** the currently selected entity */
  private Map gedcom2current = new HashMap();
  
  /**
   * Singleton access
   */
  public static ViewManager getInstance() {
    if (instance==null) instance = new ViewManager();
    return instance;
  }

  /**
   * Returns all known descriptors
   */
  public Descriptor[] getDescriptors() {
    return descriptors;
  }
  
  /**
   * Returns the currently selected entity
   * @return the entity (might be null)
   */
  public Entity getCurrentEntity(Gedcom gedcom) {
    Entity result = (Entity)gedcom2current.get(gedcom);
    if (result!=null) {
      if (!gedcom.getEntities(result.getType()).contains(result)) {
        gedcom2current.remove(gedcom);
        result = null;
      }
    }
    return result;
  }

  /**
   * Sets the current entity
   */
  public void setCurrentEntity(Entity entity) {
    // already?
    Gedcom gedcom = entity.getGedcom();
    if (gedcom2current.get(gedcom)==entity) return;
    // remember
    gedcom2current.put(gedcom, entity);
    // loop and tell to views
    Iterator it = viewWidgets.iterator();
    while (it.hasNext()) {
      ViewWidget vw = (ViewWidget)it.next();
      // only if view on same gedcom
      if (vw.getGedcom()!= gedcom) continue;
      // tell it
      vw.setCurrentEntity(entity);
    }
    // done
  }

  /**
   * Sets the current property
   */
  public void setCurrentProperty(Property property) {
    setCurrentEntity(property.getEntity());
      
   /**
    // already?
    if (currentProperty==property) return;
    // remember
    currentProperty = property;
    Gedcom gedcom = property.getGedcom();
    // loop and tell to views
    Iterator it = viewWidgets.iterator();
    while (it.hasNext()) {
      ViewWidget vw = (ViewWidget)it.next();
      // only if view on same gedcom
      if (vw.getGedcom()!= gedcom) continue;
      // tell it
      vw.setCurrentProperty(currentProperty);
    }
    */
    // done
  }

  /**
   * Helper that returns the next logical registry-view
   * for given gedcom and name of view
   */
  private Registry getRegistry(Gedcom gedcom, String nameOfView) {

    // Check which iteration number is available next
    Origin origin = gedcom.getOrigin();
    String name = origin.getFileName();
    int number;
    for (number=1;;number++) {
      if (App.getInstance().getFrame(name+"."+nameOfView+"."+number)==null) {
        break;
      }
    }

    // Try to find a registry
    Registry registry = Registry.lookup(name);
    if (registry==null) {
      registry = new Registry(name, origin);
    }

    return new Registry(registry, nameOfView+"."+number);

    // done
  }
  
  /**
   * Opens settings for given view settings component
   */
  /*package*/ void openSettings(ViewWidget viewWidget) {

    // the frame for the settings
    JFrame frame = App.getInstance().getFrame("settings");
    if (frame==null) {
      // create it
      frame = App.getInstance().createFrame(
        resources.getString("view.edit.title"),
        Images.imgSettings,
        "settings",
        new Dimension(256,480)
      );
      // and the SettingsWidget
      SettingsWidget sw = new SettingsWidget(resources, frame);
      frame.getContentPane().add(sw);
      // layout      
      frame.pack();
    }
    
    // get the SettingsWidget
    SettingsWidget sw = (SettingsWidget)frame.getContentPane().getComponent(0);
    sw.setViewWidget(viewWidget);
    
    // show it
    frame.show();
        
    // done
  }

  /**
   * Callback that a view was closed
   */
  /*package*/ void closeNotify(ViewWidget viewWidget) {
    
    // close property editor if open and showing settings
    JFrame frame = App.getInstance().getFrame("settings");
    if (frame!=null) { 
      // close the settings 
     SettingsWidget sw = (SettingsWidget)frame.getContentPane().getComponent(0);
     if (sw.getViewWidget()==viewWidget) frame.dispose();
    }
    
    // get rid of traces
    if (viewWidgets.contains(viewWidget)) viewWidgets.remove(viewWidget);
    
    // done
  }
  
  /**
   * Get actions for given entity/gedcom
   */
  /*package*/ List getActions(Object object) {
    // loop through descriptors
    List result = new ArrayList(16);
    for (int d=0; d<descriptors.length; d++) {
      Descriptor descriptor = descriptors[d];
      if (descriptor.factory instanceof ContextMenuSupport) {
        ContextMenuSupport cms = (ContextMenuSupport)descriptor.factory;
        List as = object instanceof Gedcom ? cms.createActions((Gedcom)object) : cms.createActions((Entity)object);
        if (as!=null) result.add(as);
      }
    }
    // loop through views
    Gedcom gedcom = object instanceof Gedcom ? (Gedcom)object : ((Entity)object).getGedcom();
    Iterator views = viewWidgets.iterator();
    while (views.hasNext()) {
      ViewWidget view = (ViewWidget)views.next();
      if (view.getGedcom()==gedcom&&view.getView() instanceof ContextMenuSupport) {
        ContextMenuSupport cms = (ContextMenuSupport)view.getView();
        List as = object instanceof Gedcom ? cms.createActions((Gedcom)object) : cms.createActions((Entity)object);
        if (as!=null) result.add(as);
      }
    }
    // done
    return result;
  }

  /**
   * Opens a view on a gedcom file
   */
  public void openView(Descriptor descriptor, Gedcom gedcom) {
    
    // get a registry 
    Registry registry = getRegistry(gedcom, descriptor.key);
    
    // a frame
    JFrame frame = App.getInstance().createFrame(
      gedcom.getName()+" - "+descriptor.getTitle()+" ("+registry.getViewSuffix()+")",
      descriptor.img,
      gedcom.getName() + "." + registry.getView(),
      descriptor.dim
    );
    
    // the viewwidget
    ViewWidget viewWidget = new ViewWidget(frame,gedcom,registry,descriptor);

    // show it
    frame.getContentPane().add(viewWidget);
    frame.pack();
    frame.show();
    
    // remember
    viewWidgets.add(viewWidget);
    
    // done
  }
  
  /**
   * Closes all views on given Gedcom
   */
  public void closeViews(Gedcom gedcom) {
    // look for views looking at gedcom    
    Iterator it = viewWidgets.iterator();
    while (it.hasNext()) {
      ViewWidget vw = (ViewWidget)it.next();
      if (vw.getGedcom()==gedcom) {
        it.remove();
        vw.getFrame().dispose();
      }
    }
    // remove its key from gedcom2current
    gedcom2current.remove(gedcom);

    // done
    
  }

  /**
   * A descriptor of a View
   */
  public static class Descriptor {
    
    public ViewFactory factory;
    public String key;
    public ImgIcon img;
    public Dimension dim;
    
    /**
     * Constructor
     */
    protected Descriptor(String f, String k, ImgIcon i, Dimension d) {
      // remember
      key=k;img=i;dim=d;
      // create
      try {
        factory = (ViewFactory)Class.forName(f).newInstance();
      } catch (Throwable t) {
        throw new RuntimeException("ViewFactory "+factory+" couldn't be instantiated");
      }
      // done
    }
    
    /**
     * Return a title representation
     */
    public String getTitle() {    
      return resources.getString("view.title."+key);
    }
    
    /**
     * Return a short representation
     */
    public String getShortTitle() {    
      return resources.getString("view.short."+key);
    }

    /**
     * Return a tip representation
     */
    public String getTip() {    
      return resources.getString("view.tip."+key);
    }
    
  } //Descriptor

}
