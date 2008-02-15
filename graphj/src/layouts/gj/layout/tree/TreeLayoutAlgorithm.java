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
import gj.geom.Path;
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;
import gj.util.EdgeLayoutHelper;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Vertex layout for Trees
 */
public class TreeLayoutAlgorithm implements LayoutAlgorithm {

  /** distance of nodes in generation */
  private int distanceInGeneration = 20;

  /** distance of nodes between generations */
  private int distanceBetweenGenerations = 20;

  /** the alignment of parent over its children */
  private double alignmentOfParent = 0.5;

  /** whether children should be balanced or simply stacked */
  private boolean isBalanceChildren = false;

  /** whether arcs are direct or bended */
  private boolean isBendArcs = false;

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
  public Vertex getRoot() {
    return rootOfTree;
  }

  /**
   * Getter - root node
   */
  public void setRoot(Vertex root) {
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
    Shape result = layout(graph, null, rootOfTree, layout).shape;
    if (debugShapes!=null) {
      debugShapes.add(result);
    }
    
    // done
    return result;
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
    
    // create merged branch of sub-branches
    return new Branch(graph, backtrack, root, children.toArray(new Vertex[children.size()]), layout);

  }
  
  /**
   * A Branch is the recursively worked on part of the tree
   */
  private class Branch extends Geometry implements Comparator<Vertex> {
    
    /** tree */
    private Graph graph;
    private Layout2D layout;
    
    /** root of branch */
    private Vertex root;
    
    /** contained vertices */
    private List<Vertex> vertices = new ArrayList<Vertex>();
    
    /** shape of branch */
    private Point2D top;
    private GeneralPath shape;
    
    /** constructor for a leaf */
    Branch(Graph graph, Vertex leaf, Layout2D layout) {
      this.graph = graph;
      this.layout = layout;
      this.root = leaf;
      vertices.add(leaf);
      Point2D pos = layout.getPositionOfVertex(leaf);
      shape = new GeneralPath(getConvexHull(
          layout.getShapeOfVertex(leaf).getPathIterator(
          AffineTransform.getTranslateInstance(pos.getX(), pos.getY()))
      ));
      
    }

    /** constructor for a parent and its children */
    Branch(Graph graph, Vertex backtrack, Vertex parent, Vertex[] children, Layout2D layout) {

      this.graph = graph;
      this.layout = layout;
      
      double layoutAxis = getRadian(orientation);
      
      // sort by current position
      if (isOrderSiblingsByPosition) 
        Arrays.sort(children, this);
      
      // keep track of root and vertices
      root = parent;
      vertices.add(root);
      
      // recurse into children and take over descendants
      Branch[] branches = new Branch[children.length];
      for (int i=0;i<children.length;i++) {
        
        branches[i] = layout(graph, parent, children[i], layout);
        
        vertices.addAll(branches[i].vertices);

      }
      
      // Calculate deltas of children left-aligned
      double lrAlignment = layoutAxis - QUARTER_RADIAN;
      Point2D[] lrDeltas  = new Point2D[children.length];
      lrDeltas[0] = new Point2D.Double();
      for (int i=1;i<children.length;i++) {
        // calculate delta from top alignment position
        lrDeltas[i] = getDelta(branches[i].top(), branches[0].top());
        // calculate distance from all previous siblings
        double distance = Double.MAX_VALUE;
        for (int j=0;j<i;j++) {
          distance = Math.min(distance, getDistance(getTranslated(branches[j].shape, lrDeltas[j]), getTranslated(branches[i].shape, lrDeltas[i]), lrAlignment) - distanceInGeneration);
        }
        // calculate delta from top aligned position with correct distance
        lrDeltas[i] = getPoint(lrDeltas[i], lrAlignment, -distance);
      }
      
      // place last child
      branches[branches.length-1].moveBy(lrDeltas[lrDeltas.length-1]);

      // Calculate deltas of children right-aligned
      Point2D[] rlDeltas  = lrDeltas;
      if (children.length>2 && isBalanceChildren) {
        rlDeltas = new Point2D[children.length];
        double rlAlignment = layoutAxis + QUARTER_RADIAN;
        rlDeltas [rlDeltas.length-1] = new Point2D.Double();
        for (int i=rlDeltas.length-2;i>=0;i--) {
          // calculate delta from top alignment position
          rlDeltas[i] = getDelta(branches[i].top(), branches[branches.length-1].top());
          // calculate distance from all previous siblings
          double distance = Double.MAX_VALUE;
          for (int j=rlDeltas.length-1;j>i;j--) {
            distance = Math.min(distance, getDistance(getTranslated(branches[j].shape, rlDeltas[j]), getTranslated(branches[i].shape, rlDeltas[i]), rlAlignment) - distanceInGeneration);
          }
          assert distance != Double.MAX_VALUE;
          // calculate delta from top aligned position with correct distance
          rlDeltas[i] = getPoint(rlDeltas[i], rlAlignment, -distance);
        }
      }
      
      // place all children in between
      for (int i=1; i<children.length-1; i++) {
        branches[i].moveBy(getMid(lrDeltas[i], rlDeltas[i]));
      }
      
      
      // Place Root
      //
      //        rrr
      //        r r  
      //     b  rrr  c
      //     |   |   |
      //     |  =f=  |
      //     |   |   |
      //   --+---e---+--a
      //   11|11 | NN|NN    
      //   1 | 1.d.N | N
      //   11|11 | NN|NN
      //    /|\  |  /|\
      //
      //
      Point2D a = branches[0].top();
      Point2D b = layout.getPositionOfVertex(branches[0].root);
      Point2D c = layout.getPositionOfVertex(branches[branches.length-1].root);
      Point2D d = getPoint(b, c, alignmentOfParent); 
      Point2D e = getIntersection(a, layoutAxis-QUARTER_RADIAN, d, layoutAxis - HALF_RADIAN);
      Point2D f = getPoint(e, layoutAxis-HALF_RADIAN, distanceBetweenGenerations/2);
      
      Point2D r = getPoint(
          e, layoutAxis - HALF_RADIAN, 
          distanceBetweenGenerations + getLength(getMax(shape(root), layoutAxis)) 
        );
      
      layout.setPositionOfVertex(root, r);
      
      // calculate new shape
      GeneralPath gp = new GeneralPath();
      gp.append(layout.getShapeOfVertex(root), false);
      gp.transform(AffineTransform.getTranslateInstance(r.getX(), r.getY()));
      for (Branch branch : branches)
        gp.append(branch.shape, false);
      shape = new GeneralPath(getConvexHull(gp));
      
      // layout edges
      for (Edge edge : graph.getEdges(root)) {
        
        // don't do edge to backtrack
        if (ModelHelper.contains(edge, backtrack))
          continue;

        Path path;
        if (isBendArcs) {
          // calc edge layout
          Point2D[] points;
          if (edge.getStart().equals(parent)) {
            Point2D g = getIntersection(f, layoutAxis-QUARTER_RADIAN, pos(edge.getEnd()), layoutAxis);
            points = new Point2D[]{ pos(edge.getStart()), f, g, pos(edge.getEnd()) };
          } else {
            Point2D g = getIntersection(f, layoutAxis-QUARTER_RADIAN, pos(edge.getStart()), layoutAxis);
            points = new Point2D[]{ pos(edge.getStart()), g, f, pos(edge.getEnd()) };
          }
          path = EdgeLayoutHelper.getPath(points, shape(edge.getStart()), shape(edge.getEnd()));
          
        } else {
          path = EdgeLayoutHelper.getPath(edge, layout);
        }
        layout.setPathOfEdge(edge, path);
      }

      // done
    }
    
    Point2D top() {
      if (top==null)
        top= getMax(shape, getRadian(orientation) - HALF_RADIAN);
      return top;
      
    }
    
    Point2D pos() {
      return pos(root);
    }
    
    Point2D pos(Vertex vertex) {
      return layout.getPositionOfVertex(vertex);
    }
    
    Shape shape(Vertex vertex) {
      return layout.getShapeOfVertex(vertex);
    }
    
    Vertex other(Edge edge, Vertex other) {
      return edge.getStart().equals(other) ? edge.getEnd() : edge.getStart();
    }
    
    
    /** translate a branch */
    void moveBy(Point2D delta) {
      
      for (Vertex vertice : vertices) 
        ModelHelper.translate(layout, vertice, delta);
      
      top = null;
      
      shape.transform(AffineTransform.getTranslateInstance(delta.getX(), delta.getY()));
    }
    
    /** translate a branch */
    void moveTo(Point2D pos) {
      moveBy(getDelta(layout.getPositionOfVertex(root), pos));
    }

    /** compare positions of two verticies */
    public int compare(Vertex v1,Vertex v2) {
      
      double layoutAxis = getRadian(orientation);
      Point2D p1 = layout.getPositionOfVertex(v1);
      Point2D p2 = layout.getPositionOfVertex(v2);
      
      double delta =
        Math.cos(layoutAxis) * (p2.getX()-p1.getX()) + Math.sin(layoutAxis) * (p2.getY()-p1.getY());
      
      return (int)(delta);
    }
  } //Branch
  
} //TreeLayout
