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
 * Property wrapping the condition of having an association to 
 * another entity
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
   * @see genj.gedcom.Property#addDefaultProperties()
   */
  public Property addDefaultProperties() {
    Entity e = getReferencedEntity();
    addProperty(new PropertyGenericAttribute("TYPE",e==null?"":getGedcom().getTagFor(e.getType())));
    addProperty(new PropertyGenericAttribute("RELA"));
    return this;
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

    // Get enclosing indi?
    Indi indi;
    try {
      indi = (Indi)getEntity();
    } catch (ClassCastException ex) {
      throw new GedcomException("ASSO can't be linked when not in indi");
    }

    // Look for entity
    String id = getReferencedId();
    if (id.length()==0) return;

    Entity ent = (Entity)getGedcom().getEntity(id);
    if (ent == null) 
      return;

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    ent.addForeignXRef(fxref);

    // ... and point
    setTarget(fxref);

    // Done
  }

  /**
   * The expected referenced type
   */
  public int getExpectedReferencedType() {
    return Gedcom.INDIVIDUALS;
  }
  
} //PropertyAssociation
