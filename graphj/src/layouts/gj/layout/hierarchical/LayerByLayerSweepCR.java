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
package gj.layout.hierarchical;

import static gj.geom.Geometry.getIntersection;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * crossing reduction implementation 
 */
public class LayerByLayerSweepCR implements CrossingReduction {

  /**
   * algorithmic part of reducing crossings of layers via layout
   */
  public void reduceCrossings(List<Layer> layers) {
    
    // we're going from layer to layer reducing number of crossings between layer i and i+1
    for (int i=1;i<layers.size();i++)
      sweepLayer(layers.get(i), -1);

    // TODO sweeping from source>sink vs sink>source can lead to a better result - we should probably try both
//    for (int i=layers.size()-2;i>=0;i--)
//      sweepLayer(layers.get(i), +1);
  }

  /**
   * reduce crossings in two layered digraph G with layers layer1+2
   */
  private int sweepLayer(Layer layer, int direction) {
    
    // calculate current number of crossings
    int originalNumCrossings = 0;
    for (int u=0,v=1;u<layer.size()-1;u++,v++) {
      originalNumCrossings += crossingNumber(u, v, layer, direction);
    }    
    
    // no improvement possible?
    if (originalNumCrossings==0)
      return originalNumCrossings;
    
    // loop until number crossings cannot be reduced
    int optimizedNumCrossings = originalNumCrossings;
    while (true) {

      int nc = 0;
      for (int u=0,v=1;u<layer.size()-1;u++,v++) {
        
        // calculate crossing numbers 
        int cuv = crossingNumber(u, v, layer, direction);
        int cvu = crossingNumber(v, u, layer, direction);
        if (cuv>cvu) layer.swap(u, v);

        // keep track of crossings
        nc += Math.min(cuv,cvu);
          
        // next u,v
      }

      // stop if number of crossings wasn't reduced
      if (nc>=optimizedNumCrossings)
        break;
      
      // commit
      optimizedNumCrossings = nc;
    }
    
    // done
    return optimizedNumCrossings;
  }

  /**
   * calculate crossing number for u,v
   */
  private int crossingNumber(int u, int v, Layer layer, int direction) {
    
    // start with # crossings = 0
    int cn = 0;

    // loop over all u1>u2 and v1>v2 pairs
    Layer.Assignment u1 = layer.get(Math.min(u,v));
    for (Layer.Assignment u2 : u1.adjacents(direction)) {
      
      Layer.Assignment v1 = layer.get(Math.max(u,v));
      for (Layer.Assignment v2 : v1.adjacents(direction)) {
        
        if (u2.pos()!=v2.pos() && intersects(u, u2.pos(), v, v2.pos()))
          cn ++;
          
      }
    }

    // dne
    return cn;
  }
  
  private boolean intersects(int u1, int u2, int v1, int v2) {
    return getIntersection(new Point2D.Double(u1,0), new Point2D.Double(u2,1), new Point2D.Double(v1,0), new Point2D.Double(v2,1)) != null;
  }
  
}
