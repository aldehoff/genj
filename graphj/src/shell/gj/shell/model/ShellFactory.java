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
package gj.shell.model;

import gj.model.Arc;
import gj.model.Factory;
import gj.model.Graph;
import gj.model.Node;

import java.awt.Shape;

/**
 * Factory for Shell
 */
public class ShellFactory implements Factory {

  private Shape defaultShape;

  /**
   * Constructor
   */
  public ShellFactory(Shape shape) {
    defaultShape = shape;
  }

  /**
   * @see gj.model.Factory#createGraph()
   */
  public Graph createGraph() {
    return new ShellGraph(); 
  }

  /**
   * @see gj.model.Factory#createNode(gj.model.Graph, java.awt.Shape, java.lang.Object)
   */
  public Node createNode(Graph graph, Shape shape, Object content) {
    return ((ShellGraph)graph).createNode(shape!=null?shape:defaultShape, content);
  }

  /**
   * @see gj.model.Factory#createArc(gj.model.Graph, gj.model.Node, gj.model.Node)
   */
  public Arc createArc(Graph graph, Node from, Node to) {
    
    if (!(graph instanceof ShellGraph))
      throw new IllegalArgumentException("unknown graph");
      
    ShellGraph sgraph = (ShellGraph)graph;
    
    if (!sgraph.getNodes().contains(from))
      throw new IllegalArgumentException("unknown from");
    
    if (!sgraph.getNodes().contains(to))
      throw new IllegalArgumentException("unknown to");
      
    return sgraph.createArc((ShellNode)from, (ShellNode)to);
  }

} //ShellFactory