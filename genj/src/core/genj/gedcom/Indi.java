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

import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.util.swing.ImageIcon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class for encapsulating a person
 */
public class Indi extends Entity {
  
  private final static TagPath
    PATH_INDI = new TagPath("INDI"),
    PATH_INDIFAMS = new TagPath("INDI:FAMS"),
    PATH_INDIFAMC = new TagPath("INDI:FAMC"),
    PATH_INDIBIRTDATE = new TagPath("INDI:BIRT:DATE"),
    PATH_INDIDEATDATE = new TagPath("INDI:DEAT:DATE");

  public final static ImageIcon
    IMG_MALE    = MetaProperty.get(PATH_INDI).getImage("male"),
    IMG_FEMALE  = MetaProperty.get(PATH_INDI).getImage("female"),
    IMG_UNKNOWN = MetaProperty.get(PATH_INDI).getImage();
    
  /**
   * Adds a family in which the individual is a partner
   */
  /*package*/ Fam addFam() throws GedcomException {
    return addFam((Fam)getGedcom().createEntity(Gedcom.FAM));
  }

  /**
   * Adds a family in which the individual is a partner
   */
  /*package*/ Fam addFam(Fam fam) throws GedcomException {

    // Remember Fam where this is spouse in
    PropertyFamilySpouse pfs = new PropertyFamilySpouse(fam.getId());
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
   * Calculate indi's birth date
   */
  public PropertyDate getBirthDate() {
    // Calculate BIRT|DATE
    return (PropertyDate)getProperty(PATH_INDIBIRTDATE);
  }

  /**
   * Calculate indi's death date
   */
  public PropertyDate getDeathDate() {
    // Calculate DEAT|DATE
    return (PropertyDate)getProperty(PATH_INDIDEATDATE);
  }
  
  /**
   * Calculate the 'younger' siblings
   */
  public Indi[] getOlderSiblings() {
    
    // this is a child in a family?
    Fam f[] = getCFamilies();
    if (f==null) 
      return new Indi[0];
    
    // what are the children of that one
    LinkedList result = new LinkedList();
    for (int inx=0; inx < f.length; inx++) {
        Indi[] cs = f[inx].getChildren();
    for (int c=0;c<cs.length;c++) {
      if (cs[c]==this) break;
      result.addFirst(cs[c]);
    }
    }
    
    // done
    return toIndiArray(result);
  }
  
  /**
   * Calculate the 'older' sibling
   */
  public Indi[] getYoungerSiblings() {
    
    // this is a child in a family?
    Fam f[] = getCFamilies();
    if (f==null) 
      return new Indi[0];
    
    // what are the children of that one
    LinkedList result = new LinkedList();
    
    for (int inx=0; inx < f.length; inx++) {
        Indi[] cs = f[inx].getChildren();
    for (int c=cs.length-1;c>=0;c--) {
      if (cs[c]==this) break;
      result.addFirst(cs[c]);
    }
    }
    
    // done
    return toIndiArray(result);
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
      for (int c=0;c<cs.length;c++)
          if (!l.contains(cs[c])) l.add(cs[c]);
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

  public Indi[] getFathers() {
      LinkedList result = new LinkedList();
    
      Fam f[] = getCFamilies();
      for (int inx=0; inx < f.length; inx++) {
          Indi cs = f[inx].getHusband();
          if (cs==this) break;
          if (!result.contains(cs))
            result.addFirst(cs);
      }
    
      // done
      return toIndiArray(result);
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

  public Indi[] getMothers() {
      LinkedList result = new LinkedList();

      Fam f[] = getCFamilies();
      for (int inx=0; inx < f.length; inx++) {
          Indi cs = f[inx].getWife();
          if (cs==this) break;
          if (!result.contains(cs))
            result.addFirst(cs);
      }
    
      // done
      return toIndiArray(result);
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
   * Returns the selected family in which the individual is a partner
   */
  public Fam getFam(int which) {
    
    for (int i=0,j=getNoOfProperties();i<j;i++) {
      Property prop = getProperty(i);
      if ("FAMS".equals(prop.getTag())&&prop.isValid()) {
        if (which==0)
          return ((PropertyFamilySpouse)prop).getFamily();
        which--;
      }
    }
    
    throw new IllegalArgumentException("no such family");
  }
  
  /**
   * Returns the family in which the person is child
   */
  public Fam getFamc( ) {
    Property prop = getProperty("FAMC",true);
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
    fam = (Fam)getGedcom().createEntity(Gedcom.FAM);
    fam.addChild(this);
    return fam;    
  }

  /**
   * Returns the families in which this individual is a child
   */
  public Fam[] getCFamilies() {

    ArrayList result = new ArrayList(getNoOfProperties());
    
    for (int i=0,j=getNoOfProperties();i<j;i++) {
      Property prop = getProperty(i);
      if ("FAMC".equals(prop.getTag())&&prop.isValid()) 
        result.add(((PropertyFamilyChild)prop).getFamily());
    }

    return Fam.toFamArray(result);
  }

  /**
   * Returns indi's first name
   */
  public String getFirstName() {
    PropertyName p = (PropertyName)getProperty(PropertyName.TAG,true);
    return p!=null ? p.getFirstName() : EMPTY_STRING;  
  }

  /**
   * Calculate indi's last name
   */
  public String getLastName() {
    PropertyName p = (PropertyName)getProperty(PropertyName.TAG,true);
    return p!=null ? p.getLastName() : EMPTY_STRING; 
  }

  /**
   * Calculate indi's name suffix
   */
  public String getNameSuffix() {
    PropertyName p = (PropertyName)getProperty(PropertyName.TAG,true);
    return p!=null ? p.getSuffix() : EMPTY_STRING; 
  }
  
  /**
   * Sets indi's name
   */
  public void setName(String first, String last) {
    PropertyName p = (PropertyName)getProperty(PropertyName.TAG,true);
    if (p==null) p = (PropertyName)addProperty(new PropertyName()); 
    p.setName(first, last);
  }
  
  /**
   * Returns indi's name (e.g. "Meier, Nils")
   */
  public String getName() {
    PropertyName p = (PropertyName)getProperty(PropertyName.TAG,true);
    return p!=null ? p.getName() : EMPTY_STRING; 
  }
  
  /** 
   * Returns the number of parents this individual has
   */
  // TODO: K. Mraz - make this understand individual can be member of multiple familes
  public int getNoOfParents() {
    Fam fam = getFamc();
    return fam==null?0:fam.getNoOfSpouses();
  }

  /**
   * Returns the number of families in which the individual is a partner
   */
  public int getNoOfFams() {
    int result = 0;
    for (int i=0,j=getNoOfProperties();i<j;i++) {
      Property prop = getProperty(i);
      if ("FAMS".equals(prop.getTag())&&prop.isValid())
        result++;
    }
    return result;
  }
  
  /**
   * Returns the families in which this individual is a partner
   */
  public Fam[] getFamilies() {
    
    ArrayList result = new ArrayList(getNoOfProperties());
    
    for (int i=0,j=getNoOfProperties();i<j;i++) {
      Property prop = getProperty(i);
      if ("FAMS".equals(prop.getTag())&&prop.isValid()) 
        result.add(((PropertyFamilySpouse)prop).getFamily());
    }

    return Fam.toFamArray(result);
  }

  /**
   * Returns indi's sex
   */
  public int getSex() {
    PropertySex p = (PropertySex)getProperty("SEX",true);
    return p!=null ? p.getSex() : PropertySex.UNKNOWN;
  }
  
  /**
   * Set indi's sex
   * @param sex one of PropertySex.MALE or PropertySex.FEMALE
   */
  public void setSex(int sex) {
    PropertySex p = (PropertySex)getProperty("SEX",true);
    if (p==null) p = (PropertySex)addProperty(new PropertySex());
    p.setSex(sex);
  }

  /**
   * Return all ancestors of this individual
   */
  public Set getAncestors() {
    return getAncestors(new HashSet());
  }
  
  // TODO: K. Mraz - make this understand individual can be member of multiple familes
  /*package*/ Set getAncestors(Set collect) {
    
    // would be in parental family
    Fam fam = getFamc();
    if (fam!=null)
      fam.getAncestors(collect);

    // done      
    return collect;
  }

  /**
   * Returns all descendants for this individual
   */
  public Set getDescendants() {
    return getDescendants(new HashSet());
  }
  
  /*package*/ Set getDescendants(Set collect) {
    
    // fams?
    Fam[] fams = getFamilies();
    for (int f=0; f<fams.length; f++) {
      fams[f].getDescendants(collect);      
    }
    
    // done
    return collect;
  }

  /**
   * Sets the family in which the person is child
   */
  /*package*/ Indi setFamc(Fam fam) throws GedcomException {

    // Remove old
    Property p = getProperty("FAMC",true);
    if (p!=null) {
      delProperty(p);
    }

    // Remember new Fam where this is child in
    PropertyFamilyChild pfc = new PropertyFamilyChild(fam.getId());
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
   * Returns this entity as String description
   */
  public String toString() {
    return super.toString(getName());
  }
  
  /**
   * list of indis to array
   */
  /*package*/ static Indi[] toIndiArray(Collection c) {
    return (Indi[])c.toArray(new Indi[c.size()]);    
  }

  /**
   * Image
   */
  public ImageIcon getImage(boolean checkValid) {
    // check sex (no need to check valid here)
    switch (getSex()) {
      case PropertySex.MALE: return IMG_MALE;
      case PropertySex.FEMALE: return IMG_FEMALE;
      default: return IMG_UNKNOWN;
    }
  }

  /**
   * Calculate indi's age at given point in time
   */
  public String getAgeString(PointInTime pit) {
  
    // try to get birth    
    PropertyDate pbirth = getBirthDate();
    if (pbirth==null) 
      return EMPTY_STRING;
    
    Delta delta = Delta.get(pbirth.getStart(), pit);
    if (delta==null)
      return EMPTY_STRING;
      
    return delta.toString();
  }
  
  /**
   * Calculate indi's age at given point in time or null if an error occured
   */
  public Delta getAge(PointInTime pit) {
  
    // try to get birth    
    PropertyDate pbirth = getBirthDate();
    if (pbirth==null) 
      return null;
    
    Delta delta = Delta.get(pbirth.getStart(), pit);
    if (delta==null)
      return null;
      
    return delta;
  }  
  
} //Indi