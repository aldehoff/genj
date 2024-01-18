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

import genj.util.WordBuffer;

/**
 * Class for encapsulating a family with parents and children
 */
public class Fam extends PropertyFam implements Entity {

  private String id = "";
  private Gedcom gedcom;
  private PropertySet foreignXRefs = new PropertySet();

  /**
   * Default constructor
   */
  /*package*/ Fam() {
  }

  /**
   * Adds another child to the family
   */
  Fam addChild(Indi newChild) throws GedcomException {

    // Remember Indi who is child
    PropertyChild pc = new PropertyChild("",newChild.getId());
    addProperty(pc);

    // Link !
    try {
      pc.link();
    } catch (GedcomException ex) {
      delProperty(pc);
      throw ex;
    }

    return this;
  }

  /**
   * Notification to entity that it has been added to a Gedcom
   */
  public void addNotify(Gedcom gedcom) {
    this.gedcom = gedcom;
  }

  /**
   * Notification to entity that it has been deleted from a Gedcom
   */
  public void delNotify() {

    // Notify to properties
    super.delNotify();

    // Remove all foreign XRefs
    foreignXRefs.deleteAll();

    // Break connection
    this.gedcom = null;
  }

  /**
   * Removes a property
   * This overrides the default behaviour by first
   * looking in this entity's foreign list
   */
  public boolean delProperty(Property which) {

    if (foreignXRefs.contains(which)) {
      foreignXRefs.delete(which);
      return true;
    }
    return super.delProperty(which);
  }

  /**
   * Returns child #i
   */
  public Indi getChild(int which) {
    Property[] chils = getProperties(new TagPath("FAM:CHIL"),true);
    if (which > chils.length) {
      throw new IllegalArgumentException("Family doesn't have "+which+" children");
    }
    return ((PropertyChild)chils[which]).getChild();
  }

  /**
   * Returns children
   */
  public Indi[] getChildren() {
    Property chils[] = getProperties(new TagPath("FAM:CHIL"),true);
    Indi result[] = new Indi[chils.length];
    for (int i=0;i<result.length;i++) {
      result[i] = ((PropertyChild)chils[i]).getChild();
    }
    return result;
  }

  /**
   * Gedcom this entity's in
   * @return containing Gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }

  /**
   * Returns the husband of the family
   */
  public Indi getHusband() {
    Property husb = getProperty(new TagPath("FAM:HUSB"),true);
    if (husb==null) return null;
    return ((PropertyHusband)husb).getHusband();
  }

  /**
   * This family's id
   */
  public String getId() {
    return id;
  }

  /**
   * The number of children
   */
  public int getNoOfChildren() {
    Property[] chils = getProperties(new TagPath("FAM:CHIL"),true);
    return chils.length;
  }
  
  /**
   * The number of spouses
   */
  public int getNoOfSpouses() {
    int result = 0;
    if (getHusband()!=null) result++;
    if (getWife   ()!=null) result++;
    return result;
  } 

  /**
   * Returns the other parent to the given one
   */
  public Indi getOtherSpouse(Indi spouse) {
    Indi wife = getWife();
    if (wife==spouse) return getHusband();
    return wife;
  }

  /**
   * This family's root property
   */
  public Property getProperty() {
    return this;
  }

  /**
   * Returns the type to which this entity belongs
   * INDIVIDUALS, FAMILIES, MULTIMEDIAS, NOTES, ...
   */
  public int getType() {
    return Gedcom.FAMILIES;
  }

  /**
   * Returns the wife of the family
   */
  public Indi getWife() {
    Property wife = getProperty(new TagPath("FAM:WIFE"),true);
    if (wife==null) return null;
    return ((PropertyWife)wife).getWife();
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
   * Checks wether this family is descendant of individual
   */
  /*package*/ boolean isDescendantOf(Indi indi) {

    // Prepare VARs
    Indi husband,wife;

    // Husband ?
    husband = getHusband();
    if ((husband!=null)&&(husband.isDescendantOf(indi))) {
      return true;
    }

    // Wife ?
    wife = getWife();
    if ((wife!=null)&&(wife.isDescendantOf(indi))) {
      return true;
    }

    // Not descendant
    return false;
  }

  /**
   * Sets the husband of this family
   */
  /*package*/ void setHusband(Indi husband) throws GedcomException {
    
    // Remove old husband
    PropertyHusband ph = (PropertyHusband)getProperty(new TagPath("FAM:HUSB"),false);
    if (ph!=null) delProperty(ph);
    
    // Add new husband
    ph = new PropertyHusband("",husband.getId());
    addProperty(ph);

    // Link !
    try {
      ph.link();
    } catch (GedcomException ex) {
      delProperty(ph);
      throw ex;
    }

    // done    
  }

  /**
   * Sets the family of the family
   */
  /*package*/ void setWife(Indi wife) throws GedcomException {

    // Remove old wife
    PropertyWife pw = (PropertyWife)getProperty(new TagPath("FAM:WIFE"),true);
    if (pw!=null) delProperty(pw);

    // Add new wife
    pw = new PropertyWife("",wife.getId());
    addProperty(pw);

    // Link !
    try {
      pw.link();
    } catch (GedcomException ex) {
      delProperty(pw);
      throw ex;
    }

    // Done
  }

  /**
   * Sets one of the spouses
   */
  /*package*/ void setSpouse(Indi spouse) throws GedcomException {  
    Indi husband = getHusband();
    Indi wife = getWife();
    if (husband==null&&wife!=null) {
      setHusband(spouse);
      return;
    }
    if (husband!=null&wife==null) {
      setWife(spouse);
      return;
    }
    if (spouse.getSex()==PropertySex.FEMALE) setWife(spouse);
    else setHusband(spouse);
  }
  
  /**
   * Returns this entity as String description
   */
  public String toString() {
    
    WordBuffer wb = new WordBuffer();

    // Fxyz:...
    wb.append(getId());
    wb.append(":");

    // ... Someone, Joe (Iabc) ...
    Indi husband = getHusband();
    if (husband!=null) wb.append(husband);
    
    // ... + Another, Susan (Izyx) ...
    Indi wife = getWife();
    if (wife!=null) wb.append(wife);

    // ... \n Little, One (Iefg) ...
    Indi[] children = getChildren();
    for (int c=0;c<children.length;c++) {
      wb.append(children[c].toString());
    }

    // Done
    return wb.toString();
  }
  
  /**
   * Adds a PropertyForeignXRef to this entity
   */
  public void addForeignXRef(PropertyForeignXRef fxref) {
    foreignXRefs.add(fxref);
  }

} //Fam
