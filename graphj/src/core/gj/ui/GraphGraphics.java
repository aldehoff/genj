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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Stack;

/**
 * A Graphics for drawing Graphs
 */
public class GraphGraphics {

  /** a stack of transformations we keep for push/pop */
  private Stack transformations = new Stack();
  
  /** the graphics object we're working on*/
  private Graphics2D graphics;
  
  /**
   * Constructor
   */
  public GraphGraphics(Graphics2D g2d) {
    graphics = g2d;
  }
  
  /**
   * Translates
   */
  public void translate(double x, double y) {
    graphics.translate(x,y);
  }

  /**
   * Pops a saved AffineTransformation
   */
  public void popTransformation() {
    graphics.setTransform((AffineTransform)transformations.pop());
  }

  /**
   * Pushes a AffineTransformation for later
   */
  public void pushTransformation() {
    transformations.push(graphics.getTransform());
  }

  /**
   * Sets color
   */
  public GraphGraphics setColor(Color color) {
    graphics.setColor(color);
    return this;
  }

  /**
   * Draw/Fill a Shape
   */
  public GraphGraphics draw(Shape shape, boolean fill) {
    if (fill)
      graphics.fill(shape);
    else
      graphics.draw(shape);
    return this;
  }
  
  /**
   * Draw/Fill a Shape
   */
  public GraphGraphics draw(Shape shape, double x, double y, double sx, double sy, double alpha, boolean fill) {

    pushTransformation();
    graphics.translate(x, y);
    if ((sx!=1)||(sy!=1))
      graphics.scale(sx,sy);
    
    if (alpha!=0)
      graphics.rotate(alpha);
    if (fill)
      graphics.fill(shape);
    else
      graphics.draw(shape);
    popTransformation();
    
    return this;
  }
  
  /**
   * Renders a line
   */
  public GraphGraphics draw(double x1, double y1, double x2, double y2) {
    graphics.drawLine( (int)x1, (int)y1, (int)x2, (int)y2 );
    return this;
  }

  /**
   * Renders a line
   */
  public GraphGraphics draw(Point2D start, Point2D end) {
    draw(start.getX(), start.getY(), end.getX(), end.getY());
    return this;
  }
  
  /**
   * Renders a rectangle
   */
  public GraphGraphics draw(double x1, double y1, double x2, double y2, boolean fill) {
    
    double 
      h = y2-y1,
      w = x2-x1;
      
    if (fill) {
      graphics.fillRect((int)x1, (int)y1, (int)w+1, (int)h+1 );
    } else {
      graphics.drawRect((int)x1, (int)y1, (int)w, (int)h );
    }
      
    return this;
  }
  
  /**
   * Renders text
   */
  public GraphGraphics draw(String txt, double x, double y, boolean left) {
    
    int sx = left? - graphics.getFontMetrics().stringWidth(txt)/2 : 0;
    int sy = - graphics.getFontMetrics().getHeight()/2 +  graphics.getFontMetrics().getAscent();
    
    graphics.drawString(txt, (int)(x+sx), (int)(y+sy));

    return this;    
  }

}
