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
public class ControlCenter extends JPanel implements ActionListener {

  /** members */
  private GedcomTable tGedcoms;
  private JButton bNewIndi,bNewFam,bNewMedia,bNewNote,bNewSource,bNewSubmitter,bNewRepository,bDelEntity,bUndo;
  private JButton bOpenGedcom,bNewTable,bNewTree,bNewTimeline,bNewEdit,bNewReport,bNewNavigator,bSettings;
  private JFrame frame;
  private Vector busyGedcoms;
  private ControlCenter me;
  private JMenu gedcomMenu,toolMenu,helpMenu;
  private Registry registry;
  private HelpBridge helpBridge;

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
        shutdown();
      }
    });

    // Table of Gedcoms
    tGedcoms = new GedcomTable();
    tGedcoms.setColumnWidths(registry.get("columns",(int[])null));

    // ... Listening
    awtx.table.TableListener tlistener = new awtx.table.TableListener() {
      // LCD
      /** callback in case that a row has been selected */
      public void rowSelectionChanged(int[] rows) {
        // Calc Gedcom
        int row = tGedcoms.getSelectedRow();
        Gedcom gedcom = (row < 0 ? null : tGedcoms.getGedcom(row));

        // Check if action on selected item is possible
        boolean on = (gedcom!=null);

        // Switch on/off Buttons
        bNewTable   .setEnabled(on);
        bNewTree    .setEnabled(on);
        bNewTimeline.setEnabled(on);
        bNewEdit    .setEnabled(on);
        bNewReport  .setEnabled(on);
        bNewIndi    .setEnabled(on);
        bNewFam     .setEnabled(on);
        bNewMedia   .setEnabled(on);
        bNewNote    .setEnabled(on);
        bDelEntity  .setEnabled(on);

        // Switch on/off menu
        if (gedcomMenu!=null) {

          JMenuItem item;
          for (int i=0;i<gedcomMenu.getItemCount();i++) {
          item = gedcomMenu.getItem(i);
          if (item==null)
            continue;
          if (item.getActionCommand().equals("CLOSE")
             ||item.getActionCommand().equals("SAVE")
             ||item.getActionCommand().equals("SAVEAS"))
            item.setEnabled(on);
          }
        }
        // Done
      }
      /** callback in case of a double click or enter */
      public void actionPerformed(int row) {
      }
      // EOC
    };
    tGedcoms.addTableListener(tlistener);

    // We handle the PopupProcess
    awtx.table.PopupProvider provider = new awtx.table.PopupProvider() {
      // LCD
      /** How to provide a popup for Table */
      public void providePopup(Component c, int x, int y) {
        // Gedcom
        Gedcom gedcom = tGedcoms.getSelectedGedcom();
        if (gedcom==null)
          return;
        // Create popup
        JPopupMenu jpopup = new JPopupMenu();
        // Add our actions
        boolean busy=busyGedcoms.contains(gedcom);
        addItemTo(jpopup,App.resources.getString("cc.menu.save"  ),"SAVE"   ,!busy);
        addItemTo(jpopup,App.resources.getString("cc.menu.saveas"),"SAVEAS" ,!busy);
        addItemTo(jpopup,App.resources.getString("cc.menu.close" ),"CLOSE"  ,!busy);
        // Show it
        jpopup.show(c,x,y);
        // Done
      }
      /** Helper for menu actions */
      private void addItemTo(JPopupMenu menu,String text,String action,boolean enabled) {
        // create entry
        JMenuItem mi = new JMenuItem(text);
        mi.setEnabled(enabled);
        mi.setActionCommand(action);
        mi.addActionListener(me);
        menu.add(mi);
      }
      // EOC
    };
    tGedcoms.setPopupProvider(provider);

    // Create Pane for buttons
    JPanel gedcomPane = new JPanel();
    gedcomPane.setLayout(new BoxLayout(gedcomPane,BoxLayout.X_AXIS));

    // .. Buttons
    bOpenGedcom  = createButton(Images.imgGedcom      ,"OPEN"        ,"cc.tip.open_file"    ,true );
    bNewTable    = createButton(Images.imgNewTable    ,"NEWTABLE"    ,"cc.tip.open_table"   ,false);
    bNewTree     = createButton(Images.imgNewTree     ,"NEWTREE"     ,"cc.tip.open_tree"    ,false);
    bNewTimeline = createButton(Images.imgNewTimeline ,"NEWTIMELINE" ,"cc.tip.open_timeline",false);
    bNewEdit     = createButton(Images.imgNewEdit     ,"NEWEDIT"     ,"cc.tip.open_edit"    ,false);
    bNewReport   = createButton(Images.imgNewReport   ,"NEWREPORT"   ,"cc.tip.open_report"  ,false);
    bNewNavigator= createButton(Images.imgNewNavigator,"NEWNAVIGATOR","cc.tip.open_navigator",true );
    bSettings    = createButton(Images.imgSettings    ,"VIEWEDIT"    ,"cc.tip.settings"     ,true );

    // .. Layout
    gedcomPane.add(bOpenGedcom);
    gedcomPane.add(bNewTable   );
    gedcomPane.add(bNewTree    );
    gedcomPane.add(bNewTimeline);
    gedcomPane.add(bNewEdit    );
    gedcomPane.add(bNewReport  );
    gedcomPane.add(bNewNavigator);
    gedcomPane.add(bSettings   );

    // Actions Pane
    JPanel entityPane = new JPanel();
    entityPane.setLayout(new BoxLayout(entityPane,BoxLayout.X_AXIS));

    // .. Buttons
    bNewIndi      = createButton(Images.imgNewIndi      ,"NEWINDI"      ,"cc.tip.create_indi"       ,false);
    bNewFam       = createButton(Images.imgNewFam       ,"NEWFAM"       ,"cc.tip.create_fam"        ,false);
    bNewMedia     = createButton(Images.imgNewMedia     ,"NEWMEDIA"     ,"cc.tip.create_media"      ,false);
    bNewNote      = createButton(Images.imgNewNote      ,"NEWNOTE"      ,"cc.tip.create_note"       ,false);
    bNewSource    = createButton(Images.imgNewSource    ,"NEWSOURCE"    ,"cc.tip.create_source"     ,false);
    bNewSubmitter = createButton(Images.imgNewSubmitter ,"NEWSUBMITTER" ,"cc.tip.create_submitter"  ,false);
    bNewRepository= createButton(Images.imgNewRepository,"NEWREPOSITORY","cc.tip.create_repository" ,false);
    bDelEntity    = createButton(Images.imgDelEntity    ,"DEL"          ,"cc.tip.delete_entity"     ,false);
    //    bUndo        = createButton(Images.imgUndo        ,"UNDO"        ,"cc.tip.undo_change"      ,false);

    entityPane.add(bNewIndi     );
    entityPane.add(bNewFam      );
    entityPane.add(bNewMedia    );
    entityPane.add(bNewNote     );
    entityPane.add(bNewSource   );
    entityPane.add(bNewSubmitter);
    entityPane.add(bNewRepository);
    entityPane.add(bDelEntity   );
    //    entityPane.add(bUndo        );

    // Layout
    setLayout(new BorderLayout());
    add(gedcomPane ,"North");
    add(tGedcoms    ,"Center");
    add(entityPane ,"South");

    // Done
  }
  
  /**
   * Action About
   */
  private void actionAbout() {
    
    // know the frame already?
    JFrame frame = App.getInstance().getFrame("about");
    if (frame==null) {
      // create it
      frame = App.getInstance().createFrame(App.resources.getString("cc.title.about"),null,"about",null);
      frame.getContentPane().add(new AboutDialog(frame,this));
    }
    frame.pack();
    frame.show();

    // done      
  }

  /**
   * Action closing Gedcom
   */
  private void actionCloseGedcom(Gedcom gedcom) {

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
   * Action for DELETEENTITY
   */
  private void actionDelEntity() {

    // Setup delete dialog
    JFrame frame = App.getInstance().createFrame(
      App.resources.getString("cc.title.delete_entity"),
      Images.imgDelEntity,
      "delentity",
      null
    );

    OptionDelEntity option = new OptionDelEntity(frame,tGedcoms.getGedcoms(),tGedcoms.getSelectedGedcom());
    frame.getContentPane().add(option);
    frame.pack();
    frame.show();

    // Done
  }

  /**
   * This method gets triggered in case of a button press
   */
  public void actionHelp() {

    if (helpBridge!=null) {
      helpBridge.open(registry);
      return;
    }

    try {
      HelpBridge h = (HelpBridge)Class.forName("genj.app.HelpBridgeImpl").newInstance();
      h.open(registry);
      helpBridge = h;
    } catch (Throwable e) {
      String m = e.getMessage()+"@"+e.getClass().getName();
      System.out.println("Couldn't build a bridge to Java Help - make sure the setup correctly includes jhbasic.jar in the CLASSPATH ("+m+")");
    }

  }

  /**
   * Action started for MERGE
   */
  private void actionMerge(Gedcom gedcom) {

    // Setup merge dialog
    JFrame frame = App.getInstance().createFrame(
      App.resources.getString("cc.title.merge_gedcoms"),
      Images.imgGedcom,
      "merge",
      null
    );

    Transaction transaction = new MergeTransaction(gedcom, tGedcoms.getGedcoms(),this);

    TransactionPanel tpanel = new TransactionPanel(frame,transaction);
    frame.getContentPane().add(tpanel);

    frame.pack();
    frame.show();

    // Done
  }

  /**
   * Action for NEWENTITY
   */
  private void actionNewEntity(String which) {

    ImgIcon img;
    int type;
    while (true) {

      if (which.equals("NEWINDI")) {
        type = OptionNewEntity.INDIVIDUAL;
        img  = Images.imgNewIndi;
        break;
      }
      if (which.equals("NEWFAM")) {
        type = OptionNewEntity.FAMILY;
        img  = Images.imgNewFam;
        break;
      }
      if (which.equals("NEWMEDIA")) {
        type = OptionNewEntity.MULTIMEDIA;
        img  = Images.imgNewMedia;
        break;
      }
      if (which.equals("NEWNOTE")) {
        type = OptionNewEntity.NOTE;
        img  = Images.imgNewNote;
        break;
      }
      // Hmm - shouldn't be
      return;
    }

    JFrame frame = App.getInstance().createFrame(
      App.resources.getString("cc.title.create_entity"),
      img,
      which.toLowerCase(),
      null
    );

    OptionNewEntity option = new OptionNewEntity(frame,type,tGedcoms.getGedcoms(),tGedcoms.getSelectedGedcom());
    frame.getContentPane().add(option);
    frame.pack();
    frame.show();

    // Done
  }

  /**
   * Action started for OPEN
   */
  private void actionOpenGedcom() {

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
      FileChooser chooser = createFileChooser(
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

      registerGedcom(new Gedcom(origin));
      return;
    }

    case 1: {
      // ---------------------- CHOOSE FILE ----------------------------
      FileChooser chooser = createFileChooser(
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
   * This method gets triggered in case of a button press
   */
  public void actionPerformed(ActionEvent e) {

    // HELP CONTENTS?
    if (e.getActionCommand().equals("CONTENTS")) {
      actionHelp();
      return;
    }

    // ABOUT?
    if (e.getActionCommand().equals("ABOUT")) {
      actionAbout();
      return;
    }

    // CLOSE ?
    if (e.getActionCommand().equals("EXIT")) {
      shutdown();
      return;
    }

    // New Gedcom ?
    if (e.getActionCommand().equals("OPEN")) {
      actionOpenGedcom();
      return;
    }

     // Delete Entity ?
    if (e.getActionCommand().equals("DEL")) {
      actionDelEntity();
      return;
    }

    // New Entity ?
    if ( (e.getActionCommand().equals("NEWINDI" ))
       ||(e.getActionCommand().equals("NEWFAM"  ))
       ||(e.getActionCommand().equals("NEWNOTE" ))
       ||(e.getActionCommand().equals("NEWMEDIA")) ) {

      actionNewEntity(e.getActionCommand());
      return;
    }

    // Calculate Gedcom for Action
    Gedcom gedcom=tGedcoms.getSelectedGedcom();
    if (gedcom!=null) {
      // .. busy ?
      if (busyGedcoms.contains(gedcom)) {
        return;
      }
    }

    // Merge ?
    if (e.getActionCommand().equals("MERGE")) {
      actionMerge(gedcom);
      return;
    }
    // Verify ?
    if (e.getActionCommand().equals("VERIFY")) {
      actionVerify(gedcom);
      return;
    }

    // From here on we need a valid Gedcom
    if (gedcom==null) {
      return;
    }

    // New Properties View ?
    if (e.getActionCommand().equals("NEWEDIT")) {
      actionOpenView(ViewBridge.EDIT,gedcom);
      return;
    }
    // New Table View ?
    if (e.getActionCommand().equals("NEWTABLE")) {
      actionOpenView(ViewBridge.TABLE,gedcom);
      return;
    }
    // New Tree View ?
    if (e.getActionCommand().equals("NEWTREE")) {
      actionOpenView(ViewBridge.TREE,gedcom);
      return;
    }
    // New timeline View ?
    if (e.getActionCommand().equals("NEWTIMELINE")) {
      actionOpenView(ViewBridge.TIMELINE,gedcom);
      return;
    }
    // New report View?
    if (e.getActionCommand().equals("NEWREPORT")) {
      actionOpenView(ViewBridge.REPORT,gedcom);
      return;
    }
    // New navigator View?
    if (e.getActionCommand().equals("NEWNAVIGATOR")) {
      actionOpenView(ViewBridge.NAVIGATOR,gedcom);
      return;
    }
    // Save As ?
    if (e.getActionCommand().equals("SAVEAS")) {
      actionSave(gedcom,true);
      return;
    }
    // Save ?
    if (e.getActionCommand().equals("SAVE")) {
      actionSave(gedcom,false);
      return;
    }
    // Close ?
    if (e.getActionCommand().equals("CLOSE")) {
      actionCloseGedcom(gedcom);
      return;
    }
    // Settings?
    if (e.getActionCommand().equals("VIEWEDIT")) {
      actionViewEdit();
      return;
    }
    // Unknown !
  }

  /**
   * Action started for SAVE
   */
  private void actionSave(final Gedcom gedcom, boolean ask) {

    // Dialog ?
    Origin origin = gedcom.getOrigin();
    File file;
    if ( (ask) || (origin==null) || (!origin.isFile()) ) {

      // .. choose file
      FileChooser chooser = createFileChooser(
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
   * Action started for VERIFY
   */
  private void actionVerify(Gedcom gedcom) {

    // Setup verify dialog
    JFrame frame = App.getInstance().createFrame(
      App.resources.getString("cc.title.verify_gedcom"),
      Images.imgGedcom,
      "verify",
      null
    );

    Transaction transaction = new VerifyTransaction(gedcom, tGedcoms.getGedcoms());

    TransactionPanel tpanel = new TransactionPanel(frame,transaction);
    frame.getContentPane().add(tpanel);
    frame.pack();
    frame.show();

    // Done
  }

  /**
   * Action started for VIEWEDIT
   */
  private void actionViewEdit() {

    // Open?
    JFrame frame = App.getInstance().getFrame("settings");
    if (frame!=null) {
      frame.dispose();
      return;
    }

    // Setup editor dialog
    frame = App.getInstance().createFrame(
      App.resources.getString("cc.title.settings_edit"),
      Images.imgGedcom,
      "settings",
      null
    );

    ViewEditor editor = new ViewEditor(frame);

    frame.getContentPane().add(editor);
    frame.pack();
    frame.show();

    // Done
  }

  /**
   * Helper method for menu
   */
  private void addItemTo(JMenu menu,String text,String action) {
    JMenuItem mi = new JMenuItem(text);
    mi.setActionCommand(action);
    mi.addActionListener(this);
    menu.add(mi);
  }

  /**
   * Called when component is first shown
   */
  public void addNotify() {

    super.addNotify();

    // Load known gedcoms
    SwingUtilities.invokeLater(new Runnable() {
      // LCD
      /** main */
      public void run() {
        loadLastOpen();
      }
      // EOC
    });

    // Done
  }

  /**
   * Closes all frame created by this controlcenter
   */
  private boolean shutdown() {

    // Unsaved changes ?
    Enumeration gedcoms = tGedcoms.getGedcoms().elements();
    while (gedcoms.hasMoreElements()) {
      Gedcom g = (Gedcom)gedcoms.nextElement();
      if (g.hasUnsavedChanges()) {
        int rc = JOptionPane.showConfirmDialog(
          frame,
          App.resources.getString("cc.exit_changes?"),
          App.resources.getString("app.warning"),
          JOptionPane.YES_NO_OPTION
        );
        if (rc==JOptionPane.NO_OPTION) {
          return false;
        }
        break;
      }
    }
    
    // Tell the HelpBridge
    if (helpBridge!=null) {
      helpBridge.close(registry);
    }

    // Save settings from GedcomTable
    registry.put("columns",tGedcoms.getColumnWidths());

    // Tell it to the app
    App.getInstance().shutdown();

    // Done
    return true;
  }

  /**
   * Another helper which creates a JButton
   */
  private JButton createButton(ImgIcon img,String action, String tip,boolean enabled) {
    JButton b = new JButton(ImgIconConverter.get(img));
    b.setToolTipText(App.resources.getString(tip));
    b.addActionListener(this);
    b.setActionCommand(action);
    b.setEnabled(enabled);
    b.setMargin(new Insets(4,4,4,4));
    b.setRequestFocusEnabled(false);
    return b;
  }

  /**
   * Returns a menu for frame showing this controlcenter
   */
  /*package*/ JMenuBar getMenu() {

    // Create Menues
    if (gedcomMenu==null) {

      gedcomMenu = new JMenu(App.resources.getString("cc.menu.file"));

      // Create Items
      addItemTo(gedcomMenu,App.resources.getString("cc.menu.open"  ) ,"OPEN" );
      gedcomMenu.addSeparator();
      addItemTo(gedcomMenu,App.resources.getString("cc.menu.save"  ) ,"SAVE"  );
      addItemTo(gedcomMenu,App.resources.getString("cc.menu.saveas") ,"SAVEAS");
      addItemTo(gedcomMenu,App.resources.getString("cc.menu.close" ) ,"CLOSE" );
      gedcomMenu.addSeparator();
      addItemTo(gedcomMenu,App.resources.getString("cc.menu.exit"  ) ,"EXIT" );

    }

    if (toolMenu==null) {

      toolMenu = new JMenu(App.resources.getString("cc.menu.tools"  ));

      addItemTo(toolMenu,App.resources.getString("cc.menu.merge" ) ,"MERGE" );
      addItemTo(toolMenu,App.resources.getString("cc.menu.verify") ,"VERIFY");

    }

    if (helpMenu==null) {
      helpMenu = new JMenu(App.resources.getString("cc.menu.help"  ));
      addItemTo(helpMenu,App.resources.getString("cc.menu.contents"),"CONTENTS");
      addItemTo(helpMenu,App.resources.getString("cc.menu.about"),"ABOUT");
    }

    // Create JMenuBar
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(gedcomMenu);
    menuBar.add(toolMenu);
    menuBar.add(Box.createHorizontalGlue());
    menuBar.add(helpMenu);

    // Done
    return menuBar;
  }

  /**
   * Loads gedcoms that were open last time cc was shown
   */
  private void loadLastOpen() {

    String[] gedcoms = registry.get("open",new String[0]);
    for (int g=0;g<gedcoms.length;g++) {

      Origin origin;
      try {
        origin = Origin.create(gedcoms[g]);
      } catch (MalformedURLException x) {
        continue;
      }
      readGedcomFrom(origin);
    }

  }

  /**
   * Open a specific view
   */
  private void actionOpenView(int which, Gedcom gedcom) {

    // Create new View
    JFrame view = ViewBridge.getInstance().open(which,gedcom);
    if (view==null) return;
    view.pack();
    view.show();

    // Done
  }

  /**
   * Read Gedcom from URL
   */
  private void readGedcomFrom(Origin origin) {

    // Check if already open
    Vector gedcoms = tGedcoms.getGedcoms();
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
          // .. read it
          Gedcom newGedcom = gedReader.readGedcom();
          // .. done
          registerGedcom(newGedcom);
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
   * Includes the given Gedcom to be ready for working on
   */
  public void registerGedcom(Gedcom gedcom) {
    tGedcoms.addGedcom(gedcom);
  }

  /**
   * Called when component is dismissed
   */
  public void removeNotify() {

    // Remember open gedcoms
    Vector gedcoms = tGedcoms.getGedcoms();
    Vector save = new Vector(gedcoms.size());
    Gedcom gedcom;
    for (int i=0;i<gedcoms.size();i++) {
      gedcom = (Gedcom)gedcoms.elementAt(i);
      if (gedcom.getOrigin()!=null) {
        save.addElement(""+gedcom.getOrigin());
      }
    }
    registry.put("open",save);

    // Done
    super.removeNotify();
  }
  
  /**
   * Creates a FileChooser for given arguments
   */
  private FileChooser createFileChooser(JFrame frame, String title, String action) {
   
    return new FileChooser(
      frame,
      title,
      action,
      new String[]{"ged"},
      "GEDCOM (*.ged)",
      System.getProperty("genj.gedcom.dir")
    );
  }
}
