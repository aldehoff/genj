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
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import gj.awt.geom.Path;
import gj.model.Arc;
import gj.model.Node;
import gj.ui.UnitGraphics;

/**
 * The renderer knowing how to render the content of tree's model
 */
public class ContentRenderer {

  /** background color */
  /*package*/ Color cBackground = null;
  
  /** shape color for indis */
  /*package*/ Color cIndiShape = null;
  
  /** shape color for fams */
  /*package*/ Color cFamShape = null;
  
  /** shape color for unknowns */
  /*package*/ Color cUnknownShape = null;
  
  /** shape color for arcs */
  /*package*/ Color cArcs = null;

  /** selected color */
  /*package*/ Color cSelectedShape = null;

  /** an entity that we consider selected */
  /*package*/ Entity selection = null;
  
  /** whether to render content */
  /*package*/ boolean isRenderContent = true;
  
  /**
   * Render the content
   * FIXME : have to do drawing optimizations
   */
  public void render(UnitGraphics ug, Model model) {  
    // translate to center
    Rectangle2D bounds = model.getBounds();
    ug.translate(-bounds.getX(), -bounds.getY());
    // render background
    renderBackground(ug, bounds);
    // render the nodes
    renderNodes(ug, model.getNodes());
    // render the arcs
    renderArcs(ug, model.getArcs());
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
      Node node = (Node)it.next();
      // render it
      renderNode(g, node);
      // next
    }
    // done
  }
  
  /**
   * Render a node
   */
  private void renderNode(UnitGraphics g, Node node) {
    // parameters
    Point2D pos = node.getPosition();
    double 
      x = pos.getX(),
      y = pos.getY();
    Object content = node.getContent();
    Shape shape = node.getShape();
    if (shape==null) return;
    // draw its shape
    Color color = getColor(content);
    if (color!=cBackground) {
      g.setColor(color);
      g.draw(shape, x, y, false);
    }
    // draw its content
    renderContent(g, x, y, shape, content);
    // done
  }
  
  /**
   * Calc color for given node   */
  private Color getColor(Object content) {
    // selected?
    if (cSelectedShape!=null&&content!=null&&content==selection) {
      return cSelectedShape;
    }
    // indi?
    if (content instanceof Indi)
      return cIndiShape;
    // fam?
    if (content instanceof Fam)
      return cFamShape;
    // unknown
    return cUnknownShape;
  }
  
  /**
   * Render the content of a node
   */
  private void renderContent(UnitGraphics g, double x, double y, Shape shape, Object content) {
    // safety check
    if (!isRenderContent||content==null) return;
    // preserve clip
    g.pushClip(x, y, shape.getBounds2D());
    // draw it
    g.draw(content.toString(), x, y);
    // restore clip
    g.popClip();
    // done
  }
  
  /**
   * Render the arcs
   */
  private void renderArcs(UnitGraphics g, Collection arcs) {
    // prepare color
    if (cArcs==cBackground) return;
    g.setColor(cArcs);
    // loop
    Iterator it = arcs.iterator();
    while (it.hasNext()) {
      // grab arc
      Arc arc = (Arc)it.next();
      // its path
      Path path = arc.getPath();
      if (path!=null) g.draw(path, 0, 0, false);
      // next
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
