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
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import genj.gedcom.*;
import genj.util.ImgIcon;
import genj.util.swing.ImgIconConverter;

/**
 * Option - Delete Entity
 */
public class OptionDelEntity extends Option implements ActionListener, GedcomListener {

  private Entity entity   = null;
  private Vector gedcoms  = null;

  private JFrame       frame;
  private JLabel       lWhich;
  private JButton      bDelete;

  static private ImageIcon imgTrash;

  /**
   * Constructor for new indiviual option
   */
  public OptionDelEntity(JFrame frame, Vector gedcoms, Gedcom start) {

    super(frame);

    // Images ?
    if (imgTrash==null) {
      imgTrash = ImgIconConverter.get(new ImgIcon(this,"Trash.gif"));
    }

    // Data
    this.frame=frame;
    this.gedcoms=gedcoms;

    // Panel for Which
    lWhich = new JLabel(getResourceString("mask.none"),SwingConstants.CENTER) {
      public Dimension getPreferredSize() {
      return new Dimension(128,64);
      }
    };

    // Panel for Action
    JPanel pAction = new JPanel();
    pAction.setLayout(new FlowLayout(FlowLayout.RIGHT));

    bDelete = new JButton(getResourceString("mask.delete"));
    pAction.add(bDelete);

    // Layout
    setLayout(new BorderLayout(3,3));
    add(lWhich,"Center");
    add(pAction,"South");
    add(new JLabel(imgTrash),"West");

    // Listeners
    Enumeration gs = gedcoms.elements();
    while (gs.hasMoreElements()) {
      ((Gedcom)gs.nextElement()).addListener(this);
    }
    bDelete.addActionListener(this);

    // Initial relative
    if (start==null)
      setEntity(null);
    else {
      setEntity(start.getLastEntity());
    }

    // Done
  }

  /**
   * Gets triggered when a button has been pressed
   */
  public void actionPerformed(ActionEvent e) {

    // Delete
    if (entity==null) {
      return;
    }

    Gedcom gedcom = entity.getGedcom();
    if (gedcom==null) {
      return;
    }

    if (!gedcom.startTransaction()) {
      return;
    }

    try {
      gedcom.deleteEntity(entity);
    } catch (GedcomException ge) {
    }
    gedcom.endTransaction();

    // Done & Dispose (?)
    frame.dispose();
  }

  /**
   * Helper function that adds components to panels with GridBagLayout
   */
  private void addComponent(Container to,GridBagLayout layout,Component which,int gridx,int gridy,int gridwidth,int gridheight,boolean fill) {
    GridBagConstraints c = new GridBagConstraints();
    c.gridx=gridx;
    c.gridy=gridy;
    c.gridheight=gridheight;
    c.gridwidth=gridwidth;
    c.fill = fill ? GridBagConstraints.BOTH : GridBagConstraints.NONE;
    layout.setConstraints(which,c);
    to.add(which);
  }

  /**
   * Notification that a change in a Gedcom-object took place.
   */
  public void handleChange(Change change) {
    // Back off in case someone deleted entities
    if ( change.isChanged(Change.EDEL) )
      setEntity(null);
    // Done
  }

  /**
   * Notification that the gedcom is being closed
   */
  public void handleClose(Gedcom which) {
    if ((entity!=null)&&(entity.getGedcom()==which))
      setEntity(null);
    which.removeListener(this);
  }

  /**
   * Notification that an entity has been selected.
   */
  public void handleSelection(Selection selection) {
    setEntity(selection.getEntity());
  }

  /**
   * Notification when component is not used any more
   */
  public void removeNotify() {
    super.removeNotify();

    Enumeration gs = gedcoms.elements();
    while (gs.hasMoreElements()) {
      ((Gedcom)gs.nextElement()).removeListener(this);
    }
  }

  /**
   * Remembers entity to be deleted
   * @param entity reference to be remembered
   */
  private void setEntity(Entity entity) {

    // No entity any more ?
    if (entity==null) {

      lWhich.setText( getResourceString("mask.none") );
      lWhich.setIcon( ImgIconConverter.get(Property.getDefaultImage("?")) );
      bDelete.setEnabled(false);

    } else {

      lWhich.setText( entity.toString() );
      lWhich.setIcon( ImgIconConverter.get(entity.getProperty().getImage(false)) );
      bDelete.setEnabled(true);

    }

    // Remember
    this.entity=entity;

    // Done
  }

}
