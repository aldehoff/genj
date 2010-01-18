/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2009 Nils Meier <nils@meiers.net>
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

import genj.common.ContextListWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genj.gedcom.UnitOfWork;
import genj.io.Filter;
import genj.io.GedcomEncodingException;
import genj.io.GedcomIOException;
import genj.io.GedcomReader;
import genj.io.GedcomReaderContext;
import genj.io.GedcomReaderFactory;
import genj.io.GedcomWriter;
import genj.option.OptionProvider;
import genj.option.OptionsWidget;
import genj.util.EnvironmentChecker;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.SafeProxy;
import genj.util.ServiceLookup;
import genj.util.Trackable;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.FileChooser;
import genj.util.swing.HeapStatusWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.MenuHelper;
import genj.util.swing.ProgressWidget;
import genj.view.ActionProvider;
import genj.view.SelectionSink;
import genj.view.View;
import genj.view.ViewContext;
import genj.view.ViewFactory;
import genj.view.ActionProvider.Purpose;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import spin.Spin;
import swingx.docking.DefaultDockable;
import swingx.docking.Dockable;
import swingx.docking.Docked;
import swingx.docking.DockingPane;
import swingx.docking.dock.TabbedDock;
import swingx.docking.persistence.XMLPersister;

/**
 * The central component of the GenJ application
 */
public class Workbench extends JPanel implements SelectionSink {

  private final static Logger LOG = Logger.getLogger("genj.app");
  private final static String 
    ACC_SAVE = "ctrl S", 
    ACC_NEW = "ctrl N", 
    ACC_OPEN = "ctrl O",
    ACC_CLOSE = "ctrl W";

  
  private final static Resources RES = Resources.get(Workbench.class);
  private final static Registry REGISTRY = Registry.get(Workbench.class);

  /** members */
  private List<WorkbenchListener> listeners = new CopyOnWriteArrayList<WorkbenchListener>();
  private List<Object> plugins = new ArrayList<Object>();
  private List<ViewFactory> viewFactories = ServiceLookup.lookup(ViewFactory.class);
  private Context context = new Context();
  private DockingPane dockingPane = new WorkbenchPane();
  private Menu menu = new Menu();
  private Toolbar toolbar = new Toolbar();
  private Runnable runOnExit;
  private StatusBar statusBar = new StatusBar();
  
  /**
   * Constructor
   */
  public Workbench(Runnable onExit) {

    // Initialize data
    runOnExit = onExit;
    
    // plugins
    LOG.info("loading plugins");
    for (PluginFactory pf : ServiceLookup.lookup(PluginFactory.class)) {
      LOG.info("Loading plugin "+pf.getClass());
      Object plugin = pf.createPlugin(this);
      plugins.add(plugin);
    }
    LOG.info("/loading plugins");

    // Layout
    setLayout(new BorderLayout());
    add(toolbar, BorderLayout.NORTH);
    add(dockingPane, BorderLayout.CENTER);
    add(statusBar, BorderLayout.SOUTH);

    // restore layout
    String layout = REGISTRY.get("restore.layout", (String)null);
    if (layout!=null)
      new LayoutPersister(dockingPane, new StringReader(layout)).load();
    else
      new LayoutPersister(dockingPane, new InputStreamReader(getClass().getResourceAsStream("layout.xml"))).load();

    // hook up close view action
    new ActionCloseView();
    
    // Done
  }
  
  @Override
  public void addNotify() {
    super.addNotify();
    // install menu into frame
    DialogHelper.visitContainers(this, new DialogHelper.ComponentVisitor() {
      public Component visit(Component parent, Component child) {
        if (parent instanceof JFrame)
          ((JFrame)parent).setJMenuBar(menu);
        return null;
      }
    });
  }
  
  /**
   * current context
   * @return null or context
   */
  public Context getContext() {
    return context;
  }
  
  /**
   * create a new gedcom file
   */
  public void newGedcom() {
    
    // let user choose a file
    File file = chooseFile(RES.getString("cc.create.title"), RES.getString("cc.create.action"), null);
    if (file == null)
      return;
    if (!file.getName().endsWith(".ged"))
      file = new File(file.getAbsolutePath() + ".ged");
    if (file.exists()) {
      int rc = DialogHelper.openDialog(RES.getString("cc.create.title"), DialogHelper.WARNING_MESSAGE, RES.getString("cc.open.file_exists", file.getName()), Action2.yesNo(), Workbench.this);
      if (rc != 0)
        return;
    }
    
    // close existing
    if (!closeGedcom())
      return;
    
    // form the origin
    Gedcom gedcom;
    try {
      gedcom = new Gedcom(Origin.create(new URL("file:"+file.getAbsolutePath())));
    } catch (MalformedURLException e) {
      LOG.log(Level.WARNING, "unexpected exception creating new gedcom", e);
      return;
    }
    
    // done
    setGedcom(gedcom);
  }
  
  /**
   * asks and loads gedcom file
   */
  public boolean openGedcom() {

    // ask user
    File file = chooseFile(RES.getString("cc.open.title"), RES.getString("cc.open.action"), null);
    if (file == null)
      return false;
    REGISTRY.put("last.dir", file.getParentFile().getAbsolutePath());
    
    // close what we have
    if (!closeGedcom())
      return false;
    
    // form origin
    try {
      return openGedcom(new URL("file:"+file.getAbsolutePath()));
    } catch (Throwable t) {
      // shouldn't
      return false;
    }
    // done
  }
  
  /**
   * loads gedcom file
   */
  public boolean openGedcom(URL url) {

    // close what we have
    if (!closeGedcom())
      return false;
    
    // open connection
    final Origin origin = Origin.create(url);

    // Open Connection and get input stream
    final List<ViewContext> warnings = new ArrayList<ViewContext>();
    GedcomReader reader;
    try {

      // .. prepare our reader
      reader = (GedcomReader)Spin.off(GedcomReaderFactory.createReader(origin, (GedcomReaderContext)Spin.over(new GedcomReaderContext() {
        public String getPassword() {
          return DialogHelper.openDialog(origin.getName(), DialogHelper.QUESTION_MESSAGE, RES.getString("cc.provide_password"), "", Workbench.this);
        }
        public void handleWarning(int line, String warning, Context context) {
          warnings.add(new ViewContext(RES.getString("cc.open.warning", new Object[] { new Integer(line), warning}), context));
        }
      })));

    } catch (IOException ex) {
      String txt = RES.getString("cc.open.no_connect_to", origin) + "\n[" + ex.getMessage() + "]";
      DialogHelper.openDialog(origin.getName(), DialogHelper.ERROR_MESSAGE, txt, Action2.okOnly(), Workbench.this);
      return false;
    }
    
    try {
      for (WorkbenchListener l : listeners) l.processStarted(this, reader);
      setGedcom(reader.read());
      if (!warnings.isEmpty()) {
        dockingPane.putDockable("warnings", new GedcomDockable(
            RES.getString("cc.open.warnings", context.getGedcom().getName()), 
            Images.imgOpen,
            new ContextListWidget(context.getGedcom(), warnings))
        );
      }
    } catch (GedcomIOException ex) {
      // tell the user about it
      DialogHelper.openDialog(origin.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.open.read_error", "" + ex.getLine()) + ":\n" + ex.getMessage(), Action2.okOnly(), Workbench.this);
      // abort
      return false;
    } finally {
      for (WorkbenchListener l : listeners) l.processStopped(this, reader);
    }
    
    // remember
    List<String> history = REGISTRY.get("history", new ArrayList<String>());
    history.remove(origin.toString());
    history.add(0, origin.toString());
    if (history.size()>5)
      history.remove(history.size()-1);
    REGISTRY.put("history", history);
    
    // done
    return true;
  }
  
  private void setGedcom(Gedcom gedcom) {
    
    // catch anyone trying this without close
    if (context.getGedcom()!=null)
      throw new IllegalArgumentException("context.gedcom!=null");

    // restore context
    try {
      context = Context.fromString(gedcom, REGISTRY.get(gedcom.getName()+".context", gedcom.getName()));
    } catch (GedcomException ge) {
    } finally {
      // fixup context if necessary - start with adam if available
      Entity adam = gedcom.getFirstEntity(Gedcom.INDI);
      if (context.getEntities().isEmpty())
        context = new Context(gedcom, adam!=null ? Collections.singletonList(adam) : null, null);
    }
    
    // tell everone
    fireSelection(context, true);
    
    for (WorkbenchListener listener: listeners)
      listener.gedcomOpened(this, gedcom);
  
    // done
  }
  
  /**
   * save gedcom file
   */
  public boolean saveAsGedcom() {
    
    if (context.getGedcom() == null)
      return false;
    
    // ask everyone to commit their data
    fireCommit();
    
    // .. choose file
    SaveOptionsWidget options = new SaveOptionsWidget(context.getGedcom());
    File file = chooseFile(RES.getString("cc.save.title"), RES.getString("cc.save.action"), options);
    if (file == null)
      return false;
  
    // Need confirmation if File exists?
    if (file.exists()) {
      int rc = DialogHelper.openDialog(RES.getString("cc.save.title"), DialogHelper.WARNING_MESSAGE, RES.getString("cc.open.file_exists", file.getName()), Action2.yesNo(), Workbench.this);
      if (rc != 0) 
        return false;
    }
    
    // .. take chosen one & filters
    if (!file.getName().endsWith(".ged"))
      file = new File(file.getAbsolutePath() + ".ged");
    
    Filter[] filters = options.getFilters();
    Gedcom gedcom = context.getGedcom();
    gedcom.setPassword(options.getPassword());
    gedcom.setEncoding(options.getEncoding());
    
    // .. create new origin
    try {
      gedcom.setOrigin(Origin.create(new URL("file", "", file.getAbsolutePath())));
    } catch (Throwable t) {
      LOG.log(Level.FINER, "Failed to create origin for file "+file, t);
      return false;
    }
  
    return saveGedcomImpl(gedcom, filters);
  }
  
  /**
   * save gedcom file
   */
  public boolean saveGedcom() {

    if (context.getGedcom() == null)
      return false;
    
    // ask everyone to commit their data
    fireCommit();
    
    // do it
    return saveGedcomImpl(context.getGedcom(), new Filter[0]);
    
  }
  
  /**
   * save gedcom file
   */
  private boolean saveGedcomImpl(Gedcom gedcom, Filter[] filters) {
  
//  // .. open progress dialog
//  progress = WindowManager.openNonModalDialog(null, RES.getString("cc.save.saving", file.getName()), WindowManager.INFORMATION_MESSAGE, new ProgressWidget(gedWriter, getThread()), Action2.cancelOnly(), getTarget());

    try {
      
      // prep files and writer
      GedcomWriter writer = null;
      File file = null, temp = null;
      try {
        // .. resolve to canonical file now to make sure we're writing to the
        // file being pointed to by a symbolic link
        file = gedcom.getOrigin().getFile().getCanonicalFile();

        // .. create a temporary output
        temp = File.createTempFile("genj", ".ged", file.getParentFile());

        // .. create writer
        writer = new GedcomWriter(gedcom, new FileOutputStream(temp));
      } catch (GedcomEncodingException gee) {
        DialogHelper.openDialog(gedcom.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.save.write_encoding_error", gee.getMessage()), Action2.okOnly(), Workbench.this);
        return false;
      } catch (IOException ex) {
        DialogHelper.openDialog(gedcom.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.save.open_error", gedcom.getOrigin().getFile().getAbsolutePath()), Action2.okOnly(), Workbench.this);
        return false;
      }
      writer.setFilters(filters);

      // .. write it
      writer.write();
      
      // .. make backup
      if (file.exists()) {
        File bak = new File(file.getAbsolutePath() + "~");
        if (bak.exists())
          bak.delete();
        file.renameTo(bak);
      }

      // .. and now !finally! move from temp to result
      if (!temp.renameTo(file))
        throw new GedcomIOException("Couldn't move temporary " + temp.getName() + " to " + file.getName(), -1);

    } catch (GedcomIOException gioex) {
      DialogHelper.openDialog(gedcom.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.save.write_error", "" + gioex.getLine()) + ":\n" + gioex.getMessage(), Action2.okOnly(), Workbench.this);
      return false;
    }

//  // close progress
//  WindowManager.close(progress);
    
    // .. note changes are saved now
    if (gedcom.hasChanged())
      gedcom.doMuteUnitOfWork(new UnitOfWork() {
        public void perform(Gedcom gedcom) throws GedcomException {
          gedcom.setUnchanged();
        }
      });

    // .. done
    return true;
  }
  
  /**
   * exit workbench
   */
  public void exit() {
    
    // remember current context for exit
    if (context.getGedcom()!=null)
      REGISTRY.put("restore.url", context.getGedcom().getOrigin().toString());
    
    // close
    if (!closeGedcom())
      return;
    
    // store layout
    StringWriter layout = new StringWriter();
    new LayoutPersister(dockingPane, layout).save();
    LOG.fine("Storing layout "+layout);
    REGISTRY.put("restore.layout", layout.toString());
    
    // close all dockets
    for (Object key : dockingPane.getDockableKeys()) 
      dockingPane.removeDockable(key);
    
    // Shutdown
    runOnExit.run();
  }
  
  /**
   * closes gedcom file
   */
  public boolean closeGedcom() {

    // noop?
    if (context.getGedcom()==null)
      return true;
    
    // commit changes
    fireCommit();
    
    // changes?
    if (context.getGedcom().hasChanged()) {
      
      // close file officially
      int rc = DialogHelper.openDialog(null, DialogHelper.WARNING_MESSAGE, RES.getString("cc.savechanges?", context.getGedcom().getName()), Action2.yesNoCancel(), Workbench.this);
      // cancel - we're done
      if (rc == 2)
        return false;
      // yes - close'n save it
      if (rc == 0) 
        if (!saveGedcom())
          return false;

    }
    
    // tell 
    for (WorkbenchListener listener: listeners)
      listener.gedcomClosed(this, context.getGedcom());
    
    // remember context
    REGISTRY.put(context.getGedcom().getName(), context.toString());

    // remember and tell
    context = new Context();
    for (WorkbenchListener listener : listeners) 
      listener.selectionChanged(this, context, true);
    
    // done
    return true;
  }
  
  /**
   * Restores last loaded gedcom file
   */
  public void restoreGedcom() {

    String restore = REGISTRY.get("restore.url", (String)null);
    try {
      // no known key means load default
      if (restore==null)
        restore = new File("gedcom/example.ged").toURI().toURL().toString();
      // known key needs value
      if (restore.length()>0)
        openGedcom(new URL(restore));
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "unexpected error", t);
    }
  }
  
  /**
   * Lookup providers
   */
  @SuppressWarnings("unchecked")
  /*package*/ <T> List<T> lookup(Class<T> type) {
    
    List<T> result = new ArrayList<T>();
    
    // check all dock'd components
    for (Object key : dockingPane.getDockableKeys()) {
      Dockable dockable = dockingPane.getDockable(key);
      if (dockable instanceof DefaultDockable) {
        DefaultDockable vd = (DefaultDockable)dockable;
        if (type.isAssignableFrom(vd.getContent().getClass()))
          result.add((T)vd.getContent());
      }
    }
    
    // check all plugins
    for (Object plugin : plugins) {
      if (type.isAssignableFrom(plugin.getClass()))
        result.add((T)plugin);
    }
    
    // sort by priority
    Collections.sort(result, new Comparator<T>() {
      public int compare(T a1, T a2) {
        Priority P1 = a1.getClass().getAnnotation(Priority.class);
        Priority P2 = a2.getClass().getAnnotation(Priority.class);
        int p1 = P1!=null ? P1.priority() : Priority.NORMAL;
        int p2 = P2!=null ? P2.priority() : Priority.NORMAL;
        return p2 - p1;
      }
    });

    // harden and done
    return SafeProxy.harden(result, LOG);
  }
  
  public void fireCommit() {
    for (WorkbenchListener listener : listeners)
      listener.commitRequested(this);
  }
  
  public void fireSelection(Context context, boolean isActionPerformed) {
    
    // appropriate?
    if (context.getGedcom()!= this.context.getGedcom()) {
      LOG.log(Level.FINER, "context selection on unknown gedcom", new Throwable());
      return;
    }
    
    // already known?
    if (!isActionPerformed && this.context.equals(context))
      return;
    
    LOG.fine("fireSelection("+context+","+isActionPerformed+")");
    
    // remember 
    this.context = context;
    
    if (context.getGedcom()!=null) 
      REGISTRY.put(context.getGedcom().getName()+".context", context.toString());
    
    // notify
    for (WorkbenchListener listener : listeners) 
      listener.selectionChanged(this, context, isActionPerformed);
    
  } 
  
  private void connect(List<Action> actions) {
    for (Action action : actions) {
      if (context.getGedcom()!=null && action instanceof GedcomListener)
        context.getGedcom().addGedcomListener((GedcomListener)Spin.over(action));
      if (action instanceof WorkbenchListener)
        addWorkbenchListener((WorkbenchListener)action);
    }
  }
  
  private void disconnect(List<Action> actions) {
    for (Action action : actions) {
      if (context.getGedcom()!=null && action instanceof GedcomListener)
        context.getGedcom().removeGedcomListener((GedcomListener)Spin.over(action));
      if (action instanceof WorkbenchListener)
        removeWorkbenchListener((WorkbenchListener)action);
    }
  }
  
  private void fireViewOpened(View view) {
    // tell 
    for (WorkbenchListener listener : listeners)
      listener.viewOpened(this, view);
  }

  private void fireViewRestored(View view) {
    // tell 
    for (WorkbenchListener listener : listeners)
      listener.viewRestored(this, view);
  }
  
  private void fireViewClosed(View view) {
    // tell plugins
    for (WorkbenchListener listener : listeners)
      listener.viewClosed(this, view);
  }
  
  public void addWorkbenchListener(WorkbenchListener listener) {
    listeners.add(SafeProxy.harden(listener));
  }

  public void removeWorkbenchListener(WorkbenchListener listener) {
    listeners.remove(SafeProxy.harden(listener));
  }

  /**
   * access a view
   * @return view or null if not open
   */
  public View getView(Class<? extends ViewFactory> factoryClass) {
    ViewDockable dockable = (ViewDockable)dockingPane.getDockable(factoryClass);
    return dockable!=null ? dockable.getView() : null;
  }
  
  /**
   * close a view
   */
  public void closeView(Class<? extends ViewFactory> factory) {
    
    View view = getView(factory);
    if (view==null)
      return;
    
    dockingPane.removeDockable(factory);

    // tell others
    fireViewClosed(view);

  }
  
  /**
   * (re)open a view
   */
  public View openView(Class<? extends ViewFactory> factory) {
    return openView(factory, context);
  }
  
  /**
   * (re)open a view
   */
  public View openView(Class<? extends ViewFactory> factory, Context context) {
    for (ViewFactory vf : viewFactories) {
      if (vf.getClass().equals(factory))
        return openViewImpl(vf, context);
    }
    throw new IllegalArgumentException("unknown factory");
  }
  
  private View openViewImpl(ViewFactory factory, Context context) {
    
    // already open or new
    ViewDockable dockable = (ViewDockable)dockingPane.getDockable(factory.getClass());
    if (dockable != null) {
      // bring forward
      dockingPane.putDockable(factory.getClass(), dockable);
      // done
      return dockable.getView();
    }
    
    // open it & signal current selection
    try {
      dockable = new ViewDockable(Workbench.this, factory);
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "cannot open view for "+factory.getClass().getName(), t);
      return null;
    }
    dockingPane.putDockable(factory.getClass(), dockable);

    // tell others
    fireViewOpened(dockable.getView());

    return dockable.getView();
  }

  /**
   * Let the user choose a file
   */
  private File chooseFile(String title, String action, JComponent accessory) {
    FileChooser chooser = new FileChooser(Workbench.this, title, action, "ged", EnvironmentChecker.getProperty(Workbench.this, new String[] { "genj.gedcom.dir", "user.home" }, ".", "choose gedcom file"));
    chooser.setCurrentDirectory(new File(REGISTRY.get("last.dir", "user.home")));
    if (accessory != null)
      chooser.setAccessory(accessory);
    if (JFileChooser.APPROVE_OPTION != chooser.showDialog())
      return null;
    // check the selection
    File file = chooser.getSelectedFile();
    if (file == null)
      return null;
    // remember last directory
    REGISTRY.put("last.dir", file.getParentFile().getAbsolutePath());
    // done
    return file;
  }
  
  /**
   * Action - a workbench action
   */
  private class WorkbenchAction extends Action2 implements WorkbenchListener {
    
    public void commitRequested(Workbench workbench) {
    }

    public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
    }

    public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
    }

    public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
    }
    
    public void viewClosed(Workbench workbench, View view) {
    }
    
    public void viewRestored(Workbench workbench, View view) {
    }

    public void viewOpened(Workbench workbench, View view) {
    }

    public boolean workbenchClosing(Workbench workbench) {
      return true;
    }
    
    public void processStarted(Workbench workbench, Trackable process) {
      setEnabled(false);
    }

    public void processStopped(Workbench workbench, Trackable process) {
      setEnabled(true);
    }
  }

  /**
   * Action - about
   */
  private class ActionAbout extends Action2 {
    /** constructor */
    protected ActionAbout() {
      setText(RES, "cc.menu.about");
      setImage(Images.imgAbout);
    }

    /** run */
    public void actionPerformed(ActionEvent event) {
      DialogHelper.openDialog(RES.getString("cc.menu.about"), DialogHelper.INFORMATION_MESSAGE, new AboutWidget(), Action2.okOnly(), Workbench.this);
      // done
    }
  } // ActionAbout

  /**
   * Action - exit
   */
  private class ActionExit extends WorkbenchAction {
    
    /** constructor */
    protected ActionExit() {
      setText(RES, "cc.menu.exit");
      setImage(Images.imgExit);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      exit();
    }
  }
  
  /**
   * Action - close and exit
   */
  private class ActionClose extends WorkbenchAction {
    
    /** constructor */
    protected ActionClose() {
      setText(RES, "cc.menu.close");
      setImage(Images.imgClose);
    }
    
    /** run */
    public void actionPerformed(ActionEvent event) {
      closeGedcom();
    }
  } // ActionExit

  /**
   * Action - new
   */
  private class ActionNew extends WorkbenchAction {

    /** constructor */
    ActionNew() {
      setText(RES, "cc.menu.new");
      setTip(RES, "cc.tip.create_file");
      setImage(Images.imgNew);
      install(Workbench.this, ACC_NEW, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** execute callback */
    public void actionPerformed(ActionEvent event) {
      newGedcom();
    }

  } // ActionNew

  /**
   * Action - open
   */
  private class ActionOpen extends WorkbenchAction {
    
    private URL url;

    /** constructor - good for button or menu item */
    protected ActionOpen() {
      setTip(RES, "cc.tip.open_file");
      setText(RES, "cc.menu.open");
      setImage(Images.imgOpen);
      install(Workbench.this, ACC_OPEN, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    protected ActionOpen(int m, URL url) {
      this.url = url;
      String txt = ((char)('1'+m)) + " " + url.getFile();
      
      int i = txt.indexOf('/');
      int j = txt.lastIndexOf('/');
      if (i!=j) 
        txt = txt.substring(0, i + 1) + "..." + txt.substring(j);
      setMnemonic(txt.charAt(0));
      setText(txt);
    }

    public void actionPerformed(ActionEvent event) {
      if (url!=null)
        openGedcom(url);
      else
        openGedcom();
    }
  } // ActionOpen

  /**
   * Action - Save
   */
  private class ActionSave extends WorkbenchAction {
    /** whether to ask user */
    private boolean saveAs;
    /** gedcom */
    protected Gedcom gedcomBeingSaved;
    /** writer */
    private GedcomWriter gedWriter;
    /** origin to load after successfull save */
    private Origin newOrigin;
    /** filters we're using */
    private Filter[] filters;
    /** progress key */
    private String progress;
    /** exception we might encounter */
    private GedcomIOException ioex = null;
    /** temporary and target file */
    private File temp, file;
    /** password used */
    private String password;

    /**
     * Constructor for saving gedcom 
     */
    protected ActionSave(boolean saveAs) {
      // remember
      this.saveAs = saveAs;
      // text
      if (saveAs)
        setText(RES.getString("cc.menu.saveas"));
      else {
        setText(RES.getString("cc.menu.save"));
        
        install(Workbench.this, ACC_SAVE, JComponent.WHEN_IN_FOCUSED_WINDOW);
      }
      setTip(RES, "cc.tip.save_file");
      // setup
      setImage(Images.imgSave);
      setEnabled(context.getGedcom()!=null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (saveAs)
        saveAsGedcom();
      else
        saveGedcom();
    }

  } // ActionSave

  /**
   * Action - Close View (for keyboard binding only)
   *
   */
  private class ActionCloseView extends Action2 {
    public ActionCloseView() {
      install(Workbench.this, ACC_CLOSE, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      DialogHelper.visitContainers(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), new DialogHelper.ComponentVisitor() {
        public Component visit(Component parent, Component child) {
          // check any tabbed pane we can find for possibly contained views
          if (child instanceof TabbedDock) {
            ((ViewDockable)((TabbedDock)child).getSelectedDockable()).close();
            return child;
          }
          // encountered view?
          if (child instanceof View) {
            ViewDockable.getDockable((View)child).close();
            return child;
          }
          return null;
        }
      });

    }
  }
  
  /**
   * Action - Open View
   */
  private class ActionOpenView extends Action2 {
    /** which ViewFactory */
    private ViewFactory factory;

    /** constructor */
    protected ActionOpenView(ViewFactory vw) {
      factory = vw;
      setText(factory.getTitle());
      setTip(RES.getString("cc.tip.open_view", factory.getTitle()));
      setImage(factory.getImage());
    }

    /** run */
    public void actionPerformed(ActionEvent event) {
      openViewImpl(factory, context);
    }
  } // ActionOpenView

  /**
   * Action - Options
   */
  private class ActionOptions extends Action2 {
    /** constructor */
    protected ActionOptions() {
      setText(RES.getString("cc.menu.options"));
      setImage(OptionsWidget.IMAGE);
    }

    /** run */
    public void actionPerformed(ActionEvent event) {
      // create widget for options
      OptionsWidget widget = new OptionsWidget(getText());
      widget.setOptions(OptionProvider.getAllOptions());
      // open dialog
      DialogHelper.openDialog(getText(), DialogHelper.INFORMATION_MESSAGE, widget, Action2.okOnly(), Workbench.this);
      // done
    }
  } // ActionOptions

  /**
   * a little status tracker
   */
  private class StatusBar extends JPanel implements GedcomMetaListener, WorkbenchListener {

    private int commits;

    private JLabel[] label = new JLabel[Gedcom.ENTITIES.length];
    private JLabel changes = new JLabel("", SwingConstants.RIGHT);
    private HeapStatusWidget heap = new HeapStatusWidget();
    
    StatusBar() {

      super(new BorderLayout());

      JPanel panel = new JPanel();
      for (int i = 0; i < Gedcom.ENTITIES.length; i++) {
        label[i] = new JLabel("0", Gedcom.getEntityImage(Gedcom.ENTITIES[i]), SwingConstants.LEFT);
        panel.add(label[i]);
      }
      add(panel, BorderLayout.WEST);
      add(changes, BorderLayout.CENTER);
      add(heap, BorderLayout.EAST);

      addWorkbenchListener(this);
    }

    public void processStarted(Workbench workbench, Trackable process) {
      remove(2);
      add(new ProgressWidget(process),BorderLayout.EAST);
    }

    public void processStopped(Workbench workbench, Trackable process) {
      remove(2);
      add(heap,BorderLayout.EAST);
    }
    
    private void update(Gedcom gedcom) {
      
      for (int i=0;i<Gedcom.ENTITIES.length;i++)  {
        String tag = Gedcom.ENTITIES[i];
        int es = gedcom.getEntities(tag).size();
        int ps = gedcom.getPropertyCount(tag);
        if (ps==0) {
          label[i].setText(Integer.toString(es));
          label[i].setToolTipText(Gedcom.getName(tag, true));
        } else {
          label[i].setText(es + "/" + ps);
          label[i].setToolTipText(Gedcom.getName(tag, true)+" ("+RES.getString("cc.tip.record_inline")+")");
        }
      }
      
      changes.setText(commits>0?RES.getString("stat.commits", new Integer(commits)):"");
    }
    
    public void gedcomWriteLockReleased(Gedcom gedcom) {
      commits++;
      update(gedcom);
    }

    public void gedcomHeaderChanged(Gedcom gedcom) {
    }

    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    }

    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    }

    public void gedcomWriteLockAcquired(Gedcom gedcom) {
    }

    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property prop) {
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
    }

    public void commitRequested(Workbench workbench) {
    }

    public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
      gedcom.removeGedcomListener((GedcomListener)Spin.over(this));
      commits = 0;
      for (int i=0;i<Gedcom.ENTITIES.length;i++) 
        label[i].setText("-");
      changes.setText("");
    }


    public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
      gedcom.addGedcomListener((GedcomListener)Spin.over(this));
      update(gedcom);
    }

    public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
    }

    public void viewClosed(Workbench workbench, View view) {
    }
    
    public void viewRestored(Workbench workbench, View view) {
    }

    public void viewOpened(Workbench workbench, View view) {
    }

    public boolean workbenchClosing(Workbench workbench) {
      return true;
    }

  } // Stats

  /**
   * Our MenuBar
   */
  private class Menu extends JMenuBar implements SelectionSink, WorkbenchListener {
    
    private List<Action> actions = new ArrayList<Action>();
    
    // we need to play delegate for selectionsink since the menu is not a child 
    // of Workbench but the window's root-pane - selections bubbling up the
    // window hierarchy are otherwise running into null-ness
    public void fireSelection(Context context, boolean isActionPerformed) {
      Workbench.this.fireSelection(context, isActionPerformed);
    }
    
    private Menu() {
      
      addWorkbenchListener(this);
      setup();
      
    }
    
    private void setup() {

      // remove old
      disconnect(actions);
      actions.clear();
      removeAll();
      revalidate();
      repaint();

      Action2.Group groups = new Action2.Group("ignore");
      
      // File
      Action2.Group file = new ActionProvider.FileActionGroup();
      groups.add(file);
      file.add(new ActionNew());
      file.add(new ActionOpen());
      file.add(new ActionSave(false));
      file.add(new ActionSave(true));
      file.add(new ActionClose());
      file.add(new ActionProvider.SeparatorAction());
      int i=0; for (String recent : REGISTRY.get("history", new ArrayList<String>())) try {
        if (context.getGedcom()==null||!recent.equals(context.getGedcom().getOrigin().toString()))
          file.add(new ActionOpen(i++, new URL(recent)));
      } catch (MalformedURLException e) { }
      file.add(new ActionProvider.SeparatorAction());
      if (!EnvironmentChecker.isMac())   // Mac's don't need exit actions in
        file.add(new ActionExit()); // application menus apparently
      
      // Edit
      groups.add(new ActionProvider.EditActionGroup());
      
      // Views
      Action2.Group views = new ActionProvider.ViewActionGroup();
      groups.add(views);
      for (ViewFactory factory : viewFactories) 
        views.add(new ActionOpenView(factory));

      // merge providers' actions
      if (context.getGedcom()!=null) {
        for (ActionProvider provider : lookup(ActionProvider.class)) {
          for (Action2 action : provider.createActions(context, Purpose.MENU)) {
            if (action instanceof Action2.Group) {
              groups.add(action);
            } else {
              LOG.warning("ActionProvider "+provider+" returned a non-group for menu");
            }
          }
        }
      }
      
      Action2.Group edit = new ActionProvider.EditActionGroup();
      edit.add(new ActionProvider.SeparatorAction());
      edit.add(new ActionOptions());
      groups.add(edit);

      Action2.Group help = new ActionProvider.HelpActionGroup();
      help.add(new ActionAbout());
      groups.add(help);

      // Build menu
      MenuHelper mh = new MenuHelper().pushMenu(this);
      for (Action2 group : groups) {
        mh.createMenu((Action2.Group)group);
        mh.popMenu();
      }
      
      // remember actions
      actions.addAll(mh.getActions());
      
      // connect
      connect(actions);
      
      // Done
    }
    
    // 20060209 don't use a glue component to move help all the way over to the right
    // (Reminder: according to Stephane this doesn't work on MacOS Tiger)
    // java.lang.ArrayIndexOutOfBoundsException: 3 > 2::
    // at java.util.Vector.insertElementAt(Vector.java:557)::
    // at apple.laf.ScreenMenuBar.add(ScreenMenuBar.java:266)::
    // at apple.laf.ScreenMenuBar.addSubmenu(ScreenMenuBar.java:207)::
    // at apple.laf.ScreenMenuBar.addNotify(ScreenMenuBar.java:53)::
    // at java.awt.Frame.addNotify(Frame.java:478)::
    // at java.awt.Window.pack(Window.java:436)::
    // http://lists.apple.com/archives/java-dev/2005/Aug/msg00060.html
    
    public void commitRequested(Workbench workbench) {
    }

    public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
      setup();
    }

    public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
      setup();
    }

    public void processStarted(Workbench workbench, Trackable process) {
    }

    public void processStopped(Workbench workbench, Trackable process) {
    }

    public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
      setup();
    }

    public void viewClosed(Workbench workbench, View view) {
    }
    
    public void viewRestored(Workbench workbench, View view) {
    }

    public void viewOpened(Workbench workbench, View view) {
    }

    public boolean workbenchClosing(Workbench workbench) {
      return true;
    }
    
  } // Menu

  /**
   * our toolbar
   */
  private class Toolbar extends JToolBar implements WorkbenchListener {

    private List<Action> actions = new ArrayList<Action>();
    
    /**
     * Constructor
     */
    private Toolbar() {
      setFloatable(false);
      addWorkbenchListener(this);
      setup();
    }
    
    private void setup() {
      
      // cleanup
      disconnect(actions);
      actions.clear();
      removeAll();
        
      // defaults
      add(new ActionNew());
      add(new ActionOpen());
      add(new ActionSave(false));
      
      
      // let providers speak
      if (context.getGedcom()!=null) {
        addSeparator();
        for (ActionProvider provider : lookup(ActionProvider.class)) {
          for (Action2 action : provider.createActions(context, Purpose.TOOLBAR)) {
            if (action instanceof Action2.Group)
              LOG.warning("ActionProvider "+provider+" returned a group for toolbar");
            else {
              if (action instanceof ActionProvider.SeparatorAction)
                toolbar.addSeparator();
              else {
                add(action);
              }
            }
          }
        }
      }
      
      // connect actions
      connect(actions);
      
      // done
    }
    
    @Override
    public JButton add(Action action) {
      // remember
      actions.add(action);
      // no mnemonic (e.g. alt-o triggering Open action), no text
      action.putValue(Action.MNEMONIC_KEY, null);
      action.putValue(Action.NAME, null);
      // non focus button
      JButton button = new JButton(action);
      button.setFocusable(false);
      super.add(button);
      return button;
    }

    public void commitRequested(Workbench workbench) {
    }

    public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
      setup();
    }

    public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
      setup();
    }

    public void processStarted(Workbench workbench, Trackable process) {
    }

    public void processStopped(Workbench workbench, Trackable process) {
    }

    public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
      setup();
    }

    public void viewClosed(Workbench workbench, View view) {
    }
    
    public void viewRestored(Workbench workbench, View view) {
    }

    public void viewOpened(Workbench workbench, View view) {
    }

    public boolean workbenchClosing(Workbench workbench) {
      return true;
    }
  }

  /**
   * layout persist/restore
   */
  private class LayoutPersister extends XMLPersister {
    
    private List<ViewDockable> dockables = new ArrayList<ViewDockable>();
    
    LayoutPersister(DockingPane dockingPane, Reader layout) {
      super(dockingPane, layout, "1");
    }
    
    LayoutPersister(DockingPane dockingPane, Writer layout) {
      super(dockingPane, layout, "1");
    }
    
    @Override
    protected Object parseKey(String key) throws SAXParseException {
      // a view dockable key?
      try {
        return Class.forName(key);
      } catch (ClassNotFoundException e) {
      }
      // a string key then
      return key;
    }
    
    @Override
    protected Dockable resolveDockable(Object key) {
      for (ViewFactory vf : viewFactories) {
        if (vf.getClass().equals(key)) {
          ViewDockable vd = new ViewDockable(Workbench.this, vf);
          dockables.add(vd);
          return vd;
        }
      }
      LOG.finer("can't find view factory for docking key"+key);
      return null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected String formatKey(Object key) throws SAXException {
      if (key instanceof String)
        return (String)key;
      return ((Class<? extends ViewFactory>)key).getName();
    }
    
    @Override
    public void load() {
      try {
        super.load();
      } catch (Exception ex) {
        LOG.log(Level.WARNING, "unable to load layout", ex);
      }
      
      // tell others now
      for (ViewDockable vd : dockables)
        fireViewRestored(vd.getView());
  
    }
  
    @Override
    public void save() {
      try {
        super.save();
      } catch (Exception ex) {
        LOG.log(Level.WARNING, "unable to save layout", ex);
      }
    }
  }
  
  /**
   * A dockable for showing gedcom related info - no view yet, something we want to close if gedcom goes away
   */
  private class GedcomDockable extends DefaultDockable implements WorkbenchListener {
    
    private GedcomDockable(String title, ImageIcon img, JComponent content) {
      setContent(content);
      setTitle(title);
      setIcon(img);
    }
    
    @Override
    public void docked(Docked docked) {
      super.docked(docked);
      addWorkbenchListener(this);
    }
    
    @Override
    public void undocked() {
      super.undocked();
      removeWorkbenchListener(this);
    }

    public void commitRequested(Workbench workbench) {
    }

    public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
      for (Object key : dockingPane.getDockableKeys())
        if (dockingPane.getDockable(key)==this)
          dockingPane.putDockable(key, null);
      return;
    }

    public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
    }

    public void processStarted(Workbench workbench, Trackable process) {
    }

    public void processStopped(Workbench workbench, Trackable process) {
    }

    public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
    }

    public void viewClosed(Workbench workbench, View view) {
    }
    
    public void viewRestored(Workbench workbench, View view) {
    }

    public void viewOpened(Workbench workbench, View view) {
    }

    public boolean workbenchClosing(Workbench workbench) {
      return true;
    }
  }
  
  private class WorkbenchPane extends DockingPane implements WorkbenchListener {
    
    private List<JDialog> dialogs = new ArrayList<JDialog>();
    
    public WorkbenchPane() {
      addWorkbenchListener(this);
    }
    
    private void updateTitle(JDialog dlg, String title) {
      dlg.setTitle(title);
    }
    
    private void updateTitles(String title) {
      for (JDialog dlg : dialogs) 
        updateTitle(dlg, title);
    }
    
    @Override
    protected JDialog createDialog() {
      JDialog dialog = super.createDialog();
      dialog.setUndecorated(true);
      if (context.getGedcom()!=null);
        updateTitle(dialog, context.getGedcom()!=null ? context.getGedcom().getName() : "");
      return dialog;
    }
    
    @Override
    protected void dismissDialog(JDialog dialog) {
      super.dismissDialog(dialog);
      dialogs.remove(dialog);
    }

    public void commitRequested(Workbench workbench) {
    }

    public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
      updateTitles("");
    }

    public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
      updateTitles(gedcom.getName());
    }

    public void processStarted(Workbench workbench, Trackable process) {
    }

    public void processStopped(Workbench workbench, Trackable process) {
    }

    public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
    }

    public void viewClosed(Workbench workbench, View view) {
    }

    public void viewOpened(Workbench workbench, View view) {
    }

    public void viewRestored(Workbench workbench, View view) {
    }

    public boolean workbenchClosing(Workbench workbench) {
      return true;
    }
  }

} // ControlCenter
