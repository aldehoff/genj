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
 * About Menu class - Authors
 * This class creates the content on the Authors tabbed pane in the
 * About Menu application
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/core/genj/app/AuthorsPanel.java,v 1.3 2002-05-16 21:03:03 island1 Exp $
 * @author Francois Massonneau <frmas@free.fr>
 * @version 1.0
 *
 */

package genj.app;

// Import for buttons, labels, and images
import javax.swing.*;
// Import for layout manager.
import java.awt.*;


public class AuthorsPanel extends JPanel
{ // Opens class
  
    public AuthorsPanel()
  {  // Opens constructor
    // Sets the layout by instantiating a BorderLayout container in the setLayout method
    setLayout(new BorderLayout());
    // Sets the background color for this copyright panel object
    setBackground(Color.white);
    
    
  }  // Closes constructor
  

} // Closes class

