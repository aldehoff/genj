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
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.MenuHelper;
import genj.window.WindowManager;

import java.awt.Point;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.Timer;

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
  
  /** the currently valid context */
  private Map gedcom2context = new HashMap();
  
  /** a print manager */
  private PrintManager printManager = null;

  /** a window manager */
  private WindowManager windowManager = null;
  
  /** context listeners */
  private List contextListeners = new LinkedList();
  
  /** a timer for delayed context changes */
  private Timer propagateContext;

  /**
   * Constructor
   */
  public ViewManager(Registry reGistry, PrintManager pManager, WindowManager wManager, String[] factoryTypes) {
    
    // remember
    registry = reGistry;
    printManager = pManager;
    windowManager = wManager;
    
    // creat list of factories
    factories = new ViewFactory[factoryTypes.length];
    for (int f=0;f<factories.length;f++) {    
      try {
        ViewFactory factory = (ViewFactory)Class.forName(factoryTypes[f]).newInstance();
        factories[f] = factory;
        if (factory instanceof ContextListener)
          addContextListener((ContextListener)factory);
      } catch (Throwable t) {
        throw new IllegalArgumentException("Factory of type "+factories[f]+" cannot be instantiated ("+t.getMessage()+")");
      }
    }
    
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
   * @return the context
   */
  public Context getContext(Gedcom gedcom) {
    
    // grab one from map
    Context result = (Context)gedcom2context.get(gedcom);
    
    // fallback to last stored?
    if (result==null) {
      try {
        result = new Context(gedcom.getEntity(getRegistry(gedcom).get("lastEntity", (String)null)));
      } catch (Throwable t) {
      }
    } else {
      // still in valid entity?
      Entity entity = result.getEntity();
      // 20040305 make sure entity isn't null by now
      if (entity==null||!gedcom.getEntities(entity.getTag()).contains(entity)) {
        // remove from map 
        gedcom2context.remove(gedcom);
        // reset
        result=null;
      }
    }
    
    // fallback to first indi 
    if (result==null)
      result = new Context(gedcom, gedcom.getAnyEntity(Gedcom.INDI), null);

    // remember
    gedcom2context.put(gedcom, result);
    
    // done here
    return result;
  }

  /**
   * Sets the current context
   */
  public void setContext(Context context) {
    
    // valid?
    if (!context.isValid())
      return;

    // remember context
    Gedcom gedcom = context.getGedcom();
    gedcom2context.put(gedcom, context);
    if (context.getEntity()!=null)
      getRegistry(gedcom).put("lastEntity", context.getEntity().getId());
    
    // clear any menu selections
    MenuSelectionManager.defaultManager().clearSelectedPath();

    // initiate context propagation
    if (propagateContext==null)
      propagateContext = new Timer(100, new PropagateContext());
    propagateContext.stop();
    propagateContext.start();
    
    // done
  }

  /** 
   * Accessor - DPI
   */  
  public Point getDPI() {
    return Options.getInstance().getDPI();
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
    return BlueprintManager.getInstance();
  }
  
  /**
   * Opens settings for given view settings component
   */
  /*package*/ void openSettings(ViewContainer viewWidget) {
    
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
   * Helper that returns registry for gedcom
   */
  private Registry getRegistry(Gedcom gedcom) {
    Origin origin = gedcom.getOrigin();
    String name = origin.getFileName();
    Registry registry = Registry.lookup(name);
    if (registry==null) 
      registry = new Registry(name, origin);
    return registry;
  }

  /**
   * Helper that returns the next logical registry-view
   * for given gedcom and name of view
   */
  private Registry getNextRegistry(Gedcom gedcom, String nameOfView) {

    // Check which iteration number is available next
    String name = gedcom.getOrigin().getFileName();
    int number;
    for (number=1;;number++) {
      if (!key2viewwidget.containsKey(name+"."+nameOfView+"."+number)) 
        break;
    }

    // create the view
    return new Registry(getRegistry(gedcom), nameOfView+"."+number);
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
    Registry registry = getNextRegistry(gedcom, getKey(factory));
    
    // title & key
    final String 
      title = gedcom.getName()+" - "+factory.getTitle(false)+" ("+registry.getViewSuffix()+")",
      key = gedcom.getName() + "." + registry.getView();
    
    // the viewwidget
    final ViewContainer viewWidget = new ViewContainer(key,title,gedcom,registry,factory, this);

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
    ViewContainer[] vws = (ViewContainer[])key2viewwidget.values().toArray(new ViewContainer[key2viewwidget.size()]);
    for (int i=0;i<vws.length;i++) {
      if (vws[i].getGedcom()==gedcom) 
        windowManager.close(vws[i].getKey());
    }
    
    // remove its key from gedcom2current
    gedcom2context.remove(gedcom);

    // done
  }
  
  /** 
   * Show a view (bring it to front)
   */
  public void showView(JComponent view) {

    // loop through views
    Iterator vws = key2viewwidget.values().iterator();
    while (vws.hasNext()) {
      ViewContainer vw = (ViewContainer)vws.next();
      if (vw.getView()==view) {
        windowManager.show(vw.getKey());
        break;
      }
    }
    
    // not found    
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
      ViewContainer view = (ViewContainer)views.next();
      if (view.getGedcom()==gedcom && of.isAssignableFrom(view.getView().getClass()))
        result.add(view.getView());
    }
    
    // done
    return result.toArray((Object[])Array.newInstance(of, result.size()));
  }
  
  /**
   * Register a listener
   */
  public void addContextListener(ContextListener listener) {
    contextListeners.add(listener);
  }
  
  /**
   * Deregister a listener
   */
  public void removeContextListener(ContextListener listener) {
    contextListeners.remove(listener);
  }
  
  /**
   * Get a context menu
   */
  public JPopupMenu getContextMenu(Context context, List actions, JComponent target) {
    
    // make sure context is valid
    if (!context.isValid())
      return null;
    
    Property property = context.getProperty();
    Entity entity = context.getEntity();
    Gedcom gedcom = context.getGedcom();

    // make sure any existing popup is cleared
    MenuSelectionManager.defaultManager().clearSelectedPath();

    // hook up context's source
    context.setSource(target);
    
    // create a popup
    MenuHelper mh = new MenuHelper().setTarget(target);
    JPopupMenu popup = mh.createPopup();

    // popup local actions?
    if (actions!=null&&!actions.isEmpty())
      mh.createItems(actions, false);
  
    // find ActionSupport implementors
    ActionProvider[] as = (ActionProvider[])getInstances(ActionProvider.class, context.getGedcom());

    // items for property
    if (property!=null&&property!=entity) {
      String title = Property.LABEL+" '"+TagPath.get(property)+'\'';
      mh.createMenu(title, property.getImage(false));
      for (int i = 0; i < as.length; i++) {
        mh.createItems(as[i].createActions(property, this), true);
      }
      mh.popMenu();
    }
        
    // items for entity
    if (entity!=null) {
      String title = Gedcom.getName(entity.getTag(),false)+" '"+entity.getId()+'\'';
      mh.createMenu(title, entity.getImage(false));
      for (int i = 0; i < as.length; i++) {
        mh.createItems(as[i].createActions(entity, this), true);
      }
      mh.popMenu();
    }
        
    // items for gedcom
    String title = "Gedcom '"+gedcom.getName()+'\'';
    mh.createMenu(title, Gedcom.getImage());
    for (int i = 0; i < as.length; i++) {
      mh.createItems(as[i].createActions(gedcom, this), true);
    }
    mh.popMenu();

    // done
    return popup;
  }
  
  /**
   * Show a context menu
   */
  public void showContextMenu(Context context, List actions, JComponent component, Point pos) {
    JPopupMenu popup = getContextMenu(context, actions, component);
    if (popup!=null)
      popup.show(component, pos.x, pos.y);
  }
  
  /**
   * Propagate context change(s)
   */
  private class PropagateContext extends ActionDelegate {
    
    /**
     * propagate
     */
    protected void execute() {
      
      // check current contexts
      Iterator contexts = gedcom2context.values().iterator();
      while (contexts.hasNext()) {
        Context context = (Context)contexts.next();
        if (!context.isPropagated()) {
          propagate(context);
        }
      }
      
      // done
    }
    
    /**
     * propagate
     */
    private void propagate(Context context) {
      
      Gedcom gedcom = context.getGedcom();
      
      // connect to us
      context.setManager(ViewManager.this);
      
      // see if source is a view which will get context message first
      JComponent view = context.getView();
      if (view!=null&&view instanceof ContextListener) {
        try {
          ((ContextListener)view).setContext(context);
        } catch (Throwable t) {
          Debug.log(Debug.WARNING, view, "threw "+t+" on setContext()");
        }
      }
      
      // loop and tell to views
      Iterator it = key2viewwidget.values().iterator();
      while (it.hasNext()) {
        ViewContainer vw = (ViewContainer)it.next();
        // only if view on same gedcom
        if (vw.getGedcom()!= gedcom) continue;
        // and context supported
        if (vw.getView() instanceof ContextListener) try {
          ((ContextListener)vw.getView()).setContext(context);
        } catch (Throwable t) {
          Debug.log(Debug.WARNING, vw.getView(), "ContextListener threw throwable", t);
        }
        // next
      }
      
      // loop and tell to context listeners
      ContextListener[] ls = (ContextListener[])contextListeners.toArray(new ContextListener[contextListeners.size()]);
      for (int l=0;l<ls.length;l++)
        try {      
          ls[l].setContext(context);
        } catch (Throwable t) {
          Debug.log(Debug.WARNING, ls[l], "ContextListener threw throwable", t);
        }
      
      // done
      context.setPropagated();
    }
    
  } //ContextChange
  
} //ViewManager