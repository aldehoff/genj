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
 * Gedcom Property : CHIL
 * The property wrapping the condition of having a child in a family
 */
public class PropertyChild extends PropertyXRef {

  /**
   * Constructor with reference
   * @param target referenced PropertyXRef
   */
  public PropertyChild(PropertyXRef target) {
    super(target);
  }

  /**
   * Constructor with Tag,Value,Level parameters
   * @param tag Tag
   * @param value Value
   * @param level Level
   */
  public PropertyChild(String tag, String value) {
    super(tag,value);
  }

  /**
   * Returns the child
   * @return referenced child
   */
  public Indi getChild() {
    return (Indi)getReferencedEntity();
  }

  /**
   * Returns a warning string that describes what happens when this
   * property would be deleted
   * @return warning as <code>String</code>, <code>null</code> when no warning
   */
  public String getDeleteVeto() {
    return "The connection to the referenced child and its reference to this family are lost";
  }

  /**
   * Returns the Gedcom-Tag of this property
   * @return tag as <code>String</code>
   */
  public String getTag() {
    return "CHIL";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when property has no parent property,
   * referenced individual is child, wife or husband in enclosing family
   * or it would become ancestor of itself by this action.
   */
  public void link() throws GedcomException {

    // Something to do ?
    if (getChild()!=null) {
      return;
    }

    // Get enclosing family ?
    Fam fam;
    try {
      fam = (Fam)getEntity();
    } catch (ClassCastException ex) {
      throw new GedcomException("CHIL can't be linked to individual when not in family");
    }

    // Prepare some VARs
    Property p;
    Property ps[];

    // Look for child (not-existing -> Gedcom throws Exception)
    String id = getReferencedId();
    Indi child = getGedcom().getIndiFromId(id);

    if (child==null) {
      throw new GedcomException("Couldn't find child with ID "+id);
    }

    // Child already has parents ?
    if (child.getFamc()!=null) {
      throw new GedcomException("Individual @"+child.getId()+"@ is already child of a family");
    }

    // Enclosing family has indi as child, husband or wife ?
    if (fam.getWife()==child) {
      throw new GedcomException("Individual @"+id+"@ is already wife in family @"+fam.getId()+"@");
    }
    if (fam.getHusband()==child) {
      throw new GedcomException("Individual @"+id+"@ is already husband in family @"+fam.getId()+"@");
    }

    Indi children[] = fam.getChildren();
    for (int i=0;i<children.length;i++) {
      if ( children[i] == child ) {
        throw new GedcomException("Individual @"+id+"@ is already child in family @"+fam.getId()+"@");
      }
    }

    // Child is ancestor of husband or wife ?
    if (fam.isDescendantOf(child)) {
      throw new GedcomException("Individual @"+id+"@ is ancestor of family @"+fam.getId()+"@");
    }

    // Connect back from child (maybe using back reference)
    ps = child.getProperties(new TagPath("INDI:FAMC"),false);
    PropertyFamilyChild pfc;
    for (int i=0;i<ps.length;i++) {
      pfc = (PropertyFamilyChild)ps[i];
      if ( (!pfc.isValid()) && (pfc.getReferencedId().equals(child.getId())) ) {
        pfc.setTarget(this);
        setTarget(pfc);
        return;
      }
    }

    // .. new back referencing property
    pfc = new PropertyFamilyChild(this);
    setTarget(pfc);
    child.addProperty(pfc);

    // Done
  }

  /**
   * The expected referenced type
   */
  public int getExpectedReferencedType() {
    return Gedcom.INDIVIDUALS;
  }

}
