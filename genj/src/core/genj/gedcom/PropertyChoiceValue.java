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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Gedcom Property : simple value with choices
 */
public class PropertyChoiceValue extends PropertySimpleValue {

  /** mapping tags refence sets */
  private static Map tags2refsets = new HashMap();

  /**
   * Lookup a refset
   */
  private ReferenceSet getRefSet() {
    // lookup
    String tag = getTag();
    ReferenceSet result = (ReferenceSet)tags2refsets.get(tag);
    if (result==null) {
      // .. instantiate if necessary
      result = new ReferenceSet();
      tags2refsets.put(tag, result);
      // .. and pre-fill
      StringTokenizer tokens = new StringTokenizer(Gedcom.resources.getString(tag+".vals",""),",");
      while (tokens.hasMoreElements()) result.add(tokens.nextToken().trim());
    }
    // done
    return result;
  }

  /**
   * Remember a value
   */
  private void remember(String oldValue, String newValue) {
    String tag = getTag();
    ReferenceSet refSet = getRefSet();
    // forget old
    if (oldValue.length()>0) refSet.remove(oldValue);
    // remember new
    if (newValue.length()>0) refSet.add(newValue);
    // done
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
   * @see genj.gedcom.PropertyRelationship#delNotify()
   */
  public void delNotify() {
    // forget value
    setValue("");
    // continue
    super.delNotify();
  }
  
  /**
   * Used choices
   */
  public List getChoices() {
    return new ArrayList(getRefSet());
  }
  
  /**
   * @see genj.gedcom.PropertyRelationship#getProxy()
   */
  public String getProxy() {
    return "Choice";
  }

} //PropertyChoiceValue
