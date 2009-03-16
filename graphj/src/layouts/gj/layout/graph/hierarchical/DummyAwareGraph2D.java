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
package gj.layout.graph.hierarchical;

import gj.geom.Path;
import gj.layout.Graph2D;
import gj.layout.Port;
import gj.model.Edge;
import gj.model.Vertex;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A layout that knows how to handle layer dummy vertices
 */
public class DummyAwareGraph2D implements Graph2D {
  
  private Map<Vertex, Point2D> dummy2pos = new HashMap<Vertex, Point2D>();
  private Graph2D wrapped;
  
  public DummyAwareGraph2D(Graph2D wrapped) {
    this.wrapped = wrapped;
  }
  
  public Collection<? extends Vertex> getVertices() {
    return wrapped.getVertices();
  }
  
  public Collection<? extends Edge> getEdges() {
    return wrapped.getEdges();
  }
  
  public Point2D getPositionOfVertex(Vertex vertex) {
    if (!(vertex instanceof LayerAssignment.DummyVertex))
      return wrapped.getPositionOfVertex(vertex);
    Point2D result = dummy2pos.get(vertex);
    return result!=null ? result : new Point2D.Double();
  }
  
  public void setPositionOfVertex(Vertex vertex, Point2D pos) {
    if (!(vertex instanceof LayerAssignment.DummyVertex))
      wrapped.setPositionOfVertex(vertex, pos);
    else
      dummy2pos.put(vertex, pos);
  }
  
  public Shape getShapeOfVertex(Vertex vertex) {
    if (!(vertex instanceof LayerAssignment.DummyVertex))
      return wrapped.getShapeOfVertex(vertex);
    return new Rectangle2D.Double();
  }
  
  public Path getPathOfEdge(Edge edge) {
    return wrapped.getPathOfEdge(edge);
  }

  public AffineTransform getTransformOfVertex(Vertex vertex) {
    return wrapped.getTransformOfVertex(vertex);
  }

  public void setPathOfEdge(Edge edge, Path shape) {
    wrapped.setPathOfEdge(edge, shape);
  }

  public void setTransformOfVertex(Vertex vertex, AffineTransform transform) {
    if (!(vertex instanceof LayerAssignment.DummyVertex))
      wrapped.setTransformOfVertex(vertex, transform);
  }

  public Port getPort(Vertex vertex, Edge edge) {
    return wrapped.getPort(vertex, edge);
  }

} //DummyAwareGraph2D
