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

import genj.gedcom.Gedcom;
import genj.io.Filter;
import genj.io.GedcomEncryptionException;
import genj.io.GedcomIOException;
import genj.io.GedcomReader;
import genj.io.GedcomWriter;
import genj.option.OptionProvider;
import genj.option.OptionsWidget;
import genj.print.PrintManager;
import genj.util.ActionDelegate;
import genj.util.EnvironmentChecker;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.FileChooser;
import genj.util.swing.MenuHelper;
import genj.util.swing.ProgressWidget;
import genj.view.ContextListener;
import genj.view.ContextSelectionEvent;
import genj.view.ViewFactory;
import genj.view.ViewManager;
import genj.window.CloseWindow;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * The central component of the GenJ application
 */
public class ControlCenter extends JPanel {

  /** members */
  private JMenuBar menuBar; 
  private GedcomTableWidget tGedcoms;
  private Registry registry;
  private Vector gedcomButtons = new Vector();
  private Vector tniButtons = new Vector();
  private Resources resources = Resources.get(this);
  private WindowManager windowManager;
  private ViewManager viewManager;
  private PrintManager printManager;
    
  /**
   * Constructor
   */
  public ControlCenter(Registry setRegistry, WindowManager winManager) {

    // Initialize data
    registry = new Registry(setRegistry, "cc");
    windowManager = winManager;
    printManager = new PrintManager(windowManager);
    viewManager = new ViewManager(new Registry(setRegistry, "views"), printManager, windowManager);
    
    // Table of Gedcoms
    tGedcoms = new GedcomTableWidget(viewManager, registry, new ActionSave(false), new ActionClose());
    
    // ... Listening
    tGedcoms.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        ButtonHelper.setEnabled(gedcomButtons, tGedcoms.getSelectedGedcom() != null);
      }
    });
    
    viewManager.addContextListener(new ContextListener() {
      public void handleContextSelectionEvent(ContextSelectionEvent event) {
        tGedcoms.setSelection(event.getContext().getGedcom());
      }
    });
    
    // Layout
    setLayout(new BorderLayout());
    add(createToolBar(), "North");
    add(new JScrollPane(tGedcoms), "Center");

    // Init menu bar at this point (so it's ready when the first file is loaded)
    menuBar = createMenuBar();

    // Done
  }
  
  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    // continue
    super.addNotify();
    // Load known gedcoms
    SwingUtilities.invokeLater(new ActionLoadLastOpen());
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(280,180);
  }

  /**
   * Adds another Gedcom to the list of Gedcoms
   */
  /*package*/ void addGedcom(Gedcom gedcom) {
    tGedcoms.addGedcom(gedcom);
  }

  /**
   * Removes a Gedcom from the list of Gedcoms
   */
  /*package*/ void removeGedcom(Gedcom gedcom) {
    
    // close views
    viewManager.closeViews(gedcom);

    // forget about it
    tGedcoms.removeGedcom(gedcom);
  }
  
  /**
   * Exit action
   */
  /*package*/ ActionDelegate getExitAction() {
    return new ActionExit().setTarget(this);
  }
  
  /**
   * Returns a menu for frame showing this controlcenter
   */
  /*package*/ JMenuBar getMenuBar() {
    return menuBar;
  }
  
  /**
   * Returns a button bar for the top
   */
  private JToolBar createToolBar() {

    // create it
    JToolBar result = new JToolBar();

    // wether we're showing text on the buttons, too    
    boolean imageAndText = registry.get("imagesandtext", false);

    // .. Buttons
    ButtonHelper bh =
      new ButtonHelper()
        .setResources(resources)
        .setInsets(4)
        .setFocusable(false)
        .setContainer(result)
        .setTextAllowed(imageAndText)
        .setShortTexts(true)
        .setImageOverText(true)
        .setFontSize(10)
        .addCollection(tniButtons);

    bh.setEnabled(true).create(new ActionOpen());

    bh.setEnabled(false).addCollection(gedcomButtons).setResources(null);
    ViewFactory[] factories = viewManager.getFactories();
    for (int i = 0; i < factories.length; i++) {
      bh.create(new ActionView(-1, factories[i]));
    }
    bh.setEnabled(true).removeCollection(gedcomButtons).setResources(resources);

    result.add(Box.createGlue());

    // Menu
    MenuHelper mh = new MenuHelper().setResources(resources);
    mh.createPopup(result);
    mh.createItem(new ActionToggleTnI());

    // done
    return result;
  }
  
  /**
   * Creates our MenuBar
   */
  private JMenuBar createMenuBar() {

    MenuHelper mh = new MenuHelper().setResources(resources);

    JMenuBar result = mh.createBar();

    // Create Menues
    mh.createMenu("cc.menu.file");

    mh.createItem(new ActionOpen());
    mh.createSeparator().setEnabled(false).setCollection(gedcomButtons);
    mh.createItem(new ActionSave(false));
    mh.createItem(new ActionSave(true));
    mh.createItem(new ActionClose());
    mh.createSeparator().setEnabled(true).setCollection(null);
    mh.createItem(new ActionExit());

    mh.popMenu().createMenu("cc.menu.view");

    mh.setEnabled(false).setCollection(gedcomButtons).setResources(null);
    ViewFactory[] factories = viewManager.getFactories();
    for (int i = 0; i < factories.length; i++)
      mh.createItem(new ActionView(i+1, factories[i]));
    mh.setEnabled(true).setCollection(null).setResources(resources);
    mh.createSeparator();
    mh.createItem(new ActionOptions());

    result.add(Box.createHorizontalGlue());

    mh.popMenu().setEnabled(true).createMenu("cc.menu.help");

    mh.createItem(new ActionHelp());
    mh.createItem(new ActionAbout());

    // Done
    return result;
  }
  
  /**
   * Let the user choose a file
   */
  private File chooseFile(String title, String action, JComponent accessory) {
    FileChooser chooser = new FileChooser(
      ControlCenter.this, title, action, "ged",
      EnvironmentChecker.getProperty(ControlCenter.this, new String[] { "genj.gedcom.dir", "user.home" } , ".", "choose gedcom file")
    );
    chooser.setCurrentDirectory(new File(registry.get("last.dir", "user.home")));
    if (accessory!=null) chooser.setAccessory(accessory);
    if (JFileChooser.APPROVE_OPTION!=chooser.showDialog())
      return null;
    // check the selection
    File file = chooser.getSelectedFile();
    if (file == null)
      return null;
    // remember last directory
    registry.put("last.dir", file.getParentFile().getAbsolutePath());
    // done
    return file;
  }

  /**
   * Action - toggle text&images
   */
  private class ActionToggleTnI extends ActionDelegate {
    /** constructor */
    protected ActionToggleTnI() {
      setText("cc.menu.tni");
    }
    /** run */
    protected void execute() {
      boolean set = !registry.get("imagesandtext", false);
      registry.put("imagesandtext", set);
      ButtonHelper.setTextAllowed(tniButtons, set);
    }
  }

  /**
   * Action - about
   */
  private class ActionAbout extends ActionDelegate {
    /** constructor */
    protected ActionAbout() {
      setText("cc.menu.about");
      setImage(Images.imgAbout);
    }
    /** run */
    protected void execute() {
      if (windowManager.show("about"))
        return;
      windowManager.openFrame(
        "about",
        resources.getString("cc.menu.about"),
        Gedcom.getImage(),
        new AboutWidget(viewManager),
        resources.getString("cc.menu.close")
      );
      // done      
    }
  } //ActionAbout

  /**
   * Action - help
   */
  private class ActionHelp extends ActionDelegate {
    /** constructor */
    protected ActionHelp() {
      setText("cc.menu.contents");
      setImage(Images.imgHelp);
    }
    /** run */
    protected void execute() {
      if (windowManager.show("help"))
        return;
      windowManager.openFrame(
        "help",
        resources.getString("cc.menu.help"),
        Images.imgHelp,
        new HelpWidget(),
        resources.getString("cc.menu.close")
      );
      // done
    }
  } //ActionHelp

  /**
   * Action - exit
   */
  private class ActionExit extends ActionDelegate {
    /** constructor */
    protected ActionExit() {
      setText("cc.menu.exit");
      setImage(Images.imgExit);
    }
    /** run */
    protected void execute() {
      // Remember open gedcoms
      Collection save = new ArrayList();
      for (Iterator gedcoms=tGedcoms.getAllGedcoms().iterator(); gedcoms.hasNext(); ) {
        // next gedcom
        Gedcom gedcom = (Gedcom) gedcoms.next();
        // changes need saving?
        if (gedcom.hasUnsavedChanges()) {
          // close file officially
          int rc = windowManager.openDialog(
              "confirm-exit", null, WindowManager.IMG_WARNING, 
              resources.getString("cc.savechanges?", gedcom.getName()), 
              CloseWindow.YESandNOandCancel(), ControlCenter.this
            );
          // cancel - we're done
          if (rc==2) return;
          // yes - close'n save it
          if (rc==0) {
            removeGedcom(gedcom);
            new ActionSave(gedcom) {
              // after the save
              protected void postExecute() {
                // super first
                super.postExecute();
                // add gedcom again we removed temporarily
                addGedcom(gedcom);
                // stop still unsaved changes that didn't make it through saving
                if (gedcom.hasUnsavedChanges()) 
                  return;
                // continue with exit
                getExitAction().trigger();
              }
            }.trigger();
            return;
          }
          // no - skip it
        }
        // remember as being open
        save.add(gedcom.hasPassword() ? gedcom.getOrigin() + "," + gedcom.getPassword() : gedcom.getOrigin().toString());
      }
      registry.put("open", save);

      // Close all Windows
      windowManager.closeAll();
      
      // Shutdown - wanted to do without but SingUtilities creates
      // a hidden frame that sticks around (for null-parent dialogs)
      // preventing the event dispatcher thread from shutting down
      System.exit(0);

      // Done
    }
  } //ActionExit

  /**
   * Action - open
   */
  private class ActionOpen extends ActionDelegate {

    /** a preset origin we're reading from */
    private Origin origin;

    /** a reader we're working on */
    private GedcomReader reader;

    /** an error we might encounter */
    private GedcomIOException exception;

    /** a gedcom we're creating */
    private Gedcom gedcom;
    
    /** key of progress dialog */
    private String progress;
    
    /** password in use */
    private String password = Gedcom.PASSWORD_NOT_SET;

    /** constructor */
    protected ActionOpen() {
      setTip("cc.tip.open_file");
      setText("cc.menu.open");
      setImage(Images.imgOpen);
      setAsync(ASYNC_NEW_INSTANCE);
    }

    /** constructor */
    protected ActionOpen(Origin setOrigin) {
      setAsync(ASYNC_NEW_INSTANCE);
      origin = setOrigin;
    }

    /**
     * (sync) pre execute
     */
    protected boolean preExecute() {

      // try to get an origin we're interested in
      if (origin==null) {
        origin = getOrigin();
        if (origin == null)
          return false; // don't continue into (async) execute without
      }

      // open it
      return open(origin);
    }

    /**
     * (Async) execute
     */
    protected void execute() {
      try {
        gedcom = reader.read();
      } catch (GedcomIOException ex) {
        exception = ex;
      }
    }

    /**
     * (sync) post execute
     */
    protected void postExecute() {
      
      // close progress
      windowManager.close(progress);
      
      // any error bubbling up?
      if (exception != null) {
        
        // maybe try with different password
        if (exception instanceof GedcomEncryptionException) {
          
          password = windowManager.openDialog(
            null, 
            origin.getName(), 
            WindowManager.IMG_QUESTION, 
            resources.getString("cc.provide_password"),
            "", 
            ControlCenter.this
          );
          
          if (password==null)
            password = Gedcom.PASSWORD_UNKNOWN;
          
          // retry
          exception = null;
          trigger();
          
          return;
        }

        // tell the user about it        
        windowManager.openDialog(
          null, 
          origin.getName(), 
          WindowManager.IMG_ERROR, 
          resources.getString("cc.open.read_error", "" + exception.getLine()) + ":\n" + exception.getMessage(),
          CloseWindow.OK(), 
          ControlCenter.this
        );

      } else {
        
        // show warnings
        if (reader!=null) {
          List warnings = reader.getWarnings();
          if (!warnings.isEmpty()) {
            windowManager.openNonModalDialog(
              null,
              resources.getString("cc.open.warnings", gedcom.getName()),
              WindowManager.IMG_WARNING,
              new JScrollPane(new JList(warnings.toArray())),
              CloseWindow.OK(),
              ControlCenter.this
            );
          }
        }
        
      }
      
      // got a successfull gedcom
      if (gedcom != null) {
        addGedcom(gedcom);
      }
      
      // done
    }

    /** 
     * Ask for an origin 
     */
    private Origin getOrigin() {

      // pop choice of opening
      ActionDelegate actions[] = {
        new CloseWindow(resources.getString("cc.open.choice.new"  )),
        new CloseWindow(resources.getString("cc.open.choice.local")),
        new CloseWindow(resources.getString("cc.open.choice.inet" )),
        new CloseWindow(CloseWindow.TXT_CANCEL),
      };

      int rc = windowManager.openDialog(
        null,
        resources.getString("cc.open.title"),
        WindowManager.IMG_QUESTION,
        resources.getString("cc.open.choice"),
        actions,
        ControlCenter.this
      );

      // check choices
      switch (rc) {
        case 0 :
          createNew();
          return null;
        case 1 :
          return chooseExisting();
        case 2 :
          return chooseURL();
        default :
          return null;
      }

      // done      
    }

    /** 
     * create a new Gedcom
     */
    private void createNew() {

      // let user choose a file
      File file = chooseFile(resources.getString("cc.create.title"), resources.getString("cc.create.action"), null);
      if (file == null)
        return;
      if (!file.getName().endsWith(".ged"))
        file = new File(file.getAbsolutePath()+".ged");
      if (file.exists()) {
        int rc = windowManager.openDialog(
          null,
          resources.getString("cc.create.title"),
          WindowManager.IMG_WARNING,
          resources.getString("cc.open.file_exists", file.getName()),
          CloseWindow.YESandNO(),
          ControlCenter.this
        );
        if (rc!=0)
          return;
      }
      // form the origin
      try {
        gedcom = new Gedcom(
          Origin.create(new URL("file", "", file.getAbsolutePath())),
          true
        );
      } catch (MalformedURLException e) {
      }
      // done
    }

    /**
     * choose a file
     */
    private Origin chooseExisting() {
      // ask user
      File file = chooseFile(resources.getString("cc.open.title"), resources.getString("cc.open.action"), null);
      if (file == null)
        return null;
      // remember last directory
      registry.put("last.dir", file.getParentFile().getAbsolutePath());
      // form origin
      try {
        return Origin.create(new URL("file", "", file.getAbsolutePath()));
      } catch (MalformedURLException e) {
        return null;
      }
      // done
    }

    /**
     * choose a url
     */
    private Origin chooseURL() {

      // pop a chooser    
      String[] choices = (String[])registry.get("urls", new String[0]);
      ChoiceWidget choice = new ChoiceWidget(choices, "");
      JLabel label = new JLabel(resources.getString("cc.open.enter_url"));
      
      int rc = windowManager.openDialog(null, resources.getString("cc.open.title"), WindowManager.IMG_QUESTION, new JComponent[]{label,choice}, CloseWindow.OKandCANCEL(), ControlCenter.this);
    
      // check the selection
      String item = choice.getText();
      if (rc!=0||item.length()==0) return null;

      // Try to form Origin
      Origin origin;
      try {
        origin = Origin.create(item);
      } catch (MalformedURLException ex) {
        windowManager.openDialog(null, item, WindowManager.IMG_ERROR, resources.getString("cc.open.invalid_url"), CloseWindow.OKandCANCEL(), ControlCenter.this);
        return null;
      }

      // Remember URL for dialog
      Set remember = new HashSet();
      remember.add(item);
      for (int c=0; c<choices.length&&c<9; c++) {
        remember.add(choices[c]);
      }
      registry.put("urls", remember);

      // ... continue
      return origin;
    }

    /**
     * Open Origin - continue with (async) execute if true
     */
    private boolean open(Origin origin) {

      // Check if already open
      if (tGedcoms.getGedcom(origin.getName())!=null) {
        windowManager.openDialog(null,origin.getName(),WindowManager.IMG_ERROR,resources.getString("cc.open.already_open", origin.getName()),CloseWindow.OK(),ControlCenter.this);
        return false;
      }

      // Open Connection and get input stream
      try {
        
        // .. prepare our reader
        reader = new GedcomReader(origin);
        
        // .. set password we're using
        reader.setPassword(password);

      } catch (IOException ex) {
        String txt = 
          resources.getString("cc.open.no_connect_to", origin)
            + "\n["
            + ex.getMessage()
            + "]";
        windowManager.openDialog(null, origin.getName(), WindowManager.IMG_ERROR, txt, CloseWindow.OK(), ControlCenter.this);
        return false;
      }

      // .. show progress dialog
      progress = windowManager.openNonModalDialog(
        null,
        resources.getString("cc.open.loading", origin.getName()),
        WindowManager.IMG_INFORMATION,
        new ProgressWidget(reader, getThread()),
        null,
        ControlCenter.this
      );

      // .. continue into (async) execute
      return true;
    }
    
  } //ActionOpen

  /**
   * Action - LoadLastOpen
   */
  private class ActionLoadLastOpen extends ActionDelegate {
    /** run */
    public void execute() {
      
      String example = "./gedcom/example.ged";
      String[] defaults =
        new File(example).exists() ? new String[] { "file:" + example }
      : new String[0];
      
      String[] lasts = registry.get("open", defaults);
      for (int i=0; i<lasts.length; i++) {
        try {
          String last = lasts[i];
          String pwd = null;
          int comma = last.indexOf(',');
          if (comma>0) {
            pwd = last.substring(comma+1);
            last = last.substring(0, comma);
          }
          ActionOpen open = new ActionOpen(Origin.create(last));
          if (pwd!=null) open.password = pwd;
          open.trigger();
        } catch (MalformedURLException x) {
        }
      }
      
    }
  } //LastOpenLoader

  /**
   * Action - Save
   */
  private class ActionSave extends ActionDelegate {
    /** whether to ask user */
    private boolean ask;
    /** gedcom */
    protected Gedcom gedcom;
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
    private File temp, result;
    /** password used */
    private String password;
    /** 
     * Constructor for saving gedcom file 
     */
    protected ActionSave(Gedcom gedcom) {
      this(false);
      this.gedcom = gedcom;
    }
    /** 
     * Constructor
     */
    protected ActionSave(boolean ask) {
      // remember
      this.ask = ask;
      // text
      if (ask)
        setText(resources.getString("cc.menu.saveas"));
      else
        setText(resources.getString("cc.menu.save"));
      // setup
      setImage(Images.imgSave);
      setAsync(ASYNC_NEW_INSTANCE);
    }
    /**
     * Initialize save
     * @see genj.util.ActionDelegate#preExecute()
     */
    protected boolean preExecute() {

      // Choose currently selected Gedcom if necessary
      if (gedcom==null) {
	      gedcom = tGedcoms.getSelectedGedcom();
	      if (gedcom == null)
	        return false;
      }
      
      // Do we need a file-dialog or not?
      Origin origin = gedcom.getOrigin();
      String encoding = gedcom.getEncoding();
      password = gedcom.getPassword();
      
      if (ask || origin==null || !origin.isFile()) {

        // .. choose file
        SaveOptionsWidget options = new SaveOptionsWidget(gedcom, viewManager);
        result = chooseFile(resources.getString("cc.save.title"), resources.getString("cc.save.action"), options);
        if (result==null)
          return false;

        // .. take choosen one & filters
        if (!result.getName().endsWith(".ged"))
          result = new File(result.getAbsolutePath()+".ged");
        filters = options.getFilters();
        if (gedcom.hasPassword())
          password = options.getPassword();
        encoding = options.getEncoding().toString();

        // .. create new origin
        try {
          newOrigin =
            Origin.create(new URL("file", "", result.getAbsolutePath()));
        } catch (Throwable t) {
        }

      } else {

        // .. form File from URL
        result = origin.getFile();

      }

      // Need confirmation if File exists?
      if (result.exists()&&ask) {

        int rc = windowManager.openDialog(null,resources.getString("cc.save.title"),WindowManager.IMG_WARNING,resources.getString("cc.open.file_exists", result.getName()),CloseWindow.YESandNO(),ControlCenter.this);
        if (rc!=0) {
          newOrigin = null;
          //20030221 no need to go for newOrigin in postExecute()
          return false;
        }
        
      }
      
      // .. open io 
      try {
        
        // .. create a temporary output
        temp = File.createTempFile("genj", ".ged", result.getParentFile());

        // .. create writer
        gedWriter =
          new GedcomWriter(gedcom, result.getName(), encoding, new FileOutputStream(temp));
          
        // .. set options
        gedWriter.setFilters(filters);
        gedWriter.setPassword(password);
        
      } catch (IOException ex) {
        windowManager.openDialog(null,gedcom.getName(),WindowManager.IMG_ERROR,resources.getString("cc.save.open_error", result.getAbsolutePath()),CloseWindow.OK(),ControlCenter.this);
        return false;
      }

      // .. open progress dialog
      progress = windowManager.openNonModalDialog(
        null,
        resources.getString("cc.save.saving", result.getName()),
        WindowManager.IMG_INFORMATION,
        new ProgressWidget(gedWriter, getThread()),
        null,
        ControlCenter.this
      );

      // .. continue (async)
      return true;

    }

    /** 
     * (async) execute
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {

      // catch io problems
      try {

        // .. do the write
        gedWriter.write();

        // .. make backup
        if (result.exists()) {
          File bak = new File(result.getAbsolutePath()+"~");
          if (bak.exists()) 
            bak.delete();
          result.renameTo(bak);
        }
        
        // .. and now !finally! move from temp to result
        if (!temp.renameTo(result))
          throw new GedcomIOException("Couldn't move temporary "+temp.getName()+" to "+result.getName(), -1);
       
        // .. note changes are saved now
        if (newOrigin == null) 
          gedcom.setUnchanged();

      } catch (GedcomIOException ex) {
        ioex = ex;
      }

      // done
    }

    /**
     * (sync) post write
     * @see genj.util.ActionDelegate#postExecute()
     */
    protected void postExecute() {

      // close progress
      windowManager.close(progress);
      
      // problem encountered?      
      if (ioex!=null) {
        windowManager.openDialog(null,gedcom.getName(),WindowManager.IMG_ERROR,resources.getString("cc.save.write_error", "" + ioex.getLine()) + ":\n" + ioex.getMessage(),CloseWindow.OK(),ControlCenter.this);
      } else {
        // .. open new
        if (newOrigin != null) {
          Gedcom old = tGedcoms.getGedcom(newOrigin.getName());
          if (old!=null)
            removeGedcom(old);
          ActionOpen open = new ActionOpen(newOrigin);
          open.password = password;
          open.trigger();
        }
      }

      // .. done
    }
    
  } //ActionSave

  /**
   * Action - Close
   */
  private class ActionClose extends ActionDelegate {
    /** constructor */
    protected ActionClose() {
      setText(resources.getString("cc.menu.close"));
      setImage(Images.imgClose);
    }
    /** run */
    protected void execute() {
  
      // Current Gedcom
      final Gedcom gedcom = tGedcoms.getSelectedGedcom();
      if (gedcom == null)
        return;
  
      // changes we should care about?      
      if (gedcom.hasUnsavedChanges()) {
        
        int rc = windowManager.openDialog(null,null,WindowManager.IMG_WARNING,
            resources.getString("cc.savechanges?", gedcom.getName()),
            CloseWindow.YESandNOandCancel(),ControlCenter.this);
        // cancel everything?
        if (rc==2)
          return;
        // save now?
        if (rc==0) {
          // Remove it so the user won't change it while being saved
          removeGedcom(gedcom);
          // and save
          new ActionSave(gedcom) {
            protected void postExecute() {
              // super first
              super.postExecute();
              // add back if still changed
              if (gedcom.hasUnsavedChanges())
                addGedcom(gedcom);
            }
          }.trigger();
          return;
        }
      }
  
      // Remove it
      removeGedcom(gedcom);
  
      // Done
    }
  } //ActionClose

  /**
   * Action - View
   */
  private class ActionView extends ActionDelegate {
    /** which ViewFactory */
    private ViewFactory factory;
    /** constructor */
    protected ActionView(int i, ViewFactory vw) {
      factory = vw;
      setText( (i>0 ? Integer.toString(i) + ' ' : "") +factory.getTitle(false));
      setShortText(factory.getTitle(true));
      setTip(
        resources.getString("cc.tip.open_view", factory.getTitle(false)));
      setImage(factory.getImage());
    }
    /** run */
    protected void execute() {
      // Current Gedcom
      final Gedcom gedcom = tGedcoms.getSelectedGedcom();
      if (gedcom == null)
        return;
      // Create new View
      viewManager.openView(factory, gedcom);
    }
  } //ActionView

  /**
   * Action - Options
   */
  private class ActionOptions extends ActionDelegate {
    /** constructor */
    protected ActionOptions() {
      setText(resources.getString("cc.menu.options"));
      setImage(OptionsWidget.IMAGE);
    }
    /** run */
    protected void execute() {
      // tell options about window manager - curtesy only
      Options.getInstance().setWindowManager(windowManager);
      // create widget for options
      OptionsWidget widget = new OptionsWidget(windowManager);
      widget.setOptions(OptionProvider.getAllOptions());
      // open dialog
      windowManager.openDialog("options", getText(), WindowManager.IMG_INFORMATION, widget, CloseWindow.OK(), ControlCenter.this);
      // done
    }
  } //ActionOptions

} //ControlCenter
