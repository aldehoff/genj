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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.print.PrintManager;
import genj.renderer.BlueprintManager;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.MenuHelper;
import genj.window.WindowManager;

import java.awt.Point;
import java.awt.Toolkit;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;

/**
 * A bridge to open/manage Views
 */
public class ViewManager {

  /** resources */
  public static Resources resources = Resources.get(ViewManager.class);
  
  /** registry */
  private Registry registry;

  /** factory instances of views */
  private ViewFactory[] factories = null;
  
  /** open views */
  private Map key2viewwidget = new HashMap();
  
  /** the currently selected entity */
  private Map gedcom2current = new HashMap();
  
  /** a print manager */
  private PrintManager printManager = null;

  /** a window manager */
  private WindowManager windowManager = null;
  
  /** a blueprint manager */
  private BlueprintManager blueprintManager = null;

  /**
   * Constructor
   */
  public ViewManager(Registry reGistry, PrintManager pManager, WindowManager wManager, String[] factoryTypes) {
    // remember
    registry = reGistry;
    printManager = pManager;
    windowManager = wManager;
    blueprintManager = new BlueprintManager(registry);
    // creat list of factories
    factories = new ViewFactory[factoryTypes.length];
    for (int f=0;f<factories.length;f++) {    
      try {
        factories[f] = (ViewFactory)Class.forName(factoryTypes[f]).newInstance();
      } catch (Throwable t) {
        throw new IllegalArgumentException("Factory of type "+factories[f]+" cannot be instantiated ("+t.getMessage()+")");
      }
    }
    // done
  }

  /**
   * Makes a snapshot of the current view configuration
   */
  public void snapshot() {
    // tell to blueprint manager
    blueprintManager.snapshot();
    // save window setup?
    // ...
    // done
  }
  
  /**
   * Returns all known view factories
   */
  public ViewFactory[] getFactories() {
    return factories;
  }
  
  /**
   * Returns the last set context for given gedcom
   * @return the property (might be null)
   */
  public Property getContext(Gedcom gedcom) {
    // grab one from map
    Property result = (Property)gedcom2current.get(gedcom);
    if (result!=null) {
      // still in valid entity?
      Entity entity = result.getEntity();
      // 20040305 make sure entity isn't null by now
      if (entity!=null&&gedcom.getEntities(entity.getTag()).contains(entity))
        return result;
      // remove from map 
      gedcom2current.remove(gedcom);
    }
    // try first indi 
    result = gedcom.getAnyEntity(Gedcom.INDI);
    if (result!=null) 
      gedcom2current.put(gedcom, result);
    // done here
    return result;
  }

  /**
   * Sets the current context
   */
  public void setContext(Property property) {
    // already?
    Gedcom gedcom = property.getGedcom();
    // remember
    // 20030402 don't block propagation if already current
    // let it be signalled twice if necessary
    //   if (gedcom2current.get(gedcom)==property) return;
    gedcom2current.put(gedcom, property);
    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();
    // loop and tell to views
    Iterator it = key2viewwidget.values().iterator();
    while (it.hasNext()) {
      ViewWidget vw = (ViewWidget)it.next();
      // only if view on same gedcom
      if (vw.getGedcom()!= gedcom) continue;
      // and context supported
      if (vw.getView() instanceof ContextSupport)
        ((ContextSupport)vw.getView()).setContext(property);
      // next
    }
    // done
  }

  /** 
   * Accessor - DPI
   */  
  public Point getDPI() {
    Point dpi = registry.get("dpi",(Point)null); 
    if (dpi==null) {
      dpi = new Point( 
        Toolkit.getDefaultToolkit().getScreenResolution(),
        Toolkit.getDefaultToolkit().getScreenResolution()
      );
    }
    return dpi;
  }
  
  /** 
   * Accessor - DPI
   */  
  public void setDPI(Point dpi) {
    registry.put("dpi",dpi); 
  }
  
  /**
   * The print manager
   */
  public PrintManager getPrintManager() {
    return printManager;
  }
  
  /**
   * The window manager
   */
  public WindowManager getWindowManager() {
    return windowManager;
  }
  
  /**
   * The blueprint manager
   */
  public BlueprintManager getBlueprintManager() {
    return blueprintManager;
  }
  
  /**
   * Opens settings for given view settings component
   */
  /*package*/ void openSettings(ViewWidget viewWidget) {
    
    // Frame already open?
    SettingsWidget settings = (SettingsWidget)windowManager.getContent("settings");
    if (settings==null) {
      settings = new SettingsWidget(resources, this);
      settings.setViewWidget(viewWidget);
      windowManager.openFrame(
        "settings", 
        resources.getString("view.edit.title"),
        Images.imgSettings,
        settings,
        null, null, 
        null
      );
    } else {
      settings.setViewWidget(viewWidget);
    }
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
      if (!key2viewwidget.containsKey(name+"."+nameOfView+"."+number)) {
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
   * Resolves the Gedcom for given context
   */
  private Gedcom getGedcom(Object context) {
    if (context instanceof Gedcom  ) return (Gedcom)context;
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
      if (factories[f] instanceof ActionSupport) {
        List as = getActions((ActionSupport)factories[f], context);
        if (as!=null&&!as.isEmpty()) result.add(as);
      }
    }
    // loop through views
    Gedcom gedcom = getGedcom(context);
    Iterator views = key2viewwidget.values().iterator();
    while (views.hasNext()) {
      ViewWidget view = (ViewWidget)views.next();
      if (view.getGedcom()==gedcom&&view.getView() instanceof ActionSupport) {
        List as = getActions((ActionSupport)view.getView(), context);
        if (as!=null&&!as.isEmpty()) result.add(as);
      }
    }
    // done
    return result;
  }

  /**
   * Resolves the context information from support/context
   */
  private List getActions(ActionSupport cs, Object context) {
    // get result by calling appropriate support method
    List result;
    if (context instanceof Gedcom) 
      result = cs.createActions((Gedcom  )context, this);
    else if (context instanceof Entity) 
      result = cs.createActions ((Entity  )context, this);
    else if (context instanceof Property) 
      result = cs.createActions ((Property)context, this);
    else throw new IllegalArgumentException();
    // done
    return result;
  }

  /**
   * Calculate a logical key for given factory
   */
  private String getKey(ViewFactory factory) {
    
    String key = factory.getClass().getName();
    
    // get rid of classname
    int lastdot = key.lastIndexOf('.');
    if (lastdot>0) key = key.substring(0, lastdot);
    
    // get rid of pre-packages
    while (true) {
      int dot = key.indexOf('.');
      if (dot<0) break;
      key = key.substring(dot+1);
    }

    return key.toLowerCase();    
// 20030521 interestingly getPackage() doesn't
// always seem to return something (e.g. Konqueror applet)
//    String pkg = factory.getClass().getPackage().getName();
//    int lastdot = pkg.lastIndexOf('.');
//    return lastdot<0 ? pkg : pkg.substring(lastdot+1);
  }

  /**
   * Opens a view on a gedcom file
   * @return the view component
   */
  public JComponent openView(Class factory, Gedcom gedcom) {
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
  public JComponent openView(ViewFactory factory, Gedcom gedcom) {
    
    // get a registry 
    Registry registry = getRegistry(gedcom, getKey(factory));
    
    // title & key
    final String 
      title = gedcom.getName()+" - "+factory.getTitle(false)+" ("+registry.getViewSuffix()+")",
      key = gedcom.getName() + "." + registry.getView();
    
    // the viewwidget
    final ViewWidget viewWidget = new ViewWidget(key,title,gedcom,registry,factory, this);

    // remember
    key2viewwidget.put(key, viewWidget);

    // prepare to forget
    Runnable onClose = new Runnable() {
      public void run() {
        // close property editor if open and showing settings
        windowManager.close("settings");
        // 20021017 @see note at the bottom of file
        MenuSelectionManager.defaultManager().clearSelectedPath();
        // forget about it
        key2viewwidget.remove(key);
        // done
      }
    };
    
    // open frame
    windowManager.openFrame(
      key, 
      title, 
      factory.getImage(),
      viewWidget,
      null, null,
      onClose
    );
        
    // done
    return viewWidget.getView();
  }
  
  /**
   * Closes all views on given Gedcom
   */
  public void closeViews(Gedcom gedcom) {
    
    // look for views looking at gedcom    
    ViewWidget[] vws = (ViewWidget[])key2viewwidget.values().toArray(new ViewWidget[key2viewwidget.size()]);
    for (int i=0;i<vws.length;i++) {
      if (vws[i].getGedcom()==gedcom) 
        windowManager.close(vws[i].getKey());
    }
    
    // remove its key from gedcom2current
    gedcom2current.remove(gedcom);

    // done
  }
  
  /** 
   * Show a view (bring it to front)
   */
  public void showView(JComponent view) {

    // loop through views
    Iterator vws = key2viewwidget.values().iterator();
    while (vws.hasNext()) {
      ViewWidget vw = (ViewWidget)vws.next();
      if (vw.getView()==view) {
        windowManager.show(vw.getKey());
        break;
      }
    }
    
    // not found    
  }

  /**
   * Show a context menu for given point - at this
   * point we assume that view instanceof EntityPopupSupport
   */
  public void showContextMenu(JComponent container, Point point, Gedcom gedcom, ContextSupport.Context context) {
    
    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();

    // create a popup
    MenuHelper mh = new MenuHelper().setTarget(container);
    mh.setTarget(container);
    JPopupMenu popup = mh.createPopup();

    // the context might have some actions we're going to add
    mh.createItems(context.getActions());
  
    // we need Entity, property and Gedcom (above) from context
    Property property = context.getProperty();
    if (property!=null) {
      Entity entity = property.getEntity();
  
      // items for property
      if (property!=entity) {
        List actions = getActions(property);
        if (!actions.isEmpty()) {
          String title = "Property '"+TagPath.get(property)+'\'';
          mh.createMenu(title, property.getImage(false));
          mh.createItems(actions);
          mh.popMenu();
        }
      }
      
      // items for entity
      List actions = getActions(entity);
      if (!actions.isEmpty()) {
        String title = Gedcom.getEntityName(entity.getTag(),false)+" '"+entity.getId()+'\'';
        mh.createMenu(title, entity.getImage(false));
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
    
    // show the popup
    if (popup.getComponentCount()>0)
      popup.show(container, point.x, point.y);
    
    // done
  }  
  
  /**
   * Returns views and factories with given support 
   */
  public Object[] getInstances(Class of, Gedcom gedcom) {
    
    List result = new ArrayList(16);
    
    // loop through factories
    for (int f=0; f<factories.length; f++) {
      if (of.isAssignableFrom(factories[f].getClass())) 
        result.add(factories[f]);
    }
    // loop through views
    Iterator views = key2viewwidget.values().iterator();
    while (views.hasNext()) {
      ViewWidget view = (ViewWidget)views.next();
      if (view.getGedcom()==gedcom && of.isAssignableFrom(view.getView().getClass()))
        result.add(view.getView());
    }
    
    // done
    return result.toArray((Object[])Array.newInstance(of, result.size()));
  }

} //ViewManager