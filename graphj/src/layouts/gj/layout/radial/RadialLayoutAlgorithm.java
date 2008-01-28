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

import gj.geom.Geometry;
import gj.layout.AbstractLayoutAlgorithm;
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A radial layout for Trees
 */
public class RadialLayoutAlgorithm extends AbstractLayoutAlgorithm implements LayoutAlgorithm {
  
  private WeakReference<Vertex> rootOfTree;
  private double distanceBetweenGenerations = 60;
  private boolean isAdjustDistances = false;
  private boolean isFanOut = false; 
  private double distanceInGeneration = 0;
  private boolean isOrderSiblingsByPosition = true;
  private Map<Edge, Integer> edge2distance = new HashMap<Edge, Integer>();

  /**
   * Accessor - root node
   */
  public Vertex getRootVertex() {
    return rootOfTree != null ? rootOfTree.get() : null;
  }

  /**
   * Accessor - root node
   */
  public void setRootVertex(Vertex root) {
    rootOfTree = new WeakReference<Vertex>(root);
  }
  
  /**
   * Accessor - edge distance
   */
  public void lengthenEdge(Edge edge) {
    Integer distance = edge2distance.get(edge);
    edge2distance.put(edge, new Integer(distance !=null ? distance.intValue()+1 : 1));
  }
  
  /**
   * Accessor - edge distance
   */
  public void shortenEdge(Edge edge) {
    Integer distance = edge2distance.get(edge);
    if (distance.intValue()==1)
      edge2distance.remove(edge);
    else
      edge2distance.put(edge, new Integer(distance.intValue()-1));
  }
  
  /**
   * Accessor - distance of generations
   */
  public void setDistanceBetweenGenerations(double distanceBetweenGenerations) {
    this.distanceBetweenGenerations = Math.max(1, distanceBetweenGenerations);
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
    ModelHelper.assertSpanningTree(graph);
    
    // ignore an empty tree
    Set<? extends Vertex> verticies = graph.getVertices();
    if (verticies.isEmpty())
      return bounds;
    
    // check root
    Vertex root = getRootVertex();
    if (root==null || !verticies.contains(root))  {
      root = verticies.iterator().next();
      setRootVertex(root);
    }
    
    // check distances
    for (Edge edge : edge2distance.keySet()) {
      System.out.println(edge+":"+edge2distance.get(edge));
    }
    
    // run recursion
    return new Recursion(graph, root, distanceBetweenGenerations,layout, debugShapes).getShape();
    
  }
  
  /** 
   * the recursion
   */
  private class Recursion extends Geometry implements Comparator<Vertex> {
    
    Graph graph;
    Object root;
    Layout2D layout;
    Collection<Shape> debug;
    int depth;
    Map<Object, Double> root2size = new HashMap<Object, Double>();
    Point2D center;
    double currentNorth;
    double distanceBetweenGenerations;
    
    Recursion(Graph graph, Vertex root, double distanceBetweenGenerations, Layout2D layout, Collection<Shape> debug) {
      
      // init state
      this.graph = graph;
      this.root = root;
      this.layout = layout;
      this.debug = debug;
      this.center = layout.getPositionOfVertex(root);
      this.distanceBetweenGenerations =  distanceBetweenGenerations;
      
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
     * calculate the diameter of a node's shape
     */
    double getDiameter(Vertex vertex) {
      return Geometry.getMaximumDistance(new Point2D.Double(0,0), layout.getShapeOfVertex(vertex)) * 2;
    }

    
    /**
     * calculate the size of a sub-tree starting at root
     */
    double getSize(Vertex backtrack, Vertex root, int generation) {
      
      // update our depth info
      depth = Math.max(depth, generation+1);

      // calculate size for children
      double factor = generation==0 ? 1: (double)generation/(generation+1);
      Set<? extends Vertex> neighbours = ModelHelper.getNeighbours(graph, root);
      double sizeOfChildren = 0;
      for (Vertex child : neighbours) {
        if (!child.equals(backtrack)) {
          sizeOfChildren +=  getSize(root, child, generation + 1) * factor;
        }
      }
      
      // root?
      if (generation==0) {
        double reqDistBetGens  = sizeOfChildren  / ONE_RADIAN;
        if (reqDistBetGens>distanceBetweenGenerations && isAdjustDistances)
          distanceBetweenGenerations = reqDistBetGens;
        return 0;
      }
      
      // calculate size root
      double sizeOfRoot = getDiameter(root) + distanceInGeneration;
      
      // keep and return
      double result = Math.max( sizeOfChildren, sizeOfRoot);
      root2size.put(root, new Double(result));
      return result;
    }
    
    /**
     * recursive layout call
     */
    void layout(Vertex backtrack, Vertex root, double fromRadian, double toRadian, double radius) {
      
      // assemble list of children
      Set<? extends Vertex> neighbours = ModelHelper.getNeighbours(graph, root);
      List<Vertex> children = new ArrayList<Vertex>(neighbours.size());
      for (Vertex child : neighbours) 
        if (!child.equals(backtrack)) children.add(child);
      if (children.isEmpty())
        return;
      
      // sort children by current position
      if (isOrderSiblingsByPosition)  {
        currentNorth = fromRadian+(toRadian-fromRadian)/2+Geometry.HALF_RADIAN;
        Collections.sort(children, this);
      }
      
      // compare actual radians available vs allocation
      double sizeOfChildren = 0;
      for (Object child : children) {
        sizeOfChildren += root2size.get(child).doubleValue();
      }
      radius += distanceBetweenGenerations;
      double shareFactor = (toRadian-fromRadian) * radius / sizeOfChildren;

      // check for extra radians/2 we can skip
      if ( shareFactor>1 && !isFanOut) {  
        if (backtrack!=null)
          fromRadian += ((toRadian-fromRadian) - sizeOfChildren/radius) / 2;
        shareFactor = 1;
      }
      
      // position children and iterate into their placement recursion
      int depth = 0;
      for (Vertex child : children) {
        
        double radians = root2size.get(child).doubleValue() / radius * shareFactor;
        
        layout.setPositionOfVertex(child, getPoint(center, fromRadian + radians/2, radius));
        
        if (debug!=null) {
          debug.add(new Line2D.Double(getPoint(center, fromRadian, radius - distanceBetweenGenerations/2), getPoint(center, fromRadian, radius+distanceBetweenGenerations/2)));
          debug.add(new Line2D.Double(getPoint(center, fromRadian+radians, radius - distanceBetweenGenerations/2), getPoint(center, fromRadian+radians, radius+distanceBetweenGenerations/2)));
        }
        
        layout(root, child, fromRadian, fromRadian+radians, radius);
        
        fromRadian += radians;
      }

      // done
    }
    
    /** compare two verticies' current position */
    public int compare(Vertex v1, Vertex v2) {
      
      double r1 = getRadian(getDelta(center,layout.getPositionOfVertex(v1)));
      double r2 = getRadian(getDelta(center,layout.getPositionOfVertex(v2)));
      
      if (r1>currentNorth)
        r1 -= ONE_RADIAN;
      if (r2>currentNorth)
        r2 -= ONE_RADIAN;
      
      if (r1<r2) 
        return -1;
      if (r1>r2)
        return 1;
      return 0;
    }
    
  } //Recursion
  
} //RadialLayoutAlgorithm
