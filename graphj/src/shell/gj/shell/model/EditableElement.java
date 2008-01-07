/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2002-2004 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.shell.model;

import java.awt.Rectangle;
import java.awt.Shape;

/**
 * Base impl for elements
 */
public class EditableElement {

  /** the default shape */
  private static final Shape EMPTY_SHAPE = new Rectangle();
  
  /** the shape of this node */
  private Shape shape;

  /**
   * interface implementation
   */
  public Shape getShape() {
    return shape;
  }
  
  /**
   * interface implementation
   */
  public void setShape(Shape set) {
    shape = set!=null ? set : EMPTY_SHAPE;
  }
  
} //DefaultElement
