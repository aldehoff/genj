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

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import genj.gedcom.*;
import genj.util.*;
import genj.util.swing.*;

/**
 * Mask for creating : FAMILY
 */
class MaskForNewFam extends MaskForNewEntity implements ItemListener {

  private Entity  entity= null;
  private boolean intern=false;

  JLabel       lMarrDate,lMarrPlace,lNote;
  DateEntry    dMarrDate;
  JTextField   tMarrPlace;

  JLabel       lMemberIs;
  JCheckBox    cbMemberIs;
  JRadioButton rbParent  ,
               rbChild   ;

  /**
   * Initializer
   */
  protected void init(Option option) {

    super.init(option);

    // Family Data
    lMarrDate  = new JLabel(getResourceString("mask.fam.marr_date"));
    lMarrPlace = new JLabel(getResourceString("mask.fam.marr_place"));

    tMarrPlace = new JTextField("");
    dMarrDate  = new DateEntry(null,null,null);

    // Relation
    cbMemberIs  = new JCheckBox(getResourceString("mask.fam.member_is"),false);
    cbMemberIs .setEnabled(false);
    lMemberIs   = new JLabel(getResourceString("mask.none"));
    lMemberIs  .setBackground(new Color(255,255,239));
    cbMemberIs.addItemListener(this);

    rbParent  = new JRadioButton(getResourceString("mask.fam.parent"),true );
    rbChild   = new JRadioButton(getResourceString("mask.fam.child" ),false);

    rbParent .setToolTipText(getResourceString("mask.fam.parent.tip"));
    rbChild  .setToolTipText(getResourceString("mask.fam.child.tip"));

    ButtonGroup g2 = new ButtonGroup();
    g2.add(rbParent);
    g2.add(rbChild);

    // Init
    handleContextChange(null);

    // Done
  }

  /**
   * Initiates entity's creation
   */
  boolean createIn(Gedcom gedcom) {

    // Talking about the same Gedcom ?
    if ((entity!=null)&&(entity.getGedcom()!=gedcom)) {
      return false;
    }

    // Create it
    int memberIs = Gedcom.REL_NONE;

    if (cbMemberIs.isSelected() == true) {

      if (rbParent .isSelected() && rbParent.isEnabled() )
      memberIs = Gedcom.REL_PARENT;
      if (rbChild  .isSelected() && rbChild .isEnabled() )
      memberIs = Gedcom.REL_CHILD;

    }

    PropertyDate p = new PropertyDate();
    p.getStart().set(dMarrDate.getDay(),dMarrDate.getMonth(),dMarrDate.getYear());

    Fam fam;
    try {
      fam = gedcom.createFam(memberIs,entity);
    } catch (GedcomException e) {
      Debug.log(Debug.WARNING, this, e);
      return false;
    }
    fam.addMarriage(p.toString(),tMarrPlace.getText());

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

    helper.add(lMarrDate ,1,1,1,1);
    helper.add(dMarrDate ,2,1,1,1,helper.GROW_HORIZONTAL);
    helper.add(lMarrPlace,1,2,1,1);
    helper.add(tMarrPlace,2,2,1,1,helper.GROW_HORIZONTAL);

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

    helper.add(firstRow  ,0,0,3,1);
    helper.add(cbMemberIs,0,1,2,1);
    helper.add(lMemberIs ,2,1,1,1);
    helper.add(Box.createHorizontalStrut(6) ,0,2,1,1);
    helper.add(rbParent  ,1,2,1,1);
    helper.add(rbChild   ,2,2,1,1);

    // Done
    return result;
  }

  /**
   * One of the checkboxes has been selected
   */
  public void itemStateChanged(ItemEvent e) {

    // Not always a reaction
    if (intern) {
      return;
    }

    // No relationship any more / again
    if (!cbMemberIs.isSelected()) {

      rbParent  .setEnabled(false);
      rbChild   .setEnabled(false);

    } else {
      if (entity!=null) {
        handleContextChange(entity.getGedcom());
      }
    }

    // Done
  }            

  /**
   * Sets mask's entity
   */
  void handleContextChange(Gedcom gedcom) {

    // Check out if we can find an entity to relate to
    Entity newEntity = null;

    if (gedcom!=null) {

      // .. the last one?
      newEntity = gedcom.getLastEntity();
      if (!(newEntity instanceof Indi)) {

        // .. the first Indi?
        try { newEntity = gedcom.getIndi(0); } catch (Exception e) {}

      }
    }

    // No more?
    if (newEntity==null) {

      this.entity=null;

      rbParent  .setEnabled(false);
      rbChild   .setEnabled(false);
      intern = true;
      cbMemberIs.setSelected(false);
      cbMemberIs.setEnabled (false);
      intern = false;

      lMemberIs.setText( getResourceString("mask.none") );
      lMemberIs.setIcon( null );

      return;
    }

    // Remember the entity
    entity = newEntity;

    // Individual !
    Indi indi = (Indi)entity;

    lMemberIs.setText( "@"+indi.getId()+"@" );
    lMemberIs.setIcon( ImgIconConverter.get(Property.getDefaultImage("indi")) );

    rbParent .setEnabled(true);
    rbChild  .setEnabled(indi.getFamc() == null);

    intern = true;
    cbMemberIs.setEnabled(true);
    cbMemberIs.setSelected(true);
    intern = false;

    // Done
  }

}
