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
import gj.util.ArcHelper;
import gj.util.ArcIterator;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * The layout's algorithm
 */
/*package*/ class Algorithm {

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
  
  /** whether latitude alignment is enabled */
  private boolean latalign;
  
  /** whether we align children */
  private boolean balance;
  
  /** whether we bend arcs */
  private boolean bendarcs;
  
  /**
   * Constructor
   */
  /*package*/ Algorithm(Orientation orientation, NodeOptions nodeOptions, ArcOptions arcOptions, boolean isLatAlignmentEnabled, boolean isBalanceChildrenEnable, Set orientationToggles, boolean isBendedArcs) {
    orientn = orientation;
    nodeop  = nodeOptions;
    arcop = arcOptions;
    latalign = isLatAlignmentEnabled;
    balance = isBalanceChildrenEnable;
    bendarcs = isBendedArcs;
    if (latalign) orientntggls = new HashSet();
    else orientntggls = orientationToggles;
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
    placeAllDescendants(tree.getRoot(), null, orientn.getPoint2D(dlat,dlon));

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
    placeAllDescendants(tree.getRoot(), null, orientn.getPoint2D(dlat,dlon));

    // done
    return result;
  }

  /**
   * Layout a node and all its descendants
   * <il>
   *  <li>all children of node (without backtracking to parent) are layouted
   *      recursively and placed beside each other (west to east)
   *  <li>node is placed as the parent of children (north of)
   *  <li>arcs are layouted between node and its children
   *  <li>arcs/nodes to children of node are placed relative to its position  
   *  <li>a merged contour for node and its children is calculated
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

    // .. and the arcs to its children
    layoutArcs2Children( 
      node, 
      toggleOrientation ? orientn.getLatitude(node.getPosition()) : root.south, 
      orientn,
      arcop
    );

    // place arcs/children relative to current node
    placeChildrenRelative2Parent(node, parent);

    // The result is a hull comprised of root's and children's hull
    List l = new ArrayList(); 
      l.add(root);
      for (int i=0; i<children.length; i++) {
      	l.add(children[i]);
      } 
      l.add(root);
    Contour result = Contour.merge((Contour[])l.toArray(new Contour[0]));
     
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

      // we don't go after seconds, loops or backwards
      if (!it.isFirst||it.isLoop||it.dest==parent) continue;

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
  private void placeChildrenRelative2Parent(Node node, Node parent) {

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
  private void placeAllDescendants(Node node, Node parent, Point2D delta) {

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
      placeAllDescendants(child, node, node.getPosition());
    }

    // done
  }

  /**
   * make an arc
   */
  private void layoutArcs2Children(Node node, double equator, Orientation orientation, ArcOptions arcop) {
    
    // Loop through arcs to children (without backtrack)
    ArcIterator it = new ArcIterator(node);
    while (it.next()) {
      // no path no interest
      if (it.arc.getPath()==null) continue;
      // handle loops separate from specialized
      if (it.isLoop) ArcHelper.update(it.arc);
      else {
        if (bendarcs) layoutBendedArc(it.arc, equator, orientation, arcop);
        else layoutStraightArc(it.arc, orientation, arcop);
      }
    }
    // done      
  }
  
  /**
   * make a straight arc
   */
  private void layoutStraightArc(Arc arc, Orientation o, ArcOptions arcop) {
    
    // grab nodes and their position/shape
    Node
      n1 = arc.getStart(),
      n2 = arc.getEnd  ();
    Point2D 
      p1 = arcop.getPort(arc, n1, o),
      p2 = arcop.getPort(arc, n2, o);
    Shape 
      s1 = n1.getShape(),
      s2 = n2.getShape();

    // calculate south of p1 and north of p2
    p1 = Geometry.getIntersection(
      p1, o.getPoint2D(o.getLatitude(p2), o.getLongitude(p1)),
      p1, s1
    );
    p2 = Geometry.getIntersection(
      p2, o.getPoint2D(o.getLatitude(p1), o.getLongitude(p2)),
      p2, s2
    );

    // strike a path
    Path path = arc.getPath();
    path.reset();
    path.moveTo(p1);
    path.lineTo(p2);
    
    // done  
  }
  
  /**
   * make a bended arc
   */
  protected void layoutBendedArc(Arc arc, double equator, Orientation o, ArcOptions arcop) {
    
    // grab arc's information
    Node
      n1 = arc.getStart(),
      n2 = arc.getEnd();
      
    Point2D
      p1 = arcop.getPort(arc, n1, o),
      p2 = new Point2D.Double(),
      p3 = new Point2D.Double(),
      p4 = arcop.getPort(arc, n2, o);

    // straight line up?
    if (o.getLongitude(p1)==o.getLongitude(p4)) {
      layoutStraightArc(arc, o, arcop);
      return;
    }        

    // bending around equator
    if (equator==o.getLatitude(p1)) {
      p2.setLocation(o.getPoint2D(equator, o.getLongitude(p4)));
      p3=p2;
    } else if (equator==o.getLatitude(p4)) {
      p2.setLocation(o.getPoint2D(equator, o.getLongitude(p1)));
      p3=p2;
    } else {
      p2.setLocation(o.getPoint2D(equator, o.getLongitude(p1)));
      p3.setLocation(o.getPoint2D(equator, o.getLongitude(p4)));
    }

    // layout       
    ArcHelper.update(
      arc.getPath(),
      new Point2D[]{p1,p2,p3,p4},
      n1.getShape(),
      n2.getShape()
    );
    
    // done
  }
    
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
  } //ToggleAlignment


} //NodeLayout
