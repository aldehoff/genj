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

import gj.model.factory.*;
import gj.model.*;
import gj.model.Node;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.MutableGraph;
import gj.model.factory.*;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

/**
 * Base for all Graph/Tree/.. creation
 */
public abstract class AbstractFactory implements Factory {

  /**
   * Helper that returns the node with minimum degree from a list
   */
  protected Node getMinDegNode(List list, boolean remove) {
    
    int pos = getRandomIndex(list.size());
    
    Node result = (Node)list.get(pos);
    for (int i=1;i<list.size();i++) {
      Node other = (Node)list.get( (pos+i)%list.size() );
      if (other.getArcs().size()<result.getArcs().size()) result=other;
    }
    if (remove) list.remove(result);
    return result;
  }
  
  /**
   * Helper that returns a random index for a list
   */
  protected int getRandomIndex(int ceiling) {
    double rnd = 1;
    while (rnd==1) rnd=Math.random();
    return (int)(rnd*ceiling);
  }

  /**
   * Helper that returns a random DefaultNode from a list
   */
  protected Node getRandomNode(List list, boolean remove) {
    int i = getRandomIndex(list.size());
    if (remove) return (Node)list.remove(i);
    return (Node)list.get(i);
  }
  
  /**
   * Helper that returns a random DefaultArc from a list
   */
  protected Arc getRandomArc(List list, boolean remove) {
    int i = getRandomIndex(list.size());
    if (remove) return (Arc)list.remove(i);
    return (Arc)list.get(i);
  }
  

  /**
   * Helper to create a random position in given canvas
   * @param canvas the canvas to respect
   * @param shape the shape that will be place at the resulting position
   */
  protected Point2D getRandomPosition(Rectangle2D canvas, Shape shape) {
    
    Rectangle2D nodeCanvas = shape.getBounds2D();

    double 
      x = canvas.getMinX() - nodeCanvas.getMinX(),
      y = canvas.getMinY() - nodeCanvas.getMinY(),
      w = canvas.getWidth() - nodeCanvas.getWidth(),
      h = canvas.getHeight() - nodeCanvas.getHeight();

    return new Point2D.Double(x + Math.random()*w, y + Math.random()*h);
    
  }
  

  /**
   * Creates a graph
   */
  public abstract void create(MutableGraph graph, Shape nodeShape);
  
  /**
   * Returns the factory's name
   */
  public abstract String getName();
  
  
}
