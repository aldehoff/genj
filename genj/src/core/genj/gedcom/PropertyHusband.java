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

import java.util.*;

import genj.util.*;

/**
 * Gedcom Property : HUSB
 * Property wrapping the condition of having a husband in a family
 */
public class PropertyHusband extends PropertyXRef {

  /**
   * Constructor with reference
   */
  public PropertyHusband(PropertyXRef target) {
    super(target);
  }

  /**
   * Constructor with Tag,Value parameters
   */
  public PropertyHusband(String tag, String value) {
    super(tag,value);
  }

  /**
   * Returns a warning string that describes what happens when this
   * property would be deleted
   * @return warning as <code>String</code>, <code>null</code> when no warning
   */
  public String getDeleteVeto() {
    return "The connection to the referenced husband and its reference to this family are lost";
  }

  /**
   * Returns the husband
   */
  public Indi getHusband() {
    return (Indi)getReferencedEntity();
  }

  /**
   * Returns the Gedcom-Tag of this property
   */
  public String getTag() {
    return "HUSB";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when property has no parent property,
   * or a double husband/wife situation would be the result
   */
  public void link() throws GedcomException {

    // Something to do ?
    if (getHusband()!=null) {
      return;
    }

    // Get enclosing family ?
    Fam fam;
    try {
      fam = (Fam)getEntity();
    } catch (ClassCastException ex) {
      throw new GedcomException("HUSB can't be linked to individual when not in family");
    }

    // Prepare some VARs
    Property p;
    Property ps[];

    // Enclosing family has a husband already ?
    if (fam.getHusband()!=null)
      throw new GedcomException("Family @"+fam.getId()+"@ can't have two husbands");

    // Look for husband (not-existing -> Gedcom throws Exception)
    String id = getReferencedId();
    Indi husband = getGedcom().getIndiFromId(id);

    if (husband==null)
      throw new GedcomException("Couldn't find husband with ID "+id);

    // Enclosing family has indi as child or wife ?
    if (fam.getWife()==husband)
      throw new GedcomException("Individual @"+id+"@ is already wife in family @"+fam.getId()+"@");

    Indi children[] = fam.getChildren();
    for (int i=0;i<children.length;i++) {
      if ( children[i] == husband )
      throw new GedcomException("Individual @"+id+"@ is already child in family @"+fam.getId()+"@");
    }

    // Connect back from husband (maybe using invalid back reference)
    ps = husband.getProperties(new TagPath("INDI:FAMS"),false);
    PropertyFamilySpouse pfs;
    for (int i=0;i<ps.length;i++) {
      pfs = (PropertyFamilySpouse)ps[i];
      if ( (!pfs.isValid()) && (pfs.getReferencedId().equals(fam.getId())) ) {
      pfs.setTarget(this); // Changed Oct 23 from pfs.setTarget(pfs);
      setTarget(pfs);      // Inserted Oct 23
      return;
      }
    }

    // .. new back referencing property
    pfs = new PropertyFamilySpouse(this);
    husband.addProperty(pfs);
    setTarget(pfs);

    // Done
  }

  /**
   * The expected referenced type
   */
  public int getExpectedReferencedType() {
    return Gedcom.INDIVIDUALS;
  }
}
