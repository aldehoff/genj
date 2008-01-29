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
import gj.model.Edge;
import gj.util.EdgeLayoutHelper;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * A default implementation for an Edge
 */
public class EditableEdge extends EditableElement implements Edge {
  
  /** starting vertex */
  private EditableVertex start;

  /** ending vertex */
  private EditableVertex end;
  
  private long hash = -1;
  
  /**
   * Constructor
   */
  EditableEdge(EditableVertex start, EditableVertex end, Shape shape) {
    this.start = start;
    this.end = end;
    
    setShape(shape);
  }
  
  /**
   * Check if a point lies at vertex
   */
  public boolean contains(Point2D point) {
    Point2D origin = start.getPosition();
    return 8>Geometry.getMinimumDistance(point, getShape().getPathIterator(AffineTransform.getTranslateInstance(origin.getX(), origin.getY())));
  }
  
  /**
   * overriden - create a default edge shape if necessary
   */
  @Override
  public Shape getShape() {
    
    if (!updateHash())
      setShape(makeShape());
    
    return super.getShape();
  }
  
  private Shape makeShape() {
    return EdgeLayoutHelper.getShape(start.getPosition(), start.getShape(), end.getPosition(), end.getShape());   
  }
  
  @Override
  public void setShape(Shape set) {
    if (set==null)
      set = makeShape();
    super.setShape(set);
    updateHash();
  }
  
  boolean updateHash() {
    long oldHash = hash;
    hash = Geometry.getDelta(getEnd().getPosition(), getStart().getPosition()).hashCode()
     + getStart().getShape().hashCode() + getEnd().getShape().hashCode();
    return oldHash==hash;
  }

  /**
   * String represenation
   */
  @Override
  public String toString() {
    return start.toString() + ">" + end.toString();
  }
  
  /**
   * the start of the edge
   */
  public EditableVertex getStart() {
    return start;
  }

  /**
   * the end of the edge
   */
  public EditableVertex getEnd() {
    return end;
  }

}
