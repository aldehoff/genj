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
package genj.gedcom;

/**
 * Interface for generic Entity in a genealogic file.
 */
public interface Entity {

  /**
   * Notification to entity that it has been added to a Gedcom
   */
  public void addNotify(Gedcom gedcom);

  /**
   * Notification to entity that it has been deleted from a Gedcom
   */
  public void delNotify();

  /**
   * Gedcom this entity's in
   * @return containing Gedcom
   */
  public Gedcom getGedcom();

  /**
   * Returns entity's id
   * @return id
   */
  public String getId();

  /**
   * Entity's main property
   * @return Property
   */
  public Property getProperty();

  /**
   * Returns the type to which this entity belongs
   * INDIVIDUALS, FAMILIES, MULTIMEDIAS, NOTES, ...
   */
  public int getType();

  /**
   * Set Gedcom this entity's in
   */
  public void setGedcom(Gedcom gedcom);

  /**
   * Sets entity's id
   */
  public void setId(String setId);
  
  /**
   * Adds another foreign xref - normally references between
   * entities are modelled via a property in each entity (e.g.
   * FAMC-CHIL or FAMS-HUSB). They are linked against each 
   * other. If a property doesn't have a complement in an 
   * entity (e.g. NOTE-?) this 'hidden' property handles the
   * back reference (therefore NOTE-ForeignXRef). It is 
   * expected that any functionality overriden in superclass
   * Property (e.g. delProperty) takes these foreigns into
   * consideration
   */
  public void addForeignXRef(PropertyForeignXRef fxref);

}
