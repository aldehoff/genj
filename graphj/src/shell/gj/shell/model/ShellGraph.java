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
package gj.shell.model;

import gj.awt.geom.Geometry;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Graph for Shell
 */
public class ShellGraph implements gj.model.Graph {
  
  /** nodes in the graph */
  private ArrayList nodes = new ArrayList();
  
  /** arcs in this graph */
  private ArrayList arcs = new ArrayList();
  
  /** a parent we might be added to */
  private ShellNode parent;
  
  /**
   * Remove node
   */
  protected void removeNode(ShellNode node) {
    nodes.remove(node);
  }

  /**
   * Remove arc
   */
  protected void removeArc(ShellArc arc) {
    arcs.remove(arc);
  }
  
  /**
   * Add Notification
   */
  protected void addNotify(ShellNode node) {
    parent = node;
  }

  /**
   * Add Notification
   */
  protected void removeNotify() {
    parent = null;
  }
  
//  /**
//   * Change Notification
//   */
//  protected void revalidate() {
//
//    // propagate to parent?
//    if (parent!=null)
//      parent.revalidate(false);
//      
//    // done
//  }
  
  /**
   * Bounds
   */
  public Rectangle2D getBounds() {
    // could be cached
    return ModelHelper.getBounds(nodes);
  }

  /**
   * @see gj.model.Graph#getArcs()
   */
  public Collection getArcs() {
    return arcs;
  }

  /**
   * @see gj.model.Graph#getNodes()
   */
  public Collection getNodes() {
    return nodes;
  }

  /**
   * Create a node
   */
  public ShellNode createNode(Shape shape, Object content) {
    return createNode(shape, content, null);
  }
  
  /**
   * Create a node
   */
  public ShellNode createNode(Shape shape, Object content, Point2D pos) {
    ShellNode result = new ShellNode(this, shape, content);
    if (pos!=null) result.getPosition().setLocation(pos);
    nodes.add(result);
//    revalidate();
    return result;
  }

  /**
   * Create an arc
   */
  public ShellArc createArc(ShellNode from, ShellNode to) {
    if (!nodes.contains(from)||!nodes.contains(to))
      throw new IllegalArgumentException("from or to unknown");
    ShellArc result = new ShellArc(this, from, to);
    from.addArc(result);
    to.addArc(result);
    arcs.add(result);
    return result;
  }
  
  /**
   * Return the position of a node which might be in a sub-graph
   */
  public Point2D getPosition(ShellNode node) {
    
    // look through nodes
    for (int n=0; n<nodes.size(); n++) {
      
      ShellNode candidate = (ShellNode)nodes.get(n);
      
      // the one?
      if (candidate==node) {
        return candidate.getPosition();
      }
      
      // maybe in sub-graph
      Object content = candidate.getContent();
      if (content instanceof ShellGraph) {
        
        Point2D result = ((ShellGraph)content).getPosition(node);
        if (result!=null)
          return Geometry.add(candidate.getPosition(),result);
      }
      
      // next
    }

    // not found!
    return null;
  }
  
  /**
   * Set position of a node which might be in a sub-graph
   */
  public boolean setPosition(ShellNode node, Point2D point) {

    // look through nodes
    for (int n=0; n<nodes.size(); n++) {
      
      ShellNode candidate = (ShellNode)nodes.get(n);
      
      // the one?
      if (candidate==node) {
        candidate.setPosition(point);
        return true;
      }
      
      // maybe in sub-graph
      Object content = candidate.getContent();
      if (content instanceof ShellGraph) {
        if (((ShellGraph)content).setPosition(node, Geometry.sub(candidate.getPosition(),point)))
          return true;
      }
      
      // next
    }

    // not found!
    return false;
  }

  /**
   * Return node at given point
   */
  public ShellNode getNode(Point2D point) {

    // look through nodes
    for (int n=0; n<nodes.size(); n++) {
      
      // check a node
      ShellNode node = (ShellNode)nodes.get(n);
      if (!node.contains(point)) continue;
        
      // o.k. check sub-graph
      Object content = node.getContent();
      if (content instanceof ShellGraph) {
        ShellNode inner = ((ShellGraph)content).getNode(Geometry.sub(node.getPosition(),point)); 
        if (inner!=null) node = inner;
      }
      
      return node;
    }
    
    // not found
    return null;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return nodes.size()+" nodes + "+arcs.size()+" arcs";
  }

  
} //ShellGraph