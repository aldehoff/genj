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

import gj.awt.geom.Path;
import gj.layout.AbstractLayout;
import gj.layout.Layout;
import gj.layout.LayoutException;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import java.awt.geom.Point2D;
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
  private double lonPadding = 12;

  /** the alignment of parents */
  private double lonAlignment = 0.5;

  /** the alignment of generations (if isAlignGeneration) */
  private double latAlignment = 0.5;
  
  /** whether we ignore unreached nodes */
  private boolean isIgnoreUnreachables = false;
  
  /** whether latAlignment is enabled */
  /*package*/ boolean isLatAlignmentEnabled = false;
  
  /** current node options */
  private NodeOptions nodeOptions = new DefaultNodeOptions();
  
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

  /** the inverters of the tree */
  /*package*/ Set orientationToggles = new HashSet();

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
   * Declares that the orientation at given node should
   * be changed (counter-/clockwise) for the whole sub-tree
   * starting at that node. If the node was marked for
   * an orientation change this reverses that mark.
   */
  public void toggleOrientation(Node node) {
    if (!orientationToggles.remove(node)) orientationToggles.add(node);
  }
  
  /**
   * Sets custom node options to use during layout
   */
  public void setNodeOptions(NodeOptions no) {
    nodeOptions = no;
  }
  
  /**
   * Sets custom arc options to use during layout
   */
  public void setArcOptions(ArcOptions ao) {
    arcOptions = ao;
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
    
    // and a node layout
    NodeLayout nlayout = new NodeLayout(
      orientn, 
      nodeOptions, 
      arcOptions,
      isLatAlignmentEnabled,
      orientationToggles,
      TreeArcLayout.get(isBendArcs)
    );

    // keep track of nodes we haven't visited yet
    Set unvisited = new HashSet(graph.getNodes());

    // get the root of the graph
    Node root = getRoot();
    if (root==null||!graph.getNodes().contains(root)) root=(Node)unvisited.iterator().next();

    // loop as long as there are nodes that we haven't visited yet
    double north=0, west=0, east=0, south=0;
    Contour contour = null;
    while (true) {

      // create a Tree for current root
      Tree tree = new Tree(graph,root,latPadding,orientn);

      // all nodes in that will be visited
      unvisited.removeAll(tree.getNodes());

      // layout through root
      if (contour==null) {
        // 1st time
        contour = nlayout.applyTo(tree);
        // move position for next tree & update bounds
        north = contour.north;
        west  = contour.west ;
        east  = contour.east ;
        south = contour.south;
      } else {
        contour = nlayout.applyTo(tree, south, west);
        east  = Math.max(east , contour.east );
        south = contour.south;
      }
      
      // and keep the contour(s)
      if (isDebug()) debug(contour, orientn);

      // choose a new root (for a new sub-graph)
      if (isIgnoreUnreachables||unvisited.isEmpty()) break;

      // next
      root = (Node)unvisited.iterator().next();
    }


    // Lastly tell the graph its size
    graph.getBounds().setRect(orientn.getBounds(new Contour(north,west,east,south)));

    // Done
  }

  /**
   * A complement is a layout that is rotated counter-clockwise
   * (or clockwise for a complement of a complement)
   */
//  /*package*/ TreeLayout getComplement() {
//    try {
//      // a clone gives us what we need
//      TreeLayout result = (TreeLayout)clone();
//      // rotate it according to whether we're a complement or not
//      getOrientation().rotate(result, isComplement);
//      // it's a complement now ... or back to not a complement
//      result.isComplement = !isComplement;
//      // the padding flips 
//      result.lonPadding = latPadding;
//      result.latPadding = lonPadding;
//      // the layout of parents is extreme
//      result.lonAlignment = result.isComplement ? 1D : 0D;
//      // done
//      return result;
//    } catch (CloneNotSupportedException e) {
//      throw new RuntimeException("Couldn't clone TreeLayout");
//    }
//  }

  /**
   * Adds more debugging information
   */
  private void debug(Contour contour, Orientation orientn) {

    // add debugging information about contour's segments
   Path path = new Path();

    Contour.Iterator it = new Contour.Iterator(contour, Contour.WEST);
    Point2D a = orientn.getPoint2D(it.north, it.longitude);
    path.moveTo(a);
    do {
      path.lineTo(orientn.getPoint2D(it.north, it.longitude));
      path.lineTo(orientn.getPoint2D(it.south, it.longitude));
    } while (it.next());
    Point2D b = path.getLastPoint();
    path.moveTo(a);

    it = new Contour.Iterator(contour, Contour.EAST);
    do {
      path.lineTo(orientn.getPoint2D(it.north, it.longitude));
      path.lineTo(orientn.getPoint2D(it.south, it.longitude));
    } while (it.next());
    path.lineTo(b);

    debugShapes.add(path);
    debugShapes.add(orientn.getBounds(contour));

    // done
  }

  /**
   * Default NodeOptions
   */
  private class DefaultNodeOptions implements NodeOptions {
    /**
     * @see gj.layout.tree.NodeOptions#getPadding(int)
     */
    public double getPadding(Node node, int dir, Orientation o) {
      if (node instanceof NodeOptions) 
        return ((NodeOptions)node).getPadding(node, dir, o);
      if (dir==WEST||dir==EAST) return lonPadding/2;
      return latPadding/2;
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
     * @see gj.layout.tree.NodeOptions#getLongitude(Node, double, double, double, double)
     */
    public double getLongitude(Node node, double minc, double maxc, double mint, double maxt, Orientation o) {
      if (node instanceof NodeOptions) 
        return ((NodeOptions)node).getLongitude(node, minc, maxc, mint, maxt, o);
      return minc + (maxc-minc) * Math.min(1D, Math.max(0D, lonAlignment));
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