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
import genj.io.GedcomFormatException;
import genj.io.GedcomIOException;
import genj.io.GedcomReader;
import genj.io.GedcomWriter;
import genj.print.PrintManager;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.EnvironmentChecker;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.FileChooser;
import genj.util.swing.MenuHelper;
import genj.view.ViewFactory;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;

/**
 * The central component of the GenJ application
 */
public class ControlCenter extends JPanel {

  /** members */
  private GedcomTableWidget tGedcoms;
  private Vector busyGedcoms;
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
    printManager = new PrintManager(new Registry(setRegistry, "print"), windowManager);
    viewManager = new ViewManager(new Registry(setRegistry, "views"), printManager, windowManager);
    busyGedcoms = new Vector();

    // Table of Gedcoms
    tGedcoms = new GedcomTableWidget();
    tGedcoms.setRegistry(registry);

    // ... Listening
    tGedcoms.getSelectionModel().addListSelectionListener(
      (ListSelectionListener) new ActionToggleButtons().as(
        ListSelectionListener.class));

    // Layout
    setLayout(new BorderLayout());
    add(createToolBar(), "North");
    add(new JScrollPane(tGedcoms), "Center");

    // Load known gedcoms
    SwingUtilities.invokeLater(
      (Runnable) new ActionLoadLastOpen().as(Runnable.class));

    // Done
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(280,180);
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
      bh.create(new ActionView(factories[i]));
    }
    bh.setEnabled(true).removeCollection(gedcomButtons).setResources(resources);

    result.add(Box.createGlue());

    // Menu
    MenuHelper mh = new MenuHelper().setResources(resources);
    mh.createPopup(null, result);
    mh.createItem(new ActionToggleTnI());

    // done
    return result;
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
      mh.createItem(new ActionView(factories[i]));
    mh.setEnabled(true).setCollection(null).setResources(resources);

    result.add(Box.createHorizontalGlue());

    mh.popMenu().setEnabled(true).createMenu("cc.menu.help");

    mh.createItem(new ActionHelp());
    mh.createItem(new ActionAbout());

    // Done
    return result;
  }

  /**
   * A FileChoose that accepts .ged and looks into the appropriate
   * directory for Gedcom files
   */
  private class GedcomFileChooser extends FileChooser {

    /**
     * Constructor
     */
    protected GedcomFileChooser(String title, String action) {
      super(
        ControlCenter.this,
        title,
        action,
        new String[] { "ged" },
        "GEDCOM (*.ged)",
        EnvironmentChecker.getProperty(
          ControlCenter.this,
          new String[] { "genj.gedcom.dir" },
          ".",
          "choose gedcom file"));
    }

  } //GedcomFileChooser

  /**
   * Action - toggle text&images
   */
  private class ActionToggleTnI extends ActionDelegate {
    /** constructor */
    protected ActionToggleTnI() {
      super.setText("cc.menu.tni");
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
      super.setText("cc.menu.about");
      super.setImage(Images.imgAbout);
    }
    /** run */
    protected void execute() {
      windowManager.openFrame(
        "about",
        resources.getString("cc.menu.about"),
        Gedcom.getImage(),
        new AboutWidget(viewManager),
        null, null,
        null
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
      super.setText("cc.menu.contents");
      super.setImage(Images.imgHelp);
    }
    /** run */
    protected void execute() {
      windowManager.openFrame(
        "help",
        resources.getString("cc.menu.help"),
        Images.imgHelp,
        new HelpWidget(),
        null, null,
        null
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
      super.setText("cc.menu.exit");
      super.setImage(Images.imgExit);
    }
    /** run */
    protected void execute() {
      // Remember open gedcoms
      boolean unsaved = false;
      Vector save = new Vector();
      Enumeration gedcoms = tGedcoms.getAllGedcoms().elements();
      while (gedcoms.hasMoreElements()) {
        Gedcom gedcom = (Gedcom) gedcoms.nextElement();
        if (gedcom.getOrigin() != null) {
          save.addElement("" + gedcom.getOrigin());
        }
        unsaved |= gedcom.hasUnsavedChanges();
      }
      registry.put("open", save);

      // Unsaved changes ?
      if (unsaved) {
        int rc = windowManager.openDialog(
          null, 
          null, 
          WindowManager.IMG_WARNING, 
          resources.getString("cc.exit_changes?"), 
          WindowManager.OPTIONS_YES_NO, 
          ControlCenter.this
        );
        if (rc!=0) return;
      }

      // Tell it to the app
      windowManager.closeAll();

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

    /** an error we'll provide */
    private String error;

    /** a gedcom we're creating */
    private Gedcom gedcom;

    /** constructor */
    protected ActionOpen() {
      super.setImage(Gedcom.getImage());
      super.setTip("cc.tip.open_file");
      super.setText("cc.menu.open");
      super.setImage(Images.imgOpen);
      super.setAsync(ASYNC_NEW_INSTANCE);
    }

    /** constructor */
    protected ActionOpen(Origin setOrigin) {
      super.setAsync(ASYNC_NEW_INSTANCE);
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
        gedcom = reader.readGedcom();
      } catch (GedcomIOException ex) {
        error =
          resources.getString("cc.open.read_error", "" + ex.getLine())
            + ":\n"
            + ex.getMessage();
      } catch (GedcomFormatException ex) {
        error =
          resources.getString("cc.open.format_error", "" + ex.getLine())
            + ":\n"
            + ex.getMessage();
      }
    }

    /**
     * (sync) post execute
     */
    protected void postExecute() {
      // any error bubbling up?
      if (error != null) {
        windowManager.openDialog(
          null, 
          origin.getName(), 
          WindowManager.IMG_ERROR, 
          error, 
          WindowManager.OPTIONS_OK, 
          ControlCenter.this
        );
      } else {
        // show warnings
        if (reader!=null) {
          List warnings = reader.getWarnings();
          if (!warnings.isEmpty()) {
            windowManager.openDialog(
              null,
              origin.getName(),
              WindowManager.IMG_WARNING,
              new JScrollPane(new JList(warnings.toArray())),
              WindowManager.OPTIONS_OK,
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
      String options[] = {
        resources.getString("cc.open.choice.new"),
        resources.getString("cc.open.choice.local"),
        resources.getString("cc.open.choice.inet"),
        WindowManager.OPTION_CANCEL,
      };

      int rc = windowManager.openDialog(
        null,
        resources.getString("cc.open.title"),
        WindowManager.IMG_QUESTION,
        resources.getString("cc.open.choice"),
        options,
        ControlCenter.this
      );

      // check choices
      switch (rc) {
        case 0 :
          createNew();
          return null;
        case 1 :
          return chooseFile();
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

      // pop a chooser
      FileChooser chooser = new GedcomFileChooser(
        resources.getString("cc.create.title"),
        resources.getString("cc.create.action")
      );
      chooser.showDialog();
      // check the selection
      File file = chooser.getSelectedFile();
      if (file == null)
        return;
      if (file.exists()) {
        int rc = windowManager.openDialog(
          null,
          resources.getString("cc.create.title"),
          WindowManager.IMG_WARNING,
          resources.getString("cc.open.file_exists", file.getName()),
          WindowManager.OPTIONS_YES_NO,
          ControlCenter.this
        );
        if (rc!=0)
          return;
      }
      // form the origin
      try {
        Origin origin =
          Origin.create(new URL("file", "", file.getAbsolutePath()));
        gedcom = new Gedcom(origin);
      } catch (MalformedURLException e) {
      }
      // done
    }

    /**
     * choose a file
     */
    private Origin chooseFile() {

      // pop a chooser      
      FileChooser chooser = new GedcomFileChooser(
          resources.getString("cc.open.title"),
          resources.getString("cc.open.action")
      );
      chooser.showDialog();
      // check the selection
      File file = chooser.getSelectedFile();
      if (file == null)
        return null;
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
      choice.setEditable(true);
      JLabel label = new JLabel(resources.getString("cc.open.enter_url"));
      
      int rc = windowManager.openDialog(
        null, 
        resources.getString("cc.open.title"), 
        WindowManager.IMG_QUESTION,
        new JComponent[]{label,choice},
        WindowManager.OPTIONS_OK_CANCEL,
        ControlCenter.this
      );
    
      // check the selection
      String item = choice.getText();
      if (rc!=0||item.length()==0) return null;

      // Try to form Origin
      Origin origin;
      try {
        origin = Origin.create(item);
      } catch (MalformedURLException ex) {
        windowManager.openDialog(
          null,
          item,
          WindowManager.IMG_ERROR,
          resources.getString("cc.open.invalid_url"),
          WindowManager.OPTIONS_OK,
          ControlCenter.this
        );
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
      Vector gedcoms = tGedcoms.getAllGedcoms();
      for (int i = 0; i < gedcoms.size(); i++) {

        Gedcom g = (Gedcom) gedcoms.elementAt(i);

        if (origin.getName().equals(g.getName())) {
          windowManager.openDialog(
            null, 
            g.getName(), 
            WindowManager.IMG_ERROR, 
            resources.getString("cc.open.already_open", g.getName()),
            WindowManager.OPTIONS_OK,
            ControlCenter.this
          );
          return false;
        }
      }

      // Open Connection and get input stream
      InputStream in;
      long size;
      try {
        Origin.Connection connection = origin.open();
        // .. query for input stream & length
        in = connection.getInputStream();
        size = connection.getLength();
      } catch (IOException ex) {
        windowManager.openDialog(
          null, 
          origin.getName(), 
          WindowManager.IMG_ERROR, 
          resources.getString("cc.open.no_connect_to", origin)
            + "\n["
            + ex.getMessage()
            + "]",
          WindowManager.OPTIONS_OK,
          ControlCenter.this
        );
        return false;
      }

      // .. prepare our reader
      reader = new GedcomReader(in, origin, size);

      // .. prepare our thread
      getThread().setPriority(Thread.MIN_PRIORITY);

      // .. show progress dialog
// FIXME missing      
//      new ProgressDialog(
//        ControlCenter.this,
//        resources.getString("cc.open.loading"),
//        origin.getName(),
//        reader,
//        getThread());

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
      String[] gedcoms = registry.get("open", defaults);
      for (int g = 0; g < gedcoms.length; g++) {
        try {
          new ActionOpen(Origin.create(gedcoms[g])).trigger();
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
    private Gedcom gedcom;
    /** writer */
    private GedcomWriter gedWriter;
    /** origin to load after successfull save */
    private Origin newOrigin;
    /** filters we're using */
    private Filter[] filters;
    /** 
     * Constructor
     */
    protected ActionSave(boolean ask) {
      // remember
      this.ask = ask;
      // text
      if (ask)
        super.setText("cc.menu.saveas");
      else
        super.setText("cc.menu.save");
      // setup
      super.setImage(Images.imgSave);
      super.setAsync(ASYNC_NEW_INSTANCE);
    }
    /**
     * Initialize save
     * @see genj.util.ActionDelegate#preExecute()
     */
    protected boolean preExecute() {

      // Current Gedcom
      gedcom = tGedcoms.getSelectedGedcom();
      if (gedcom == null)
        return false;

      // Dialog ?
      Origin origin = gedcom.getOrigin();
      File file;
      String encoding = null;
      if ((ask) || (origin == null) || (!origin.isFile())) {

        // .. choose file
        FileChooser chooser = new GedcomFileChooser(
          resources.getString("cc.save.title"),
          resources.getString("cc.save.action")
        );

        // .. with options        
        SaveOptionsWidget options = new SaveOptionsWidget(gedcom, viewManager);
        chooser.setAccessory(options);

        // .. ask user
        if (FileChooser.APPROVE_OPTION != chooser.showDialog()) {
          return false;
        }

        // .. take choosen one & filters
        file = chooser.getSelectedFile();
        filters = options.getFilters();
        encoding = options.getEncoding().toString();

        // .. create new origin
        try {
          newOrigin =
            Origin.create(new URL("file", "", file.getAbsolutePath()));
        } catch (Throwable t) {
        }

      } else {

        // .. form File from URL
        file = origin.getFile();

      }

      // .. exists already?
      if (file.exists()) {

        // aks user
        if (ask) {
          int rc = windowManager.openDialog(
            null,
            resources.getString("cc.save.title"),
            WindowManager.IMG_WARNING,
            resources.getString("cc.open.file_exists", file.getName()),
            WindowManager.OPTIONS_YES_NO,
            ControlCenter.this
          );
          if (rc!=0) {
            newOrigin = null;
            //20030221 no need to go for newOrigin in postExecute()
            return false;
          }
        }
        
        // create backup
        backup(file);
      }

      // .. open writer on file
      try {
        gedWriter =
          new GedcomWriter(gedcom, file.getName(), new FileOutputStream(file), encoding);
        gedWriter.setFilters(filters);
      } catch (IOException ex) {
        windowManager.openDialog(
          null, 
          gedcom.getName(), 
          WindowManager.IMG_ERROR, 
          resources.getString("cc.save.open_error", file.getAbsolutePath()),
          WindowManager.OPTIONS_OK,
          ControlCenter.this
        );
        return false;
      }

      // .. prepare save 
      busyGedcoms.addElement(gedcom);

      // .. open progress dialog
// FIXME missing      
//      new ProgressDialog(
//        ControlCenter.this,
//        resources.getString("cc.save.saving"),
//        file.getAbsolutePath(),
//        gedWriter,
//        super.getThread()
//      );

      // .. continue (async)
      return true;

    }

    /** 
     * (async) execute
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {

      // .. do the write
      try {
        gedWriter.writeGedcom();
        if (newOrigin == null) {
          gedcom.setUnchanged();
        }
      } catch (GedcomIOException ex) {
        windowManager.openDialog(
          null,
          gedcom.getName(),
          WindowManager.IMG_ERROR,
          resources.getString("cc.save.write_error", "" + ex.getLine())
            + ":\n"
            + ex.getMessage(),
          WindowManager.OPTIONS_OK,
          ControlCenter.this
        );
        newOrigin = null;
      }

      // done
    }

    /**
     * (sync) post write
     * @see genj.util.ActionDelegate#postExecute()
     */
    protected void postExecute() {

      // .. finished
      busyGedcoms.removeElement(gedcom);

      // .. open new
      if (newOrigin != null) {

        Enumeration gedcoms = tGedcoms.getAllGedcoms().elements();
        while (gedcoms.hasMoreElements()) {
          Gedcom gedcom = (Gedcom) gedcoms.nextElement();
          if (gedcom.getOrigin().getName().equals(newOrigin.getName()))
            removeGedcom(gedcom);
        }

        new ActionOpen(newOrigin).trigger();
      }

      // .. done
    }
    
    /**
     * Backup a file
     */
    private void backup(File file) {
      
      // calc bak
      File bak = new File(file.getAbsolutePath()+"~");
      
      // remove if there
      if (bak.exists()) bak.delete();
      
      // rename file
      if (!file.renameTo(bak))
        Debug.log(Debug.WARNING, this, "Couldn't backup "+file);
      
      // done
    }

  } //ActionSave

  /**
   * Action - Close
   */
  private class ActionClose extends ActionDelegate {
    /** constructor */
    protected ActionClose() {
      super.setText("cc.menu.close");
      super.setImage(Images.imgClose);
    }
    /** run */
    protected void execute() {

      // Current Gedcom
      final Gedcom gedcom = tGedcoms.getSelectedGedcom();
      if (gedcom == null)
        return;

      // changes we should care about?      
      if (gedcom.hasUnsavedChanges()) {
        int rc = windowManager.openDialog(
          null,
          null,
          WindowManager.IMG_WARNING,
          resources.getString("cc.close_changes?"),
          WindowManager.OPTIONS_YES_NO,
          ControlCenter.this
        );
        if (rc!=0) return;
      }

      // Remove it
      removeGedcom(gedcom);

      // Done
    }
  } //ActionClose

  /**
   * Action - View
   */
  protected class ActionView extends ActionDelegate {
    /** which ViewFactory */
    private ViewFactory factory;
    /** constructor */
    protected ActionView(ViewFactory vw) {
      factory = vw;
      super.setText(factory.getTitle(false));
      super.setShortText(factory.getTitle(true));
      super.setTip(
        resources.getString("cc.tip.open_view", factory.getTitle(false)));
      super.setImage(factory.getImage());
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
   * Action - ActionToggleButtons
   */
  private class ActionToggleButtons extends ActionDelegate {
    /** run */
    protected void execute() {
      ButtonHelper.setEnabled(
        gedcomButtons,
        tGedcoms.getSelectedGedcom() != null);
    }
  } //ActionToggleButtons    

}
