/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.tree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;

import genj.renderer.UnitGraphics;

/**
 * The renderer knowing how to render the content of tree's model
 */
public class ContentRenderer {

  /** background color */
  /*package*/ Color cBackground = null;
  
  /** shape color for indis */
  /*package*/ Color cIndiShape = null;

  /**
   * The dimension of the content
   */
  public Dimension getDimension(Model model) {
    Rectangle2D bounds = model.getBounds();
    int 
      w = UnitGraphics.units2pixels(bounds.getWidth (), UnitGraphics.CENTIMETERS),
      h = UnitGraphics.units2pixels(bounds.getHeight(), UnitGraphics.CENTIMETERS);
    return new Dimension(w,h);
  }

  /**
   * Render the content
   */
  public void render(Graphics g, Model model) {  
    // go 2d
    UnitGraphics ug = new UnitGraphics(g, UnitGraphics.CENTIMETERS);
    // translate to center
    Rectangle2D bounds = model.getBounds();
    ug.translate(-bounds.getX(), -bounds.getY());
    // render background
    renderBackground(ug, bounds);
    // render the nodes
    renderNodes(ug, model.getNodes());
    // done
  }  
  
  /**
   * Render the nodes
   */
  private void renderNodes(UnitGraphics g, Collection nodes) {
    // loop
    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      // grab node
      Model.EntityNode node = (Model.EntityNode)it.next();
      Point2D pos = node.getPosition();
      double 
        x = pos.getX(),
        y = pos.getY();
      // draw its shape
      g.setColor(cIndiShape);
      g.draw(node.getShape(), x, y, false);
      // draw its content
      g.draw("Hi", x, y);
      // done
    }
    // done
  }
  
  /**
   * Render the background
   */
  private void renderBackground(UnitGraphics g, Rectangle2D bounds) {
    if (cBackground==null) return;
    g.setColor(cBackground);
    g.draw(bounds, 0, 0, true);
  }

} //ContentRenderer
