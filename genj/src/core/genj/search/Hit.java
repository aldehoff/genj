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
  
  /** html (cached) */
  private String html;

  /** an arbitray (cached) attribute */
  private Object attr;
  
  /**
   * test for hit
   */
  /*package*/ static Hit test(Matcher matcher, Property prop) {
    // test value
    String value = prop instanceof MultiLineSupport ? ((MultiLineSupport)prop).getLinesValue() : prop.getValue();
    Matcher.Match[] matches = matcher.match(value);
    // something?
    if (matches.length==0)
      return null;
    // calc html
    StringBuffer html = new StringBuffer(value.length()+matches.length*10);
    html.append("<html>");
    html.append("<b>");
    html.append(prop.getTag());
    html.append("</b>");
    if (prop instanceof Entity) {
      html.append(" @");
      html.append(((Entity)prop).getId());
      html.append('@');
    }
    html.append(' ');
    html.append(Matcher.format(value, matches, OPEN, CLOSE, NEWLINE));
    html.append("</html>");
    
    // instantiate & done
    return new Hit(prop, html.toString());
  }
  
  /** 
   * Constructor
   */
  private Hit(Property prop, String htm) {
    // keep property
    property = prop;
    // cache img
    img = property.getImage(false);
    // cache html
    html = htm;
    // done
  }
  
  /**
   * Property
   */
  /*package*/ Property getProperty() {
    return property;
  }
  
  /**
   * Image
   */
  /*package*/ ImageIcon getImage() {
    return img;
  }
  
  /**
   * HTML
   */
  /*package*/ String getHTML() {
    return html;
  }
  
  /**
   * Arbitrary attribute
   */
  /*package*/ Object getAttribute() {
    return attr;
  }
  
  /**
   * Arbitrary attribute
   */
  /*package*/ void setAttribute(Object attrib) {
    attr = attrib;
  }
  
} //Hit
