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
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import genj.Version;
import genj.gedcom.*;
import genj.util.*;
import genj.util.swing.*;
import genj.print.*;
import genj.tool.*;
import genj.option.*;
import genj.io.*;

/**
 * The central component of the GenJ application
 */
public class ControlCenter extends JPanel {
  
  private final static String
    CREATE_PREFIX = "create.",
    VIEW_PREFIX   = "view.";

  /** members */
  private GedcomTableWidget tGedcoms;
  private JFrame frame;
  private Vector busyGedcoms;
  private ControlCenter me;
  private Registry registry;
  private Vector gedcomButtons = new Vector();
  private Vector tniButtons = new Vector();

  /**
   * Constructor
   */
  public ControlCenter(JFrame setFrame, Registry setRegistry) {

    // Initialize data
    me      =this;
    frame   =setFrame;
    registry=new Registry(setRegistry,"cc");
    busyGedcoms=new Vector();

    // Initialize the frame
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener((WindowListener)new ActionExit().as(WindowListener.class,"windowClosing"));
    frame.setJMenuBar(getMenuBar());

    // Table of Gedcoms
    tGedcoms = new GedcomTableWidget();
    tGedcoms.setRegistry(registry);

    // ... Listening
    tGedcoms.getSelectionModel().addListSelectionListener((ListSelectionListener)new ActionToggleButtons().as(ListSelectionListener.class));

    // Create Pane for buttons
    JPanel gedcomPane = getTopButtonBar();
    
    // Actions Pane
    JPanel entityPane = getBottomButtonBar();
    
    // Layout
    setLayout(new BorderLayout());
    add(gedcomPane ,"North");
    add(new JScrollPane(tGedcoms), "Center");
    add(entityPane ,"South");

    // Load known gedcoms
    SwingUtilities.invokeLater((Runnable)new ActionLoadLastOpen().as(Runnable.class));

    // Done
  }
  
  /**
   * Adds another Gedcom to the list of Gedcoms
   */
  public void addGedcom(Gedcom gedcom) {
    tGedcoms.addGedcom(gedcom);
  }

  /**
   * Returns a button bar for the top
   */
  private JPanel getTopButtonBar() {

    // wether we're showing text on the buttons, too    
    boolean imageAndText = registry.get("imagesandtext", false);
    
    // .. Buttons
    JPanel pButtons = new JPanel(new GridLayout());
    ButtonHelper bh = new ButtonHelper()
      .setResources(App.resources)
      .setInsets(4)
      .setFocusable(false)
      .setContainer(pButtons)
      .setTextAllowed(imageAndText)
      .setShortTexts(true)
      .setImageOverText(true)
      .setFontSize(10)
      .addCollection(tniButtons);

    bh.setEnabled(true).create(new ActionOpen());
    
    bh.setEnabled(false).addCollection(gedcomButtons);
    ViewManager.Descriptor[] ds=ViewManager.getInstance().getDescriptors();
    for (int i=0; i<ds.length; i++) {
      JButton b = bh.create(new ActionView(ds[i]));
    }
    
    bh.setEnabled(true).removeCollection(gedcomButtons);
    bh.create(new ActionSettings());

    // the result
    JPanel result = new JPanel(new BorderLayout());
    result.add(pButtons, BorderLayout.WEST);
    result.add(Box.createGlue(), BorderLayout.CENTER);
    
    // Menu
    MenuHelper mh = new MenuHelper()
      .setResources(App.resources);
    mh.createPopup(null, result);
    mh.createItem(new ActionToggleTnI());

    // done
    return result;
  }

  /**
   * Returns a menu for frame showing this controlcenter
   */
  private JPanel getBottomButtonBar() {
  
    JPanel result = new JPanel();
    result.setLayout(new BoxLayout(result,BoxLayout.X_AXIS));

    // .. Buttons
    ButtonHelper bh = new ButtonHelper()
      .setResources(App.resources)
      .setInsets(4)
      .setFocusable(false)
      .setContainer(result)
      .addCollection(gedcomButtons)
      .setEnabled(false);
      
    bh.create(new ActionCreate(Gedcom.INDIVIDUALS, Images.imgNewIndi, "cc.tip.create_indi"));
    bh.create(new ActionCreate(Gedcom.FAMILIES, Images.imgNewFam, "cc.tip.create_fam"));
    bh.create(new ActionCreate(Gedcom.MULTIMEDIAS, Images.imgNewMedia, "cc.tip.create_media"));
    bh.create(new ActionCreate(Gedcom.NOTES, Images.imgNewNote, "cc.tip.create_note"));
    bh.removeCollection(gedcomButtons);
    bh.create(new ActionCreate(Gedcom.SOURCES, Images.imgNewSource, "cc.tip.create_source"));
    bh.create(new ActionCreate(Gedcom.SUBMITTERS, Images.imgNewSubmitter, "cc.tip.create_submitter"));
    bh.create(new ActionCreate(Gedcom.REPOSITORIES, Images.imgNewRepository, "cc.tip.create_repository"));
    bh.addCollection(gedcomButtons);
    bh.create(new ActionDelete());

    // done
    return result;
  }
  
  /**
   * Returns a menu for frame showing this controlcenter
   */
  private JMenuBar getMenuBar() {
    
    MenuHelper mh = new MenuHelper().setResources(App.resources);
    
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
    
      mh.setEnabled(false).setCollection(gedcomButtons);    
      ViewManager.Descriptor[] ds=ViewManager.getInstance().getDescriptors();
      for (int i=0; i<ds.length; i++) 
        mh.createItem(new ActionView(ds[i]));
      mh.setEnabled(true).setCollection(null);    

    mh.popMenu().createMenu("cc.menu.tools");

      mh.setEnabled(false).setCollection(gedcomButtons);    
      mh.createItem(new ActionMerge());
      mh.createItem(new ActionVerify());
      mh.setEnabled(true).setCollection(null);    

    result.add(Box.createHorizontalGlue());

    mh.popMenu().createMenu("cc.menu.help");
    
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
    
    protected GedcomFileChooser(JFrame frame, String title, String action) {
      super(frame,title,action,
        new String[]{"ged"}, "GEDCOM (*.ged)",
        EnvironmentChecker.getProperty(
          ControlCenter.this,
          new String[]{ "genj.gedcom.dir" },
          ".",
          "choose gedcom file"
        )
      );
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
      registry.put("imagesandtext",set);
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
    }
    /** run */
    protected void execute() {
      // know the frame already?
      JFrame frame = App.getInstance().getFrame("about");
      if (frame==null) {
        frame = App.getInstance().createFrame(App.resources.getString("cc.title.about"),null,"about",null);
        frame.getContentPane().add(new AboutWidget(frame));
        frame.pack();
      }
      frame.show();
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
    }
    /** run */
    protected void execute() {
      // know the frame already?
      JFrame frame = App.getInstance().getFrame("help");
      if (frame==null) {
        frame = App.getInstance().createFrame(App.resources.getString("cc.title.help"),Images.imgHelp,"help",new Dimension(640,480));
        frame.getContentPane().add(new HelpWidget(frame));
        frame.pack();
      }
      frame.show();
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
    }
    /** run */
    protected void execute() {
      // Remember open gedcoms
      boolean unsaved = false;
      Vector save = new Vector();
      Enumeration gedcoms = tGedcoms.getAllGedcoms().elements();
      while (gedcoms.hasMoreElements()) {
        Gedcom gedcom = (Gedcom)gedcoms.nextElement();
        if (gedcom.getOrigin()!=null) {
          save.addElement(""+gedcom.getOrigin());
        }
        unsaved |= gedcom.hasUnsavedChanges();
      }
      registry.put("open",save);
  
      // Unsaved changes ?
      if (unsaved) {
        int rc = JOptionPane.showConfirmDialog(
          frame,
          App.resources.getString("cc.exit_changes?"),
          App.resources.getString("app.warning"),
          JOptionPane.YES_NO_OPTION
        );
        if (rc==JOptionPane.NO_OPTION) {
          return;
        }
      }
  
      // Tell it to the app
      App.getInstance().shutdown();
  
      // Done
    } 
  } //ActionExit

  /**
   * Action - open
   */
  private class ActionOpen extends ActionDelegate { 
    
    /** a preset origin we're reading from */
    private Origin presetOrigin;
    
    /** a reader we're working on */
    private GedcomReader reader;

    /** an error we'll provide */
    private String error;
    
    /** a gedcom we're creating */
    private Gedcom gedcom;
    
    /** constructor */
    protected ActionOpen() {
      super.setImage(Images.imgGedcom);
      super.setTip("cc.tip.open_file");
      super.setText("cc.menu.open");
      super.setAsync(ASYNC_NEW_INSTANCE);
    }

    /** constructor */
    protected ActionOpen(Origin origin) {
      super.setAsync(ASYNC_NEW_INSTANCE);
      presetOrigin=origin;            
    }
    
    /**
     * (sync) pre execute
     */
    protected boolean preExecute() {
      
      // try to get an origin we're interested in
      Origin origin;
      if (presetOrigin!=null) 
        origin = presetOrigin;
      else
        origin = getOrigin();
        
      if (origin==null) return false; // don't continue into (async) execute without
      
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
        error = App.resources.getString("cc.open.read_error",""+ex.getLine())+":\n"+ex.getMessage();
      } catch (GedcomFormatException ex) {
        error = App.resources.getString("cc.open.format_error",""+ex.getLine())+":\n"+ex.getMessage();
      }
    }
    
    /**
     * (sync) post execute
     */
    protected void postExecute() {
      if (error!=null) {
        JOptionPane.showMessageDialog(frame,error,App.resources.getString("app.error"),JOptionPane.ERROR_MESSAGE);
      }
      if (gedcom!=null) {
        addGedcom(gedcom);
      }
    }
    
    /** 
     * Ask for an origin 
     */
    private Origin getOrigin() {
  
      // pop choice of opening
      String selections[] = {
        App.resources.getString("cc.open.choice.new"),
        App.resources.getString("cc.open.choice.local"),
        App.resources.getString("cc.open.choice.inet"),
        App.resources.getString("app.cancel"),
      };
  
      int rc = JOptionPane.showOptionDialog(frame,
        App.resources.getString("cc.open.choice"),
        App.resources.getString("cc.open.title"),
        0,JOptionPane.QUESTION_MESSAGE,null,selections,selections[1]
      );

      // check choices
      switch (rc) {
        case 0: createNew(); return null;
        case 1: return chooseFile();
        case 2: return chooseURL();
        default: return null;
      }

      // done      
    }
    
    /** 
     * create a new Gedcom
     */
    private void createNew() {

      // pop a chooser
      FileChooser chooser = new GedcomFileChooser(
        frame,
        App.resources.getString("cc.create.title"),
        App.resources.getString("cc.create.action")
      );
      chooser.showDialog();
      // check the selection
      File file = chooser.getSelectedFile();
      if (file==null) return;
      if (file.exists()) {
        int rc = JOptionPane.showConfirmDialog(
          frame,
          App.resources.getString("cc.open.file_exists"),
          App.resources.getString("cc.create.title"),
          JOptionPane.YES_NO_OPTION
        );
        if (rc==JOptionPane.NO_OPTION) return;
      }
      // form the origin
      try {
        Origin origin = Origin.create(new URL("file","",file.getAbsolutePath()));
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
        frame,
        App.resources.getString("cc.open.title"),
        App.resources.getString("cc.open.action")
      );
      chooser.showDialog();
      // check the selection
      File file = chooser.getSelectedFile();
      if (file==null) return null;
      // form origin
      try {
        return Origin.create(new URL("file","",file.getAbsolutePath()));
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
      Vector    vUrls  = registry.get("urls", new Vector());
      JLabel    lEnter = new JLabel(App.resources.getString("cc.open.enter_url"));
      JComboBox cEnter = new JComboBox(vUrls);
      cEnter.setEditable(true);
      Object message[] = { lEnter, cEnter };
      Object options[] = { App.resources.getString("app.ok"), App.resources.getString("app.cancel") };

      int rc = JOptionPane.showOptionDialog(
        frame,
        message,
        App.resources.getString("cc.open.title"),
        JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
        options,options[0]
      );

      // check the selection
      String item=(String)cEnter.getEditor().getItem();
      if ((rc!=JOptionPane.OK_OPTION)||(item.length()==0)) {
        return null;
      }

      // Try to form Origin
      Origin origin;
      try {
        origin = Origin.create(item);
      } catch (MalformedURLException ex) {
        JOptionPane.showMessageDialog(frame,
          App.resources.getString("cc.open.invalid_url"),
          App.resources.getString("app.error"),
          JOptionPane.ERROR_MESSAGE
        );
        return null;
      }

      // Remember URL for dialog
      for (int i=0;i<vUrls.size();i++) {
        if ( vUrls.elementAt(i).toString().equals(item) ) {
          vUrls.removeElementAt(i);
          break;
        }
      }
      vUrls.insertElementAt(item,0);
      if (vUrls.size() > 10)
      vUrls.setSize( 10 );
      registry.put("urls",vUrls);

      // ... continue
      return origin;
    }
    
    /**
     * Open Origin - continue with (async) execute if true
     */
    private boolean open(Origin origin) {
  
      // Check if already open
      Vector gedcoms = tGedcoms.getAllGedcoms();
      for (int i=0;i<gedcoms.size();i++) {
  
        Gedcom g = (Gedcom)gedcoms.elementAt(i);
  
        if (origin.getName().equals(g.getName())) {
          JOptionPane.showMessageDialog(frame,
            App.resources.getString("cc.open.already_open", g.getName() ),
            App.resources.getString("app.error"),
            JOptionPane.ERROR_MESSAGE
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
        JOptionPane.showMessageDialog(frame,
          App.resources.getString("cc.open.no_connect_to", origin ) + "\n[" + ex.getMessage() +"]",
          App.resources.getString("app.error"),
          JOptionPane.ERROR_MESSAGE
        );
        return false;
      }
  
      // .. prepare our reader
      reader = new GedcomReader(in,origin,size);
      
      // .. prepare our thread
      getThread().setPriority(Thread.MIN_PRIORITY);
  
      // .. show progress dialog
      new ProgressDialog(frame,App.resources.getString("cc.open.loading"),
        ""+origin,reader,getThread()
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
      String[] defaults = new File(example).exists() ? new String[]{"file:"+example} : new String[0];
      String[] gedcoms = registry.get("open",defaults);
      for (int g=0;g<gedcoms.length;g++) {
        try {
          new ActionOpen(Origin.create(gedcoms[g])).trigger();
        } catch (MalformedURLException x) {
        }
      }
    }
  } //LastOpenLoader

  /**
   * Action delete
   */
  private class ActionDelete extends ActionDelegate { 
    /** constructor */
    protected ActionDelete() {
      super.setImage(Images.imgDelEntity);
      super.setTip("cc.tip.delete_entity");
    }
    /** run */
    protected void execute() {
      // Setup delete dialog
      JFrame frame = App.getInstance().createFrame(
        App.resources.getString("cc.title.delete_entity"),
        Images.imgDelEntity,
        "delentity",
        null
      );
      OptionDelEntity option = new OptionDelEntity(frame,tGedcoms.getAllGedcoms(),tGedcoms.getSelectedGedcom());
      frame.getContentPane().add(option);
      frame.pack();
      frame.show();
      // Done
    } 
  } //ActionDelete
  
  /**
   * Action - Save
   */
  private class ActionSave extends ActionDelegate {
    /** which */
    private boolean ask;
    /** constructor */
    protected ActionSave(boolean ask) {
      this.ask=ask;
      if (ask) super.setText("cc.menu.saveas");
      else super.setText("cc.menu.save");
    }
    /** run */
    protected void execute() {
      
      // Current Gedcom
      final Gedcom gedcom = tGedcoms.getSelectedGedcom();
      if (gedcom==null) return;
      
      // Dialog ?
      Origin origin = gedcom.getOrigin();
      File file;
      if ( (ask) || (origin==null) || (!origin.isFile()) ) {
  
        // .. choose file
        FileChooser chooser = new GedcomFileChooser(
          frame,
          App.resources.getString("cc.save.title"),
          App.resources.getString("cc.save.action")
        );
  
        if (FileChooser.APPROVE_OPTION != chooser.showDialog()) {
          return;
        }
  
        // .. take choosen one
        file = chooser.getSelectedFile();
  
        // .. remember (new) origin
        try {
          gedcom.setOrigin(
            Origin.create(new URL("file","",file.getAbsolutePath()))
          );
        } catch (Exception e) {
        }
  
      } else {
  
        // .. form File from URL
        file = origin.getFile();
  
      }
  
      // .. exits ?
      if (ask&&file.exists()) {
  
        int rc = JOptionPane.showConfirmDialog(
          frame,
          App.resources.getString("cc.open.file_exists"),
          App.resources.getString("cc.save.title"),
          JOptionPane.YES_NO_OPTION
        );
  
        if (rc==JOptionPane.NO_OPTION) {
          return;
        }
      }
  
      // .. open file
      final FileWriter writer;
      try {
        writer = new FileWriter(file);
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(
          frame,
          App.resources.getString("cc.save.open_error",file.getAbsolutePath()),
          App.resources.getString("app.error"),
          JOptionPane.ERROR_MESSAGE
        );
        return;
      }
  
      // .. save data
      busyGedcoms.addElement(gedcom);
  
      final GedcomWriter gedWriter = new GedcomWriter(gedcom,file.getName(),new BufferedWriter(writer));
  
      final Thread threadWriter = new Thread() {
        // LCD
        /** main */
        public void run() {
  
          // .. do the write
          try {
            gedWriter.writeGedcom();
          } catch (GedcomIOException ex) {
            JOptionPane.showMessageDialog(
              frame,
              App.resources.getString("cc.save.write_error",""+ex.getLine())+":\n"+ex.getMessage(),
              App.resources.getString("app.error"),
              JOptionPane.ERROR_MESSAGE
            );
          }
  
          // .. finished
          busyGedcoms.removeElement(gedcom);
  
          // .. done
        }
        // EOC
      };
      threadWriter.start();
  
      // .. show progress dialog
      new ProgressDialog(
        frame,
        App.resources.getString("cc.save.saving"),
        file.getAbsolutePath(),
        gedWriter,
        threadWriter
      );
  
      // .. done
      return;
  
    }
    
  } //ActionSave
  
  /**
   * Action - Close
   */
  private class ActionClose extends ActionDelegate { 
    /** constructor */
    protected ActionClose() {
      super.setText("cc.menu.close");
    }
    /** run */
    protected void execute() {
  
      // Current Gedcom
      final Gedcom gedcom = tGedcoms.getSelectedGedcom();
      if (gedcom==null) return;
  
      // changes we should care about?      
      if (gedcom.hasUnsavedChanges()) {
        int rc = JOptionPane.showConfirmDialog(
          frame,
          App.resources.getString("cc.close_changes?"),
          App.resources.getString("app.warning"),
          JOptionPane.YES_NO_OPTION
        );
        if (rc==JOptionPane.NO_OPTION) {
          return;
        }
      }
  
      // Close all views for that gedcom
      ViewManager.getInstance().closeViews(gedcom);
  
      // Close instance
      gedcom.close();
  
      // Done
    } 
  } //ActionClose
  
  /**
   * Action - Merge
   */
  private class ActionMerge extends ActionDelegate { 
    /** constructor */
    protected ActionMerge() {
      super.setText("cc.menu.merge");
    }    
    /** run */
    protected void execute() {
  
      // Current Gedcom
      final Gedcom gedcom = tGedcoms.getSelectedGedcom();
      if (gedcom==null) return;
    
      // Setup merge dialog
      JFrame frame = App.getInstance().createFrame(
        App.resources.getString("cc.title.merge_gedcoms"),
        Images.imgGedcom,
        "merge",
        null
      );
    
      Transaction transaction = new MergeTransaction(gedcom, tGedcoms.getAllGedcoms(),ControlCenter.this);
    
      TransactionPanel tpanel = new TransactionPanel(frame,transaction);
      frame.getContentPane().add(tpanel);
    
      frame.pack();
      frame.show();
    
      // Done
    } 
  } //ActionMerge

  /**
   * Action - Verify
   */
  private class ActionVerify extends ActionDelegate { 
    /** constructor */
    protected ActionVerify() {
      super.setText("cc.menu.verify");
    }    
    /** run */
    protected void execute() {
  
      // Current Gedcom
      final Gedcom gedcom = tGedcoms.getSelectedGedcom();
      if (gedcom==null) return;
  
      // Setup verify dialog
      JFrame frame = App.getInstance().createFrame(
        App.resources.getString("cc.title.verify_gedcom"),
        Images.imgGedcom,
        "verify",
        null
      );
  
      Transaction transaction = new VerifyTransaction(gedcom, tGedcoms.getAllGedcoms());
  
      TransactionPanel tpanel = new TransactionPanel(frame,transaction);
      frame.getContentPane().add(tpanel);
      frame.pack();
      frame.show();
  
      // Done
    } 
  } //ActionVerify
  
  /**
   * Action - Settings
   */
  protected class ActionSettings extends ActionDelegate { 
    /** constructor */
    protected ActionSettings() {
      super.setImage(Images.imgSettings);
      super.setText("cc.view.settings");
      super.setShortText("cc.view.short.settings");
      super.setTip("cc.tip.settings");
    }    
    /** run */
    protected void execute() {
      ViewManager.getInstance().openSettings(null);
    } 
  } // ActionSettings
    
  /**
   * Action - Create
   */
  private class ActionCreate extends ActionDelegate { 
    
    /** type */
    private int type;
    
    /** constructor */
    protected ActionCreate(int type, ImgIcon img, String tip) {
      this.type = type;
      super.setImage(img);
      super.setTip(tip);
    }
    
    /** run */
    protected void execute() {
  
      ImgIcon img;
      int otype;
      
      switch (type) {
        case Gedcom.INDIVIDUALS:
          otype = OptionNewEntity.INDIVIDUAL;
          img  = Images.imgNewIndi;
          break;
        case Gedcom.FAMILIES:
          otype = OptionNewEntity.FAMILY;
          img  = Images.imgNewFam;
          break;
        case Gedcom.MULTIMEDIAS:
          otype = OptionNewEntity.MULTIMEDIA;
          img  = Images.imgNewMedia;
          break;
        case Gedcom.NOTES:
          otype = OptionNewEntity.NOTE;
          img  = Images.imgNewNote;
          break;
        default:
          return;
      }
  
      JFrame frame = App.getInstance().createFrame(
        App.resources.getString("cc.title.create_entity")+" - "+Gedcom.getNameFor(type,false),
        img,
        Gedcom.getTagFor(type),
        null
      );
  
      OptionNewEntity option = new OptionNewEntity(frame,otype,tGedcoms.getAllGedcoms(),tGedcoms.getSelectedGedcom());
      frame.getContentPane().add(option);
      frame.pack();
      frame.show();
  
      // Done
    }
  } //ActionCreate

  /**
   * Action - View
   */
  protected class ActionView extends ActionDelegate { 
    /** which (ViewBridge.getDescriptors() index) */
    private ViewManager.Descriptor which;
    /** constructor */
    protected ActionView(ViewManager.Descriptor which) {
      this.which = which;
      super.setText("cc.view."+which.key);
      super.setShortText("cc.view.short."+which.key);
      super.setTip("cc.tip.open_"+which.key);
      super.setImage(which.img);
    }
    /** run */
    protected void execute() {
      // Current Gedcom
      final Gedcom gedcom = tGedcoms.getSelectedGedcom();
      if (gedcom==null) return;
      // Create new View
      ViewManager.getInstance().openView(which,gedcom);
    } 
  } //ActionView
      
  /**
   * Action - ActionToggleButtons
   */
  private class ActionToggleButtons extends ActionDelegate {
    /** run */
    protected void execute() {
      ButtonHelper.setEnabled(gedcomButtons, tGedcoms.getSelectedGedcom()!=null);
    }
  } //ActionToggleButtons    
  
}
