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

import genj.gedcom.Property;

import javax.swing.ImageIcon;
import javax.swing.text.View;

/**
 * A search hit
 */
/*package*/ class Hit {

  /** the property */
  private Property property;
  
  /** an image (cached) */
  private ImageIcon img; 
  
  /** a view (cached) */
  private View view;
  
  /** n-th entity  */
  private int entity;

  /** 
   * Constructor
   */
  /*package*/ Hit(Property setProp, View setView, int setEntity) {
    // keep property
    property = setProp;
    // cache img
    img = property.getImage(false);
    // keep view
    view = setView;
    // keep sequence
    entity = setEntity;
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
   * View
   */
  /*package*/ View getView() {
    return view;
  }
 
  /**
   * n-th entity
   */
  /*package*/ int getEntity() {
    return entity;
  }
  
} //Hit
