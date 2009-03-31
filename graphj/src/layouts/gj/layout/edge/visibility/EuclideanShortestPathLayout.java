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

import static gj.geom.Geometry.getConvexHull;
import gj.geom.Geometry;
import gj.geom.Path;
import gj.layout.EdgeLayout;
import gj.layout.Graph2D;
import gj.layout.GraphLayout;
import gj.layout.GraphNotSupportedException;
import gj.layout.LayoutContext;
import gj.layout.LayoutException;
import gj.layout.Port;
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
import java.util.Collection;
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
    GraphWrapper wrapper = new GraphWrapper(graph2d, edge);
  
    // create a visibility graph for the edge
    VisibilityGraph graph = new VisibilityGraph(wrapper);
    Vertex source = graph.getVertex(wrapper.sourcePort);
    Vertex dest = graph.getVertex(wrapper.destPort);
    
    // find shortest path for edge
    List<Vertex> route = new DijkstraShortestPath().getShortestPath(graph, source, dest);

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

    private List<Vertex> vertices;
    private Map<Vertex,Shape> vertex2shape = new HashMap<Vertex,Shape>();
    private Map<Vertex,Point2D> vertex2pos = new HashMap<Vertex,Point2D>();
    private Point2D sourcePort, destPort;
    
    GraphWrapper(Graph2D delegated, Edge edge) {
      super(delegated);
      
      // grab vertices
      Collection<? extends Vertex> vertices = super.getVertices();
      this.vertices = new ArrayList<Vertex>(vertices.size()+2);
      this.vertices.addAll(vertices);
      
      // patch source and sink
      sourcePort = patch(delegated, edge, true);
      destPort = patch(delegated, edge, false);
    }
    
    /** overwrite a vertex's shape */
    private Point2D patch(Graph2D delegated, Edge edge, boolean start) {

      Vertex vertex = start ? edge.getStart() : edge.getEnd();
      Port port = getPortOfEdge(edge, vertex);

      // special shape based on port
      switch (port) {
        case Fixed:

          Point2D p1 = getPositionOfVertex(vertex);
          Point2D p2  = Geometry.getSum(
            start ? getPathOfEdge(edge).getFirstPoint() : getPathOfEdge(edge).getLastPoint(),
            getPositionOfVertex(edge.getStart())
          );
          return dummy(Geometry.getClosest(p2, Geometry.getIntersections(
                p1, p2, true,
                p1, getShapeOfVertex(vertex)
            )));
        case West:
        case East:
        case North:
        case South:
          return dummy(LayoutHelper.getPort(getPositionOfVertex(vertex), getShapeOfVertex(vertex), 0, 1, port));
        default:
        case None:
          // no shape for source and sink
          vertex2shape.put(vertex, new Rectangle2D.Double());
          return super.getPositionOfVertex(vertex);
      }
        
    }
    
    /** create a dummy */
    private Point2D dummy(Point2D pos) {
      Vertex dummy = new DummyVertex();
      vertices.add(dummy);
      vertex2pos.put(dummy, pos);
      vertex2shape.put(dummy, new Rectangle2D.Double());
      return pos;
    }
    
    @Override
    public Collection<? extends Vertex> getVertices() {
      return vertices;
    }
    
    @Override
    public Shape getShapeOfVertex(Vertex vertex) {
      
      Shape shape = vertex2shape.get(vertex);
      if (shape==null) {
        
        // build convex hull
        GeneralPath hull = new GeneralPath(getConvexHull(super.getShapeOfVertex(vertex)));
        
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
        vertex2shape.put(vertex, hull);
        
        shape = hull;
      }
      return shape;
    }
    
    @Override
    public Point2D getPositionOfVertex(Vertex vertex) {
      Point2D pos = vertex2pos.get(vertex);
      return pos!=null ? pos : super.getPositionOfVertex(vertex);
    }
    
  } //ManipulatingGraph
  
  private class DummyVertex implements Vertex {
    public Collection<? extends Edge> getEdges() {
      return new ArrayList<Edge>();
    }
  };


} //EuclideanShortestPathLayout
