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
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/core/genj/app/AboutDialog.java,v 1.3 2002-08-08 20:18:13 nmeier Exp $
 * @author Francois Massonneau <frmas@free.fr>
 * @version 1.0
 *
 */
 
package genj.app;

import genj.Version;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * AboutDialog 
 */
public class AboutDialog {
  
  /** one static frame we keep around */
  private static JFrame frame;

  /**
   * main
   */
  public static void showAboutDialog() {
    
    // got a frame already?
    if (frame!=null) {
      frame.show();
      return;
    }
    
    // create a north panel
    JLabel pNorth = new JLabel(App.resources.getString("cc.about.dialog.northpanel.label"), null, JLabel.CENTER);
    
    // create a south panel
    JPanel pSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton bExit = new JButton(App.resources.getString("cc.about.dialog.exit"));
    bExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        frame.hide();
      }
    });
    pSouth.add(bExit);

    // create a center panel
    JTabbedPane pCenter = new JTabbedPane(SwingConstants.LEFT);
    pCenter.addTab(App.resources.getString("cc.about.dialog.tab1.title"), null, new WelcomePanel(), App.resources.getString("cc.about.dialog.tab1.title.tip"));
    pCenter.addTab(App.resources.getString("cc.about.dialog.tab2.title"), null, new AuthorsPanel(), App.resources.getString("cc.about.dialog.tab2.title.tip"));
    pCenter.addTab(App.resources.getString("cc.about.dialog.tab4.title"), null, new CopyrightPanel(), App.resources.getString("cc.about.dialog.tab4.title.tip"));
    pCenter.addTab(App.resources.getString("cc.about.dialog.tab5.title"), null, new LookNFeelPanel(), App.resources.getString("cc.about.dialog.tab5.title.tip"));

    // create the main panel    
    JPanel pMain = new JPanel(new BorderLayout());
    pMain.add(pNorth , BorderLayout.NORTH );
    pMain.add(pCenter, BorderLayout.CENTER);
    pMain.add(pSouth , BorderLayout.SOUTH );
    
    // create and show it
    frame = new JFrame(App.resources.getString("cc.about.dialog.frame"));
    frame.getContentPane().add(pMain);
    frame.setSize(480, 300);
    frame.show();

    // done    
  }

  /**
   * Helper to read text from a file
   */
  protected static void readTextFile(JTextArea ta, String file, String fallback) {
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
  private static class AuthorsPanel extends JScrollPane {

    /** 
     * Constructor
     */  
    protected AuthorsPanel() {

      // create contained text area
      JTextArea text = new JTextArea();
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setEditable(false);
      
      String path = System.getProperty("user.dir") + File.separatorChar + "doc" + File.separatorChar + "authors.txt";
      
      readTextFile(text, path, App.resources.getString("cc.about.file_missing.text") + path);

      // setup looks
      setViewportView(text);      
      
      setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

      // done
      
    }      
  } // AuthorsPanel

  /**
   * Panel - Welcome
   */  
  private static class WelcomePanel extends JPanel  {

    /**
     * Constructor
     */
    protected WelcomePanel() {
      
      super(new BorderLayout());
        
      // the text
      JTextArea text = new JTextArea("\n" + App.resources.getString("cc.about.tab1.text1") +"\n\n");
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
  private static class CopyrightPanel extends JPanel {

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
      
      JTextArea text = new JTextArea(App.resources.getString("cc.about.tab4.text1"));
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setEditable(false);
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(App.resources.getString("cc.about.tab4.text1.title")),
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
      JTextArea text = new JTextArea();
      String path = System.getProperty("user.dir") + File.separatorChar + "doc" + File.separatorChar + "gpl.txt";
      readTextFile(text, path, App.resources.getString("cc.about.file_missing.text") + path);
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setEditable(false);
      text.setBorder(new EmptyBorder(3, 3, 3, 3));
      
      // a scroller
      JScrollPane scroll = new JScrollPane(text);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      scroll.setBorder(BorderFactory.createTitledBorder(App.resources.getString("cc.about.tab4.text2.title")));
      
      // done
      return scroll;
    }
  
  } // CopyrightPanel
  
  /**
   * Panel - Look&Feel
   */
  private static class LookNFeelPanel extends JPanel {
  }
  
} // Closes AboutDialog class
