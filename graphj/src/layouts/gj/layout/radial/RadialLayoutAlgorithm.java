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
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;
import gj.util.DefaultEdge;
import gj.util.EdgeLayoutHelper;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A radial layout for Trees
 */
public class RadialLayoutAlgorithm implements LayoutAlgorithm {
  
  private WeakReference<Vertex> rootOfTree;
  private double distanceBetweenGenerations = 60;
  private boolean isAdjustDistances = false;
  private boolean isFanOut = false; 
  private double distanceInGeneration = 0;
  private boolean isOrderSiblingsByPosition = true;
  private Map<Edge, Integer> edge2length = new HashMap<Edge, Integer>();

  /**
   * Accessor - root node
   */
  public Vertex getRoot() {
    return rootOfTree != null ? rootOfTree.get() : null;
  }

  /**
   * Accessor - root node
   */
  public void setRoot(Vertex root) {
    rootOfTree = new WeakReference<Vertex>(root);
  }
  
  /**
   * Accessor - number of generations an edge spans 
   * @edge the edge to set the length for
   * @length length in generations (great equals 1)
   */
  public void setLength(Edge edge, int length) {
    if (length < 1)
      edge2length.remove(edge);
    else
      edge2length.put(edge, new Integer(length));
  }
  
  /**
   * Accessor - number of generations an edge spans 
   */
  public int getLength(Edge edge) {
    Integer result = edge2length.get(edge);
    if (result!=null)
      return result.intValue();
    return 1;
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
    if (graph.getNumVertices()==0)
      return bounds;
    
    // check root
    Vertex root = getRoot();
    if (root==null) {
      root = graph.getVertices().iterator().next();
      setRoot(root);
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
    Map<Vertex, Double> vertex2radians = new HashMap<Vertex, Double>();
    Point2D center;
    double currentNorth;
    double distanceBetweenGenerations;
    double maxDiameter;
    
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
     * calculate the distance of two vertices (1+)
     */
    int getLengthOfEdge(Vertex a, Vertex b) {
      Integer result = edge2length.get(new DefaultEdge(a,b));
      if (result!=null)
        return result.intValue();
      result = edge2length.get(new DefaultEdge(b,a));
      if (result!=null)
        return result.intValue();
      return 1;
    }

    
    /**
     * calculate the size of a sub-tree starting at root
     */
    double getSize(Vertex backtrack, Vertex root, int generation) {
      
      // update our depth info
      depth = Math.max(depth, generation+1);

      // calculate size for children
      Set<? extends Vertex> neighbours = ModelHelper.getNeighbours(graph, root);
      double radiansOfChildren = 0;
      for (Vertex child : neighbours) {
        if (!child.equals(backtrack)) {
          int generationOfChild = generation + getLengthOfEdge(root,child);
          radiansOfChildren +=  getSize(root, child, generationOfChild);
        }
      }
      
      // root?
      if (generation==0) {
        double reqDistanceBetweenGenerations = Math.max(
            maxDiameter,
            radiansOfChildren / ONE_RADIAN * distanceBetweenGenerations
            );
        if (isAdjustDistances && reqDistanceBetweenGenerations>distanceBetweenGenerations)
          distanceBetweenGenerations = reqDistanceBetweenGenerations;
        return 0;
      }
      
      // calculate size root
      double diamOfRoot = ModelHelper.getDiameter(root, layout);
      maxDiameter = Math.max(maxDiameter, diamOfRoot);
      double radiansOfRoot = ( diamOfRoot + distanceInGeneration ) / (generation*distanceBetweenGenerations);
      
      // keep and return
      double result = Math.max( radiansOfChildren, radiansOfRoot);
      vertex2radians.put(root, new Double(result));
      
      return result;
    }
    
    /**
     * recursive layout call
     */
    void layout(Vertex backtrack, Vertex root, double fromRadian, double toRadian, double radius) {
      
      // assemble list of children
      Collection<Vertex> children = ModelHelper.getNeighbours(graph, root);
      children.remove(backtrack);
      if (children.isEmpty())
        return;
      
      // sort children by current position
      if (isOrderSiblingsByPosition)  {
        currentNorth = fromRadian+(toRadian-fromRadian)/2+Geometry.HALF_RADIAN;
        Vertex[] tmp = children.toArray(new Vertex[children.size()]);
        Arrays.sort(tmp, this);
        children = Arrays.asList(tmp);
      }
      
      // compare actual radians available vs allocation
      double radiansOfChildren = 0;
      for (Vertex child : children) {
        //double radiusOfChild = radius + getLengthOfEdge(root, child) * distanceBetweenGenerations;
        radiansOfChildren += vertex2radians.get(child).doubleValue();
      }
      double shareFactor = (toRadian-fromRadian) / radiansOfChildren;

      // check for extra radians/2 we can skip
      if ( shareFactor>1 && !isFanOut) {  
        if (backtrack!=null)
          fromRadian += ((toRadian-fromRadian) - radiansOfChildren) / 2;
        shareFactor = 1;
      }
      
      // position children and iterate into their placement recursion
      for (Vertex child : children) {
        
        double radiusOfChild = radius + getLengthOfEdge(root, child) * distanceBetweenGenerations;
        double radiansOfChild = vertex2radians.get(child).doubleValue() * shareFactor;
        layout.setPositionOfVertex(child, getPoint(center, fromRadian + radiansOfChild/2, radiusOfChild ));
        
        for (Edge edge : graph.getEdges(root)) {
          if (!ModelHelper.contains(edge, backtrack))
            EdgeLayoutHelper.setPath(edge, layout);
        }
        
        if (debug!=null) {
          debug.add(new Line2D.Double(getPoint(center, fromRadian, radiusOfChild - distanceBetweenGenerations/2), getPoint(center, fromRadian, radiusOfChild+distanceBetweenGenerations/2)));
          debug.add(new Line2D.Double(getPoint(center, fromRadian+radiansOfChild, radiusOfChild - distanceBetweenGenerations/2), getPoint(center, fromRadian+radiansOfChild, radiusOfChild+distanceBetweenGenerations/2)));
        }
        
        layout(root, child, fromRadian, fromRadian+radiansOfChild, radiusOfChild);
        
        fromRadian += radiansOfChild;
      }

      // done
    }
    
    /** compare two vertices' current position */
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
