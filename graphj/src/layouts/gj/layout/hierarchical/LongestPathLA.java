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

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
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
  private Map<Vertex, Integer> vertex2layer = new HashMap<Vertex, Integer>();
  private int numLayers;
  private List<List<Vertex>> layers;

  /** layering algorithm */
  public void assignLayers(Graph graph) throws GraphNotSupportedException {
    
    // find sinks
    for (Vertex v : graph.getVertices()) {
      if (LayoutHelper.isSink(v)) {
        sinks.add(v);
        recurse(v, new Stack<Vertex>());
      }
    }
    
    // build layers
    layers = new ArrayList<List<Vertex>>(numLayers);
    for (int i=0;i<numLayers;i++) layers.add(new ArrayList<Vertex>());
    for (Vertex vertex : graph.getVertices()) 
      layers.get(getLayer(vertex)).add(vertex);

    // done
  }
  
  public void debug(Layout2D layout, Collection<Shape> debugShapes) {
    
    // mark sinks
    for (Vertex sink : getSinks()) {
      double d = LayoutHelper.getDiameter(sink, layout);
      Point2D p = layout.getPositionOfVertex(sink); 
      debugShapes.add(new Ellipse2D.Double(p.getX()-d/2, p.getY()-d/2, d, d));
    }
    
  }
  
  private void recurse(Vertex vertex, Stack<Vertex> path) throws GraphNotSupportedException{
    
    // check if we're back at a vertex we've seen in this iteration
    if (path.contains(vertex))
      throw new GraphNotSupportedException("graph has to be acyclic");
    
    // good layer = can stop
    if (getLayer(vertex)>=path.size())
      return;
    
    // dive in 
    path.add(vertex);
    
    // asume length of path for the moment
    vertex2layer.put(vertex, new Integer(path.size()-1));
    numLayers = Math.max(numLayers, path.size());
    
    // check incoming neighbours  
    for (Edge edge : vertex.getEdges()) {
      if (edge.getEnd().equals(vertex))  {
        recurse(edge.getStart(), path);
      }
    }
    
    // backtrack
    path.remove(vertex);
  }
  
  public int getNumLayers() {
    return layers.size();
  }
  
  public List<Vertex> getLayer(int layer) {
    return layers.get(layer);
  }
  
  public int getLayer(Vertex vertex) {
    Integer result = vertex2layer.get(vertex);
    return result!=null ? result.intValue() : -1;
  }
  
  /** 
   * accessor - sinks
   */
  public List<? extends Vertex> getSinks() {
    return sinks;
  }
}
