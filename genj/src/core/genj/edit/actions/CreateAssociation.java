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
package genj.edit.actions;

import genj.edit.Images;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertyAssociation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Create an association 
 */
public class CreateAssociation extends AbstractChange {
  
  /** gedcom */
  private Gedcom ged;
  
  /** individual */
  private Indi indi;
  
  /** associated with */
  private Entity target;
  
  /**
   * Constructor
   */
  public CreateAssociation(Indi inDi) {
    super(inDi.getGedcom(), Images.imgNewLink, resources.getString("new", Gedcom.getName("ASSO")));
    indi = inDi;
    ged = indi.getGedcom();
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#change()
   */
  protected void change() throws GedcomException {
    // have a target?
    if (target==null) return;
    // add association
    PropertyAssociation pa = new PropertyAssociation("ASSO", target.getId());
    indi.addProperty(pa);
    pa.link();
    pa.addDefaultProperties();
    
    // done
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
   */
  protected String getConfirmMessage() {
    // setup right?
    if (target==null) return "";
    //You are about to associate INDI {0} with entity {1}!
    return resources.getString("confirm.associate", new String[]{
      indi.toString(), target.getId()
    });
  }
  
  /**
   * @see genj.edit.actions.AbstractChange#getOptions()
   */
  protected JComponent getOptions() {
    
    // Combos
    final JComboBox types = new JComboBox();
    for (int t=0; t<Gedcom.NUM_TYPES; t++) {
      types.addItem("With "+Gedcom.getNameFor(t, false));
    }
    final JComboBox ents = new JComboBox(ged.getEntities(Gedcom.INDIVIDUALS).toArray());
    
    // wrap up
    final JPanel result = new JPanel(new BorderLayout());
    result.add(types, BorderLayout.WEST);
    result.add(ents , BorderLayout.CENTER);

    // listen
    types.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ents.setModel(new DefaultComboBoxModel(ged.getEntities(types.getSelectedIndex()).toArray()));
        if (ents.getSelectedItem()!=null) 
          target = (Entity)ents.getSelectedItem();
        result.firePropertyChange("type", 0, 1);        
      }
    });
    
    ents .addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        target = (Entity)ents.getSelectedItem();
        result.firePropertyChange("target", 0, 1);        
      }
    });
    
    ents.setSelectedIndex(0);
    
    // done
    return result;
  }

} //CreateAssociation

