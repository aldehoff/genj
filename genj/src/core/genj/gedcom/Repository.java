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

import java.util.Date;

/**
 * Class for encapsulating a repository
 */
public class Repository extends PropertyRepository implements Entity {

  private String id = "";
  private Gedcom gedcom;
  private PropertySet foreignXRefs = new PropertySet();

  /**
   * Constructor for Repository
   */
  /*package*/ Repository() {
    super(null);
  }

  /**
   * Adds a PropertyForeignXRef to this entity
   */
  public void addForeignXRef(PropertyForeignXRef fxref) {
    foreignXRefs.add(fxref);
  }

  /**
   * Notification to entity that it has been added to a Gedcom
   */
  public void addNotify(Gedcom gedcom) {
    this.gedcom = gedcom;
  }

  /**
   * Notification to entity that it has been deleted from a Gedcom
   */
  public void delNotify() {

    // Notify to properties
    super.delNotify();

    // Remove all foreign XRefs
    foreignXRefs.deleteAll();

    // Break connection
    this.gedcom = null;
  }

  /**
   * Removes a property
   * This overrides the default behaviour by first
   * looking in this entity's foreign list
   */
  public boolean delProperty(Property which) {

    if (foreignXRefs.contains(which)) {
      foreignXRefs.delete(which);
      return true;
    }
    return super.delProperty(which);
  }

  /**
   * Gedcom this entity's in
   * @return containing Gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }

  /**
   * Returns this entity's id.
   */
  public String getId() {
    return id;
  }

  /**
   * Returns this entity's first property
   */
  public Property getProperty() {
    return this;
  }

  /**
   * Returns the type to which this entity belongs
   * INDIVIDUALS, FAMILIES, MULTIMEDIAS, NOTES, ...
   */
  public int getType() {
    return Gedcom.REPOSITORIES;
  }

  /**
   * Set Gedcom this entity's in
   */
  public void setGedcom(Gedcom gedcom) {
    this.gedcom=gedcom;
  }

  /**
   * Sets entity's id.
   * @param id new id
   */
  public void setId(String id) {
    this.id=id;
  }

  /**
   * Returns this property as a string
   */
  public String toString() {
    return getId()+":"+super.toString();
  }

  /**
   * @see genj.gedcom.Entity#addLink(Property, Class)
   */
  public void addLink(Property owner, Class anchor) {
    // Create repository on owner's end
    owner.addProperty(new PropertyRepository(this));
    // done
  }

} //Repository
