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

import gj.layout.AbstractLayout;
import gj.layout.Layout;
import gj.layout.LayoutException;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Layout for trees
 */
public class TreeLayout extends AbstractLayout implements Layout {

  /** padding between generations */
  private double latPadding = 20;

  /** padding between siblings */
  private double lonPadding = 20;

  /** the alignment of parents */
  private double lonAlignment = 0.5;

  /** the alignment of generations (if isAlignGeneration) */
  private double latAlignment = 0.5;
  
  /** whether we ignore unreached nodes */
  private boolean isIgnoreUnreachables = false;
  
  /** whether latAlignment is enabled */
  /*package*/ boolean isLatAlignmentEnabled = false;
  
  /** current node options */
  private NodeOptions nodeOptions = null;
  
  /** current arc options */
  private ArcOptions arcOptions = new DefaultArcOptions();

  /** whether children should be balanced or simply stacked */
  /*package*/ boolean isBalanceChildren = true;

  /** whether arcs are direct or bended */
  private boolean isBendArcs = true;

  /** whether we're vertical or not (=horizontal) */
  private boolean isVertical = true;

  /** whether we're doing it top/down or bottom/up */
  private boolean isTopDown = true;

  /** the root of the tree */
  /*package*/ Node declaredRoot = null;

  /** the Graph we've been applied to */
  /*package*/ Graph appliedTo = null;

  /** debugging information */
  /*package*/ List debugShapes = new ArrayList();

  /** whether we're a complement */
  private boolean isComplement = false;

  /**
   * Getter - padding between generations
   */
  public double getLatPadding() {
    return latPadding;
  }

  /**
   * Setter - padding between generations
   */
  public void setLatPadding(double set) {
    latPadding = set;
  }

  /**
   * Getter - padding between siblings
   */
  public double getLonPadding() {
    return lonPadding;
  }

  /**
   * Setter - padding between siblings
   */
  public void setLonPadding(double set) {
    lonPadding=set;
  }

  /**
   * Getter - the alignment of parents
   */
  public double getLonAlignment() {
    return lonAlignment;
  }

  /**
   * Setter - the alignment of parents
   */
  public void setLonAlignment(double set) {
    lonAlignment = set;
  }

  /**
   * Getter - the alignment of generations (if isAlignGenerations)
   */
  public double getLatAlignment() {
    return latAlignment;
  }

  /**
   * Setter - the alignment of generations (if isAlignGenerations)
   */
  public void setLatAlignment(double set) {
    latAlignment = set;
  }

  /**
   * Getter - whether generations should be aligned
   */
  public boolean isLatAlignmentEnabled() {
    return isLatAlignmentEnabled;
  }
  
  /**
   * Setter - whether generations should be aligned
   */
  public void setLatAlignmentEnabled(boolean set) {
    isLatAlignmentEnabled=set;
  }

  /**
   * Getter - whether we ignore unreached nodes
   */
  public boolean isIgnoreUnreachables() {
    return isIgnoreUnreachables;
  }

  /**
   * setter - whether we ignore unreached nodes
   */
  public void setIgnoreUnreachables(boolean set) {
    isIgnoreUnreachables = set;
  }

  /**
   * Getter - whether children are balanced optimally 
   * (spacing them apart where necessary) instead of
   * simply stacking them. Example
   * <pre>
   *      A                A
   *    +-+---+         +--+--+
   *    B C   D   -->   B  C  D
   *  +-+-+ +-+-+     +-+-+ +-+-+
   *  E F G H I J     E F G H I J
   * </pre>
   */
  public boolean getBalanceChildren() {
    return isBalanceChildren;
  }

  /**
   * Setter 
   * @see TreeLayout#getBalancedChildren()
   */
  public void setBalanceChildren(boolean set) {
    isBalanceChildren=set;
  }

  /**
   * Getter - whether arcs are direct or bended
   */
  public boolean isBendArcs() {
    return isBendArcs;
  }

  /**
   * Setter - whether arcs are direct or bended
   */
  public void setBendArcs(boolean set) {
    isBendArcs=set;
  }

  /**
   * Getter - whether we're vertical or not
   */
  public boolean isVertical() {
    return isVertical;
  }

  /**
   * Setter - whether we're vertical or not
   */
  public void setVertical(boolean set) {
    isVertical=set;
  }

  /**
   * Getter - whether we're doing it top/down (or bottom/up)
   */
  public boolean isTopDown() {
    return isTopDown;
  }

  /**
   * Setter - whether we're doing it top/down (or bottom/up)
   */
  public void setTopDown(boolean set) {
    isTopDown = set;
  }

  /**
   * Set root of tree. If a graph does not have a
   * preset root or it is not connected then random
   * secondary roots are choosen automatically.
   */
  public void setRoot(Node node) {
    declaredRoot = node;
  }

  /**
   * Returns the root of tree. If a graph doesn not
   * have a preset root a random node is returned
   * (or null if graph doesn't contain nodes).
   */
  public Node getRoot() {
    return declaredRoot;
  }

  /**
   * Accesspr - node options
   */
  public void setNodeOptions(NodeOptions no) {
    nodeOptions = no;
  }
  
  /**
   * Accesspr - node options
   */
  public NodeOptions getNodeOptions() {
    return nodeOptions!=null ? nodeOptions : new DefaultNodeOptions();
  }
  
  /**
   * Sets custom arc options to use during layout
   */
  public void setArcOptions(ArcOptions ao) {
    arcOptions = ao;
  }
  
  /**
   * Layout starting from a root node
   */
  public Rectangle2D layout(Node root, int estimatedSize) throws LayoutException {

    // get an orientation
    Orientation orientn = Orientation.get(isVertical,isTopDown);
    NodeOptions nopt = getNodeOptions();
        
    // and a node layout
    Algorithm algorithm = new Algorithm(
      orientn, 
      nopt, 
      arcOptions,
      isLatAlignmentEnabled,
      isBalanceChildren,
      isBendArcs
    );

    // create a Tree for current root assuming that all nodes in it will be visited
    Tree tree = new Tree(root, nopt, orientn, estimatedSize);

    // layout and return bounds
    return algorithm.layout(tree, null);
  }
  
  /**
   * @see gj.layout.Layout#applyTo(Graph)
   */
  public void layout(Graph graph) throws LayoutException {

    // remember
    appliedTo = graph;

    // remove debugging information that might be attached to the Graph
    debugShapes.clear();

    // something to do for me?
    if (graph.getNodes().isEmpty()) return;

    // get an orientation
    Orientation orientn = Orientation.get(isVertical,isTopDown);
    NodeOptions nopt = getNodeOptions();
    
    // and a node layout
    Algorithm nlayout = new Algorithm(
      orientn, 
      nopt, 
      arcOptions,
      isLatAlignmentEnabled,
      isBalanceChildren,
      isBendArcs
    );

    // keep track of nodes we haven't visited yet
    Set unvisited = new HashSet(graph.getNodes());

    // get the root of the graph
    Node root = getRoot();
    if (root==null||!graph.getNodes().contains(root)) root=(Node)unvisited.iterator().next();

    // loop as long as there are nodes that we haven't visited yet
    Rectangle2D bounds = new Rectangle2D.Double();
    while (true) {

      // create a Tree for current root assuming that all nodes in it will be visited
      Tree tree = new Tree(graph,root,nopt,orientn);
      unvisited.removeAll(tree.getNodes());

      // update bounds
      bounds.add(nlayout.layout(tree, isDebug()?debugShapes:null));
      
      // choose a new root (for a new sub-graph)
      if (isIgnoreUnreachables||unvisited.isEmpty()) break;

      // next
      root = (Node)unvisited.iterator().next();
    }


    // Lastly tell the graph its size
    graph.getBounds().setRect(bounds);

    // Done
  }

  /**
   * Default NodeOptions
   */
  private class DefaultNodeOptions implements NodeOptions {
    /** default padding n,w,e,s */
    private double[] pad = new double[]{ latPadding/2, lonPadding/2, lonPadding/2, latPadding/2};
    /**
     * @see gj.layout.tree.NodeOptions#getPadding()
     */
    public double[] getPadding(Node node, Orientation o) {
      if (node instanceof NodeOptions) 
        return ((NodeOptions)node).getPadding(node, o);
      return pad;
    }
    /**
     * @see gj.layout.tree.NodeOptions#getLatitude(Node, double, double)
     */
    public double getLatitude(Node node, double min, double max, Orientation o) {
      if (node instanceof NodeOptions) 
        return ((NodeOptions)node).getLatitude(node, min, max, o);
      return min + (max-min) * Math.min(1D, Math.max(0D, latAlignment));
    }
    /**
     * @see gj.layout.tree.NodeOptions#getLongitude(Node, Branch[], Orientation)
     */
    public double getLongitude(Node node, Branch[] children, Orientation o) {
      // delegate?
      if (node instanceof NodeOptions) 
        return ((NodeOptions)node).getLongitude(node, children, o);
      // calculate center point
      return Branch.getLongitude(children, lonAlignment, o);
    }
  } //DefaultNodeOptions
  
  /**
   * Default ArcOptions   */
  private class DefaultArcOptions implements ArcOptions {
    /**
     * @see gj.layout.tree.ArcOptions#getPort(gj.model.Arc, gj.model.Node)
     */
    public Point2D getPort(Arc arc, Node node, Orientation o) {
      if (arc instanceof ArcOptions)
        return ((ArcOptions)arc).getPort(arc, node, o);
      return node.getPosition();
    }
  } //DefaultArcOptions

} //TreeLayout