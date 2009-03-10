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

import gj.geom.Path;
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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Given a set of vertices w/shapes find the shortest path between two vertices
 * that does not intersect any of the shapes.
 */
public class EuclideanShortestPathLayout implements GraphLayout {

  /**
   * apply it
   */
  public Shape apply(Graph2D graph2d, LayoutContext context) throws LayoutException {

    // loop over all edges and perform layout
    boolean debugOnce = false;
    for (Edge edge : graph2d.getEdges()) {
      
      VisibilityGraph vg = layout(edge, graph2d);
      if (context.isDebug()&&!debugOnce) {
        debugOnce = true;
        context.addDebugShape(vg.getDebugShape());
      }
    }
    
    // done
    return LayoutHelper.getBounds(graph2d);
  }
  
  /**
   * apply it 
   */
  public void apply(Edge edge, Graph2D graph2d) throws GraphNotSupportedException {
    layout(edge, graph2d);
  }
  
  /** layout generation of one edge */
  private VisibilityGraph layout(final Edge edge, Graph2D graph2d) throws GraphNotSupportedException {
    
    // create a wrapped graph that overwrites start/end vertices' shape
    DelegatingGraph collapsed = new DelegatingGraph(graph2d) {
      @Override
      public Shape getShapeOfVertex(Vertex vertex) {
        if (LayoutHelper.contains(edge, vertex))
          return new Rectangle2D.Double();
        return super.getShapeOfVertex(vertex);
      }
    };
  
    // create a visibility graph for the edge
    VisibilityGraph graph = new VisibilityGraph(collapsed);
    Vertex source = graph.getVertex(collapsed.getPositionOfVertex(edge.getStart()));
    Vertex sink = graph.getVertex(collapsed.getPositionOfVertex(edge.getEnd()));
    
    // find shortest path for edge
    List<Vertex> route = new DijkstraShortestPath().getShortestPath(graph, source, sink);

    // debug
    List<Point2D> ps = new ArrayList<Point2D>(route.size());
    for (Vertex v : route) 
      ps.add(graph.getPositionOfVertex(v));
      
    Path path = LayoutHelper.getPath(ps, graph2d.getShapeOfVertex(edge.getStart()), graph2d.getShapeOfVertex(edge.getEnd()), false);
    collapsed.setPathOfEdge(edge, path);
   
    // done
    return graph;
  }
  
} //EuclideanShortestPathLayout
