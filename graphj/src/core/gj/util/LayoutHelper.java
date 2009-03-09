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

import static gj.geom.Geometry.*;
import gj.geom.Path;
import gj.layout.GraphNotSupportedException;
import gj.layout.Graph2D;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper for analyzing model.*
 */
public class LayoutHelper {
  
  /**
   * Translates a node's position
   */
  public static void translate(Graph2D layout, Vertex vertex, Point2D delta) {
    Point2D pos = layout.getPositionOfVertex(vertex);
    layout.setPositionOfVertex(vertex, new Point2D.Double( pos.getX() + delta.getX(), pos.getY() + delta.getY() ));
  }

  /**
   * Checks whether given Node n is neighbour of any of the given nodes. 
   * That is  E node(i), E arc(i,j) where node = node(j)
   */
  public static boolean isNeighbour(Graph graph, Vertex vertex, Collection<? extends Vertex> vertices) {
    for (Edge edge : vertex.getEdges()) {
      if (vertices.contains(edge.getStart()) || vertices.contains(edge.getEnd()) )
        return true;
    }
    return false;
  }

  /**
   * Calculates the dimension of set of nodes
   */
  public static Rectangle2D getBounds(Graph2D graph2d) {
    // no content?
    if (graph2d==null||graph2d.getVertices().size()==0) 
      return new Rectangle2D.Double(0,0,0,0);
    // loop through nodes and calculate
    double x1=Double.MAX_VALUE,y1=Double.MAX_VALUE,x2=-Double.MAX_VALUE,y2=-Double.MAX_VALUE;
    for (Vertex vertex : graph2d.getVertices()) {
      Point2D p = graph2d.getPositionOfVertex(vertex);
      Rectangle2D box = graph2d.getShapeOfVertex(vertex).getBounds2D();
      x1 = Math.min(x1,p.getX()+box.getMinX());
      y1 = Math.min(y1,p.getY()+box.getMinY());
      x2 = Math.max(x2,p.getX()+box.getMaxX());
      y2 = Math.max(y2,p.getY()+box.getMaxY());
    }
    return new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
  }
 
  /**
   * Check graph for spanning tree
   */
  public static void assertSpanningTree(Graph graph) throws GraphNotSupportedException {
    
    if (graph.getVertices().size()==0)
      return;
    
    // look for cycles
    Set<Vertex> visited = new HashSet<Vertex>();
    if (containsCycle(graph, null, graph.getVertices().iterator().next(), visited))
      throw new GraphNotSupportedException("graph is not acyclic");
    
    // check spanning
    if (visited.size() != graph.getVertices().size())
      throw new GraphNotSupportedException("graph is not a spanning tree");
    
  }

  /**
   * Find cycles
   */
  private static boolean containsCycle(Graph graph, Vertex backtrack, Vertex root, Set<Vertex> visited) {
    
    // to shouldn't have been visited before
    if (visited.contains(root)) 
      return true;
  
    // remember it
    visited.add(root);
    
    // Recurse into neighbours
    for (Vertex neighbour : getNeighbours(root)) {
      if (neighbour.equals(backtrack)) 
        continue;
      if (containsCycle(graph, root, neighbour, visited))
        return true;
    }
    
    // done
    return false;
  }
  
  /**
   * Get other vertex in an edge
   */
  public static Vertex getOther(Edge edge, Vertex vertex) {
    if (edge.getStart().equals(vertex))
      return edge.getEnd();
    if (edge.getEnd().equals(vertex))
      return edge.getStart();
    throw new IllegalArgumentException("vertex "+vertex+" not in "+edge);
  }
  
  /**
   * get normalized edges. That's all edges from a given vertex without loops and dupes
   */
  public static List<Edge> getNormalizedEdges(Vertex vertex) {
    
    Set<Vertex> children = new HashSet<Vertex>();
    List<Edge> edges = new ArrayList<Edge>(vertex.getEdges().size());
    for (Edge edge : vertex.getEdges()) {
      Vertex child = LayoutHelper.getOther(edge, vertex);
      if (children.contains(child)||child.equals(vertex))
        continue;
      children.add(child);
      edges.add(edge);
    }

    return edges;
  }
  
  /**
   * Check vertex being sink
   */
  public static boolean isSink(Vertex vertex) {
    for (Edge edge : vertex.getEdges()) {
      if (edge.getStart().equals(vertex))
        return false;
    }
    return true;
  }
  
  /**
   * Get neighbouring vertices. That's all 
   * <pre>
   *   A e E n : e(vertex,n ) || e(n,vertex) e graph && !n==vertex
   * </pre> 
   */
  public static Set<Vertex> getNeighbours(Vertex vertex) {
    Set<Vertex> result = new LinkedHashSet<Vertex>();
    for (Edge edge : vertex.getEdges()) {
      Vertex start = edge.getStart();
      Vertex end = edge.getEnd();
      if (start.equals(vertex)&&!end.equals(vertex)) 
        result.add(end);
      else if (!start.equals(vertex))
        result.add(start);
    }
    return result;
  }
  
  /**
   * Get children of a given vertex. That's all 
   * <pre>
   *   A e E n : e(vertex,n ) e graph && !n==vertex
   * </pre> 
   */
  public static Set<Vertex> getChildren(Vertex vertex) {
    Set<Vertex> result = new LinkedHashSet<Vertex>();
    for (Edge edge : vertex.getEdges()) {
      Vertex start = edge.getStart();
      Vertex end = edge.getEnd();
      if (start.equals(vertex)&&!end.equals(vertex)) 
        result.add(end);
    }
    return result;
  }
  
  public static boolean contains(Edge edge, Vertex vertex) {
    return edge.getStart().equals(vertex) || edge.getEnd().equals(vertex);
  }
  
  public static double getDiameter(Vertex vertex, Graph2D layout) {
    return getMaximumDistance(new Point2D.Double(0,0), layout.getShapeOfVertex(vertex)) * 2;
  }

  /**
   * Calculate shape of all arcs in graph
   */
  public static void setPaths(Graph2D graph2d) {
    
    for (Edge edge : graph2d.getEdges()) { 
      setPath(edge, graph2d);
    }
    
  }

  /**
   * Calculate a shape for an arc
   */
  public static void setPath(Edge edge, Graph2D graph2d) {
    graph2d.setPathOfEdge(edge, getPath(edge, graph2d));
  }
  
  /**
   * path for given points
   */
  public static Path getPath(List<Point2D> points) {
    return getPath(points, null, null, false);
  }

  /**
   * path for given edge
   */
  public static Path getPath(Edge edge, Graph2D graph2d) {
    return getPath(
        graph2d.getPositionOfVertex(edge.getStart()),
        graph2d.getShapeOfVertex(edge.getStart()),
        graph2d.getPositionOfVertex(edge.getEnd()),
        graph2d.getShapeOfVertex(edge.getEnd()));
  }

  /**
   * path with a line going through points between two shapes
   * @param points a sequence of points describing the path (first point is the origin of the shape)
   * @param s1 shape positioned at the first point
   * @param s2 shape positioned at the last point
   */  
  public static Path getPath(List<Point2D> points, Shape s1, Shape s2, boolean reversed) {
    return getPath(points.toArray(new Point2D[points.size()]),s1,s2,reversed);
  }
  
  public static Path getPath(Point2D[] points, Shape s1, Shape s2, boolean reversed) {
    
    int n = points.length;
    
    // A simple line through points
    Path result = new Path();
    
    // intersect the first segment with s1
    Point2D
      a = s1!=null ? getVectorEnd(points[1], points[0], points[0], s1) : points[0],
      b = s2!=null ? getVectorEnd(points[n-2], points[n-1], points[n-1], s2) : points[n-1];
    
    // add the points to this path relative to start
    if (!reversed) {
      double cx = points[0].getX(), cy = points[0].getY();
      result.start(new Point2D.Double( a.getX() - cx, a.getY() - cy));
      for (int i=1;i<n-1;i++) 
        result.lineTo( new Point2D.Double( points[i].getX() - cx, points[i].getY() - cy ));
      result.lineTo(new Point2D.Double( b.getX() - cx, b.getY() - cy));
    } else {
      double cx = points[n-1].getX(), cy = points[n-1].getY();
      result.start(new Point2D.Double( b.getX() - cx, b.getY() - cy));
      for (int i=n-2;i>0;i--) 
        result.lineTo( new Point2D.Double( points[i].getX() - cx, points[i].getY() - cy ));
      result.lineTo(new Point2D.Double( a.getX() - cx, a.getY() - cy));
    }
    
    // done
    return result;
  }

  /**
   * Creates a connection between given points between two shapes
   * @param p1 the starting point (origin of shape)
   * @param s1 the shape sitting at p1
   * @param p2 the ending point
   * @param s2 the shape sitting at p2
   */
  public static Path getPath(Point2D p1, Shape s1, Point2D p2, Shape s2) {
  
    Point2D 
    	a = getVectorEnd(p2, p1, p1, s1),
    	b = getVectorEnd(p1, p2, p2, s2);
    
    // A simple line
    Path result = new Path();
    result.start(new Point2D.Double(a.getX()-p1.getX(), a.getY()-p1.getY()));
    result.lineTo(new Point2D.Double(b.getX()-p1.getX(), b.getY()-p1.getY()));
    
    // done
    return result; 
  }

} //ModelHelper
