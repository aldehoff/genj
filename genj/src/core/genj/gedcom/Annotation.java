/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2006 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.gedcom;

import genj.util.swing.ImageIcon;

/**
 * An annotation contains information that can be shown 
 * to the user. It is normally associated with a context (property).
 */
public class Annotation implements Comparable {

  /** the context this annotation is for */
  private Property prop;
  
  /** an annotation text */
  private String txt;
  
  /** an annotation image */
  private ImageIcon img;
  
  /** constructor */
  public Annotation(String text, Property property) {
    this(text, null, property);
  }
  
  /** constructor */
  public Annotation(String text, ImageIcon image, Property property) {
    txt = text;
    prop = property;
    if (image!=null)
      img = image;
    else if (prop!=null) 
      img = prop.getImage(true);
  }

  /** comparison  */
  public int compareTo(Object o) {
    Annotation that = (Annotation)o;
    return this.txt.compareTo(that.txt);
  }

  /**
   * String representation
   */
  public String toString() {
    return getText();
  }
  
  /** 
   * returns the annotation's text 
   */
  public String getText() {
    return txt;
  }
  
  /** 
   * sets the annotation's text 
   */
  public void setText(String text) {
    txt = text;
  }
  
  /** 
   * returns the annotation's image
   */
  public ImageIcon getImage() {
    return img;
  }
  
  /** 
   * The annotation's context 
   * @return property or null if n/a
   */ 
  public Property getProperty() {
    return prop;
  }

  /**
   * Helper for converting array of objects to array of annotations
   */
  public static Annotation[] toArray(Object[] array) {
    Annotation[] result = new Annotation[array.length];
    System.arraycopy(array, 0, result, 0, array.length);
    return result;
  }
  
} //Warning
