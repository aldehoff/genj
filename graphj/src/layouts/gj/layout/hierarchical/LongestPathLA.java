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
import gj.layout.Layout2D;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;
import gj.util.LayoutHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * A layering based on longest paths from sinks
 */
public class LongestPathLA implements LayerAssignment {
  
  /** sinks */
  private List<Vertex> sinks = new ArrayList<Vertex>();
  private Map<Vertex, Layer.Assignment> vertex2assignment = new HashMap<Vertex, Layer.Assignment>();
  private int numLayers;
  private List<Layer> layers;

  /** layering algorithm */
  public void assignLayers(Graph graph, Layout2D layout) throws GraphNotSupportedException {
    
    // find sinks
    for (Vertex v : graph.getVertices()) {
      if (LayoutHelper.isSink(v)) {
        sinks.add(v);
        sinkToSource(v, new Stack<Layer.Assignment>());
      }
    }
    
    // build layers
    layers = new ArrayList<Layer>(numLayers);
    for (int i=0;i<numLayers;i++) {
      layers.add(new Layer(layout));
    }

    // add dummy vertices
    dummyVertices();

    // fill in vertices
    for (Vertex vertex : graph.getVertices()) {
      Layer.Assignment assignment = vertex2assignment.get(vertex);
      layers.get(assignment.layer()).add(assignment);
    }
    
    // done
  }
  
  /**
   * add dummy vertices were edges span multiple layers
   */
  private void dummyVertices() {

    // loop over layers and check incoming
    for (int l=0;l<layers.size();l++) {
      Layer layer = layers.get(l);
      
      // FIXME need dummy vertices
    }

    // done
  }

  /**
   * walk from sink to source recursively and collect layer information plus incoming vertices
   */
  private void sinkToSource(Vertex vertex, Stack<Layer.Assignment> path) throws GraphNotSupportedException{
    
    // check if we're back at a vertex we've seen in this iteration
    if (path.contains(vertex))
      throw new GraphNotSupportedException("graph has to be acyclic");
    numLayers = Math.max(numLayers, path.size()+1);
    
    // create or reuse an assignment
    Layer.Assignment assignment = vertex2assignment.get(vertex);
    if (assignment==null) {
      assignment = new Layer.Assignment(vertex, -1);
      vertex2assignment.put(vertex, assignment);
    }
    
    // add adjacent vertices (previous in path)
    if (!path.isEmpty())
      assignment.add(path.peek());
    
    // push to new layer and continue if assignments layer has changed
    if (!assignment.push(path.size()))
      return;      

    // recurse into incoming edges direction of source
    path.push(assignment);
    for (Edge edge : vertex.getEdges()) {
      if (edge.getEnd().equals(vertex))
        sinkToSource(edge.getStart(), path);
    }
    path.pop();
    
    // done
    return;
  }
  
  public int getNumLayers() {
    return layers.size();
  }
  
  public Layer getLayer(int layer) {
    return layers.get(layer);
  }
  
  public int getLayer(Vertex vertex) {
    Layer.Assignment assignment = vertex2assignment.get(vertex);
    return assignment!=null ? assignment.layer() : -1;
  }
  
  /** 
   * accessor - sinks
   */
  public List<? extends Vertex> getSinks() {
    return sinks;
  }
  
}
