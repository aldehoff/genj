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
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PDelete - delete a property
 */  
public class DelProperty extends AbstractChange {
  
  /** the candidates to delete */
  private Set<Property> candidates = new HashSet<Property>();
  
  /**
   * Constructor
   */
  public DelProperty(Property property) {
    super(property.getGedcom(), Images.imgDel, resources.getString("delete"));
    candidates.add(property);
  }

  /**
   * Constructor
   */
  public DelProperty(List<? extends Property> properties) {
    super(properties.get(0).getGedcom(), Images.imgDel, resources.getString("delete"));
    candidates.addAll(properties);
  }

  /**
   * Perform the delete
   */
  protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
    for (Property prop : candidates) 
      prop.getParent().delProperty(prop);
    return null;
  }
  
} //DelProperty

