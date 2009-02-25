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

  public void assignLayers(Graph graph, Layout2D layout) throws GraphNotSupportedException;
  
  public int getNumLayers();
  
  public int getLayerSize(int layer);
  
  public Vertex getVertex(int layer, int u);
  
  public void swapVertices(int layer, int u, int v);
  
  public Point[] getRouting(Edge edge);
  
  public int[] getIncomingIndices(int layer, int u);
  
  public int[] getOutgoingIndices(int layer, int u);
  
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
