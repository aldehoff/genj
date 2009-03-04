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
package gj.layout.random;

import gj.layout.GraphLayout;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmContext;
import gj.layout.LayoutAlgorithmException;
import gj.model.Graph;
import gj.model.Vertex;
import gj.util.LayoutHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

/**
 * A random layout
 */
public class RandomLayoutAlgorithm implements LayoutAlgorithm {
  
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
   * @see LayoutAlgorithm#apply(Graph, GraphLayout, LayoutAlgorithmContext)
   */
  public Shape apply(Graph graph, GraphLayout layout, LayoutAlgorithmContext context) throws LayoutAlgorithmException {
    
    // something to do for me?
    if (graph.getVertices().isEmpty())
      return new Rectangle2D.Double();
    
    // get a seed
    Random random = new Random(seed++);

    // place the nodes    
    for (Vertex vertex : graph.getVertices()) {
      
      Rectangle2D nodeCanvas = layout.getShapeOfVertex(vertex).getBounds2D();
      Rectangle2D preferred = context.getPreferredBounds();
      if (preferred==null)
        throw new IllegalArgumentException("LayoutAlgorithmContext.getPreferredBounds() cannot be null");

      double 
        x = preferred.getMinX() - nodeCanvas.getMinX(),
        y = preferred.getMinY() - nodeCanvas.getMinY(),
        w = preferred.getWidth() - nodeCanvas.getWidth(),
        h = preferred.getHeight() - nodeCanvas.getHeight();

      Point2D pos = layout.getPositionOfVertex(vertex);
      pos.setLocation(
        isApplyHorizontally ? x + random.nextDouble()*w : pos.getX(), 
        isApplyVertically ? y + random.nextDouble()*h : pos.getY()
      );
      layout.setPositionOfVertex(vertex, pos);

    }
    
    // place the arcs
    LayoutHelper.setPaths(graph, layout);
    
    // done
    return context.getPreferredBounds();
  }

}
