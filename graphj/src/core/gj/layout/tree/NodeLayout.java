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
import gj.model.Arc;
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
  
  /** the orientation toggles */
  private Set orientntggls;
  
  /** the arc layout we use */
  private ArcLayout alayout;
  
  /** whether latitude alignment is enabled */
  private boolean latalign;
  
  /**
   * Constructor
   */
  /*package*/ NodeLayout(Orientation orientation, NodeOptions nodeOptions, boolean isLatAlignmentEnabled, Set orientationToggles, ArcLayout arcLayout) {
    orientn = orientation;
    nodeop  = nodeOptions;
    latalign = isLatAlignmentEnabled;
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
    relative2absolute(tree.getRoot(), orientn.getPoint2D(dlat,dlon), null);

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
    relative2absolute(tree.getRoot(), orientn.getPoint2D(dlat,dlon), null);

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
  private Contour layoutNode(Node node, Arc backtrack, Tree tree, int generation) {
    
    // are we looking at an inverted case?
    boolean toggleOrientation = orientntggls.contains(node);
    if (toggleOrientation) {
      // patch alignment of nodes, too
      if (oldos.size()==0) nodeop = new AlignNodeOptions(nodeop);
      // save old orientation
      oldos.push(orientn);
      // set new
      orientn = orientn.rotate((oldos.size()&1)==0);
    }

    // we layout the children
    Contour[] children = layoutChildren(node, backtrack, tree, generation);

    // we layout the root
    Contour root = layoutParent(node, backtrack, children, tree, generation);

    // we layout the arcs
    alayout.applyTo(node, backtrack, toggleOrientation ? Double.NaN : root.south, orientn);

    // make everything children/arcs directly 'under' node relative
    absolute2relative(node, backtrack);

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
      if (oldos.size()==0) nodeop = ((AlignNodeOptions)nodeop).getOriginal();
    }

    // done
    return result;
  }


  /**
   * Layout children of root and create contours for them
   */
  private Contour[] layoutChildren(Node root, Arc backtrack, Tree tree, int generation) {

    Node[] nodes = new Node[root.getArcs().size()];
    Contour[] contours = new Contour[nodes.length];
    
    // we loop through all arcs leaving this node
    ArcIterator it = new ArcIterator(root);
    int c=0;while (it.next()) {

      // we don't go after seconds
      if (!it.isFirst) continue;
      // we don't go after loops
      if (it.isLoop) continue;
      // we don't follow 'back'
      if (it.isDup(backtrack)) continue;

      // the current child
      nodes[c] = ModelHelper.getOther(it.arc, root);

      // recursive step into child
      contours[c] = layoutNode(nodes[c], it.arc, tree, generation+1);

      // position 'new' child if not first
      if (c>0) {
        
        // place n-th child top-align
        double dlat = contours[c-1].north - contours[c].north;
        contours[c].translate(dlat, 0);
    
        // calculate the deltas to previous children
        double[] deltas = deltas(contours, c, contours[c]);
        int min = deltas.length-1;
        for (int d=deltas.length-2; d>=0; d--) min = deltas[d]<deltas[min] ? d : min;
        
        // place n-th child as close as possible
        double dlon = -deltas[min];
        contours[c].translate(0, dlon);
        ModelHelper.translate(nodes[c],orientn.getPoint2D(dlat,dlon));
        
        // balance children?
//        if (tlayout.isBalanceChildren) {
//    
//          // re-position all nodes east of [min]
//          double slon = (orientn.getLongitude(nodes[c].getPosition()) - orientn.getLongitude(nodes[min].getPosition()))/(c-min);
//          dlon = 0;
//          for (int s=min+1;s<c;s++) {
//            // calc delta which will place all nodes min<s<n fine
//            dlon = Math.max(
//              dlon,
//              (orientn.getLongitude(nodes[min].getPosition())+(s-min)*slon) - orientn.getLongitude(nodes[s].getPosition())
//            );
//            // reality-check against delta constraints
//            for (int m=s;m<c;m++) dlon = Math.min(deltas[m] - deltas[min], dlon);
//            // contour and sub-tree @ node
//            contours[s].translate(0,dlon);
//            ModelHelper.translate(nodes[s],orientn.getPoint2D(0,dlon));
//          }
//      
//          // done
//        }

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
  private Contour layoutParent(Node node, Arc backtrack, Contour[] children, Tree tree, int generation) {

    // the parent's contour
    Shape shape = node.getShape();
    Contour parent = shape==null ? new Contour() : orientn.getContour(shape.getBounds2D());
    parent.north -= nodeop.getPadding(node, nodeop.NORTH, orientn);
    parent.south += nodeop.getPadding(node, nodeop.SOUTH, orientn);
    parent.west  -= nodeop.getPadding(node, nodeop.WEST , orientn);
    parent.east  += nodeop.getPadding(node, nodeop.EAST , orientn);

    // the parent's position
    double lat,lon;
    if (children.length==0) {

      // a leaf is simply placed
      lon = 0;
      lat = 0;

    } else {

      // calculate min/maxs
      double
        minc = children[0].getIterator(Contour.WEST).longitude - parent.west,
        maxc = children[children.length-1].getIterator(Contour.EAST).longitude - parent.east,
        mint =  Double.MAX_VALUE,
        maxt = -Double.MAX_VALUE;

      for (int c=0; c<children.length; c++) {
        mint = Math.min(mint, children[c].west - parent.west);
        maxt = Math.max(maxt, children[c].east - parent.east);
      }

      lon = nodeop.getLongitude(node, minc, maxc, mint, maxt, orientn);
      lat = children[0].north - parent.south;

    }

    // Override latitude for isAlignGeneration
    if (latalign) {
      lat = tree.getLatitude(generation);
      double
        min = lat - parent.north,
        max = lat + tree.getHeight(generation) - parent.south;

      lat = nodeop.getLatitude(node, min, max, orientn);
    }

    // place it at (lat,lon)
    node.getPosition().setLocation(orientn.getPoint2D(lat,lon));
    parent.translate(lat,lon);
    if (latalign) {
      parent.north = tree.getLatitude(generation);
    }

    // done
    return parent;
  }

  /**
   * Calculates the deltas of each contour in cs with c
   */
  private double[] deltas(Contour[] cs, int css, Contour c) {
    
    // create a result
    double[] result = new double[css];

    // we'll iterate west of c - our east
    Contour.Iterator east = new Contour.Iterator(c, c.WEST);
    
    // assume unlimited delta
    for (int i=0;i<css;i++) result[i] = Double.MAX_VALUE;
      
    // loop through from east to west
    loop: for (int i=css-1;i>=0;i--) {
      
      // here's the iterator west
      Contour.Iterator west = new Contour.Iterator(cs[i], c.EAST);
      
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
  private void absolute2relative(Node node, Arc backtrack) {

    // loop through arcs    
    Point2D delta = Geometry.getNegative(node.getPosition());
    ArcIterator it = new ArcIterator(node);
    while (it.next()) {
      // don't follow backtrack
      if (it.isDup(backtrack)) continue;
      // relativate arc
      Path path = it.arc.getPath();
      if (path!=null) path.translate(delta);
      // relativate other
      if (it.isFirst&&!it.isLoop) ModelHelper.translate(ModelHelper.getOther(it.arc, node), delta);
    }

    // done    
  }
  
  /**
   * Transforms all relative positions of tree starting
   * at node into absolute ones (recursively)
   */
  private void relative2absolute(Node node, Point2D delta, Arc backtrack) {

    // change the node's position
    ModelHelper.translate(node, delta);

    // propagate via arcs
    ArcIterator it = new ArcIterator(node);
    while (it.next()) {
      // .. only down the tree
      if (it.isDup(backtrack)) continue;
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
      relative2absolute(child, node.getPosition(), it.arc);
    }

    // done
  }

  
  /**
   * AlignNodeOptions
   */
  private class AlignNodeOptions implements NodeOptions {
    /** the orignal node options */
    private NodeOptions original;
    /**
     * Constructor
     */
    private AlignNodeOptions(NodeOptions originl) {
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
