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
package genj.util;

import java.awt.*;

/**
 * This class provides basic helper functions for
 * managing java.awt.GridBagLayout
 */
public class GridBagHelper {

  private GridBagLayout layout;
  private GridBagConstraints constraints;
  private Container container;

  public final static int
    FILL_HORIZONTAL =  1,
    FILL_VERTICAL   =  2,
    FILL_BOTH       =  4,
    FILL_NONE       =  8,
    GROW_HORIZONTAL = 16,
    GROW_VERTICAL   = 32,
    GROW_BOTH       = 64;

  /**
   * A filling component
   */
  private class Fill extends Component {
    // LCD
    Dimension size;
    Fill(Dimension size) {
      this.size=size;
    }
    public Dimension getPreferredSize() {
      return size;
    }
    public Dimension getMinimumSize() {
      return size;
    }
    // EOC
  }

  /**
   * Constructor
   * @param container target to layout
   */
  public GridBagHelper(Container container) {

    // Remember container
    this.container=container;

    // Prepare Layout
    layout = new GridBagLayout();
    container.setLayout(layout);

    // .. and constraints
    constraints = new GridBagConstraints();

    // Done
  }

  /**
   * Adds component to container with GridBagLayout (1x1 grids)
   * @param component component to add
   * @param x horizontal position in grid
   * @param y vertical position in grid
   * @return Added component
   */
  public Component add(Component component,int x,int y) {
    return add(component,x,y,1,1,0);
  }

  /**
   * Adds component to container with GridBagLayout
   * @param component component to add
   * @param x horizontal position in grid
   * @param y vertical position in grid
   * @param w width
   * @param h height
   * @return Added component
   */
  public Component add(Component component,int x,int y,int w,int h) {
    return add(component,x,y,w,h,0);
  }

  /**
   * Adds component to container with GridBagLayout
   * @param component component to add
   * @param x horizontal position in grid
   * @param y vertical position in grid
   * @param w width
   * @param h height
   * @param parm parameter via constants FILL_HORIZONTAL, FILL_VERTICAL, FILL_BOTH
   * GROW_HORIZONTAL, GROW_VERTICAL, GROW_BOTH
   * @return Added component
   */
  public Component add(Component component,int x,int y,int w,int h,int parm) {
    return add(component,x,y,w,h,parm,new Insets(0,0,0,0));
  }

  /**
   * Adds component to container with GridBagLayout
   * @param component component to add
   * @param x horizontal position in grid
   * @param y vertical position in grid
   * @param w width
   * @param h height
   * @param parm parameter via constants FILL_HORIZONTAL, FILL_VERTICAL, FILL_BOTH
   * GROW_HORIZONTAL, GROW_VERTICAL, GROW_BOTH
   * @param insets component's insets
   * @return Added component
   */
  public Component add(Component component,int x,int y,int w,int h,int parm, Insets insets) {

    // Add component to container
    container.add(component);

    // Prepare constraints
    constraints.gridx     = x;
    constraints.gridy     = y;
    constraints.gridwidth = w;
    constraints.gridheight= h;
    constraints.weightx   = isSet(parm,GROW_BOTH) || isSet(parm,GROW_HORIZONTAL) ? 1 : 0;
    constraints.weighty   = isSet(parm,GROW_BOTH) || isSet(parm,GROW_VERTICAL  ) ? 1 : 0;
    constraints.fill      = GridBagConstraints.BOTH;
    constraints.insets    = insets;

    if ( isSet(parm,FILL_BOTH) || (isSet(parm,FILL_HORIZONTAL)&&isSet(parm,FILL_VERTICAL)) )
      constraints.fill = GridBagConstraints.BOTH      ;
    else if (isSet(parm,FILL_HORIZONTAL))
      constraints.fill = GridBagConstraints.HORIZONTAL;
    else if (isSet(parm,FILL_VERTICAL  ))
      constraints.fill = GridBagConstraints.VERTICAL  ;
    else if (isSet(parm,FILL_NONE      ))
      constraints.fill = GridBagConstraints.NONE      ;

    // Set constraints
    layout.setConstraints(component,constraints);

    // Done
    return component;
  }

  /**
   * Add a filling component that grows to maximum but shows nothing
   */
  public void addFiller(int x, int y) {
    add(new Label(), x, y, 1, 1, GROW_BOTH);
  }

  /**
   * Add a filling component that takes the given space
   */
  public void addFiller(int x, int y, Dimension dim) {
    add(new Fill(dim), x, y, 1, 1);
  }

  /**
   * Helper for checking bit mask
   */
  private boolean isSet(int value, int mask) {
    return ((value&mask)!=0);
  }

}
