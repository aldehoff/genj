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

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * GraphFactory - a Tree
 */
public class TreeFactory extends AbstractGraphFactory {
  
  /** the maximum depth */
  private int maxDepth = 5;

  /** the maximum number of children */
  private int maxChildren = 4;

  /** the number of nodes */
  private int numNodes = 4;

  /** 
   * Getter - the maximum depth 
   */
  public int getMaxDepth() {
    return maxDepth;
  }

  /** 
   * Setter - the maximum depth 
   */
  public void setMaxDepth(int set) {
    maxDepth=set;
  }

  /** 
   * Getter - the maximum number of children 
   */
  public int getMaxChildren() {
    return maxChildren;
  }

  /** 
   * Setter - the maximum number of children 
   */
  public void setMaxChildren(int set) {
    maxChildren=set;
  }

  /** 
   * Getter - the number of nodes 
   */
  public int getNumNodes() {
    return numNodes;
  }

  /** 
   * Setter - the number of nodes 
   */
  public void setNumNodes(int set) {
    numNodes=set;
  }

  /** a sample */
  private static final String[][] sample = {
      { "1.1", "1.2" },
      { "1.2", "1.3", "1.2.1.1", "1.2.2.1", "1.2.3.1" },
      { "1.3", "1.4" },
      { "1.4", "1.5", "1.4.1.1" },
      { "1.5", "1.6" },
      { "1.6" },
      
      { "1.2.1.1", "1.2.1.2" },
      { "1.2.1.2" },
      
      { "1.2.2.1", "1.2.2.2" },
      { "1.2.2.2", "1.2.2.3" },
      { "1.2.2.3", "1.2.2.4" },
      { "1.2.2.4" },

      { "1.2.3.1", "1.2.3.2" },
      { "1.2.3.2" },
      
      { "1.4.1.1", "1.4.1.2", "1.4.1.2.1.1"},
      { "1.4.1.2" },
      { "1.4.1.2.1.1",  "1.4.1.2.1.2"},
      { "1.4.1.2.1.2" }
    };
    
  /**
   * @see gj.model.factory.AbstractGraphFactory#create(gj.model.Factory, java.awt.geom.Rectangle2D)
   */
  public Graph create(Factory factory, Rectangle2D bounds) {
    
    // create the graph
    Graph graph = factory.createGraph();
    
    // We loop through the sample data
    Map nodes = new HashMap(sample.length);
    for (int s = 0; s < sample.length; s++) {

      String key = sample[s][0];
      Node node = factory.createNode(graph, null, key);
      node.getPosition().setLocation(super.getRandomPosition(bounds, node.getShape()));
      nodes.put(key, node);
    }
     
    for (int s = 0; s < sample.length; s++) {
      Node from = (Node)nodes.get(sample[s][0]);
      for (int c = 1; c < sample[s].length; c++) {
        String key = sample[s][c];        
        Node to = (Node)nodes.get(key);

        Arc arc;
        if (Math.random()>0.5) {
          arc = factory.createArc(graph, from, to);
        } else {
          arc = factory.createArc(graph, to, from);
        }
        ArcHelper.update(arc);
      }
    }
    
    // Done    
    return graph;
  }

} //TreeFactory
