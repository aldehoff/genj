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
package genj.almanac;

import java.text.MessageFormat;

import genj.util.Resources;

/**
 * An event category
 */
public class Category {
  
  private final Resources resources = Resources.get(Category.class);
  
  private String key;
  
  private String name;
  
  private MessageFormat pattern;
  
  /**
   * Constructor
   */
  protected Category(String set) {
    // remember
    key = set;
    // init name
    name = resources.getString("category."+key, false);
    if (name==null)
      name = resources.getString("category.*");
    // init pattern
    String p = resources.getString("category."+key+".pattern", false);
    if (p==null) p = "{0}";
    pattern = Resources.getMessageFormat(p);
    // done
  }
  
  /**
   * Format a text for this category
   */
  /*package*/ String format(String text) {
    return pattern.format(new Object[]{text});
  }
  
  /**
   * Accessor
   */
  public String getKey() {
    return key;
  }
  
  /**
   * String representation
   */
  public String toString() {
    return name + " ("+key+")";
  }

} //Category
