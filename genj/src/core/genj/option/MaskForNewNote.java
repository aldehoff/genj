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
 * Mask for creating: Note
 */
class MaskForNewNote extends MaskForNewEntity{

  private Frame     frame;
  private JTextArea tarea;

  /**
   * Initializing
   */
  protected void init(Option option) {
    super.init(option);
  }

  /**
   * Initiates entity's creation
   */
  protected boolean createIn(Gedcom gedcom) {

    // Go for it
    Note note;
    try {
      note = gedcom.createNote();
      note.setValue(tarea.getText());
    } catch (GedcomException e) {
      Debug.log(Debug.WARNING, this, e);
      return false;
    }

    // Add it's preset properties

    // Done
    return true;
  }

  /**
   * Returns the data-page for entity
   */
  protected JPanel getDataPage() {

    // Prepare panel
    JPanel result = new JPanel(new GridLayout(1,1));
    tarea = new JTextArea(10,10);

    result.add(new JScrollPane(tarea));

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
  void handleContextChange(Gedcom gedcom) {
    // Done
  }
}
