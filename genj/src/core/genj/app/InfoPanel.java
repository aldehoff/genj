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
 * InfoPanel class -
 * This class creates a super class for some of the About Dialog classes
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/core/genj/app/InfoPanel.java,v 1.1 2002-05-26 09:18:58 island1 Exp $
 * @author Francois Massonneau <frmas@free.fr>
 * @version 1.0
 *
 */


package genj.app;


import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;



public class InfoPanel extends JPanel
{ // Opens class
  
  // variables
  
  
  
  
  // This method reads the text to feed variables and if the text file is
  // missing, it sends a substitute sentence.
  public void readTextFile(JTextArea ta, String file, String fallback) {
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

