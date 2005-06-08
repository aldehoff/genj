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
  
  static int 
    NOT_BIOLOGICAL = 0,
    MAYBE_BIOLOGICAL = 1,
    CONFIRMED_BIOLOGICAL = 2;
  
  /**
   * Check if this is a biological link (not necessarily deterministic)
   */
  protected int isBiological() {
    // certainly not if contained in ADOPtion
    if ("ADOP".equals(getParent().getTag()))
      return NOT_BIOLOGICAL;
    // check for PEDI? could be if not present
    Property pedi = getProperty("PEDI");
    if (pedi==null)
      return MAYBE_BIOLOGICAL;
    // check well known keywords
    String value = pedi.getValue();
    if ("birth".equals(value)) return CONFIRMED_BIOLOGICAL;
    if ("adopted".equals(value)) return NOT_BIOLOGICAL;
    if ("foster".equals(value)) return NOT_BIOLOGICAL; 
    if ("sealing".equals(value)) return NOT_BIOLOGICAL;
    return MAYBE_BIOLOGICAL;
  }

  /**
   * @see genj.gedcom.PropertyXRef#getForeignDisplayValue()
   */
  protected String getForeignDisplayValue() {
    // can only really be called if this is an ADOPtion case
    Property adop = getParent();
    if (adop instanceof PropertyEvent&&adop.getTag().equals("ADOP"))
      return resources.getString("foreign.ADOP", getEntity().toString());
    // fallback
    return super.getForeignDisplayValue();
  }
  
  /**
   * Returns the reference to family
   */
  public Fam getFamily() {
    return (Fam)getTargetEntity();
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

    // Get enclosing individual ?
    Indi indi;
    try {
      indi = (Indi)getEntity();
    } catch (ClassCastException ex) {
      throw new GedcomException(resources.getString("error.noenclosingindi"));
    }
    
    // Look for family
    Fam fam = (Fam)getCandidate();

    // Enclosing individual is Husband/Wife in family ?
    if ((fam.getHusband()==indi)||(fam.getWife()==indi))
      throw new GedcomException(resources.getString("error.already.spouse", new String[]{ indi.toString(), fam.toString() }));

    // Family is descendant of indi ?
    if (fam.getAncestors().contains(indi))
      throw new GedcomException(resources.getString("error.already.ancestor", new String[]{ indi.toString(), fam.toString() }));

    // Connect back from family (maybe using invalid back reference) 
    for (int i=0,j=fam.getNoOfProperties();i<j;i++) {
      Property prop = fam.getProperty(i);
      if (!"CHIL".equals(prop.getTag()))
        continue;
      PropertyChild pc = (PropertyChild)prop;
      if (pc.isCandidate(indi)) {
        pc.setTarget(this);
        setTarget(pc);
        return;
      }
    }

    // .. new back referencing property
    PropertyXRef xref = new PropertyChild(this);
    fam.addProperty(xref);
    setTarget(xref);

    // Done
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.FAM;

  }

} //PropertyFamilyChild
