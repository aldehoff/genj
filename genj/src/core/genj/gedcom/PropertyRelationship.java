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
import java.util.List;
import java.util.TreeMap;

/**
 * Gedcom Property : RELA
 */
public class PropertyRelationship extends Property {

  /** the relationship */
  private String relationship = EMPTY_STRING;
  
  /** relationships */
  private static TreeMap relationships = new TreeMap();
  
  static {
    remember("Witness");
    remember("Informant");
    remember("Godfather");
    remember("Godmother"); 
    remember("Other");    
  }
  
  /**
   * Remember a relationship
   */
  private static void remember(String rela) {
    // don't remember empty 
    if (rela.length()==0) return;
    // get old count
    Integer i = (Integer)relationships.get(rela);
    if (i==null) relationships.put(rela, new Integer(1));
    else relationships.put(rela, new Integer(i.intValue()+1));
    // done
  }

  /**
   * Forget a relationship
   */
  private static void forget(String rela) {
    // only if it's still being remembered
    Integer i = (Integer)relationships.get(rela);
    if (i==null) return;
    // decrease old count
    if (i.intValue()<2) relationships.remove(rela);
    else relationships.put(rela, new Integer(i.intValue()-1));
    // done
  }

  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return "RELA";
  }

  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  public void setTag(String tag) throws GedcomException {
    if (!"RELA".equals(tag)) throw new GedcomException("Unsupported Tag");
  }
  
  /**
   * Returns the value of this property
   */
  public String getValue() {
    return relationship;
  }

  /**
   * Sets the value of this property
   */
  public void setValue(String value) {
    // forget old
    forget(relationship);
    // change
    noteModifiedProperty();
    relationship=value;
    // remember new
    remember(relationship);
    // done
  }

  /**
   * @see genj.gedcom.PropertyRelationship#delNotify()
   */
  public void delNotify() {
    forget(relationship);
    super.delNotify();
  }
  
  /**
   * Used relationships
   */
  public static List getRelationships() {
    return new ArrayList(relationships.keySet());
  }
  
  /**
   * @see genj.gedcom.PropertyRelationship#getProxy()
   */
  public String getProxy() {
    return "Choice";
  }


} //PropertyRelationship
