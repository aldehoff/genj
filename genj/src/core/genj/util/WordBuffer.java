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
 * A buffer that accepts words - meaning spaces will be automatially
 * inserted if appropriate
 */
public class WordBuffer {
  
  /** a buffer we collect words in */
  private StringBuffer buffer;
  
  /** 
   * Constructor
   */
  public WordBuffer(String content) {
    buffer = new StringBuffer(content.length()*2);
    buffer.append(content);
  }
  
  /** 
   * Constructor
   */
  public WordBuffer() {
    buffer = new StringBuffer(80);
  }
  
  /** 
   * String representation of the content
   */
  public String toString() {
    return buffer.toString();
  }

  /**
   * Append a generic object (null->"")
   */
  public WordBuffer append(Object object) {
    if (object!=null) append(object.toString());
    return this;
  }

  /**
   * Append a word
   */  
  public WordBuffer append(String word) {
    if ((word!=null)&&(word.length()!=0)) {
      if ((buffer.length()>0)&&(!isStartingWithPunctuation(word))) buffer.append(' ');
      buffer.append(word.trim());
    }
    return this;
  }
  
  /**
   * Checks whether a word starts with a punctuation
   */
  private final boolean isStartingWithPunctuation(String word) {
    switch (word.charAt(0)) {
      default: return false;
      case '.': return true;
      case ',': return true;
      case ':': return true;
    }
  }

}
