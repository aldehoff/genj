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

import gj.awt.geom.Geometry;
import gj.awt.geom.Path;
import gj.model.Node;
import gj.util.ArcIterator;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * The NodeLayout
 */
/*package*/ class NodeLayout {

  /** a stack of orientations to return to */
  private Stack oldos = new Stack();
  
  /** the orientation in use */
  private Orientation orientn;
  
  /** the node options in use */
  private NodeOptions nodeop;
  
  /** the arc options in use */
  private ArcOptions arcop;
  
  /** the orientation toggles */
  private Set orientntggls;
  
  /** the arc layout we use */
  private TreeArcLayout alayout;
  
  /** whether latitude alignment is enabled */
  private boolean latalign;
  
  /** whether we align children */
  private boolean balance;
  
  /**
   * Constructor
   */
  /*package*/ NodeLayout(Orientation orientation, NodeOptions nodeOptions, ArcOptions arcOptions, boolean isLatAlignmentEnabled, boolean isBalanceChildrenEnable, Set orientationToggles, TreeArcLayout arcLayout) {
    orientn = orientation;
    nodeop  = nodeOptions;
    arcop = arcOptions;
    latalign = isLatAlignmentEnabled;
    balance = isBalanceChildrenEnable;
    if (latalign) orientntggls = new HashSet();
    else orientntggls = orientationToggles;
    alayout = arcLayout;
  }
  
  /**
   * Apply to a node - root will stay at present position
   * @param tree the tree to layout
   * @param tlayout the tree-layout
   */
  /*package*/ Contour applyTo(Tree tree) {

    // Save root's original position
    Node root = tree.getRoot();
    double 
      rootLat = orientn.getLatitude (root.getPosition()),
      rootLon = orientn.getLongitude(root.getPosition());

    // layout starting with the root of the tree
    Contour result = layoutNode(root, null, tree, 0);
    
    // calculate delta to get everything back to original position
    double
      dlat = rootLat - orientn.getLatitude (root.getPosition()),
      dlon = rootLon - orientn.getLongitude(root.getPosition());

    // translate the absolute contour
    result.translate(dlat,dlon);
    
    // transform relative node/arc positions into absolute ones
    relative2absolute(tree.getRoot(), null, orientn.getPoint2D(dlat,dlon));

    // done
    return result;
  }
  
  /**
   * Apply to a node
   * @param tree the tree to layout
   * @param lat,lon where to place the tree's north/west
   * @param tlayout the tree-layout
   */
  /*package*/ Contour applyTo(Tree tree, double lat, double lon) {

    // layout starting with the root of the tree
    Contour result = layoutNode(tree.getRoot(), null, tree, 0);
    
    // calculate delta to get everything to lat/lon
    double 
      dlat = lat - result.north,
      dlon = lon - result.west ;
    
    // translate the absolute contour
    result.translate(dlat,dlon);
    
    // transform relative node/arc positions into absolute ones
    relative2absolute(tree.getRoot(), null, orientn.getPoint2D(dlat,dlon));

    // done
    return result;
  }

  /**
   * Layout a node and all its descendants
   * <il>
   *  <li>all children of node (without backtrack) are layouted
   *      recursively and placed into one row - returning a contour
   *  <li>node is placed as the parent of children - returning a contour
   *  <li>arcs are layouted between node and children
   *  <li>a merged contour for node and its children is calculated
   *  <li>note: all nodes (exception is node) and arcs have relative positions
   * </il>
   */
  private Contour layoutNode(Node node, Node parent, Tree tree, int generation) {
    
    // are we looking at an inverted case?
    boolean toggleOrientation = orientntggls.contains(node);
    if (toggleOrientation) {
      // patch alignment of nodes, too
      if (oldos.size()==0) nodeop = new ToggleAlignment(nodeop);
      // save old orientation
      oldos.push(orientn);
      // set new
      orientn = orientn.rotate((oldos.size()&1)==0);
    }

    // we layout the children
    Contour[] children = layoutChildren(node, parent, tree, generation);

    // we layout the root
    Contour root = layoutParent(node, children, tree, generation);

    // we layout the arcs
    alayout.layout(
      node, 
      toggleOrientation ? orientn.getLatitude(node.getPosition()) : root.south, 
      orientn,
      arcop
    );

    // make everything children/arcs directly 'under' node relative
    absolute2relative(node, parent);

    // The result is a hull comprised of root's and children's hull
    Contour result = Contour.merge(
      new Contour.List(children.length+2).add(root).add(children).add(root)
    );
     
    // If another layout was used to create the contour result
    if (toggleOrientation) {
      // transform result into 2d
      Rectangle2D bounds = orientn.getBounds(result);
      // restore old orientation
      orientn = (Orientation)oldos.pop();
      // transform result into lat/lon
      result = orientn.getContour(bounds);
      // restore alignment?
      if (oldos.size()==0) nodeop = ((ToggleAlignment)nodeop).getOriginal();
    }

    // done
    return result;
  }


  /**
   * Layout children of root and create contours for them
   */
  private Contour[] layoutChildren(Node node, Node parent, Tree tree, int generation) {

    Node[] nodes = new Node[node.getArcs().size()];
    Contour[] contours = new Contour[nodes.length];
    
    // we loop through all arcs leaving this node
    ArcIterator it = new ArcIterator(node);
    int c=0;while (it.next()) {

      // we don't go after seconds
      if (!it.isFirst) continue;
      // we don't go after loops
      if (it.isLoop) continue;
      // we don't follow 'back'
      if (it.dest==parent) continue;

      // the current child
      nodes[c] = it.dest;

      // recursive step into child
      contours[c] = layoutNode(nodes[c], node, tree, generation+1);

      // position 'new' child if not first
      if (c>0) {
        
        // place n-th child top-align
        double dlat = contours[c-1].north - contours[c].north;
        contours[c].translate(dlat, 0);
    
        // calculate the distnace to previous children
        double dlon = calcMinDist(contours, c, contours[c]);

        // place n-th child as close as possible
        contours[c].translate(0, -dlon);
        ModelHelper.move(nodes[c],orientn.getPoint2D(dlat, -dlon));
                
        // 'new' child is positioned
      }
      
      // one child handled
      c++;
    }

    // done
    Object[] result=new Contour[c];
    System.arraycopy(contours,0,result,0,c);
    return (Contour[])result;
  }
  
  /**
   * Place root in parent-position to children create a contour for it
   */
  private Contour layoutParent(Node parent, Contour[] children, Tree tree, int generation) {

    // the parent's contour
    Shape shape = parent.getShape();
    Contour result = orientn.getContour(shape!=null ? shape.getBounds2D() : new Rectangle2D.Double());
    result.north -= nodeop.getPadding(parent, nodeop.NORTH, orientn);
    result.south += nodeop.getPadding(parent, nodeop.SOUTH, orientn);
    result.west  -= nodeop.getPadding(parent, nodeop.WEST , orientn);
    result.east  += nodeop.getPadding(parent, nodeop.EAST , orientn);

    // the parent's position
    double lat,lon;
    if (children.length==0) {

      // a leaf is simply placed
      lon = nodeop.getLongitude(parent, 0, 0, 0, 0, orientn);
      lat = 0;

    } else {

      // calculate min/maxs
      double
        minc = children[0].getIterator(Contour.WEST).longitude - result.west,
        maxc = children[children.length-1].getIterator(Contour.EAST).longitude - result.east,
        mint =  Double.MAX_VALUE,
        maxt = -Double.MAX_VALUE;

      for (int c=0; c<children.length; c++) {
        mint = Math.min(mint, children[c].west - result.west);
        maxt = Math.max(maxt, children[c].east - result.east);
      }

      lon = nodeop.getLongitude(parent, minc, maxc, mint, maxt, orientn);
      lat = children[0].north - result.south;

    }

    // Override latitude for isAlignGeneration
    if (latalign) {
      lat = tree.getLatitude(generation);
      double
        min = lat - result.north,
        max = lat + tree.getHeight(generation) - result.south;

      lat = nodeop.getLatitude(parent, min, max, orientn);
    }

    // place it at (lat,lon)
    parent.getPosition().setLocation(orientn.getPoint2D(lat,lon));
    result.translate(lat,lon);
    if (latalign) {
      result.north = tree.getLatitude(generation);
    }

    // done
    return result;
  }

  /**
   * Calculates the minimum distance of n contours in cs with c 
   * @param cs contours
   * @param n number of contours in cs
   * @param c contour
   */
  private double calcMinDist(Contour[] cs, int css, Contour c) {

    // all min distances
    double[] ds = calcMinDists(cs,css,c);
    
    // find minimum distance
    double result = Double.MAX_VALUE;
    for (int d=0; d<ds.length; d++) result = Math.min(ds[d], result); 
      
    // done
    return result;
  }

  /**
   * Calculates the deltas of each contour in cs with c
   * @param cs contours to compare against
   * @param css number of contours in cs
   * @param c contour to compare against
   */
  private double[] calcMinDists(Contour[] cs, int css, Contour c) {
    
    // create a result
    double[] result = new double[css];

    // we'll iterate west of c - our east
    Contour.Iterator east = c.getIterator(c.WEST);
    
    // assume unlimited delta
    for (int i=0;i<css;i++) result[i] = Double.MAX_VALUE;
      
    // loop through from east to west
    loop: for (int i=css-1;i>=0;i--) {
      
      // here's the iterator west
      Contour.Iterator west = cs[i].getIterator(c.EAST);
      
      // calculate distance
      while (true) {
        
        // skip west segments north of east
        while (west.south<=east.north) if (!west.next()) continue loop;
        // skip east segments north of west
        while (east.south<=west.north) if (!east.next()) break loop;

        // calc distance of segments
        result[i] = Math.min( result[i], east.longitude-west.longitude);

        // skip northern segment
        if (west.south<east.south) {
          if (!west.next()) continue loop;
        } else {
          if (!east.next()) break loop;
        }
      
        // continue with next segment
      }      
      
      // continue further west
    }
    
    // done
    return result;
  }
  
  /**
   * Transforms absolute positions of direct descendants
   * into relative ones
   */
  private void absolute2relative(Node node, Node parent) {

    // loop through arcs    
    Point2D delta = Geometry.getNegative(node.getPosition());
    ArcIterator it = new ArcIterator(node);
    while (it.next()) {
      // don't follow backtrack
      if (it.dest==parent) continue;
      // relativate arc
      Path path = it.arc.getPath();
      if (path!=null) path.translate(delta);
      // relativate other
      if (it.isFirst&&!it.isLoop) ModelHelper.move(ModelHelper.getOther(it.arc, node), delta);
    }

    // done    
  }
  
  /**
   * Transforms all relative positions of tree starting
   * at node into absolute ones (recursively)
   */
  private void relative2absolute(Node node, Node parent, Point2D delta) {

    // change the node's position
    ModelHelper.move(node, delta);

    // propagate via arcs
    ArcIterator it = new ArcIterator(node);
    while (it.next()) {
      // .. only down the tree
      if (it.dest==parent) continue;
      // .. tell the arc's path
      Path path = it.arc.getPath();
      if (path!=null) path.translate(node.getPosition());
      // .. never loop'd
      if (it.isLoop) continue;
      // .. 1st only
      if (!it.isFirst) continue;
      // .. child
      Node child = ModelHelper.getOther(it.arc, node);
      // .. recursion
      relative2absolute(child, node, node.getPosition());
    }

    // done
  }

//  /**
//   * Balance children - we assume that at this point sub-tree i
//   * described by its root ns[i] and cs[i] for i>0 is placed as
//   * close as possible to all sub-trees with j<i
//   * (#ns==#cs > 2)
//   */
//  private void balanceChilden(Node[] ns, Contour[] cs, int n, Orientation orientn) {
//    
//    // calculate min dists of 0<=i<n-1 to n-1    
//    double[] ds = calcMinDists(cs, n-1, cs[n-1]);
//
//    // space between cs[n-1] and cs[n]
//    double space = ds[ds.length-1]; 
//    if (space<=0) return;
//    
//    // distribute space 
//    double[] shares = new double[n-1];
//    double share = space/(n-1);
//    for (int i=shares.length;i>0;i--) {
//      // default share 
//      shares[i-1] = i*share;
//      // check all but the last
//      if (i==ds.length) continue;
//      // too much?
//      if (shares[i-1]>ds[i]) shares[i-1]=ds[i];
//    }
//      
//    // move sub-trees
//    for (int i=0; i<shares.length-1; i++) {
//      share = shares[i];
//      cs[i+1].translate(0, share);
//      ModelHelper.translate(ns[i+1],orientn.getPoint2D(0, share));
//         	
//    }
//    	
//    // done
//  }
  
  /**
   * AlignNodeOptions
   */
  private class ToggleAlignment implements NodeOptions {
    /** the orignal node options */
    private NodeOptions original;
    /**
     * Constructor
     */
    private ToggleAlignment(NodeOptions originl) {
      original = originl;
    }
    /**
     * Returns the original no
     */
    /*package*/ NodeOptions getOriginal() {
      return original;
    }
    /**
     * @see gj.layout.tree.NodeOptions#getLatitude(Node, double, double)
     */
    public double getLatitude(Node node, double min, double max, Orientation o) {
      return original.getLatitude(node, min, max, o);
    }
    /**
     * @see gj.layout.tree.NodeOptions#getLongitude(Node, double, double, double, double)
     */
    public double getLongitude(Node node, double minc, double maxc, double mint, double maxt, Orientation o) {
      return (oldos.size()&1)==0 ? minc : maxc;
    }
    /**
     * @see gj.layout.tree.NodeOptions#getPadding(int)
     */
    public double getPadding(Node node, int dir, Orientation o) {
      if ((oldos.size()&1)!=0) dir = (dir+1)&3;
      return original.getPadding(node, dir, o);
    }
  } //ComplementNodeOptions

} //NodeLayout
