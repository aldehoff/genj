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

import gj.layout.LayoutException;
import gj.model.Graph;
import gj.model.Node;
import gj.util.ArcIterator;
import java.awt.Shape;
import java.util.HashSet;
import java.util.Set;

/**
 * A 'tree' wrapper for a Graph
 */
public class Tree {
  
  /** the height of generations */
  private double[] height;
  
  /** the position of generations (aligned) */
  private double[] latitude;
  
  /** the number of generations */
  private int numGenerations;
  
  /** the spanned nodes of the tree */
  private Set nodes;
  
  /** the root */
  private Node root;
  
  /**
   * Constructor
   */
  public Tree(Graph graph, Node root, double padGenerations, Orientation o) throws LayoutException {

    // remember
    this.root = root;
    
    // Analyze tree
    // - no cycles
    // - get generations' heights & position
    // - collect spanned nodes
    height = new double[graph.getNodes().size()];
    nodes = new HashSet(graph.getNodes().size());
    analyze(root, null, 0, o);

    // Calculate generation's positions    
    latitude = new double[numGenerations];
    double pos = 0;
    for (int i=0;i<numGenerations;i++) {
      latitude[i] = pos;
      height[i] += padGenerations;
      pos+=height[i];
    }
    
    // Done
  }

  /**
   * Analyzes one generation
   */
  private void analyze(Node node, Node parent, int generation, Orientation o) throws LayoutException {
    
    // this node shouldn't have been visited before
    if (nodes.contains(node)) {
      throw new LayoutException("Arc "+parent+"->"+node+" is part of a cycle");
    }
    nodes.add(node);
    
    // update number of generations
    numGenerations = Math.max(numGenerations, generation+1);

    // Analyze the root's height
    Shape shape = node.getShape();
    if (shape!=null) {
      Contour contour = o.getContour(shape.getBounds2D());
      height[generation] = Math.max(
        height[generation],
        contour.south-contour.north
      );
    }
   
    // Recurse into children
    ArcIterator it = new ArcIterator(node);
    while (it.next()) {
      if (!it.isFirst) continue;
      if (it.isLoop) continue;
      if (it.dest==parent) continue;
      analyze(it.dest, node, generation+1, o);
    }

    // Done 
  }
  
  /**
   * Return a height for current generation
   */
  public double getHeight(int generation) {
    return height[generation];
  }
  
  /**
   * Return a position for current generation
   */
  public double getLatitude(int generation) {
    return latitude[generation];
  }
  
  /**
   * Returns the nodes in this tree
   */
  public Set getNodes() {
    return nodes;
  }
  
  /**
   * Returns the root of this tree
   */
  public Node getRoot() {
    return root;
  }
  
} //GenerationInfo
