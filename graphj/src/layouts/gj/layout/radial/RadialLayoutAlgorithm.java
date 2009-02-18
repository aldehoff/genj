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
package gj.layout.radial;

import static gj.util.LayoutHelper.setPath;
import static gj.util.LayoutHelper.assertSpanningTree;
import static gj.util.LayoutHelper.getDiameter;
import static gj.util.LayoutHelper.getNormalizedEdges;
import static gj.util.LayoutHelper.getOther;
import static java.lang.Math.max;
import gj.geom.Geometry;
import gj.geom.Path;
import gj.layout.AbstractLayoutAlgorithm;
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithmException;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for a Radial layout algorithm
 */
public class RadialLayoutAlgorithm extends AbstractLayoutAlgorithm<GraphAttributes> {

  private double distanceBetweenGenerations = 60;
  private boolean isAdjustDistances = true;
  private boolean isFanOut = false; 
  private double distanceInGeneration = 0;
  private boolean isOrderSiblingsByPosition = true;
  private boolean isRotateShapes = true;
  private boolean isBendArcs = true;

  @Override
  protected GraphAttributes getAttribute(Graph graph) {
    GraphAttributes attrs = super.getAttribute(graph);
    if (attrs==null) {
      attrs = new GraphAttributes();
      super.setAttribute(graph, attrs);
    }
    return attrs;
  }
  
  /**
   * Accessor - root node
   */
  public Vertex getRoot(Graph graph) {
    return getAttribute(graph).getRoot();
  }

  /**
   * Accessor - root node
   */
  public void setRoot(Graph graph, Vertex root) {
    getAttribute(graph).setRoot(root);
  }
  
  /**
   * Accessor - number of generations an edge spans 
   * @edge the edge to set the length for
   * @length length in generations (great equals 1)
   */
  public void setLength(Graph graph, Edge edge, int length) {
    getAttribute(graph).setLength(edge, length);
  }
  
  /**
   * Accessor - number of generations an edge spans 
   */
  public int getLength(Graph graph, Edge edge) {
    return getAttribute(graph).getLength(edge);
  }
  
  /**
   * Accessor - distance of generations
   */
  public void setDistanceBetweenGenerations(double distanceBetweenGenerations) {
    this.distanceBetweenGenerations = max(1, distanceBetweenGenerations);
  }

  /**
   * Accessor - distance of generations
   */
  public double getDistanceBetweenGenerations() {
    return distanceBetweenGenerations;
  }

  /**
   * Accessor - distance in generation
   */
  public void setDistanceInGeneration(double distanceInGeneration) {
    this.distanceInGeneration = distanceInGeneration;
  }

  /**
   * Accessor - distance in generation
   */
  public double getDistanceInGeneration() {
    return distanceInGeneration;
  }

  /**
   * Accessor - whether to adjust distances to generate space avoiding vertex overlap 
   */
  public boolean isAdjustDistances() {
    return isAdjustDistances;
  }

  /**
   * Accessor - whether to adjust distances to generate space avoiding vertex overlap 
   */
  public void setAdjustDistances(boolean isAdjustDistances) {
    this.isAdjustDistances = isAdjustDistances;
  }

  /**
   * Accessor - whether to fan out children as much as possible or group them closely
   */
  public boolean isFanOut() {
    return isFanOut;
  }

  /**
   * Accessor - whether to fan out children as much as possible or group them closely
   */
  public void setFanOut(boolean isFanOut) {
    this.isFanOut = isFanOut;
  }

  /**
   * Accessor - whether to rotate shape of vertex
   */
  public boolean isRotateShapes() {
    return isRotateShapes;
  }

  /**
   * Accessor - whether to fan out children as much as possible or group them closely
   */
  public void setRotateShapes(boolean isRotateShapes) {
    this.isRotateShapes = isRotateShapes;
  }

  /**
   * Getter - whether arcs are direct or bended
   */
  public boolean isBendArcs() {
    return isBendArcs;
  }

  /**
   * Setter - whether arcs are direct or bended
   */
  public void setBendArcs(boolean set) {
    isBendArcs=set;
  }
  
  /**
   * Setter - whether to order siblings by their current position
   */
  public void setOrderSiblingsByPosition(boolean isOrderSiblingsByPosition) {
    this.isOrderSiblingsByPosition = isOrderSiblingsByPosition;
  }

  /**
   * Getter - whether to order siblings by their current position
   */
  public boolean isOrderSiblingsByPosition() {
    return isOrderSiblingsByPosition;
  }

  /**
   * Layout a layout capable graph
   */
  public Shape apply(Graph graph, Layout2D layout, Rectangle2D bounds, Collection<Shape> debugShapes) throws LayoutAlgorithmException {
    
    // check that we got a tree
    assertSpanningTree(graph);
    
    // ignore an empty tree
    if (graph.getVertices().size()==0)
      return bounds;
    
    // check root
    Vertex root = getRoot(graph);
    if (root==null) 
      root = graph.getVertices().iterator().next();
    
    // run recursion
    return new Recursion(graph, root, distanceBetweenGenerations,layout, debugShapes).getShape();
    
  }
  
  /** 
   * the recursion
   */
  private class Recursion extends Geometry {
    
    Graph graph;
    Layout2D layout;
    Collection<Shape> debug;
    int depth;
    Map<Vertex, Double> vertex2radians = new HashMap<Vertex, Double>();
    Point2D center;
    double distanceBetweenGenerations;
    double maxDiameter;
    GraphAttributes attrs;
    
    Recursion(Graph graph, Vertex root, double distanceBetweenGenerations, Layout2D layout, Collection<Shape> debug) {
      
      // init state
      this.graph = graph;
      this.layout = layout;
      this.debug = debug;
      this.center = layout.getPositionOfVertex(root);
      this.distanceBetweenGenerations =  distanceBetweenGenerations;
      this.attrs = getAttribute(graph);
      
      // calculate sub-tree sizes
      getSize(null, root, 0);
      
      // layout
      double north = layout(null, root, 0, Geometry.ONE_RADIAN, 0);
      
      // modify root's shape
      if (isRotateShapes)
        layout.setTransformOfVertex(root, AffineTransform.getRotateInstance(HALF_RADIAN + north) );
      else
        layout.setTransformOfVertex(root, new AffineTransform());
      
      // add debug rings
      if (debug!=null) {
        for (int i=1;i<=depth ;i++) 
          debug.add(getCircle(i*this.distanceBetweenGenerations - this.distanceBetweenGenerations/2));
      }

      // done
    }
    
    Shape getShape() {
      return getCircle(depth*distanceBetweenGenerations - distanceBetweenGenerations/2);
    }
    
    Shape getCircle(double radius) {
      return new Ellipse2D.Double(center.getX()-radius, center.getY()-radius, radius*2, radius*2);
    }
    
    /**
     * calculate the distance of two vertices (1+)
     */
    int getLengthOfEdge(Edge e) {
      return attrs.getLength(e);
    }
    
    /**
     * calculate the size of a sub-tree starting at root
     */
    double getSize(Vertex backtrack, Vertex root, int generation) {
      
      // update our depth info
      depth = max(depth, generation+1);

      // calculate size for children
      double radiansOfChildren = 0;
      for (Edge edge : getNormalizedEdges(root)) {
        Vertex child = getOther(edge, root);
        if (child.equals(backtrack)) 
          continue;
        int generationOfChild = generation + getLengthOfEdge(edge);
        radiansOfChildren += getSize(root, child, generationOfChild);
      }
      
      // root?
      if (generation==0) {
        double reqDistanceBetweenGenerations = max(
            maxDiameter,
            radiansOfChildren / ONE_RADIAN * distanceBetweenGenerations
            );
        if (isAdjustDistances && reqDistanceBetweenGenerations>distanceBetweenGenerations)
          distanceBetweenGenerations = reqDistanceBetweenGenerations;
        return 0;
      }
      
      // calculate size root
      double diamOfRoot = getDiameter(root, layout);
      maxDiameter = max(maxDiameter, diamOfRoot);
      double radiansOfRoot = ( diamOfRoot + distanceInGeneration ) / (generation*distanceBetweenGenerations);
      
      // keep and return
      double result = max( radiansOfChildren, radiansOfRoot);
      vertex2radians.put(root, new Double(result));
      
      return result;
    }
    
    /**
     * recursive layout call
     */
    double layout(Vertex backtrack, final Vertex root, double fromRadian, final double toRadian, final double radius) {
      
      // assemble list of children
      List<Edge> edges = getNormalizedEdges(root);
      if (backtrack!=null)
        edges.removeAll(backtrack.getEdges());
      if (edges.isEmpty())
        return (toRadian+fromRadian)/2;
      
      // sort children by current position
      if (isOrderSiblingsByPosition)  {
        final double north = fromRadian+(toRadian-fromRadian)/2+Geometry.HALF_RADIAN;
 
        Edge[] tmp = edges.toArray(new Edge[edges.size()]);
        Arrays.sort(tmp, new Comparator<Edge>() {
          public int compare(Edge e1, Edge e2) {
            
            double r1 = getRadian(getDelta(center,layout.getPositionOfVertex(getOther(e1, root))));
            double r2 = getRadian(getDelta(center,layout.getPositionOfVertex(getOther(e2, root))));
            
            if (r1>north)
              r1 -= ONE_RADIAN;
            if (r2>north)
              r2 -= ONE_RADIAN;
            
            if (r1<r2) 
              return -1;
            if (r1>r2)
              return 1;
            return 0;
          }
        });
        edges = Arrays.asList(tmp);
      }
      
      // compare actual radians available vs allocation
      double radiansOfChildren = 0;
      for (Edge edge : edges) {
        radiansOfChildren += vertex2radians.get(getOther(edge, root)).doubleValue();
      }
      double shareFactor = (toRadian-fromRadian) / radiansOfChildren;

      // check for extra radians/2 we can skip
      if ( shareFactor>1 && !isFanOut) {  
        if (backtrack!=null)
          fromRadian += ((toRadian-fromRadian) - radiansOfChildren) / 2;
        shareFactor = 1;
      }
      
      double radianOfRoot = fromRadian+radiansOfChildren*shareFactor/2;
      double[] radianOfChild = new double[edges.size()];
      
      // position children and iterate into their placement recursion
      for (int c=0;c<edges.size();c++) {
        
        Edge edge = edges.get(c);
        
        // place child
        Vertex child = getOther(edge, root);
        double radiusOfChild = radius + getLengthOfEdge(edge) * distanceBetweenGenerations;
        double radiansOfChild = vertex2radians.get(child).doubleValue() * shareFactor;
        radianOfChild[c] = fromRadian + radiansOfChild/2;
        layout.setPositionOfVertex(child, getPoint(center, radianOfChild[c], radiusOfChild ));

        // modify shape
        if (isRotateShapes)
          layout.setTransformOfVertex(child, AffineTransform.getRotateInstance(HALF_RADIAN + radianOfChild[c]) );
        else
          layout.setTransformOfVertex(child, new AffineTransform());
        
        // add debugging information
        if (debug!=null) {
          debug.add(new Line2D.Double(getPoint(center, fromRadian, radiusOfChild - distanceBetweenGenerations/2), getPoint(center, fromRadian, radiusOfChild+distanceBetweenGenerations/2)));
          debug.add(new Line2D.Double(getPoint(center, fromRadian+radiansOfChild, radiusOfChild - distanceBetweenGenerations/2), getPoint(center, fromRadian+radiansOfChild, radiusOfChild+distanceBetweenGenerations/2)));
        }
        
        layout(root, child, fromRadian, fromRadian+radiansOfChild, radiusOfChild);
        
        fromRadian += radiansOfChild;
      }
      
      // layout edges
      for (int c=0;c<edges.size();c++) {
        Edge edge = edges.get(c);
        layout(edge, root, radianOfRoot, radius+distanceBetweenGenerations/2, radianOfChild[c], getOther(edge, root), radianOfChild);
      }

      // done
      return radianOfRoot;
    }
    
    /**
     * layout an edge 
     */
    void layout(Edge edge, Vertex parent, double radianOfParent, double radius, double radianOfChild, Vertex child, double[] stopRadians) {

      // easy case - direc path
      if (!isBendArcs || (radianOfParent==radianOfChild)) {
        setPath(edge, layout);
        return;
      } 
      
      Path path = new Path();
      
      // start with path at child
      Point2D p1 = layout.getPositionOfVertex(child);
      Point2D p2 = getPoint(center, radianOfChild, radius);
      path.start(getVectorEnd(p2, p1, layout.getShapeOfVertex(child)));
      path.lineTo(p2);

      // run over stops in arc (clock/counter-clockwise)
      if (radianOfParent>radianOfChild) {
        for (int stop=0;stopRadians[stop]<radianOfParent;stop++) {
          if (stopRadians[stop]<=radianOfChild)
            continue;
          path.arcTo(center, radius, radianOfChild, stopRadians[stop]);
          radianOfChild = stopRadians[stop];
        }
      } else {
        for (int stop=stopRadians.length-1;stopRadians[stop]>radianOfParent;stop--) {
          if (stopRadians[stop]>=radianOfChild)
            continue;
          path.arcTo(center, radius, radianOfChild, stopRadians[stop]);
          radianOfChild = stopRadians[stop];
        }
      }

      // end path with final segment to parent
      Point2D p3 = getPoint(center, radianOfParent, radius);
      Point2D p4 = layout.getPositionOfVertex(parent);
      path.arcTo(center, radius, radianOfChild, radianOfParent);
      path.lineTo(getVectorEnd(p3, p4, layout.getShapeOfVertex(parent)));

      // layout relative to start
      if (parent.equals(edge.getStart())) {
        path.setInverted();
        path.translate(getNeg(layout.getPositionOfVertex(parent)));
      } else {
        path.translate(getNeg(layout.getPositionOfVertex(child)));
      }
        
      layout.setPathOfEdge(edge, path);

    }

    
  } //Recursion
  
} //RadialLayoutAlgorithm
