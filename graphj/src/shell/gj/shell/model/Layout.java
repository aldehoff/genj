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
package gj.shell.model;

import gj.layout.Layout2D;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Our implementation for a view
 */
public class Layout implements Layout2D {

  /** a shape */
  private Shape shape = new Rectangle2D.Float();

  /**
   * interface implementation
   */
  public Shape getShapeOfEdge(Object vertexA, Object vertexB) {
    return ((Vertex)vertexA).getEdge((Vertex)vertexB).getShape();
  }

  /**
   * interface implementation
   */
  public void setShapeOfEdge(Object vertexA, Object vertexB, Shape shape) {
    ((Vertex)vertexA).getEdge((Vertex)vertexB).setShape(shape);
  }

  /**
   * interface implementation
   */
  public Shape getShapeOfVertex(Object node) {
    return ((Vertex)node).getShape();
  }

  /**
   * interface implementation
   */
  public Point2D getPositionOfVertex(Object node) {
    return ((Vertex)node).getPosition();
  }

  /**
   * interface implementation
   */
  public void setPositionOfVertex(Object node, Point2D pos) {
    ((Vertex)node).setPosition(pos);
  }

  /**
   * interface implementation
   */
  public Shape getShapeOfGraph() {
    return shape;
  }

  /**
   * interface implementation
   */
  public void setShapeOfGraph(Shape set) {
    shape = set;
  }

} //View
