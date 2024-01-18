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

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;

/**
 * Add a new entity  
 */
public class CreateEntity extends AbstractChange {

  /** the type of the added entity*/
  private int type;
  
  /**
   * Constructor
   */
  public CreateEntity(Gedcom ged, int typ) {
    super(ged, newImages[typ], resources.getString("new", Gedcom.getNameFor(typ, false) ));
    type = typ;
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
   */
  protected String getConfirmMessage() {
    // You are about to create a {0} in {1}!
    String about = resources.getString("confirm.new", new Object[]{ Gedcom.getNameFor(type,false), gedcom});
    // This entity will not be connected ... / This entity will be {0}.
    String detail = resources.getString("confirm.new.unrelated");
    // done
    return about + '\n' + detail;
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#change()
   */
  protected void change() throws GedcomException {
    // create the entity
    focus = gedcom.createEntity(type, null);
    focus.getProperty().addDefaultProperties();
    // done
  }
  
} //Create

