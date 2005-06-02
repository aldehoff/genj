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

import java.util.List;

/**
 * Gedcom Property : simple value with choices
 */
public class PropertyChoiceValue extends PropertySimpleValue {

  /**
   * Remember a value
   */
  protected void remember(String oldValue, String newValue) {
    // got access to a reference set?
    Gedcom gedcom = getGedcom();
    if (gedcom==null)
      return;
    ReferenceSet refSet = gedcom.getReferenceSet(getTag());
    // forget old
    if (oldValue.length()>0) refSet.remove(oldValue, this);
    // remember new
    if (newValue.length()>0) refSet.add(newValue, this);
    // done
  }
  
  /**
   * Returns all choices in same gedcom file as this
   */
  public String[] getChoices(boolean sort) {
    // got access to a reference set?
    Gedcom gedcom = getGedcom();
    if (gedcom==null)
      return new String[0];
    return getChoices(gedcom, getTag(), sort);
  }
  
  /**
   * Returns all choices for given property tag
   */
  public static String[] getChoices(final Gedcom gedcom, final String tag, boolean sort) {
    
    // lookup choices
    List choices = gedcom.getReferenceSet(tag).getKeys(sort ? gedcom.getCollator() : null);

    // done
    return (String[])choices.toArray(new String[choices.size()]);
    
  }
  
  /**
   * Returns all Properties that contain the same value
   */
  public Property[] getSameChoices() {
    // got access to a reference set?
    Gedcom gedcom = getGedcom();
    if (gedcom==null)
      return new Property[0];
    ReferenceSet refSet = gedcom.getReferenceSet(getTag());
    // convert
    return toArray(refSet.getReferences(super.getValue()));
  }
  
  /**
   * @see genj.gedcom.PropertySimpleValue#setValue(java.lang.String)
   */
  public void setValue(String value) {
    setValueInternal(trim(value));
  }
  
  /**
   * A special value that allows global substitution
   */
  public void setValue(String value, boolean global) {
    
    value = trim(value);
    
    // more?
    if (global) {
      // change value of all with value
      Property[] others = getSameChoices();
      for (int i=0;i<others.length;i++) {
        Property other = others[i];
        if (other instanceof PropertyChoiceValue&&other!=this) 
          ((PropertyChoiceValue)other).setValueInternal(value);
      }
    }    
      
    // change me
    setValueInternal(value);
    
    // done
  }

  protected String trim(String value) {
    // TUNING: for choices we expect a lot of repeating values so
    // we build the intern representation of value here - this makes
    // us share string instances for an upfront cost
    return value.intern();
  }
  
  private void setValueInternal(String value) {
    // remember
    remember(super.getValue(), value);
    // delegate
    super.setValue(value);
  }

  /**
   * @see genj.gedcom.Property#addNotify(genj.gedcom.Property)
   */
  /*package*/ void addNotify(Property parent) {
    // delegate
    super.addNotify(parent);
    // a remember wouldn't have worked until now
    remember("", super.getValue());
    // done
  }

  /**
   * Removing us from the reference set (our value is not used anymore)
   * @see genj.gedcom.PropertyRelationship#delNotify()
   */
  /*package*/ void delNotify(Property old) {
    // forget value
    remember(super.getValue(), "");
    // continue
    super.delNotify(old);
  }
  
  /**
   * Used choices (this will not work unless parent not null)
   * 20041210 I'm passing gedcom here so properties that haven't
   * been added to a parent yet (EditView) can already be edited
   * nicely
   */
  public List getChoices(Gedcom gedcom) {
    return gedcom.getReferenceSet(getTag()).getKeys(gedcom.getCollator());
  }
  
  /**
   * Our proxy of choice
   */
  public String getProxy() {
    return "Choice";
  }

} //PropertyChoiceValue
