/**
 * GraphJ
 * 
 * Copyright (C) 2002 Nils Meier
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package gj.model;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * A Node has a position, shape, content, outgoing&incoming arcs
 */
public interface Node {

  /** 
   * Its position - mutable
   */
  public Point2D getPosition();
  
  /** 
   * Its shape (at the position) - immutable or mutable
   */
  public Shape getShape();
  
  /** 
   * Its content - immutable
   */
  public Object getContent();

  /** 
   * Its arcs (ordered) - immutable
   */
  public List getArcs();
  
}
