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

import genj.gedcom.Entity;
import genj.util.swing.ImageIcon;
import genj.util.swing.UnitGraphics;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

/**
 * A renderer knowing how to render a ruler for the timeline
 */
public class ContentRenderer {
  
  /** a mark used for demarking time spans */
  private Shape FROM_MARK, TO_MARK; 
  
  /** whether we paint tags or not */
  /*package*/ boolean paintTags = false;
  
  /** whether we paint dates or not */
  /*package*/ boolean paintDates = true;
  
  /** whether we paint a grid or not */
  /*package*/ boolean paintGrid = false;
  
  /** an entity that we consider selected */
  /*package*/ Entity selection = null;
  
  /** background color */
  /*package*/ Color cBackground = null;
  
  /** text color */
  /*package*/ Color cText = null;
  
  /** tag color */
  /*package*/ Color cTag = null;
  
  /** date color */
  /*package*/ Color cDate = null;
  
  /** timespane color */
  /*package*/ Color cTimespan= null;
  
  /** timespane color */
  /*package*/ Color cGrid = null;
  
  /** selected color */
  /*package*/ Color cSelected = null;
  
  /**
   * Renders the model
   */
  public void render(UnitGraphics graphics, Model model) {
    // calculate marks
    FROM_MARK = calcFromMark(graphics.getUnit());
    TO_MARK = calcToMark(graphics.getUnit());
    // render background
    renderBackground(graphics, model);
    // render grid
    renderGrid(graphics, model);
    // render layers
    renderLayers(graphics, model);
    // done
  }
  
  /**
   * Renders the background
   */
  protected void renderBackground(UnitGraphics g, Model model) {
    if (cBackground==null) return;
    g.setColor(cBackground);
    Rectangle2D r = new Rectangle2D.Double(model.min, 0, model.max-model.min, 1024);
    g.draw(r, 0, 0, true);
  }
  
  /**
   * Renders a grid
   */
  private final void renderGrid(UnitGraphics g, Model model) {
    // check 
    if (!paintGrid) return;
    // color 
    g.setColor(cGrid);
    // loop
    Rectangle2D r = g.getClip();
    int layers = model.layers.size();
    double 
      from = Math.floor(r.getMinX()),
      to = Math.ceil(r.getMaxX());
    for (double year=from;year<=to;year++) {
      g.draw(year, 0, year, layers);
    }
    // done
  }

  /** 
   * Renders layers
   */
  private final void renderLayers(UnitGraphics g, Model model) {
    // check clip as we go
    Rectangle2D clip = g.getClip();
    // loop
    List layers = model.layers;
    for (int l=0; l<layers.size(); l++) {
      if (l<Math.floor(clip.getMinY())||l>Math.ceil(clip.getMaxY())) continue;
      List layer = (List)layers.get(l);
      renderEvents(g, model, layer, l);
    }
    // done
  }
  
  /** 
   * Renders a layer
   */
  private final void renderEvents(UnitGraphics g, Model model, List layer, int level) {
    // check clip as we go
    Rectangle2D clip = g.getClip();
    // loop through events
    Iterator events = layer.iterator();
    Model.Event event = (Model.Event)events.next();
    while (true) {
      // already grabbing next because we paint as much as we can
      Model.Event next = events.hasNext() ? (Model.Event)events.next() : null;
      // check clipping and draw
      if ((next==null||next.from>clip.getMinX())&&(event.from<clip.getMaxX())) {
        renderEvent(g, model, event, next, level);
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
  private final void renderEvent(UnitGraphics g, Model model, Model.Event event, Model.Event next, int level) {

    // calculate some parameters
    boolean em  = event.pe.getEntity() == selection;
    FontMetrics fm = g.getFontMetrics();
       
    // draw it's extend
    g.setColor(cTimespan);
    
    g.draw(FROM_MARK, event.from, level+1, true);
    if (event.from!=event.to) {
      
      // FIXME : calculate 1 pixel before starting to draw
      g.draw(
        event.from, 
        level+1 -(float)(1F/g.getUnit().getY()), 
        event.to, 
        level+1 -(float)(1F/g.getUnit().getY())
      );
      g.draw(TO_MARK, event.to, level+1, true);
    }

    // clipping from here    
    g.pushClip(event.from, level, next==null?Integer.MAX_VALUE:next.from, level+1);
        
    // draw its image
    ImageIcon img = event.pe.getImage(false);
    g.draw(img, event.from, level+0.5);
    int dx=img.getIconWidth();

    // draw its tag    
    if (paintTags) {
      String tag = event.pe.getTag();
      g.setColor(cTag);
      g.draw(tag, event.from, level+1, 0, 1, dx, 0);
      dx+=fm.stringWidth(tag)+fm.charWidth(' ');
    }

    // draw its text 
    if (cSelected!=null&&event.pe.getEntity()==selection) g.setColor(cSelected);
    else g.setColor(cText);
    String txt = event.content;
    g.draw(txt, event.from, level+1, 0, 1, dx, 0);
    dx+=fm.stringWidth(txt)+fm.charWidth(' ');
    
    // draw its date
    if (paintDates) {
      String date = " (" + event.pd + ')';
      g.setColor(cDate);
      g.draw(date, event.from, level+1, 0, 1, dx, 0);
    }

    // done with clipping
    g.popClip();

    // done
  }
  
  /**
   * Generates a mark
   */
  private Shape calcFromMark(Point2D unit) {
    GeneralPath result = new GeneralPath();
    result.moveTo((float)( 0F/unit.getX()),(float)(-1F/unit.getY()));
    result.lineTo((float)(-3F/unit.getX()),(float)(-5F/unit.getY()));
    result.lineTo((float)(-3F/unit.getX()),(float)(+3F/unit.getY()));
    result.closePath();
    return result;
  }
  
  /**
   * Generates a mark
   */
  private Shape calcToMark(Point2D unit) {
    GeneralPath result = new GeneralPath();
    result.moveTo((float)( 0F/unit.getX()),(float)(-1F/unit.getY()));
    result.lineTo((float)( 4F/unit.getX()),(float)(-6F/unit.getY()));
    result.lineTo((float)( 4F/unit.getX()),(float)(+4F/unit.getY()));
    result.closePath();
    return result;
  }
  
} //RulerRenderer
