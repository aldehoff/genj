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
package gj.util;

import java.awt.geom.Point2D;

import gj.layout.Graph2D;
import gj.layout.Port;
import gj.model.Vertex;

/**
 * The default port supporting a side
 */
public class SimplePort implements Port {
  
  /**
   * the side of the port (or none)
   */
  public enum Side {
    NORTH,
    WEST,
    EAST,
    SOUTH
  }

  /** the side of the port */
  private Side side;
  
  /**
   * Construct the port
   */
  public SimplePort(Side side) {
    this.side = side;
  }

  /** 
   * the offset of the port relative to the position of vertex in graph
   */
  public Point2D getOffset(Graph2D graph, Vertex vertex) {
    switch (side) {
      case NORTH:
      case WEST:
      case EAST:
      case SOUTH:
    }
    return null;
  }

} //DefaultPort
