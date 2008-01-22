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
import gj.layout.GraphNotSupportedException;
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.model.Graph;
import gj.model.Tree;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A radial layout for Trees
 */
public class RadialLayoutAlgorithm extends AbstractLayoutAlgorithm implements LayoutAlgorithm {
  
  private Object rootOfTree;
  private double distanceOfGeneration = 60;
  private boolean isAdjustDistances = true;
  private boolean isFanOut = false; 

  /**
   * Accessor - root node
   */
  public Object getRootOfTree() {
    return rootOfTree;
  }

  /**
   * Accessor - root node
   */
  public void setRootOfTree(Object root) {
    this.rootOfTree = root;
  }
  
  /**
   * Accessor - distance of generations
   */
  public void setDistanceOfGeneration(double distanceOfGeneration) {
    this.distanceOfGeneration = distanceOfGeneration;
  }

  /**
   * Accessor - distance of generations
   */
  public double getDistanceOfGeneration() {
    return distanceOfGeneration;
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
   * Layout a layout capable graph
   */
  public Shape apply(Graph graph, Layout2D layout, Rectangle2D bounds) throws LayoutAlgorithmException {
    
    // check that we got a tree
    if (!(graph instanceof Tree))
      throw new GraphNotSupportedException("only trees allowed", Tree.class);
    Tree tree = (Tree)graph;
    
    // ignore an empty tree
    Set<?> verticies = tree.getVertices();
    if (verticies.isEmpty())
      return bounds;
    
    // check root
    if (rootOfTree==null || !verticies.contains(rootOfTree)) 
      rootOfTree = verticies.iterator().next();
    
//    // calculate circumference of generations
//    double[] generation2circumference = new double[verticies.size()];
//    calcCircumferences(tree, null, rootOfTree, 0, generation2circumference, layout);
//    for (int i=1;i<generation2circumference.length;i++)
//      generation2circumference[i] = Math.max( generation2circumference[i-1] + i*10*Geometry.ONE_RADIAN,generation2circumference[i]);
    
    // calculate sub-tree angular share
    Map<Object, Double> root2share = new HashMap<Object, Double>();
    calcAngularShare(tree, null, rootOfTree, 0, root2share, layout);

    // layout
    layout(tree, null, rootOfTree, layout.getPositionOfVertex(tree, rootOfTree), 0, Geometry.ONE_RADIAN, 0, root2share, layout);
    
    // done
    return ModelHelper.getBounds(graph, layout);
  }
  
  /**
   * calculate the diameter of a node's shape
   */
  private double getDiameter(Tree tree, Object vertex, Layout2D layout) {
    return Geometry.getMaximumDistance(new Point2D.Double(0,0), layout.getShapeOfVertex(tree, vertex)) * 2;
  }

  
  /**
   * calculate the anglular share for a root
   */
  private double calcAngularShare(Tree tree, Object backtrack, Object root, int generation, Map<Object, Double> root2share, Layout2D layout) {

    // calculate angular share required for children
    Set<?> neighbours = tree.getNeighbours(root);
    double shareOfChildren = 0;
    for (Object child : neighbours) {
      if (!child.equals(backtrack)) 
        shareOfChildren += calcAngularShare(tree, root, child, generation+1, root2share, layout);
    }

    // calculate angular share required for root
    double shareOfRoot = 0;
    if (generation!=0) {
      // radian = diameter / (2PI*r) * 2PI
      shareOfRoot = getDiameter(tree, root, layout) / (generation*distanceOfGeneration) ;
    }
    
    // keep and return
    double result = Math.max( shareOfChildren, shareOfRoot);
    root2share.put(root, new Double(result));
    return result;
  }
  
  /**
   * recursive layout call
   */
  private void layout(Graph tree, Object backtrack, Object root, Point2D center, double fromRadian, double toRadian, double radius, Map<Object, Double> root2share, Layout2D layout) {
    
    // assemble list of children
    Set<?> neighbours = tree.getNeighbours(root);
    List<Object> children = new ArrayList<Object>(neighbours.size());
    for (Object child : neighbours) 
      if (!child.equals(backtrack)) children.add(child);
    if (children.isEmpty())
      return;
    
    // calculate how much angular each child can get now that we have actual from/to
    double factor = (toRadian-fromRadian) / root2share.get(root).doubleValue();
    if (factor<1 && isAdjustDistances) 
      System.out.println("TODO:adjusting "+root+"'s distance");
    if (factor>1 && !isFanOut)  
      factor = 1;

    // position and recurse
    for (int c = 0; c<children.size(); c++) {
      
      Object child = children.get(c);
      
      double share = root2share.get(child).doubleValue() * factor;
      
      layout.setPositionOfVertex(tree, child, new Point2D.Double(
          center.getX() + Math.sin(fromRadian + share/2) * (radius+distanceOfGeneration),
          center.getY() - Math.cos(fromRadian + share/2) * (radius+distanceOfGeneration)
          ));
      
      layout(tree, root, child, center, fromRadian, fromRadian+share, radius+distanceOfGeneration, root2share, layout);
      
      fromRadian += share;
    }

    // done
  }
  
} //TreeLayout
