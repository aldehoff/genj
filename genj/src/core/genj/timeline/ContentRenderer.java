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
import java.util.Iterator;
import java.util.List;

/**
 * A renderer knowing how to render a ruler for the timeline
 */
public class ContentRenderer extends Renderer {
  
  /** centimeters per year */
  /*package*/ double cmPyear = 1.0D;
  
  /** pixels per event*/
  /*package*/ int pixelsPevent = 128;
  
  /** whether we colorize or not */
  /*package*/ boolean colorize = true;
  
  /** 
   * Calculates the model size in pixels
   */
  public Dimension getDimension(Model model, FontMetrics metrics) {
    return new Dimension(
      cm2pixels((model.max-model.min)*cmPyear),
      model.layers.size()*(metrics.getHeight()+1)
    );
  }
  
  /**
   * Renders the model
   */
  public void render(Graphics g, Model model) {
    // loop through layers
    Iterator layers = model.layers.iterator();
    for (int l=0; layers.hasNext(); l++) {
      List layer = (List)layers.next();
      render(g, g.getFontMetrics(), model, layer, l);
    }
    // done
  }
  
  /** 
   * Renders a layer
   */
  private final void render(Graphics g, FontMetrics fm, Model model, List layer, int level) {
    // loop through events
    Iterator events = layer.iterator();
    while (events.hasNext()) {
      Model.Event event = (Model.Event)events.next();
      render(g, fm, model, event, level);
    }
    // done
  }
  
  /**
   * Renders an event
   */
  private final void render(Graphics g, FontMetrics fm, Model model, Model.Event event, int level) {
    // calculate some parameters
    int
      fh  = fm.getHeight(),
      fd  = fm.getDescent(),
      x1  = cm2pixels((event.from-model.min)*cmPyear),
      x2  = cm2pixels((event.to-model.min)*cmPyear),
      y   = level*(fh+1);

    boolean em  = event.prop.getEntity().equals(model.gedcom.getLastEntity());

    // draw it's extend
    if (colorize) g.setColor(Color.blue);
    g.drawLine(x1-1, y+fh-1, x1-1, y+fh);
    g.drawLine(x1, y+fh  , x2, y+fh);
    g.drawLine(x2+1, y+fh-1, x2+1, y+fh);
    
    // draw it's text and image (not extending past model.max)
    if (colorize) g.setColor(em ? Color.red : Color.black);
    
    ImgIcon img = event.prop.getImage(false);
    double years = pixels2cm(Math.min(pixelsPevent,img.getIconWidth() + fm.stringWidth(event.tag)))/cmPyear;
    if (event.from+years > model.max) {
      x1 = cm2pixels((model.max-model.min-years)*cmPyear);
    }
    pushClip(g, x1, y, pixelsPevent, fh+1);
    img.paintIcon(g, x1, y+fh/2-img.getIconHeight()/2);
    x1+=img.getIconWidth();
    g.drawString(event.tag, x1, y + fh - fd);
    popClip(g);
    
    // done
  }
  
} //RulerRenderer
