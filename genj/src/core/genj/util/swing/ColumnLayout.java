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
package genj.util.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.geom.Point2D;

import javax.swing.JComponent;

/**
 * A layout that arranges components in columns from top to bottom from left to right
 */
public class ColumnLayout implements LayoutManager {

  /** a client key used for attaching weight information to a component */
  private final static String WEIGHT = "WEIGHT";

  /** number of elements (rows) per column */
  private int columnSize = 4;

  /** container analyzed */
  private Container analyzed;

  /** column information */
  private Column[] columns = null;

  /**
   * Constructor
   */
  public ColumnLayout(int columnSize) {
    this.columnSize = Math.max(1, columnSize);
  }

  /**
   * layout container
   */
  public void layoutContainer(Container parent) {

    // analyze first
    analyzeColumns(parent);
    
    // consult insets
    Insets insets = parent.getInsets();

    // check what's available in horizontal over weight
    double weightx = 0;
    int avail = parent.getWidth() - insets.left - insets.right;
    for (int i=0; i<columns.length; i++) {
      weightx += columns[i].weightx;
      avail -= columns[i].width;
    }
    double haow = weightx>0 ? avail/weightx : 0;

    // place components
    for (int i = 0, x = insets.left, y = insets.top; i < columns.length; i++) {

      // colymn by column
      Column column = columns[i];

      // its width
      int w = column.width + (int) (column.weightx * haow);
      
      // vertical available over weight
      double vaow = (parent.getHeight()-insets.top-insets.bottom-column.height)/column.weighty;
      if (Double.isNaN(vaow)||vaow<0)
        vaow = 0;
      
      // loop over contained components
      for (int j = 0; j < column.comps.length; j++) {

        Component comp = column.comps[j];
        if (comp == null)
          break;

        Point2D weight = getWeight(null, comp);

        int h = comp.getPreferredSize().height + (int)(weight.getY()*vaow);

        comp.setBounds(x, y, w, h);

        y += h;

      }

      y = insets.top;
      x += w;

    }

    // done      
  }

  /**
   * Calculate minium size
   */
  public Dimension minimumLayoutSize(Container parent) {
    return preferredLayoutSize(parent);
  }

  /**
   * Calculate preferred size
   */
  public Dimension preferredLayoutSize(Container parent) {

    // analyze first
    analyzeColumns(parent);

    // wrap
    Dimension result = new Dimension(0, 0);
    for (int i = 0; i < columns.length; i++) {
      Column column = columns[i];
      result.width += column.width;
      result.height = Math.max(result.height, column.height);
    }
    
    // consult inset
    Insets insets = parent.getInsets();
    result.width += insets.left + insets.right;
    result.height+= insets.top  + insets.bottom;

    // done
    return result;
  }

  /**
   * add callback
   */
  public void addLayoutComponent(String name, Component comp) {
    // reset cached info
    columns = null;
  }

  /**
   * remove callback
   */
  public void removeLayoutComponent(Component comp) {
    // reset cached info
    columns = null;
  }

  /**
   * Set the weight of a component
   * @param weight the weight (can be null)
   */
  public static void setWeight(JComponent comp, Point2D weight) {
    comp.putClientProperty(ColumnLayout.WEIGHT, weight);
  }

  /**
   * Analyze columns
   */
  private void analyzeColumns(Container parent) {

    // done?
    if (analyzed == parent && columns != null)
      return;

    // calculate width and height of each column
    columns = new Column[1 + parent.getComponentCount() / columnSize];

    for (int i = 0; i < columns.length; i++) {

      Column column = new Column();

      for (int j = 0; j < columnSize; j++) {

        // get component n
        int n = i * columnSize + j;
        if (n >= parent.getComponentCount())
          break;
        Component c = parent.getComponent(n);

        // keep it
        column.comps[j] = c;

        // check its preference
        Dimension dim = c.getPreferredSize();
        column.width = Math.max(column.width, dim.width);
        column.height += dim.height;

        // width information?
        Point2D weight = getWeight(null, c);
        if (weight != null) {
          column.weightx = Math.max(column.weightx, weight.getX());
          column.weighty += weight.getY();
        }

        // next
      }

      columns[i] = column;
    }

    // done
  }

  /**
   * Resolve weight for given component
   */
  private Point2D getWeight(Point2D result, Component comp) {
    
    // create result?
    if (result==null)
      result = new Point2D.Double();
    
    // component itself
    if (comp instanceof JComponent) {
      Object prop = ((JComponent) comp).getClientProperty(ColumnLayout.WEIGHT);
      if (prop instanceof Point2D) {
        Point2D weight = (Point2D) prop;
        result.setLocation(Math.max(result.getX(), weight.getX()), Math.max(result.getY(), weight.getY()));
      }
    }

    // recursive?
    if (comp instanceof Container) {
      Container container = (Container) comp;
      for (int i = 0, j = container.getComponentCount(); i < j; i++)
        result = getWeight(result, container.getComponent(i));
    }

    // done
    return result;
  }

  /**
   * Column information
   */
  private class Column {
    private int width = 0;
    private int height = 0;
    private Component[] comps = new Component[columnSize];
    private double weightx = 0;
    private double weighty = 0;
  };

} //ColumnLayout