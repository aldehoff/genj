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
 * Gedcom Property : WIFE
 * Property wrapping the condition of having a wife in a family
 */
public class PropertyWife extends PropertyXRef {

  /**
   * Constructor with reference
   */
  public PropertyWife(PropertyXRef target) {
    super(target);
  }

  /**
   * Constructor with Tag,Value parameters
   */
  public PropertyWife(String tag, String value) {
    super(tag,value);
  }

  /**
   * Returns a warning string that describes what happens when this
   * property would be deleted
   * @return warning as <code>String</code>, <code>null</code> when no warning
   */
  public String getDeleteVeto() {
    return "The connection to the referenced wife and its reference to this family are lost";
  }

  /**
   * Returns the Gedcom-Tag of this property
   */
  public String getTag() {
    return "WIFE";
  }

  /**
   * Returns the wife
   */
  public Indi getWife() {
    return (Indi)getReferencedEntity();
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when property has no parent property,
   * or a double husband/wife situation would be the result
   */
  public void link() throws GedcomException {

    // Something to do ?
    if (getWife()!=null) {
      return;
    }

    // Get enclosing family ?
    Fam fam = null;
    try {
      fam = (Fam)getEntity();
    } catch (ClassCastException ex) {
    }
    if (fam==null)
      throw new GedcomException("WIFE can't be linked to individual when not in family");

    // Prepare some VARs
    Property p;
    Property ps[];

    // Enclosing family has a wife already ?
    if (fam.getWife()!=null)
      throw new GedcomException("Family @"+fam.getId()+"@ can't have two wifes");

    // Look for wife (not-existing -> Gedcom throws Exception)
    String id = getReferencedId();
    Indi wife = getGedcom().getIndiFromId(id);
    if (wife==null)
      throw new GedcomException("Couldn't find wife with ID "+id);

    // Enclosing family has indi as child or husband ?
    if (fam.getHusband()==wife)
      throw new GedcomException("Individual @"+id+"@ is already husband in family @"+fam.getId()+"@");

    Indi children[] = fam.getChildren();
    for (int i=0;i<children.length;i++) {
      if ( children[i] == wife )
        throw new GedcomException("Individual @"+id+"@ is already child in family @"+fam.getId()+"@");
    }

    // Connect back from husband (maybe using invalid back reference)
    ps = wife.getProperties(new TagPath("INDI:FAMS"),false);
    PropertyFamilySpouse pfs;
    for (int i=0;i<ps.length;i++) {
      pfs = (PropertyFamilySpouse)ps[i];
      if ( (!pfs.isValid()) && (pfs.getReferencedId().equals(fam.getId())) ) {
        pfs.setTarget(this);
        setTarget(pfs);
        return;
      }
    }

    // .. new back referencing property
    pfs = new PropertyFamilySpouse(this);
    wife.addProperty(pfs);
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
