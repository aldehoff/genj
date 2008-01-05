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
package gj.layout.tree;

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
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A 'simple' tree layout for Trees
 */
public class TreeLayoutAlgorithm extends AbstractLayoutAlgorithm implements LayoutAlgorithm {

  /** distance of nodes in generation */
  private int distanceInGeneration = 20;

  /** distance of nodes between generations */
  private int distanceBetweenGenerations = 20;

  /** the alignment of parent over its children */
  private double alignmentOfParent = 0.5;

  /** the alignment of a generation */
  private double alignmentOfGeneration = 0;

  /** whether children should be balanced or simply stacked */
  private boolean isBalanceChildren = true;

  /** whether arcs are direct or bended */
  private boolean isBendArcs = true;

  /** orientation in degrees 0-359 */
  private double orientation = 180;

  /**
   * Getter - distance of nodes in generation
   */
  public int getDistanceInGeneration() {
    return distanceInGeneration;
  }

  /**
   * Setter - distance of nodes in generation
   */
  public void setDistanceInGeneration(int set) {
    distanceInGeneration = set;
  }

  /**
   * Getter - distance of nodes between generations
   */
  public int getDistanceBetweenGenerations() {
    return distanceBetweenGenerations;
  }

  /**
   * Setter - distance of nodes between generations
   */
  public void setDistanceBetweenGenerations(int set) {
    distanceBetweenGenerations=set;
  }

  /**
   * Getter - the alignment of parent over its children
   */
  public double getAlignmentOfParent() {
    return alignmentOfParent;
  }

  /**
   * Setter - the alignment of parent over its children
   */
  public void setAlignmentOfParent(double set) {
    alignmentOfParent = set;
  }

  /**
   * Getter - the alignment of a generation
   */
  public double getAlignmentOfGeneration() {
    return alignmentOfGeneration;
  }

  /**
   * Setter - the alignment of a generation
   */
  public void setAlignmentOfGeneration(double set) {
    alignmentOfGeneration = set;
  }

  /**
   * Getter - whether children are balanced optimally 
   * (spacing them apart where necessary) instead of
   * simply stacking them. Example
   * <pre>
   *      A                A
   *    +-+---+         +--+--+
   *    B C   D   -->   B  C  D
   *  +-+-+ +-+-+     +-+-+ +-+-+
   *  E F G H I J     E F G H I J
   * </pre>
   */
  public boolean getBalanceChildren() {
    return isBalanceChildren;
  }

  /**
   * Setter 
   */
  public void setBalanceChildren(boolean set) {
    isBalanceChildren=set;
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
   * Setter - which orientation to use
   * @param orientation value between 0 and 360 degree
   */
  public void setOrientation(double orientation) {
    this.orientation = orientation;
  }
  
  /**
   * Getter - which orientation to use
   * @return value between 0 and 360 degree
   */
  public double getOrientation() {
    return orientation;
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
    if (tree.getNumVertices()==0)
      return bounds;
    
    // recurse into it
    Branch branch = layout(tree, layout, null, tree.getRoot());
    
    // done
    return branch.shape;
  }
  
  /**
   * Resolve layout axis in radian (0 bottom up, PI top down, ...)
   */
  private double getLayoutAxis() {
    return orientation/360*Geometry.ONE_RADIAN;
  }
  
  /**
   * Layout a branch
   */
  private Branch layout(Tree tree, Layout2D layout, Object parent, Object root) {
    
    // check children
    int children = tree.getNumAdjacentVertices(root) + (parent!=null ? -1 : 0); 

    // a leaf is easy
    if (children==0) 
      return new Branch(root, layout);
    
    // loop over all children - a branch for each
    Branch[] branches = new Branch[children];
    int b = 0;
    Point2D.Double pos = new Point2D.Double();
    for (Object child : tree.getAdjacentVertices(root) ) {

      // don't return
      if (child==parent)
        continue;
      
      // create the branch
      branches[b] = layout(tree, layout, root, child);

      // position alongside previous
      if (b>0) 
        branches[b].moveTo(layout, branches[b-1]);
      
      // next
      b++;
    }
    
    // create a branch for parent and branches
    return new Branch(root, layout, branches);

  }
  
  /**
   * A Branch is the recursively worked on part of the tree
   */
  private class Branch extends Geometry {
    
    /** root of branch */
    private Object root;
    
    /** contained vertices */
    private List<Object> vertices = new ArrayList<Object>();
    
    /** shape of branch */
    private GeneralPath shape;
    
    /** constructor for a leaf */
    Branch(Object root, Layout2D layout) {
      this.root = root;
      vertices.add(root);
      shape = new GeneralPath(layout.getShapeOfVertex(root));
      Point2D pos = layout.getPositionOfVertex(root);
      shape.transform(AffineTransform.getTranslateInstance(pos.getX(), pos.getY()));
    }

    /** constructor for a branch of sub-branches */
    Branch(Object root, Layout2D layout, Branch[] branches) {
      
      // calculate where to place root
      //  c = center between 1st and nth child
      //  t = topmost point of children
      //  ct = topmost point centered between children 
      //
      //  m = maximum extend of root shape
      //  b = bottom of root shape
      //  d = distance that root needs from ct 
      double layoutAxis = getLayoutAxis();
      Point2D c = getPoint(layout.getPositionOfVertex(branches[0].root), layout.getPositionOfVertex(branches[branches.length-1].root));
      Point2D t = getMax(branches[0].shape, layoutAxis - HALF_RADIAN);
      Point2D ct = getIntersection(t, layoutAxis-QUARTER_RADIAN, c, layoutAxis);
      
      Point2D m = getMax(layout.getShapeOfVertex(root), layoutAxis);
      Point2D b = getIntersection(m, layoutAxis-QUARTER_RADIAN, new Point2D.Double(), layoutAxis);
      
      double d = getLength(b) + distanceBetweenGenerations;

      Point2D r = getPoint(ct, layoutAxis-HALF_RADIAN, d);
      
      // set us up
      this.root = root;
      vertices.add(root);
      for (Branch branch : branches) 
        vertices.addAll(branch.vertices);
      layout.setPositionOfVertex(root, r);
      shape = new GeneralPath(layout.getShapeOfVertex(root));
      shape.transform(AffineTransform.getTranslateInstance(r.getX(), r.getY()));

      
      // done
    }
    
    /** shape of root */
    Shape getShapeOfRoot(Layout2D layout) {
      Point2D pos = layout.getPositionOfVertex(root);
      GeneralPath shape = new GeneralPath(layout.getShapeOfVertex(root));
      shape.transform(AffineTransform.getTranslateInstance(pos.getX(), pos.getY()));
      return shape;
    }
    
    /** translate a branch */
    void moveBy(Layout2D layout, Point2D delta) {
      for (Object vertice : vertices) 
        ModelHelper.translate(layout, vertice, delta);
      shape.transform(AffineTransform.getTranslateInstance(delta.getX(), delta.getY()));
    }
    
    /** translate a branch */
    void moveTo(Layout2D layout, Point2D pos) {
      moveBy(layout, getDelta(layout.getPositionOfVertex(root), pos));
    }
    
    /** move beside other branch */
    void moveTo(Layout2D layout, Branch other) {
      
      double layoutAxis = getLayoutAxis();
      double alignmentAxis = layoutAxis - QUARTER_RADIAN;

      // move on top of each other at point of respective maximum in reversed layout direction
      moveBy(layout, getDelta(
          getMax(shape, layoutAxis - HALF_RADIAN),
          getMax(other.shape, layoutAxis - HALF_RADIAN)
      ));          
      
      // calculate distance in alignment axis + padding
      double distance = getDistance(other.shape, shape, alignmentAxis ) - distanceInGeneration;
      
      // move it
      moveBy(layout, new Point2D.Double(-Math.sin(alignmentAxis) * distance, Math.cos(alignmentAxis) * distance));
      
    }
    
    
  } //Branch
  
} //TreeLayout
