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
package gj.layout.tree;

import java.awt.Color;
import java.awt.Shape;
import java.util.Iterator;

import gj.awt.geom.Geometry;
import gj.layout.Layout;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.ui.UnitGraphics;
import gj.ui.GraphRenderer;

/**
 * A renderer for TreeLayout
 */
public class TreeLayoutRenderer implements GraphRenderer {

  /**
   * @see gj.ui.LayoutRenderer#render(Graph, GraphGraphics)
   */
  public void render(Graph graph, Layout layout, UnitGraphics graphics) {

    // the layout has to ours
    if (!(layout instanceof TreeLayout)) return;
    TreeLayout tlayout = (TreeLayout)layout;
    
    // and it had to be applied to the graph
    if (tlayout.appliedTo!=graph) return;

    // Render the root
    Node root = tlayout.getRoot();
    if ((root!=null)&&graph.getNodes().contains(root)) {
      emphasize(root, Color.green, graphics);
    }

    // debugging must be on for sketching the contour
    if (tlayout.isDebug()) {
      
      // Render the contours
      graphics.setColor(Color.lightGray);
      
      Iterator it = tlayout.debugShapes.iterator();
      while (it.hasNext()) graphics.draw((Shape)it.next(),0,0,false);
      
      // end debug
    }
    
    // done  
  }

  /**
   * @see gj.ui.GraphRenderer#renderArc(Arc, UnitGraphics)
   */
  public void renderArc(Arc arc, UnitGraphics graphics) {
    // noop
  }

  /**
   * @see gj.ui.GraphRenderer#renderNode(Node, UnitGraphics)
   */
  public void renderNode(Node node, UnitGraphics graphics) {
    // noop
  }

  /**
   * Helper that 'emphasizes' a node by drawing a coloured
   * frame around its shape
   */
  private void emphasize(Node node, Color color, UnitGraphics graphics) {
    graphics.setColor(color);
    graphics.draw(
      node.getShape(), 
      node.getPosition().getX(), 
      node.getPosition().getY(),
      1.1D,1.1D,0,false
    );
  }
  
}
