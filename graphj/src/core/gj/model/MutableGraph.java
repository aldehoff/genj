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

import gj.awt.geom.Path;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * A <i>mutable</i> Graph
 */
public interface MutableGraph extends Graph {

  /**
   * Creates an Arc
   */
  public Arc createArc(Node from, Node to, Path path);

  /**
   * Removes an Arc
   */  
  public void removeArc(Arc arc);

  /**
   * Creates a Node
   */
  public Node createNode(Point2D position, Shape shape, Object content);

  /**
   * Removes a Node
   */
  public void removeNode(Node node);

  /**
   * Sets a Node's shape
   */
  public void setShape(Node node, Shape shape);
  
  /**
   * Sets a Node's content
   */
  public void setContent(Node node, Object content);

  /**
   * Sets a Node's ordering of its Arcs
   * @throws IllegalArgumentException if !node.getArcs().containsAll(arcs) || !arcs.containsAll(node.getArcs())
   */
  public void setOrder(Node node, List arcs);  
 
}

