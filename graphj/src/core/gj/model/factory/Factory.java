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
package gj.model.factory;

import gj.model.MutableGraph;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * A factory for a graph
 */
public interface Factory {

  /**
   * Create a graph 
   * @param graph the mutable graph to fill with data
   * @param bounds bounds to adhere to if possible
   * @param nodeShape the shape of nodes
   */
  public Rectangle2D create(MutableGraph graph, Rectangle2D bounds, Shape nodeShape);

} //Factory
