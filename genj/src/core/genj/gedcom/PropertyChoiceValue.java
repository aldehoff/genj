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
   * Lookup reference set
   */
  private ReferenceSet getReferenceSet(boolean notNull) {
    // look it up
    Gedcom gedcom = getGedcom();
    if (gedcom!=null)
      return gedcom.getReferenceSet(getTag());
    // none available!
    if (notNull) throw new IllegalArgumentException("getReferenceSet() n/a with parent==null");
    return null;
  }

  /**
   * Remember a value
   */
  private void remember(String oldValue, String newValue) {
    // remember by tag
    String tag = getTag();
    // got access to a reference set?
    ReferenceSet refSet = getReferenceSet(false);
    if (refSet==null)
      return;
    // forget old
    if (oldValue.length()>0) refSet.remove(oldValue, this);
    // remember new
    if (newValue.length()>0) refSet.add(newValue, this);
    // done
  }
  
  /**
   * Returns all Properties that contain the same value
   */
  public Property[] getSameChoices() {
    return toArray(getReferenceSet(true).getReferences(getValue()));
  }
  
  /**
   * @see genj.gedcom.PropertySimpleValue#setValue(java.lang.String)
   */
  public void setValue(String value) {
    // remember
    remember(getValue(), value);
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
    remember(getValue(), getValue());
    // done
  }

  /**
   * Removing us from the reference set (our value is not used anymore)
   * @see genj.gedcom.PropertyRelationship#delNotify()
   */
  /*package*/ void delNotify() {
    // forget value
    remember(getValue(), EMPTY_STRING);
    // continue
    super.delNotify();
  }
  
  /**
   * Used choices - this won't work unless parent!=null
   */
  public List getChoices() {
    return getReferenceSet(true).getValues();
  }
  
  /**
   * @see genj.gedcom.PropertyRelationship#getProxy()
   */
  public String getProxy() {
    return "Choice";
  }

} //PropertyChoiceValue
