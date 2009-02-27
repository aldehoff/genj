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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A hierarchical layout algorithm
 */
public class HierarchicalLayoutAlgorithm implements LayoutAlgorithm {

  private double distanceBetweenLayers = 50; 
  private double distanceBetweenVertices= 50; 
  private boolean isSinksAtBottom = true;
  private double alignmentOfLayers = 0.5;
  
  /**
   * do the layout
   */
  public Shape apply(Graph graph, Layout2D layout, Rectangle2D bounds, Collection<Shape> debugShapes) throws LayoutAlgorithmException {

    // empty case?
    if (graph.getVertices().isEmpty())
      return bounds;
    
    // wrap layout into dummy aware one
    layout = new DummyAwareLayout(layout);
    
    // 1st step - calculate layering
    LayerAssignment layerAssignment = new LongestPathLA();
    layerAssignment.assignLayers(graph, layout);
    
    // 2nd step - crossing reduction
    new LayerByLayerSweepCR().reduceCrossings(layerAssignment);
    
    // 3rd step - vertex positioning
    assignPositions(layerAssignment, layout);
    
    // 4th step - edge positioning
    routeEdges(graph, layerAssignment, layout);
    
    // done
    return new Rectangle2D.Double(
        -distanceBetweenVertices/2,
        isSinksAtBottom?-layerAssignment.getHeight()*distanceBetweenLayers+distanceBetweenLayers/2:0,
        layerAssignment.getWidth()*distanceBetweenVertices,
        layerAssignment.getHeight()*distanceBetweenLayers
    );
  }

  /**
   * assign positions to vertices
   */
  private void assignPositions(LayerAssignment layerAssignment, Layout2D layout) {
    
    int height = layerAssignment.getHeight();
    
    // calculate true width of widest layer in points
    double width = 0;
    double[] widths = new double[height];
    for (int i=0;i<height;i++) {
      for (int j=0;j<layerAssignment.getWidth(i);j++) {
        if (j>0) widths[i]+=distanceBetweenVertices;
        Vertex v = layerAssignment.getVertex(i, j);
        widths[i] += layout.getShapeOfVertex(v).getBounds2D().getWidth();
      }
      if (widths[i]>width) width = widths[i];
    }

    // loop over layers and place vertices (not dummies of course)
    int dir = isSinksAtBottom ? -1 : 1;
    for (int i=0;i<height;i++) {
      double alignment = (width-layerAssignment.getWidth(i)*distanceBetweenVertices)*alignmentOfLayers; 
      for (int j=0; j<layerAssignment.getWidth(i); j++) {
        Vertex vertex = layerAssignment.getVertex(i,j);
        Point2D start = new Point2D.Double(alignment+j*distanceBetweenVertices, dir*i*distanceBetweenLayers);
        layout.setPositionOfVertex(vertex, start);
      }
    }
    
    // done
  }

  /**
   * route edges appropriately
   */
  private void routeEdges(Graph graph, LayerAssignment layerAssignment, Layout2D layout) {
    
    int width = layerAssignment.getWidth();
    int dir = isSinksAtBottom ? -1 : 1;
    
    for (Edge edge : graph.getEdges()) {
      
      Point[] routing = layerAssignment.getRouting(edge);
      List<Point2D> points = new ArrayList<Point2D>(routing.length);
      for (int r=0;r<routing.length;r++) {
        int layer = routing[r].y;
        int pos = routing[r].x;
        points.add(layout.getPositionOfVertex(layerAssignment.getVertex(layer, pos)));
      }
      
      layout.setPathOfEdge(edge, 
          LayoutHelper.getPath(points, layout.getShapeOfVertex(edge.getStart()), layout.getShapeOfVertex(edge.getEnd()), false)
      );
    }
    
    
  }

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

  /**
   * Accessor - sinks at bottom or not
   */
  public void setSinksAtBottom(boolean sinksAtBottom) {
    this.isSinksAtBottom = sinksAtBottom;
  }

  /**
   * Accessor - sinks at bottom or not
   */
  public boolean getSinksAtBottom() {
    return isSinksAtBottom;
  }

  /**
   * Accessor - alignment of layers
   */
  public void setAlignmentOfLayers(double alignmentOfLayers) {
    this.alignmentOfLayers = Math.min(1,Math.max(0, alignmentOfLayers));
  }

  /**
   * Accessor - alignment of layers
   */
  public double getAlignmentOfLayers() {
    return alignmentOfLayers;
  }
  
}
