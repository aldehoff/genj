/**
 * Nils Abstract Window Toolkit
 *
 * Copyright (C) 2000 Nils Meier <nils@meiers.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package awtx.scroll;

import java.awt.*;

/**
 * A placeholder to be replaced by user defined components
 */
class Quadrant extends Container {

  /**
   * Remembering the view that's being hold
   */
  private Component view;
  private int       type;
  private boolean   suppressInvalidate;

  /**
   * Constructor
   */
  /*package*/ Quadrant(int type) {
    this.type=type;
  }

  /**
   * Our preferred size
   */
  public Dimension getPreferredSize() {
    if (view==null)
      return new Dimension(0,0);
    return view.getPreferredSize();
  }

  /**
   * Returns the view of this Quadrant
   */
  public Component getView() {
    return view;
  }

  /**
   * Our viewed component tells us that it has changed it's layout parameters
   */
  public void invalidate() {

    // Make sure we do not react to our own layout change
    // during updateView
    if (suppressInvalidate) {
      return;
    }
    super.invalidate();

  }

  /**
   * Sets the view to hold
   */
  void setView(Component c) {
    removeAll();
    view = c;
    add(view);
  }

  /**
   * Update of scrolled position
   */
  public void updateView(Quadrant quadrants[], Adjustable adjustHori, Adjustable adjustVert) {

    // Ready?
    if (view==null)
      return;
    Dimension pref = view.getPreferredSize();

    // Update Boundaries
    Rectangle bounds = new Rectangle(
      -adjustHori.getValue(),
      -adjustVert.getValue(),
      pref.width,
      pref.height
    );

    switch (type) {
      case ScrolledContent.WEST:
      case ScrolledContent.EAST:
        bounds.x=0;
        bounds.height=quadrants[ScrolledContent.CENTER].getPreferredSize().height;
        break;
      case ScrolledContent.NORTH:
      case ScrolledContent.SOUTH:
        bounds.y=0;
        bounds.width=quadrants[ScrolledContent.CENTER].getPreferredSize().width;
        break;
    }

    suppressInvalidate=true;
    view.setBounds(bounds);
    suppressInvalidate=false;

    // Remember us as being valid
    validate();

    // Done
  }
}
