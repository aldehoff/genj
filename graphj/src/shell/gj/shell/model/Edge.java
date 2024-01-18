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

import gj.geom.Geometry;
import gj.util.EdgeLayoutHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 * A default implementation for an Edge
 */
public class Edge extends Element {
  
  /** starting vertex */
  private Vertex start;

  /** ending vertex */
  private Vertex end;
  
  /**
   * Constructor
   */
  /*package*/ Edge(Vertex start, Vertex end, Shape shape) {
    this.start = start;
    this.end = end;
    
    setShape(shape);
  }
  
  /**
   * Check if a point lies at vertex
   */
  public boolean contains(Point2D point) {
    return 8>Geometry.getDistance(point, getShape().getPathIterator(null));
  }
  
  /**
   * overriden - create a default edge shape if necessary
   */
  @Override
  public void setShape(Shape set) {
    if (set==null) 
      set = EdgeLayoutHelper.getShape(start.getPosition(), start.getShape(), end.getPosition(), end.getShape(), 1);
    super.setShape(set);
  }

  /**
   * String represenation
   */
  @Override
  public String toString() {
    return start.toString() + ">" + end.toString();
  }
  
  /**
   * @see Arc#getStart()
   */
  public Vertex getStart() {
    return start;
  }

  /**
   * @see Arc#getEnd()
   */
  public Vertex getEnd() {
    return end;
  }

}
