/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2009 Nils Meier
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
package gj.layout.graph.tree;

import static gj.util.LayoutHelper.*;
import static gj.geom.Geometry.*;

import gj.layout.Graph2D;
import gj.layout.GraphNotSupportedException;
import gj.layout.LayoutContext;
import gj.layout.LayoutException;
import gj.model.Edge;
import gj.model.Vertex;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * A Branch is the recursively worked on part of the tree
 */
/*package*/ class Branch implements Comparator<Vertex> {
  
  /** tree */
  private Graph2D graph2d;
  
  /** root of branch */
  private Vertex root;
  
  /** contained branches */
  private List<Branch> branches;
  
  /** shape of branch */
  private Point2D top;
  private GeneralPath shape;
  private double orientation;
  
  /** constructor for a parent and its children */
  /*package*/ Branch(Vertex backtrack, Vertex parent, Graph2D graph2d, Deque<Vertex> stack, Set<Vertex> visited, TreeLayout layout, LayoutContext context) throws LayoutException, GraphNotSupportedException{
    
    // track coverage
    visited.add(parent);

    // init state
    this.graph2d = graph2d;
    this.root = parent;
    this.orientation = layout.getOrientation();
    
    // grab and sort children 
    List<Vertex> children = children(backtrack, parent, layout);
    if (layout.isOrderSiblingsByPosition()) 
      Collections.sort(children, this);
    
    // recurse into children and take over descendants
    stack.addLast(parent);
    branches = new ArrayList<Branch>(children.size());
    for (Vertex child : children) {
      
      // catch possible recurse step into already visited nodes
      if (visited.contains(child)) {
        
        // we don't allow directed cycles
        if (stack.contains(child)) {
          context.getLogger().info("cannot handle directed cycle at all");
          throw new GraphNotSupportedException("Graph contains cycle involving ["+parent+">"+child+"]");
        }

        // allowing acyclic graphs
        if (!layout.isSingleSourceDAG()) {
          context.getLogger().info("cannot handle undirected graph with cycle unless isConsiderDirection=true");
          throw new GraphNotSupportedException("Non Digraph contains non-directed cycle involving ["+parent+">"+child+"]");
        }
        
        // don't re-recurse into child - remember applicable edge(s)
        continue;
      }
      
      // recurse
      Branch branch = new Branch(parent, child, graph2d, stack, visited, layout, context);
      branches.add(branch);
    }
    stack.removeLast();
    
    // no children?
    if (branches.isEmpty()) {
      
      // simple shape for a leaf
      Point2D pos = graph2d.getPosition(parent);
      shape = new GeneralPath(getConvexHull(
          graph2d.getShape(parent).getPathIterator(
          AffineTransform.getTranslateInstance(pos.getX(), pos.getY()))
      ));
      top();
      
      // done
      return;
    }
    
    // Calculate deltas of children left-aligned
    double layoutAxis = getRadian(layout.getOrientation());
    double lrAlignment = layoutAxis - QUARTER_RADIAN;
    Point2D[] lrDeltas  = new Point2D[branches.size()];
    lrDeltas[0] = new Point2D.Double();
    for (int i=1;i<branches.size();i++) {
      // calculate delta from top alignment position
      lrDeltas[i] = getDelta(branches.get(i).top(), branches.get(0).top());
      // calculate distance from all previous siblings
      double distance = Double.MAX_VALUE;
      for (int j=0;j<i;j++) {
        distance = Math.min(distance, getDistance(getTranslated(branches.get(j).shape, lrDeltas[j]), getTranslated(branches.get(i).shape, lrDeltas[i]), lrAlignment) - layout.getDistanceInGeneration());
      }
      // calculate delta from top aligned position with correct distance
      lrDeltas[i] = getPoint(lrDeltas[i], lrAlignment, -distance);
    }
    
    // place last child
    branches.get(branches.size()-1).moveBy(lrDeltas[lrDeltas.length-1]);

    // Calculate deltas of children right-aligned
    Point2D[] rlDeltas  = lrDeltas;
    if (branches.size()>2 && layout.getBalanceChildren()) {
      rlDeltas = new Point2D[branches.size()];
      double rlAlignment = layoutAxis + QUARTER_RADIAN;
      rlDeltas [rlDeltas.length-1] = new Point2D.Double();
      for (int i=rlDeltas.length-2;i>=0;i--) {
        // calculate delta from top alignment position
        rlDeltas[i] = getDelta(branches.get(i).top(), branches.get(branches.size()-1).top());
        // calculate distance from all previous siblings
        double distance = Double.MAX_VALUE;
        for (int j=rlDeltas.length-1;j>i;j--) {
          distance = Math.min(distance, getDistance(getTranslated(branches.get(j).shape, rlDeltas[j]), getTranslated(branches.get(i).shape, rlDeltas[i]), rlAlignment) - layout.getDistanceInGeneration());
        }
        assert distance != Double.MAX_VALUE;
        // calculate delta from top aligned position with correct distance
        rlDeltas[i] = getPoint(rlDeltas[i], rlAlignment, -distance);
      }
    }
    
    // place all children in between
    for (int i=1; i<branches.size()-1; i++) {
      branches.get(i).moveBy(getMid(lrDeltas[i], rlDeltas[i]));
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
    Point2D a = branches.get(0).top();
    Point2D b = graph2d.getPosition(branches.get(0).root);
    Point2D c = graph2d.getPosition(branches.get(branches.size()-1).root);
    Point2D d = getPoint(b, c, layout.getAlignmentOfParent()); 
    Point2D e = getIntersection(a, layoutAxis-QUARTER_RADIAN, d, layoutAxis - HALF_RADIAN);
    
    Point2D r = getPoint(
        e, layoutAxis - HALF_RADIAN, 
        layout.getDistanceBetweenGenerations() + getLength(getMax(shape(parent), layoutAxis)) 
      );
    
    graph2d.setPosition(parent, r);
    
    // calculate new shape
    GeneralPath gp = new GeneralPath();
    gp.append(graph2d.getShape(parent), false);
    gp.transform(AffineTransform.getTranslateInstance(r.getX(), r.getY()));
    for (Branch branch : branches)
      gp.append(branch.shape, false);
    shape = new GeneralPath(getConvexHull(gp));
   
    // done
  }
  
  /*package*/ Shape getShape() {
    return shape;
  }
  
  /*package*/ Vertex getRoot() {
    return root;
  }
  
  /*package*/ List<Branch> getBranches() {
    return branches;
  }
  
  /** calculate children of parent */
  private List<Vertex> children(Vertex backtrack, Vertex parent, TreeLayout layout) throws GraphNotSupportedException {
    
    List<Vertex> result = new ArrayList<Vertex>(10);
    
    // either all children as per directed edges or all neighbours w/o backtrack
    if (layout.isSingleSourceDAG()) {
      result.addAll(getChildren(parent));
      if (backtrack!=null && result.contains(backtrack))
        throw new GraphNotSupportedException("Graph contains backtracking edge ["+parent+">"+backtrack+"]");
    } else {
      result.addAll(getNeighbours(parent));
      result.remove(backtrack);
    }
    
    // done
    return result;      
  }
  
  private Point2D top() throws LayoutException{
    if (top==null)
      top= getMax(shape, getRadian(orientation) - HALF_RADIAN);
    if (top==null)
      throw new LayoutException("branch for vertex "+root+" has no valid shape containing (0,0)");
    return top;
    
  }
  
  private Point2D pos() {
    return pos(root);
  }
  
  private Point2D pos(Vertex vertex) {
    return graph2d.getPosition(vertex);
  }
  
  private Shape shape(Vertex vertex) {
    return graph2d.getShape(vertex);
  }
  
  private Vertex other(Edge edge, Vertex other) {
    return edge.getStart().equals(other) ? edge.getEnd() : edge.getStart();
  }
  
  
  /** translate a branch */
  private void moveBy(Point2D delta) {
    
    translate(graph2d, root, delta);
    
    for (Branch branch : branches) 
      branch.moveBy(delta);
    
    top = null;
    
    shape.transform(AffineTransform.getTranslateInstance(delta.getX(), delta.getY()));
  }
  
  /** translate a branch */
  private void moveTo(Point2D pos) {
    moveBy(getDelta(graph2d.getPosition(root), pos));
  }

  /** compare positions of two vertices */
  public int compare(Vertex v1,Vertex v2) {
    
    double layoutAxis = getRadian(orientation);
    Point2D p1 = graph2d.getPosition(v1);
    Point2D p2 = graph2d.getPosition(v2);
    
    double delta =
      Math.cos(layoutAxis) * (p2.getX()-p1.getX()) + Math.sin(layoutAxis) * (p2.getY()-p1.getY());
    
    return (int)(delta);
  }
  
} //Branch

