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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * An acyclic graph
 */
public class AcyclicGraph {
  
  /** graph */
  private Graph graph;
  
  /** sinks */
  private List<Vertex> sinks = new ArrayList<Vertex>();
  private Map<Vertex, Integer> vertex2layer = new HashMap<Vertex, Integer>();
  private int numLayers;
  private List<List<Vertex>> layers;

  /** constructor */
  public AcyclicGraph(Graph graph) throws GraphNotSupportedException {
    
    this.graph = graph;

    // check acyclic character - find layers&sinks
    Set<? extends Vertex> todo = new HashSet<Vertex>(graph.getVertices());
    while (!todo.isEmpty()) 
      _findSinksEnsureAcyclic(todo.iterator().next(), new Stack<Vertex>(), todo);
    
    // build layers
    layers = new ArrayList<List<Vertex>>(numLayers);
    for (int i=0;i<numLayers;i++) layers.add(new ArrayList<Vertex>());
    for (Vertex vertex : graph.getVertices()) 
      layers.get(getLayer(vertex)).add(vertex);

    // done
  }
  
  public List<List<Vertex>> getLayers() {
    return layers;
  }
  
  public List<Vertex> getLayer(int layer) {
    return layers.get(layer);
  }
  
  public int getLayer(Vertex vertex) {
    Integer result = vertex2layer.get(vertex);
    return result!=null ? result.intValue() : -1;
  }
  
  private void _findSinksEnsureAcyclic(Vertex vertex, Stack<Vertex> path, Set<? extends Vertex> todo) throws GraphNotSupportedException{
    // check if we're back at a vertex we've seen in this iteration
    if (path.contains(vertex))
      throw new GraphNotSupportedException("graph has to be acyclic");
    // don't have to revisit vertex
    todo.remove(vertex);
    // good layer = can stop
    if (getLayer(vertex)>=path.size())
      return;
    vertex2layer.put(vertex, new Integer(path.size()));
    numLayers = Math.max(numLayers, path.size()+1);
    // continue
    path.add(vertex);
    // check vertex for sink and cycle 
    boolean isSink = true;
    for (Edge edge : vertex.getEdges()) {
      if (edge.getStart().equals(vertex))  {
        isSink = false;
        _findSinksEnsureAcyclic(edge.getEnd(), path, todo);
      }
    }
    if (isSink) sinks.add(vertex);
    // backtrack
    path.remove(vertex);
  }
  
  /** 
   * accessor - sinks
   */
  public List<? extends Vertex> getSinks() {
    return sinks;
  }
}
