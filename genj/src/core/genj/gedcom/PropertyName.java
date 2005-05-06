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

import genj.util.ReferenceSet;
import genj.util.WordBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Gedcom Property : NAME
 */
public class PropertyName extends Property {
  
  public final static String TAG =  "NAME";
  
  /** the first + last name */
  private String
    lastName  = EMPTY_STRING,
    firstName = EMPTY_STRING,
    suffix    = EMPTY_STRING;

  /** the name if unparsable */
  private String nameAsString;

  /**
   * Empty Constructor
   */
  public PropertyName() {
  }
  
  /**
   * Constructor
   */
  public PropertyName(String first, String last) {
    setName(first, last);
  }
  
  /**
   * Constructor
   */
  public PropertyName(String name) {
    setValue(name);
  }
  
  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(Object o) {
  
    // cast to PropertyName if applicable
    if (!(o instanceof PropertyName)) 
      return super.compareTo(o);
    PropertyName that = (PropertyName)o;

    // check last name initially
    int result = compare(this.getLastName(), that.getLastName());
    if (result!=0)
      return result;
     
    // advance to first name
    return compare(this.getFirstName(), that.getFirstName());
  }

  /**
   * the first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Returns <b>true</b> if this property is valid
   */
  public boolean isValid() {
    /// no indi -> true
    if (!(getEntity() instanceof Indi||getEntity() instanceof Submitter)) return true;
    return nameAsString==null;
  }


  /**
   * Returns localized label for first name
   */
  static public String getLabelForFirstName() {
    return Gedcom.getResources().getString("prop.name.firstname");
  }

  /**
   * Returns localized label for last name
   */
  static public String getLabelForLastName() {
    return Gedcom.getResources().getString("prop.name.lastname");
  }

  /**
   * Returns localized label for last name
   */
  static public String getLabelForSuffix() {
    return "Suffix";
  }

  /**
   * the last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * the suffix
   */
  public String getSuffix() {
    return suffix;
  }

  /**
   * the name (e.g. "Meier, Nils")
   */
  public String getName() {
    if (nameAsString!=null) 
      return nameAsString;
    if (firstName.length()==0) 
      return lastName;
    return lastName + ", " + firstName;
  }

  /**
   * a proxy tag
   */
  public String getProxy() {
    if (nameAsString!=null)
      return super.getProxy();
    return "Name";
  }

  /**
   * the tag
   */
  public String getTag() {
    return TAG;
  }
  
  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  /*package*/ Property init(MetaProperty meta, String value) throws GedcomException {
    assume(TAG.equals(meta.getTag()), UNSUPPORTED_TAG);
    return super.init(meta,value);
  }

  /**
   * the gedcom value
   */
  public String getValue() {

    if (nameAsString != null) 
      return nameAsString;
    
    WordBuffer wb = new WordBuffer();
    wb.append(firstName);
    // 20050328 need last name //'s if there's a suffix
    if (lastName.length()>0||suffix.length()>0)
      wb.append("/"+lastName+"/");
    if (suffix.length()>0)
      wb.append(suffix);
    return wb.toString();
  }
  
  /**
   * a value for display
   */
  public String getDisplayValue() {
    
    // n/a
    if (nameAsString!=null)
      return nameAsString;
    
    WordBuffer b = new WordBuffer();
    b.append(getLastName());
    b.append(getSuffix());
    b.setFiller(", ");
    b.append(getFirstName());
    
    return b.toString();
  }

  /**
   * Sets name to a new value
   */
  public PropertyName setName(String first, String last) {
    return setName(first,last,EMPTY_STRING);
  }

  /**
   * Sets name to a new value
   */
  public PropertyName setName(String first, String last, String suff) {

    String old = getValue();
    
    // TUNING We expect that a lot of first and last names are the same
    // so we pay the upfront cost of reusing an intern cached String to
    // save overall memorey
    first = first.trim().intern();
    last = last.trim().intern();
    suff = suff.trim();

    // forget/remember
    rememberLastName(lastName, last);

    // Make sure no Information is kept in base class
    nameAsString=null;

    lastName  = last;
    firstName = first;
    suffix    = suff;

    // tell about it 
    propagateChange(old);
    
    // Done
    return this;
  }
  
  /**
   * Hook:
   * + Remember last names in reference set
   * 
   * @see genj.gedcom.PropertyName#addNotify(genj.gedcom.Property)
   */
  /*package*/ void addNotify(Property parent) {
    // continue
    super.addNotify(parent);
    // our change to remember the last name
    rememberLastName(lastName, lastName);
    // done
  }
  
  /**
   * Callback:
   * + Forget last names in reference set
   * @see genj.gedcom.Property#delNotify()
   */
  /*package*/ void delNotify(Property old) {
    // forget value
    rememberLastName(lastName, EMPTY_STRING);
    // continue
    super.delNotify(old);
    // done
  }


  /**
   * sets the name to a new gedcom value
   */
  public void setValue(String newValue) {

    // Only name specified ?
    if (newValue.indexOf('/')<0) {
      setName(newValue, EMPTY_STRING, EMPTY_STRING);
      return;
    }

    // Name AND First name
    String f = newValue.substring( 0 , newValue.indexOf('/') ).trim();
    String l = newValue.substring( newValue.indexOf('/') + 1 );

    // ... wrong format (2 x '/'s !)
    if (l.indexOf('/') == -1)  {
      setName(EMPTY_STRING,EMPTY_STRING,EMPTY_STRING);
      nameAsString=newValue;
      return;
    }

    // ... format ok
    suffix = l.substring( l.indexOf('/') + 1 );
    l = l.substring( 0 , l.indexOf('/') );

    // keep
    setName(f,l,suffix);
    
    // done
  }
  
  /**
   * Return all last names
   */
  public List getLastNames(boolean sortByName) {
    Gedcom gedcom = getGedcom();
    if (gedcom==null)
      return new ArrayList(0);
    return getLastNames(gedcom, sortByName);
  }

  /**
   * Return all last names
   */
  public static List getLastNames(Gedcom gedcom, boolean sortByName) {
    return gedcom.getReferenceSet(TAG).getKeys(sortByName ? gedcom.getCollator() : null);
  }

  /**
   * Returns all PropertyNames that contain the same name 
   */
  public int getLastNameCount() {
    Gedcom gedcom = getGedcom();
    if (gedcom==null)
      return 0;
    return getLastNameCount(gedcom, getLastName());
  }
  
  /**
   * Returns all PropertyNames that contain the same name 
   */
  public static int getLastNameCount(Gedcom gedcom, String last) {
    return gedcom.getReferenceSet(TAG).getReferences(last).size();
  }
  
  /**
   * Returns all PropertyNames that contain the same name 
   */
  public Property[] getSameLastNames() {
    return toArray(getGedcom().getReferenceSet(TAG).getReferences(getLastName()));
  }
  /**
   * Remember a last name
   */
  private void rememberLastName(String oldName, String newName) {
    // got access to a reference set?
    Gedcom gedcom = getGedcom();
    if (gedcom==null)
      return;
    ReferenceSet refSet = gedcom.getReferenceSet(TAG);
    // forget old
    if (oldName!=null&&oldName.length()>0) refSet.remove(oldName, this);
    // remember new
    if (newName!=null&&newName.length()>0) refSet.add(newName, this);
    // done
  }
} //PropertyName
