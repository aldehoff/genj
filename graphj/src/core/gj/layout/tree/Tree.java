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

import java.util.HashSet;
import java.util.Set;

/**
 * A 'tree' wrapper for a Graph
 */
public class Tree {
  
  /** the spanned nodes of the tree */
  private Set nodes;
  
  /** the root */
  private Node root;

  /**
   * Constructor
   */
  public Tree(Graph graph, Node root, NodeOptions nopt, Orientation o) throws LayoutException {
    this(root, nopt, o, graph.getNodes().size());
  }
  
  /**
   * Constructor
   */
  public Tree(Node root, NodeOptions nopt, Orientation o, int estimatedSize) throws LayoutException {

    // remember
    this.root = root;
    
    // Analyze tree
    // - no cycles
    nodes = new HashSet(estimatedSize);
    analyze(root, null, 0, nopt, o);

    // Done
  }

  /**
   * Analyzes one generation
   */
  private void analyze(Node node, Node parent, int generation, NodeOptions nopt, Orientation o) throws LayoutException {
    
    // this node shouldn't have been visited before
    if (nodes.contains(node)) {
      throw new LayoutException("Arc "+parent+"->"+node+" is part of a cycle");
    }
    nodes.add(node);
    
    // Recurse into children
    ArcIterator it = new ArcIterator(node);
    while (it.next()) {
      if (!it.isFirst) continue;
      if (it.isLoop) continue;
      if (it.dest==parent) continue;
      analyze(it.dest, node, generation+1, nopt, o);
    }

    // Done 
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
  
} //Tree
