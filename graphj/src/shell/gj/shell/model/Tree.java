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

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Implementation for tree - we wrap a graph and test for cycles
 * and spanning
 */
public class Tree extends Graph implements gj.model.Tree {
  
  private final static Stroke DASH = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{ 2, 3}, 0.0f);
  
  /** root */
  private Vertex root;
  
  /**
   * Constructor
   */
  public Tree(Graph graph) {
    super(graph);    
    validate();
    // done
  }
  
  /**
   * Override addEdge to enforce tree invariants
   */
  @Override
  public Edge addEdge(Vertex from, Vertex to, Shape shape) {
    // TODO enfore no circles
    return super.addEdge(from, to, shape);
  }
  
  /**
   * Override adding vertex to enforce tree invariants
   */
  @Override
  public Vertex addVertex(Point2D position, Shape shape, Object content) {
    // TODO enforce spanning
    return super.addVertex(position, shape, content);
  }
  
  /**
   * Provide children of a parent
   */
  public List<?> getChildren(Object parent) {
    // TODO calculate children
    throw new UnsupportedOperationException("getChildren not implemented");
  }
  
  /**
   * Provide parent of a child
   */
  public Object getParent(Object child) {
    // TODO calculate parent
    throw new UnsupportedOperationException("getParent not implemented");
  }
  
  /**
   * Validate
   */
  @Override
  public void validate() {
    
    // empty is fine
    if (getNumVertices() == 0)
      return;

    // need root
    Vertex root = getRootOfTree();
    if (root==null)
      throw new IllegalArgumentException("graph doesn't contain root of tree");

    // look for cycles
    Set<Vertex> visited = new HashSet<Vertex>();
    if (cycleCheck(null, root, visited))
    	throw new IllegalArgumentException("graph is not acyclic");
    
    // check spanning
    if (visited.size() != getNumVertices())
      throw new IllegalArgumentException("graph is not a spanning tree");
    
  }
  
  /**
   * Find cycles
   */
  private boolean cycleCheck(Vertex from, Vertex to, Set<Vertex> visited) {
    
    // to shouldn't have been visited before
    if (visited.contains(to)) 
      return true;

    // remember it
    visited.add(to);
    
    // Recurse into neighbours
    for (Vertex neighbour : to.getNeighbours()) {
      if (neighbour==from) 
        continue;
      if (cycleCheck(to, neighbour, visited))
        return true;
    }
    
    // done
    return false;
  }
  
  /** 
   * overriden - provide special stroke for root vertex
   */
  @Override
  protected Stroke getStroke(Vertex vertex) {
    if (vertex!=getRootOfTree())
      return super.getStroke(vertex);
    return DASH;
  }

  /**
   * implementation
   */
  public Object getRoot() {
    if (root==null&&!vertices.isEmpty())
      root = (Vertex)vertices.iterator().next();
    return root;
  }
  
  /**
   * Accessor - root
   */
  public Vertex getRootOfTree() {
    return (Vertex)getRoot();
  }
  
  /**
   * Accessor - root
   */
  public void setRootOfTree(Vertex vertex) {
    root = vertex;
  }
  
} //Tree
