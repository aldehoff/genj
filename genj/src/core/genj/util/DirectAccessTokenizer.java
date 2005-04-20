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
  
  /**
   * Constructor
   */
  public DirectAccessTokenizer(String string, String separator) {
    this.string = string;
    this.separator = separator;
    from = 0;
    index = 0;
  }
  
  /**
   * Access token by index
   */
  public String get(int pos) {
    
    // passed already?
    if (pos<index) {
      from = 0;
      index = 0;
    }

    // loop
    String result = null;
    while (index<=pos) {

      // any possibility to get at it?
      if (from<0)
        throw new IllegalArgumentException("no value "+pos);
      
      // look for next tab
      to = string.indexOf(separator, from);
      result =  string.substring(from, to);
      from = to+1;
      
      // we're moving the index now
      index++;
    }
    
    // done
    return result;
  }
  
  /**
   * String representation 
   */
  public String toString() {
    return string.replaceAll(separator, ", ");
  }
  
}

