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



/**
 * Class for encapsulating a source
 * Basic strategy was to copy parts of note and media and then strip
 * out as much as I could.
 */
public class Source extends Entity {

  /**
   * Returns this property as a string
   */
  public String toString() {
    String title = getTitle();
    return title.length()>0 ? title : super.toString();
  }
  
  /** 
   * the title
   */
  public String getTitle() {
    Property title = getProperty("TITL");
    return title!=null ? title.getValue() : EMPTY_STRING;
  }
  
  /**
   * The text
   */
  public String getText() {
    Property text = getProperty("TEXT");
    if (text instanceof PropertyMultilineValue) 
      return ((PropertyMultilineValue)text).getLinesValue();
    if (text!=null) return text.getValue();
    return EMPTY_STRING;
  }
  
} //Source
