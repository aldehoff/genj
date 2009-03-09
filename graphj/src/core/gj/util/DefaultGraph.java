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

import gj.geom.Path;
import gj.layout.Graph2D;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple default implementation of a layout
 */
public class DefaultGraph implements Graph2D {

  private Graph graph;
  private Shape defaultShape;
  private Map<Vertex, Point2D> vertex2point = new HashMap<Vertex, Point2D>();
  private Map<Vertex, AffineTransform> vertex2transform = new HashMap<Vertex, AffineTransform>();
  private Map<Vertex, Shape> vertex2shape = new HashMap<Vertex, Shape>();
  private Map<Edge, Path> edge2path = new HashMap<Edge, Path>();
  
  /*package*/ DefaultGraph() {
    this(null);
  }
  
  public DefaultGraph(Graph graph) {
    this(graph, new Rectangle());
  }
  
  public DefaultGraph(Graph graph, Shape defaultShape) {
    this.graph = graph;
    this.defaultShape = defaultShape;
  }
  
  public Collection<? extends Edge> getEdges() {
    return graph==null ? new ArrayList<Edge>() : graph.getEdges();
  }
  
  public Collection<? extends Vertex> getVertices() {
    return graph==null ? new ArrayList<Vertex>() : graph.getVertices();
  }
  
  protected Shape getDefaultShape() {
    return defaultShape;
  }
  
  public Point2D getPositionOfVertex(Vertex vertex) {
    Point2D result = vertex2point.get(vertex);
    if (result==null) 
      result = new Point2D.Double();
    return result;
  }

  public void setPositionOfVertex(Vertex vertex, Point2D pos) {
    vertex2point.put(vertex, new Point2D.Double(pos.getX(), pos.getY()));
  }

  public Path getPathOfEdge(Edge edge) {
    
    Path result = edge2path.get(edge);
    if (result==null) {
      result = LayoutHelper.getPath(edge, this);
      edge2path.put(edge, result);
    }
    return result;
  }

  public void setPathOfEdge(Edge edge, Path path) {
    edge2path.put(edge, path);
  }

  public Shape getShapeOfVertex(Vertex vertex) {
    Shape result = vertex2shape.get(vertex);
    if (result==null)
      result = defaultShape;
    AffineTransform transform = vertex2transform.get(vertex);
    if (transform!=null&&!transform.isIdentity()) {
      GeneralPath gp = new GeneralPath(result);
      gp.transform(transform);
      result = gp;
    }
    return result;
  }

  public void setShapeOfVertex(Vertex vertex, Shape shape) {
    vertex2shape.put(vertex, shape);
  }

  public void setTransformOfVertex(Vertex vertex, AffineTransform transform) {
    if (transform==null)
      vertex2transform.remove(vertex);
    else
      vertex2transform.put(vertex, transform);
  }

  public AffineTransform getTransformOfVertex(Vertex vertex) {
    AffineTransform t = vertex2transform.get(vertex);
    return t==null ? new AffineTransform() : t;
  }
}
