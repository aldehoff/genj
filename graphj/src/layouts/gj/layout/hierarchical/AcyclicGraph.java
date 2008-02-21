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
package gj.layout.hierarchical;

import gj.layout.GraphNotSupportedException;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;
import gj.util.ModelHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An acyclic graph
 */
public class AcyclicGraph {
  
  /** graph */
  private Graph graph;
  
  /** sinks */
  private List<Vertex> sinks = new ArrayList<Vertex>();
  

  /** constructor */
  public AcyclicGraph(Graph graph) throws GraphNotSupportedException {
    
    this.graph = graph;

    // check acyclic character - find sinks
    Set<? extends Vertex> unchecked = ModelHelper.toSet(graph.getVertices());
    while (!unchecked.isEmpty()) 
      _findSinksEnsureAcyclic(unchecked.iterator().next(), new HashSet<Vertex>(), unchecked);

    // done
  }
  
  private void _findSinksEnsureAcyclic(Vertex vertex, Set<Vertex> seen, Set<? extends Vertex> unchecked) throws GraphNotSupportedException{
    // check if we're back at a vertex we've seen in this iteration
    if (seen.contains(vertex))
      throw new GraphNotSupportedException("graph has to be acyclic");
    seen.add(vertex);
    // check vertex for sink and cycle 
    if (unchecked.remove(vertex)) {
      boolean isSink = true;
      for (Edge edge : graph.getEdges(vertex)) {
        if (edge.getStart().equals(vertex))  {
          isSink = false;
          _findSinksEnsureAcyclic(edge.getEnd(), seen, unchecked);
        }
      }
      if (isSink) sinks.add(vertex);
    }
    // return
    seen.remove(vertex);
  }
  
  /** 
   * accessor - sinks
   */
  public List<? extends Vertex> getSinks() {
    return sinks;
  }
}
