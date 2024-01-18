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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Class for encapsulating a family with parents and children
 */
public class Fam extends Entity {
  
  public final static TagPath
    PATH_FAMMARRDATE = new TagPath("FAM:MARR:DATE"),
    PATH_FAMMARRPLAC = new TagPath("FAM:MARR:PLAC"),
    PATH_FAMDIVDATE  = new TagPath("FAM:DIV:DATE"),
    PATH_FAMDIVPLAC  = new TagPath("FAM:MARR:PLAC");
  
  private final static Comparator SORT_SIBLINGS =  new PropertyComparator("INDI:BIRT:DATE");

  /**
   * Returns child #i
   */
  public Indi getChild(int which) {

    for (int i=0,j=getNoOfProperties();i<j;i++) {
      Property prop = getProperty(i);
      if ("CHIL".equals(prop.getTag())&&prop.isValid()) {
        if (which==0)
          return ((PropertyChild)prop).getChild();
        which--;
      }
    }
    
    throw new IllegalArgumentException("no such child");
  }

  /**
   * Returns children
   */
  public Indi[] getChildren() {
    
    ArrayList children = new ArrayList(getNoOfProperties());
    
    List childs = getProperties(PropertyChild.class);
    for (int i=0,j=childs.size();i<j;i++) {
      PropertyChild prop = (PropertyChild)childs.get(i);
      if (prop.isValid()) 
        children.add(prop.getChild());
    }
    
    // convert to array & sort
    Indi[] result = Indi.toIndiArray(children);
    Arrays.sort(result, SORT_SIBLINGS);
    
    // done
    return result;
  }

  /**
   * Returns the husband of the family
   */
  public Indi getHusband() {
    Property husb = getProperty("HUSB", true);
    if (husb instanceof PropertyHusband)
      return ((PropertyHusband)husb).getHusband();
    return null;    
  }

  /**
   * The number of children
   */
  public int getNoOfChildren() {
    int result = 0;
    for (int i=0,j=getNoOfProperties();i<j;i++) {
      Property prop = getProperty(i);
      if (prop.getClass()==PropertyChild.class&&prop.isValid())
        result++;
    }
    return result;
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
   * Spouse by index
   */
  public Indi getSpouse(int which) {
    Indi husband = getHusband();
    if (husband!=null) {
      if (which==0)
        return husband;
      which--;
    }
    Indi wife = getWife();
    if (wife!=null) {
      if (which==0)
        return wife;
      which--;
    }
    throw new IllegalArgumentException("No such spouse");
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
   * Returns the wife of the family
   */
  public Indi getWife() {
    
    Property wife = getProperty("WIFE", true);
    if (wife instanceof PropertyWife) 
      return ((PropertyWife)wife).getWife();
    return null;
  }

  /**
   * Sets the husband of this family
   */
  public Indi setHusband(Indi husband) throws GedcomException {
    
    // Remove old husband (first valid one would be the one)
    for (int i=0,j=getNoOfProperties();i<j;i++) {
      Property prop = getProperty(i);
      if ("HUSB".equals(prop.getTag())&&prop.isValid()) {
        delProperty(prop);
        break;
      }
    }
    
    // done?
    if (husband==null)
      return null;
    
    // Add new husband
    PropertyHusband ph = new PropertyHusband(husband.getId());
    addProperty(ph);

    // Link !
    try {
      ph.link();
    } catch (GedcomException ex) {
      delProperty(ph);
      throw ex;
    }
    
    // check sex of husband
    if (husband.getSex()!=PropertySex.MALE)
      husband.setSex(PropertySex.MALE);

    // done    
    return husband;
  }

  /**
   * Sets the wife of the family
   */
  public Indi setWife(Indi wife) throws GedcomException {

    // Remove old wife (first valid one would be the one)
    for (int i=0,j=getNoOfProperties();i<j;i++) {
      Property prop = getProperty(i);
      if ("WIFE".equals(prop.getTag())&&prop.isValid()) {
        delProperty(prop);
        break;
      }
    }
    
    // done?
    if (wife==null)
      return null;
    
    // Add new wife
    PropertyWife pw = new PropertyWife(wife.getId());
    addProperty(pw);

    // Link !
    try {
      pw.link();
    } catch (GedcomException ex) {
      delProperty(pw);
      throw ex;
    }

    // check sex of wife
    if (wife.getSex()!=PropertySex.FEMALE)
      wife.setSex(PropertySex.FEMALE);

    // Done
    return wife;
  }

  /**
   * Sets one of the spouses
   */
  public void setSpouse(Indi spouse) throws GedcomException {  
    
    Indi husband = getHusband();
    Indi wife = getWife();
    
    // won't do if husband and wife already known
    if (husband!=null&&wife!=null)
      throw new GedcomException(resources.getString("error.already.spouses", this));

    // check gender of spouse 
    switch (spouse.getSex()) {
      default:
      case PropertySex.UNKNOWN:
        // remember new spouse
        if (husband!=null) setWife(spouse);
        else setHusband(spouse);
        // done
        break;
      case PropertySex.MALE:
        // remember new husband
        setHusband(spouse);
        // keep old husband as wife if necessary
        if (husband!=null)
          wife = setWife(husband);
        // done
        break;
      case PropertySex.FEMALE:
        // remember new wife
        setWife(spouse);
        // keep old wife as husband if necessary
        if (wife!=null)
          husband = setHusband(wife);
        // done
        break;
    }
    
    // done
  }
  
  /**
   * Adds another child to the family
   */
  public Fam addChild(Indi newChild) throws GedcomException {

    // Remember Indi who is child
    PropertyChild pc = new PropertyChild(newChild.getId());
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
   * list of famas to array
   */
  /*package*/ static Fam[] toFamArray(Collection c) {
    return (Fam[])c.toArray(new Fam[c.size()]);    
  }

  /**
   * Returns this entity as String description
   */
  public String toString() {
    
    StringBuffer result = new StringBuffer();

    Indi husband = getHusband();
    if (husband!=null) {
      result.append(husband.toString());
      result.append(Options.getInstance().getTxtMarriageSymbol());
    }
    
    Indi wife = getWife();
    if (wife!=null) {
      result.append(wife.toString());
    }

    // Done
    return super.toString(result);
  }
  
  /**
   * Calculate fam's Marriage date
   * @return date or null
   */
  public PropertyDate getMarriageDate() {
      return getMarriageDate(false);
    // Calculate MARR|DATE
  }

  /**
   * returns a PropertyDate view on the marriage date of this family
   * @param create if true, and the property doesn't already exist,  initialize the Property
   * @return a PropertyDate or null.  If create is true, this method will not return null
   */
  public PropertyDate getMarriageDate(boolean create) {
      PropertyDate date = (PropertyDate)getProperty(PATH_FAMMARRDATE);
      if( null != date || !create )
          return date;
      setValue(PATH_FAMMARRDATE,"");
      return (PropertyDate)getProperty(PATH_FAMMARRDATE);
  }

/**
   * Calculate fam's divorce date
   * @return date or null
   */
  public PropertyDate getDivorceDate() {
    // Calculate DIV|DATE
    return (PropertyDate)getProperty(PATH_FAMDIVDATE);
  }

  /**
   * Swap spouses
   */
  public void swapSpouses() throws GedcomException {
    
    Indi 
      husband = getHusband(),
      wife = getWife();

    setWife(null);
    setHusband(null);
      
    if (wife!=null)
      setHusband(wife);
    if (husband!=null)
      setWife(husband);
      
  }
  
} //Fam
