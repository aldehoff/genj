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

import gj.model.DirectedGraph;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A default impl for 
 * @see gj.layout.Layout2D
 */
public class EditableGraph implements DirectedGraph {
  
  /** current selection - either edge or vertex*/
  private EditableElement selection;
  
  /** the contained nodes */
  protected Set<EditableVertex> vertices = new HashSet<EditableVertex>(10);

  /** the contained arcs */
  private Collection<EditableEdge> edges = new HashSet<EditableEdge>(10);
  
  /**
   * Constructor
   */
  public EditableGraph() {
  }

  /**
   * Constructor
   */
  public EditableGraph(EditableGraph other) {
    this.vertices.addAll(other.vertices);
    this.edges.addAll(other.edges);
  }
  
  /**
   * Validate - good for subclasses like tree
   */
  public void validate() {
  }

  /**
   * add an edge
   */
  public EditableEdge addEdge(EditableVertex from, EditableVertex to, Shape shape) {
    if (from==to)
      throw new IllegalArgumentException("no edges between self allowed");
    EditableEdge edge = from.addEdge(to, shape);
    edges.add(edge);
    return edge;
  }
  
  /**
   * All Edges
   */
  public Collection<EditableEdge> getEdges() {
    return edges;
  }
  
  /**
   * remove an edge
   */
  public void removeEdge(EditableEdge edge) {
    if (!edges.remove(edge))
      throw new IllegalArgumentException("remove on non-graph edge");
    EditableVertex
     start = edge.getStart(),
     end   = edge.getEnd();
    start.removeEdge(edge);
    end  .removeEdge(edge);
    edges.remove(edge);
  }

  /**
   * add a vertex
   */
  public EditableVertex addVertex(Point2D position, Shape shape, Object content) {
    EditableVertex node = new EditableVertex(position, shape, content);
    vertices.add(node);
    return node;
  }

  /**
   * removes a vertex
   */
  public void removeVertex(EditableVertex node) {
    
    for (EditableEdge edge : new ArrayList<EditableEdge>(node.getEdges()) )
      removeEdge(edge);
      
    vertices.remove(node);
  }
  
  /**
   * Access - current selection
   */
  public void setSelection(EditableElement set) {
    selection = set;
  }
  
  /**
   * Access - current selection
   */
  public EditableElement getSelection() {
    return selection;
  }
  
  /**
   * find a vertex or edge by position
   */
  public EditableElement getElement(Point2D point) {
    
    // look through vertices
    EditableElement result = getVertex(point);
    if (result!=null)
      return result;

    // look through edges
    result = getEdge(point);
    if (result!=null)
      return result;
    
    // not found
    return null;
    
  }
  
  /**
   * Get Edge by position
   */
  public EditableEdge getEdge(Point2D point) {
    
    EditableEdge result = null;

    for (EditableEdge edge : edges) {
      // check an edge
      if (edge.contains(point))
        result = edge;
      else
        if (result!=null) break;
    }
    
    // not found
    return result;
  }
  
  
  /**
   * find a node by position
   */
  public EditableVertex getVertex(Point2D point) {

    // look through nodes
    Iterator<?> it = vertices.iterator();
    while (it.hasNext()) {
      
      // check a node
      EditableVertex node = (EditableVertex)it.next();
      if (node.contains(point)) 
        return node;
    }
    
    // not found
    return null;
  }
  
  public Collection<?> getVerticesOfEdge(Object edge) {
    List<EditableVertex> result = new ArrayList<EditableVertex>(2);
    result.add( ((EditableEdge)edge).getStart() );
    result.add( ((EditableEdge)edge).getEnd() );
    return result;
  }
  
  /**
   * 
   */
  public int getDirectionOfEdge(Object from, Object to) {
    return ((EditableVertex)from).getEdge((EditableVertex)to).getStart()==from ? 1 : -1;
  }
  
  /**
   * interface implementation
   */
  public int getNumVertices() {
    return vertices.size();
  }
  
  /**
   * interface implementation
   */
  public Set<EditableVertex> getVertices() {
    return vertices;
  }
  
  /**
   * interface implementation
   */
  public int getNumAdjacentVertices(Object vertex) {
    return ((EditableVertex)vertex).getNumNeighbours();
  }
  
  /**
   * interface implementation
   */
  public Set<EditableVertex> getNeighbours(Object vertex) {
    return ((EditableVertex)vertex).getNeighbours();
  }
  
  /**
   * interface implementation
   */
  public int getNumDirectPredecessors(Object vertex) {
    int result = 0;
    for (EditableEdge edge : ((EditableVertex)vertex).getEdges()) {
      if (edge.getEnd() == vertex) result++;
    }  
    return result;
  }
  
  /**
   * interface implementation
   */
  public Iterable<EditableVertex> getDirectPredecessors(Object vertex) {
    // FIXME this could be in-situ without temporary array
    List<EditableVertex> predecessors = new ArrayList<EditableVertex>();
    for (EditableEdge edge : ((EditableVertex)vertex).getEdges()) {
      if (edge.getEnd() == vertex)
        predecessors.add(edge.getStart());
    }  
    return predecessors;
  }
  
  /**
   * interface implementation
   */
  public int getNumDirectSuccessors(Object vertex) {
    int result = 0;
    for (EditableEdge edge : ((EditableVertex)vertex).getEdges()) {
      if (edge.getStart() == vertex) result++;
    }  
    return result;
  }
  
  /**
   * interface implementation
   */
  public Iterable<?> getDirectSuccessors(Object vertex) {
    // FIXME this could be in-situ without temporary array
    List<EditableVertex> successors = new ArrayList<EditableVertex>();
    for (EditableEdge edge : ((EditableVertex)vertex).getEdges()) {
      if (edge.getStart() == vertex)
        successors.add(edge.getEnd());
    }  
    return successors;
  }
  
} //DefaultGraph