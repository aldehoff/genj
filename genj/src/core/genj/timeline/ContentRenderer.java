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
package genj.timeline;

import genj.util.ImgIcon;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

/**
 * A renderer knowing how to render a ruler for the timeline
 */
public class ContentRenderer extends Renderer {
  
  /** centimeters per year */
  /*package*/ double cmPyear = 1.0D;
  
  /** whether we colorize or not */
  /*package*/ boolean colorize = true;
  
  /** whether we paint tags or not */
  /*package*/ boolean paintTags = false;
  
  /** whether we paint dates or not */
  /*package*/ boolean paintDates = true;
  
  /** whether we paint a grid or not */
  /*package*/ boolean paintGrid = false;
  
  /** 
   * Calculates the model size in pixels
   */
  public Dimension getDimension(Model model, FontMetrics fm) {
    return new Dimension(
      cm2pixels((model.max-model.min)*cmPyear),
      model.layers.size()*calcLayerHeight(fm)
    );
  }
  
  /**
   * Renders the model
   */
  public void render(Graphics g, Model model) {
    // prepare some data
    FontMetrics fm = g.getFontMetrics();
    Dimension d = getDimension(model, fm);
    // calculate the clipping
    Clip clip = new Clip(g, fm, model);
    // render grid
    renderGrid(g, model, d, clip);
    // render layers
    renderLayers(g, fm, model, clip);
    // done
  }
  
  /**
   * Renders a grid
   */
  private final void renderGrid(Graphics g, Model model, Dimension d, Clip clip) {
    // check 
    if (!paintGrid) return;
    // global color is light gray
    g.setColor(Color.lightGray);
    // prepare data
    int 
      from = (int)model.min,
      to = (int)model.max;
    // loop
    for (int year=from;year<=to;year++) {
      int x = cm2pixels((year-from)*cmPyear);
      g.drawLine(x, 0, x, d.height);
    }
    // done
  }

  /** 
   * Renders layers
   */
  private final void renderLayers(Graphics g, FontMetrics fm, Model model, Clip clip) {
    // global color is black
    g.setColor(Color.black);
    // loop
    List layers = model.layers;
    for (int l=0; l<layers.size(); l++) {
      if (l<clip.minLayer||l>clip.maxLayer) continue;
      List layer = (List)layers.get(l);
      renderEvents(g, fm, model, layer, l, clip);
    }
    // done
  }
  
  /** 
   * Renders a layer
   */
  private final void renderEvents(Graphics g, FontMetrics fm, Model model, List layer, int level, Clip clip) {
    // loop through events
    Iterator events = layer.iterator();
    Model.Event event = (Model.Event)events.next();
    while (true) {
      // already grabbing next because we paint as much as we can
      Model.Event next = events.hasNext() ? (Model.Event)events.next() : null;
      // check clipping and draw
      if ((next==null||next.from>clip.minYear)&&(event.from<clip.maxYear)) {
        renderEvent(g, fm, model, event, next, level);
      }
      // no more?
      if (next==null) break;
      // one more!
      event = next;
    } 
    // done
  }
  
  /**
   * Renders an event
   */
  private final void renderEvent(Graphics g, FontMetrics fm, Model model, Model.Event event, Model.Event next, int level) {
    // calculate some parameters
    int
      lh  = calcLayerHeight(fm),
      fd  = fm.getDescent(),
      x1  = cm2pixels((event.from-model.min)*cmPyear),
      x2  = cm2pixels((event.to-model.min)*cmPyear),
      w   = next == null ? Integer.MAX_VALUE : cm2pixels((next.from-model.timeBeforeEvent-event.from)*cmPyear),
      y   = level*calcLayerHeight(fm);

    boolean em  = event.pe.getEntity().equals(model.gedcom.getLastEntity());
    
    String txt = event.content;
    if (paintTags) txt = event.pe.getTag() + ' ' + txt;
    if (paintDates) txt = txt + '(' + event.pd + ')';

    // draw it's extend
    if (colorize) g.setColor(Color.blue);
    g.drawLine(x1-1, y+lh-2, x1-1, y+lh-1);
    g.drawLine(x1  , y+lh-1, x2  , y+lh-1);
    g.drawLine(x2+1, y+lh-2, x2+1, y+lh-1);
    
    // draw it's text and image (not extending past model.max)
    if (colorize) g.setColor(em ? Color.red : Color.black);
    
    pushClip(g, x1, y, w, lh);
    ImgIcon img = event.pe.getImage(false);
    img.paintIcon(g, x1, y+lh/2-img.getIconHeight()/2);
    x1+=img.getIconWidth();
    g.drawString(txt, x1, y + lh - fd);
    popClip(g);
    
    // done
  }
  
  /** 
   * Helper calculating layer height
   */
  protected int calcLayerHeight(FontMetrics fm) {
    return fm.getHeight()+1;
  }
  
  /**
   * Class for specialized Clip region
   */
  protected class Clip {
    /** attributes */
    double minYear, maxYear;
    int minLayer, maxLayer;
    /**
     * Constructor
     */
    protected Clip(Graphics g, FontMetrics fm, Model model) {
      Rectangle r = g.getClipBounds();
      minYear = model.min+pixels2cm(r.x)/cmPyear;
      maxYear = model.min+pixels2cm(r.x+r.width)/cmPyear;
      int lh = calcLayerHeight(fm);
      minLayer = r.y/lh;
      maxLayer = minLayer + r.height/lh + 1;
    }
  } //Clip
  
} //RulerRenderer
