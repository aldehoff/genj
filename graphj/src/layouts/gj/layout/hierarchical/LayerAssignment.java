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

import java.awt.Point;
import java.util.Collection;

import gj.layout.GraphNotSupportedException;
import gj.layout.Layout2D;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;

/**
 * A interface to a layering of vertexes
 */
public interface LayerAssignment {

  /**
   * Process given graph and layout and produce a proper layer assignment
   * @param graph the graph to analyze
   * @param layout the layout 
   * @throws GraphNotSupportedException
   */
  public void assignLayers(Graph graph, Layout2D layout) throws GraphNotSupportedException;

  /**
   * Number of layers
   * @return height
   */
  public int getHeight();

  /**
   * Maximum of number of Vertices each layer
   * @return width
   */
  public int getWidth();
  
  /**
   * Number of Vertices for given layer 
   * @param layer the layer to prompt
   * @return width
   */
  public int getWidth(int layer);
  
  /**
   * Vertex in a layer
   * @param layer the layer
   * @param u the position in the layer
   * @return selected vertex in graph or DUMMY
   * @see LayerAssignment#DUMMY
   */
  public Vertex getVertex(int layer, int u);

  /**
   * Swap two vertices in a layer
   * @param layer the layer
   * @param u first position
   * @param v second position
   */
  public void swapVertices(int layer, int u, int v);

  /**
   * Routing of a given edge
   * @param edge the edge
   * @return list of pairs (layer,position)*
   */
  public Point[] getRouting(Edge edge);

  /**
   * Adjacent positions for layer and positions
   * @param layer the layer
   * @param u the position
   * @return list of indices in layer-1
   */
  public int[] getIncomingIndices(int layer, int u);
  
  /**
   * Adjacent positions for layer and positions
   * @param layer the layer
   * @param u the position
   * @return list of indices in layer+1
   */
  public int[] getOutgoingIndices(int layer, int u);

  /**
   * The dummy vertex 
   * @see LayerAssignment#getVertex(int, int)
   */
  public static Vertex DUMMY = new Vertex() {
    @Override
    public String toString() {
      return "Dummy";
    }
    public Collection<? extends Edge> getEdges() {
      throw new IllegalArgumentException("n/a");
    }
  };
}
