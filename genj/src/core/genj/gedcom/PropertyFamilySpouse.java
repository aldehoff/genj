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
 * Gedcom Property : FAMS
 * The property wrapping the condition of being a spouse in a family
 */
public class PropertyFamilySpouse extends PropertyXRef {

  /** applicable target types */
  public final static String[] 
    TARGET_TYPES = { Gedcom.FAM };

  /**
   * Empty Constructor
   */
  public PropertyFamilySpouse() {
  }
  
  /**
   * Constructor with reference
   */
  public PropertyFamilySpouse(String target) {
    setValue(target);
  }

  /**
   * Constructor with reference
   */
  public PropertyFamilySpouse(PropertyXRef target) {
    super(target);
  }

  /**
   * Returns a warning string that describes what happens when this
   * property would be deleted
   * @return warning as <code>String</code>, <code>null</code> when no warning
   */
  public String getDeleteVeto() {
    return resources.getString("prop.fams.veto");
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
    return "FAMS";
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

    // Look for family (not-existing -> Gedcom throws Exception)
    String id = getReferencedId();
    Fam fam = (Fam)getGedcom().getEntity(Gedcom.FAM, id);
    if (fam==null)
      throw new GedcomException("Couldn't find family with ID "+id);

    // Enclosing individual is Husband/Wife in family ?
    Indi husband = fam.getHusband();
    Indi wife    = fam.getWife();

    if ((husband!=null)&&(wife!=null))
      throw new GedcomException("Family @"+fam.getId()+"@ already has husband and wife");

    if ((husband==indi)||(wife==indi))
      throw new GedcomException("Individual @"+indi.getId()+"@ is already spouse in family @"+id+"@");

    if (indi.getFamc()==fam)
      throw new GedcomException("Individual @"+indi.getId()+"@ is already child in family @"+id+"@");
      
    // Enclosing individual is descendant of family
    if (fam.getDescendants().contains(indi)) 
      throw new GedcomException("Individual @"+indi.getId()+"@ is already descendant of family @"+id+"@");

    // place as husband or wife according to gender
    if (indi.getSex()==PropertySex.UNKNOWN) 
      indi.setSex(husband==null ? PropertySex.MALE : PropertySex.FEMALE);

    // place as husband or wife
    PropertyXRef backref;
    
    found: switch (indi.getSex()) {
      // HUSB
      case PropertySex.MALE: default:{
        // swap if necessary
        if (husband!=null)
          fam.swapSpouses();
        // look for dead back reference
        Property[] ps = fam.getProperties(new TagPath("FAM:HUSB"),QUERY_ALL);
        for (int i=0;i<ps.length;i++) {
          PropertyHusband ph = (PropertyHusband)ps[i];
          if ( (!ph.isValid()) && (ph.getReferencedId().equals(indi.getId())) ) {
            backref = ph;
            break found;
          }
        }
        // create new back ref
        backref = new PropertyHusband();
        return;
      } 
      // WIFE
      case PropertySex.FEMALE : {
        // swap if necessary
        if (wife!=null)
          fam.swapSpouses();
        // look for dead back reference
        Property[] ps = fam.getProperties(new TagPath("FAM:WIFE"),QUERY_ALL);
        for (int i=0;i<ps.length;i++) {
          PropertyWife pw = (PropertyWife)ps[i];
          if ( (!pw.isValid()) && (pw.getReferencedId().equals(indi.getId())) ) {
            backref = pw;
            break found;
          }
        }
        // create new back ref
        backref = new PropertyWife();
        break;      
      }
    } 

    // connect
    if (backref.getParent()==null)
      fam.addProperty(backref);
    backref.setTarget(this);
    setTarget(backref);

    // Done
  }

  /**
   * The expected referenced type
   */
  public String[] getTargetTypes() {
    return TARGET_TYPES;
  }
  
} //PropertyFamilySpouse
