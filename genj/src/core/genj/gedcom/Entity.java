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
 * Abstract base type for all Entities
 */
public abstract class Entity extends Property {
  
  /** the containing gedcom */
  private Gedcom gedcom;
  
  /** the id */
  private String id;
  
  /** the tag */
  private String tag;
  
  /** the type */
  private int type;

  /**
   * Notification to entity that it has been added to a Gedcom
   */
  public void addNotify(Gedcom ged) {
    gedcom = ged;
    
    type = ged.getTypeFor(getClass());
    tag = ged.getTagFor(type);
    
  }

  /**
   * Gedcom this entity's in
   * @return containing Gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * @see genj.gedcom.Property#getEntity()
   */
  public Entity getEntity() {
    return this;
  }

  /**
   * Returns entity's id
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets entity's id
   */
  public void setId(String setId) {
    id = setId;
  }
  
  /**
   * Returns the type to which this entity belongs
   * INDIVIDUALS, FAMILIES, MULTIMEDIAS, NOTES, ...
   */
  public int getType() {
    return type;
  }

  /**
   * @see genj.gedcom.Property#toString()
   */
  public String toString() {
    return getTag() + ' ' + id;
  }

  /**
   * @see genj.gedcom.PropertyNote#getProxy()
   */
  public String getProxy() {
    return "Entity";
  }
  
  /**
   * @see genj.gedcom.Property#getTag()
   */
  public String getTag() {
    return tag;
  }
  
  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  public void setTag(String tag) {
  }
  
  /**
   * @see genj.gedcom.Property#getValue()
   */
  public String getValue() {
    return EMPTY_STRING;
  }
  
  /**
   * @see genj.gedcom.Property#setValue(java.lang.String)
   */
  public void setValue(String value) {
  }

} //Entity
