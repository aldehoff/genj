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
import genj.util.ImgIcon;
import gj.ui.UnitGraphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

/**
 * A renderer knowing how to render a ruler for the timeline
 */
public class ContentRenderer {
  
  /** a mark used for demarking time spans */
  private final static Shape 
    FROM_MARK = calcFromMark(),
    TO_MARK = calcToMark();
  
  /** centimeters per year */
  /*package*/ double cmPyear = 1.0D;
  
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
   * Calculates the model size in pixels
   */
  public Dimension getDimension(Model model, FontMetrics fm) {
    return new Dimension(
      UnitGraphics.units2pixels(model.max-model.min, UnitGraphics.CENTIMETERS*cmPyear),
      UnitGraphics.units2pixels(model.layers.size(),fm.getHeight()+1)
    );
  }
  
  /**
   * Renders the model
   */
  public void render(Graphics g, Model model) {
    // prepare UnitGraphics
    UnitGraphics graphics = new UnitGraphics(g, 
      UnitGraphics.CENTIMETERS*cmPyear, 
      g.getFontMetrics().getHeight()+1
    );
    graphics.translate(-model.min,0);
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
    int dy = -fm.getDescent();
       
    // draw it's extend
    g.setColor(cTimespan);
    
    g.draw(FROM_MARK, event.from, level+1, Double.NaN, Double.NaN, 0, true);
    if (event.from!=event.to) {
      g.draw(event.from, level+1, event.to, level+1, 0, -1);
      g.draw(TO_MARK, event.to, level+1, Double.NaN, Double.NaN, 0, true);
    }

    // clipping from here    
    g.pushClip(event.from, level, next==null?Integer.MAX_VALUE:next.from, level+1);
        
    // draw its image
    ImgIcon img = event.pe.getImage(false);
    g.draw(img.getImage(), event.from, level+0.5);
    int dx=img.getIconWidth();

    // draw its tag    
    if (paintTags) {
      String tag = event.pe.getTag();
      g.setColor(cTag);
      g.draw(tag, event.from, level+1, 0, dx, dy);
      dx+=fm.stringWidth(tag)+fm.charWidth(' ');
    }

    // draw its text 
    if (cSelected!=null&&event.pe.getEntity()==selection) g.setColor(cSelected);
    else g.setColor(cText);
    String txt = event.content;
    g.draw(txt, event.from, level+1, 0, dx, dy);
    dx+=fm.stringWidth(txt)+fm.charWidth(' ');
    
    // draw its date
    if (paintDates) {
      String date = " (" + event.pd + ')';
      g.setColor(cDate);
      g.draw(date, event.from, level+1, 0, dx, dy);
    }

    // done with clipping
    g.popClip();

    // done
  }
  
  /**
   * Class for specialized Clip region
   */
/*    
  protected class Clip {
    double minYear, maxYear;
    int minLayer, maxLayer;
    protected Clip(Graphics g, FontMetrics fm, Model model) {
      Rectangle r = g.getClipBounds();
      minYear = model.min+pixels2cm(r.x)/cmPyear;
      maxYear = model.min+pixels2cm(r.x+r.width)/cmPyear;
      int lh = calcLayerHeight(fm);
      minLayer = r.y/lh;
      maxLayer = minLayer + r.height/lh + 1;
    }    
  } //Clip
*/  

  /**
   * Generates a mark
   */
  private static Shape calcFromMark() {
    Polygon result = new Polygon();
    result.addPoint(0,-1);
    result.addPoint(-3,-4);
    result.addPoint(-3,2);
    return result;
  }
  
  /**
   * Generates a mark
   */
  private static Shape calcToMark() {
    Polygon result = new Polygon();
    result.addPoint(0,-1);
    result.addPoint(3,-4);
    result.addPoint(3,2);
    return result;
  }
  
} //RulerRenderer
