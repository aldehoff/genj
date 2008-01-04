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
  
  /** neighbours of this vertex */
  private Set<Vertex> successors = new HashSet<Vertex>();
  private Set<Vertex> predecessors = new HashSet<Vertex>();
  
  /** all edges of this vertex */
  private List<Edge> edges = new ArrayList<Edge>(3);
  
  /**
   * Constructor
   */  
  /*package*/ Vertex(Point2D position, Shape shape, Object content) {
    
    this.position = position!=null ? position : new Point2D.Double();
    this.content = content;
    
    setShape(shape);
  }
  
  /**
   * Number of neighbours
   */
  public int getNumNeighbours() {
    return successors.size() + predecessors.size();
  }
  
  /**
   * Returns neighbours
   */
  public List<Vertex> getNeighbours() {
    // FIXME this could be in-situ without temporary array
    List<Vertex> result = new ArrayList<Vertex>();
    result.addAll(successors);
    result.addAll(predecessors);
    return result;
  }

  /**
   * Check for neighbour
   */
  public boolean isNeighbour(Vertex v) {
    return successors.contains(v) || predecessors.contains(v);
  }
  
  /**
   * Adds an edge to given vertex
   */
  /*package*/ Edge addEdge(Vertex that, Shape shape) {

    // don't allow duplicates
    if (predecessors.contains(that) || successors.contains(that))
      throw new IllegalArgumentException("already exists edge between "+this+" and "+that);

    // setup self
    Edge edge = new Edge(this, that, shape);
    this.successors.add(that);
    this.edges.add(edge);
    
    // setup other
    that.predecessors.add(this);
    if (that!=this) 
      that.edges.add(edge);
    
    // done
    return edge;
  }
  
  /**
   * Retrieves one edge
   */
  public Edge getEdge(Vertex to) {
    for (Edge edge : edges) {
      if (edge.getStart()==this&&edge.getEnd()==to||edge.getStart()==to&&edge.getEnd()==this)
        return edge;
    }
    throw new IllegalArgumentException("no edge between "+this+" and "+to);
  }
  
  /**
   * Retrieves all edges
   */
  /*package*/ Collection<Edge> getEdges() {
    return edges;
  }
  
  /**
   * Removes edge from this vertex
   */
  /*package*/ void removeEdge(Edge edge, Vertex to) {
    edges.remove(edge);
    successors.remove(to);
  }
  
  /**
   * Reset edges
   */
  /*package*/ void resetEdges() {

    for (Edge edge : edges) 
      edge.setShape(null);
  }
  
  /**
   * overriden - shape
   */
  @Override
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
  @Override
  public String toString() {
    if (content==null) {
      return super.toString();
    } else {
      return content.toString();
    }
  }

} //Vertex
