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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Class for encapsulating a person
 */
public class Indi extends PropertyIndi implements Entity {

  private String id = "";
  private Gedcom gedcom;

  /**
   * Constructor for Individual
   */
  protected Indi() {
  }

  /**
   * Adds a family in which the individual is a partner
   */
  /*package*/ Fam addFam() throws GedcomException {
    return addFam((Fam)getGedcom().createEntity(Gedcom.FAMILIES, null));
  }

  /**
   * Adds a family in which the individual is a partner
   */
  /*package*/ Fam addFam(Fam fam) throws GedcomException {

    // Remember Fam where this is spouse in
    PropertyFamilySpouse pfs = new PropertyFamilySpouse("",fam.getId());
    addProperty(pfs);

    // Link !
    try {
      pfs.link();
    } catch (GedcomException ex) {
      delProperty(pfs);
    }

    return fam;
  }

  /**
   * Notification to entity that it has been added to a Gedcom
   */
  public void addNotify(Gedcom gedcom) {
    this.gedcom = gedcom;
  }

  /**
   * Deletes a family in which the person was a partner
   */
  /*package*/ Indi delFam(int which ) {
    Property[] fams = getProperties(new TagPath("INDI:FAMS"),true);
    if (which > fams.length)
      throw new IllegalArgumentException("Individual isn't spouse in "+which+" families");
    delProperty(fams[which-1]);
    return this;
  }

  /**
   * Deletes the family in which the Individual was child
   */
  /*package*/ Indi delFamc() {
    Property prop = getProperty(new TagPath("INDI:FAMC"),true);
    if (prop==null) {
      return this;
    }
    delProperty(prop);
    return this;
  }

  /**
   * Calculate indi's birth date
   */
  public PropertyDate getBirthDate() {

    // Calculate BIRT|DATE
    PropertyDate p = (PropertyDate)getProperty(new TagPath("INDI:BIRT:DATE"),true);
    if (p==null) {
      return null;
    }

    // Return string value
    return p;
  }

  /**
   * Calculate indi's death date
   */
  public PropertyDate getDeathDate() {

    // Calculate DEAT|DATE
    PropertyDate p = (PropertyDate)getProperty(new TagPath("INDI:DEAT:DATE"),true);
    if (p==null) {
      return null;
    }

    // Return string value
    return p;
  }
  
  /**
   * Calculate the 'younger' sibling
   */
  public Indi getOlderSibling() {
    
    // this is a child in a family?
    Fam f = getFamc();
    if (f==null) return null;
    
    // what are the children of that one
    Indi[] cs = f.getChildren();
    for (int c=0;c<cs.length;c++) {
      if (cs[c]==this) return (c>0) ? cs[c-1] : null;
    }
    
    // there's no previous one
    return null;
  }
  
  /**
   * Calculate the 'older' sibling
   */
  public Indi getYoungerSibling() {
    
    // this is a child in a family?
    Fam f = getFamc();
    if (f==null) return null;
    
    // what are the children of that one
    Indi[] cs = f.getChildren();
    for (int c=cs.length-1;c>=0;c--) {
      if (cs[c]==this) return (c<cs.length-1) ? cs[c+1] : null;
    }
    
    // there's no previous one
    return null;
  }
  
  /** 
   * Calculate indi's partners. The number of partners can be
   * smaller than the number of families this individual is
   * part of because spouses in families don't have to be defined.
   */
  public Indi[] getPartners() {
    // Look at all families and remember spouses
    Fam[] fs = getFamilies();
    List l = new ArrayList(fs.length);
    for (int f=0; f<fs.length; f++) {
      Indi p = fs[f].getOtherSpouse(this);
      if (p!=null) l.add(p);
    }
    // Return result
    Indi[] result = new Indi[l.size()];
    l.toArray(result);
    return result;
  }
  
  /**
   * Calculate indi's children
   */
  public Indi[] getChildren() {
    // Look at all families and remember children
    Fam[] fs = getFamilies();
    List l = new ArrayList(fs.length);
    for (int f=0; f<fs.length; f++) {
      Indi[]cs = fs[f].getChildren();
      for (int c=0;c<cs.length;c++) l.add(cs[c]);
    }
    // Return result
    Indi[] result = new Indi[l.size()];
    l.toArray(result);
    return result;
  }
  
  /** 
   * Calculate indi's father
   */
  public Indi getFather() {
    // have we been child in family?
    Fam f = getFamc();
    if (f==null) return null;
    // ask fam
    return f.getHusband();
  }

  /** 
   * Calculate indi's mother
   */
  public Indi getMother() {
    // have we been child in family?
    Fam f = getFamc();
    if (f==null) return null;
    // ask fam
    return f.getWife();
  }

  /**
   * Calculate indi's birth date
   */
  public String getBirthAsString() {

    PropertyDate p = getBirthDate();
    if (p==null) {
      return "";
    }

    // Return string value
    return p.toString();
  }

  /**
   * Calculate indi's death date
   */
  public String getDeathAsString() {

    PropertyDate p = getDeathDate();
    if (p==null) {
      return "";
    }

    // Return string value
    return p.toString();
  }

  /**
   * Calculate indi's age at given date
   * (some code borrowed from jLifelines)
   * @param pEnd the date at which to calc age (or null for *now*)
   * @return age as a string description or null
   */
  public String getAge(PropertyDate pEnd) {

    // try to get birth    
    PropertyDate pBirth = getBirthDate();
    if (pBirth==null)
      return null;

    // calculate    
    boolean showMonth, showDay;
    PropertyDate.PointInTime pit;

    pit = pBirth.getStart();
    Calendar birthCal = pit.getCalendar();
    showMonth = (pit.getMonth() != null);
    showDay   = (pit.getDay()   != null);

    Calendar end = Calendar.getInstance(); // default to current time
    if (pEnd!=null) {
      pit = pEnd.getStart();
      end = pit.getCalendar();
      showMonth |= (pit.getMonth() != null);
      showDay   |= (pit.getDay()   != null);
    }

    int ageYears  = end.get(Calendar.YEAR)  - birthCal.get(Calendar.YEAR);
    int ageMonths = end.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH);
    int ageDays   = end.get(Calendar.DATE)  - birthCal.get(Calendar.DATE);

    if ((ageMonths < 0) || ((ageMonths == 0) && (ageDays < 0))) {
      ageYears  -= 1;
      ageMonths += 12;
    }
    if (ageDays < 0) {
      ageMonths -= 1;
      // a lot of work just to get the number of days in the previous month
      Calendar tmp = Calendar.getInstance();
      tmp.set(birthCal.get(Calendar.YEAR), birthCal.get(Calendar.MONTH), 1);
      tmp.add(Calendar.YEAR,  ageYears);
      tmp.add(Calendar.MONTH, ageMonths);
      // worst case: born 30 Jan, died 1 Mar - 1m 1d
      int prevMonth = tmp.getActualMaximum(Calendar.DATE);
      if (birthCal.get(Calendar.DATE) > prevMonth)
        ageDays = end.get(Calendar.DATE);
      else
        ageDays += prevMonth;
    }

    if (ageYears < 0)
      return null;                        // bogus info

    StringBuffer buf = new StringBuffer(ageYears + "y");
    if (showMonth) {
      buf.append(" ").append(ageMonths).append("m");
      if (showDay) {
        buf.append(" ").append(ageDays).append("d");
      }
    }

    return buf.toString();
  }

  /**
   * Returns the selected family in which the individual is a partner
   */
  public Fam getFam(int which) {
    Property[] props = getProperties(new TagPath("INDI:FAMS"),true);
    if (which>=props.length) {
      return null;
    }
    return ((PropertyFamilySpouse)props[which]).getFamily();
  }
  
  /**
   * Get Family with option to create
   */
  /*package*/ Fam getFam(boolean create) throws GedcomException {
    Fam fam = getFam(0);
    if (fam!=null||!create) return fam;
    fam = (Fam)getGedcom().createEntity(Gedcom.FAMILIES, null);
    if (getSex()==PropertySex.FEMALE) fam.setWife(this);
    else fam.setHusband(this);
    return fam;    
  }

  /**
   * Returns the family in which the person is child
   */
  public Fam getFamc( ) {
    Property prop = getProperty(new TagPath("INDI:FAMC"),true);
    if (prop==null) {
      return null;
    }
    return ((PropertyFamilyChild)prop).getFamily();
  }

  /**
   * Get Family with option to create
   */
  public Fam getFamc(boolean create) throws GedcomException {
    Fam fam = getFamc();
    if (fam!=null||!create) return fam;
    fam = (Fam)getGedcom().createEntity(Gedcom.FAMILIES, null);
    fam.addChild(this);
    return fam;    
  }

  /**
   * Returns indi's first name
   */
  public String getFirstName() {

    // Calculate NAME
    PropertyName p = (PropertyName)getProperty(new TagPath("INDI:NAME"),true);
    if (p==null) {
      return "";
    }

    // Return string value
    return p.getFirstName();
  }

  /**
   * Gedcom this entity's in
   * @return containing Gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }

  /**
   * This individual's id
   */
  public String getId() {
    return id;
  }

  /**
   * Calculate indi's last name
   */
  public String getLastName() {

    // Calculate NAME
    PropertyName p = (PropertyName)getProperty(new TagPath("INDI:NAME"),true);
    if (p==null) {
      return "";
    }

    // Return string value
    return p.getLastName();
  }

  /**
   * Returns indi's name
   */
  public String getName() {

    // Calculate NAME
    Property name = getProperty("NAME",true);
    if (name instanceof PropertyName)
      return ((PropertyName)name).getName();  
    return "";
  }
  
  /** 
   * Returns the number of parents this individual has
   */
  public int getNoOfParents() {
    Fam fam = getFamc();
    return fam==null?0:fam.getNoOfSpouses();
  }

  /**
   * Returns the number of families in which the individual is a partner
   */
  public int getNoOfFams() {
    Property[] props = getProperties(new TagPath("INDI:FAMS"),true);
    return props.length;
  }
  
  /**
   * Returns the families in which this individual is a partner
   */
  public Fam[] getFamilies() {
    Property[] props = getProperties(new TagPath("INDI:FAMS"),true);
    Fam[] result = new Fam[props.length];
    for (int f=0; f<result.length; f++) {
      result[f] = ((PropertyFamilySpouse)props[f]).getFamily();
    }    
    return result;
  }

  /**
   * This individual's root property (self)
   */
  public Property getProperty() {
    return this;
  }

  /**
   * Returns indi's sex
   */
  public int getSex() {

    // Calculate SEX
    PropertySex p = (PropertySex)getProperty(new TagPath("INDI:SEX"),true);
    if (p==null) {
      return 0;
    }

    // Return value
    return p.getSex();
  }

  /**
   * Returns the type to which this entity belongs
   * INDIVIDUALS, FAMILIES, MULTIMEDIAS, NOTES, ...
   */
  public int getType() {
    return Gedcom.INDIVIDUALS;
  }

  /**
   * Checks wether this individual is descendant of individual
   */
  /*package*/ boolean isDescendantOf(Indi indi) {

    // Me ?
    if (this==indi) {
      return true;
    }

    // Childhood ?
    Fam fam = getFamc();
    if (fam==null) {
      return false;
    }

    // Recursive call
    return fam.isDescendantOf(indi);
  }


  /**
   * Checks wether this individual is descendant of family 
   */
  /*package*/ boolean isDescendantOf(Fam fam) {
    // fam's children
    Indi[] children = fam.getChildren();
    for (int c=0; c<children.length; c++) {
      if (isDescendantOf(children[c])) return true;
    }
    return false;
  }

  /**
   * Sets the family in which the person is child
   */
  /*package*/ Indi setFamc(Fam fam) throws GedcomException {

    // Remove old
    Property p = getProperty(new TagPath("INDI:FAMC"),true);
    if (p!=null) {
      delProperty(p);
    }

    // Remember new Fam where this is child in
    PropertyFamilyChild pfc = new PropertyFamilyChild("",fam.getId());
    addProperty(fam);

    // Link !
    try {
      pfc.link();
    } catch (GedcomException ex) {
      delProperty(pfc);
    }

    return this;
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

    // Done
  }

  /**
   * Returns this entity as String description
   */
  public String toString() {

    String result = getId()+":"+getName();

    return result;
  }

} //Indi
