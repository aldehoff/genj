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
package gj.layout.edge.visibility;

import gj.geom.Geometry;
import gj.geom.ShapeHelper;
import gj.layout.Graph2D;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * a visibility graph implementation
 */
public class VisibilityGraph implements Graph {
  
  private Map<Point2D, V> p2v;
    
  /**
   * Constructor
   * @param graph2d 2d graph to build visibility graph from
   */
  public VisibilityGraph(Graph2D graph2d) {
    
    // init
    this.p2v = new HashMap<Point2D, V>(graph2d.getVertices().size()*4);
     
    // each vertex is a hole
    final List<Hole> holes = new ArrayList<Hole>();
    for (Vertex v : graph2d.getVertices())
      holes.add(new Hole(graph2d.getShapeOfVertex(v), graph2d.getPositionOfVertex(v)));
    
    // loop over holes and check visibility to others
    // TODO visibility graph can be done faster
    //  http://www.geometrylab.de/VisGraph/index.html.en
    // this can be done faster
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
            vertex(sourcePoint).sees(vertex(destPoint));
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
  private V vertex(Point2D pos) {
    V v = p2v.get(pos);
    if (v==null) {
      v = new V(pos);
      p2v.put(pos, v);
    }
    return v;
  }
  
  /** interface implementation */
  public Collection<? extends Vertex> getVertices() {
    return p2v.values();
  }

  /** interface implementation */
  public Collection<? extends Edge> getEdges() {
    return new ArrayList<Edge>();
  }

  /** debug shape */
  public Shape getDebugShape() {
    GeneralPath result = new GeneralPath();
    for (V v : p2v.values()) {
      for (E e : v.es) {
        if (e.start.equals(v)) {
          result.moveTo(v.x, v.y);
          result.lineTo(e.end.x, e.end.y);
        }
      }
    }
    return result;
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
        vertex(points.get(i)).sees(vertex(points.get( (i+1)%points.size() )));
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
  private static class E implements Edge {
    
    V start,end;
    
    public E(V start, V end) {
      this.start = start;
      this.end = end;
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
  private static class V implements Vertex {
    
    double x,y;
    List<E> es = new ArrayList<E>(4);
    
    V(Point2D pos) {
      x = pos.getX();
      y = pos.getY();
    }
    
    void sees(V that) {
      
      for (int i=0;i<es.size();i++) {
        if (es.get(i).end.equals(that)||es.get(i).start.equals(that))
          return;
      }
      
      E e = new E(this, that);
      this.es.add(e);
      that.es.add(e);
    }
    
    public Collection<? extends Edge> getEdges() {
      return new ArrayList<Edge>();
    }
  } //V
  
} //DefaultVisibilityGraph
