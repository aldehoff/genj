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
      render(g, model, layer, l);
    }
    // done
  }
  
  /** 
   * Renders a layer
   */
  private void render(Graphics g, Model model, List layer, int level) {
    
    // prepare parameters
    int 
      fh = g.getFontMetrics().getHeight(),
      fd = g.getFontMetrics().getDescent();
    
    // loop through events
    Iterator events = layer.iterator();
    while (events.hasNext()) {
      
      // here's the event
      Model.Event event = (Model.Event)events.next();
      int     x1  = cm2pixels((event.from-model.min)*cmPyear);
      int     x2  = cm2pixels((event.to-model.min)*cmPyear);
      String  tag = event.prop.getTag() + " of " + event.prop.getEntity() + " (" + event.prop.getDate() + ")";
      int     y   = level*fh;
      ImgIcon img = event.prop.getImage(false);
      boolean em  = event.prop.getEntity().equals(model.gedcom.getLastEntity());

      // color
      if (colorize) g.setColor(em ? Color.red : Color.black);
      
      // draw it's extend
      g.drawLine(x1, y+fh-1, x1, y+fh);
      g.drawLine(x1, y+fh  , x2, y+fh);
      g.drawLine(x2, y+fh-1, x2, y+fh);
      
      // .. image
      img.paintIcon(g, x1, y+fh/2-img.getIconHeight()/2);
      
      // .. txt
      g.drawString(tag, x1 + img.getIconWidth(), y + fh - fd);
      
      // next
    }
    // done
  }

} //RulerRenderer
