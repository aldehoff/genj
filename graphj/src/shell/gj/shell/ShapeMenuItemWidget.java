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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

import javax.swing.Action;
import javax.swing.JMenuItem;

/**
 * A Widget that shows a Shape
 */
public class ShapeMenuItemWidget extends JMenuItem {
  
  /** the shape we're representing */
  private Shape shape;
  
  /**
   * Constructor
   */
  public ShapeMenuItemWidget(Shape shape, Action action) {
    super(action);
    this.shape = shape;
  }

  /**
   * @see Component#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    Dimension size = shape.getBounds().getSize();
    return new Dimension(size.width+5, size.height+5);
  }

  /**
   * @see Component#paintComponent
   */
  public void paintComponent(Graphics g) {
    
    super.paintComponent(g);

    Dimension size = getSize();
    Graphics2D graphics = (Graphics2D)g;
    
    graphics.setColor(Color.black);
    graphics.translate(size.width/2, size.height/2);
    graphics.draw(shape);
    graphics.translate(-size.width/2, -size.height/2);
  }

}
