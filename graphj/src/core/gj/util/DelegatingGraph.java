/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2009 Nils Meier
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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;

import gj.geom.Path;
import gj.layout.Graph2D;
import gj.layout.Port;
import gj.model.Edge;
import gj.model.Vertex;

/**
 * A delegating graph2d
 */
public class DelegatingGraph implements Graph2D {
  
  private Graph2D delegated;
  
  /** constructor */
  public DelegatingGraph(Graph2D delegated) {
    this.delegated = delegated;
  }

  /** delegating call */
  public Path getPathOfEdge(Edge edge) {
    return delegated.getPathOfEdge(edge);
  }

  /** delegating call */
  public Point2D getPositionOfVertex(Vertex vertex) {
    return delegated.getPositionOfVertex(vertex);
  }

  /** delegating call */
  public Shape getShapeOfVertex(Vertex vertex) {
    return delegated.getShapeOfVertex(vertex);
  }

  /** delegating call */
  public AffineTransform getTransformOfVertex(Vertex vertex) {
    return delegated.getTransformOfVertex(vertex);
  }

  /** delegating call */
  public void setPathOfEdge(Edge edge, Path shape) {
    delegated.setPathOfEdge(edge, shape);
  }

  /** delegating call */
  public void setPositionOfVertex(Vertex vertex, Point2D pos) {
    delegated.setPositionOfVertex(vertex, pos);
  }

  /** delegating call */
  public void setTransformOfVertex(Vertex vertex, AffineTransform transform) {
    delegated.setTransformOfVertex(vertex, transform);
  }

  /** delegating call */
  public Collection<? extends Edge> getEdges() {
    return delegated.getEdges();
  }

  /** delegating call */
  public Collection<? extends Vertex> getVertices() {
    return delegated.getVertices();
  }
  
  /** delegating call */
  public Port getPort(Vertex vertex, Edge edge) {
    return delegated.getPort(vertex, edge);
  }
  
  
} //DelegatingGraph2D
