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
import java.util.Collection;
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
   */
  /*package*/ Rectangle2D layout(Tree tree, Collection debugShapes) {

    // Save root's original position
    Node root = tree.getRoot();
    double 
      rootLat = orientn.getLatitude (root.getPosition()),
      rootLon = orientn.getLongitude(root.getPosition());

    // layout starting with the root of the tree
    Branch branch = layout(root, null, tree, 0);
    
    // calculate delta to get everything back to original position
    double
      dlat = rootLat - orientn.getLatitude (root.getPosition()),
      dlon = rootLon - orientn.getLongitude(root.getPosition());

    // finalize and return
    Contour result = branch.finalize(dlat, dlon);
    
    // debug?
    if (debugShapes!=null) debugShapes.add(new DebugShape(result));
    
    // done
    return orientn.getBounds(result); 
  }
  
  /**
   * Layout a node and all its descendants
   * <il>
   *  <li>all children of node (without backtracking to parent) are layouted
   *      recursively and placed beside each other (west to east)
   *  <li>node is placed as atop of children (north of)
   *  <li>arcs are layouted between node and its children
   *  <li>arcs/nodes to children are placed relative to root  
   *  <li>a merged contour for node and its children is calculated
   * </il>
   */
  private Branch layout(Node node, Node parent, Tree tree, int generation) {
    
    // are we looking at an inverted case?
    boolean toggleOrientation = orientntggls.contains(node);
    if (toggleOrientation) {
      // patch alignment of nodes, too
      if (oldos.size()==0) nodeop = new AlignedNodeOptions(nodeop);
      // save old orientation
      oldos.push(orientn);
      // set new
      orientn = orientn.rotate((oldos.size()&1)==0);
    }

    // we calculate the children (now from west to east)
    Branch[] children = calcChildren(node, parent, tree, generation);
    
    // create a contour for the root @ the right position
    Contour contour = calcParentPosition(node, children, tree, generation);
    
    // calculate arcs to children
    calcArcs( node, contour ); 

    // merge contours
    contour = calcContour(contour, children);
     
    // If another layout was used to create the contour result
    if (toggleOrientation) {
      // transform result into 2d
      Rectangle2D bounds = orientn.getBounds(contour);
      // restore old orientation
      orientn = (Orientation)oldos.pop();
      // transform result into lat/lon
      contour = orientn.getContour(bounds);
      // restore alignment?
      if (oldos.size()==0) nodeop = ((AlignedNodeOptions)nodeop).getOriginal();
    }

    // done
    return new Branch(node, parent, contour);
  }


  /**
   * Calculate branches for children side-by-side
   */
  private Branch[] calcChildren(Node root, Node parent, Tree tree, int generation) {

    // prepare some space
    List children = new ArrayList(root.getArcs().size());
    
    // looping through all arcs leaving root
    ArcIterator it = new ArcIterator(root);
    while (it.next()) {
      
      // we don't go after seconds, loops or upwards(parent)
      if (!it.isFirst||it.isLoop||it.dest==parent) continue;

      // recursive step for another child that we 
      // insert beside (east) other children 
      layout(it.dest, root, tree, generation+1).insertEastOf(children);
        
      // next
   }

    // create result
    Branch[] result = (Branch[])children.toArray(new Branch[children.size()]);
    
    // balancing?
    if (balance) calcBalance(result,0,result.length-1,true);

    // done
    return result;
  }


  /**
   * Calculate a merged contour
   */
  private Contour calcContour(Contour parent, Branch[] children) {
    List contours = new ArrayList(children.length+2);
    contours.add(parent);
    for (int c=0; c<children.length; c++) contours.add(children[c].contour);        
    contours.add(parent);
    return Contour.merge((Contour[])contours.toArray(new Contour[0]));
  }

  /**
   * Calculate parent's position and contour
   */
  private Contour calcParentPosition(Node root, Branch[] children, Tree tree, int generation) {
  
    // the parent's contour
    Shape shape = root.getShape();
    Contour result = orientn.getContour(shape!=null ? shape.getBounds2D() : new Rectangle2D.Double());
    result.north -= nodeop.getPadding(root, nodeop.NORTH, orientn);
    result.south += nodeop.getPadding(root, nodeop.SOUTH, orientn);
    result.west  -= nodeop.getPadding(root, nodeop.WEST , orientn);
    result.east  += nodeop.getPadding(root, nodeop.EAST , orientn);
    
    // the parent's position
    double lat = 0, lon = 0;
    if (children.length>0) {
  
      // calculate min/maxs
      double
        minc = children[0].contour.getIterator(Contour.WEST).longitude - result.west,
        maxc = children[children.length-1].contour.getIterator(Contour.EAST).longitude - result.east,
        mint =  Double.MAX_VALUE,
        maxt = -Double.MAX_VALUE;
  
      for (int c=0; c<children.length; c++) {
        mint = Math.min(mint, children[c].contour.west - result.west);
        maxt = Math.max(maxt, children[c].contour.east - result.east);
      }
  
      lon = nodeop.getLongitude(root, minc, maxc, mint, maxt, orientn);
      lat = children[0].contour.north - result.south;
  
    }
  
    // Override latitude for isAlignGeneration
    if (latalign) {
      lat = tree.getLatitude(generation);
      double
        min = lat - result.north,
        max = lat + tree.getHeight(generation) - result.south;
  
      lat = nodeop.getLatitude(root, min, max, orientn);
    }
  
    // place it at (lat,lon)
    root.getPosition().setLocation(orientn.getPoint2D(lat,lon));
    result.translate(lat,lon);
    
    if (latalign) {
      result.north = tree.getLatitude(generation);
    }
  
    // done
    return result;
  }
  
  /**
   * Calculate the arcs to children
   */
  private void calcArcs(Node node, Contour parent) {
    
    // Loop through arcs to children (without backtrack)
    ArcIterator it = new ArcIterator(node);
    while (it.next()) {
      // no path no interest
      if (it.arc.getPath()==null) continue;
      // handle loops separate from specialized
      if (it.isLoop) ArcHelper.update(it.arc);
      else {
        if (bendarcs) calcBendedArc(it.arc, parent);
        else calcStraightArc(it.arc);
      }
    }
    // done      
  }
  
  /**
   * make a straight arc
   */
  private void calcStraightArc(Arc arc) {
    
    // grab nodes and their position/shape
    Node
      n1 = arc.getStart(),
      n2 = arc.getEnd  ();
    Point2D 
      p1 = arcop.getPort(arc, n1, orientn),
      p2 = arcop.getPort(arc, n2, orientn);
    Shape 
      s1 = n1.getShape(),
      s2 = n2.getShape();

    // calculate south of p1 and north of p2
    p1 = Geometry.getIntersection(
      p1, orientn.getPoint2D(orientn.getLatitude(p2), orientn.getLongitude(p1)),
      p1, s1
    );
    p2 = Geometry.getIntersection(
      p2, orientn.getPoint2D(orientn.getLatitude(p1), orientn.getLongitude(p2)),
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
  private void calcBendedArc(Arc arc, Contour parent) {
    
    // grab arc's information
    Node
      n1 = arc.getStart(),
      n2 = arc.getEnd();
      
    Point2D
      p1 = arcop.getPort(arc, n1, orientn),
      p2 = new Point2D.Double(),
      p3 = new Point2D.Double(),
      p4 = arcop.getPort(arc, n2, orientn);

    p2.setLocation(orientn.getPoint2D(parent.south, orientn.getLongitude(p1)));
    p3.setLocation(orientn.getPoint2D(parent.south, orientn.getLongitude(p4)));

    // layout       
    ArcHelper.update(arc.getPath(), new Point2D[]{p1,p2,p3,p4}, n1.getShape(), n2.getShape());
    
    // done
  }
    
  /**
   * Calculates the minimum distance of list of branches and other
   * @param branches the list of branches
   * @param other branch
   */
  private double calcMinimumDistance(List branches, Branch other) {

    // all min distances
    double[] ds = calcMinimumDistances(branches, other);
    
    // find minimum distance
    double result = Double.MAX_VALUE;
    for (int d=0; d<ds.length; d++) result = Math.min(ds[d], result); 
      
    // done
    return result;
  }

  /**
   * Calculates the distances of a list of branches and other
   * @param branches the list of branches
   * @param other branch
   */
  private double[] calcMinimumDistances(List branches, Branch other) {
    
    // create a result
    double[] result = new double[branches.size()];
    for (int r=0;r<result.length;r++) result[r] = Double.MAX_VALUE;

    // we'll iterate west-side of other -> the east
    Contour.Iterator east = other.contour.getIterator(Contour.WEST);
      
    // loop through from east to west
    loop: for (int r=result.length-1;r>=0;r--) {
      
      // the east-side of branch r -> west
      Contour.Iterator west = ((Branch)branches.get(r)).contour.getIterator(Contour.EAST);
      
      // calculate distance
      while (true) {
        
        // skip west segments north of east
        while (west.south<=east.north) if (!west.next()) continue loop;
        // skip east segments north of west
        while (east.south<=west.north) if (!east.next()) break loop;

        // calc distance of segments
        result[r] = Math.min( result[r], east.longitude-west.longitude);

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
   * Calculate the balance of branches that are placed from west to east 
   */
  private void calcBalance(Branch[] branches, int start, int end, boolean right) {
    // FIXME
  }
    
  /**
   * AlignNodeOptions
   */
  private class AlignedNodeOptions implements NodeOptions {
    /** the orignal node options */
    private NodeOptions original;
    /**
     * Constructor
     */
    private AlignedNodeOptions(NodeOptions originl) {
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


  /**
   * The layouted branch of a tree 
   * <il>
   *  <li>all nodes for descendants of root have a position relative to ancestor (delta) 
   *  <li>all arcs in the branch have a position relative to root (delta) 
   * </il>
   */
  private class Branch {
    
    /** the root */
    private Node root;
    
    /** the contour */
    private Contour contour;
    
    /**
     * Constructor
     */
    private Branch(Node root, Node parent, Contour contour) {
      
      // remember
      this.root = root;
      this.contour = contour;
      
      // place arcs/children relative to current node
      Point2D delta = Geometry.getNegative(root.getPosition());
      ArcIterator it = new ArcIterator(root);
      while (it.next()) {
        // don't follow back
        if (it.dest==parent) continue;
        // update the arc
        Path path = it.arc.getPath();
        if (path!=null) path.translate(delta);
        // don't go twice or loop
        if (!it.isFirst&&it.isLoop) continue;
        // update the node
        ModelHelper.move(it.dest, delta);
      }

      // done      
    }
    
    /**
     * Places all nodes in the branch at absolute positions
     */
    private Contour finalize(double dlat, double dlon) {
      finalize(root, null, orientn.getPoint2D(dlat,dlon));
      contour.translate(dlat, dlon);
      return contour;
    }
    
    /**
     * Places all nodes under node at absolute positions 
     */
    private void finalize(Node node, Node parent, Point2D delta) {

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
        // .. recursion
        finalize(it.dest, node, node.getPosition());
      }

      // done
    }
     
    /**
     * Move this branch
     */
    private void moveBy(double dlat, double dlon) {
      contour.translate(dlat, dlon);
      ModelHelper.move(root, orientn.getPoint2D(dlat, dlon));
    }

    /**
     * Move this branch
     */
    private void moveTo(double lat, double lon) {
      Point2D pos = root.getPosition();
      moveBy( lat - orientn.getLatitude(pos), lon - orientn.getLongitude(pos));
    }
  
    /**
     * Inserts this branch beside (east) and top-align of others
     */
    private void insertEastOf(List others) {
      
      // no placing to do?
      if (!others.isEmpty()) {
        
        // top-align to first 
        moveBy(((Branch)others.get(0)).contour.north - this.contour.north, 0);
          
        // and then as close as possible to other (east of)
        moveBy(0, -calcMinimumDistance(others, this));
        
      }
        
      // insert
      others.add(this);
      
    }
  
  } //Branch

  /**
   * A shape that can be used for debugging a contour
   */
  private class DebugShape extends Path {
    /**
     * Constructor
     */
    private DebugShape(Contour contour) {

      // add debugging information about contour's segments
      Contour.Iterator it = contour.getIterator(Contour.WEST);
      Point2D a = orientn.getPoint2D(it.north, it.longitude);
      moveTo(a);
      do {
        lineTo(orientn.getPoint2D(it.north, it.longitude));
        lineTo(orientn.getPoint2D(it.south, it.longitude));
      } while (it.next());
      Point2D b = getLastPoint();
      moveTo(a);

      it = contour.getIterator(Contour.EAST);
      do {
        lineTo(orientn.getPoint2D(it.north, it.longitude));
        lineTo(orientn.getPoint2D(it.south, it.longitude));
      } while (it.next());
      lineTo(b);

      // done
    }
  }

} //NodeLayout
