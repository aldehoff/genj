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

import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Delta;

/**
 * A filtering scheme applying privacy to Gedcom properties
 */
public class PrivacyPolicy {
  
  public static PrivacyPolicy 
    PUBLIC = new PrivacyPolicy() { public boolean isPrivate(Property prop) { return false; } },
    PRIVATE = new PrivacyPolicy() { public boolean isPrivate(Property prop) { return true; } };
  
  public final static String 
    MASK_DATE = Gedcom.resources.getString("private.mask.date"),
    MASK_PLACE = Gedcom.resources.getString("private.mask.place"),
    MASK_VALUE = Gedcom.resources.getString("private.mask.value");
  
  private boolean infoOfDeceasedIsPublic;
  private int yearsInfoIsPrivate;
  private String tagMarkingPrivate;
  
  /**
   * private constructor
   */
  private PrivacyPolicy() {
  }
  
  /** 
   * constructor
   * @param infoOfDeceasedIsPublic information pertaining to deceased persons is public
   * @param yearsInfoIsPrivate information pertaining to events in the last n years is private
   * @param tagMarkingPrivate information marked with property of this tag is private
  */
  public PrivacyPolicy(boolean infoOfDeceasedIsPublic, int yearsInfoIsPrivate, String tagMarkingPrivate) {
    this.infoOfDeceasedIsPublic = infoOfDeceasedIsPublic;
    this.yearsInfoIsPrivate = Math.max(yearsInfoIsPrivate, 0);
    this.tagMarkingPrivate = tagMarkingPrivate==null||tagMarkingPrivate.length()==0 ? null : tagMarkingPrivate;
  }

  /** filter a value */
  public String getDisplayValue(Property prop) {
    return isPrivate(prop) ? MASK_VALUE : prop.getDisplayValue();
  }
  
  /** check for privacy */
  public boolean isPrivate(Property prop) {
    
    // not if property belongs to deceased
    if (infoOfDeceasedIsPublic&&isInfoOfDeceased(prop))
      return false;
    
    // maybe prop is tagged?
    if (tagMarkingPrivate!=null&&hasTagMarkingPrivate(prop))
      return true;
    
    // maybe because it's recent?
    if (yearsInfoIsPrivate>0&&isWithinPrivateYears(prop))
      return true;
    
    // maybe parent is private?
    prop =  prop.getParent();
    return prop!=null ? isPrivate(prop) : false;
  }
  
  /** check whether a property belongs to deceased individuals only */
  private boolean isInfoOfDeceased(Property prop) {
    // contained in indi? check death-date
    Entity e = prop.getEntity();
    if (e instanceof Indi) {
      Property deathdate = e.getProperty(new TagPath("INDI:DEAT:DATE"));
      if (deathdate!=null&&deathdate.isValid())
        return true;
    } 
    // contained in Fam? check husband and wife
    if (e instanceof Fam) {
      Property deathdate = e.getProperty(new TagPath("FAM:HUSB:*:INDI:DEAT:DATE"));
      if (deathdate==null||!deathdate.isValid())
        return false;
      deathdate = e.getProperty(new TagPath("FAM:WIFE:*:INDI:DEAT:DATE"));
      if (deathdate==null||!deathdate.isValid())
        return false;
      // yes both wife and husband are deceased
      return true;
    }
    
    // dunno
    return false;
  }
  
  /** check for marked with tag */
  private boolean hasTagMarkingPrivate(Property prop) {
    return getPropertyFor(prop, tagMarkingPrivate, Property.class)!=null;
  }
  
  /** whether a prop is still within the privat years' - only if it has a date sub-property */
  private boolean isWithinPrivateYears(Property prop) {
    // check date
    PropertyDate date = (PropertyDate)getPropertyFor(prop, "DATE", PropertyDate.class);
    if (date==null)
      return false;
    // check anniversary of property's date
    Delta anniversary = date.getAnniversary();
    return anniversary!=null&&anniversary.getYears()<yearsInfoIsPrivate;
  }
    
  /** find a sub-property by tag and type */
  private Property getPropertyFor(Property prop, String tag, Class type) {
    // check children
    for (int i=0, j=prop.getNoOfProperties(); i<j; i++) {
      Property child = prop.getProperty(i);
      if (is(child,tag,type))
        return child;
    }
    return null;
  }
  
  private boolean is(Property prop, String tag, Class type) {
    return prop.getTag().equals(tag) && type.isAssignableFrom(prop.getClass()); 
  }
  
} //PrivacyFilter
