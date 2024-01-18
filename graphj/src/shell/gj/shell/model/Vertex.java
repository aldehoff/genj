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

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A default implementation for a vertex
 */
public class Vertex extends Element {

  /** the content of this vertex */
  private Object content;
  
  /** the position of this vertex */
  private Point2D position;
  
  /** all neighbours of this vertex */
  private Set neighbours = new HashSet();
  
  /** all edges of this vertex */
  private List edges = new ArrayList(3);
  
  /**
   * Constructor
   */  
  /*package*/ Vertex(Point2D position, Shape shape, Object content) {
    
    this.position = position!=null ? position : new Point2D.Double();
    this.content = content;
    
    setShape(shape);
  }
  
  /**
   * Returns neighbours
   */
  public Set getNeighbours() {
    return neighbours;
  }
  
  /**
   * Adds an edge to given vertex
   */
  /*package*/ Edge addEdge(Vertex that, Shape shape) {

    // don't allow duplicates
    if (neighbours.contains(that))
      throw new IllegalArgumentException("already exists edge between from and to");

    // setup self
    Edge edge = new Edge(this, that, shape);
    this.neighbours.add(that);
    this.edges.add(edge);
    
    // setup other
    if (that!=this) {
      that.neighbours.add(this);
      that.edges.add(edge);
    }
    
    // done
    return edge;
  }
  
  /**
   * Retrieves one edge
   */
  public Edge getEdge(Vertex to) {
    Iterator it = edges.iterator();
    while (it.hasNext()) {
      Edge edge = (Edge)it.next();
      if (edge.getStart()==this&&edge.getEnd()==to||edge.getStart()==to&&edge.getEnd()==this)
        return edge;
    }
    throw new IllegalArgumentException("no edge between "+this+" and "+to);
  }
  
  /**
   * Retrieves all edges
   */
  /*package*/ Collection getEdges() {
    return edges;
  }
  
  /**
   * Removes edge from this vertex
   */
  /*package*/ void removeEdge(Edge edge, Vertex to) {
    edges.remove(edge);
    neighbours.remove(to);
  }
  
  /**
   * Reset edges
   */
  /*package*/ void resetEdges() {
    
    Iterator it = edges.iterator();
    while (it.hasNext()) 
      ((Edge)it.next()).setShape(null);
  }
  
  /**
   * overriden - shape
   */
  public void setShape(Shape set) {
    // check for null
    if (set==null)
      set = new Rectangle2D.Float();
    // continue
    super.setShape(set);
    // reset edges
    resetEdges();
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
    
    // update edges
    resetEdges();
  }
  
  /**
   * interface implementation
   */
  public Object getContent() {
    return content;
  }

  /**
   * interface implementation
   */
  public void setContent(Object set) {
    content = set;
  }

  /**
   * String representation
   */
  public String toString() {
    if (content==null) {
      return super.toString();
    } else {
      return content.toString();
    }
  }
  
} //Vertex
