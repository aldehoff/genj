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

import static gj.util.LayoutHelper.contains;
import static gj.util.LayoutHelper.getChildren;
import static gj.util.LayoutHelper.getInDegree;
import static gj.util.LayoutHelper.getNeighbours;
import static gj.util.LayoutHelper.getOther;
import static gj.util.LayoutHelper.getPath;
import static gj.util.LayoutHelper.getPort;
import static gj.util.LayoutHelper.translate;
import gj.geom.Geometry;
import gj.geom.Path;
import gj.layout.Graph2D;
import gj.layout.GraphNotSupportedException;
import gj.layout.LayoutContext;
import gj.layout.LayoutException;
import gj.layout.edge.visibility.EuclideanShortestPathLayout;
import gj.model.Edge;
import gj.model.Vertex;
import gj.util.AbstractGraphLayout;
import gj.util.LayoutHelper.Side;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Vertex layout for Trees
 */
public class TreeLayout extends AbstractGraphLayout<Vertex> {

  /** distance of nodes in generation */
  private int distanceInGeneration = 20;

  /** distance of nodes between generations */
  private int distanceBetweenGenerations = 20;

  /** the alignment of parent over its children */
  private double alignmentOfParent = 0.5;

  /** whether children should be balanced or simply stacked */
  private boolean isBalanceChildren = false;

  /** what we do with edges */
  private EdgeControl edgeControl = EdgeControl.DirectLinesOnCenter;

  /** orientation in degrees 0-359 */
  private double orientation = 180;

  /** whether to order by position instead of natural sequence */
  private boolean isOrderSiblingsByPosition = true;
  
  /** whether we allow acyclic graphs */
  private boolean isSingleSourceDAG = true;

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
   * Getter - what we do with edges
   */
  public EdgeControl getEdgeControl() {
    return edgeControl;
  }

  /**
   * Setter - whether arcs are direct or bended
   */
  public void setEdgeControl(EdgeControl set) {
    edgeControl = set;
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
  public Vertex getRoot(Graph2D graph2d) {
    // check remembered
    Vertex result = getAttribute(graph2d);
    if (result==null) {
      // find root with zero in-degree
      for (Vertex v : graph2d.getVertices()) {
        if (getInDegree(v)==0) {
          result = v;
          setRoot(graph2d, result);
          break;
        }
      }
    }
    // done
    return result;
  }

  /**
   * Getter - root node
   */
  public void setRoot(Graph2D graph2d, Vertex root) {
    setAttribute(graph2d, root);
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
   * Whether layout should assume a single source directed acyclic graph or not.
   * In the former case this means that direction of edges is considered and graphs without
   * directed cycles but with shared sub-trees are handled by routing edges between 
   * non-siblings via a euclidean shortest path layout
   */
  public boolean isSingleSourceDAG() {
    return isSingleSourceDAG;
  }

  /**
   * Whether layout should assume a single source directed acyclic graph or not.
   * In the former case this means that direction of edges is considered and graphs without
   * directed cycles but with shared sub-trees are handled by routing edges between 
   * non-siblings via a euclidean shortest path layout
   */
  public void setSingleSourceDAG(boolean setSingleSourceDAG) {
    this.isSingleSourceDAG = setSingleSourceDAG;
  }

  /**
   * Layout a layout capable graph
   */
  public Shape apply(Graph2D graph2d, LayoutContext context) throws LayoutException {
    
    // ignore an empty tree
    Collection<? extends Vertex> vertices = graph2d.getVertices(); 
    if (vertices.isEmpty())
      return new Rectangle2D.Double();
    
    // check root
    Vertex root = getRoot(graph2d);
    if (root==null)
      throw new GraphNotSupportedException("Graph is not a tree (no vertex with in-degree of zero)");
    
    context.getLogger().fine("root is ["+root+"]");
    
    // recurse into it
    Set<Vertex> visited = new HashSet<Vertex>();
    List<Edge> edgesToRoute = new ArrayList<Edge>();
    Shape result = new Branch(null, root, graph2d, new ArrayDeque<Vertex>(), visited, edgesToRoute, context).shape;
    
    if (context.isDebug())
      context.addDebugShape(result);
    
    // check spanning tree in case we assumed DAG with single source
    if (isSingleSourceDAG&&visited.size()!=vertices.size()) {
      context.getLogger().fine("not a spanning tree (#visited="+visited.size()+" #vertices="+vertices.size());
      throw new GraphNotSupportedException("Graph is not a spanning tree ("+vertices.size()+"!="+visited.size()+")");
    }
    
    // catch up on layouting edges that need routing because of acyclic graph
    EuclideanShortestPathLayout edgeLayout = new EuclideanShortestPathLayout();
    edgeLayout.setEdgeVertexDistance(Math.min(distanceBetweenGenerations, distanceInGeneration)/4);
    for (Edge e : edgesToRoute)  {
      context.getLogger().fine("routing edge ["+e+"]");
      edgeLayout.apply(e, graph2d, context);
    }
    
    // done
    return result;
  }

  /**
   * A Branch is the recursively worked on part of the tree
   */
  private class Branch extends Geometry implements Comparator<Vertex> {
    
    /** tree */
    private Graph2D layout;
    
    /** root of branch */
    private Vertex root;
    
    /** contained vertices */
    private List<Vertex> vertices = new ArrayList<Vertex>();
    
    /** shape of branch */
    private Point2D top;
    private GeneralPath shape;
    
    /** constructor for a parent and its children */
    Branch(Vertex backtrack, Vertex parent, Graph2D graph2d, Deque<Vertex> stack, Set<Vertex> visited, List<Edge> edgesToRoute, LayoutContext context) throws LayoutException, GraphNotSupportedException{
      
      // track coverage
      visited.add(parent);

      // init state
      this.layout = graph2d;
      this.root = parent;
      vertices.add(root);
      
      // reset vertex's transformation
      graph2d.setTransformOfVertex(root, null);
      
      // grab and sort children 
      List<Vertex> children = children(backtrack, root);
      if (isOrderSiblingsByPosition) 
        Collections.sort(children, this);
      
      // recurse into children and take over descendants
      stack.addLast(parent);
      List<Branch> branches = new ArrayList<Branch>(children.size());
      for (Vertex child : children) {
        
        // catch possible recurse step into already visited nodes
        if (visited.contains(child)) {
          
          // we don't allow directed cycles
          if (stack.contains(child)) {
            context.getLogger().info("cannot handle directed cycle at all");
            throw new GraphNotSupportedException("Graph contains cycle involving ["+parent+">"+child+"]");
          }

          // allowing acyclic graphs
          if (!isSingleSourceDAG) {
            context.getLogger().info("cannot handle undirected graph with cycle unless isConsiderDirection=true");
            throw new GraphNotSupportedException("Non Digraph contains non-directed cycle involving ["+parent+">"+child+"]");
          }
          
          // don't re-recurse into child - remember applicable edge(s)
          for (Edge e : parent.getEdges()) {
            if (e.getEnd().equals(child))
              edgesToRoute.add(e);
          }
          continue;
        }
        
        // recurse
        Branch branch = new Branch(parent, child, graph2d, stack, visited, edgesToRoute, context);
        branches.add(branch);
        vertices.addAll(branch.vertices);

      }
      stack.removeLast();
      
      // no children?
      if (branches.isEmpty()) {
        
        // simple shape for a leaf
        Point2D pos = graph2d.getPositionOfVertex(root);
        shape = new GeneralPath(getConvexHull(
            graph2d.getShapeOfVertex(root).getPathIterator(
            AffineTransform.getTranslateInstance(pos.getX(), pos.getY()))
        ));
        top();
        
        // done
        return;
      }
      
      // Calculate deltas of children left-aligned
      double layoutAxis = getRadian(orientation);
      double lrAlignment = layoutAxis - QUARTER_RADIAN;
      Point2D[] lrDeltas  = new Point2D[branches.size()];
      lrDeltas[0] = new Point2D.Double();
      for (int i=1;i<branches.size();i++) {
        // calculate delta from top alignment position
        lrDeltas[i] = getDelta(branches.get(i).top(), branches.get(0).top());
        // calculate distance from all previous siblings
        double distance = Double.MAX_VALUE;
        for (int j=0;j<i;j++) {
          distance = Math.min(distance, getDistance(getTranslated(branches.get(j).shape, lrDeltas[j]), getTranslated(branches.get(i).shape, lrDeltas[i]), lrAlignment) - distanceInGeneration);
        }
        // calculate delta from top aligned position with correct distance
        lrDeltas[i] = getPoint(lrDeltas[i], lrAlignment, -distance);
      }
      
      // place last child
      branches.get(branches.size()-1).moveBy(lrDeltas[lrDeltas.length-1]);

      // Calculate deltas of children right-aligned
      Point2D[] rlDeltas  = lrDeltas;
      if (branches.size()>2 && isBalanceChildren) {
        rlDeltas = new Point2D[branches.size()];
        double rlAlignment = layoutAxis + QUARTER_RADIAN;
        rlDeltas [rlDeltas.length-1] = new Point2D.Double();
        for (int i=rlDeltas.length-2;i>=0;i--) {
          // calculate delta from top alignment position
          rlDeltas[i] = getDelta(branches.get(i).top(), branches.get(branches.size()-1).top());
          // calculate distance from all previous siblings
          double distance = Double.MAX_VALUE;
          for (int j=rlDeltas.length-1;j>i;j--) {
            distance = Math.min(distance, getDistance(getTranslated(branches.get(j).shape, rlDeltas[j]), getTranslated(branches.get(i).shape, rlDeltas[i]), rlAlignment) - distanceInGeneration);
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
      Point2D b = graph2d.getPositionOfVertex(branches.get(0).root);
      Point2D c = graph2d.getPositionOfVertex(branches.get(branches.size()-1).root);
      Point2D d = getPoint(b, c, alignmentOfParent); 
      Point2D e = getIntersection(a, layoutAxis-QUARTER_RADIAN, d, layoutAxis - HALF_RADIAN);
      Point2D f = getPoint(e, layoutAxis-HALF_RADIAN, distanceBetweenGenerations/2);
      
      Point2D r = getPoint(
          e, layoutAxis - HALF_RADIAN, 
          distanceBetweenGenerations + getLength(getMax(shape(root), layoutAxis)) 
        );
      
      graph2d.setPositionOfVertex(root, r);
      
      // calculate new shape
      GeneralPath gp = new GeneralPath();
      gp.append(graph2d.getShapeOfVertex(root), false);
      gp.transform(AffineTransform.getTranslateInstance(r.getX(), r.getY()));
      for (Branch branch : branches)
        gp.append(branch.shape, false);
      shape = new GeneralPath(getConvexHull(gp));
      
      // layout edges
      for (Edge edge : root.getEdges()) {
        
        // don't do edge to backtrack
        if (contains(edge, backtrack))
          continue;

        // calculate path
        Path path;
        switch (edgeControl) {
          case BendedLines:
            // calc edge layout
            Point2D[] points;
            if (edge.getStart().equals(parent)) {
              Point2D g = getIntersection(f, layoutAxis-QUARTER_RADIAN, pos(edge.getEnd()), layoutAxis);
              points = new Point2D[]{ pos(edge.getStart()), f, g, pos(edge.getEnd()) };
            } else {
              Point2D g = getIntersection(f, layoutAxis-QUARTER_RADIAN, pos(edge.getStart()), layoutAxis);
              points = new Point2D[]{ pos(edge.getStart()), g, f, pos(edge.getEnd()) };
            }
            path = getPath(points, pos(edge.getStart()), shape(edge.getStart()), pos(edge.getEnd()), shape(edge.getEnd()), false);
            break;
          case DirectLinesWithPorts:
            Side side = side();
            if (edge.getEnd().equals(parent))
              side = side.opposite();
            path = getPath(
                pos(edge.getStart()), shape(edge.getStart()), getPort(pos(edge.getStart()), shape(edge.getStart()), children.indexOf(getOther(edge, parent)), children.size(), side           ),
                pos(edge.getEnd  ()), shape(edge.getEnd  ()), getPort(pos(edge.getEnd  ()), shape(edge.getEnd  ()), 0  , 1              , side.opposite())
            );
            break;
          case DirectLinesOnCenter: default:
            path = getPath(edge, graph2d);
            break;
        }
        
        graph2d.setPathOfEdge(edge, path);
        
        
        // next
        
      }

      // done
    }
    
    /** vertex port side for current orientation */
    Side side() {
      if (orientation==0)
        return Side.North;
      if (orientation==180)
        return Side.South;
      if (orientation==90)
        return Side.East;
      if (orientation==270)
        return Side.West;
      // only support NWES
      return Side.None;
    }
    
    List<Vertex> children(Vertex backtrack, Vertex parent) throws GraphNotSupportedException {
      
      List<Vertex> result = new ArrayList<Vertex>(10);
      
      // either all children as per directed edges or all neighbours w/o backtrack
      if (isSingleSourceDAG) {
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
    
    Point2D top() throws LayoutException{
      if (top==null)
        top= getMax(shape, getRadian(orientation) - HALF_RADIAN);
      if (top==null)
        throw new LayoutException("branch for vertex "+root+" has no valid shape containing (0,0)");
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
        translate(layout, vertice, delta);
      
      top = null;
      
      shape.transform(AffineTransform.getTranslateInstance(delta.getX(), delta.getY()));
    }
    
    /** translate a branch */
    void moveTo(Point2D pos) {
      moveBy(getDelta(layout.getPositionOfVertex(root), pos));
    }

    /** compare positions of two vertices */
    public int compare(Vertex v1,Vertex v2) {
      
      double layoutAxis = getRadian(orientation);
      Point2D p1 = layout.getPositionOfVertex(v1);
      Point2D p2 = layout.getPositionOfVertex(v2);
      
      double delta =
        Math.cos(layoutAxis) * (p2.getX()-p1.getX()) + Math.sin(layoutAxis) * (p2.getY()-p1.getY());
      
      return (int)(delta);
    }
  } //Branch
  
  /**
   * our edge control
   */
  public enum EdgeControl {
    DirectLinesOnCenter,
    DirectLinesWithPorts,
    BendedLines
  }
  
} //TreeLayout
