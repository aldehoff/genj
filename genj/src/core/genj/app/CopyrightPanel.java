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
 * About Menu class - Copyright
 * This class creates the content on the Copyright tabbed pane in the
 * About Menu application
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/core/genj/app/CopyrightPanel.java,v 1.5 2002-05-20 16:41:04 island1 Exp $
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


public class CopyrightPanel extends JPanel
{ // Opens class
  
  // Some variables
  private JPanel panelCopyR;
  private JPanel panelGpl;
  private JTextArea taCopyR;
  private JTextArea taGpl;
  
    public CopyrightPanel()
  {  // Opens constructor
    // Sets the layout by instantiating a BorderLayout container in the setLayout method
    setLayout(new BorderLayout());
    // Sets the background color for this copyright panel object
//    setBackground(Color.white);
    
    // Call method to build Panel 1 - panelCopyR
    buildPanelCopyr();
    
    // Call method to build Panel 2 - panelGpl
    buildPanelGpl();
    
    // Time to add each panel to the main panel
    add(panelCopyR, BorderLayout.NORTH);
    add(panelGpl, BorderLayout.CENTER);
    
  }  // Closes constructor
  
  // This method create the panel to display the Copyright stuff
  private void buildPanelCopyr() {
    panelCopyR = new JPanel();
    panelCopyR.setLayout(new BorderLayout());
    panelCopyR.setBackground(Color.white);
    panelCopyR.setBorder(BorderFactory.createTitledBorder(App.resources.getString("cc.about.tab4.text1.title")));
    taCopyR = new JTextArea(App.resources.getString("cc.about.tab4.text1"));
    // Sets the font face and size
    taCopyR.setFont(new Font("Times-Roman", Font.PLAIN, 12));
    // Line wrap is set for the text area and it is not editable
    taCopyR.setLineWrap(true);
    taCopyR.setWrapStyleWord(true);
    taCopyR.setEditable(false);
    taCopyR.setBorder(new EmptyBorder(3, 3, 3, 3));
    panelCopyR.add(taCopyR, BorderLayout.CENTER);
  }
    
  // This method create the panel to display the GPL file
  private void buildPanelGpl() {
    panelGpl = new JPanel();
    panelGpl.setLayout(new BorderLayout());
    panelGpl.setBackground(Color.white);
    panelGpl.setBorder(BorderFactory.createTitledBorder(App.resources.getString("cc.about.tab4.text2.title")));
    taGpl = new JTextArea();
    // We give the path for the gpl.txt file, using a platform-independant construction
    String pathAndFileToGPLDoc = System.getProperty("user.dir") + File.separatorChar + "doc" + File.separatorChar + "gpl.txt";
    readTextFile(taGpl, pathAndFileToGPLDoc, App.resources.getString("cc.about.tab4.text2"));
    taGpl.setFont(new Font("Times-Roman", Font.PLAIN, 12));
    taGpl.setLineWrap(true);
    taGpl.setWrapStyleWord(true);
    taGpl.setEditable(false);
    taGpl.setBorder(new EmptyBorder(3, 3, 3, 3));
    JScrollPane spGpl = new JScrollPane(taGpl);
    spGpl.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    spGpl.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    panelGpl.add(spGpl, BorderLayout.CENTER);
  }
  
  // This method reads the text to feed the variable taGpl and if the text file is
  // missing, it sends a substitute sentence.
  private void readTextFile(JTextArea ta, String file, String fallback) {
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
  
  
} // Closes class

