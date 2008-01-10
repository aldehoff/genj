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
import java.util.HashSet;
import java.util.Set;


/**
 * Implementation for tree - we wrap a graph and test for cycles
 * and spanning
 */
public class EditableTree extends EditableGraph implements gj.model.Tree {
  
  /** root */
  private EditableVertex root;
  
  /**
   * Constructor
   */
  public EditableTree(EditableGraph graph) {
    super(graph);    
    validate();
    // done
  }
  
  /**
   * Override addEdge to enforce tree invariants
   */
  @Override
  public EditableEdge addEdge(EditableVertex from, EditableVertex to, Shape shape) {
    // TODO enfore no circles
    return super.addEdge(from, to, shape);
  }
  
  /**
   * Override adding vertex to enforce tree invariants
   */
  @Override
  public EditableVertex addVertex(Point2D position, Shape shape, Object content) {
    // TODO enforce spanning
    return super.addVertex(position, shape, content);
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
    EditableVertex root = getRootOfTree();
    if (root==null)
      throw new IllegalArgumentException("graph doesn't contain root of tree");

    // look for cycles
    Set<EditableVertex> visited = new HashSet<EditableVertex>();
    if (cycleCheck(null, root, visited))
    	throw new IllegalArgumentException("graph is not acyclic");
    
    // check spanning
    if (visited.size() != getNumVertices())
      throw new IllegalArgumentException("graph is not a spanning tree");
    
  }
  
  /**
   * Find cycles
   */
  private boolean cycleCheck(EditableVertex from, EditableVertex to, Set<EditableVertex> visited) {
    
    // to shouldn't have been visited before
    if (visited.contains(to)) 
      return true;

    // remember it
    visited.add(to);
    
    // Recurse into neighbours
    for (EditableVertex neighbour : to.getNeighbours()) {
      if (neighbour==from) 
        continue;
      if (cycleCheck(to, neighbour, visited))
        return true;
    }
    
    // done
    return false;
  }
  
  /**
   * implementation
   */
  public Object getRoot() {
    if (root==null&&!vertices.isEmpty())
      root = (EditableVertex)vertices.iterator().next();
    return root;
  }
  
  /**
   * Accessor - root
   */
  public EditableVertex getRootOfTree() {
    return (EditableVertex)getRoot();
  }
  
  /**
   * Accessor - root
   */
  public void setRootOfTree(EditableVertex vertex) {
    root = vertex;
  }
  
} //Tree
