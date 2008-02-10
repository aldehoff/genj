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
package gj.model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract tree for easily wrapping tree-like information
 */
public abstract class AbstractTree<V extends Vertex> implements Graph {
  
  public abstract V getRoot();
  
  public abstract List<V> getChildren(V parent);
  
  public abstract V getParent(V child);

  public Set<? extends V> getVertices() {
    return _getVertices(getRoot(), new LinkedHashSet<V>());
  }
    
  private Set<V> _getVertices(V parent, Set<V> result) {
    result.add(parent);
    for (V child : getChildren(parent)) 
      _getVertices(child, result);
    return result;
  }

  public Set<? extends Edge> getEdges() {
    return _getEdges(getRoot(), new HashSet<Edge>());
  }
  
  public Set<Edge> _getEdges(V parent, Set<Edge> result) {
    for (V child : getChildren(parent)) {
      result.add(new DefaultEdge(parent, child));
      _getEdges(child, result);
    }
    return result;
  }
      
  @SuppressWarnings("unchecked")
  public Set<? extends Edge> getEdges(Vertex vertex) {
    Set<Edge> result = new LinkedHashSet<Edge>();
    if (!vertex.equals(getRoot()))
      result.add(new DefaultEdge(getParent((V)vertex), vertex));
    for (V child : getChildren((V)vertex))
      result.add(new DefaultEdge(vertex, child));
    return result;
  }

}
