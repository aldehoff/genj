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

import java.awt.*;
import java.awt.event.*;

import genj.gedcom.*;

/**
 * The component showing the timeline content
 */
class TimelineContent extends Component {

  /** members */
  private TimelineModel model;
  private TimelineView  timeline;

  /**
   * Returns the logical size of the renderer's view
   */
  public Dimension getPreferredSize() {
    return new Dimension(
      model.getYears()*timeline.getPixelsPerYear(),
      model.getLevels()*getGraphics().getFontMetrics().getHeight()*2
    );
  }

  /**
   * Paints this content's data
   */
  public void paint(Graphics g) {

    // Fill Background
    Dimension size = getSize();
    g.setColor(Color.white);
    g.fillRect(0,0,size.width,size.height);

    // Calc some parms
    FontMetrics fm = g.getFontMetrics();
    int    minYear = model.getMinYear(),
           maxYear = model.getMaxYear();

    // Render the links
    renderLinks(g,fm);

    // Done
  }

  /**
   * Helper that renders the links from model
   */
  private void renderLinks(Graphics g, FontMetrics fm) {

    // Prepare painting
    int min = model.getMinYear(),
        max = model.getMaxYear();

    final TimelineGraphics tg = new TimelineGraphics(
      g,
      min,
      max,
      timeline.getPixelsPerYear(),
      timeline.isPaintTags(),
      timeline.isPaintDates()
    );

    // Draw Grid
    if (timeline.isPaintGrid()) {

      g.setColor(Color.lightGray);

      int x;
      int h = getSize().height;
      for (int y=min;y<=max;y++) {
        x = tg.year2pixel(y);
        g.drawLine(x,0,x,h);
      }
    }

    // Draw Links
    g.setColor(Color.black);

    TimelineModel.Action action = new TimelineModel.Action() {
      // LCD
      /** members */
      private float   max     = 0.0F;
      private boolean visible = false;
      /** what to do for every link */
      public int doAction(Link link, boolean newLevel) {
        // Setup level of TimelineGraphics
        if (newLevel) {
          // .. tell graphics
          tg.incLevel();
          // .. visible level ?
          if (!tg.isVisibleLevel()) {
            return TimelineModel.Action.BREAK_LEVEL;
          }
          // .. prepare maximum for leftmost (first) link
          max    = model.getMaxYear();
          // .. initially none is visible
          visible=false;
        }
        // Visible ?
        if (tg.areVisibleYears(link.fromYear,max)) {
          // .. draw visible
          link.drawOn(tg,max);
          // .. turn to visible mode
          visible=true;
          // ...
        } else {
          // .. invisible even though we were visible already ?
          if (visible) {
            // .. end this level
            return TimelineModel.Action.BREAK_LEVEL;
          }
          // ...
        }
        // Maximum for next link is begin of this one
        max = link.toYear;
        // Done
        return TimelineModel.Action.CONTINUE;
      }
      // EOC
    };

    model.doForAll(action);

    // Done
  }

  /**
   * Constructor
   */
  TimelineContent(TimelineModel model, TimelineView timeline) {

    // Remember
    this.model = model;
    this.timeline = timeline;

    // Done
  }

}
