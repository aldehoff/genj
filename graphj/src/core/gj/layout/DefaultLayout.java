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
package gj.layout;

import gj.model.Edge;
import gj.model.Vertex;
import gj.util.EdgeLayoutHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple default implementation of a layout
 */
public class DefaultLayout implements Layout2D {
  
  private Shape defaultShape;
  private Map<Vertex, Point2D> vertex2point = new HashMap<Vertex, Point2D>();
  private Map<Vertex, Shape> vertex2shape = new HashMap<Vertex, Shape>();
  private Map<Edge, Shape> edge2shape = new HashMap<Edge, Shape>();

  public DefaultLayout(Shape defaultShape) {
    this.defaultShape = defaultShape;
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

  public Shape getShapeOfEdge(Edge edge) {
    
    Shape result = edge2shape.get(edge);
    if (result==null) {
      result = EdgeLayoutHelper.getShape(edge, this);
      edge2shape.put(edge, result);
    }
    return result;
  }

  public void setShapeOfEdge(Edge edge, Shape shape) {
    edge2shape.put(edge, shape);
  }

  public Shape getShapeOfVertex(Vertex vertex) {
    Shape result = vertex2shape.get(vertex);
    if (result==null)
      result = defaultShape;
    return result;
  }

  public void setShapeOfVertex(Vertex vertex, Shape shape) {
    vertex2shape.put(vertex, shape);
  }

}
