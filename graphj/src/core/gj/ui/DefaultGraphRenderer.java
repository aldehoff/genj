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
package gj.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import gj.util.ArcIterator;
import gj.awt.geom.Geometry;
import gj.awt.geom.ShapeHelper;
import gj.awt.geom.Path;
import gj.layout.Layout;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;

/**
 * A renderer that knows how to render a graph
 */
public class DefaultGraphRenderer {
  
  /** an arrow-head to the right */
  private final static Shape ARROW_HEAD = ShapeHelper.createShape(0,0,1,1,new double[]{
      0, 0, 0, 1, -7, 5, 1, -7, -5, 1, 0, 0
  });
  
  /**
   * The rendering functionality
   */
  public void render(Graph graph, Layout layout, UnitGraphics graphics) {
    
    // the arcs
    renderArcs(graph.getArcs(),graphics);    
    
    // the nodes
    renderNodes(graph.getNodes(),graphics);

    // done
  }
  
  /**
   * Renders all Nodes
   */
  private void renderNodes(Collection nodes, UnitGraphics graphics) {
    
    // Loop through the graph's nodes
    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      
      // .. this is the node
      Node node = (Node)it.next();
      
      // .. render
      renderNode(node, graphics);
      
      // .. next
    }
    // Done
  }

  public void renderNode(Node node, UnitGraphics graphics) {

    double 
      x = node.getPosition().getX(),
      y = node.getPosition().getY();
      
    // draw its shape
    graphics.setColor(Color.black);
    graphics.draw(node.getShape(), x, y, 1D, 1D, 0D, false);

    // and content    
    Object content = node.getContent();
    if (content!=null) {
      graphics.draw(content.toString(), x,y);
    }

    // done
  }

  /**
   * Renders all Arcs
   */
  private void renderArcs(Collection arcs, UnitGraphics graphics) {
    
    // Loop through the graph's arcs
    Iterator it = arcs.iterator();
    while (it.hasNext()) {
      Arc arc = (Arc)it.next();
      renderArc(arc, graphics);
    }
   
    // Done
  }
  
  /**
   * Renders an Arc
   */
  public void renderArc(Arc arc, UnitGraphics graphics) {
    
    Path path = arc.getPath();
    
    // arbitrary color
    graphics.setColor(Color.red);
    
    // the path's shape
    graphics.draw(path,0,0,false);
    
    // and it's end
    Point2D p = path.getLastPoint();
    double a = path.getLastAngle();
    graphics.draw(ARROW_HEAD, p.getX(), p.getY(), 1, 1, a, true);

    // done      
  }
  
}
