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
 * Gedcom Property : ASSO
 * Property wrapping the condition of a property having an association 
 * to another entity
 */
public class PropertyAssociation extends PropertyXRef {

  /**
   * Constructor with reference
   */
  public PropertyAssociation(PropertyXRef target) {
    super(target);
  }

  /**
   * Constructor with Tag,Value parameters
   */
  public PropertyAssociation(String tag, String value) {
    super(tag,value);
  }
  
  /**
   * Returns a warning string that describes what happens when this
   * property would be deleted
   * @return warning as <code>String</code>, <code>null</code> when no warning
   */
  public String getDeleteVeto() {
    return "The association to the referenced entity is lost";
  }

  /**
   * Returns the Gedcom-Tag of this property
   */
  public String getTag() {
    return "ASSO";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when property has no parent property,
   * or a double husband/wife situation would be the result
   */
  public void link() throws GedcomException {

    // Something to do ?
    if (getReferencedEntity()!=null) {
      return;
    }

    // Look for entity
    String id = getReferencedId();
    if (id.length()==0) {
      return;
    } 

    Entity ent = (Entity)getGedcom().getEntity(id);
    if (ent==null) 
      throw new GedcomException("Couldnt't find individual with ID "+id);
    if (!(ent instanceof Indi)) {
      throw new GedcomException("Target of ASSO has to be Individual");
    }

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    ent.addProperty(fxref);

    // ... and point
    setTarget(fxref);

    // .. update type
    Property type = getProperty("TYPE");
    if (type!=null) type.setValue(getGedcom().getTagFor(ent.getType()));

    // Done
  }

  /**
   * The expected referenced type
   */
  public int getExpectedReferencedType() {
    return Gedcom.INDIVIDUALS;
  }
  
} //PropertyAssociation
