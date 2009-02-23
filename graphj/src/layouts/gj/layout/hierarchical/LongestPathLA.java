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

import static gj.layout.hierarchical.Layer.Assignment;

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
  
  /** layering algorithm */
  public List<Layer> assignLayers(Graph graph, Layout2D layout) throws GraphNotSupportedException {

    // prepare state
    Map<Vertex, Assignment> vertex2assignment = new HashMap<Vertex, Assignment>();
    List<Layer> layers = new ArrayList<Layer>();

    // find sinks
    for (Vertex v : graph.getVertices()) {
      if (LayoutHelper.isSink(v)) 
        sinkToSource(v, new Stack<Assignment>(), vertex2assignment, layers, layout);
    }
    
    // place vertices in resulting layers
    for (Vertex vertex : graph.getVertices()) {
      Assignment assignment = vertex2assignment.get(vertex);
      layers.get(assignment.layer()).add(assignment);
    }
    
    // add dummy vertices
    dummyVertices(layers, layout);

    // done
    return layers;
  }
  
  /**
   * add dummy vertices were edges span multiple layers
   */
  private void dummyVertices(List<Layer> layers, Layout2D layout) {
    // FIXME need dummy vertices

    // loop over layers and check incoming
    for (int i=0;i<layers.size()-1;i++) {
      Layer layer = layers.get(i);
      
      for (Assignment sink : layer) {
        
        for (int j=0;j<sink.incoming().size();j++) {
          
          Assignment source = sink.incoming().get(j);
          if (source.layer()!=i+1) {

            // add a dummy vertex/assignment
            Assignment dummy = new Assignment(source, sink, i+1, layout.getPositionOfVertex(sink.vertex()).getX());
            layers.get(i+1).add(dummy);
            
          }
        }
        
      }
      
    }

    // done
  }

  /**
   * walk from sink to source recursively and collect layer information plus incoming vertices
   */
  private Assignment sinkToSource(Vertex vertex, Stack<Assignment> path, Map<Vertex, Assignment> vertex2assignment, List<Layer> layers, Layout2D layout) throws GraphNotSupportedException{
    
    // check if we're back at a vertex we've seen in this iteration
    if (path.contains(vertex))
      throw new GraphNotSupportedException("graph has to be acyclic");
    
    // make sure we have enough layers
    if (layers.size()<path.size()+1)
      layers.add(new Layer());
    
    // create or reuse an assignment
    Assignment assignment = vertex2assignment.get(vertex);
    if (assignment==null) {
      assignment = new Assignment(vertex, -1, layout.getPositionOfVertex(vertex).getX());
      vertex2assignment.put(vertex, assignment);
    }
    
    // add adjacent vertices (previous in path)
    if (!path.isEmpty())
      assignment.addOutgoing(path.peek());
    
    // push to new layer and continue if assignments layer has changed
    if (!assignment.push(path.size()))
      return assignment;      

    // recurse into incoming edges direction of source
    path.push(assignment);
    for (Edge edge : vertex.getEdges()) {
      if (edge.getEnd().equals(vertex))
        assignment.addIncoming(sinkToSource(edge.getStart(), path, vertex2assignment, layers, layout));
    }
    path.pop();
    
    // done
    return assignment;
  }
   
}
