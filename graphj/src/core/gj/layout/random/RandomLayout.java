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
package gj.layout.random;

import gj.layout.AbstractLayout;
import gj.layout.Layout;
import gj.layout.LayoutException;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.util.ArcHelper;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Random;

/**
 * A random layout
 */
public class RandomLayout extends AbstractLayout implements Layout {
  
  /** the seed */
  private long seed = 0;
  
  /** wether to change x-coordinates */
  private boolean isApplyHorizontally = true;

  /** wether to change y-coordinates */
  private boolean isApplyVertically = true;

  /** 
   * Getter - the seed 
   */
  public long getSeed() {
    return seed;
  }
  
  /** 
   * Setter - the seed 
   */
  public void setSeed(long set) {
    seed=set;
  }
  
  /** 
   * Getter - wether to change x-coordinates 
   */
  public boolean isApplyHorizontally() {
    return isApplyHorizontally;
  }

  /** 
   * Setter - wether to change x-coordinates 
   */
  public void setApplyHorizontally(boolean set) {
    isApplyHorizontally=set;
  }

  /** 
   * Getter - wether to change y-coordinates 
   */
  public boolean isApplyVertically() {
    return isApplyVertically;
  }

  /** 
   * Setter - wether to change y-coordinates 
   */
  public void setApplyVertically(boolean set) {
    isApplyVertically=set;
  }

  /**
   * @see Layout#applyTo(Graph)
   */
  public void layout(Graph graph) throws LayoutException {
    
    // something to do for me?
    if (graph.getNodes().isEmpty()) return;
    
    // get a seed
    Random random = new Random(seed++);

    // place the nodes    
    Rectangle2D canvas = graph.getBounds();
    Iterator nodes = graph.getNodes().iterator();
    while (nodes.hasNext()) {
      
      Node node = (Node)nodes.next();
      Rectangle2D nodeCanvas = node.getShape().getBounds2D();

      double 
        x = canvas.getMinX() - nodeCanvas.getMinX(),
        y = canvas.getMinY() - nodeCanvas.getMinY(),
        w = canvas.getWidth() - nodeCanvas.getWidth(),
        h = canvas.getHeight() - nodeCanvas.getHeight();

      node.getPosition().setLocation( 
        isApplyHorizontally ? x + random.nextDouble()*w : node.getPosition().getX(), 
        isApplyVertically ? y + random.nextDouble()*h : node.getPosition().getY()
      );

    }
    
    // place the arcs
    Iterator arcs = graph.getArcs().iterator();
    while (arcs.hasNext()) {
      
      Arc arc = (Arc)arcs.next();
      
      Node 
        from = arc.getStart(),
        to   = arc.getEnd();
      
      ArcHelper.update(arc);
    }
    
    // done
  }

}
