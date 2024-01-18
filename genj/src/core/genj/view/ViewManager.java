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
import genj.util.Debug;
import genj.util.ImgIcon;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImgIconConverter;
import genj.util.swing.MenuHelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
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
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.MenuSelectionManager;
import javax.swing.border.TitledBorder;

/**
 * A bridge to open/manage Views
 */
public class ViewManager {

  /** instance */
  private static ViewManager instance;

  /** resources */
  public final static Resources resources = new Resources("genj.view");

  /** factories of views */
  static final private String[] FACTORIES = new String[]{
    "genj.table.TableViewFactory",
    "genj.tree.TreeViewFactory",
    "genj.timeline.TimelineViewFactory",
    "genj.edit.EditViewFactory",
    "genj.report.ReportViewFactory",
    "genj.nav.NavigatorViewFactory",
    "genj.entity.EntityViewFactory" 
  };
  
  /** factory instances of views */
  static private ViewFactory[] factories = null;
  
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
   * Returns all known view factories
   */
  public ViewFactory[] getFactories() {
    // already computed?
    if (factories!=null) return factories;
    // create 'em
    List result = new ArrayList();
    for (int f=0; f<FACTORIES.length; f++) {
      try {
        result.add((ViewFactory)Class.forName(FACTORIES[f]).newInstance());
      } catch (Throwable t) {
        Debug.log(Debug.ERROR, this, "ViewFactory "+FACTORIES[f]+" couldn't be instantiated");
      }
    }
    // convert to array
    factories = new ViewFactory[result.size()];
    result.toArray(factories);
    // done
    return factories;
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
    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();
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
    
    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();
    
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
    for (int f=0; f<factories.length; f++) {
      if (factories[f] instanceof ContextMenuSupport) {
        ContextMenuSupport cms = (ContextMenuSupport)factories[f];
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
   * @return the view component
   */
  public Component openView(ViewFactory factory, Gedcom gedcom) {
    
    // get a registry 
    Registry registry = getRegistry(gedcom, factory.getKey());
    
    // a frame
    JFrame frame = App.getInstance().createFrame(
      gedcom.getName()+" - "+factory.getTitle(false)+" ("+registry.getViewSuffix()+")",
      factory.getImage(),
      gedcom.getName() + "." + registry.getView(),
      factory.getDefaultDimension()
    );
    
    // the viewwidget
    ViewWidget viewWidget = new ViewWidget(frame,gedcom,registry,factory);

    // show it
    frame.getContentPane().add(viewWidget);
    frame.pack();
    frame.show();
    
    // remember
    viewWidgets.add(viewWidget);
    
    // done
    return viewWidget.getView();
  }
  
  /**
   * Checks whether a view instance for given type is open
   */
  public boolean isOpenView(Class view) {
    // look through views
    Iterator it = viewWidgets.iterator();
    while (it.hasNext()) {
      ViewWidget vw = (ViewWidget)it.next();
      if (vw.getView().getClass().equals(view)) return true;
    }
    // none
    return false;
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
   * Show a context menu for given point - at this
   * point we assume that view instanceof EntityPopupSupport
   */
  public void showContextMenu(JComponent container, Point point, Gedcom gedcom, Entity entity) {
    
    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();

    // create a popup
    MenuHelper mh = new MenuHelper();
    JPopupMenu popup = mh.createPopup("");
    
    // items for entity
    if (entity!=null) {
      List actions = ViewManager.getInstance().getActions(entity);
      if (!actions.isEmpty()) {
        mh.createMenu(entity.getId(), entity.getProperty().getImage(false));
        mh.createItems(actions);
        mh.popMenu();
      }
    }
    
    // items for gedcom
    List actions = getActions(gedcom);
    if (!actions.isEmpty()) {
      mh.createMenu(gedcom.getName(), Gedcom.getImage());
      mh.createItems(actions);
      mh.popMenu();
    }
    
    // show the popup
    if (popup.getComponentCount()>0)
      popup.show(container, point.x, point.y);
    
    // done
  }

  // 20021017 strangely Popups for JPopupMenu don't seem to
  // disappear even though of mouse-clicks somewhere in the
  // view. Calling
  //  MenuSelectionManager.defaultManager().clearSelectedPath();
  // before bringing up a popup makes sure that it disappears 
  // when anything is clicking in the view.
  // Also popups are not removed by Swing after opening up
  // a JPopupMenu in one view and clicking on components in 
  // another view. We could do some stuff with windowDeactivated 
  // but that seems too much to bother. So for now we'll call
  //  MenuSelectionManager.defaultManager().clearSelectedPath();
  // which will get rid of the popup anytime 
  //  setCurrentEntity() 
  // is called. That will make sure there's no current-change
  // with a popup still being open for the last current.
  // Lastly we also call 
  //  MenuSelectionManager.defaultManager().clearSelectedPath();
  // in removeNotify() when a view is removed. Otherwise Swing
  // might keep a popup open in another view showing items that
  // are applicable to an already closed view.
  
} //ViewManager
