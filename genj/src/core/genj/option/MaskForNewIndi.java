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
import genj.util.swing.ImgIconConverter;

/**
 * Mask for creating : Individual
 */
class MaskForNewIndi extends MaskForNewEntity implements ItemListener {

  private Entity entity   = null;
  private boolean intern  = false;

  private JLabel       lFirst,lLast,lSex;
  private JTextField   tLast,tFirst;
  private JRadioButton rbMale    ,
                       rbFemale  ;

  private JCheckBox    cbRelative;
  private JLabel       lRelative;
  private JRadioButton rbSibling ,
                       rbParent  ,
                       rbChild   ,
                       rbSpouse  ;

  /**
   * Initializer
   */
  protected void init(Option option) {

    super.init(option);

    // Create components for data
    lFirst    = new JLabel(PropertyName.getLabelForFirstName());
    lLast     = new JLabel(PropertyName.getLabelForLastName() );
    lSex      = new JLabel(PropertySex.getLabelForSex()       );

    tFirst = new JTextField("");
    tLast  = new JTextField("");

    rbMale   = new JRadioButton(PropertySex.getLabelForSex(Gedcom.MALE),false);
    rbFemale = new JRadioButton(PropertySex.getLabelForSex(Gedcom.FEMALE),false);
    ButtonGroup g1 = new ButtonGroup();
    g1.add(rbMale);
    g1.add(rbFemale);

    // Create components for relation
    cbRelative  = new JCheckBox(getResourceString("mask.indi.relative_of"));
    cbRelative.addItemListener(this);
    lRelative   = new JLabel(getResourceString("mask.none"));
    lRelative  .setBackground(new Color(255,255,239));

    rbSibling = new JRadioButton(getResourceString("mask.indi.sibling"),false);
    rbParent  = new JRadioButton(getResourceString("mask.indi.parent") ,false);
    rbChild   = new JRadioButton(getResourceString("mask.indi.child" ) ,false);
    rbSpouse  = new JRadioButton(getResourceString("mask.indi.spouse") ,true );

    rbSibling.setToolTipText(getResourceString("mask.indi.sibling.tip"));
    rbParent .setToolTipText(getResourceString("mask.indi.parent.tip"));
    rbChild  .setToolTipText(getResourceString("mask.indi.child.tip"));
    rbSpouse .setToolTipText(getResourceString("mask.indi.spouse.tip"));

    ButtonGroup g2 = new ButtonGroup();
    g2.add(rbSibling);
    g2.add(rbParent);
    g2.add(rbChild);
    g2.add(rbSpouse);

    // Init
    handleContextChange(null);

    // Done
  }

  /**
   * Initiates entity's creation
   */
  protected boolean createIn(Gedcom gedcom) {

    // Talking about the same Gedcom ?
    if ((entity!=null)&&(entity.getGedcom()!=gedcom))
      return false;

    // Create it
    int relatedTo = Gedcom.REL_NONE;

    if (cbRelative.isSelected() == true) {
      if (rbSibling.isSelected() && rbSibling.isEnabled() )
      relatedTo = Gedcom.REL_SIBLING;
      if (rbParent .isSelected() && rbParent .isEnabled())
      relatedTo = Gedcom.REL_PARENT;
      if (rbChild  .isSelected() && rbChild  .isEnabled())
      relatedTo = Gedcom.REL_CHILD;
      if (rbSpouse .isSelected() && rbSpouse .isEnabled())
      relatedTo = Gedcom.REL_SPOUSE;
    }

    String last = tLast.getText(), first = tFirst.getText();
    int sex = rbMale.isSelected() ? Gedcom.MALE : (rbFemale.isSelected() ? Gedcom.FEMALE : -1) ;

    try {
      gedcom.createIndi(tLast.getText(),tFirst.getText(),sex,relatedTo,entity);
    } catch (GedcomException e) {
      Debug.log(Debug.WARNING, this, e);
      return false;
    }

    // Clear
    tLast.setText("");
    tFirst.setText("");
    rbMale.setSelected(false);
    rbFemale.setSelected(false);

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

    helper.add(lFirst  ,1,1,1,1);
    helper.add(lLast   ,1,2,1,1);
    helper.add(lSex    ,1,3,1,1);

    helper.add(tFirst  ,2,1,2,1);
    helper.add(tLast   ,2,2,2,1);
    helper.add(rbMale  ,2,3,1,1);
    helper.add(rbFemale,3,3,1,1);

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
    helper.add(cbRelative,0,1,2,1);
    helper.add(lRelative ,2,1,1,1);
    helper.add(Box.createHorizontalStrut(6) ,0,2,1,1);
    helper.add(rbSibling ,1,2,1,1);
    helper.add(rbChild   ,2,2,1,1);
    helper.add(rbParent  ,1,3,1,1);
    helper.add(rbSpouse  ,2,3,1,1);

    // Done
    return result;
  }

  /**
   * One of the checkboxes has been selected
   */
  public void itemStateChanged(ItemEvent e) {

    // Not always reaction
    if (intern)
      return;

    // No relationship any more / again
    if (!cbRelative.isSelected()) {

      rbSibling.setEnabled(false);
      rbParent .setEnabled(false);
      rbChild  .setEnabled(false);
      rbSpouse .setEnabled(false);

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
  protected void handleContextChange(Gedcom gedcom) {

    // Check out if we can find an entity to relate to
    Entity newEntity = null;
    if (gedcom!=null) {

      // .. the last one?
      newEntity = gedcom.getLastEntity();
      if (newEntity==null) {

        // .. the first Indi?
        try { newEntity = gedcom.getIndi(0); } catch (Exception e) {}

        // .. the first fam?
        if (newEntity==null)
          try { newEntity = gedcom.getFam(0); } catch (Exception e) {}
      }
    }

    // No entity any more ?
    if (newEntity==null) {

      this.entity = null;

      rbSibling .setEnabled(false);
      rbParent  .setEnabled(false);
      rbChild   .setEnabled(false);
      rbSpouse  .setEnabled(false);
      intern = true;
      cbRelative.setSelected(false);
      cbRelative.setEnabled (false);
      intern = false;

      lRelative.setText(getResourceString("mask.none"));
      lRelative.setIcon( null );

      return;
    }

    // Individual ?
    if (newEntity instanceof Indi) {
      entity = newEntity;
      Indi indi = (Indi)entity;

      lRelative.setText( "@"+indi.getId()+"@" );
      lRelative.setIcon( ImgIconConverter.get(Property.getDefaultImage("indi")) );

      rbSibling.setEnabled(true);
      rbParent .setEnabled(   indi.getFamc() == null
                || indi.getFamc().hasMissingSpouse() );

      rbChild  .setEnabled(true);
      rbSpouse .setEnabled(true);

      intern = true;
      cbRelative.setEnabled(true);
      cbRelative.setSelected(true);
      intern = false;

      return;
    }

    // Family ?
    if (newEntity instanceof Fam) {
      entity = newEntity;
      Fam fam = (Fam)entity;

      lRelative.setText( "@"+fam.getId()+"@" );
      lRelative.setIcon( ImgIconConverter.get(Property.getDefaultImage("fam")) );

      rbSibling.setEnabled(false);
      rbParent .setEnabled(false);
      rbChild  .setEnabled(true);
      rbSpouse .setEnabled(fam.hasMissingSpouse());

      intern = true;
      cbRelative.setEnabled(true);
      cbRelative.setSelected(true);
      intern = false;

      return;
    }

    // Done
  }
}
