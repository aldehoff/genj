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
 * Gedcom Property : FAMC
 * A property wrapping the condition of being a child in a family
 */
public class PropertyFamilyChild extends PropertyXRef {

  /**
   * Empty Constructor
   */
  public PropertyFamilyChild() {
  }
  
  /**
   * Constructor with reference
   */
  public PropertyFamilyChild(String target) {
    setValue(target);
  }

  /**
   * Constructor with reference
   */
  public PropertyFamilyChild(PropertyXRef target) {
    super(target);
  }

  /**
   * Returns the reference to family
   */
  public Fam getFamily() {
    return (Fam)getReferencedEntity();
  }

  
  

  /**
   * Returns the Gedcom-Tag of this property
   */
  public String getTag() {
    return "FAMC";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when property has no parent property,
   * referenced individual is child, wife or husband in enclosing family
   * or it would become ancestor of itself by this action.
   */
  public void link() throws GedcomException {

    // Something to do ?
    if (getFamily()!=null) {
      return;
    }

    // Get enclosing individual ?
    Indi indi;
    try {
      indi = (Indi)getEntity();
    } catch (ClassCastException ex) {
      throw new GedcomException("FAMS can't be linked to family when not in individual");
    }

    // Prepare some VARs
    Property p;
    Property ps[];

    // Enclosing individual has a childhood already ?
// FIXME need to refactor ADOP as event with CHIL
//    if (indi.getFamc()!=null)
//      throw new GedcomException("Individual @"+indi.getId()+"@ is already child of a family");

    // Look for family (not-existing -> Gedcom throws Exception)
    String id = getReferencedId();
    Fam fam = (Fam)getGedcom().getEntity(Gedcom.FAM, id);
    if (fam==null)
      throw new GedcomException("Couldn't find family with ID "+id);

    // Enclosing individual is child in family ?
    Indi cindi[] = fam.getChildren();
    for (int inx=0; inx < cindi.length; inx++) {
        if (cindi[inx]==indi)
            throw new GedcomException("Family @"+id+"@ already contains Individual @"+indi.getId()+"@ as a child");
    }

    // Enclosing individual is Husband/Wife in family ?
    if ((fam.getHusband()==indi)||(fam.getWife()==indi))
      throw new GedcomException("Individual @"+indi.getId()+"@ is already spouse in family @"+id+"@");

    // Family is descendant of indi ?
    if (fam.getAncestors().contains(indi))
      throw new GedcomException("Individual @"+indi.getId()+"@ is already ancestor of family @"+fam.getId()+"@");

    // Connect back from family (maybe using invalid back reference)
    PropertyChild pc;
    for (int i=0,j=fam.getNoOfProperties();i<j;i++) {
      Property prop = fam.getProperty(i);
      if (!"CHIL".equals(prop.getTag()))
        continue;
      pc = (PropertyChild)prop;
      if ( !pc.isValid() && pc.getReferencedId().equals(indi.getId()) ) {
        pc.setTarget(this);
        setTarget(pc);
        return;
      }
    }

    // .. new back referencing property
    pc = new PropertyChild(this);
    fam.addProperty(pc);
    setTarget(pc);

    // Done
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.FAM;

  }

} //PropertyFamilyChild
