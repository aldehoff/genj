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
 * will use to change a property : FILE
 */
class ProxyFile extends Proxy implements ActionListener {

  /** members */
  private EditView     edit;
  private Frame        frame;
  private JLabel       lImage;
  private JScrollPane  sImage;
  private JTextField   tFile;

  /**
   * User pressed button to select file
   */
  public void actionPerformed(ActionEvent e) {

    PropertyFile p = (PropertyFile)prop;
    String newValue=null;
    File   newFile =null;

    // Button or text-field ?
    if (e.getSource()==tFile) {
      newValue = tFile.getText();
    } else {
      // Let the user choose a file
      String dir = EnvironmentChecker.getProperty(
        this,
        new String[]{ "genj.gedcom.dir", "user.home" },
        ".",
        "choose multimedia file"
      );
      JFileChooser chooser = new JFileChooser(dir);
      chooser.setDialogTitle("Choose a file");

      int rc=chooser.showDialog(frame, "Choose file");

      // Cancel ?
      if (JFileChooser.APPROVE_OPTION != rc)
        return;
      newFile = chooser.getSelectedFile();

    }

    // Can I write ?
    Gedcom gedcom = prop.getGedcom();

    if (!gedcom.startTransaction()) {
      return;
    }

    // Change File-link
    if (newFile!=null)
      p.setValue(newFile);
    else
      p.setValue(newValue);

    // Done
    gedcom.endTransaction();

    // Let it show
    showFile();
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
  private void showFile() {

    PropertyFile pFile = (PropertyFile)prop;
    tFile.setText( pFile.getValue() );

    // Image from File?
    ImgIcon i = pFile.getValueAsIcon();
    ImageIcon icon = (i==null ? null : ImgIconConverter.get(i));
    if ( (icon==null)||(icon.getIconWidth()<1)||(icon.getIconHeight()<1) ) {
      lImage.setText("No image file");
      lImage.setIcon(null);
    } else {
      lImage.setIcon(icon);
      lImage.setText("");
    }

    // Update visually
    lImage.invalidate();
    sImage.validate();
  }

  /**
   * Start editing a property through proxy
   */
  protected void start(JPanel in, JLabel setLabel, Property setProp, EditView setEdit) {

    edit =setEdit        ;
    prop =setProp        ;
    frame=edit.getFrame();

    // Add a button for FileChooserAction
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
    p.setAlignmentX(0);

    tFile = createTextField("", "F", null, null);
    tFile.setAlignmentY(0.5F);
    tFile.addActionListener(this);
    p.add(tFile);

    JButton bFile = createButton("Choose File", "CHOOSE", true, this, null);
    bFile.setAlignmentY(0.5F);
    p.add(bFile);

    in.add(p);

    // Any graphical information that could be shown ?
    lImage = new JLabel();
    lImage.setHorizontalAlignment(SwingConstants.CENTER);
    sImage = new JScrollPane(lImage);
    sImage.setMinimumSize(new Dimension(0,0));
    in.add(sImage);

    showFile();

    // Done
  }

}
