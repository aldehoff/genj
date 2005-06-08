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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
    IMG_MALE    = Grammar.getMeta(PATH_INDI).getImage("male"),
    IMG_FEMALE  = Grammar.getMeta(PATH_INDI).getImage("female"),
    IMG_UNKNOWN = Grammar.getMeta(PATH_INDI).getImage();
    
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
   * Calculate all siblings (biological)
   */
  public Indi[] getSiblings(boolean includeMe) {
    
    // collect siblings
    Fam fam = getFamilyWhereBiologicalChild();
    if (fam==null)
      return new Indi[0];
    List result  = new ArrayList(fam.getNoOfChildren());
    Indi[] siblings = fam.getChildren();
    for (int s=0;s<siblings.length;s++)
      if (includeMe||siblings[s]!=this) result.add(siblings[s]);
    
    // done
    return toIndiArray(result);
    
  }
  
  /**
   * Calculate the 'younger' siblings - a list ordered by position in fam
   */
  public Indi[] getYoungerSiblings() {
    
    // grab 'em all
    Indi[] siblings = getSiblings(true);
    
    // sort by date
    Arrays.sort(siblings, new PropertyComparator("INDI:BIRT:DATE"));
    
    // grab everything up to me
    List result = new ArrayList(siblings.length);
    for (int i=siblings.length-1;i>=0;i--) {
      if (siblings[i]==this)
        break;
      result.add(0, siblings[i]);
    }
    
    // done
    return toIndiArray(result);
  }
  
  /**
   * Calculate the 'older' sibling
   */
  public Indi[] getOlderSiblings() {
    
    // grab 'em all
    Indi[] siblings = getSiblings(true);
    
    // sort by date
    Arrays.sort(siblings, new PropertyComparator("INDI:BIRT:DATE"));
    
    // grab everything up older than me
    List result = new ArrayList(siblings.length);
    for (int i=0,j=siblings.length;i<j;i++) {
      if (siblings[i]==this)
        break;
      result.add(0, siblings[i]);
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
    Fam[] fs = getFamiliesWhereSpouse();
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
    Fam[] fs = getFamiliesWhereSpouse();
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
   * Calculate indi's birth date
   */
  public String getBirthAsString() {

    PropertyDate p = getBirthDate();
    if (p==null) 
      return "";

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
   * Returns the families in which this individual is a spouse
   */
  public Fam[] getFamiliesWhereSpouse() {
    ArrayList result = new ArrayList(getNoOfProperties());
    for (int i=0,j=getNoOfProperties();i<j;i++) {
      Property prop = getProperty(i);
      if ("FAMS".equals(prop.getTag())&&prop.isValid()) 
        result.add(((PropertyFamilySpouse)prop).getFamily());
    }
    return Fam.toFamArray(result);
  }

  
  /**
   * Returns the families in which the person is child (biological, foster, etc.)
   */
  public Fam[] getFamiliesWhereChild( ) {
    
    List famcs = getProperties(PropertyFamilyChild.class);
    List result = new ArrayList(famcs.size());
    for (int i=0; i<famcs.size(); i++) {
      PropertyFamilyChild famc = (PropertyFamilyChild)famcs.get(i);
      if (famc.isValid())
        result.add(famc.getTargetEntity());
    }
    
    return Fam.toFamArray(result);
  }

  /**
   * Returns the family in which the person is biological child
   * @return reference to 1st family or family with 'PEDI birth'
   */
  public Fam getFamilyWhereBiologicalChild( ) {

    // look at all FAMCs
    Fam result = null;
    List famcs = getProperties(PropertyFamilyChild.class);
    for (int i=0; i<famcs.size(); i++) {
      PropertyFamilyChild famc = (PropertyFamilyChild)famcs.get(i);
      // not valid - not interesting
      if (!famc.isValid()) continue;
      int biological = famc.isBiological();
      // stop if confirmed (first) biological
      if (biological==PropertyFamilyChild.CONFIRMED_BIOLOGICAL) 
        return (Fam)famc.getTargetEntity();
      // keep if maybe biological and first
      if (biological==PropertyFamilyChild.MAYBE_BIOLOGICAL&&result==null)
        result = (Fam)famc.getTargetEntity();
    }
    
    // done
    return result;
  }
  
  /**
   * Returns indi's first name
   */
  public String getFirstName() {
    PropertyName p = (PropertyName)getProperty(PropertyName.TAG,true);
    return p!=null ? p.getFirstName() : "";  
  }

  /**
   * Calculate indi's last name
   */
  public String getLastName() {
    PropertyName p = (PropertyName)getProperty(PropertyName.TAG,true);
    return p!=null ? p.getLastName() : ""; 
  }

  /**
   * Calculate indi's name suffix
   */
  public String getNameSuffix() {
    PropertyName p = (PropertyName)getProperty(PropertyName.TAG,true);
    return p!=null ? p.getSuffix() : ""; 
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
    return p!=null ? p.getName() : ""; 
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
    // check whatever sex is there
    PropertySex p = (PropertySex)getProperty("SEX",false);
    // don't change what's wrong
    if (p!=null&&!p.isValid())
      return;
    // add it if necessary
    if (p==null) 
      p = (PropertySex)addProperty(new PropertySex());
    // change
    p.setSex(sex);
  }

  /**
   * Return all ancestors of this individual
   */
  public Set getAncestors() {
    return getAncestors(new HashSet());
  }
  
  /*package*/ Set getAncestors(Set collect) {
    
    // would be in parental family
    Fam fam = getFamilyWhereBiologicalChild();
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
    Fam[] fams = getFamiliesWhereSpouse();
    for (int f=0; f<fams.length; f++) {
      fams[f].getDescendants(collect);      
    }
    
    // done
    return collect;
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
      return "";
    
    Delta delta = Delta.get(pbirth.getStart(), pit);
    if (delta==null)
      return "";
      
    return delta.toString();
  }
  
  /**
   * Tag always INDI
   */
  public String getTag() {
    return Gedcom.INDI;
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

  /** 
   * Calculate indi's father
   */
  public Indi getBiologicalFather() {
    Fam f = getFamilyWhereBiologicalChild();
    return f!=null ? f.getHusband() : null;
  }

  /** 
   * Calculate indi's mother
   */
  public Indi getBiologicalMother() {
    Fam f = getFamilyWhereBiologicalChild();
    return f!=null ? f.getWife() : null;
  }  
  
} //Indi