/**
 * GraphJ
 * 
 * Copyright (C) 2002 Nils Meier
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package gj.model.factory;

import gj.model.Arc;
import gj.model.Factory;
import gj.model.Graph;
import gj.model.Node;
import gj.util.ArcHelper;
import gj.util.ModelHelper;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * GraphFactory - a Tree
 */
public class GraphFactory extends AbstractGraphFactory {
  
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
   * @see gj.model.factory.AbstractGraphFactory#create(gj.model.Factory, java.awt.geom.Rectangle2D)
   */
  public Graph create(Factory factory, Rectangle2D bounds) {
    
    // create graph
    Graph graph = factory.createGraph();
    
    // create nodes
    createNodes(graph, bounds, factory);
    
    // create arcs
    createArcs(graph, factory);
    
    // done
    return graph;
  }
  
  /**
   * Creates Nodes
   */
  private void createNodes(Graph graph, Rectangle2D canvas, Factory factory) {
    
    // loop for nodes
    for (int n=0;n<numNodes;n++) {
      
      Node node = factory.createNode(graph, null, ""+(n+1));
      node.getPosition().setLocation(
        getRandomPosition(canvas,node.getShape())
      );
      
    }
    
    // done
  }

  /**
   * Creates Arcs
   */
  private void createArcs(Graph graph, Factory factory) {
  
    List nodes = new ArrayList(graph.getNodes());
    
    // No Nodes?
    if (nodes.isEmpty())
      return;

    // create num arcs
    for (int i=0;i<minArcs;i++) {
      
      Node from = super.getRandomNode(nodes, false);
      Node to   = super.getRandomNode(nodes, false);
      
      if (to==from) to = super.getRandomNode(nodes, false);
      
      Arc arc = factory.createArc(graph, from, to);
      
      ArcHelper.update(arc);
    }
    
    // isConnected?
    if (isConnected) {
      ensureConnected(graph, factory);
    }
    
    // minDegree?
    if (minDegree>0) {
      ensureMinDegree(graph, factory);
    }
    
    // done
  }
  
  /**
   * Creates arcs for given Nodes so that
   *  A n_x <- nodes : deg(n_x) > min
   */
  private void ensureMinDegree(Graph graph, Factory factory) {
    
    List nodes = new ArrayList(graph.getNodes());
    
    // validate minDegree - maximum n-1 so that there
    // are always enough nodes to connect to without dups
    // or loops
    minDegree = Math.min(minDegree, nodes.size()-1);
    
    // while ...
    while (true) {
      
      // .. there's a node with deg(n)<minDegree
      Node node = getMinDegNode(nodes,false);
      if (node.getArcs().size()>=minDegree) break;
      
      // we don't want to connect to a neighbour
      List others = new LinkedList(nodes);
      others.removeAll(ModelHelper.getNeighbours(node));
      
      // find other
      while (true) {
        Node other = getRandomNode(others,true);
        if (other.getArcs().size()<minDegree||others.isEmpty()) {
          ArcHelper.update(factory.createArc(graph, node, other));
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
  protected void ensureConnected(Graph graph, Factory factory) {
    
    List nodes = new LinkedList(graph.getNodes());
    
    while (nodes.size()>1) {
      Node from = getMinDegNode(nodes,true);
      if (!ModelHelper.isNeighbour(from,nodes)) {
        Node to = getMinDegNode(nodes,false);
        ArcHelper.update(factory.createArc(graph, from, to));
      }
    }
    
    // done
  }
  
} //GraphFactory
