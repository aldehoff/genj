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
package genj.edit.beans;

import genj.common.AbstractPropertyTableModel;
import genj.common.PropertyTableModel;
import genj.common.PropertyTableWidget;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.Relationship;
import genj.gedcom.TagPath;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A complex bean displaying parents of an individual
 */
public class ParentsBean extends PropertyBean {
  
  private PropertyTableWidget table;
  
  /**
   * Initialization
   */
  protected void initializeImpl() {
    
    // setup layout & table
    table = new PropertyTableWidget(viewManager);
    table.setContextPropagation(PropertyTableWidget.CONTEXT_PROPAGATION_ON_DOUBLE_CLICK);
    table.setPreferredSize(new Dimension(64,64));
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, table);
    
    // done
  }

  /**
   * Set context to edit
   */
  protected void setContextImpl(Property prop) {

    // a table for the families
    PropertyTableModel model = null;
    if (prop instanceof Indi)
      model = new ParentsOfChild((Indi)prop);
    if (prop instanceof Fam)
      model = new ParentsInFamily((Fam)prop);
    
    table.setModel(model);
    
    // done
  }
  
  private static class ParentsInFamily extends AbstractPropertyTableModel {
    
    private final static TagPath[] PATHS = {
//        new TagPath("FAM:HUSB:*:..", Relationship.LABEL_FATHER),  
//        new TagPath("FAM:HUSB:*:..:NAME"),  
//        new TagPath("FAM:WIFE:*:..", Relationship.LABEL_MOTHER),
//        new TagPath("FAM:WIFE:*:..:NAME")
        new TagPath("INDI"),
        new TagPath("INDI:NAME"),
        new TagPath("INDI:BIRT:DATE"),
        new TagPath("INDI:BIRT:PLAC"),
    };
    
    private Fam fam;
    
    private ParentsInFamily(Fam fam) {
      this.fam = fam;
    }
    public Gedcom getGedcom() {
      return fam.getGedcom();
    }
    public int getNumCols() {
      return PATHS.length;
    }
    public int getNumRows() {
      return fam.getNoOfSpouses();
    }
    public TagPath getPath(int col) {
      return PATHS[col];
    }
    public Property getProperty(int row) {
      return fam.getSpouse(row);
    }
  }
  
  private static class ParentsOfChild extends AbstractPropertyTableModel {
    
    private final static TagPath PATHS[] = {
      new TagPath("FAM"),  
      new TagPath("FAM:HUSB:*:..", Relationship.LABEL_FATHER),  
      new TagPath("FAM:HUSB:*:..:NAME"),  
      new TagPath("FAM:WIFE:*:..", Relationship.LABEL_MOTHER),
      new TagPath("FAM:WIFE:*:..:NAME")
    };
    
    private Indi child;
    private Fam[] familiesWhereChild;
    
    private ParentsOfChild(Indi child) {
      this.child = child;
      familiesWhereChild = child.getFamiliesWhereChild();
    }
      
    public Gedcom getGedcom() {
      return child.getGedcom();
    }
    public int getNumCols() {
      return PATHS.length;
    }
    public int getNumRows() {
      return familiesWhereChild.length;
    }
    public TagPath getPath(int col) {
      return PATHS[col];
    }
    public Property getProperty(int row) {
      return familiesWhereChild[row];
    }
  }

} //ParentsBean
