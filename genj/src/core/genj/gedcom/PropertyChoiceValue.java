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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Gedcom Property : simple value with choices
 */
public class PropertyChoiceValue extends PropertySimpleValue {

  /** relationships */
  private static Map tag2values = new HashMap();
  
  /**
   * @see genj.gedcom.PropertySimpleValue#setValue(java.lang.String)
   */
  public void setValue(String value) {
    // get map value2count
    Map value2count = getValueMap();
    // decrease count for old value
    String old = getValue();
    if (old.length()>0){
      Integer count = (Integer)value2count.get(old);
      if (count==null||count.intValue()<=1)
        value2count.remove(old);
      else 
        value2count.put(old, new Integer(count.intValue()-1));
    }
    // increase count for new value
    value = value.trim();
    if (value.length()>0) {
      Integer count = (Integer)value2count.get(value);
      count = new Integer((count==null?0:count.intValue())+1);
      value2count.put(value, count);
    }
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
   * Lookup map of values
   */
  private Map getValueMap() {
    // get map with value2count from values
    String tag = getTag();
    Map values = (Map)tag2values.get(tag);
    if (values==null) {
      // .. instantiate
      values = new TreeMap();
      // .. and pre-fill if necessary
      StringTokenizer tokens = new StringTokenizer(Gedcom.resources.getString(tag+".vals",""),",");
      while (tokens.hasMoreElements()) values.put(tokens.nextToken().trim(), new Integer(1));
      tag2values.put(tag, values);
    }
    // got it
    return values;
  }
  
  /**
   * Used choices
   */
  public List getChoices() {
    return new ArrayList(getValueMap().keySet());
  }
  
  /**
   * @see genj.gedcom.PropertyRelationship#getProxy()
   */
  public String getProxy() {
    return "Choice";
  }


} //PropertyChoiceValue
