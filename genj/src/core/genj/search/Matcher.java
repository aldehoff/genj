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

import java.util.ArrayList;
import java.util.List;

/**
 * Matching 
 */
public abstract class Matcher {
  
  /** 
   * init
   */
  public abstract void init(String pattern);
  
  /**
   * match
   */
  public final Match[] match(String value) {
    List result = new ArrayList(100);
    match(value, result);
    return (Match[])result.toArray(new Match[result.size()]);
  }

  /**
   * match (impl)
   */
  protected abstract void match(String value, List result);
  
  /**
   * formats a search value according to given matches 
   */
  public static String format(String value, Match[] matches, String open, String close, String newline) {
    // no matches?
    if (matches.length==0) return value;

    // loop over matches
    char[] chars = value.toCharArray();
    StringBuffer buffer = new StringBuffer(chars.length+matches.length*8);
    int pos = 0;
    for (int i = 0; i < matches.length; i++) {
      Matcher.Match match = matches[i];
      // PREFIX...
      if (match.pos>0)
        append(buffer, chars, pos, match.pos-pos, newline);
      // prefix-OPEN-...
      buffer.append(open);
      // prefix-open-MATCH...
      append(buffer, chars, match.pos, match.len, newline);
      // prefix-open-match-CLOSE...
      buffer.append(close);
      // next
      pos = match.pos+match.len;
    }
    // prefix-open-match-close-...-POSTFIX
    if (pos<chars.length)
      append(buffer, chars, pos, chars.length-pos, newline);

    // done
    return buffer.toString();  
  }
  
  private static void append(StringBuffer buffer, char[] chars, int pos, int len, String newline) {
    for (int i=0; i<len; i++) {
      char c = chars[pos+i];
      if (c=='\n') buffer.append(newline);
      else buffer.append(chars[pos+i]);    	
    }
  }
  
  /**
   * A match
   */
  public static class Match {
    /** section */
    public int pos, len;
    /** constructor */
    protected Match(int p, int l) { pos=p; len=l; }
  } //Match

} //Matcher