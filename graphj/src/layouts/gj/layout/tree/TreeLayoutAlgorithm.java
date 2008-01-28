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
import gj.geom.ShapeHelper;
import gj.layout.AbstractLayoutAlgorithm;
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.model.Graph;
import gj.model.Vertex;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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

  /** whether children should be balanced or simply stacked */
  private boolean isBalanceChildren = true;

  /** whether arcs are direct or bended */
  private boolean isBendArcs = true;

  /** orientation in degrees 0-359 */
  private double orientation = 180;

  /** root to start with */
  private Vertex rootOfTree;

  /** whether to order by position instead of natural sequence */
  private boolean isOrderSiblingsByPosition = true;

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
    alignmentOfParent = Math.max(0, Math.min(1,set));
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
   * Getter - root node
   */
  public Vertex getRootVertex() {
    return rootOfTree;
  }

  /**
   * Getter - root node
   */
  public void setRootVertex(Vertex root) {
    this.rootOfTree = root;
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
    if (rootOfTree==null || !verticies.contains(rootOfTree)) 
      rootOfTree = verticies.iterator().next();

    // recurse into it
    return layout(graph, null, rootOfTree, layout).area;
  }
  
  /**
   * Layout a branch
   */
  private Branch layout(Graph graph, Vertex backtrack, Vertex root, Layout2D layout) {
    
    // check # children in neighbours (we don't count backtrack as child) - leaf?
    Collection<Vertex> children = ModelHelper.getNeighbours(graph, root);
    children.remove(backtrack);
    if (children.isEmpty())
      return new Branch(graph, root, layout);
    
    // sort by current position
    if (isOrderSiblingsByPosition) {
      Vertex[] tmp = children.toArray(new Vertex[children.size()]);
      Arrays.sort(tmp, new ComparePositions(graph, layout));
      children = Arrays.asList(tmp);
    }
    
    // create merged branch of sub-branches
    return new Branch(graph, root, children, layout);

  }
  
  /**
   * A comparator for comparing sibling vertices by their position
   */
  private class ComparePositions extends Geometry implements Comparator<Vertex> {

    private Layout2D layout;
    private Graph graph;
    
    ComparePositions(Graph graph, Layout2D layout) {
      this.graph = graph;
      this.layout = layout;
    }
    
    public int compare(Vertex v1,Vertex v2) {
      double layoutAxis = getRadian(orientation);
      Point2D p1 = layout.getPositionOfVertex(v1);
      Point2D p2 = layout.getPositionOfVertex(v2);
      
      double delta =
        Math.cos(layoutAxis) * (p2.getX()-p1.getX()) + Math.sin(layoutAxis) * (p2.getY()-p1.getY());
      
      return (int)(delta);
    }
  }

  /**
   * A Branch is the recursively worked on part of the tree
   */
  private class Branch extends Geometry {
    
    /** tree */
    private Graph graph;
    
    /** root of branch */
    private Vertex root;
    
    /** contained vertices */
    private List<Vertex> vertices = new ArrayList<Vertex>();
    
    /** shape of branch */
    private Area area;
    
    /** constructor for a leaf */
    Branch(Graph graph, Vertex leaf, Layout2D layout) {
      this.root = leaf;
      vertices.add(leaf);
      area = new Area(layout.getShapeOfVertex(leaf));
      Point2D pos = layout.getPositionOfVertex(leaf);
      area.transform(AffineTransform.getTranslateInstance(pos.getX(), pos.getY()));
    }

    /** constructor for a parent and its children */
    Branch(Graph graph, Vertex parent, Collection<Vertex> children, Layout2D layout) {

      double layoutAxis = getRadian(orientation);
      double alignmentAxis = layoutAxis - QUARTER_RADIAN;
      
      // keep track of root and vertices
      root = parent;
      vertices.add(root);
      
      // create a branch for each child and place beside siblings
      LinkedList<Branch> branches = new LinkedList<Branch>();
      for (Vertex child : children)  {
        
        // another branch
        Branch next = layout(graph, parent, child, layout) ; 
        
        // add its vertices to ours
        vertices.addAll(next.vertices);

        // place child n+1 beside 1..n
        if (!branches.isEmpty()) {

          // move top aligned respective to reversed layout direction ("top-aligned")
          next.moveBy(layout, getDelta( getMax(next.area, layoutAxis - HALF_RADIAN), getMax(branches.getLast().area, layoutAxis - HALF_RADIAN) ));          
          
          // calculate distance in alignment axis + padding
          double distance = Double.MAX_VALUE;
          for (Branch prev : branches) {
            distance = Math.min(distance, getDistance(prev.area, next.area, alignmentAxis ) - distanceInGeneration);
          }
          
          // move it
          next.moveBy(layout, new Point2D.Double(-Math.sin(alignmentAxis) * distance, Math.cos(alignmentAxis) * distance));
        }
        
        // next
        branches.add(next);
      }

      // calculate where to place root
      //  f = pos of first child 
      //  l = pos of last child
      //  c = pos of parent in generation axis
      //  ct = topmost point centered between children 
      //
      //  m = maximum extend of root shape in direction of layout 
      //  b = bottom of root shape
      //  d = distance that root needs from ct 
      Point2D f = layout.getPositionOfVertex(branches.getFirst().root);
      Point2D l = layout.getPositionOfVertex(branches.getLast().root);
      Point2D c = new Point2D.Double(
          f.getX() + (l.getX()-f.getX())*alignmentOfParent,
          f.getY() + (l.getY()-f.getY())*alignmentOfParent
        );
      
      Point2D ct = getIntersection(
          getMax(branches.getFirst().area, layoutAxis - HALF_RADIAN), layoutAxis-QUARTER_RADIAN, 
          c, layoutAxis
        );
      
      Point2D m = getMax(layout.getShapeOfVertex(root), layoutAxis);
      Point2D b = getIntersection(m, layoutAxis-QUARTER_RADIAN, new Point2D.Double(), layoutAxis);
      double d = getLength(b) + distanceBetweenGenerations;
      Point2D r = getPoint(ct, layoutAxis-HALF_RADIAN, d);
      
      // place root
      layout.setPositionOfVertex(root, r);
      
      // calculate new area with shape of root
      area = new Area(layout.getShapeOfVertex(root));
      area.transform(AffineTransform.getTranslateInstance(r.getX(), r.getY()));
      
      // .. an umbrella between root and first/last child
      area.add(new Area(ShapeHelper.createShape(
          r, 
          getMax(branches.getFirst().getShapeOfRoot(layout), layoutAxis+QUARTER_RADIAN),
          getMax(branches.getLast().getShapeOfRoot(layout), layoutAxis-QUARTER_RADIAN),
          r)));

      // .. and each sub-branch
      for (Branch branch : branches)
        area.add(branch.area);
      
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
      for (Vertex vertice : vertices) 
        ModelHelper.translate(graph, layout, vertice, delta);
      area.transform(AffineTransform.getTranslateInstance(delta.getX(), delta.getY()));
    }
    
    /** translate a branch */
    void moveTo(Layout2D layout, Point2D pos) {
      moveBy(layout, getDelta(layout.getPositionOfVertex(root), pos));
    }
    
  } //Branch
  
} //TreeLayout
