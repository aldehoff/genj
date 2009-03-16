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
package gj.visibility;

import gj.geom.Geometry;
import gj.geom.Path;
import gj.geom.ShapeHelper;
import gj.layout.Graph2D;
import gj.model.Edge;
import gj.model.Vertex;
import gj.model.WeightedGraph;
import gj.util.LayoutHelper;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * a visibility graph implementation
 * TODO visibility graph can be done faster
 *  http://www.geometrylab.de/VisGraph/index.html.en
 *  http://bengimizrahi.googlepages.com/visibilitygraphconstructionforasetofpolygons(in2d)
 */
public class VisibilityGraph implements Graph2D, WeightedGraph {
  
  private Map<Point2D, PointLocation> point2location;
  private List<VisibleConnection> connections;
    
  /**
   * Constructor
   * @param graph2d 2d graph to build visibility graph from
   */
  public VisibilityGraph(Graph2D graph2d) {
    
    // init
    this.point2location = new HashMap<Point2D, PointLocation>(graph2d.getVertices().size()*4);
    this.connections = new ArrayList<VisibleConnection>();
     
    // each vertex is a hole
    final List<Hole> holes = new ArrayList<Hole>();
    for (Vertex v : graph2d.getVertices())
      holes.add(new Hole(graph2d.getShapeOfVertex(v), graph2d.getPositionOfVertex(v)));
    
    // loop over holes and check visibility to others
    for (int i=0; i<holes.size(); i++) 
      scan(holes.get(i), holes.subList(i+1, holes.size()), holes);
    
    // done
  }
  
  private void scan(Hole source, List<Hole> destinations, List<Hole> holes) {
    
    for (Hole dest : destinations) {
      
      for (int i=0;i<source.points.size();i++) {
        Point2D sourcePoint = source.points.get(i);
        for (int j=0;j<dest.points.size();j++) {
          Point2D destPoint = dest.points.get(j);
          if (!obstructed(sourcePoint, destPoint, holes))
            vertex(sourcePoint).sees(vertex(destPoint), connections);
        }
      }
      
    }
    // done
  }

  /** check whether a line [from,to] is obstructed by given holes */
  private boolean obstructed(Point2D from, Point2D to, List<Hole> holes) {
    for (int i=0;i<holes.size();i++) {
      if (holes.get(i).obstructs(from, to))
        return true;
    }
    return false;
  }

  
  /**
   * lookup a vertex
   */
  private PointLocation vertex(Point2D pos) {
    PointLocation v = point2location.get(pos);
    if (v==null) {
      v = new PointLocation(pos);
      point2location.put(pos, v);
    }
    return v;
  }
  
  /** interface implementation */
  public double getWeight(Edge edge) {
    return ((VisibleConnection)edge).weight;
  }
  
  /** interface implementation */
  public Collection<? extends Vertex> getVertices() {
    return point2location.values();
  }

  /** interface implementation */
  public Collection<? extends Edge> getEdges() {
    return Collections.unmodifiableCollection(connections);
  }
  
  /** vertex for position */
  public Vertex getVertex(Point2D point) throws IllegalArgumentException {
    PointLocation result = point2location.get(point);
    if (result==null)
      throw new IllegalArgumentException("Point "+point+" is not a valid point location");
    return result;
  }

  /** debug shape */
  public Shape getDebugShape() {
    
    GeneralPath result = new GeneralPath();
    for (Edge edge : getEdges()) {
      Point2D p = getPositionOfVertex(edge.getStart());
      result.append(getPathOfEdge(edge).getPathIterator(AffineTransform.getTranslateInstance(p.getX(), p.getY())), false);
    }
    return result;
  }

  public Path getPathOfEdge(Edge edge) {
    return LayoutHelper.getPath(edge, this);
  }

  public Point2D getPositionOfVertex(Vertex vertex) {
    PointLocation loc = (PointLocation)vertex;
    return new Point2D.Double(loc.x, loc.y);
  }

  public Shape getShapeOfVertex(Vertex vertex) {
    return new Rectangle2D.Double();
  }

  public AffineTransform getTransformOfVertex(Vertex vertex) {
    return null;
  }
  
  public void setPathOfEdge(Edge edge, Path shape) {
    throw new IllegalArgumentException("n/a");
  }

  public void setPositionOfVertex(Vertex vertex, Point2D pos) {
    throw new IllegalArgumentException("n/a");
  }

  public void setTransformOfVertex(Vertex vertex, AffineTransform transform) {
    throw new IllegalArgumentException("n/a");
  }
  
  /**
   * a hole in the original graph
   */
  private class Hole {
    
    private List<Point2D> points;
    
    Hole(Shape shape, Point2D pos) {
      this.points = ShapeHelper.getPoints(
        shape.getPathIterator(AffineTransform.getTranslateInstance(pos.getX(), pos.getY()))
      );
      for (int i=0;i<points.size();i++) {
        vertex(points.get(i)).sees(vertex(points.get( (i+1)%points.size() )), connections);
      }
    }
    
    boolean obstructs(Point2D lineStart, Point2D lineEnd) {
      
      for (int i=0;i<points.size();i++) {
        
        Point2D p = Geometry.getIntersection(lineStart, lineEnd, points.get(i), points.get((i+1)%points.size()));
        if (p!=null && !p.equals(lineStart) && !p.equals(lineEnd))
          return true;
      }
      
      return false;
    }
  } //Hole
  
  /**
   * An edge in the visibility graph
   */
  private static class VisibleConnection implements Edge {
    
    PointLocation start,end;
    double weight;
    
    VisibleConnection(PointLocation start, PointLocation end) {
      this.start = start;
      this.end = end;
      
      weight = end.distance(start);
    }
    public Vertex getStart() {
      return start;
    }
    public Vertex getEnd() {
      return end;
    }
  } //E
  
  /**
   * A vertex in the the visibility graph
   */
  private static class PointLocation extends Point2D.Double implements Vertex {
    
    List<VisibleConnection> es = new ArrayList<VisibleConnection>(4);
    
    PointLocation(Point2D pos) {
      super(pos.getX(), pos.getY());
    }
    
    void sees(PointLocation that, List<VisibleConnection> connections) {
      
      for (int i=0;i<es.size();i++) {
        VisibleConnection e = es.get(i); 
        if (e.end.equals(that)||e.start.equals(that))
          return;
      }
      
      VisibleConnection e = new VisibleConnection(this, that);
      connections.add(e);
      this.es.add(e);
      that.es.add(e);
      
    }
    
    public Collection<? extends Edge> getEdges() {
      return Collections.unmodifiableCollection(es);
    }
  } //PointLocation
  
} //DefaultVisibilityGraph
