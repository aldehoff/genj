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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import genj.gedcom.*;
import genj.util.ImgIcon;
import genj.util.swing.ImgIconConverter;

/**
 * Option - Create Entity
 */
public class OptionNewEntity extends Option implements ActionListener, GedcomListener, ItemListener {

  public final static int
    INDIVIDUAL = 0,
    FAMILY     = 1,
    MULTIMEDIA = 2,
    NOTE       = 3;

  private Class masks[] = {
    MaskForNewIndi.class,
    MaskForNewFam.class,
    MaskForNewMedia.class,
    MaskForNewNote.class
  };

  private Vector gedcoms  = null;
  private Gedcom gedcom   = null;
  private Entity entity   = null;
  private MaskForNewEntity mask;

  private JCheckBox    cbMemberOf;
  private JButton      bNext,bBack,bCreate;

  static private ImageIcon    imgData,imgRelation;

  /**
   * Constructor for new indiviual option
   */
  public OptionNewEntity(JFrame frame, int type, Vector gedcoms, Gedcom start) {

    super(frame);

    // Images ?
    if (imgData==null) {
      imgData     = ImgIconConverter.get(new ImgIcon(this,"Data.gif"    ));
      imgRelation = ImgIconConverter.get(new ImgIcon(this,"Relation.gif"));
    }

    // Data
    this.gedcoms=gedcoms;

    // Calculate appropriate Mask
    if ((type<0)||(masks.length<=type))
      throw new RuntimeException("Unknown type "+type);

    try {
      mask = (MaskForNewEntity)masks[type].newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }

    mask.init(this);

    // Layout Data-Page
    JPanel dataPage = new JPanel();
    dataPage.setLayout(new BorderLayout(2,2));
    dataPage.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

    JPanel pAction = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    pAction.add(createButton(getResourceString("mask.cancel"),"CANCEL"));
    pAction.add(bNext = createButton(getResourceString("mask.next"),"NEXT"));
    frame.getRootPane().setDefaultButton(bNext);

    dataPage.add(new JLabel(imgData),"West");
    dataPage.add(mask.getDataPage(),"Center");
    dataPage.add(pAction,"South");

    // Layout Relation-Page
    JPanel relationPage = new JPanel();
    relationPage.setLayout(new BorderLayout(2,2));
    relationPage.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

    pAction = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    pAction.add(createButton(getResourceString("mask.cancel"),"CANCEL"));
    pAction.add(bBack = createButton(getResourceString("mask.back"),"BACK"));
    pAction.add(bCreate = createButton(getResourceString("mask.create"),"CREATE"));

    cbMemberOf = new JCheckBox(getResourceString("mask.member_of")+" "+getResourceString("mask.none"),false);
    cbMemberOf.setEnabled(false);

    relationPage.add(new JLabel(imgRelation),"West");
    relationPage.add(mask.getRelationPage(cbMemberOf),"Center");
    relationPage.add(pAction,"South");

    // Layout this
    setLayout(new CardLayout());
    add("DATA"    ,dataPage    );
    add("RELATION",relationPage);

    // Listeners
    Enumeration gs = gedcoms.elements();
    while (gs.hasMoreElements()) {
      ((Gedcom)gs.nextElement()).addListener(this);
    }

    // Initial relative
    setContext(start);

    // Done
  }

  /**
   * Gets triggered when a button has been pressed
   */
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals("CANCEL")) {
      frame.dispose();
      return;
    }
    if (e.getActionCommand().equals("NEXT")) {
      ((CardLayout)getLayout()).show(this,"RELATION");
      frame.getRootPane().setDefaultButton(bCreate);
      return;
    }
    if (e.getActionCommand().equals("BACK")) {
      ((CardLayout)getLayout()).show(this,"DATA");
      frame.getRootPane().setDefaultButton(bNext);
      return;
    }

    // Create entity
    if (!e.getActionCommand().equals("CREATE"))
      return;

    // Gedcom selected ?
    if (gedcom==null)
      return;

    // Create it
    if (gedcom.startTransaction()) {
      mask.createIn(gedcom);
      gedcom.endTransaction();
    };

    // Done
    frame.dispose();
  }

  /**
   * Creates a button with this as action-listener
   */
  public JButton createButton(String name, String action) {
    JButton result = new JButton(name);
    result.setActionCommand(action);
    result.addActionListener(this);
    return result;
  }

  /**
   * Notification that a change in a Gedcom-object took place.
   */
  public void handleChange(Change change) {
    setContext(change.getGedcom());
  }

  /**
   * Notification that the gedcom is being closed
   */
  public void handleClose(Gedcom which) {

    if (which==gedcom)
      setContext(null);

    which.removeListener(this);

  }

  /**
   * Notification that an entity has been selected.
   */
  public void handleSelection(Selection selection) {
    setContext(selection.getEntity().getGedcom());
  }

  /**
   * One of the checkboxes has been selected
   */
  public void itemStateChanged(ItemEvent e) {
  // Done
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
   * Remembers gedcom/entity as being context for new entity
   */
  private void setContext(Gedcom gedcom) {

    // No gedcom any more ?
    if (gedcom==null) {

      cbMemberOf.setSelected(false);
      cbMemberOf.setText( getResourceString("mask.member_of")+" "+getResourceString("mask.none") );

    } else {

      cbMemberOf.setSelected(true);
      cbMemberOf.setText( getResourceString("mask.member_of")+" "+gedcom.getName() );

    }

    // Remember
    this.gedcom=gedcom;
    this.entity=entity;

    // Tell it to mask
    mask.handleContextChange(gedcom);

    // Done
  }

}
