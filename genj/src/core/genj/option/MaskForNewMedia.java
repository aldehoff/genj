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
 */
package genj.option;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import genj.gedcom.*;
import genj.util.*;

/**
 * Mask for creation: Multimedia Object
 */
class MaskForNewMedia extends MaskForNewEntity{

  private Property newBlob  ;
  private String   newTitle ="?";
  private String   newFormat="?";
  private Frame    frame    ;

  /**
   * Initializer
   */
  protected void init(Option option) {
    super.init(option);
  }

  /**
   * Choose a file from user
   */
  void chooseFile() {

    // Let the user choose a file
    JFileChooser chooser = new JFileChooser(".");
    chooser.setDialogTitle("Choose a multimedia file");
    int rc=chooser.showDialog(frame, "Open");

    // Cancel ?
    if (JFileChooser.APPROVE_OPTION != rc)
      return;

    // Read it to a blob
    File file = chooser.getSelectedFile();

    // Create new Blob
    try {
      newBlob = new PropertyBlob(file);
    } catch (GedcomException ex) {
      // .. message to user
      JOptionPane.showMessageDialog(
      frame,
      ex.getMessage(),
      getResourceString("error"),
      JOptionPane.ERROR_MESSAGE
      );
      // .. done for now
      return;
    }

    // Prepare meta-information
    newTitle = "Taken from "+file.getName();
    newFormat= file.getName();

    int i=newFormat.lastIndexOf('.');
    if (i>0)
      newFormat = newFormat.substring(i+1).toLowerCase();
    else
      newFormat = null;

    // Done for now
  }

  /**
   * Initiates entity's creation
   */
  protected boolean createIn(Gedcom gedcom) {

    // Go for it
    Media media;
    try {
      media = gedcom.createMedia();
    } catch (GedcomException e) {
      Debug.log(Debug.WARNING, this, e);
      return false;
    }

    // Add it's preset properties
    Property p = media.getProperty();
    p.addProperty(new PropertyGenericAttribute("TITL",newTitle));
    p.addProperty(new PropertyGenericAttribute("FORM",newFormat));
    if (newBlob!=null) {
      p.addProperty(newBlob);
    } else {
      p.addProperty(new PropertyBlob());
    }

    // Done
    return true;
  }

  /**
   * Returns the data-page for entity
   */
  protected JPanel getDataPage() {

    // Prepare panel
    JPanel result = new JPanel();

    GridBagHelper helper = new GridBagHelper(result);

    JLabel     label = new JLabel    ("Data File");
    JButton    button= new JButton   ("Choose");

    helper.add(label   ,1,1);
    helper.add(button  ,3,1);

    // Listen to button
    ActionListener a = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      chooseFile();
      }
      // EOC
    };
    button.addActionListener(a);

    // Done
    return result;
  }

  /**
   * Returns the data-page for entity
   */
  protected JPanel getRelationPage(JComponent firstRow) {

    // Prepare panel
    JPanel result = new JPanel();

    GridBagHelper helper = new GridBagHelper(result);

    helper.add(firstRow  ,0,0,1,1);

    // Done
    return result;
  }

  /**
   * Sets mask's entity
   */
  protected void handleContextChange(Gedcom gedcom) {
    // Done
  }

}
