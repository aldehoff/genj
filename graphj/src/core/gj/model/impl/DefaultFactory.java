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
package gj.model.impl;

import java.awt.Shape;

import gj.model.Arc;
import gj.model.Factory;
import gj.model.Graph;
import gj.model.Node;

/**
 * A default implementation for
 * @see gj.model.Factory
 */
public class DefaultFactory implements Factory {

  /**
   * @see gj.model.Factory#createArc(gj.model.Graph, gj.model.Node, gj.model.Node)
   */
  public Arc createArc(Graph graph, Node from, Node to) {
    return ((DefaultGraph)graph).addArc(from, to, null);
  }

  /**
   * @see gj.model.Factory#createGraph()
   */
  public Graph createGraph() {
    return new DefaultGraph();
  }

  /**
   * @see gj.model.Factory#createNode(gj.model.Graph, java.awt.Shape, java.lang.Object)
   */
  public Node createNode(Graph graph, Shape shape, Object content) {
    return ((DefaultGraph)graph).addNode(null, shape, content);
  }

} //DefaultFactory