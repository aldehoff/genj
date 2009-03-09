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
package gj.layout.edge.visibility;

import java.awt.Shape;

import gj.layout.EdgeLayout;
import gj.layout.Graph2D;
import gj.layout.GraphLayout;
import gj.layout.LayoutContext;
import gj.layout.LayoutException;
import gj.util.LayoutHelper;

/**
 * Given a set of vertices w/shapes find the shortest path between two vertices
 * that does not intersect any of the shapes.
 */
public class EuclideanShortestPathLayout implements GraphLayout {

  /**
   * apply it
   */
  public Shape apply(Graph2D graph2d, LayoutContext context) throws LayoutException {
    
    // create a visibility graph
    VisibilityGraph vg = new VisibilityGraph(graph2d);
    context.addDebugShape(vg.getDebugShape());
    
    // done
    return LayoutHelper.getBounds(graph2d);
  }
  
} //EuclideanShortestPathLayout
