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

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.event.*;

import genj.gedcom.*;
import genj.util.*;

/**
 * A proxy for a property that links entities
 */
class ProxyXRef extends Proxy implements ActionListener  {

  /** the editor */
  /*package*/ EditView edit;

  /** the textfield we use */
  private JTextField tfield;

  /**
   * When button is pressed
   */
  public void actionPerformed(ActionEvent e) {

    // Jump to linked entity ?
    if ("JUMP".equals(e.getActionCommand())) {
      jump();
      return;
    }

    // Jump to linked entity ?
    if ("JUMP".equals(e.getActionCommand())) {
      jump();
      return;
    }

    // Find entity-id by entered information ?
    if ("FIND".equals(e.getActionCommand())) {
      find();
      return;
    }

    // Link entered entity-id ?
    if ("LINK".equals(e.getActionCommand())) {
      link();
      return;
    }

   // Unknown
    return;
  }

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {

    // Has something been edited ?
    if ( !hasChanged() ) {
      return;
    }

    // Store changed value
    prop.setValue(tfield.getText());

    // Done
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {

    // Already Linked?
    if (tfield==null) {
      return false;
    }

    String id = ((PropertyXRef)prop).getReferencedId();
    return !tfield.getText().equals(id);
  }

  /**
   * Find id by entered information
   */
  private void find() {

    // Check if we have something to look for
    String text = tfield.getText();
    if (text.length()==0) {
      return;
    }

    // First we analyze what kind of target to expect
    PropertyXRef xref = (PropertyXRef)prop;

    int type = xref.getExpectedReferencedType();
    String name = Gedcom.getNameFor(type, false);

    // Then we try to find it through Gedcom
    EntityList all = prop.getGedcom().getEntities(type);

    Vector hits = new Vector(10);
    for (int e=0;e<all.getSize();e++) {

      // .. here's the entity we're checking (ignoring current)
      Entity entity = all.get(e);
      if (entity==edit.getEntity()) {
        continue;
      }

      // .. do the lookup
      Property property = entity.getProperty().find(text);
      if (property!=null) {
        hits.addElement(new Hit(property));
      }
    }

    // Anything found?
    if (hits.size()==0) {
      JOptionPane.showMessageDialog(
        edit,
        EditView.resources.getString("proxy.find.none", name),
        EditView.resources.getString("proxy.find.result"),
        JOptionPane.INFORMATION_MESSAGE
      );
      return;
    }

    // Then we display the result for the user to choose from
    Hit result = (Hit)JOptionPane.showInputDialog(
      edit,
      EditView.resources.getString("proxy.find.select")+" "+Gedcom.getNameFor(type,false),
      EditView.resources.getString("proxy.find.result"),
      JOptionPane.QUESTION_MESSAGE,
      null,
      hits.toArray(),
      null
    );

    // .. something?
    if (result==null) {
      return;
    }

    // Change the value
    tfield.setText(result.getProperty().getEntity().getId());

    // Done

  }

  /**
   * Type that wraps a find hit
   */
  private class Hit {
    /** the property that matched */
    private Property prop;
    /** text representation */
    private String text;
    /** constructor */
    /*package*/ Hit(Property pProp) {
      prop = pProp;
      text = Text.truncate(pProp.getEntity().toString(), 48, "...")+" | "
          +prop.getTag()+":"+Text.truncate(prop.getValue(), 32, "...");
    }
    /** getter */
    /*package*/ Property getProperty() {
      return prop;
    }
    /** toString */
    public String toString() {
      return text;
    }
    // EOC
  }

  /**
   * Link to referenced entity
   */
  private void link() {

    // .. try to write
    Gedcom gedcom = prop.getGedcom();

    if (!gedcom.startTransaction()) {
      return;
    }

    // Try to link
    String id = "@"+tfield.getText()+"@";
    prop.setValue(id);
    try {
      ((PropertyXRef)prop).link();
      edit.setEntity(prop.getEntity());
    } catch (GedcomException ex) {
      JOptionPane.showMessageDialog(
        edit.getFrame(),
        ex.getMessage(),
        EditView.resources.getString("error"),
        JOptionPane.ERROR_MESSAGE
      );
    }

    // Unlock
    gedcom.endTransaction();

    // Done
    return;
  }

  /**
   * Jump to referenced entity
   */
  private void jump() {

    Entity e = ((PropertyXRef)prop).getReferencedEntity();
    if (e==null) {
      return;
    }
    edit.setEntity( e );
  }

  /**
   * Start editing a property through proxy
   */
  protected void start(JPanel in, JLabel setLabel, Property setProp, EditView edit) {

    // Remember property & edit
    prop=setProp;
    this.edit=edit;

    // Calculate reference information
    PropertyXRef pxref = (PropertyXRef) prop;

    // Valid link ?
    if (pxref.getReferencedEntity()!=null) {
      Property p = pxref.getReferencedEntity().getProperty();
      JButton b = createButton(
        EditView.resources.getString("proxy.jump_to",pxref.getReferencedEntity().getId()),
        "JUMP",
        true,
        this,
        p.getImage(true)
      );

      in.add(b);

      JTextArea ta = new JTextArea(p.toString());
      ta.setEditable(false);
      JScrollPane jsp = new JScrollPane(ta);
      jsp.setAlignmentX(0F);
      in.add(jsp);
      return;
    }

    // Not valid link ?
    tfield = createTextField(
      pxref.getReferencedId(),
      "!VALUE",
      null,
      EditView.resources.getString("proxy.enter_id_here")
    );
    in.add(tfield);

    JButton link = createButton(
      EditView.resources.getString("proxy.link"),
      "LINK",
      true,
      this,
      new ImgIcon(this,"Link.gif")
    );
    in.add(link);

    JButton find = createButton(
      EditView.resources.getString("proxy.find"),
      "FIND",
      true,
      this,
      new ImgIcon(this,"Find.gif")
    );
    in.add(find);

    tfield.requestFocus();

    // Done
  }

}
