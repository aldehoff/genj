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
package genj.search;

import java.util.List;


/**
 * A matcher based on regular expressions
 */
public class SimpleMatcher extends Matcher {
  
  /** the pattern we're looking for */
  private String pattern; 
  
  /**
   * @see genj.search.Matcher#init(java.lang.String)
   */
  public void init(String pattern) {
    this.pattern = pattern.toLowerCase();
  }
  
  /**
   * @see genj.search.Matcher#match(java.lang.String, java.util.List)
   */
  protected void match(String input, List result) {
    // search for matches
    int end, start = 0;  
    while (true) {
      start = input.toLowerCase().indexOf(pattern, start);
      if (start<0) break;
      end = start + pattern.length();
      result.add(new Match(start, end-start));
      start = end;
    }
  }

} //RegExMatcher