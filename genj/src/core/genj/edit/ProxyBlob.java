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
package genj.edit;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

import genj.gedcom.*;
import genj.util.*;
import genj.util.swing.ImgIconConverter;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : BLOB
 */
class ProxyBlob extends Proxy implements ActionListener {

  /** members */
  private EditView     edit;
  private Frame        frame;
  private JLabel       lImage;
  private JScrollPane  sImage;

  /**
   * User pressed button to select file
   */
  public void actionPerformed(ActionEvent e) {

    // Let the user choose a file
    String dir = EnvironmentChecker.getProperty(
      this,
      new String[]{ "genj.gedcom.dir", "user.home" },
      ".",
      "choose multimedia file"
    );
    JFileChooser chooser = new JFileChooser(dir);
    chooser.setDialogTitle("Choose a multimedia blob");

    int rc=chooser.showDialog(frame, "Open");

    // Cancel ?
    if (JFileChooser.APPROVE_OPTION != rc)
      return;

    // Can I write ?
    Gedcom gedcom = prop.getGedcom();
    if (!gedcom.startTransaction()) {
      return;
    }

    // Change blob-data to file-data
    File file = chooser.getSelectedFile();
    try {
      ((PropertyBlob)prop).setValue(file);
    } catch (GedcomException ex) {
      // .. message to user
      JOptionPane.showMessageDialog(
      frame,
      ex.getMessage(),
      edit.resources.getString("error"),
      JOptionPane.ERROR_MESSAGE
      );
      // .. done for now
    }

    // Done
    gedcom.endTransaction();

    // Let it show
    showBlob();

  }

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {
    // Done
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return false;
  }

  /**
   * Shows property's image if possible
   */
  private void showBlob() {

    PropertyBlob pBlob = (PropertyBlob)prop;
    if (pBlob.getBlobData()==null) {
      lImage.setText("No data stored here");
      lImage.setIcon(null);
    } else {
      // Image from Blob?
      ImgIcon i = pBlob.getValueAsIcon();
      ImageIcon icon = (i==null ? null : ImgIconConverter.get(i));
      if ( (icon==null)||(icon.getIconWidth()<1)||(icon.getIconHeight()<1) ) {
        lImage.setText("Blob is no image");
        lImage.setIcon(null);
      } else {
        lImage.setIcon(icon);
        lImage.setText("");
      }
    }

    lImage.invalidate();
    sImage.validate();

    // done
  }

  /**
   * Start editing a property through proxy
   */
  protected void start(JPanel in, JLabel setLabel, Property setProp, EditView setEdit) {

    edit =setEdit   ;
    prop =setProp   ;
    frame=edit.getFrame();

    // Add a button for FileChooserAction
    JButton bFile = new JButton("Load from File");
    bFile.addActionListener(this);
    in.add(bFile);

    // Any graphical information that could be shown ?
    lImage = new JLabel();
    lImage.setHorizontalAlignment(SwingConstants.CENTER);
    sImage = new JScrollPane(lImage);
    sImage.setMinimumSize(new Dimension(0,0));
    in.add(sImage);

    showBlob();

    // Done
  }
}
