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
import gj.model.Arc;
import gj.model.Node;
import gj.util.ArcIterator;
import gj.util.ModelHelper;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * The NodeLayout
 */
/*package*/ class NodeLayout {
  
  /**
   * Apply to a node
   */
  /*package*/ Contour applyTo(Tree tree, double lat, double lon, TreeLayout tlayout) {

    // layout starting with the root of the tree
    Contour result = layoutNode(tree.getRoot(), null, tree, 0, tlayout);
    
    // calculate delta to get everything to lat/lon
    double
      dlat = lat - result.north,
      dlon = lon - result.west;

    // translate the absolute contour
    result.translate(dlat,dlon);
    
    // transform relative node/arc positions into absolute ones
    relative2absolute(tree.getRoot(), tlayout.getOrientation().getPoint2D(dlat,dlon), null);

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
  private Contour layoutNode(Node node, Arc backtrack, Tree tree, int generation, TreeLayout tlayout) {
    
    // are we looking at an inverted case?
    boolean toggleOrientation = tlayout.orientationToggles.contains(node)&&!tlayout.isLatAlignmentEnabled;

    // prepare our Layout we'll be working on
    TreeLayout layout = toggleOrientation ? tlayout.getComplement() : tlayout;

    // we layout the children
    Contour[] children = layoutChildren(node, backtrack, tree, generation, layout);

    // we layout the root
    Contour root = layoutParent(node, backtrack, children, tree, generation, layout);

    // we layout the arcs
    layout.getArcLayout().applyTo(node, backtrack, toggleOrientation ? Double.NaN : root.south, layout.getOrientation());

    // make everything children/arcs directly 'under' node relative
    absolute2relative(node, backtrack);

    // The result is a hull comprised of root's and children's hull
    Contour result = Contour.merge(
      new Contour.List(children.length+2).add(root).add(children).add(root)
    );
      
    // If another layout was used to create the contour result
    if (layout!=tlayout) {
      // .. we have to transform it
      Rectangle2D bounds = layout.getOrientation().getBounds(result);
      result = tlayout.getOrientation().getContour(bounds);
    }

    // done
    return result;
  }


  /**
   * Layout children of root and create contours for them
   */
  private Contour[] layoutChildren(Node root, Arc backtrack, Tree tree, int generation, TreeLayout tlayout) {

    Orientation o = tlayout.getOrientation();
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
      contours[c] = layoutNode(nodes[c], it.arc, tree, generation+1, tlayout);

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
        ModelHelper.translate(nodes[c],o.getPoint2D(dlat,dlon));
        
        // balance children?
        if (tlayout.isBalanceChildren) {
    
          // re-position all nodes east of [min]
          double slon = (o.getLongitude(nodes[c].getPosition()) - o.getLongitude(nodes[min].getPosition()))/(c-min);
          dlon = 0;
          for (int s=min+1;s<c;s++) {
            // calc delta which will place all nodes min<s<n fine
            dlon = Math.max(
              dlon,
              (o.getLongitude(nodes[min].getPosition())+(s-min)*slon) - o.getLongitude(nodes[s].getPosition())
            );
            // reality-check against delta constraints
            for (int m=s;m<c;m++) dlon = Math.min(deltas[m] - deltas[min], dlon);
            // contour and sub-tree @ node
            contours[s].translate(0,dlon);
            ModelHelper.translate(nodes[s],tlayout.getOrientation().getPoint2D(0,dlon));
          }
      
          // done
        }

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
  private Contour layoutParent(Node node, Arc backtrack, Contour[] children, Tree tree, int generation, TreeLayout tlayout) {

    // grab some information
    Orientation orientation = tlayout.getOrientation();
    NodeOptions.Padding pad = tlayout.nodeOptions.getPadding(node);
    NodeOptions.Alignment align = tlayout.nodeOptions.getAlignment(node);

    // the parent's contour
    Contour parent = orientation.getContour(node.getShape().getBounds2D());
    parent.north -= pad.north;
    parent.south += pad.south;
    parent.west  -= pad.west ;
    parent.east  += pad.east ;

    // the parent's position
    double lat,lon;
    if (children.length==0) {

      // a leaf is simply placed
      lon = 0;
      lat = 0;

    } else {

      // relative placement above children 0..1
      double
        min = children[0].getIterator(Contour.WEST).longitude - parent.west,
        max = children[children.length-1].getIterator(Contour.EAST).longitude - parent.east;
        
      lon = min + (max-min)*align.lon;
      lat = children[0].north - parent.south;

    }

    // Override latitude for isAlignGeneration
    if (tlayout.isLatAlignmentEnabled) {
      lat = tree.getLatitude(generation);
      double
        min = lat - parent.north,
        max = lat + tree.getHeight(generation) - parent.south;

      lat = min + (max-min)*align.lat;
    }

    // place it at (lat,lon)
    node.getPosition().setLocation(orientation.getPoint2D(lat,lon));
    parent.translate(lat,lon);
    if (tlayout.isLatAlignmentEnabled) {
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
      it.arc.getPath().translate(delta);
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
      // .. tell the arc
      it.arc.getPath().translate(node.getPosition());
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

} //NodeLayout
