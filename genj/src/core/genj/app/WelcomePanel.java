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
 * About Menu class - Welcome
 * This class creates the content on the Welcome tabbed pane in the
 * About Menu application
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/core/genj/app/WelcomePanel.java,v 1.4 2002-05-16 21:03:03 island1 Exp $
 * @author Francois Massonneau <frmas@free.fr>
 * @version 1.0
 *
 */

package genj.app;


import genj.Version;

// Import for buttons, labels, and images
import javax.swing.*;
import javax.swing.border.*;
// Import for layout manager.
import java.awt.*;

public class WelcomePanel extends JPanel
{ // Opens class
  
  private JTextArea ta;
  private JTextArea taversion;
  
  public WelcomePanel()
  {  // Opens constructor
    // Sets the layout by instantiating a BorderLayout container in the setLayout method
    setLayout(new BorderLayout());
    // Sets the background color for this welcome panel object
    setBackground(Color.white);
    
    // Initialize a text area object that contains the text
    ta = new JTextArea("\n" + App.resources.getString("cc.about.tab1.text1") +
    "\n\n");
    // Sets the font face and size
    ta.setFont(new Font("Times-Roman", Font.PLAIN, 12));
    // We create an empty border around the text displayed
    ta.setBorder(new EmptyBorder(8, 8, 8, 8));    
    // Line wrap is set for the text area and it is not editable
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    ta.setEditable(false);
    
    // Initialize a text area object that contains the appli name and version
    taversion = new JTextArea(App.resources.getString("app.title")+" "+Version.getInstance().toString());
    // Sets the font face and size
    taversion.setFont(new Font("Times-Roman", Font.BOLD, 13));
    // Line wrap is set for the text area and it is not editable
    taversion.setLineWrap(true);
    taversion.setWrapStyleWord(true);
    taversion.setEditable(false);
    
    // Each object is added to the layout and positioned
    add(ta, BorderLayout.NORTH);
    add(taversion, BorderLayout.SOUTH);
    
  }  // Closes constructor

} // Closes class

