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
 * AboutMenu class
 * This class creates the content of AboutMenu application
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/core/genj/app/AboutMenu.java,v 1.2 2002-05-11 19:20:25 island1 Exp $
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


// AboutMenu class is opened
public class AboutMenu {
  
  
  // Data field
  private JFrame amframe;
  private JPanel amMainpanel;
  private JPanel amNorthpanel;
  private JPanel amSouthpanel;
  private JTabbedPane amCenterpanel;
  private JLabel jlabamNorthpanel;
  
//  private JTabbedPane aboutPane;
  
  public AboutMenu()  
  { // Opens AboutMenu constructor
    
    // We create the About Menu frame
    amframe = new JFrame(App.resources.getString("cc.about.menu.frame"));
    // Now the About Menu (main) panel
    amMainpanel = new JPanel();
    // Set layout for About Menu panel
    amMainpanel.setLayout(new BorderLayout());
    // Set background color for About Menu panel
    amMainpanel.setBackground(Color.yellow);
    
    buildNorthpanel();
    
    buildCenterpanel();
    
    buildSouthpanel();
    
    // Now we add the 3 panels above to the Main panel's border layout manager
    amMainpanel.add(amNorthpanel, BorderLayout.NORTH);
    amMainpanel.add(amCenterpanel, BorderLayout.CENTER);
    amMainpanel.add(amSouthpanel, BorderLayout.SOUTH);
    
    amframe.getContentPane().add(amMainpanel);
    amframe.pack();
    amframe.setSize(500, 300);
    amframe.setBackground(Color.white);
    amframe.setVisible(true);
    
  } // Closes AboutMenu constructor
  
  
  // Methods
  
  // Method to create a panel called amNorthpanel
  private void buildNorthpanel()
  { // Opens method buildNorthpanel
    amNorthpanel = new JPanel();
    amNorthpanel.setLayout(new BorderLayout());
    amNorthpanel.setBackground(Color.white);
    jlabamNorthpanel = new JLabel(App.resources.getString("cc.about.menu.northpanel.label"), null, JLabel.CENTER);
    jlabamNorthpanel.setFont(new Font("Times-Roman", Font.BOLD, 12));
    amNorthpanel.add(jlabamNorthpanel, BorderLayout.CENTER);
  } // Closes method buildNorthpanel
  
  // Method to create a panel called amCenterpanel
  private void buildCenterpanel()
  { // Opens method buildCenterpanel
    // Tabbed pane with panels
    amCenterpanel = new JTabbedPane(SwingConstants.LEFT);
    amCenterpanel.setBackground(Color.blue);
    amCenterpanel.setForeground(Color.white);
    populateTabbedPane();
  } // Closes method buildCenterpanel
  
  // Method to create a panel called amSouthpanel
  private void buildSouthpanel()
  { // Opens method buildSouthpanel
    amSouthpanel = new JPanel();
    amSouthpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    amSouthpanel.setBackground(Color.white);
    JButton jbExitButton = new JButton(App.resources.getString("cc.about.menu.exit"));
    jbExitButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        amframe.dispose();
      }
    });
    amSouthpanel.add(jbExitButton);
  } // Closes method buildSouthpanel
  
  // method to populate tabbed pane
  private void populateTabbedPane()
  {
    amCenterpanel.addTab(App.resources.getString("cc.about.menu.tab1.title"), null, new WelcomePanel(), App.resources.getString("cc.about.menu.tab1.title.tip"));
    amCenterpanel.addTab(App.resources.getString("cc.about.menu.tab2.title"), null, new AuthorsPanel(), App.resources.getString("cc.about.menu.tab2.title"));
    amCenterpanel.addTab(App.resources.getString("cc.about.menu.tab3.title"), null, new CopyrightPanel(), App.resources.getString("cc.about.menu.tab3.title"));
  }
  
  // main method
  public static void showDialogMenu()
  {
    AboutMenu am = new AboutMenu();
  }
  
} // Closes AboutMenu class
