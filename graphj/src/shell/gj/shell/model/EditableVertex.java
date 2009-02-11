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

import gj.model.Vertex;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A default implementation for a vertex
 */
public class EditableVertex implements Vertex {
  
  /** the content of this vertex */
  private Object content;
  
  /** the position of this vertex */
  private Point2D position;
  
  /** neighbours of this vertex */
  private Set<EditableVertex> neighbours = new LinkedHashSet<EditableVertex>();
  
  /** all edges of this vertex */
  private Set<EditableEdge> edges = new LinkedHashSet<EditableEdge>(3);
  
  /** the shape of this node */
  private Shape shape;
  
  /** the transformed shape of this node */
  private GeneralPath transformedShape;
  
  
  /** transformation */
  private AffineTransform transform = null;

  /**
   * interface implementation
   */
  public Shape getShape() {
    if (transform!=null) {
      if (transformedShape==null) {
        transformedShape = new GeneralPath(shape);
        transformedShape.transform(transform);
      }
      return transformedShape;
    }
    return shape;
  }
  
  /**
   * interface implementation
   */
  public void setShape(Shape set) {
    shape = set!=null ? set : new Rectangle();
    transformedShape = null;
  }
  
  /**
   * Constructor
   */  
  EditableVertex(Point2D position, Shape shape, Object content) {
    
    this.position = position!=null ? position : new Point2D.Double();
    this.content = content;
    
    setShape(shape);
  }
  
  /**
   * Number of neighbours
   */
  public int getNumNeighbours() {
    return neighbours.size();
  }
  
  /**
   * Returns neighbours
   */
  public Set<EditableVertex> getNeighbours() {
    return neighbours;
  }

  /**
   * Check for neighbour
   */
  public boolean isNeighbour(EditableVertex v) {
    return neighbours.contains(v);
  }
  
  /**
   * Adds an edge to given vertex
   */
  /*package*/ EditableEdge addEdge(EditableVertex that) {

    // don't allow duplicates
    if (neighbours.contains(that))
      throw new IllegalArgumentException("already exists edge between "+this+" and "+that);
    if (this.equals(that))
      throw new IllegalArgumentException("can't have edge between self ("+this+")");

    // setup self
    EditableEdge edge = new EditableEdge(this, that);
    this.neighbours.add(that);
    this.edges.add(edge);
    
    // setup other
    that.neighbours.add(this);
    if (that!=this) 
      that.edges.add(edge);
    
    // done
    return edge;
  }
  
  /**
   * Retrieves one edge
   */
  public EditableEdge getEdge(EditableVertex to) {
    for (EditableEdge edge : edges) {
      if (edge.getStart()==this&&edge.getEnd()==to||edge.getStart()==to&&edge.getEnd()==this)
        return edge;
    }
    throw new IllegalArgumentException("no edge between "+this+" and "+to);
  }
  
  /**
   * Retrieves all edges
   */
  /*package*/ Set<EditableEdge> getEdges() {
    return edges;
  }
  
  /**
   * Removes edge from this vertex
   */
  /*package*/ void removeEdge(EditableEdge edge) {
    edges.remove(edge);
    neighbours.remove(edge.getStart());
    neighbours.remove(edge.getEnd());
  }
  
  /**
   * Check if a point lies within vertex
   */
  public boolean contains(Point2D point) {
    return getShape().contains(point.getX()-position.getX(),point.getY()-position.getY());   
  }
  /**
   * interface implementation
   */
  public Point2D getPosition() {
    return position;
  }
  
  /**
   * interface implementation
   */
  public void setPosition(Point2D set) {
    
    position = set;
    
  }
  
  /**
   * interface implementation
   */
  public Object getContent() {
    return content;
  }

  /**
   * accessor - content
   */
  public void setContent(Object set) {
    content = set;
  }

  /**
   * String representation
   */
  @Override
  public String toString() {
    if (content==null) {
      return super.toString();
    } else {
      return content.toString();
    }
  }

  /**
   * accessor - transformation (idempotent)
   */
  public void setTransformation(AffineTransform transform) {
    if (transform!=null&&transform.isIdentity())
      transform=null;
    this.transform = transform;
    transformedShape = null;
  }

} //Vertex
