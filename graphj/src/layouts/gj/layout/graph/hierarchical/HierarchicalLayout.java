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
package gj.layout.graph.hierarchical;

import static gj.geom.Geometry.getBounds;
import gj.layout.Graph2D;
import gj.layout.GraphLayout;
import gj.layout.LayoutContext;
import gj.layout.LayoutException;
import gj.layout.graph.hierarchical.LayerAssignment.DummyVertex;
import gj.model.Edge;
import gj.model.Vertex;
import gj.util.LayoutHelper;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A hierarchical layout
 */
public class HierarchicalLayout implements GraphLayout {

  private double distanceBetweenLayers = 20; 
  private double distanceBetweenVertices= 20; 
  private boolean isSinksAtBottom = true;
  private double alignmentOfLayers = 0.5;
  private Comparator<Vertex> orderOfVerticesInLayer = null;
  private HorizontalPositioning positioning = HorizontalPositioning.Simplex;
  
  /**
   * do the layout
   */
  public Shape apply(Graph2D graph2d, LayoutContext context) throws LayoutException {

    // empty case?
    if (graph2d.getVertices().isEmpty())
      return new Rectangle2D.Double();
    
    // wrap layout into dummy aware one
    graph2d = new DummyAwareGraph2D(graph2d);
    
    // 1st step - calculate layering
    LayerAssignment layerAssignment = new LongestPathLA();
    layerAssignment.assignLayers(graph2d, orderOfVerticesInLayer);
    
    context.getLogger().fine("Layer assignment with "+layerAssignment.getHeight()+" layers, maximum width "+layerAssignment.getWidth()+", "+layerAssignment.getNumDummyVertices()+" dummy vertices");
    
    // 2nd step - crossing reduction
    new LayerByLayerSweepCR().reduceCrossings(layerAssignment);
    
    // 3rd step - vertex positioning and edge routing
    return assignPositions(graph2d, layerAssignment);
    
  }

  /**
   * assign positions to vertices
   */
  private Rectangle2D assignPositions(Graph2D graph2d, LayerAssignment layerAssignment) {
    
    // FIXME need options for node placement 
    //  max # of bends
    //  balance nodes left/center/right
    
    int layers = layerAssignment.getHeight();
    
    // calculate true width of widest layer in points
    double totalHeight = 0;
    double totalWidth = 0;
    double[] layerWidths = new double[layers];
    double[] layerHeights = new double[layers];
    Rectangle2D[][] vertexBounds = new Rectangle2D[layers][layerAssignment.getWidth()];
    for (int i=0;i<layers;i++) {
      for (int j=0;j<layerAssignment.getWidth(i);j++) {
        if (j>0) layerWidths[i]+=distanceBetweenVertices;
        Rectangle2D r = getBounds(graph2d.getShapeOfVertex(layerAssignment.getVertex(i, j)));
        vertexBounds[i][j] = r;
        layerWidths[i] += r.getWidth();
        layerHeights[i] = Math.max(layerHeights[i], r.getHeight());
      }
      totalHeight += layerHeights[i];
      if (layerWidths[i]>totalWidth) totalWidth = layerWidths[i];
    }
    
    // loop over layers and place vertices 
    int dir = isSinksAtBottom ? -1 : 1;
    double y = 0;
    for (int i=0;i<layers;i++) {
      if (i>0) y += dir*distanceBetweenLayers;
      double x = (totalWidth-layerWidths[i])*alignmentOfLayers;
      for (int j=0; j<layerAssignment.getWidth(i); j++) {
        
        Vertex vertex = layerAssignment.getVertex(i,j);
        graph2d.setTransformOfVertex(vertex, null);
        if (j>0) x += (vertex instanceof DummyVertex || layerAssignment.getVertex(i,j-1) instanceof DummyVertex) ? distanceBetweenVertices/2 : distanceBetweenVertices;
        Rectangle2D r = vertexBounds[i][j];
        if (dir<0)
          graph2d.setPositionOfVertex(vertex, new Point2D.Double(x - r.getMinX(), y - r.getMaxY() - (layerHeights[i]-r.getHeight())/2 ));
        else
          graph2d.setPositionOfVertex(vertex, new Point2D.Double(x - r.getMinX(), y - r.getMinY() + (layerHeights[i]-r.getHeight())/2 ));
        
        x += r.getWidth();
      }
      y += dir*layerHeights[i];
    }
    
    // route edges appropriately
    for (Edge edge : graph2d.getEdges()) {
      
      Point[] routing = layerAssignment.getRouting(edge);
      List<Point2D> points = new ArrayList<Point2D>(routing.length);
      for (int r=0;r<routing.length;r++) {
        int layer = routing[r].y;
        int pos = routing[r].x;
        if (r>0&&r<routing.length-1) {
          Point2D p = graph2d.getPositionOfVertex(layerAssignment.getVertex(layer, pos));
          points.add(new Point2D.Double(p.getX(),p.getY()+dir*layerHeights[layer]/2));
          points.add(new Point2D.Double(p.getX(),p.getY()-dir*layerHeights[layer]/2));
        } else {
          points.add(graph2d.getPositionOfVertex(layerAssignment.getVertex(layer, pos)));
        }
      }
      
      graph2d.setPathOfEdge(edge, 
          LayoutHelper.getPath(points, graph2d.getShapeOfVertex(edge.getStart()), graph2d.getShapeOfVertex(edge.getEnd()), false)
      );
    }
    
    // done
    return new Rectangle2D.Double(0,y<0?y:0,totalWidth,y<0?-y:y);
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

  /**
   * Accessor - ordering of vertices in layers
   */
  public Comparator<Vertex> getOrderOfVerticesInLayer() {
    return orderOfVerticesInLayer;
  }

  /**
   * Accessor - ordering of vertices in layers
   */
  public void setOrderOfVerticesInLayer(Comparator<Vertex> orderOfVerticesInLayer) {
    this.orderOfVerticesInLayer = orderOfVerticesInLayer;
  }
  
  /**
   * Accessor - horizontal positioning
   */
  public HorizontalPositioning getHorizontalPositioning() {
    return positioning;
  }
  
  /**
   * Accessor - horizontal positioning
   */
  public void setHorizontalPositioning(HorizontalPositioning positioning) {
    this.positioning = positioning;
  }
  
  /**
   * our supported horizontal positioning
   */
  public enum HorizontalPositioning {
    
    Compact,
    Simplex 
    
  } //HorizontalPositioning

} //HierarchicalLayout
