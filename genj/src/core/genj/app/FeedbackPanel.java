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
 * About Menu class - Feedback
 * This class creates the content on the Feedback tabbed pane in the
 * About Menu application
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/core/genj/app/FeedbackPanel.java,v 1.3 2002-05-18 07:32:14 island1 Exp $
 * @author Francois Massonneau <frmas@free.fr>
 * @version 1.1
 *
 */

package genj.app;

// Import for buttons, labels, and images
import javax.swing.*;
import javax.swing.border.*;
// Import for layout manager.
import java.awt.*;
import java.io.*;
import java.util.*;

import genj.Version;


public class FeedbackPanel extends JPanel
{ // Opens class
  
  // variables for panelUp
  private JPanel panelUp;
  private JTextArea ta1Panel1;
  
  // variables for panelBottom
  private JPanel panelMiddle;
  private JTextArea ta1Panel2;
  
  // variables for panelBottom
  private JPanel panelBottom;
  private JButton b1Panel3;
  
  
    public FeedbackPanel()
  {  // Opens constructor
    // Sets the layout by instantiating a BorderLayout container in the setLayout method
    setLayout(new BorderLayout());
    // Sets the background color for this authors panel object
//    setBackground(Color.white);
    
    // Call method to build Panel 1 - panelUp
    buildPanelUp();
    
    // Call method to build Panel 2 - panelMiddle
    buildPanelMiddle();
    
    // Call method to build Panel 3 - panelBottom
    buildPanelBottom();
    
    // Time to add each panel to the main panel
    add(panelUp, BorderLayout.NORTH);
    add(panelMiddle, BorderLayout.CENTER);
    add(panelBottom, BorderLayout.SOUTH);
    
  }  // Closes constructor
  
  // This method build the Up Panel
  private void buildPanelUp() {
    panelUp = new JPanel();
    panelUp.setLayout(new BorderLayout());
    panelUp.setBackground(Color.white);
    panelUp.setBorder(BorderFactory.createTitledBorder(App.resources.getString("cc.about.tab3.text1.title")));
    ta1Panel1 = new JTextArea(App.resources.getString("cc.about.tab3.text1"));
    // Sets the font face and size
    ta1Panel1.setFont(new Font("Times-Roman", Font.PLAIN, 12));
    // Line wrap is set for the text area and it is not editable
    ta1Panel1.setLineWrap(true);
    ta1Panel1.setWrapStyleWord(true);
    ta1Panel1.setEditable(false);
    ta1Panel1.setBorder(new EmptyBorder(3, 3, 3, 3));
    panelUp.add(ta1Panel1, BorderLayout.CENTER);
  }
  
    // This method build the Middle Panel
  private void buildPanelMiddle() {
    panelMiddle = new JPanel(new BorderLayout());
    panelMiddle.setBackground(Color.white);
    panelMiddle.setBorder(BorderFactory.createTitledBorder(App.resources.getString("cc.about.tab3.text2.title")));
    ta1Panel2 = new JTextArea(getSystemInfo());
    ta1Panel2.setLineWrap(true);
    ta1Panel2.setWrapStyleWord(true);
    ta1Panel2.setEditable(true);
    ta1Panel2.setBorder(new EmptyBorder(3, 3, 3, 3));
    JScrollPane jspta1 = new JScrollPane(ta1Panel2);
    panelMiddle.add(jspta1, BorderLayout.CENTER);
  }
  
    // This method build the Bottom Panel
  private void buildPanelBottom() {
    panelBottom = new JPanel();
    panelBottom.setLayout(new FlowLayout(FlowLayout.LEFT));
    panelBottom.setBackground(Color.white);
    b1Panel3 = new JButton(App.resources.getString("cc.about.tab3.send.button"));
    panelBottom.add(b1Panel3);
  }
  
    // This method gets the User's system info
  public static String getSystemInfo()
  {
   	Properties p = System.getProperties();
    StringBuffer sb = new StringBuffer();
   	sb.append(App.resources.getString("cc.about.tab3.text2.1"));
    sb.append(": \n");
    sb.append(p.get("os.name"));
    sb.append(" ");
    sb.append(p.get("os.version"));
    sb.append(" (");
    sb.append(p.get("os.arch"));
    sb.append(") - JRE: ");
    sb.append(p.get("java.vendor"));
    sb.append(" ");
    sb.append(p.get("java.version"));
    sb.append(" - ");
    sb.append("Class version: ");
    sb.append(p.get("java.class.version"));
    sb.append("\n" + App.resources.getString("cc.about.tab3.text2.2"));
    sb.append(" " + App.resources.getString("app.title")+" "+Version.getInstance().toString());
    sb.append(".\n\n");
    sb.append(App.resources.getString("cc.about.tab3.text2.3"));
    sb.append(" :\n");
    sb.append(App.resources.getString("cc.about.tab3.text2.4"));
    sb.append(" :\n");
    sb.append(App.resources.getString("cc.about.tab3.text2.5"));
    sb.append(". . . . . . . . . . . . . . . . . . . .\n");
    sb.append("\nThis function is not yet available - Feel free to write the code needed ;-) ");
   	return sb.toString();
  }
  
} // Closes class

