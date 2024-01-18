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
package gj.shell;

import gj.awt.geom.Path;
import gj.awt.geom.ShapeHelper;
import gj.layout.Layout;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;

/**
 * A renderer that knows how to render a graph
 */
public class GraphRenderer {
  
  /** an arrow-head to the right */
  private final static Shape ARROW_HEAD = ShapeHelper.createShape(0,0,1,1,new double[]{
      0, 0, 0, 1, -7, 5, 1, -7, -5, 1, 0, 0
  });
  
  /**
   * The rendering functionality
   */
  public void render(Graph graph, Layout layout, Graphics2D graphics) {

    // the arcs
    renderArcs(graph.getArcs(),graphics);    
    
    // the nodes
    renderNodes(graph.getNodes(),graphics);

    // done
  }
  
  /**
   * Renders all Nodes
   */
  private void renderNodes(Collection nodes, Graphics2D graphics) {
    
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

  public void renderNode(Node node, Graphics2D graphics) {

    // draw its shape
    graphics.setColor(Color.black);
    draw(node.getShape(), node.getPosition(), graphics);

    // and content    
    Object content = node.getContent();
    if (content!=null) {
      draw(content.toString(), node.getPosition(), graphics);
    }

    // done
  }

  /**
   * Renders all Arcs
   */
  private void renderArcs(Collection arcs, Graphics2D graphics) {
    
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
  public void renderArc(Arc arc, Graphics2D graphics) {
    
    Path path = arc.getPath();
    
    // arbitrary color
    graphics.setColor(Color.red);
    
    // the path's shape
    graphics.draw(path);
    
    // and it's end
    Point2D p = path.getLastPoint();
    double a = path.getLastAngle();
    draw(ARROW_HEAD, p, a, true, graphics);

    // done      
  }

  /**
   * Helper that renders a shape at given position
   */
  public static void draw(Shape shape, Point2D at, Graphics2D graphics) {
    draw(shape, at, 0, false, graphics);
  }
  /**
   * Helper that renders a shape at given position with given rotation
   */
  public static void draw(Shape shape, Point2D at, double theta, boolean fill, Graphics2D graphics) {
    AffineTransform old = graphics.getTransform();
    graphics.translate(at.getX(), at.getY());
    graphics.rotate(theta);
    if (fill) graphics.fill(shape);
    else graphics.draw(shape);
    graphics.setTransform(old);
  }
  
  /**
   * Helper that renders a string at given position
   */
  public static void draw(String str, Point2D at, Graphics2D graphics) {
    float
      x = (float)at.getX(),
      y = (float)at.getY();
    FontMetrics fm = graphics.getFontMetrics();
    Rectangle2D r = fm.getStringBounds(str, graphics);
    LineMetrics lm = fm.getLineMetrics(str, graphics);
    float
      w = (float)r.getWidth(),
      h = (float)r.getHeight();
    //  graphics.draw(new Rectangle2D.Double(
    //    x-w/2, y-h/2, w, h     
    //  ));
    graphics.drawString(str, x-w/2, y+h/2-lm.getDescent());
  }
  
} //GraphRenderer
