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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.Scrollable;

/**
 * The scala for our content 
 */
/*package*/ class Ruler extends JComponent implements Scrollable {

  /** the model */
  private Model model;
  
  /**
   * Constructor
   */
  /*package*/ Ruler(Model model) {
    // remember
    this.model = model;
    // done
  }

  /**
   * @see javax.swing.JComponent#paintComponent(Graphics)
   */
  protected void paintComponent(Graphics g) {
    Rectangle r = getBounds();
    g.setColor(Color.white);
    g.fillRect(0,0,r.width,r.height);
    g.setColor(Color.blue);
    g.drawRect(0,0,r.width-1,r.height-1);
  }

  /**
   * @see java.awt.Component#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(256,32);
  }

  /**
   * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
   */
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  /**
   * @see javax.swing.Scrollable#getScrollableBlockIncrement(Rectangle, int, int)
   */
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 10;
  }

  /**
   * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
   */
  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  /**
   * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
   */
  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  /**
   * @see javax.swing.Scrollable#getScrollableUnitIncrement(Rectangle, int, int)
   */
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 1;
  }
  
} //Ruler
