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

import genj.gedcom.Entity;
import genj.gedcom.MultiLineSupport;
import genj.gedcom.Property;
import genj.util.WordBuffer;

import javax.swing.ImageIcon;

/**
 * A search hit
 */
/*package*/ class Hit {
  
  /** formatting */
  private final static String
   OPEN = "<font color=red>",
   CLOSE = "</font>",
   NEWLINE = "<br>";
  
  /** the property */
  private Property property;
  
  /** an image (cached) */
  private ImageIcon img; 

  /** the value (cached) */
  private String value;
  
  /** an attribute (cached) */
  private Object attrib;
  
  /** the matches (cached) */
  private Matcher.Match[] matches;
  
  /**
   * test for hit
   */
  /*package*/ static Hit test(Matcher matcher, Property prop) {
    // test value
    String value = prop instanceof MultiLineSupport ? ((MultiLineSupport)prop).getLinesValue() : prop.getValue();
    Matcher.Match[] matches = matcher.match(value);
    // something?
    return  matches.length==0 ? null : new Hit(prop, value, matches);
  }
  
  /** 
   * Constructor
   */
  private Hit(Property prop, String val, Matcher.Match[] mats) {
    // keep property & value
    property = prop;
    value = val;
    matches = mats;
    // done
  }
  
  /**
   * Property
   */
  /*package*/ Property getProperty() {
    return property;
  }
  
  /**
   * Get a cached attribute
   */
  /*package*/ Object getAttribute() {
    return attrib;
  }
  
  /**
   * Set a cached attribute
   */
  /*package*/ void setAttribute(Object attr) {
    attrib = attr;
  }
  
  /**
   * Image (lazy once)
   */
  /*package*/ ImageIcon getImage() {
    if (img==null)
      img = property.getImage(false);
    return img;
  }
  
  /**
   * HTML representation
   */
  /*package*/ String getHTML() {
        
    // calc html
    WordBuffer html = new WordBuffer();
    html.append("<html>");
    html.append("<b>");
    html.append(property.getTag());
    html.append("</b>");
    if (property instanceof Entity) {
      html.append('@'+((Entity)property).getId()+'@');
    }
    html.append(Matcher.format(value, matches, OPEN, CLOSE, NEWLINE));
    html.append("</html>");
    
    // done
    return html.toString();
  }
  
} //Hit
