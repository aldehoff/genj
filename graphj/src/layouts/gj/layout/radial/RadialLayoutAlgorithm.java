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

// TODO the default animation is not pretty to look at especially in the radial case - it should rotate nodes to their destination
public class RadialLayoutAlgorithm extends AbstractLayoutAlgorithm<GraphAttributes> {

  private double distanceBetweenGenerations = 60;
  private boolean isAdjustDistances = false;
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
    Object root;
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
      this.root = root;
      this.layout = layout;
      this.debug = debug;
      this.center = layout.getPositionOfVertex(root);
      this.distanceBetweenGenerations =  distanceBetweenGenerations;
      this.attrs = getAttribute(graph);
      
      // calculate sub-tree sizes
      getSize(null, root, 0);
      
      // layout
      layout(null, root, 0, Geometry.ONE_RADIAN, 0);
      
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
    void layout(Vertex backtrack, final Vertex root, double fromRadian, final double toRadian, final double radius) {
      
      // assemble list of children
      List<Edge> edges = getNormalizedEdges(root);
      if (backtrack!=null)
        edges.removeAll(backtrack.getEdges());
      if (edges.isEmpty())
        return;
      
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
      
      // position children and iterate into their placement recursion
      double radianOfChild = fromRadian;
      for (Edge edge : edges) {
        
        Vertex child = getOther(edge, root);
        double radiusOfChild = radius + getLengthOfEdge(edge) * distanceBetweenGenerations;
        double radiansOfChild = vertex2radians.get(child).doubleValue() * shareFactor;
        layout.setPositionOfVertex(child, getPoint(center, radianOfChild + radiansOfChild/2, radiusOfChild ));
        
        if (isRotateShapes)
          layout.setTransformOfVertex(child, AffineTransform.getRotateInstance(HALF_RADIAN + radianOfChild + radiansOfChild/2) );
        else
          layout.setTransformOfVertex(child, new AffineTransform());
        
        // layout edges
        if (isBendArcs) {
          Path path = new Path();
          path.start(layout.getPositionOfVertex(root));
          path.lineTo(getPoint(center, fromRadian+radiansOfChildren/2, (radius+radiusOfChild)/2 ));
          path.lineTo(getPoint(center, radianOfChild + radiansOfChild/2, (radius+radiusOfChild)/2 ));
          path.lineTo(layout.getPositionOfVertex(child));
          if (root!=edge.getStart()) path.invert();
          path.translate(getPoint(layout.getPositionOfVertex(root==edge.getStart()?root:child), -1));
          layout.setPathOfEdge(edge, path);
        } else {
          setPath(edge, layout);
        }

        // add debugging information
        if (debug!=null) {
          debug.add(new Line2D.Double(getPoint(center, radianOfChild, radiusOfChild - distanceBetweenGenerations/2), getPoint(center, radianOfChild, radiusOfChild+distanceBetweenGenerations/2)));
          debug.add(new Line2D.Double(getPoint(center, radianOfChild+radiansOfChild, radiusOfChild - distanceBetweenGenerations/2), getPoint(center, radianOfChild+radiansOfChild, radiusOfChild+distanceBetweenGenerations/2)));
        }
        
        layout(root, child, radianOfChild, radianOfChild+radiansOfChild, radiusOfChild);
        
        radianOfChild += radiansOfChild;
      }

      // done
    }
    
    
  } //Recursion
  
} //RadialLayoutAlgorithm
