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

import genj.util.swing.ImageIcon;

/**
 * Gedcom Property : CHIL
 * The property wrapping the condition of having a child in a family
 */
public class PropertyChild extends PropertyXRef {

  private final static TagPath
    PATH_FAMCHIL = new TagPath("FAM:CHIL"),
    PATH_INDIFAMC = new TagPath("INDI:FAMC"),
    PATH_INDIADOPFAMC = new TagPath("INDI:ADOP:FAMC");
    
  public final static ImageIcon
    IMG_MALE    = Grammar.getMeta(PATH_FAMCHIL).getImage("male"),
    IMG_FEMALE  = Grammar.getMeta(PATH_FAMCHIL).getImage("female"),
    IMG_UNKNOWN = Grammar.getMeta(PATH_FAMCHIL).getImage();

  /**
   * Empty Constructor
   */
  public PropertyChild() {
  }
  
  /**
   * Constructor
   */
  public PropertyChild(String target) {
    setValue(target);
  }
  
  /**
   * Constructor with reference
   * @param target referenced PropertyXRef
   */
  public PropertyChild(PropertyXRef target) {
    super(target);
  }

  /**
   * Returns the child
   * @return referenced child
   */
  public Indi getChild() {
    return (Indi)getTargetEntity();
  }

  /**
   * Returns a warning string that describes what happens when this
   * property would be deleted
   * @return warning as <code>String</code>, <code>null</code> when no warning
   */
  public String getDeleteVeto() {
    return resources.getString("prop.chil.veto");
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
    Gedcom gedcom = getGedcom();

    // Look for child (not-existing -> Gedcom throws Exception)
    Indi child = (Indi)getCandidate();

    // Child already has parents ?
//    if (child.getFamc()!=null) {
//      throw new GedcomException("Individual @"+child.getId()+"@ is already child of a family");
//    }

    // Enclosing family has indi as child, husband or wife ?
    if (fam.getWife()==child) 
      throw new GedcomException("Individual "+child+" is already wife in family @"+fam.getId()+"@");
    if (fam.getHusband()==child) 
      throw new GedcomException("Individual "+child+" is already husband in family @"+fam.getId()+"@");

    Indi children[] = fam.getChildren();
    for (int i=0;i<children.length;i++) {
      if ( children[i] == child ) 
        throw new GedcomException("Individual "+child+" is already child in family @"+fam.getId()+"@");
    }

    // Child is ancestor of husband or wife ?
    if (fam.getAncestors().contains(child)) {
      throw new GedcomException("Individual "+child+" is ancestor of family @"+fam.getId()+"@");
    }

    // Connect back from child (maybe using back reference)
    ps = child.getProperties(PATH_INDIFAMC);

    PropertyFamilyChild pfc;
    for (int i=0;i<ps.length;i++) {
      pfc = (PropertyFamilyChild)ps[i];
      // 20030616 compare against fam.getId()!!!
      if (pfc.isCandidate(fam)) {
        pfc.setTarget(this);
        setTarget(pfc);
        return;
      } else {
          ps = child.getProperties(PATH_INDIADOPFAMC);
          for (int j=0;j<ps.length;j++) {
            pfc = (PropertyFamilyChild)ps[j];
            // 20030616 compare against fam.getId()!!!
            if (pfc.isCandidate(fam)) {
              pfc.setTarget(this);
              setTarget(pfc);
              return;
            }
          }
      }
    }

    // .. new back referencing property
    pfc = new PropertyFamilyChild(this);
    child.addProperty(pfc);
    setTarget(pfc);

    // Done
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.INDI;
  }
  
  /**
   * @see genj.gedcom.PropertyXRef#getImage(boolean)
   */
  public ImageIcon getImage(boolean checkValid) {
     // check it
    Indi child = getChild();
    if (child==null) return super.getImage(checkValid);
    switch (child.getSex()) {
      case PropertySex.MALE: return overlay(IMG_MALE);
      case PropertySex.FEMALE: return overlay(IMG_FEMALE);
      default: return overlay(IMG_UNKNOWN);
    }
  }
  
} //PropertyChild
