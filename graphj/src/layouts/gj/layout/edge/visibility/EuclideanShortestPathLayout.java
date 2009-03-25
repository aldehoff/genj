/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2009 Nils Meier
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
package gj.layout.edge.visibility;

import static gj.util.LayoutHelper.*;

import gj.geom.Geometry;
import gj.geom.Path;
import gj.layout.EdgeLayout;
import gj.layout.Graph2D;
import gj.layout.GraphLayout;
import gj.layout.GraphNotSupportedException;
import gj.layout.LayoutContext;
import gj.layout.LayoutException;
import gj.model.Edge;
import gj.model.Vertex;
import gj.routing.dijkstra.DijkstraShortestPath;
import gj.util.DelegatingGraph;
import gj.util.LayoutHelper;
import gj.visibility.VisibilityGraph;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a set of vertices w/shapes find the shortest path between two vertices
 * that does not intersect any of the shapes.
 */
public class EuclideanShortestPathLayout implements GraphLayout, EdgeLayout {
  
  private double edgeVertexDistance = 3;
  private int debugIndex = 0;

  /**
   * apply it
   */
  public Shape apply(Graph2D graph2d, LayoutContext context) throws LayoutException {

    if (context.isDebug() && ++debugIndex>graph2d.getEdges().size()-1)
      debugIndex = 0;
    
    // loop over all edges and perform layout
    int debug = 0;
    for (Edge edge : graph2d.getEdges()) {
      
      VisibilityGraph vg = layout(edge, graph2d);
      if (context.isDebug()&& debug++==debugIndex) {
        context.addDebugShape(vg.getDebugShape());
      }
    }
    
    // done
    return LayoutHelper.getBounds(graph2d);
  }
  
  /**
   * apply it 
   */
  public void apply(Edge edge, Graph2D graph2d, LayoutContext context) throws GraphNotSupportedException {
    layout(edge, graph2d);
  }

  /**
   * distance to keep between vertices and routed edges
   */
  public void setEdgeVertexDistance(double edgeVertexDistance) {
    this.edgeVertexDistance = Math.max(0, edgeVertexDistance);
  }

  /**
   * distance to keep between vertices and routed edges
   */
  public double getEdgeVertexDistance() {
    return edgeVertexDistance;
  }

  /** layout generation of one edge */
  private VisibilityGraph layout(Edge edge, Graph2D graph2d) throws GraphNotSupportedException {
    
    // create a wrapped graph that overwrites start/end vertices' shape
    Graph2D wrapper = new GraphWrapper(graph2d, edge);
  
    // create a visibility graph for the edge
    VisibilityGraph graph = new VisibilityGraph(wrapper);
    Vertex source = graph.getVertex(wrapper.getPositionOfVertex(edge.getStart()));
    Vertex sink = graph.getVertex(wrapper.getPositionOfVertex(edge.getEnd()));
    
    // find shortest path for edge
    List<Vertex> route = new DijkstraShortestPath().getShortestPath(graph, source, sink);

    // debug
    List<Point2D> ps = new ArrayList<Point2D>(route.size());
    for (Vertex v : route) 
      ps.add(graph.getPositionOfVertex(v));
      
    Path path = LayoutHelper.getPath(ps, 
        graph2d.getPositionOfVertex(edge.getStart()),
        graph2d.getShapeOfVertex(edge.getStart()), 
        graph2d.getPositionOfVertex(edge.getEnd()),
        graph2d.getShapeOfVertex(edge.getEnd()), 
        false);
    wrapper.setPathOfEdge(edge, path);
   
    // done
    return graph;
  }
  
  /**
   * A delegating graph that changes vertex shapes
   */
  private class GraphWrapper extends DelegatingGraph {
    
    private Map<Vertex,Shape> vertex2override = new HashMap<Vertex,Shape>();
    private Edge edge;
    
    GraphWrapper(Graph2D delegated, Edge edge) {
      super(delegated);
      this.edge = edge;
    }
    
    @Override
    public Shape getShapeOfVertex(Vertex vertex) {
      
      // overwritten?
      Shape shape = vertex2override.get(vertex);
      if (shape!=null) 
        return shape;

      // do it now
      shape = override(vertex);
      
      // remember
      vertex2override.put(vertex, shape);
      
      // done
      return shape;
    }
    
    /** overwrite a vertex's shape */
    private Shape override(Vertex vertex) {
      
      // padded shape if not a start or end?
      if (!contains(edge, vertex)) 
        return pad(vertex);
        
      // special shape based on port
      switch (getPortOfEdge(edge, vertex)) {
        default: case None:
          return new Rectangle2D.Double();
      }
        
    }
    
    /** pad a vertex's shape */
    private GeneralPath pad(Vertex vertex) {
      
      // build convex hull
      GeneralPath hull = new GeneralPath(Geometry.getConvexHull(super.getShapeOfVertex(vertex)));
      
      // pad
      if (edgeVertexDistance>0) {
        // pad it once
        Rectangle2D bounds = hull.getBounds2D();
        hull.transform(AffineTransform.getScaleInstance(
            (bounds.getWidth()+(2*edgeVertexDistance))/bounds.getWidth(), 
            (bounds.getHeight()+(2*edgeVertexDistance))/bounds.getHeight()
         ));
      }

      // done
      return hull;
    }
    
  } //ManipulatingGraph

} //EuclideanShortestPathLayout
