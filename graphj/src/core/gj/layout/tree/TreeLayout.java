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

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gj.awt.geom.Dimension2D;
import gj.awt.geom.Geometry;
import gj.awt.geom.Path;
import gj.layout.AbstractLayout;
import gj.layout.Layout;
import gj.layout.LayoutException;
import gj.layout.PathHelper;

import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.util.*;

/**
 * A Layout for trees
 */
public class TreeLayout extends AbstractLayout implements Layout, Cloneable {

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
  /*package*/ NodeOptions nodeOptions = new DefaultNodeOptions();

  /** whether children should be balanced or simply stacked */
  /*package*/ boolean isBalanceChildren = true;

  /** whether arcs are direct or bended */
  /*package*/ boolean isBendArcs = false;

  /** whether we're vertical or not (=horizontal) */
  /*package*/ boolean isVertical = true;

  /** whether we're doing it top/down or bottom/up */
  /*package*/ boolean isTopDown = true;

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
    lonAlignment=set;
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
    latAlignment=set;
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
   * @see gj.layout.Layout#applyTo(Graph)
   */
  public void applyTo(Graph graph) throws LayoutException {

    // remember
    appliedTo = graph;

    // remove debugging information that might be attached to the Graph
    debugShapes.clear();

    // something to do for me?
    if (graph.getNodes().isEmpty()) return;

    // constraints check
    lonAlignment = Math.min(1D, Math.max(0D, lonAlignment));
    latAlignment = Math.min(1D, Math.max(0D, latAlignment));

    // get an orientation
    Orientation orientation = getOrientation();

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
      Tree tree = new Tree(graph,root,latPadding,orientation);

      // all nodes in that will be visited
      unvisited.removeAll(tree.getNodes());

      // layout through root
      if (contour==null) {
        // 1st time
        contour = new NodeLayout().applyTo(tree, this);
        // move position for next tree & update bounds
        north = contour.north;
        west  = contour.west ;
        east  = contour.east ;
        south = contour.south;
      } else {
        contour = new NodeLayout().applyTo(tree, south, west, this);
        east  = Math.max(east , contour.east );
        south = contour.south;
      }
      
      // and keep the contour(s)
      if (isDebug()) debug(contour);

      // choose a new root (for a new sub-graph)
      if (isIgnoreUnreachables||unvisited.isEmpty()) break;

      // next
      root = (Node)unvisited.iterator().next();
    }


    // Lastly tell the graph its size
    graph.getBounds().setRect(orientation.getBounds(new Contour(north,west,east,south)));

    // Done
  }

  /**
   * A complement is a layout that is rotated counter-clockwise
   * (or clockwise for a complement of a complement)
   */
  /*package*/ TreeLayout getComplement() {
    try {
      // a clone gives us what we need
      TreeLayout result = (TreeLayout)clone();
      // rotate it according to whether we're a complement or not
      getOrientation().rotate(result, isComplement);
      // it's a complement now ... or back to not a complement
      result.isComplement = !isComplement;
      // the padding flips 
      result.lonPadding = latPadding;
      result.latPadding = lonPadding;
      // the layout of parents is extreme
      result.lonAlignment = result.isComplement ? 1D : 0D;
      // done
      return result;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Couldn't clone TreeLayout");
    }
  }

  /**
   * Returns the orientation in use
   */
  /*package*/ Orientation getOrientation() {
    return Orientation.get(isVertical,isTopDown);
  }

  /**
   * Returns the arc-layout in use
   */
  /*package*/ ArcLayout getArcLayout() {
    return ArcLayout.get(isBendArcs);
  }

  /**
   * Adds more debugging information
   */
  /*package*/ void debug(Contour contour) {

    // add debugging information about contour's segments
    Orientation o = getOrientation();
    Path path = new Path();

    Contour.Iterator it = new Contour.Iterator(contour, Contour.WEST);
    Point2D a = o.getPoint2D(it.north, it.longitude);
    path.moveTo(a);
    do {
      path.lineTo(o.getPoint2D(it.north, it.longitude));
      path.lineTo(o.getPoint2D(it.south, it.longitude));
    } while (it.next());
    Point2D b = path.getLastPoint();
    path.moveTo(a);

    it = new Contour.Iterator(contour, Contour.EAST);
    do {
      path.lineTo(o.getPoint2D(it.north, it.longitude));
      path.lineTo(o.getPoint2D(it.south, it.longitude));
    } while (it.next());
    path.lineTo(b);

    debugShapes.add(path);
    debugShapes.add(getOrientation().getBounds(contour));

    // done
  }

  /**
   * Default NodeOptions
   */
  private class DefaultNodeOptions implements NodeOptions {
    /**
     * @see gj.layout.tree.NodeOptions#set(Node)
     */
    public void set(Node node) {
      // ignored
    }
    /**
     * @see gj.layout.tree.NodeOptions#getAlignment(int)
     */
    public double getAlignment(int dir) {
      return dir==LAT ? latAlignment : lonAlignment;
    }
    /**
     * @see gj.layout.tree.NodeOptions#getPadding(int)
     */
    public double getPadding(int dir) {
      if (dir==WEST||dir==EAST) return lonPadding/2;
      return latPadding/2;
    }
  } //DefaultNodeOptions

} //TreeLayout