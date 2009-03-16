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
package gj.layout;

import gj.model.Vertex;

import java.awt.geom.Point2D;

/**
 * A port for an edge connecting to/from a Vertex
 */
public interface Port {
  
  public final static Port NONE = new Port() {
    public Point2D getOffset(Graph2D graph, Vertex vertex) {
      throw new IllegalArgumentException("Port w/o offset");
    }
  };
  
  public Point2D getOffset(Graph2D graph, Vertex vertex);

} //Port
