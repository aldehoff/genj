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
import gj.model.Graph;
import gj.model.Vertex;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * A hierarchical layout algorithm
 */
public class HierarchicalLayoutAlgorithm implements LayoutAlgorithm {

  private double distanceBetweenLayers = 50; 
  
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
    
    // wrap
    AcyclicGraph ag = new AcyclicGraph(graph);
    
    // layout in layers
    for (Vertex vertex : graph.getVertices()) {
      Point2D p = layout.getPositionOfVertex(vertex);
      layout.setPositionOfVertex(vertex, new Point2D.Double(p.getX(), ag.getLayer(vertex)*getDistanceBetweenLayers()));
    }
    
    // mark sinks
    if (debugShapes!=null)
      for (Vertex sink : ag.getSinks()) {
        double d = ModelHelper.getDiameter(sink, layout);
        Point2D p = layout.getPositionOfVertex(sink); 
        debugShapes.add(new Ellipse2D.Double(p.getX()-d/2, p.getY()-d/2, d, d));
      }
    
    return bounds;
  }
  
  /**
   * the layering step implementation 
   */
  private class Layering {

    Layering(AcyclicGraph graph) {
      
    }
    
  } //Layering
  
}
