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

import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;
import gj.util.LayoutHelper;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * A hierarchical layout algorithm
 */
public class HierarchicalLayoutAlgorithm implements LayoutAlgorithm {

  private double distanceBetweenLayers = 50; 
  private double distanceBetweenVertices= 50; 
  
  /**
   * Accessor - distance between layers
   */
  public void setDistanceBetweenLayers(double distanceBetweenLayers) {
    this.distanceBetweenLayers = distanceBetweenLayers;
  }

  /**
   * Accessor - distance between layers
   */
  public double getDistanceBetweenLayers() {
    return distanceBetweenLayers;
  }

  /**
   * do the layout
   */
  public Shape apply(Graph graph, Layout2D layout, Rectangle2D bounds, Collection<Shape> debugShapes) throws LayoutAlgorithmException {

    // empty case?
    if (graph.getVertices().isEmpty())
      return bounds;
    
    // 1st step - calculate layering
    LayerAssignment layerAssignment = new LongestPathLA();
    layerAssignment.assignLayers(graph, layout);
    
    // 2nd step - crossing reduction
    new LayerByLayerSweepCR().reduceCrossings(layerAssignment);
    
    // 3rd step - vertex positioning
    for (int i=0;i<layerAssignment.getNumLayers();i++) {
      
      for (int j=0; j<layerAssignment.getLayerSize(i); j++) {
        Vertex vertex = layerAssignment.getVertex(i,j);
        if (vertex!=LayerAssignment.DUMMY) {
          Point2D start = new Point2D.Double(j*distanceBetweenVertices, -i*distanceBetweenLayers);
          layout.setPositionOfVertex(vertex, start);
        }
      }
    }
    
    // 4th step - edge positioning
    for (Edge edge : graph.getEdges()) {
      Point[] routing = layerAssignment.getRouting(edge);
      Point2D[] points = new Point2D.Double[routing.length];
      for (int r=0;r<routing.length;r++) 
        points[r] = new Point2D.Double(routing[r].x*distanceBetweenVertices, -routing[r].y*distanceBetweenLayers);
      layout.setPathOfEdge(edge, 
          LayoutHelper.getPath(points, layout.getShapeOfVertex(edge.getStart()), layout.getShapeOfVertex(edge.getEnd()), false)
      );
    }
    
    
    // done
    // TODO make this faster
    return LayoutHelper.getBounds(graph, layout);
  }

  /**
   * Accessor - distance between verts
   */
  public void setDistanceBetweenVertices(double distanceBetweenVertices) {
    this.distanceBetweenVertices = distanceBetweenVertices;
  }

  /**
   * Accessor - distance between verts
   */
  public double getDistanceBetweenVertices() {
    return distanceBetweenVertices;
  }
  
}
