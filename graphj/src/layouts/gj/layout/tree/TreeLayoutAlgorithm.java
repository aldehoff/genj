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

import gj.layout.AbstractLayoutAlgorithm;
import gj.layout.GraphNotSupportedException;
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.model.Graph;
import gj.model.Tree;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Set;

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
  private double orientation = 0;

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
    if (tree.getVertices().isEmpty())
      return bounds;
    
    // recurse into it
    Branch branch = layout(tree, layout, null, tree.getRoot());
    
    // done
    return branch.shape;
  }
  
  /**
   * Layout a branch
   */
  private Branch layout(Tree tree, Layout2D layout, Object parent, Object root) {
    
    // check children
    Set<?> children = tree.getAdjacentVertices(root);
    children.remove(parent);

    // a leaf is easy
    if (children.isEmpty()) 
      return new Branch(root, layout.getShapeOfVertex(root));
    
    // create a branch for each child
    Branch[] branches = new Branch[children.size()];
    Iterator<?> it = children.iterator();
    Point2D.Double pos = new Point2D.Double();
    for (int b=0;b<branches.length;b++) {
      Object child = it.next();
      branches[b] = layout(tree, layout, root, child);
      if (b==0) {
        pos.setLocation(layout.getPositionOfVertex(child));
      } else {
        // FIXME
      }
    }
    
    // merge branches
    return new Branch(root, layout.getShapeOfVertex(root), branches);

  }

  /**
   * A Branch is the recursively worked on part of the tree
   */
  private class Branch {
    
    /** root of branch */
    private Object root;

    /** shape of branch */
    private Shape shape;
    
    /** constructor for a leaf */
    Branch(Object root, Shape shape) {
      this.root = root;
      this.shape = shape;
    }

    /** constructor for a branch of sub-branches */
    Branch(Object root, Shape shape, Branch[] branches) {
      // FIXME
    }
    
  } //Branch
  
} //TreeLayout
