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
package gj.shell.factory;

import gj.shell.model.EditableEdge;
import gj.shell.model.EditableGraph;
import gj.shell.model.EditableVertex;
import gj.util.ModelHelper;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * GraphFactory - a directed graph creation
 */
public class DirectedGraphFactory extends AbstractGraphFactory {
  
  /** connected or not */
  private boolean isConnected = true;
  
  /** planar or not - TODO */
  //private boolean isPlanar = false;

  /** allows cycles or not - TODO */
  //private boolean isAllowCycles = true;

  /** # number of nodes */
  private int numNodes = 10;
  
  /** # number of arcs */
  private int minArcs = 2;
  
  /** minimum degree */
  private int minDegree = 1;
  
  /** 
   * Getter - # number of nodes 
   */
  public int getNumNodes() {
    return numNodes;
  }
  
  /** 
   * Setter - # number of nodes 
   */
  public void setNumNodes(int set) {
    numNodes=set;
  }
  
  /** 
   * Getter - # number of arcs 
   */
  public int getMinArcs() {
    return minArcs;
  }
  
  /** 
   * Setter - # number of arcs 
   */
  public void setMinArcs(int set) {
    minArcs=set;
  }
  
  /** 
   * Getter - minimum degree
   */
  public int getMinDegree() {
    return minDegree;
  }
  
  /** 
   * Getter - minimum degree
   */
  public void setMinDegree(int set) {
    minDegree=set;
  }
  
  /** 
   * Getter - connected
   */
  public boolean getConnected() {
    return isConnected;
  }
  
  /** 
   * Getter - connected
   */
  public void setConnected(boolean set) {
    isConnected=set;
  }
  
  /**
   * @see gj.shell.factory.AbstractGraphFactory#create(Rectangle2D)
   */
  @Override
  public EditableGraph create(Rectangle2D bounds) {
    
    // create graph
    EditableGraph graph = new EditableGraph();
    
    // create nodes
    createNodes(graph, bounds);
    
    // create arcs
    createArcs(graph);
    
    // done
    return graph;
  }
  
  /**
   * Creates Nodes
   */
  private void createNodes(EditableGraph graph, Rectangle2D canvas) {
    
    // loop for nodes
    for (int n=0;n<numNodes;n++) {
      
      EditableVertex vertex = graph.addVertex(null, nodeShape, ""+(n+1));
      Point2D pos = getRandomPosition(canvas,vertex.getShape());
      vertex.setPosition(pos);
     
    }
    
    // done
  }

  /**
   * Creates Arcs
   */
  private void createArcs(EditableGraph graph) {
  
    List<EditableVertex> nodes = new ArrayList<EditableVertex>((Set<EditableVertex>)graph.getVertices());
    
    // No Nodes?
    if (nodes.isEmpty())
      return;

    // create num arcs
    for (int i=0;i<minArcs;i++) {
      
      EditableVertex from = super.getRandomNode(nodes, false);
      EditableVertex to   = super.getRandomNode(nodes, false);
      
      if (to==from) 
        to = super.getRandomNode(nodes, false);
      
      EditableEdge edge = graph.addEdge(from, to, null);
    }
    
    // isConnected?
    if (isConnected) 
      ensureConnected(graph);
    
    // minDegree?
    if (minDegree>0) 
      ensureMinDegree(graph);
    
    // done
  }
  
  /**
   * Creates arcs for given Nodes so that
   *  A n_x <- nodes : deg(n_x) > min
   */
  private void ensureMinDegree(EditableGraph graph) {
    
    List<EditableVertex> nodes = ModelHelper.toList(graph.getVertices());
    
    // validate minDegree - maximum n-1 so that there
    // are always enough nodes to connect to without dups
    // or loops
    minDegree = Math.min(minDegree, nodes.size()-1);
    
    // while ...
    while (true) {
      
      // .. there's a node with deg(n)<minDegree
      EditableVertex vertex = getMinDegNode(graph, nodes, false);
      if (graph.getNumAdjacentVertices(vertex) >= minDegree) 
        break;
      
      // we don't want to connect to a neighbour
      List<EditableVertex> others = new LinkedList<EditableVertex>(nodes);
      ModelHelper.removeAll(others, graph.getAdjacentVertices(vertex));
      
      // find other
      while (true) {
        EditableVertex other = getRandomNode(others,true);
        if (graph.getNumAdjacentVertices(other) < minDegree || others.isEmpty()) {
          graph.addEdge(vertex, other, null);
          break;
        }
      }
      
      // continue
    }
    
    // done
  }
  

  /**
   * Creates arcs for given Nodes so that
   *  A n_x,n_y <- nodes : con(n_i, n_j) 
   * where
   *   con(n_i, n_j) = true             , if E arc(n_i,n_j)
   *                 = con(n_i,n_k)     , if E arc(n_j,n_k)
   *                 = false            , otherwise
   * @param nodes list of nodes that don't have arcs (mutable)
   * @param graph the graph to creat the arcs in
   */
  protected void ensureConnected(EditableGraph graph) {
    
    List<EditableVertex> nodes = new LinkedList<EditableVertex>(ModelHelper.toList(graph.getVertices()));
    
    while (nodes.size()>1) {
      EditableVertex from = getMinDegNode(graph,nodes,true);
      if (!ModelHelper.isNeighbour(graph,from,nodes)) {
        EditableVertex to = getMinDegNode(graph, nodes,false);
        graph.addEdge(from, to, null);
      }
    }
    
    // done
  }
  
} //DirectedGraphFactory
