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
package gj.util;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import gj.model.Arc;
import gj.model.Node;

/**
 * Helper for analyzing model.*
 */
public class ModelHelper {
  
  /**
   * Translates a node's position
   */
  public static void move(Node node, Point2D delta) {
    Point2D pos = node.getPosition();
    pos.setLocation( pos.getX() + delta.getX(), pos.getY() + delta.getY());
  }

  /**
   * Checks whether given Node n is neighbour of any of the given nodes. 
   * That is  E node(i), E arc(i,j) where node = node(j)
   */
  public static boolean isNeighbour(Node node, List nodes) {
    Iterator it = node.getArcs().iterator();
    while (it.hasNext()) {
      Arc arc = (Arc)it.next();
      if (nodes.contains(getOther(arc,node))) return true;
    }
    return false;
  }


  /**
   * Calculates the neighbours of given Node n. That is
   *  [i | where E arc(i,n) or arc(n,i) or i==n]
   */
  public static List getNeighbours(Node node) {
    
    // Get ready
    List result = new ArrayList(1+node.getArcs().size());
    
    // add self
    result.add(node);
    
    // add neighbours
    ArcIterator it = new ArcIterator(node);
    while (it.next()) {
      if ((it.isFirst)&&(!it.isLoop)) {
        result.add(getOther(it.arc,node));
      }
    }

    // done
    return result;
  }        
   
  
  /**
   * Calculates the 'other' Node for given node and arc
   * @param arc the arc we're looking at
   * @param node the node that is known to the caller
   * @return arc.getStart() or arc.getEnd() != node
   */
  public static Node getOther(Arc arc, Node node) {
    Node other = arc.getStart();
    if (other==node) {
      other = arc.getEnd();
    }
    return other;
  }

  /**
   * Calculates the dimension of set of nodes
   */
  public static Rectangle2D getBounds(Collection nodes) {
    // no content?
    if (nodes.isEmpty()) return new Rectangle2D.Double(0,0,0,0);
    // loop through nodes and calculate
    double x1=Double.MAX_VALUE,y1=Double.MAX_VALUE,x2=-Double.MAX_VALUE,y2=-Double.MAX_VALUE;
    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      Node node = (Node)it.next();
      Point2D p = node.getPosition();
      Rectangle2D box = node.getShape().getBounds2D();
      x1 = Math.min(x1,p.getX()+box.getMinX());
      y1 = Math.min(y1,p.getY()+box.getMinY());
      x2 = Math.max(x2,p.getX()+box.getMaxX());
      y2 = Math.max(y2,p.getY()+box.getMaxY());
    }
    return new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
  }

}
