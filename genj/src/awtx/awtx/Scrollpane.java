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
package awtx;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import javax.swing.SwingUtilities;

import awtx.scroll.*;

/**
 * A lightweight Scrollpane with support of five components
 * in places : NORTH,SOUTH,CENTER,WEST,EAST.
 * If you using getGraphics in one of the inserted components
 * you have to override getGraphics() in your provided component
 * like shown in class View. That makes sure that no component
 * draws over any sibling added to the same parent.
 */
public class Scrollpane extends Container {

  private Adjustable       adjustHori,adjustVert;
  private Component        compHori  ,compVert  ;
  private ScrolledContent  compContent;
  private Container        compEdge;
  private Color backgroundColor = Color.lightGray;

  /**
   * The Quadrant types we keep track of
   */
  public final static int
    CENTER = 0,
    NORTH  = 1,
    WEST   = 2,
    EAST   = 3,
    SOUTH  = 4,
    NUM_TYPES = 5;

  /**
   * How the center quadrant can be aligned
   */
  public final static int
    ALIGN_CENTER= 0,
    ALIGN_TOP   = 1,
    ALIGN_LEFT  = 2,
    ALIGN_RIGHT = 3,
    ALIGN_BOTTOM= 4;

  /**
   * Constructor with no args
   */
  public Scrollpane() {
    init();
  }

  /**
   * Constructor with center component
   * @param content component in center
   */
  public Scrollpane(Component content) {
    init();
    setQuadrant(CENTER,content);
  }

  /**
   * Adds a component to space in lower right corner
   */
  public void add2Edge(Component c) {
    compEdge.add(c);
  }

  /**
   * Adds an adjustment listener
   */
  public void addAdjustmentListener(AdjustmentListener listener) {
    adjustHori.addAdjustmentListener(listener);
    adjustVert.addAdjustmentListener(listener);
  }

  /**
   * Layouting
   */
  public void doLayout() {

    // Layout view&adjustables
    Dimension size = getSize();
    if ((!isVisible())||(size.width<=0)||(size.height<=0))
      return;

    Dimension scrollSize = new Dimension(
      compVert.getMinimumSize().width,
      compHori.getMinimumSize().height
    );
    Dimension edgeSize = new Dimension(
      Math.min(
      Math.max(compEdge.getMinimumSize().width ,scrollSize.width ),
      size.width-32
      ),
      Math.max(compEdge.getMinimumSize().height,scrollSize.height)
    );

    compContent.setBounds(
      0,
      0,
      size.width-scrollSize.width,
      size.height-edgeSize.height
    );
    compHori.setBounds(
      0,
      size.height-edgeSize.height,
      size.width-edgeSize.width,
      scrollSize.height
    );
    compVert.setBounds(
      size.width-scrollSize.width,
      0,
      scrollSize.width,
      size.height-edgeSize.height
    );
    compEdge.setBounds(
      size.width-edgeSize.width,
      size.height-edgeSize.height,
      edgeSize.width,
      edgeSize.height
    );

    compContent.doLayout();
    compContent.validate();

    // Done
  }

  /**
   * Return the size of the given component index
   */
  public Dimension getContentSize() {
    return compContent.getSize();
  }

  /**
   * Our minimum Size
   */
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  /**
   * Our preferred Size
   */
  public Dimension getPreferredSize() {
    return new Dimension(64,64);
  }

  /**
   * Returns one of the components used - please read the note in the class description
   * @param wherer one of CENTER,NORTH,WEST,SOUTH,EAST
    */
  public Component getQuadrant(int where) {
    return compContent.getQuadrant(where);
  }

  /**
   * Returns the scrolled position of CENTER
   */
  public Point getScrollPosition() {
    return new Point(
      adjustHori.getValue(),
      adjustVert.getValue()
    );
  }

  /**
   * An initializer
   */
  private void init() {

    LayoutManager lm = new LayoutManager() {
      // LCD
      public void layoutContainer(Container c) {
        System.out.println("1");
      }
      public Dimension minimumLayoutSize(Container c) {
        System.out.println("2");
        return new Dimension();
      }
      public void removeLayoutComponent(Component c) {
        System.out.println("3");
      }
      public void addLayoutComponent(String s,Component c) {
        System.out.println("4");
      }
      public Dimension preferredLayoutSize(Container c) {
        System.out.println("5");
        return new Dimension();
      }
      // EOC
    };
    setLayout(lm);

    // .. Fixed Components (Scrollers,View)
    compHori   = ComponentProvider.createAdjustable(Scrollbar.HORIZONTAL);
    compVert   = ComponentProvider.createAdjustable(Scrollbar.VERTICAL  );
    adjustHori = (Adjustable)compHori;
    adjustVert = (Adjustable)compVert;

    compEdge   = new Edge();

    compContent = new ScrolledContent(adjustHori, adjustVert);

    add(compHori);
    add(compVert);
    add(compContent);
    add(compEdge);

    // Something to view
    setBackground(backgroundColor);

    // Done
  }

  /**
   * In case of an invalidation we do our layout
   */
  /*
   * 
    this doesn't seem to work well with changing l&f because
    at some point invalidate is called and we start to perform
    layout operations which fails in ComboBox.getPreferredSize()
    which calls _dependant.getPreferredSize() which is in an
    instable state at that point
  */    
  public void invalidate() {
    super.invalidate();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {validate(); }//doLayout();}
    });
    
    
  }

  /**
   * Makes sure that a given point is visible
   */
  public void makeVisible(Point pos) {
    int y = adjustVert.getValue(),
        h = adjustVert.getVisibleAmount();
    if ((pos.y > y)&&(pos.y < y+h)) {
      return;
    }
    setScrollPosition(0,pos.y);
  }

  /**
   * Our paint for the Scrollpane
   */
  public void paint(Graphics g) {
    // Fill background
    g.setColor(backgroundColor);
    Rectangle bounds = getBounds();
    g.fillRect(bounds.x,bounds.y,bounds.width,bounds.height);
    // Continue
    super.paint(g);
  }

  /**
   * Removes an adjustment listener
   */
  public void removeAdjustmentListener(AdjustmentListener listener) {
    adjustHori.removeAdjustmentListener(listener);
    adjustVert.removeAdjustmentListener(listener);
  }

  /**
   * Sets a Quadrant - please read the note in the class description
   * @param which one of NORTH,SOUTH,CENTER,WEST,EAST
   * @param the component to use
   */
  public void setQuadrant(int which, Component comp) {
    compContent.setQuadrant(which,comp);
  }

  /**
   * Sets the scrolled position of CENTER
   */
  public void setScrollPosition(int xpos, int ypos) {
    adjustHori.setValue(xpos);
    adjustVert.setValue(ypos);
  }

  /**
   * Sets the scrolled position of CENTER
   */
  public void setScrollPosition(Point pos) {
    setScrollPosition(pos.x,pos.y);
  }

  /**
   * Sets the way how to use space in case there's more than CENTER needs
   */
  public void setUseSpace(int howx, int howy) {
    compContent.setUseSpace(howx,howy);
  }

}
