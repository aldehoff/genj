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
import java.util.List;
import java.util.Set;

/**
 * A radial layout for Trees
 */
public class RadialLayoutAlgorithm extends AbstractLayoutAlgorithm implements LayoutAlgorithm {
  
  private Object rootOfTree;
  private double distanceOfGeneration = 60;

  /**
   * Getter - root node
   */
  public Object getRootOfTree() {
    return rootOfTree;
  }

  /**
   * Getter - root node
   */
  public void setRootOfTree(Object root) {
    this.rootOfTree = root;
  }
  
  public void setDistanceOfGeneration(double distanceOfGeneration) {
    this.distanceOfGeneration = distanceOfGeneration;
  }

  public double getDistanceOfGeneration() {
    return distanceOfGeneration;
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

    layout(tree, null, rootOfTree, layout.getPositionOfVertex(tree, rootOfTree), 0, Geometry.ONE_RADIAN, getDistanceOfGeneration(), layout);
    
    return ModelHelper.getBounds(graph, layout);
  }
  
  /**
   * recursive layout call
   */
  private void layout(Graph tree, Object backtrack, Object root, Point2D center, double fromRadian, double toRadian, double radius, Layout2D layout) {
    
    // assemble list of children
    Set<?> neighbours = tree.getNeighbours(root);
    List<Object> children = new ArrayList<Object>(neighbours.size());
    for (Object child : neighbours) if (!child.equals(backtrack)) children.add(child);
    if (children.isEmpty())
      return;

    // calculate variables
    double share = (toRadian-fromRadian)/children.size();
    Point2D posRoot = layout.getPositionOfVertex(tree, root); 
    
    // position and recurse
    for (int c = 0; c<children.size(); c++) {
      
      Object child = children.get(c);
      
      double radian = fromRadian + share * c;
      
      layout.setPositionOfVertex(tree, child, new Point2D.Double(
          center.getX() + Math.sin(radian + share/2) * radius,
          center.getY() - Math.cos(radian + share/2) * radius
          ));

      layout(tree, root, child, center, radian, radian+share, radius+getDistanceOfGeneration(), layout);
    }

    // done
  }
  
} //TreeLayout
