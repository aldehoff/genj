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
package gj.layout.tree;

import gj.model.Arc;
import gj.model.Node;

import java.awt.geom.Point2D;

/**
 * Options for arcs */
public interface ArcOptions {
  
  /**
   * Get the port for given arc and node   * @param arc the arc   * @param node the node the arc binds to   * @param o current orientation
   * @return Point2D the port   */
  public Point2D getPort(Arc arc, Node node, Orientation o);

} // ArcOptions
