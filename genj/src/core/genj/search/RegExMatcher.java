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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A matcher based on regular expressions
 */
public class RegExMatcher implements Matcher {
  
  /** the compiled regular expression */
  private Pattern compiled; 
  
  /**
   * @see genj.search.Matcher#init(java.lang.String)
   */
  public void init(String pattern) {
    try {
      compiled = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    } catch (PatternSyntaxException pe) {
      throw new IllegalArgumentException(pe.getDescription());
    }
  }
  
  /**
   * @see genj.search.Matcher#matches(java.lang.String)
   */
  public boolean matches(String input) {
    return compiled.matcher(input).find();
  }

} //RegExMatcher