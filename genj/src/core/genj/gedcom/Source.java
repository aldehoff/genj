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
 * Class for encapsulating a source
 * Basic strategy was to copy parts of note and media and then strip
 * out as much as I could.
 */
public class Source extends PropertySource implements Entity {

  private String id = "";
  private Gedcom gedcom;

  /**
   * Constructor for Source
   */
  /*package*/ Source() {
    super(null);
  }

  /**
   * Notification to entity that it has been added to a Gedcom
   */
  public void addNotify(Gedcom gedcom) {
    this.gedcom = gedcom;
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
    return Gedcom.SOURCES;
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

    // look for text
    for (int i=0;i<getNoOfProperties();i++) {
      Property child = getProperty(i);
      if (child.getTag().equals("TEXT")) 
        return getId()+":"+child.getValue();
    }

    // id only    
    return getId();
  }
  
} //Source
