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
 *
 *
 * AboutDialog class
 * This class creates the content of AboutDialog application
 * $Header: /cvsroot/genj/dev/src/core/genj/app/AboutDialog.java,v 1.11 2002/08/15 07:17:23 island1 Exp $
 * @author Francois Massonneau <frmas@free.fr>
 * @version 1.0
 *
 */
 
package genj.app;

import genj.Version;
import genj.io.FileAssociation;
import genj.lnf.LnFBridge;
import genj.util.ActionDelegate;
import genj.util.EnvironmentChecker;
import genj.util.GridBagHelper;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.ScreenResolutionScale;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 * AboutDialog 
 */
public class AboutWidget extends JPanel{
  
  private final static int DEFAULT_ROWS = 16, DEFAULT_COLS = 40;
  
  /** the view manager */
  private ViewManager viewManager;
  
  /** the resources we're using */
  private Resources resources = Resources.get(AboutWidget.class);
  
  /**
   * Constructor
   */
  public AboutWidget(ViewManager setManager) {

    // remember    
    viewManager = setManager;
    
    // create a north panel
    JLabel pNorth = new JLabel(resources.getString("cc.about.dialog.northpanel.label"), null, JLabel.CENTER);
    
    // create a center panel
    JTabbedPane pCenter = new JTabbedPane(SwingConstants.LEFT);
    pCenter.addTab(resources.getString("cc.about.dialog.tab1.title"), null, new WelcomePanel(), resources.getString("cc.about.dialog.tab1.title.tip"));
    pCenter.addTab(resources.getString("cc.about.dialog.tab2.title"), null, new AuthorsPanel(), resources.getString("cc.about.dialog.tab2.title.tip"));
    pCenter.addTab(resources.getString("cc.about.dialog.tab3.title"), null, new CopyrightPanel(), resources.getString("cc.about.dialog.tab3.title.tip"));
    pCenter.addTab(resources.getString("cc.about.dialog.tab4.title"), null, new LookNFeelPanel(), resources.getString("cc.about.dialog.tab4.title.tip"));
    pCenter.addTab(resources.getString("cc.about.dialog.tab5.title"), null, new AssociationPanel(), resources.getString("cc.about.dialog.tab5.title.tip"));

    // create the main panel    
    setLayout(new BorderLayout());
    add(pNorth , BorderLayout.NORTH );
    add(pCenter, BorderLayout.CENTER);
    
    // done    
  }

  /**
   * Helper to read text from a file
   */
  protected void readTextFile(JTextArea ta, String file, String fallback) {
    try {
      FileInputStream fin = new FileInputStream(file);
      Reader in = new InputStreamReader(fin);
      ta.read(in,null);
      fin.close();
    }
    catch (Exception e) {
      ta.setText(fallback);
    }
  }

  /**
   * Panel - Authors
   */  
  private class AuthorsPanel extends JScrollPane {

    /** 
     * Constructor
     */  
    protected AuthorsPanel() {

      // create contained text area
      JTextArea text = new JTextArea(DEFAULT_ROWS,DEFAULT_COLS);
      text.setLineWrap(false);
      text.setWrapStyleWord(true);
      text.setEditable(false);

      String dir = EnvironmentChecker.getProperty(
        this,
        new String[]{ "user.dir" },
        ".",
        "get authors.txt"
      );
      
      String path = dir + File.separatorChar + "doc" + File.separatorChar + "authors.txt";
      
      readTextFile(text, path, resources.getString("cc.about.file_missing.text") + path);

      // setup looks
      setViewportView(text);      
      
      setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      // done
      
    }      
  } // AuthorsPanel

  /**
   * Panel - Welcome
   */  
  private class WelcomePanel extends JPanel  {

    /**
     * Constructor
     */
    protected WelcomePanel() {
      
      super(new BorderLayout());
        
      // the text
      JTextArea text = new JTextArea("\n" + resources.getString("cc.about.tab1.text1") +"\n\n",DEFAULT_ROWS,DEFAULT_COLS);
      text.setBorder(new EmptyBorder(3, 3, 3, 3));    
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setEditable(false);
    
      // the version
      JLabel version = new JLabel(resources.getString("app.title")+" "+Version.getInstance().toString());
    
      // looks
      add(text, BorderLayout.CENTER);
      add(version, BorderLayout.SOUTH);
      
    }
    
  } // WelcomePanel
  
  /**
   * Panel - Copyright
   */  
  private class CopyrightPanel extends JPanel {

    /** 
     * Constructor
     */  
    protected CopyrightPanel()  {
      
      super(new BorderLayout());
      
      add(getNorth(), BorderLayout.NORTH);
      add(getCenter(), BorderLayout.CENTER);
    
    }
    
    /**
     * Create the north 
     */
    private JComponent getNorth() {
      
      JTextArea text = new JTextArea(resources.getString("cc.about.tab3.text1"),3,DEFAULT_COLS);
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setEditable(false);
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(resources.getString("cc.about.tab3.text1.title")),
        new EmptyBorder(3, 3, 3, 3)
      ));
      panel.add(text, BorderLayout.CENTER);
      
      return panel;
    }

    /**
     * Create the center
     */
    private JComponent getCenter() {
          
      // the text    
      JTextArea text = new JTextArea(DEFAULT_ROWS,DEFAULT_COLS);
      String dir = EnvironmentChecker.getProperty(
        this,
        new String[]{ "user.dir" },
        ".",
        "read gpl.txt"
      );
      
      String path = dir + File.separatorChar + "doc" + File.separatorChar + "gpl.txt";
      readTextFile(text, path, resources.getString("cc.about.file_missing.text") + path);
      text.setLineWrap(false);
      text.setEditable(false);
      text.setBorder(new EmptyBorder(3, 3, 3, 3));
      
      // a scroller
      JScrollPane scroll = new JScrollPane(text);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scroll.setBorder(BorderFactory.createTitledBorder(resources.getString("cc.about.tab3.text2.title")));
      
      // done
      return scroll;
    }
  
  } // CopyrightPanel
  
  /**
   * Panel - Look&Feel
   */
  private class LookNFeelPanel extends JPanel implements ActionListener {

    /** the combobox with lnfs & themes*/
    private JComboBox comboLnfs,comboThemes;
    
    /** the ruler for setting screen resolution */
    private ScreenResolutionScale screenResRuler;
    
    /** combo box for language */
    private ChoiceWidget choiceLanguages;
    
    /**
     * Constructor
     */
    protected LookNFeelPanel() {
      super(new BorderLayout());
      add(getCenter(),BorderLayout.CENTER);
      add(getSouth (),BorderLayout.SOUTH );
    }
    
    /**
     * Center Panel
     */
    private JComponent getCenter() {

      // Prepare the panel      
      JPanel pResult = new JPanel();
      GridBagHelper gh = new GridBagHelper(pResult);
      gh.setParameter(gh.GROWFILL_HORIZONTAL);

      // what are the languages
      //String[] languages = {"en", "de", "es", "fr", "it", "ja"};
      String[] languages = {"en", "de", "fr", "hu"};
      choiceLanguages = new ChoiceWidget(languages, App.getInstance().getLanguage());
      choiceLanguages.setEditable(false);
      gh.add(new JLabel(resources.getString("cc.about.tab4.language")), 0, 0);
      gh.add(choiceLanguages, 1, 0);

      // what are the LnFs
      Object[] lnfs = LnFBridge.getInstance().getLnFs();
      if (lnfs.length==0) {
        JLabel label = new JLabel(
          resources.getString("cc.about.tab4.l&f_file_missing"),
          (Icon)UIManager.get( "OptionPane.errorIcon"),
          SwingConstants.CENTER
        );
        gh.add(label, 0,1,2,1);
        
      } else {
      
        // create a combo with LnFs      
        comboThemes = new JComboBox();
        
        comboLnfs = new JComboBox(new DefaultComboBoxModel(lnfs));
        comboLnfs.setSelectedItem(LnFBridge.getInstance().getLastLnF());
        comboLnfs.addActionListener(this);

        gh.add(new JLabel(resources.getString("cc.about.tab4.look&feel") ), 0,1);
        gh.add(comboLnfs               , 1,1);
        gh.add(new JLabel(resources.getString("cc.about.tab4.theme")     ), 0,2,1,1);
        gh.add(comboThemes             , 1,2);
        
        actionPerformed(null);
      }
      
      // create ruler for adjusting resolution
      screenResRuler = new ScreenResolutionScale(viewManager.getDPI());
      
      gh.add(new JLabel(resources.getString("cc.about.tab4.resolution")), 0,3);
      gh.add(screenResRuler               , 1,3);
      
      // done
      return pResult;
    }
    
    /**
     * South Panel
     */
    private JPanel getSouth() {
      // apply-button
      JPanel pResult = new JPanel();
      pResult.add(new ButtonHelper().setResources(resources).create(new ActionApply()));
      // done
      return pResult;
    }
    
    /**
     * Apply the LnF
     */
    private class ActionApply extends ActionDelegate {
      /** constructor */
      protected ActionApply() {
        setText("cc.about.tab4.button.apply");
      }
      /** run */
      public void execute() {
        App app = App.getInstance();
        // check language 
        if (choiceLanguages.hasChanged()) {
          viewManager.getWindowManager().openDialog(null, null, WindowManager.IMG_WARNING, resources.getString("cc.about.dialog.restart"), WindowManager.OPTIONS_OK, AboutWidget.this);
          app.setLanguage(choiceLanguages.getSelectedItem().toString());
        }
        // update screen resolution
        viewManager.setDPI(screenResRuler.getDPI());
        // update lnf
        if (comboLnfs!=null) {
          LnFBridge.LnF lnf = (LnFBridge.LnF)comboLnfs.getSelectedItem();
          if (lnf!=null) {
            app.setLnF(lnf,(LnFBridge.Theme)comboThemes.getSelectedItem());
          }
        }
        // done
      }
    }
    
    /**
     * Update the LnF selection
     */
    public void actionPerformed(ActionEvent e) {
      LnFBridge.LnF lnf = (LnFBridge.LnF)comboLnfs.getSelectedItem();
      if (lnf==null) return; // shouldn't be but old Swing might
      LnFBridge.Theme[] themes = lnf.getThemes();
      if (themes.length==0) {
        comboThemes.setModel(new DefaultComboBoxModel());
        comboThemes.setEnabled(false);
      } else {
        comboThemes.setModel(new DefaultComboBoxModel(themes));
        comboThemes.setSelectedItem(lnf.getLastTheme());
        comboThemes.setEnabled(true);
      }
    }
    
  } // LookNFeelPanel

  /**
   * Associations
   */
  private class AssociationPanel extends JPanel implements ListSelectionListener {
    
    /** buttons */
    private AbstractButton bAdd, bDel, bEdit;
    
    /** table */
    private JTable table;
    
    /** model */
    private Model model;
    
    /**
     * Constructor
     */
    private AssociationPanel() {
      
      // model
      model = new Model(); 
      
      // table with associations
      table = new JTable();
      table.getTableHeader().setReorderingAllowed(false);
      table.setModel(model);
      ListSelectionModel lsm = table.getSelectionModel(); 
      lsm.setSelectionMode(lsm.SINGLE_SELECTION);
      lsm.addListSelectionListener(this);
      
      // panel with actions
      JPanel panel = new JPanel();
      ButtonHelper bh = new ButtonHelper();
      bh.setContainer(panel);
      bAdd = bh.create(new Add ());
             bh.setEnabled(false);
      bDel = bh.create(new Del ());
      bEdit= bh.create(new Edit());
      
      // layout
      setLayout(new BorderLayout());
      add(new JScrollPane(table), BorderLayout.CENTER);
      add(panel                 , BorderLayout.SOUTH );
      
      // done
    }
    
    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
      boolean b = table.getSelectedRow()>=0;
      bDel .setEnabled(b);
      bEdit.setEnabled(b);
    }
    
    /**
     * Action - Add Association
     */
    private class Add extends ActionDelegate {
      /**
       * Constructor
       */
      private Add() {
        super.setText(resources.getString("cc.about.tab5.button.add"));
      }
      /**
       * @see genj.util.ActionDelegate#execute()
       */
      protected void execute() {
        int row = model.addRow("", "", "");
        table.getColumnModel().getSelectionModel().setSelectionInterval(0,0);
        table.getSelectionModel().setSelectionInterval(row,row);
        bEdit.doClick();
      }
    } //Add
    
    /**
     * Action - Del Association
     */
    private class Del extends ActionDelegate {
      /**
       * Constructor
       */
      private Del() {
        super.setText(resources.getString("cc.about.tab5.button.delete"));
      }
      /**
       * @see genj.util.ActionDelegate#execute()
       */
      protected void execute() {
        int row = table.getSelectedRow();
        if (row>=0) model.delRow(row);
      }
    } //Del
    
    /**
     * Action - Edit Association
     */
    private class Edit extends ActionDelegate {
      /**
       * Constructor
       */
      private Edit() {
        super.setText(resources.getString("cc.about.tab5.button.edit"));
      }
      /**
       * @see genj.util.ActionDelegate#execute()
       */
      protected void execute() {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        if (row>=0&&col>=0) table.editCellAt(row, col);
        table.getEditorComponent().requestFocus();
      }
    } //Edit
    
    /**
     * AssociationsModel
     */
    private class Model extends AbstractTableModel {
      
      /** instances we know about */
      List instances = new ArrayList(FileAssociation.getAll());
      
      /**
       * Removes a row
       */
      public void delRow(int row) {
        FileAssociation.del(getRow(row));
        instances = new ArrayList(FileAssociation.getAll());
        fireTableRowsDeleted(row,row);
      }
      
      /**
       * Adds a row
       */
      public int addRow(String s, String a, String e) {
        int num = instances.size();
        FileAssociation fa = new FileAssociation(s,a,e);
        FileAssociation.add(fa);
        instances.add(fa);
        fireTableRowsInserted(num,num);
        return num;
      }

      /**
       * The association at given position
       */
      public FileAssociation getRow(int row) {
        return (FileAssociation)instances.get(row);
      }
      
      /**
       * @see javax.swing.table.TableModel#getRowCount()
       */
      public int getRowCount() {
        return instances.size();
      }
      
      /**
       * @see javax.swing.table.TableModel#getColumnCount()
       */
      public int getColumnCount() {
        return 3;
      }
      /**
       * @see javax.swing.table.TableModel#getValueAt(int, int)
       */
      public Object getValueAt(int row, int col) {
        FileAssociation fa = getRow(row); 
        switch (col) { default:
          case 0: return fa.getSuffix();
          case 1: return fa.getAction();
          case 2: return fa.getExecutable();
        }
      }
      /**
       * @see javax.swing.table.AbstractTableModel#getColumnName(int)
       */
      public String getColumnName(int col) {
        switch (col) { default:
          case 0: return "Suffix";
          case 1: return "Action";
          case 2: return "Executable";
        }
      }
      /**
       * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
       */
      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
      }
      /**
       * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
       */
      public void setValueAt(Object val, int row, int col) {
        FileAssociation fa = getRow(row);
        String s = val.toString();
        switch (col) { 
          case 0: fa.setSuffix(s);    break;
          case 1: fa.setAction(s);    break;
          case 2: fa.setExecutable(s);break;
        }
        fireTableRowsUpdated(row,row);
      }

    } //Associations
    
  } //AssociationPanel
  
} //AboutWidget
