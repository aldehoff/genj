/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2002-2004 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.util;

import gj.layout.Layout2D;
import gj.model.Graph;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Helper for analyzing model.*
 */
public class ModelHelper {
  
  /**
   * Sets a node's position
   */
  public static void setPosition(Graph graph, Layout2D layout, Object node, double x, double y) {
    Point2D pos = layout.getPositionOfVertex(graph, node);
    pos.setLocation( x, y);
    layout.setPositionOfVertex(graph, node, pos);
  }
  
  /**
   * Translates a node's position
   */
  public static void translate(Graph graph, Layout2D layout, Object node, Point2D delta) {
    Point2D pos = layout.getPositionOfVertex(graph, node);
    pos.setLocation( pos.getX() + delta.getX(), pos.getY() + delta.getY());
    layout.setPositionOfVertex(graph, node, pos);
  }

  /**
   * Checks whether given Node n is neighbour of any of the given nodes. 
   * That is  E node(i), E arc(i,j) where node = node(j)
   */
  public static boolean isNeighbour(Graph graph, Object node, List<?> nodes) {
    Iterator<?> neighbours = graph.getNeighbours(node).iterator();
    while (neighbours.hasNext()) {
      if (nodes.contains(neighbours.next()))
        return true;
    }
    return false;
  }

  /**
   * Calculate a list from given iterable
   */
  public static <T> List<T> toList(Iterable<T> ts) {
    List<T> result = new ArrayList<T>();
    for (T t : ts)
      result.add(t);
    return result;
  }
  
  public static <T> void removeAll(Collection<T> c, Iterable<T> ts) {
    for (T t : ts) 
      c.remove(t);
  }
  
  public static boolean contains(Iterable<?> ts, Object t) {
    for (Object i : ts) {
      if (i.equals(t))
        return true;
    }
    return false;
  }

  /**
   * Calculates the dimension of set of nodes
   */
  public static Rectangle2D getBounds(Graph graph, Layout2D layout) {
    // no content?
    if (graph==null||graph.getVertices().isEmpty()) 
      return new Rectangle2D.Double(0,0,0,0);
    // loop through nodes and calculate
    double x1=Double.MAX_VALUE,y1=Double.MAX_VALUE,x2=-Double.MAX_VALUE,y2=-Double.MAX_VALUE;
    Iterator<?> it = graph.getVertices().iterator();
    while (it.hasNext()) {
      Object node = it.next();
      Point2D p = layout.getPositionOfVertex(graph, node);
      Rectangle2D box = layout.getShapeOfVertex(graph, node).getBounds2D();
      x1 = Math.min(x1,p.getX()+box.getMinX());
      y1 = Math.min(y1,p.getY()+box.getMinY());
      x2 = Math.max(x2,p.getX()+box.getMaxX());
      y2 = Math.max(y2,p.getY()+box.getMaxY());
    }
    return new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
  }
  

} //ModelHelper
