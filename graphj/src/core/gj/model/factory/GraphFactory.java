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

import gj.awt.geom.Path;
import gj.model.Arc;
import gj.model.MutableGraph;
import gj.model.Node;
import gj.util.ArcHelper;
import gj.util.ModelHelper;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * GraphFactory - a Tree
 */
public class GraphFactory extends AbstractFactory {
  
  /** connected or not */
  private boolean isConnected = true;
  
  /** planar or not - TODO */
  //private boolean isPlanar = false;

  /** allows cycles or not - TODO */
  //private boolean isAllowCycles = true;

  /** # number of nodes */
  private int numNodes = 10;
  
  /** # number of arcs */
  private int numArcs = 2;
  
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
  public int getNumArcs() {
    return numArcs;
  }
  
  /** 
   * Setter - # number of arcs 
   */
  public void setNumArcs(int set) {
    numArcs=set;
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
   * @see Factory#create(MutableGraph, Shape)
   */
  public void create(MutableGraph graph, Shape nodeShape) {
    
    // create nodes
    createNodes(graph,graph.getBounds(),nodeShape);
    
    // create arcs
    createArcs(graph);
    
    // done
  }
  
    /**
   * Creates Nodes
   */
  private void createNodes(MutableGraph graph, Rectangle2D canvas, Shape shape) {
    
    // loop for nodes
    for (int n=0;n<numNodes;n++) {
      Point2D position = getRandomPosition(canvas,shape);
      String content = ""+(n+1);
      Node node = graph.createNode(position, shape, content);
    }
    
    // done
  }

  /**
   * Creates Arcs
   */
  private void createArcs(MutableGraph graph) {
    
    // No Nodes?
    if (graph.getNodes().isEmpty())
      return;
      
    // validate numArcs - maximum n*(n-1)/2 so that there
    // are no dups or loops
    int n = graph.getNodes().size();
    Node[] nodes = new Node[n];
    graph.getNodes().toArray(nodes);
    numArcs = Math.min(numArcs, n*(n-1)/2);
    
    // .. loop
    for (int i=0;i<n;i++) {
      for (int j=i+1;j<n;j++) {
        Node from = nodes[i];
        Node to   = nodes[j];
        ArcHelper.update(graph.createArc(from,to,new Path()));
      }
    }
    
    List arcs = new LinkedList(graph.getArcs());
    while (arcs.size()>numArcs) {
      Arc arc = super.getRandomArc(arcs,false);
      graph.removeArc(arc);
      arcs.remove(arc);
    }
    
    // isConnected?
    if (isConnected) {
      ensureConnected(graph);
    }
    
    // minDegree?
    if (minDegree>0) {
      ensureMinDegree(graph);
    }
    
  }
  
  /**
   * Creates arcs for given Nodes so that
   *  A n_x <- nodes : deg(n_x) > min
   */
  private void ensureMinDegree(MutableGraph graph) {
    
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
        if ((other.getArcs().size()<minDegree)||(others.isEmpty())) {
          ArcHelper.update(graph.createArc(node, other, new Path()));
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
   *                 = connect(n_i,n_k) , if E arc(n_j,n_k)
   *                 = false            , otherwise
   * @param nodes list of nodes that don't have arcs (mutable)
   * @param graph the graph to creat the arcs in
   */
  protected void ensureConnected(MutableGraph graph) {
    
    List nodes = new LinkedList(graph.getNodes());
    
    while (nodes.size()>1) {
      Node from = getMinDegNode(nodes,true);
      if (!ModelHelper.isNeighbour(from,nodes)) {
        Node to = getMinDegNode(nodes,false);
        ArcHelper.update(graph.createArc(from, to, new Path()));
      }
    }
    
    // done
  }
  
  /**
   * @see IGraphFactory#getName()
   */
  public String getName() {
    return "Graph";
  }

}
