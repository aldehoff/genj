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
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.Registry;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A complex bean displaying children of a family
 */
public class ChildrenBean extends PropertyBean {

  private final static TagPath PATHS[] = {
    new TagPath("INDI", Gedcom.getName("CHIL")),
    new TagPath("INDI:NAME"),
    new TagPath("INDI:BIRT:DATE"),
    new TagPath("INDI:BIRT:PLAC")
  };
  
  /** fam we're looking at */
  private Fam fam;
  
  /**
   * Finish editing a property through proxy
   */
  public void commit(Transaction tx) {
  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, TagPath setPath, ViewManager setMgr, Registry setReg) {
    super.init(setGedcom, setProp, setPath, setMgr, setReg);

    // we assume we got a famhere
    fam = (Fam)setProp;
    
    // setup layout
    setLayout(new BorderLayout());

    // a table for the families
    PropertyTableModel model = new AbstractPropertyTableModel() {
      public Gedcom getGedcom() {
        return gedcom;
      }
      public int getNumCols() {
        return PATHS.length;
      }
      public int getNumRows() {
        return fam.getNoOfChildren();
      }
      public TagPath getPath(int col) {
        return PATHS[col];
      }
      public Property getProperty(int row) {
        return fam.getChild(row);
      }
    };
    PropertyTableWidget table = new PropertyTableWidget(model, viewManager);
    table.setContextPropagation(PropertyTableWidget.CONTEXT_PROPAGATION_ON_DOUBLE_CLICK);
    table.setPreferredSize(new Dimension(64,64));
    add(BorderLayout.CENTER, table);
    
    // done
  }

} //ChildrenBean
