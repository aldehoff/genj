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
import genj.util.MnemonicAndText;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.MenuHelper;
import genj.window.WindowManager;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import sun.misc.Service;

/**
 * A bridge to open/manage Views
 */
public class ViewManager {
  
  /*package*/ ContextHook HOOK = new ContextHook();

  /*package*/ final static Logger LOG = Logger.getLogger("genj.view");

  /** resources */
  /*package*/ static Resources RESOURCES = Resources.get(ViewManager.class);
  
  /** global accelerators */
  /*package*/ Map keyStrokes2factories = new HashMap();
  
  /** factory instances of views */
  private ViewFactory[] factories = null;
  
  /** open views */
  private Map factoryType2viewHandles = new HashMap();
  private LinkedList allHandles = new LinkedList();
  
  /** the currently valid context */
  private Map gedcom2context = new HashMap();
  
  /** a print manager */
  private PrintManager printManager = null;

  /** a window manager */
  private WindowManager windowManager = null;
  
  /** context listeners */
  private List contextListeners = new LinkedList();
  
  /** ignore flag */
  private boolean ignoreSetContext = false;
  
  /**
   * Constructor
   */
  public ViewManager(PrintManager printManager, WindowManager windowManager) {

    // lookup all factories dynamically
    List factories = new ArrayList();
    Iterator it = Service.providers(ViewFactory.class);
    while (it.hasNext()) 
      factories.add(it.next());

    // continue with init
    init(printManager, windowManager, factories);
  }
  
  /**
   * Constructor
   */
  public ViewManager(PrintManager printManager, WindowManager windowManager, String[] factoryTypes) {
    
    // instantiate factories
    List factories = new ArrayList();
    for (int f=0;f<factoryTypes.length;f++) {    
      try {
        factories.add( (ViewFactory)Class.forName(factoryTypes[f]).newInstance() );
      } catch (Throwable t) {
        LOG.log(Level.SEVERE, "Factory of type "+factoryTypes[f]+" cannot be instantiated", t);
      }
    }
    
    // continue with init
    init(printManager, windowManager, factories);
  }
  
  /**
   * get all views for given gedcom 
   */
  public ViewHandle[] getViews(Gedcom gedcom) {
    
    // look for views looking at gedcom    
    List result = new ArrayList();
    for (Iterator handles = allHandles.iterator(); handles.hasNext() ; ) {
      ViewHandle handle = (ViewHandle)handles.next();
      if (handle.getGedcom()==gedcom)  
        result.add(handle);
    }
    
    // done
    return (ViewHandle[])result.toArray(new ViewHandle[result.size()]);
  }
  
  /**
   * Initialization
   */
  private void init(PrintManager setPrintManager, WindowManager setWindowManager, List setFactories) {
    
    // remember
    printManager = setPrintManager;
    windowManager = setWindowManager;
    
    // keep factories
    factories = (ViewFactory[])setFactories.toArray(new ViewFactory[setFactories.size()]);
    
    // loop over factories, grab keyboard shortcuts and sign up context listeners
    for (int f=0;f<factories.length;f++) {    
      ViewFactory factory = factories[f];
      // a context listener?
      if (factory instanceof ContextListener)
        addContextListener((ContextListener)factory);
      // check shortcut
      String keystroke = "ctrl "+new MnemonicAndText(factory.getTitle(false)).getMnemonic();
      if (!keyStrokes2factories.containsKey(keystroke)) {
        keyStrokes2factories.put(keystroke, factory);
      }
    }
    
    // done
  }
  
  /**
   * Installs global key accelerators for given component
   */
   
  
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
  public Context getLastSelectedContext(Gedcom gedcom) {
    
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
    if (result==null) {
      Entity e = gedcom.getFirstEntity(Gedcom.INDI);
      result = e!=null ? new Context(e) : new Context(gedcom);
    }

    // remember
    gedcom2context.put(gedcom, result);
    
    // done here
    return result;
  }

  /**
   * Sets the current context
   */
  public void fireContextSelected(Context context) {
    fireContextSelected(context, null);
  }
  public void fireContextSelected(Context context, ContextProvider provider) {
    fireContextSelected(context, false, provider);
  }
  public void fireContextSelected(Context context, boolean actionPerformed, ContextProvider provider) {
    
    // ignoring context?
    if (ignoreSetContext)
      return;
    ignoreSetContext = true;
    
    // create event
    ContextSelectionEvent e = new ContextSelectionEvent(context, provider, actionPerformed);

    // remember context
    Gedcom gedcom = context.getGedcom();
    gedcom2context.put(gedcom, context);
    if (context.getEntity()!=null)
      getRegistry(gedcom).put("lastEntity", context.getEntity().getId());
    
    // clear any menu selections if different from last context
    if (!context.equals(getLastSelectedContext(gedcom)))
      MenuSelectionManager.defaultManager().clearSelectedPath();

    // connect to us
    context.setManager(ViewManager.this);
    
    // loop and tell to views 
    for (Iterator lists = factoryType2viewHandles.values().iterator(); lists.hasNext() ;) {
      List list = (List)lists.next();
      for(Iterator handles = list.iterator(); handles.hasNext(); ) {
        ViewHandle handle = (ViewHandle)handles.next();
        // empty ?
        if (handle==null) continue;
        // only if view on same gedcom
        if (handle.getGedcom()!= gedcom) continue;
        // and context supported
        if (handle.getView() instanceof ContextListener) try {
          ((ContextListener)handle.getView()).handleContextSelectionEvent(e);
        } catch (Throwable t) {
          LOG.log(Level.WARNING, "ContextListener threw throwable", t);
        }
        // next viewhandle
      }
      // next list
    }
    
    // loop and tell to context listeners
    ContextListener[] ls = (ContextListener[])contextListeners.toArray(new ContextListener[contextListeners.size()]);
    for (int l=0;l<ls.length;l++)
      try {      
        ls[l].handleContextSelectionEvent(e);
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "ContextListener threw throwable", t);
      }
    
    // done
    ignoreSetContext = false;
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
  /*package*/ void openSettings(ViewHandle handle) {
    
    // Frame already open?
    SettingsWidget settings = (SettingsWidget)windowManager.getContent("settings");
    if (settings==null) {
      settings = new SettingsWidget(this);
      settings.setView(handle);
      windowManager.openFrame(
        "settings", 
        RESOURCES.getString("view.edit.title"),
        Images.imgSettings,
        settings,
        null, null
      );
    } else {
      settings.setView(handle);
    }
    // done
  }
  
  /**
   * Helper that returns registry for gedcom
   */
  public Registry getRegistry(Gedcom gedcom) {
    Origin origin = gedcom.getOrigin();
    String name = origin.getFileName();
    return Registry.lookup(name, origin);
  }
  
  /**
   * Get the package name of a Factory
   */
  /*package*/ String getPackage(ViewFactory factory) {
    
    Matcher m = Pattern.compile(".*\\.(.*)\\..*").matcher(factory.getClass().getName());
    if (!m.find())
      throw new IllegalArgumentException("can't resolve package for "+factory);
    return m.group(1);
    
  }

  /**
   * Next in the number of views for given factory
   */
  private int getNextInSequence(ViewFactory factory) {
    
    // check handles for factory
    List handles = (List)factoryType2viewHandles.get(factory.getClass());
    if (handles==null)
      return 1;
    
    // find first empty spot
    int result = 1;
    for (Iterator it = handles.iterator(); it.hasNext(); ) {
      ViewHandle handle = (ViewHandle)it.next();
      if (handle==null) break;
      result++;
    }
    
    return result;
  }
  
  /**
   * Closes a view
   */
  protected void closeView(ViewHandle handle) {
    // close property editor if open and showing settings
    windowManager.close("settings");
    // now close view
    windowManager.close(handle.getKey());
    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();
    // forget about it
    List handles = (List)factoryType2viewHandles.get(handle.getFactory().getClass());
    handles.set(handle.getSequence()-1, null);
    allHandles.remove(handle);
    // done
  }
  
  /**
   * Opens a view on a gedcom file
   * @return the view component
   */
  public ViewHandle openView(Class factory, Gedcom gedcom) {
    for (int f=0; f<factories.length; f++) {
      if (factories[f].getClass().equals(factory)) 
        return openView(gedcom, factories[f]);   	
    }
    throw new IllegalArgumentException("Unknown factory "+factory.getName());
  }
  
  /**
   * Opens a view on a gedcom file
   * @return the view component
   */
  public ViewHandle openView(Gedcom gedcom, ViewFactory factory) {
    return openView(gedcom, factory, -1);
  }
  
  /**
   * Opens a view on a gedcom file
   * @return the view component
   */
  protected ViewHandle openView(final Gedcom gedcom, ViewFactory factory, int sequence) {
    
    // figure out what sequence # this view will get
    if (sequence<0)
      sequence = getNextInSequence(factory);
    Vector handles = (Vector)factoryType2viewHandles.get(factory.getClass());
    if (handles==null) {
      handles = new Vector(10);
      factoryType2viewHandles.put(factory.getClass(), handles);
    }
    handles.setSize(Math.max(handles.size(), sequence));
    
    // already open?
    if (handles.get(sequence-1)!=null) {
      ViewHandle old = (ViewHandle)handles.get(sequence-1);
      windowManager.show(old.getKey());
      return old;
    }
    
    // get a registry 
    Registry registry = new Registry( getRegistry(gedcom), getPackage(factory)+"."+sequence) ;

    // title 
    String title = gedcom.getName()+" - "+factory.getTitle(false)+" ("+registry.getViewSuffix()+")";

    // create the view
    JComponent view = factory.createView(title, gedcom, registry, this);
    
    // create a handle for it
    final ViewHandle handle = new ViewHandle(this, gedcom, title, registry, factory, view, sequence);
    
    // wrap it into a container
    ViewContainer container = new ViewContainer(handle);

    // remember
    handles.set(handle.getSequence()-1, handle);
    allHandles.add(handle);

    // prepare to forget
    Runnable close = new Runnable() {
      public void run() {
        // let us handle close
        closeView(handle);
      }
    };
    
    // open frame
    windowManager.openFrame(handle.getKey(), title, factory.getImage(), container, null,  close);
        
    // done
    return handle;
  }
  
  /**
   * Closes all views on given Gedcom
   */
  public void closeViews(Gedcom gedcom) {
    
    // look for views looking at gedcom    
    ViewHandle[] handles = (ViewHandle[])allHandles.toArray(new ViewHandle[allHandles.size()]);
    for (int i=0;i<handles.length;i++) {
      if (handles[i].getGedcom()==gedcom) 
        closeView(handles[i]);
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
    for (Iterator handles = allHandles.iterator(); handles.hasNext(); ) {
      ViewHandle handle = (ViewHandle)handles.next();
      if (handle.getView()==view) {
        windowManager.show(handle.getKey());
        break;
      }
    }
    
    // not found    
  }

  /**
   * Returns views and factories with given support 
   */
  public Object[] getViews(Class of, Gedcom gedcom) {
    
    List result = new ArrayList(16);
    
    // loop through factories
    for (int f=0; f<factories.length; f++) {
      if (of.isAssignableFrom(factories[f].getClass())) 
        result.add(factories[f]);
    }
    // loop through views
    for (Iterator handles = allHandles.iterator(); handles.hasNext(); ) {
      ViewHandle handle = (ViewHandle)handles.next();
      if (handle.getGedcom()==gedcom && of.isAssignableFrom(handle.getView().getClass()))
        result.add(handle.getView());
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
  public JPopupMenu getContextMenu(Context context, JComponent target) {
    
    // make sure context is valid
    if (context==null)
      return null;
    
    Property property = context.getProperty();
    Entity entity = context.getEntity();
    Gedcom gedcom = context.getGedcom();

    // make sure any existing popup is cleared
    MenuSelectionManager.defaultManager().clearSelectedPath();

    // create a popup
    MenuHelper mh = new MenuHelper().setTarget(target);
    JPopupMenu popup = mh.createPopup();

    // popup local actions?
    mh.createItems(context.getActions(), false);
  
    // find ActionSupport implementors
    ActionProvider[] as = (ActionProvider[])getViews(ActionProvider.class, context.getGedcom());
    
    // items for set of entities? more specific than Entity.class for the moment!
    Entity[] entities = context.getEntities();
    if (entities.length>1 && entities.getClass().getComponentType()!=Entity.class) {
      // a sub-menu with appropriate actions
      mh.createMenu(entities.length+" "+Gedcom.getName(entities[0].getTag(), true), entities[0].getImage(false));
      for (int i = 0; i < as.length; i++) try {
        mh.createItems(as[i].createActions(entities, this), true);
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "Action Provider threw "+t.getClass()+" on createActions(Entity[])", t);
      }
      mh.popMenu();
      
    }

    // items for single property
    while (property!=null&&!(property instanceof Entity)) {

      // a sub-menu with appropriate actions
      mh.createMenu(Property.LABEL+" '"+TagPath.get(property).getName() + '\'' , property.getImage(false));
      for (int i = 0; i < as.length; i++) try {
        mh.createItems(as[i].createActions(property, this), true);
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "Action Provider "+as[i].getClass().getName()+" threw "+t.getClass()+" on createActions(Property)", t);
      }
      mh.popMenu();
      
      // recursively for parents
      property = property.getParent();
    }
        
    // items for single entity
    if (entity!=null) {
      String title = Gedcom.getName(entity.getTag(),false)+" '"+entity.getId()+'\'';
      mh.createMenu(title, entity.getImage(false));
      for (int i = 0; i < as.length; i++) try {
        mh.createItems(as[i].createActions(entity, this), true);
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "Action Provider "+as[i].getClass().getName()+" threw "+t.getClass()+" on createActions(Entity)", t);
      }
      mh.popMenu();
    }
        
    // items for gedcom
    String title = "Gedcom '"+gedcom.getName()+'\'';
    mh.createMenu(title, Gedcom.getImage());
    for (int i = 0; i < as.length; i++) try {
      mh.createItems(as[i].createActions(gedcom, this), true);
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "Action Provider "+as[i].getClass().getName()+" threw "+t.getClass()+" on createActions(Gedcom", t);
    }
    mh.popMenu();

    // done
    return popup;
  }
  
  /**
   * Our hook into keyboard and mouse operated context changes / menues
   */
  private class ContextHook extends AbstractAction implements AWTEventListener {
    
    /** constructor */
    private ContextHook() {
      AccessController.doPrivileged(new PrivilegedAction() {
        public Object run() {
          Toolkit.getDefaultToolkit().addAWTEventListener(ContextHook.this, AWTEvent.MOUSE_EVENT_MASK);
          return null;
        }
      });
    }
    
    /**
     * Resolve context provider for given 'source'
     */
    private ContextProvider getProvider(Object source) {
      // a component?
      if (!(source instanceof Component))
        return null;
      // find context provider in component hierarchy
      Component c = (Component)source;
      while (c!=null) {
        // found?
        if (c instanceof ContextProvider) {
          ContextProvider provider = (ContextProvider)c;
          if (provider.getContext()!=null)
            return provider;
        }
        // try parent
        c = c.getParent();
      }
      // not found
      return null;
    }
    
    /**
     * A Key press initiation of the context menu
     */
    public void actionPerformed(ActionEvent e) {
      // only for jcomponents with focus
      if (!(FocusManager.getCurrentManager().getFocusOwner() instanceof JComponent))
        return;
      JComponent focus = (JComponent)FocusManager.getCurrentManager().getFocusOwner();
      // look for ContextProvider and show menu if appropriate
      ContextProvider provider = getProvider(focus);
      if (provider!=null) {
        Context context = provider.getContext();
        if (context!=null) {
          JPopupMenu popup = getContextMenu(context, focus);
          if (popup!=null)
            popup.show(focus, 0, 0);
        }
      }
      // done
    }
    
    /**
     * A mouse click initiation of the context menu
     */
    public void eventDispatched(AWTEvent event) {
      
      // a mouse event?
      if ((event.getID() & AWTEvent.MOUSE_EVENT_MASK) == 0) 
        return;
      MouseEvent me = (MouseEvent) event;
      
      // find deepest component (since components without attached listeners
      // won't be the source for this event)
      final Component component  = SwingUtilities.getDeepestComponentAt(me.getComponent(), me.getX(), me.getY());
      if (component==null)
        return;
      final Point point = SwingUtilities.convertPoint(me.getComponent(), me.getX(), me.getY(), component );
      
      // gotta be a jcomponent
      if (!(component instanceof JComponent))
        return;
      JComponent jcomponent = (JComponent)component;
      
      // fake a normal click event so that popup trigger does a selection as well as non-popup trigger
      if (!me.isControlDown() && !me.isShiftDown() && me.getButton()!=MouseEvent.BUTTON1) {
        MouseListener[] ms = me.getComponent().getMouseListeners();
        MouseEvent fake = new MouseEvent(me.getComponent(), me.getID(), me.getWhen(), 0, me.getX(), me.getY(), me.getClickCount(), false, MouseEvent.BUTTON1);
        for (int m = 0; m < ms.length; m++)  {
          switch (me.getID()) {
            case MouseEvent.MOUSE_PRESSED:
              ms[m].mousePressed(fake); break;
            case MouseEvent.MOUSE_RELEASED:
              ms[m].mouseReleased(fake); break;
          }
        }
      }
      
      // we're interested in popup trigger and double-click 
      boolean isDoubleClick = me.getID()==MouseEvent.MOUSE_CLICKED&&me.getClickCount()>1;
      if(!me.isPopupTrigger()&&!isDoubleClick)  
        return;
      
      // try to identify context
      final ContextProvider provider = getProvider(component);
      if (provider==null)
        return;
      Context context = provider.getContext();
      if (context==null) 
        return;

      // proceed with popup?
      if(!me.isPopupTrigger())  {
        
        // at least double click on provider itself?
        if (isDoubleClick&&provider==me.getComponent()) {
          fireContextSelected(context, true, provider);
        }
        
        return;
      }
      
      // cancel any menu
      MenuSelectionManager.defaultManager().clearSelectedPath();
      
      // show context menu
      JPopupMenu popup = getContextMenu(context, jcomponent);
      if (popup!=null)
        popup.show(jcomponent, point.x, point.y);
      
      // done
    }
    
  } //ContextMenuHook
  

} //ViewManager
