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

import genj.util.WordBuffer;


/**
 * Class for encapsulating a source
 * Basic strategy was to copy parts of note and media and then strip
 * out as much as I could.
 */
public class Source extends Entity {

  /**
   * Constructor for Source
   */
  /*package*/ Source() {
  }

  /**
   * Returns the type to which this entity belongs
   * INDIVIDUALS, FAMILIES, MULTIMEDIAS, NOTES, ...
   */
  public int getType() {
    return Gedcom.SOURCES;
  }

  /**
   * Returns this property as a string
   */
  public String toString() {
    // look for titl
    WordBuffer buf = new WordBuffer();
    buf.append(getProperty("TITL"));
    // author 
    buf.append("by").append(getProperty("AUTH"), "Unknown Author");
    // text
    buf.setFiller("\n").append(getText(), "text n/a");
    return super.toString() + buf.toString();
  }
  
  /**
   * @see genj.gedcom.Property#getTag()
   */
  public String getTag() {
    return "SOUR";
  }
  
  /**
   * The text
   */
  public String getText() {
    Property text = getProperty("TEXT");
    if (text instanceof PropertyMultilineValue) 
      return ((PropertyMultilineValue)text).getLinesValue();
    if (text!=null) return text.getValue();
    return "";
  }
  
} //Source
