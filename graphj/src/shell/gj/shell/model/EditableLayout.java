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

import gj.geom.Path;
import gj.layout.Layout2D;
import gj.model.Edge;
import gj.model.Vertex;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Our implementation for a view
 */
public class EditableLayout implements Layout2D {

  /**
   * interface implementation
   */
  public Path getPathOfEdge(Edge edge) {
    return ((EditableEdge)edge).getPath();
  }

  /**
   * interface implementation
   */
  public void setPathOfEdge(Edge edge, Path path) {
    ((EditableEdge)edge).setPath(path);
 }

  /**
   * interface implementation
   */
  public Shape getShapeOfVertex(Vertex node) {
    return ((EditableVertex)node).getShape();
  }

  /**
   * interface implementation
   */
  public void setShapeOfVertex(Vertex node, Shape shape) {
    ((EditableVertex)node).setShape(shape);
  }

  /**
   * interface implementation
   */
  public Point2D getPositionOfVertex(Vertex node) {
    return ((EditableVertex)node).getPosition();
  }

  /**
   * interface implementation
   */
  public void setPositionOfVertex(Vertex node, Point2D pos) {
    ((EditableVertex)node).setPosition(pos);
  }

  /**
   * interface implementation
   */
  public void setTransformOfVertex(Vertex vertex, AffineTransform transform) {
    ((EditableVertex)vertex).setTransformation(transform);
  }
  
  /**
   * interface implementation
   */
  public AffineTransform getTransformOfVertex(Vertex vertex) {
    AffineTransform t = ((EditableVertex)vertex).getTransformation();
    return t==null ? new AffineTransform() : t;
  }
} //EditableLayout
