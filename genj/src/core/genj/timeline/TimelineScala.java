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
 * The scala at the lower portion of the window showing the years
 */
class TimelineScala extends Component {

  /** members */
  private TimelineModel model;
  private TimelineView  timeline;

  /**
   * Returns the logical size of the renderer's view
   */
  public Dimension getPreferredSize() {
    return new Dimension(
      16,
      getGraphics().getFontMetrics().getHeight()
    );
  }

  /**
   * Paints this content's data
   */
  public void paint(Graphics g) {

    Dimension size = getSize();

    // Draw background
    g.setColor(Color.white);
    g.fillRect(0,0,size.width,size.height);

    // Calc some parms
    FontMetrics fm = g.getFontMetrics();
    int    w       = size.width ,
           h       = size.height,
           fh      = fm.getHeight()  ,
           minYear = model.getMinYear(),
           maxYear = model.getMaxYear();

    // Calculate how to label
    int modulo = 1;
    if (timeline.getPixelsPerYear()<=2*fm.stringWidth("9999")) {
      modulo = 1 + (int)Math.ceil( fm.stringWidth("9999")*2 / timeline.getPixelsPerYear() );
    }

    // Draw ticks
    int x = 0;
    for (int y = minYear; (x < w) && (y<=maxYear); y++) {

      // Calculate XPos
      x = (y-minYear) * timeline.getPixelsPerYear();

      // Label and tick
      g.setColor(Color.lightGray);
      if ((y-minYear)%modulo==0) {
        g.setColor(Color.black);
        String s = ""+y;
        g.drawString(
          s,
          x - (y==minYear ? 0 : fm.stringWidth(s)/(y==maxYear?1:2) ),
          h-fm.getMaxDescent()
        );
      }
      g.drawLine(x,0,x,4);

      // .. next
    }

    // Done
  }

  /**
   * Constructor
   */
  TimelineScala(TimelineModel model, TimelineView timeline) {

    // Remember
    this.model = model;
    this.timeline = timeline;

    // Done
  }

}
