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
 * Class for encapsulating a family with parents and children
 */
public class Fam extends PropertyFam implements Entity {

  private String id = "";
  private Gedcom gedcom;

  /**
   * Default constructor
   */
  Fam(Gedcom gedcom) {
    // Entity
    this.gedcom = gedcom;
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
    }

    return this;
  }

  /**
   * Adds a marriage information to an individual
   */
  public void addMarriage(String value, String place) {
    // BIRTH
    Property p = new PropertyEvent("MARR");
    addProperty(p);
    PropertyDate date = new PropertyDate();
    date.setValue(value);
    p.addProperty(date);
    p.addProperty(new PropertyPlace(place));
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

    // Break connection
    this.gedcom = null;
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
    if (husb==null)
      return null;
    return ((PropertyHusband)husb).getHusband();
  }

  /**
   * This family's id
   */
  public String getId() {
    return id;
  }

  /**
   * Calculate fam's marriage date
   */
  public String getMarriageAsString() {

    // Calculate DATE
    PropertyDate p = (PropertyDate)getProperty(new TagPath("FAM:MARR:DATE"),true);
    if (p==null)
      return "";

    // Return string value
    return p.toString();
  }

  /**
   * The number of children
   */
  public int getNoOfChildren() {
    Property[] chils = getProperties(new TagPath("FAM:CHIL"),true);
    return chils.length;
  }

  /**
   * Returns the other parent to the given one
   */
  public Indi getOtherSpouse(Indi spouse) {
    Indi wife = getWife();
    if (wife==spouse) {
      return getHusband();
    }
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
    if (wife==null) {
      return null;
    }
    return ((PropertyWife)wife).getWife();
  }

  /**
   * Whether there are children
   */
  public boolean hasChildren() {
    return getNoOfChildren() > 0;
  }

  /**
   * Wether a spouse is missing
   */
  public boolean hasMissingSpouse() {
    return ((getWife()==null) || (getHusband()==null));
  }

  /**
   * Checks wether this family is descendant of individual
   */
  boolean isDescendantOf(Indi indi) {

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
   * Sets the parents of the family
   */
  /*package*/ Fam setParents(Indi husband, Indi wife) throws GedcomException {

    // Check for sex
    if (   ((husband!=null)&&(husband.getSex()!=Gedcom.MALE  ))
      || ((wife   !=null)&&(wife.getSex()   !=Gedcom.FEMALE))  ) {
      Indi t=husband;husband=wife;wife=t;
    }

    // Remove old wife
    PropertyWife pw = (PropertyWife)getProperty(new TagPath("FAM:WIFE"),true);
    if (pw!=null) {
      delProperty(pw);
    }

    // Remove old husband
    PropertyHusband ph = (PropertyHusband)getProperty(new TagPath("FAM:HUSB"),false);
    if (ph!=null) {
      delProperty(ph);
    }

    // Remember wife which is spouse in this
    if (wife!=null) {
      pw = new PropertyWife("",wife.getId());
      addProperty(pw);

      try {
        pw.link();
      } catch (GedcomException ex) {
        delProperty(pw);
        throw ex;
      }
    }

    // Add (new) husband
    if (husband!=null) {
      ph = new PropertyHusband("",husband.getId());
      addProperty(ph);

      try {
        ph.link();
      } catch (GedcomException ex) {
        delProperty(ph);
        throw ex;
      }
    }

    // Done
    return this;
  }

  /**
   * Returns this entity as String description
   */
  public String toString() {

    // Fxyz:...
    String result = getId()+":";

    // ... Someone, Joe (Iabc) ...
    Indi husband = getHusband();
    if (husband!=null) {
      result+=husband.getName()+" ("+husband.getId()+")";
    }

    // ... + Another, Susan (Izyx) ...
    Indi wife    = getWife   ();
    if (wife   !=null) {
      result+=(husband==null?"":"+");
      result+=wife.getName   ()+" ("+wife   .getId()+")";
    }

    // ... \n Little, One (Iefg) ...
    Indi[] children = getChildren();
    for (int c=0;c<children.length;c++) {
      result += "\n" + children[c].toString();
    }

    // Done
    return result;
  }

  /**
   * @see Entity#addForeignXRef(PropertyForeignXRef)
   */  
  public void addForeignXRef(PropertyForeignXRef fxref) {
    throw new RuntimeException("Not supported yet");
  }
  
}
