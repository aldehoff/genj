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
package genj.util;

/**
 * A helper to access values in a string separated through separator by index
 */
public class DirectAccessTokenizer {
  
  private String string, separator;
  private int from, to;
  private int index;
  private boolean trim;
  
  /**
   * Constructor
   */
  public DirectAccessTokenizer(String string, String separator) {
    this(string, separator, false);
  }
  public DirectAccessTokenizer(String string, String separator, boolean trim) {
    this.trim = trim;
    this.string = string;
    this.separator = separator;
    from = 0;
    to = from-separator.length();
    index = 0;
  }
  
  /**
   * Count tokens
   */
  public int count() {
    int result = 1;
    int from = 0;
    while (true) {
      int i = string.indexOf(separator, from);
      if (i<0) break;
      from = i + separator.length();
      result ++;
    }
    return result;
  }
  
  /**
   * Access token by position
   * @return token at position or null if no such exists
   */
  public String get(int pos) {
    
    // legal argument?
    if (pos<0)
      return null;
    
    // backtrack?
    if (pos<index) {
      from = 0;
      to = from-separator.length();
      index = 0;
    }
    
    // loop
    while (index<=pos) {
      
      // move to next
      from = to+separator.length();
      
      // not available?
      if (from>string.length())
        return null;
      
      // look for next separator
      to = string.indexOf(separator, from);
      
      // no more? assume end of string
      if (to<0) 
        to = string.length();
      
      // we're moving the index now
      index++;
    }
    
    // done
    String result = string.substring(from, to);
    return trim ? result.trim() : result;
  }
  
  /**
   * String representation 
   */
  public String toString() {
    return string.replaceAll(separator, ", ");
  }
  
}

