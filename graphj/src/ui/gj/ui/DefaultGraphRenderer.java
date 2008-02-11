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
package gj.ui;

import gj.geom.Path;
import gj.geom.ShapeHelper;
import gj.layout.Layout2D;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A default implementation for rendering a graph
 */
public class DefaultGraphRenderer implements GraphRenderer {

  /** an arrow-head pointing upwards */
  private final static Shape ARROW_HEAD = ShapeHelper.createShape(0,0,1,1,new double[]{
      ShapeHelper.SEG_MOVETO, 0, 0, 
      ShapeHelper.SEG_LINETO, -5, -7, 
      ShapeHelper.SEG_MOVETO,  5, -7, 
      ShapeHelper.SEG_LINETO, 0, 0
  });

  /**
   * The rendering functionality
   */
  public void render(Graph graph, Layout2D layout, Graphics2D graphics) {
  
    // the arcs
    renderEdges(graph, layout, graphics);    
    
    // the nodes
    renderVertices(graph, layout, graphics);
  
    // done
  }

  /**
   * Renders all Nodes
   */
  private void renderVertices(Graph graph, Layout2D layout, Graphics2D graphics) {
    
    // Loop through the graph's nodes
    for (Vertex vertex : graph.getVertices()) {
      renderVertex(graph, vertex, layout, graphics);
    }
    
    // Done
  }

  private void renderVertex(Graph graph, Vertex vertex, Layout2D layout, Graphics2D graphics) {
    
    // figure out its color
    Color color = getColor(vertex);
    Stroke stroke = getStroke(vertex);
  
    // draw its shape
    Point2D pos = layout.getPositionOfVertex(vertex);
    graphics.setColor(color);
    graphics.setStroke(stroke);
    Shape shape = layout.getShapeOfVertex(vertex);
    draw(shape, pos, graphics);
  
    // and content    
    Object content = getContent(vertex);
    if (content==null) 
      return;
  
    Shape oldcp = graphics.getClip();
    graphics.clip(ShapeHelper.createShape(shape, 1, pos));
    
    draw(content.toString(), pos, graphics);
  
    graphics.setClip(oldcp);
    // done
  }
  
  protected String getContent(Vertex vertex) {
    return ""+vertex;
  }
  
  /**
   * Color resolve
   */
  protected Color getColor(Vertex vertex) {
    return Color.BLACK;    
  }

  /**
   * Color resolve
   */
  protected Color getColor(Edge edge) {
    return Color.BLACK;    
  }

  /**
   * Stroke resolve
   */
  protected Stroke getStroke(Vertex vertex) {
    return new BasicStroke();    
  }

  /**
   * Renders all Arcs
   */
  private void renderEdges(Graph graph, Layout2D layout, Graphics2D graphics) {
    
    for (Edge edge: graph.getEdges()) 
      renderEdge(graph, edge, layout, graphics);
  
    // Done
  }

  /**
   * Renders an Arc
   */
  private void renderEdge(Graph graph, Edge edge, Layout2D layout, Graphics2D graphics) {
    
    AffineTransform old = graphics.getTransform();
    
    // arbitrary color
    graphics.setColor(getColor(edge));
    
    // draw path from start
    Point2D pos = layout.getPositionOfVertex(edge.getStart());
    Path path = layout.getPathOfEdge(edge);
    graphics.translate(pos.getX(), pos.getY());
    graphics.draw(layout.getPathOfEdge(edge));
    
    // draw arrow
    pos = path.getLastPoint();
    graphics.translate(pos.getX(), pos.getY());
    graphics.rotate(path.getLastAngle());
    graphics.draw(ARROW_HEAD);
    
    // done      
    graphics.setTransform(old);
  }

  /**
   * Helper that renders a shape at given position
   */
  private void draw(Shape shape, Point2D at, Graphics2D graphics) {
    draw(shape, at, 0, false, graphics);
  }

  /**
   * Helper that renders a shape at given position with given rotation
   */
  private void draw(Shape shape, Point2D at, double theta, boolean fill, Graphics2D graphics) {
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
  private void draw(String str, Point2D at, Graphics2D graphics) {
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
  
}
