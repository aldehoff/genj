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

import gj.model.Graph;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * What a layout is all about - Implementors provide layout
 * functionality in <i>applyTo(Graph graph)</i>. 
 */
public interface LayoutAlgorithm {

  /** 
   * Applies the layout to a given graph
   * @param graph the graph to layout
   * @param bounds bounds to adhere to if possible (not guaranteed)
   * @param debugShapes TODO
   * @return resulting bounds 
   */
  public Shape apply(Graph graph, Layout2D layout, Rectangle2D bounds, Collection<Shape> debugShapes) throws LayoutAlgorithmException;
  
} //LayoutAlgorithm
