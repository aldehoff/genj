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
import genj.gedcom.TagPath;
import genj.util.Debug;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.MenuHelper;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;

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
    "genj.table.Table",
    "genj.tree.Tree",
    "genj.timeline.Timeline",
    "genj.edit.Edit",
    "genj.report.Report",
    "genj.nav.Navigator",
    "genj.entity.Entity" 
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
        result.add((ViewFactory)Class.forName(FACTORIES[f]+"ViewFactory").newInstance());
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
    // grab one from map
    Entity result = (Entity)gedcom2current.get(gedcom);
    if (result!=null) {
      // still valid?
      if (gedcom.getEntities(result.getType()).contains(result))
        return result;
      // remove from map 
      gedcom2current.remove(gedcom);
    }
    // try first indi 
    List indis = gedcom.getEntities(Gedcom.INDIVIDUALS);
    if (!indis.isEmpty()) {
      result = (Entity)indis.get(0);
      gedcom2current.put(gedcom, result);
    }
    // done here
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
      frame.dispose();
    }
    
    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();
    
    // get rid of traces
    if (viewWidgets.contains(viewWidget)) viewWidgets.remove(viewWidget);
    
    // done
  }
  
  /**
   * Resolves the Gedcom for given context
   */
  private Gedcom getGedcom(Object context) {
    if (context instanceof Gedcom  ) return (Gedcom)context;
    if (context instanceof Entity  ) return ((Entity)context).getProperty().getGedcom();
    if (context instanceof Property) return ((Property)context).getGedcom();
    throw new IllegalArgumentException("Unknown context "+context);
  }

  /**
   * Get actions for given entity/gedcom
   */
  private List getActions(Object context) {
    // loop through descriptors
    List result = new ArrayList(16);
    for (int f=0; f<factories.length; f++) {
      if (factories[f] instanceof ContextSupport) {
        List as = getActions((ContextSupport)factories[f], context);
        if (as!=null&&!as.isEmpty()) result.add(as);
      }
    }
    // loop through views
    Gedcom gedcom = getGedcom(context);
    Iterator views = viewWidgets.iterator();
    while (views.hasNext()) {
      ViewWidget view = (ViewWidget)views.next();
      if (view.getGedcom()==gedcom&&view.getView() instanceof ContextSupport) {
        List as = getActions((ContextSupport)view.getView(), context);
        if (as!=null&&!as.isEmpty()) result.add(as);
      }
    }
    // done
    return result;
  }

  /**
   * Resolves the context information from support/context
   */
  private List getActions(ContextSupport cs, Object context) {
    // get result by calling appropriate support method
    List result;
    if (context instanceof Gedcom) 
      result = cs.createActions((Gedcom  )context);
    else if (context instanceof Entity) 
      result = cs.createActions ((Entity  )context);
    else if (context instanceof Property) 
      result = cs.createActions ((Property)context);
    else throw new IllegalArgumentException();
    // done
    return result;
  }

  /**
   * Opens a view on a gedcom file
   * @return the view component
   */
  public Component openView(Class factory, Gedcom gedcom) {
    for (int f=0; f<factories.length; f++) {
      if (factories[f].getClass().equals(factory)) 
        return openView(factories[f], gedcom);   	
    }
    throw new IllegalArgumentException("Unknown factory "+factory.getName());
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

    // remember
    viewWidgets.add(viewWidget);
    
    // show it
    frame.getContentPane().add(viewWidget);
    frame.pack();
    frame.show();
    
    // done
    return viewWidget.getView();
  }
  
  /**
   * Returns views of given type    */
  public List getOpenViews(Class type, Gedcom gedcom) {
    List result = new ArrayList(5);
    // look through views
    Iterator it = viewWidgets.iterator();
    while (it.hasNext()) {
      ViewWidget vw = (ViewWidget)it.next();
      if (vw.getView().getClass().equals(type) && vw.getGedcom()==gedcom) result.add(vw.getView());
    }
    // done
    return result;
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
   * Fills a menu with context actions 
   */
  public void fillContextMenu(MenuHelper mh, Gedcom gedcom, ContextPopupSupport.Context context) {

    // the context might have some actions we're going to add
    mh.createItems(context.getActions());

    // we need Entity, property and Gedcom (above) from context
    Entity entity = null;
    Property property = null;
    if (context.getContent() instanceof Entity) {
      entity = (Entity)context.getContent();
    } else if (context.getContent() instanceof Property) {
      property = (Property)context.getContent();
      entity = property.getEntity();
    }

    // items for property
    if (property!=null) {
      List actions = getActions(property);
      if (!actions.isEmpty()) {
        String title = "Property '"+TagPath.get(property)+'\'';
        mh.createMenu(title, property.getImage(false));
        mh.createItems(actions);
        mh.popMenu();
      }
    }
    
    // items for entity
    if (entity!=null) {
      List actions = getActions(entity);
      if (!actions.isEmpty()) {
        String title = Gedcom.getNameFor(entity.getType(),false)+" '"+entity.getId()+'\'';
        mh.createMenu(title, entity.getProperty().getImage(false));
        mh.createItems(actions);
        mh.popMenu();
      }
    }
    
    // items for gedcom
    List actions = getActions(gedcom);
    if (!actions.isEmpty()) {
      String title = "Gedcom '"+gedcom.getName()+'\'';
      mh.createMenu(title, Gedcom.getImage());
      mh.createItems(actions);
      mh.popMenu();
    }

    // done    
  }
  
  /**
   * Show a context menu for given point - at this
   * point we assume that view instanceof EntityPopupSupport
   */
  public void showContextMenu(JComponent container, Point point, Gedcom gedcom, ContextPopupSupport.Context context) {
    
    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();

    // create a popup
    MenuHelper mh = new MenuHelper().setTarget(container);
    mh.setTarget(container);
    JPopupMenu popup = mh.createPopup("");

    // fill the context actions
    fillContextMenu(mh, gedcom, context);
    
    // show the popup
    if (popup.getComponentCount()>0)
      popup.show(container, point.x, point.y);
    
    // done
  }  
  
  /**
   * Returns views and factories with given support 
   */
  public Object[] getSupportFor(Class support, Gedcom gedcom) {
    
    List result = new ArrayList(16);
    
    // loop through factories
    for (int f=0; f<factories.length; f++) {
      if (support.isAssignableFrom(factories[f].getClass())) 
        result.add(factories[f]);
    }
    // loop through views
    Iterator views = viewWidgets.iterator();
    while (views.hasNext()) {
      ViewWidget view = (ViewWidget)views.next();
      if (view.getGedcom()==gedcom && support.isAssignableFrom(view.getView().getClass()))
        result.add(view.getView());
    }
    
    // done
    return result.toArray((Object[])Array.newInstance(support, result.size()));
  }

} //ViewManager