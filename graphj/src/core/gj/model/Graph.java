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

import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * A Graph contains Nodes (one optional Root) and Arcs and has a 2d size
 */
public interface Graph {

  /**
   * Access to the graph's nodes - immutable
   */
  public Collection getNodes();
 
  /**
   * Access to the graph's arcs - immutable
   */
  public Collection getArcs();
  
  /**
   * Access to the graph's bounds - mutable
   */
  public Rectangle2D getBounds();
  
}
 