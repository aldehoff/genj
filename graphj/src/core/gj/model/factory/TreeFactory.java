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
import gj.model.MutableGraph;
import gj.model.Node;
import gj.util.ArcHelper;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * GraphFactory - a Tree
 */
public class TreeFactory extends AbstractFactory {
  
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
   * @see gj.model.factory.Factory#create(gj.model.MutableGraph, java.awt.geom.Rectangle2D, java.awt.Shape)
   */
  public Rectangle2D create(MutableGraph graph, Rectangle2D bounds, Shape nodeShape) {
    
    // We loop through the sample data
    Map nodes = new HashMap(sample.length);
    for (int s = 0; s < sample.length; s++) {
      
      String key = sample[s][0];
      Point2D pos = super.getRandomPosition(bounds,nodeShape);
      
      Node node = graph.addNode(pos, nodeShape, key);
      
      nodes.put(key, node);
    }
     
    for (int s = 0; s < sample.length; s++) {
      Node from = (Node)nodes.get(sample[s][0]);
      for (int c = 1; c < sample[s].length; c++) {
        String key = sample[s][c];        
        Node to = (Node)nodes.get(key);
        if (Math.random()>0.5) {
          ArcHelper.update(graph.addArc(from, to, new Path()));
        } else {
          ArcHelper.update(graph.addArc(to, from, new Path()));
        }
      }
    }
    
    // Done    
    return ModelHelper.getBounds(nodes.values());
  }

} //TreeFactory
