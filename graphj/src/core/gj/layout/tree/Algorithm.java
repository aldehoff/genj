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

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
  
  /** whether latitude alignment is enabled */
  private boolean latalign;
  
  /** whether we align children */
  private boolean balance;
  
  /** whether we bend arcs */
  private boolean bendarcs;
  
  /**
   * Constructor
   */
  /*package*/ Algorithm(Orientation orientation, NodeOptions nodeOptions, ArcOptions arcOptions, boolean isLatAlignmentEnabled, boolean isBalanceChildrenEnable, boolean isBendedArcs) {
    orientn = orientation;
    nodeop  = nodeOptions;
    arcop = arcOptions;
    latalign = isLatAlignmentEnabled;
    balance = isBalanceChildrenEnable;
    bendarcs = isBendedArcs;
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
    Contour result = branch.finalize(dlat, dlon, orientn);
    
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
    
    // we calculate the children (now from west to east)
    Branch[] children = calcChildren(node, parent, tree, generation);
    
    // create a contour for the root @ the right position
    Contour contour = calcParentPosition(node, children, tree, generation);
    
    // calculate arcs to children
    calcArcs( node, contour ); 

    // merge contours
    contour = Contour.merge(Branch.getCountoursForMerge(contour, children));
     
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
      layout(it.dest, root, tree, generation+1).insertEastOf(children, orientn);
        
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
   * Calculate parent's position and contour
   */
  private Contour calcParentPosition(Node root, Branch[] children, Tree tree, int generation) {
  
    // the parent's contour
    Shape shape = root.getShape();
    Contour result = orientn.getContour(
      shape!=null ? shape.getBounds2D() : new Rectangle2D.Double(),
      nodeop.getPadding(root, orientn)
    );
    
    // the parent's position
    double lat = 0, lon = 0;
    if (children.length>0) {
  
      lon = nodeop.getLongitude(root, children, orientn);
      lat = children[0].getLatitude() - result.south;
  
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
   * Calculate the balance of branches that are placed from west to east 
   */
  private void calcBalance(Branch[] branches, int start, int end, boolean right) {
//    // nothing to do if nothing between start and end
//    if (start+1==end) return;
//    
//    // calculate space between branches
//    double[] spaces = new double[end-start];
//    double space = 0;
//    for (int b=0; b<spaces.length; b++) {
//      double
//        east = branches[b].contour.getIterator(Contour.EAST).longitude,
//        west = branches[b+1].contour.getIterator(Contour.WEST).longitude;
//      spaces[b] = west-east;
//      space += spaces[b];
//    }
//    
//    // the average space
//    if (space==0) return;
//    double avg = space/spaces.length;
//    
//    for (int b=0; b<spaces.length; b++) {
//    }
//    
//    // distribute from right to left
//    double lon = branches[end].contour
//    for (int b=end-1; b>start; b--) {
//      
//      // b's desired lon
//      double lon = space*(b-start);
//      
//      // possibilities :
//      // (1) would move b too far
//      // (2) would move b not far enough
//      // (3) otherwise 
//      
//    	branches[b].moveTo(0, lon);
//    } 
           
    // done
  }
    
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
