/**
 * Nils Abstract Window Toolkit
 *
 * Copyright (C) 2000-2002 Nils Meier <nils@meiers.net>
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
import java.awt.event.*;

/**
 * Content of a scroll pane wrapping NORTH,SOUTH,WEST,EAST,CENTER
 */
public class ScrolledContent extends Container {

  // Some instance stuff
  private Quadrant quadrants[];
  private Adjustable horiAdjustable,vertAdjustable;
  private Color unoccupiedColor = Color.gray;
  private Point useSpace = new Point(ALIGN_CENTER,ALIGN_CENTER);

  final static int
    CENTER = awtx.Scrollpane.CENTER,
    NORTH  = awtx.Scrollpane.NORTH ,
    WEST   = awtx.Scrollpane.WEST  ,
    EAST   = awtx.Scrollpane.EAST  ,
    SOUTH  = awtx.Scrollpane.SOUTH ,
    NUM_TYPES = 5;

  final static int
    ALIGN_CENTER= awtx.Scrollpane.ALIGN_CENTER,
    ALIGN_TOP   = awtx.Scrollpane.ALIGN_TOP,
    ALIGN_LEFT  = awtx.Scrollpane.ALIGN_LEFT,
    ALIGN_RIGHT = awtx.Scrollpane.ALIGN_RIGHT,
    ALIGN_BOTTOM= awtx.Scrollpane.ALIGN_BOTTOM;

  /**
   * Constructor for setting up quadrants
   */
  public ScrolledContent(Adjustable h, Adjustable v) {

    // Remember
    horiAdjustable = h;
    vertAdjustable = v;

    // Setup data
    quadrants = new Quadrant[NUM_TYPES];

    for (int i=quadrants.length-1;i>=0;i--) {
      quadrants[i] = new Quadrant(i);
      add(quadrants[i]);
    }

    // Listen
    AdjustmentListener alistener = new AdjustmentListener() {
      // LCD
      public void adjustmentValueChanged(AdjustmentEvent ae) {
        for (int i=0;i<quadrants.length;i++)
          quadrants[i].updateView(quadrants,horiAdjustable,vertAdjustable);
      }
      // EOC
    };
    h.addAdjustmentListener(alistener);
    v.addAdjustmentListener(alistener);

    // Done
  }

  /**
   * Layout components CENTER,NORTH,SOUTH,EAST,WEST
   */
  public void doLayout() {

    if (!isVisible()||(getSize().width==0)||(getSize().height==0)) {
      return;
    }

    // Prepare some parameters
    Dimension   viewSize = getSize();

    // .. calculate space available for center
    Rectangle centerBounds = new Rectangle(
      quadrants[WEST ].getPreferredSize().width ,
      quadrants[NORTH].getPreferredSize().height,
      viewSize.width  - quadrants[WEST ].getPreferredSize().width - quadrants[EAST ].getPreferredSize().width ,
      viewSize.height - quadrants[NORTH].getPreferredSize().height- quadrants[SOUTH].getPreferredSize().height
    );

    // .. update state of adjustables
    updateAdjustables(centerBounds);

    // .. for CENTER
    if (! ((Component)horiAdjustable).isEnabled() ) { // x-alignment needed?
      int newWidth = quadrants[CENTER].getPreferredSize().width;
      switch ( useSpace.x ) {
        case ALIGN_CENTER:
          centerBounds.x += (centerBounds.width - newWidth )/2;
          break;
        case ALIGN_LEFT:
          break;
        case ALIGN_RIGHT:
          centerBounds.x += centerBounds.width-newWidth;
          break;
      }
      centerBounds.width = newWidth;
    }

    if (! ((Component)vertAdjustable).isEnabled() ) { // y-alignment needed?
      int newHeight = quadrants[CENTER].getPreferredSize().height;
      switch ( useSpace.y ) {
        case ALIGN_CENTER:
          centerBounds.y += (centerBounds.height- newHeight)/2;
          break;
        case ALIGN_TOP:
          break;
        case ALIGN_BOTTOM:
          centerBounds.y += centerBounds.height-newHeight;
          break;
      }
      centerBounds.height = newHeight;
    }

    quadrants[CENTER].setBounds(centerBounds);

    // .. for NORTH
    quadrants[NORTH ].setBounds(
      centerBounds.x,
      0,
      centerBounds.width,
      quadrants[NORTH].getPreferredSize().height
    );

    // .. for SOUTH
    quadrants[SOUTH ].setBounds(
      centerBounds.x,
      viewSize.height - quadrants[SOUTH].getPreferredSize().height,
      centerBounds.width,
      quadrants[SOUTH].getPreferredSize().height
    );

    // .. for WEST
    quadrants[WEST  ].setBounds(
      0,
      centerBounds.y,
      quadrants[WEST].getPreferredSize().width,
      centerBounds.height
    );

    // .. for EAST
    quadrants[EAST  ].setBounds(
      viewSize.width - quadrants[EAST].getPreferredSize().width,
      centerBounds.y,
      quadrants[EAST].getPreferredSize().width,
      centerBounds.height
    );

    // Remember views being valid
    for (int i=0;i<quadrants.length;i++)
      quadrants[i].validate();

    // Done layoutContainer()
  }

  /**
   * Our preferredSize is [0,0]
   */
  public Dimension getPreferredSize() {
    return new Dimension(0,0);
  }

  /**
   * Returns one of the components used - please read the note in the class description
   * @param wherer one of CENTER,NORTH,WEST,SOUTH,EAST
    */
  public Component getQuadrant(int where) {
    return quadrants[where].getView();
  }

  /**
   * Our painting and painting of our lightweight children
   */
  public void paint(Graphics g) {

    // Prepare some params
    Rectangle bounds = getBounds();

    Rectangle cBounds = quadrants[CENTER].getBounds();

    int nMargin = quadrants[NORTH].getSize().height,
      sMargin = quadrants[SOUTH].getSize().height,
      wMargin = quadrants[WEST].getSize().width,
      eMargin = quadrants[EAST].getSize().width;

    // Fill background
    g.setColor(unoccupiedColor);
    g.fillRect(
      bounds.x + wMargin,
      bounds.y + nMargin,
      bounds.width - wMargin - eMargin,
      bounds.height - nMargin - sMargin);

    // This was super.paint(g); previously but IE's implementation
    // of graphics.clipRect() is wrong resulting in bad clips to the children :(
    // (the end-result could be <0 for clip.x/y)
    Rectangle clip = g.getClipBounds();
    for (int q = quadrants.length - 1; q >= 0; q--) {
      Quadrant quadrant = quadrants[q];
      Rectangle cr = quadrant.getBounds();
      if ((clip == null) || cr.intersects(clip)) {
        Rectangle dr = cr.intersection(clip);
        g.setClip(dr.x,dr.y,dr.width,dr.height);
        g.translate(cr.x, cr.y);
        try {
          quadrant.paint(g);
        } finally {
          g.translate(-cr.x,-cr.y);
        }
      }
    }

    // Done
  }

  /**
   * Sets a Quadrant - please read the note in the class description
   * @param which one of NORTH,SOUTH,CENTER,WEST,EAST
   * @param the component to use
   */
  public void setQuadrant(int which, Component comp) {
    quadrants[which].setView(comp);
    doLayout();
    repaint();
  }

  /**
   * Sets the way how to use space in case there's more than CENTER needs
   */
  public void setUseSpace(int howx, int howy) {
    useSpace.x = howx;
    useSpace.y = howy;
  }

  /**
   * Helper for updating the state of the adjustables
   * @param centerSize the space available for the component CENTER
   */
  private void updateAdjustables(Rectangle centerSize) {

    Dimension prefSize = quadrants[CENTER].getPreferredSize();

    // .. Horizontal
    horiAdjustable.setBlockIncrement(Math.max(1,centerSize.width));
    horiAdjustable.setMinimum(0);
    horiAdjustable.setMaximum(prefSize.width);
    horiAdjustable.setValue(
      Math.min(horiAdjustable.getValue(),prefSize.width-centerSize.width)
    );

    if (prefSize.width>centerSize.width) {
      horiAdjustable.setVisibleAmount(centerSize.width);
      ((Component)horiAdjustable).setEnabled(true);
    } else {
      horiAdjustable.setVisibleAmount(prefSize.width);
      ((Component)horiAdjustable).setEnabled(false);
    }

    // .. Vertical
    vertAdjustable.setBlockIncrement(Math.max(1,centerSize.height));
    vertAdjustable.setMinimum(0);
    vertAdjustable.setMaximum(prefSize.height);
    vertAdjustable.setValue(
      Math.min(vertAdjustable.getValue(),prefSize.height-centerSize.height)
    );

    if (prefSize.height>centerSize.height) {
      vertAdjustable.setVisibleAmount(centerSize.height);
      ((Component)vertAdjustable).setEnabled(true);
    } else {
      vertAdjustable.setVisibleAmount(prefSize.height);
      ((Component)vertAdjustable).setEnabled(false);
    }

    // Done
  }
}
