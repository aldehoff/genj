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
 * About Dialog class - Authors
 * This class creates the content on the Authors tabbed pane in the
 * About Dialog application
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/core/genj/app/AuthorsPanel.java,v 1.6 2002-05-26 09:20:07 island1 Exp $
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



public class AuthorsPanel extends InfoPanel
{ // Opens class
  
  // Some variables
  private JPanel panelAuthor;
  private JTextArea taAuthorText;
  
  
    public AuthorsPanel()
  {  // Opens constructor
    // Sets the layout by instantiating a BorderLayout container in the setLayout method
    setLayout(new BorderLayout());
    // Sets the background color for this Authors panel object
    setBackground(Color.white);
    
    // We create a green border
    setBorder(BorderFactory.createMatteBorder(1, 1, 3, 3, Color.green));
    
    
    // Call method to build  the Authors Panel
    buildPanelAuthor();
    
    // We add the authors panel to the main panel
    add(panelAuthor, BorderLayout.CENTER);
    
    
  }  // Closes constructor
  
  // This method create the panel to display the authors file
  private void buildPanelAuthor() {
    panelAuthor = new JPanel();
    panelAuthor.setLayout(new BorderLayout());
    panelAuthor.setBackground(Color.white);
    taAuthorText = new JTextArea();
    // We give the path for the authors.txt file, using a platform-independant construction
    String pathAndFileToAuthorText = System.getProperty("user.dir") + File.separatorChar + "doc" + File.separatorChar + "authors.txt";
    this.readTextFile(taAuthorText, pathAndFileToAuthorText, App.resources.getString("cc.about.file_missing.text") + pathAndFileToAuthorText);
    taAuthorText.setFont(new Font("Times-Roman", Font.PLAIN, 12));
    taAuthorText.setLineWrap(true);
    taAuthorText.setWrapStyleWord(true);
    taAuthorText.setEditable(false);
    JScrollPane spAuthor = new JScrollPane(taAuthorText);
    spAuthor.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    spAuthor.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    panelAuthor.add(spAuthor, BorderLayout.CENTER);
  }
  

} // Closes class

