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
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/core/genj/app/AboutDialog.java,v 1.2 2002-05-21 21:12:07 island1 Exp $
 * @author Francois Massonneau <frmas@free.fr>
 * @version 1.0
 *
 */
 
package genj.app;

import genj.util.*;
import genj.util.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;


// AboutDialog class is opened
public class AboutDialog {
  
  
  // Data field
  public static AboutDialog instance;
  private JFrame adFrame;
  private JPanel adMainpanel;
  private JPanel adNorthpanel;
  private JPanel adSouthpanel;
  private JTabbedPane adCenterpanel;
  private JLabel jlabadNorthpanel;
  
//  private JTabbedPane aboutPane;
  
  public AboutDialog()  
  { // Opens AboutDialog constructor
    
    // We create the About Dialog frame
    adFrame = new JFrame(App.resources.getString("cc.about.dialog.frame"));
    // Now the About Dialog (main) panel
    adMainpanel = new JPanel();
    // Set layout for About Dialog panel
    adMainpanel.setLayout(new BorderLayout());
    // Set background color for About Dialog panel
    adMainpanel.setBackground(Color.yellow);
    
    buildNorthpanel();
    
    buildCenterpanel();
    
    buildSouthpanel();
    
    // Now we add the 3 panels above to the Main panel's border layout manager
    adMainpanel.add(adNorthpanel, BorderLayout.NORTH);
    adMainpanel.add(adCenterpanel, BorderLayout.CENTER);
    adMainpanel.add(adSouthpanel, BorderLayout.SOUTH);
    
    adFrame.getContentPane().add(adMainpanel);
    adFrame.pack();
    adFrame.setSize(570, 360);
    adFrame.setBackground(Color.white);
    adFrame.setVisible(true);
    
  } // Closes AboutDialog constructor
  
  
  // Methods
  
  // Method to create a panel called adNorthpanel
  private void buildNorthpanel()
  { // Opens method buildNorthpanel
    adNorthpanel = new JPanel();
    adNorthpanel.setLayout(new BorderLayout());
    adNorthpanel.setBackground(Color.white);
    jlabadNorthpanel = new JLabel(App.resources.getString("cc.about.dialog.northpanel.label"), null, JLabel.CENTER);
    jlabadNorthpanel.setFont(new Font("Times-Roman", Font.BOLD, 12));
    adNorthpanel.add(jlabadNorthpanel, BorderLayout.CENTER);
  } // Closes method buildNorthpanel
  
  // Method to create a panel called adCenterpanel
  private void buildCenterpanel()
  { // Opens method buildCenterpanel
    // Tabbed pane with panels
    adCenterpanel = new JTabbedPane(SwingConstants.LEFT);
    adCenterpanel.setBackground(Color.blue);
    adCenterpanel.setForeground(Color.white);
    populateTabbedPane();
  } // Closes method buildCenterpanel
  
  // Method to create a panel called adSouthpanel
  private void buildSouthpanel()
  { // Opens method buildSouthpanel
    adSouthpanel = new JPanel();
    adSouthpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    adSouthpanel.setBackground(Color.white);
    JButton jbExitButton = new JButton(App.resources.getString("cc.about.dialog.exit"));
    jbExitButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        instance = null;
        adFrame.dispose();
      }
    });
    adSouthpanel.add(jbExitButton);
  } // Closes method buildSouthpanel
  
  // method to populate tabbed pane
  private void populateTabbedPane()
  {
    adCenterpanel.addTab(App.resources.getString("cc.about.dialog.tab1.title"), null, new WelcomePanel(), App.resources.getString("cc.about.dialog.tab1.title.tip"));
    adCenterpanel.addTab(App.resources.getString("cc.about.dialog.tab2.title"), null, new AuthorsPanel(), App.resources.getString("cc.about.dialog.tab2.title.tip"));
    adCenterpanel.addTab(App.resources.getString("cc.about.dialog.tab3.title"), null, new FeedbackPanel(), App.resources.getString("cc.about.dialog.tab3.title.tip"));
    adCenterpanel.addTab(App.resources.getString("cc.about.dialog.tab4.title"), null, new CopyrightPanel(), App.resources.getString("cc.about.dialog.tab4.title.tip"));
  }
  
  // main method
  public static void showAboutDialog()
  {
    if (instance!=null) {
      instance.adFrame.show();
      return;
    }
    else {
      instance = new AboutDialog();
    }
  }
  
} // Closes AboutDialog class
