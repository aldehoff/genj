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
  private Delegate actionDelegate = new Delegate();

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
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        actionDelegate.exit();
      }
    });
    frame.setJMenuBar(getMenuBar());

    // Table of Gedcoms
    tGedcoms = new GedcomTableWidget();
    tGedcoms.setRegistry(registry);

    // ... Listening
    ListSelectionListener tlistener = new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent lse) {
        // Calc Gedcom
        Gedcom gedcom = tGedcoms.getSelectedGedcom();

        // Check if action on selected item is possible
        boolean on = (gedcom!=null);

        // Switch on/off AbstractButtons related to Gedcom
        Enumeration e = gedcomButtons.elements();
        while (e.hasMoreElements()) {
          ((AbstractButton)e.nextElement()).setEnabled(on);
        }
        
        // Done
      }
    };
    tGedcoms.getSelectionModel().addListSelectionListener(tlistener);

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
    SwingUtilities.invokeLater(new LastOpenLoader());

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
    
    // the result
    JPanel result = new JPanel();
    result.setLayout(new BoxLayout(result,BoxLayout.X_AXIS));

    // .. Buttons
    ButtonHelper bh = new ButtonHelper()
      .setResources(App.resources)
      .setListener(actionDelegate)
      .setInsets(4)
      .setFocusable(false)
      .setContainer(result);

    bh.setImage(Images.imgGedcom      ).setAction("open"        ).setTip("cc.tip.open_file"     ).setEnabled(true ).create();
    bh.setEnabled(false).setCollection(gedcomButtons);
    ViewBridge.Descriptor[] ds=ViewBridge.getInstance().getDescriptors();
    for (int i=0; i<ds.length; i++) {
      ViewBridge.Descriptor d = ds[i];
      bh.setImage(d.img).setAction(VIEW_PREFIX+d.key).setTip("cc.tip.open_"+d.key).create();
    }
    bh.setImage(Images.imgSettings    ).setAction("settings"    ).setTip("cc.tip.settings"      ).setEnabled(true ).create();

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
      .setListener(actionDelegate)
      .setInsets(4)
      .setFocusable(false)
      .setContainer(result)
      .setCollection(gedcomButtons);

    bh.setEnabled(false)
      .setContainer(result);
    bh.setImage(Images.imgNewIndi      ).setAction(CREATE_PREFIX+Gedcom.INDIVIDUALS ).setTip("cc.tip.create_indi"       ).create();
    bh.setImage(Images.imgNewFam       ).setAction(CREATE_PREFIX+Gedcom.FAMILIES    ).setTip("cc.tip.create_fam"        ).create();
    bh.setImage(Images.imgNewMedia     ).setAction(CREATE_PREFIX+Gedcom.MULTIMEDIAS ).setTip("cc.tip.create_media"      ).create();
    bh.setImage(Images.imgNewNote      ).setAction(CREATE_PREFIX+Gedcom.NOTES       ).setTip("cc.tip.create_note"       ).create();
    bh.setCollection(null);
    bh.setImage(Images.imgNewSource    ).setAction(CREATE_PREFIX+Gedcom.SOURCES     ).setTip("cc.tip.create_source"     ).create();
    bh.setImage(Images.imgNewSubmitter ).setAction(CREATE_PREFIX+Gedcom.SUBMITTERS  ).setTip("cc.tip.create_submitter"  ).create();
    bh.setImage(Images.imgNewRepository).setAction(CREATE_PREFIX+Gedcom.REPOSITORIES).setTip("cc.tip.create_repository" ).create();
    bh.setCollection(gedcomButtons);
    bh.setImage(Images.imgDelEntity    ).setAction("delete"       ).setTip("cc.tip.delete_entity"     ).create();

    // done
    return result;
  }
  
  /**
   * Returns a menu for frame showing this controlcenter
   */
  private JMenuBar getMenuBar() {
    
    MenuHelper mh = new MenuHelper()
      .setListener(actionDelegate)
      .setResources(App.resources);
    
    JMenuBar result = mh.createBar();

    // Create Menues
    mh.setText("cc.menu.file").createMenu();
    
      mh.setText("cc.menu.open"  ).setAction("open"  ).createItem();
      mh.createSeparator().setEnabled(false).setCollection(gedcomButtons);
      mh.setText("cc.menu.save"  ).setAction("save"  ).createItem();
      mh.setText("cc.menu.saveas").setAction("saveas").createItem();
      mh.setText("cc.menu.close" ).setAction("close" ).createItem();
      mh.createSeparator().setEnabled(true).setCollection(null);
      mh.setText("cc.menu.exit"  ).setAction("exit"  ).createItem();

    mh.setMenu(null).setText("cc.menu.view").createMenu();
    
      mh.setEnabled(false).setCollection(gedcomButtons);    
      ViewBridge.Descriptor[] ds=ViewBridge.getInstance().getDescriptors();
      for (int i=0; i<ds.length; i++) {
        ViewBridge.Descriptor d = ds[i];
        mh.setImage(d.img).setAction(VIEW_PREFIX+d.key).setText("cc.tip.open_"+d.key).createItem();
      }
      mh.setEnabled(true).setCollection(null);    

    mh.setImage(null).setMenu(null).setText("cc.menu.tools").createMenu();

      mh.setEnabled(false).setCollection(gedcomButtons);    
      mh.setText("cc.menu.merge" ).setAction("merge" ).createItem();
      mh.setText("cc.menu.verify").setAction("verify").createItem();
      mh.setEnabled(true).setCollection(null);    

    result.add(Box.createHorizontalGlue());

    mh.setMenu(null).setText("cc.menu.help").createMenu();
    
      mh.setText("cc.menu.contents").setAction("help").createItem();
      mh.setText("cc.menu.about"   ).setAction("about"   ).createItem();

    // Done
    return result;
  }

  /**
   * Read Gedcom from URL
   */
  private void readGedcomFrom(Origin origin) {

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
        return;
      }
    }

    // Check for given argument
    long size;
    InputStream in;

    // .. Open Connection and get input stream
    Origin.Connection connection;
    try {
      connection = origin.open();
      // .. query for input stream
      in = connection.getInputStream();
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(frame,
        App.resources.getString("cc.open.no_connect_to", origin ) + "\n[" + ex.getMessage() +"]",
        App.resources.getString("app.error"),
        JOptionPane.ERROR_MESSAGE
      );
      return;
    }
    size = connection.getLength();

    // .. read gedcom from it
    final GedcomReader gedReader = new GedcomReader(in,origin,size);

    final Thread threadReader = new Thread() {
      // LCD
      /** main */
      public void run() {
        String err=null;
        try {
          addGedcom(gedReader.readGedcom());
        } catch (GedcomIOException ex) {
          err = App.resources.getString("cc.open.read_error",""+ex.getLine())+":\n"+ex.getMessage();
        } catch (GedcomFormatException ex) {
          err = App.resources.getString("cc.open.format_error",""+ex.getLine())+":\n"+ex.getMessage();
        }
        if (err!=null) {
          JOptionPane.showMessageDialog(frame,
          err,
          App.resources.getString("app.error"),
          JOptionPane.ERROR_MESSAGE);
        }
      }
      // EOC
    };

    //    threadReader.setPriority(Math.max(Thread.MIN_PRIORITY,threadReader.getPriority()-2));
    threadReader.setPriority(Thread.MIN_PRIORITY);
    threadReader.start();

    // .. show progress dialog
    new ProgressDialog(frame,App.resources.getString("cc.open.loading"),
      ""+origin,
      gedReader,
      threadReader
    );

    // .. done
  }

  /**
   * A FileChoose that accepts .ged and looks into the appropriate
   * directory for Gedcom files
   */
  private class GedcomFileChooser extends FileChooser {
    
    protected GedcomFileChooser(JFrame frame, String title, String action) {
      super(frame,title,action,
        new String[]{"ged"}, "GEDCOM (*.ged)",
        System.getProperty("genj.gedcom.dir")
      );
    }
  } //GedcomFileChooser

  /**
   * A loader that loads Gedcoms that where open last time 
   * the application was closed
   */
  private class LastOpenLoader implements Runnable {
    public void run() {
      String[] gedcoms = registry.get("open",new String[0]);
      for (int g=0;g<gedcoms.length;g++) {
        try {
          readGedcomFrom(Origin.create(gedcoms[g]));
        } catch (MalformedURLException x) {
        }
      }
    }
  } //LastOpenLoader

  /**
   * Our ActionDelegate
   */
  public class Delegate extends ActionDelegate {
    
    /**
     * about
     */
    public void about() {
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

    /**
     * help
     */
    public void help() {
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

    /**
     * close
     */
    public void exit() {
  
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

    /**
     * open
     */
    public void open() {
  
      // ... ask for way to open
  
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
  
      switch (rc) {
  
      case 0: {
        // ---------------------- NEW GEDCOM -----------------------------
        FileChooser chooser = new GedcomFileChooser(
          frame,
          App.resources.getString("cc.create.title"),
          App.resources.getString("cc.create.action")
        );
  
        if (FileChooser.APPROVE_OPTION != chooser.showDialog()) {
          return;
        }
  
        final File file = chooser.getSelectedFile();
        if (file==null) {
          return;
        }
  
        if (file.exists()) {
  
        rc = JOptionPane.showConfirmDialog(
          frame,
          App.resources.getString("cc.open.file_exists"),
          App.resources.getString("cc.create.title"),
          JOptionPane.YES_NO_OPTION
        );
  
        if (rc==JOptionPane.NO_OPTION)
          return;
        }
  
        Origin origin;
        try {
          origin = Origin.create(new URL("file","",file.getAbsolutePath()));
        } catch (MalformedURLException e) {
          return;
        }
  
        addGedcom(new Gedcom(origin));
        return;
      }
  
      case 1: {
        // ---------------------- CHOOSE FILE ----------------------------
        FileChooser chooser = new GedcomFileChooser(
          frame,
          App.resources.getString("cc.open.title"),
          App.resources.getString("cc.open.action")
        );
  
        if (FileChooser.APPROVE_OPTION != chooser.showDialog()) {
          return;
        }
  
        final File file = chooser.getSelectedFile();
        if (file==null) {
          return;
        }
  
        Origin origin;
        try {
          origin = Origin.create(new URL("file","",file.getAbsolutePath()));
        } catch (MalformedURLException e) {
          return;
        }
  
        // Read !
        readGedcomFrom(origin);
  
        // Done
        return;
      }
  
      case 2: {
  
        // -------------------- CHOOSE URL ----------------------------------
        Vector    vUrls  = registry.get("urls", new Vector());
        JLabel    lEnter = new JLabel(App.resources.getString("cc.open.enter_url"));
        JComboBox cEnter = new JComboBox(vUrls);
        cEnter.setEditable(true);
        Object message[] = { lEnter, cEnter };
        Object options[] = { App.resources.getString("app.ok"), App.resources.getString("app.cancel") };
  
        rc = JOptionPane.showOptionDialog(
          frame,
          message,
          App.resources.getString("cc.open.title"),
          JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
          options,options[0]
        );
  
        String item=(String)cEnter.getEditor().getItem();
  
        if ((rc!=JOptionPane.OK_OPTION)||(item.length()==0)) {
          return;
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
          return;
        }
  
        // Remember URLs
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
  
        // Read from URL
        readGedcomFrom(origin);
  
        // .. Done
        return;
      }
  
      default:
        // --------------------------------- UNKNOWN ---------------------------
      }
  
    }

    /**
     * delete
     */
    public void delete() {
  
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
  
    /**
     * save
     */
    public void save() {
      save(false);
    }
  
    /**
     * saveas
     */
    public void saveas() {
      save(true);
    }
    
    /**
     * save
     */
    private void save(boolean ask) {
      
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

    /**
     * close
     */
    public void close() {
  
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
      ViewBridge.getInstance().closeAll(gedcom);
  
      // Close instance
      gedcom.close();
  
      // Done
    }
  
    /**
     * merge
     */
    public void merge() {
  
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

    /**
     * verify
     */
    public void verify() {
  
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
    
    /**
     * settings
     */
    public void settings() {
      ViewEditor.startEditing(null,"");
    }
    
    /**
     * create
     */
    private void create(int which) {
  
      ImgIcon img;
      int type;
      
      switch (which) {
        case Gedcom.INDIVIDUALS:
          type = OptionNewEntity.INDIVIDUAL;
          img  = Images.imgNewIndi;
          break;
        case Gedcom.FAMILIES:
          type = OptionNewEntity.FAMILY;
          img  = Images.imgNewFam;
          break;
        case Gedcom.MULTIMEDIAS:
          type = OptionNewEntity.MULTIMEDIA;
          img  = Images.imgNewMedia;
          break;
        case Gedcom.NOTES:
          type = OptionNewEntity.NOTE;
          img  = Images.imgNewNote;
          break;
        default:
          return;
      }
  
      JFrame frame = App.getInstance().createFrame(
        App.resources.getString("cc.title.create_entity")+" - "+Gedcom.getNameFor(which,false),
        img,
        Gedcom.getTagFor(which),
        null
      );
  
      OptionNewEntity option = new OptionNewEntity(frame,type,tGedcoms.getAllGedcoms(),tGedcoms.getSelectedGedcom());
      frame.getContentPane().add(option);
      frame.pack();
      frame.show();
  
      // Done
    }

    /**
     * fallback - one of the views?
     */
    public void fallback(String action) {
      
      // Current Gedcom
      final Gedcom gedcom = tGedcoms.getSelectedGedcom();
      if (gedcom==null) return;

      // One of the views?
      if (action.startsWith(VIEW_PREFIX)) {
        action = action.substring(VIEW_PREFIX.length());
        ViewBridge.Descriptor[] ds = ViewBridge.getInstance().getDescriptors();
        for (int d=0; d<ds.length; d++) {
          if (ds[d].key.equals(action)) {
            // Create new View
            JFrame view = ViewBridge.getInstance().open(ds[d],gedcom);
            if (view!=null) { view.pack(); view.show(); }
            // Done
            break;
          }
        }
        return;
      }
      
      // Create an entity?
      if (action.startsWith(CREATE_PREFIX)) {
        action = action.substring(CREATE_PREFIX.length());
        create(Integer.parseInt(action));
        return;
      }
      
      // done and ignored
    }    

  } //ActionDelegate
}
