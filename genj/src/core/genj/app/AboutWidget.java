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
import genj.lnf.LnFBridge;
import genj.util.ActionDelegate;
import genj.util.EnvironmentChecker;
import genj.util.GridBagHelper;
import genj.util.swing.ButtonHelper;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * AboutDialog 
 */
public class AboutWidget extends JPanel{
  
  private final static int DEFAULT_ROWS = 16, DEFAULT_COLS = 40;
  
  /** the frame we're used it */
  private JFrame frame;
  
  /**
   * Constructor
   */
  public AboutWidget(JFrame setFrame) {

    // remember    
    frame=setFrame;
    
    // create a north panel
    JLabel pNorth = new JLabel(App.resources.getString("cc.about.dialog.northpanel.label"), null, JLabel.CENTER);
    
    // create a south panel
    JPanel pSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton bExit = new ButtonHelper().setResources(App.resources).create(new ActionDelegate.ActionDisposeFrame(setFrame).setText("view.close"));
    pSouth.add(bExit);

    // create a center panel
    JTabbedPane pCenter = new JTabbedPane(SwingConstants.LEFT);
    pCenter.addTab(App.resources.getString("cc.about.dialog.tab1.title"), null, new WelcomePanel(), App.resources.getString("cc.about.dialog.tab1.title.tip"));
    pCenter.addTab(App.resources.getString("cc.about.dialog.tab2.title"), null, new AuthorsPanel(), App.resources.getString("cc.about.dialog.tab2.title.tip"));
    pCenter.addTab(App.resources.getString("cc.about.dialog.tab3.title"), null, new CopyrightPanel(), App.resources.getString("cc.about.dialog.tab3.title.tip"));
    pCenter.addTab(App.resources.getString("cc.about.dialog.tab4.title"), null, new LookNFeelPanel(), App.resources.getString("cc.about.dialog.tab4.title.tip"));

    // create the main panel    
    setLayout(new BorderLayout());
    add(pNorth , BorderLayout.NORTH );
    add(pCenter, BorderLayout.CENTER);
    add(pSouth , BorderLayout.SOUTH );
    
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
      
      readTextFile(text, path, App.resources.getString("cc.about.file_missing.text") + path);

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
      JTextArea text = new JTextArea("\n" + App.resources.getString("cc.about.tab1.text1") +"\n\n",DEFAULT_ROWS,DEFAULT_COLS);
      text.setBorder(new EmptyBorder(3, 3, 3, 3));    
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setEditable(false);
    
      // the version
      JLabel version = new JLabel(App.resources.getString("app.title")+" "+Version.getInstance().toString());
    
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
      
      JTextArea text = new JTextArea(App.resources.getString("cc.about.tab3.text1"),3,DEFAULT_COLS);
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setEditable(false);
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(App.resources.getString("cc.about.tab3.text1.title")),
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
      readTextFile(text, path, App.resources.getString("cc.about.file_missing.text") + path);
      text.setLineWrap(false);
      text.setEditable(false);
      text.setBorder(new EmptyBorder(3, 3, 3, 3));
      
      // a scroller
      JScrollPane scroll = new JScrollPane(text);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scroll.setBorder(BorderFactory.createTitledBorder(App.resources.getString("cc.about.tab3.text2.title")));
      
      // done
      return scroll;
    }
  
  } // CopyrightPanel
  
  /**
   * Panel - Look&Feel
   */
  public class LookNFeelPanel extends JPanel {

    /** the combobox with lnfs & themes*/
    private JComboBox comboLnfs,comboThemes;
    
    /**
     * Constructor
     */
    protected LookNFeelPanel() {
      super(new BorderLayout());
      add(getCenter(),BorderLayout.CENTER);
      add(getSouth (),BorderLayout.SOUTH );
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
        LnFBridge.LnF lnf = (LnFBridge.LnF)comboLnfs.getSelectedItem();
        if (lnf==null) return;
        App.getInstance().setLnF(lnf,(LnFBridge.Theme)comboThemes.getSelectedItem());
      }
    }
    
    /**
     * Update the LnF selection
     */
    private class ActionUpdate extends ActionDelegate {
      public void execute() {
        LnFBridge.LnF lnf = (LnFBridge.LnF)comboLnfs.getSelectedItem();
        if (lnf==null) return; // shouldn't be but old Swing might
        LnFBridge.Theme[] themes = lnf.getThemes();
        if (themes.length==0) {
          comboThemes.setModel(new DefaultComboBoxModel());
          comboThemes.disable();
        } else {
          comboThemes.setModel(new DefaultComboBoxModel(themes));
          comboThemes.setSelectedItem(lnf.getLastTheme());
          comboThemes.enable();
        }
      }
    }
    
    /**
     * Center Panel
     */
    private JComponent getCenter() {
      
      // what are the LnFs
      Object[] lnfs = LnFBridge.getInstance().getLnFs();
      if (lnfs.length==0) {
        return new JLabel(
          App.resources.getString("cc.about.tab4.l&f_file_missing"),
          (Icon)UIManager.get( "OptionPane.errorIcon"),
          SwingConstants.CENTER
        );
      }
      
      // create a combo with LnFs      
      comboThemes = new JComboBox();
      
      comboLnfs = new JComboBox(new DefaultComboBoxModel(lnfs));
      comboLnfs.setSelectedItem(LnFBridge.getInstance().getLastLnF());
      comboLnfs.addActionListener((ActionListener)new ActionUpdate().as(ActionListener.class));
      
      // layout
      JPanel pResult = new JPanel();
      GridBagHelper gh = new GridBagHelper(pResult);
      gh.add(new JLabel("Look&Feel"), 0,0,1,1);
      gh.add(comboLnfs              , 1,0,1,1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
      gh.add(new JLabel("Theme"    ), 0,1,1,1);
      gh.add(comboThemes            , 1,1,1,1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
      
      // show status
      new ActionUpdate().execute();
      
      // done
      return pResult;
    }
    
    /**
     * South Panel
     */
    private JPanel getSouth() {
      
      // apply-button
      JButton bOk = new ButtonHelper().setResources(App.resources).create(new ActionApply());
      
      // layout
      JPanel pResult = new JPanel();
      pResult.add(bOk);
      
      // done
      return pResult;
    }
    
  } // LookNFeelPanel

}
