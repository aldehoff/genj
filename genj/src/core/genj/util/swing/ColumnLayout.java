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
import java.awt.LayoutManager2;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A layout that arranges components in columns from top to bottom from left to right
 */
public class ColumnLayout implements LayoutManager2 {

  private ArrayList columns = new ArrayList(16);
  
  /**
   * a column
   */
  private class Column {
    
    /** weight of column */
    Point2D.Double weight;

    /** preferred size of column */
    Dimension preferred;

    /** rows in column */
    ArrayList rows = new ArrayList(16);

    /** constructor */
    Column() {
    }
    
    /** add a component */
    void add(Component comp, Object constraints) {
      
      // grab last row
      Row row; 
      if (rows.isEmpty()) {
        row = new Row();
        rows.add(row);
      } else {
        row = (Row)rows.get(rows.size()-1);
      }
      
      // add it there
      row.add(comp, constraints);
      
      // invalidate state
      invalidate(false);
      
      // done
    }
    
    /** new row */
    void add(Row row) {
      
      // remember
      rows.add(row);
      
      // invalidate state
      invalidate(false);
    }
    
    /** remove component */
    boolean remove(Component comp) {
      
      // look for appropriate row
      for (int r=0;r<rows.size();r++) {
        Row row = (Row)rows.get(r);
        if (row.remove(comp)) {
          // remove row if empty
          if (row.size()==0)
            rows.remove(row);
          // invalidate state
          invalidate(false);
          return true;
        }
      }
      
      // not removed
      return false;
    }
    
    /** size */
    int size() {
      return rows.size();
    }

    /** preferred dim */
    Dimension preferred() {
      // known?
      if (preferred!=null)
        return preferred;
      preferred = new Dimension();
      // loop over rows
      Iterator it = rows.iterator();
      while (it.hasNext()) {
        Row row = (Row)it.next();
        Dimension dim = row.preferred();
        preferred.width  = Math.max(preferred.width, dim.width);
        preferred.height+= dim.height;
      }
      // done
      return preferred;
    }
    
    /** invalidate */
    void invalidate(boolean recurse) {
      
      preferred = null;
      weight = null;

      if (recurse) for (int r=0;r<rows.size();r++) {
        Row row = (Row)rows.get(r);
        row.invalidate();
      }
    }
    
    /** layout */
    void layout(Container parent, int x) {

      // loop over rows
      for (int r=0,y=0;r<rows.size();r++) {
        
        Row row = (Row)rows.get(r);
        
        row.layout(parent, x, y);

        y += row.preferred().height;
      }

    }
    
  } //Column
  
  /**
   * a row
   */
  private class Row {

    /** weight of row */
    Point2D.Double weight;

    /** preferred dimension of row */
    Dimension preferred;
    
    /** components in row */
    ArrayList comps = new ArrayList(4);

    /** component weights */
    Map comp2weight = new HashMap();
    
    /** add a component */
    void add(Component c, Object constraint) {
      // remember
      comps.add(c);
      // any constraints to take into consideration?
      if (constraint instanceof Point2D) 
        comp2weight.put(c, constraint);
      // invalidate state
      invalidate();
    }
    
    /** remove component */
    boolean remove(Component comp) {
      // remove it
      if (!comps.remove(comp))
        return false;
      // any constraints to take into consideration?
      comp2weight.remove(comp);
      // invalidate state
      invalidate();
      // done
      return true;
    }
    
    /** size */
    int size() {
      return comps.size();
    }
    
    /** preferred dim */
    Dimension preferred() {
      // known?
      if (preferred!=null)
        return preferred;
      preferred = new Dimension();
      // loop over comps
      Iterator it = comps.iterator();
      while (it.hasNext()) {
        Component comp = (Component)it.next();
        Dimension dim = comp.getPreferredSize();
        preferred.width += dim.width;
        preferred.height = Math.max(preferred.height, dim.height);
      }
      // done
      return preferred;
    }
    
    /** invalidate */
    void invalidate() {
      preferred = null;
      weight = null;
    }
    
    /** layout */
    void layout(Container parent, int x, int y) {
      
      // loop over comps
      for (int c=0;c<comps.size();c++) {
        
        Component comp = (Component)comps.get(c);
        Dimension size = comp.getPreferredSize();
        comp.setBounds(x, y, size.width, size.height);
        x += size.width;
      }

    }
    
  } //Row
  
  /**
   * Constructor
   */
  public ColumnLayout() {
  }
  
  /**
   * Last column
   */
  private Column getLastColumn() {
    // create an initial column
    if (columns.isEmpty())
      columns.add(new Column());
    return (Column)columns.get(columns.size()-1);
  }
  
  /**
   * Component/Layout lifecycle callback
   * @param comp the added component
   * @param constraints a Point2D for x/y weight (if applicable)
   */
  public void addLayoutComponent(Component comp, Object constraints) {
    // fill current row
    getLastColumn().add(comp, constraints);
  }

  /**
   * Component/Layout lifecycle callback
   * @param comp the added component
   * @param name name of component is ignored
   */
  public void addLayoutComponent(String name, Component comp) {
    // fill current row
    getLastColumn().add(comp, null);
  }

  /**
   * Component/Layout lifecycle callback
   * @param comp the removed component
   */
  public void removeLayoutComponent(Component comp) {
    
    // look for appropriate column
    for (int c=0;c<columns.size();c++) {
      Column col = (Column)columns.get(c);
      if (col.remove(comp)) {
        // remove column if empty
        if (col.size()==0)
          columns.remove(col);
        break;
      }
    }

    // done
  }

  /**
   * Our maximum size isn't limited
   */
  public Dimension maximumLayoutSize(Container target) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  /**
   * minimum equals preferred layout size
   */
  public Dimension minimumLayoutSize(Container parent) {
    return preferredLayoutSize(parent);
  }

  /**
   * Our layout alignment
   */
  public float getLayoutAlignmentX(Container target) {
    return 0;
  }

  /**
   * Our layout alignment
   */
  public float getLayoutAlignmentY(Container target) {
    return 0;
  }

  /**
   * Component/Layout lifecycle callback
   */
  public void invalidateLayout(Container target) {
    for (int c=0;c<columns.size();c++) {
      Column col = (Column)columns.get(c);
      col.invalidate(true);
    }
  }

  /**
   * Start a new row
   */
  public void endRow(Container parent) {
    getLastColumn().add(new Row());
  }
  
  /**
   * Start a new column in parent
   */
  public void endColumn(Container parent) {
    columns.add(new Column());
  }
  
  /**
   * our preferred layout size
   */
  public Dimension preferredLayoutSize(Container parent) {

    // prepare total preferred
    Dimension result = new Dimension();

    // loop over columns
    Iterator it = columns.iterator();
    while (it.hasNext()) {
      Column col = (Column)it.next();
      Dimension dim = col.preferred();
      result.width += dim.width;
      result.height = Math.max(result.height, dim.height);
    }
    
    // done
    return result;
  }
  
  /**
   * Component/Layout lifecycle callback
   */
  public void layoutContainer(Container parent) {
    
    // FIXME gotta use up extra space
    
    // loop over columns
    for (int c=0,x=0;c<columns.size();c++) {
      
      Column column = (Column)columns.get(c);
      
      column.layout(parent, x);

      x += column.preferred().width;
    }
    
  }

//  /** a client key used for attaching weight information to a component */
//  private final static String WEIGHT = "WEIGHT";
//
//  /** number of elements (rows) per column */
//  private int columnSize = 4;
//
//  /** container analyzed */
//  private Container analyzed;
//
//  /** column information */
//  private Column[] columns = null;
//
//  /**
//   * Constructor
//   */
//  public ColumnLayout(int columnSize) {
//    this.columnSize = Math.max(1, columnSize);
//  }
//
//  /**
//   * layout container
//   */
//  public void layoutContainer(Container parent) {
//
//    // analyze first
//    analyzeColumns(parent);
//    
//    // consult insets
//    Insets insets = parent.getInsets();
//
//    // check what's available in horizontal over weight
//    double weightx = 0;
//    int avail = parent.getWidth() - insets.left - insets.right;
//    for (int i=0; i<columns.length; i++) {
//      weightx += columns[i].weightx;
//      avail -= columns[i].width;
//    }
//    double haow = weightx>0 ? avail/weightx : 0;
//
//    // place components
//    for (int i = 0, x = insets.left, y = insets.top; i < columns.length; i++) {
//
//      // colymn by column
//      Column column = columns[i];
//
//      // its width
//      int w = column.width + (int) (column.weightx * haow);
//      
//      // vertical available over weight
//      double vaow = (parent.getHeight()-insets.top-insets.bottom-column.height)/column.weighty;
//      if (Double.isNaN(vaow)||vaow<0)
//        vaow = 0;
//      
//      // loop over contained components
//      for (int j = 0; j < column.comps.length; j++) {
//
//        Component comp = column.comps[j];
//        if (comp == null)
//          break;
//
//        Point2D weight = getWeight(null, comp);
//
//        int h = comp.getPreferredSize().height + (int)(weight.getY()*vaow);
//
//        comp.setBounds(x, y, w, h);
//
//        y += h;
//
//      }
//
//      y = insets.top;
//      x += w;
//
//    }
//
//    // done      
//  }
//
//  /**
//   * Calculate minium size
//   */
//  public Dimension minimumLayoutSize(Container parent) {
//    return preferredLayoutSize(parent);
//  }
//
//  /**
//   * Calculate preferred size
//   */
//  public Dimension preferredLayoutSize(Container parent) {
//
//    // analyze first
//    analyzeColumns(parent);
//
//    // wrap
//    Dimension result = new Dimension(0, 0);
//    for (int i = 0; i < columns.length; i++) {
//      Column column = columns[i];
//      result.width += column.width;
//      result.height = Math.max(result.height, column.height);
//    }
//    
//    // consult inset
//    Insets insets = parent.getInsets();
//    result.width += insets.left + insets.right;
//    result.height+= insets.top  + insets.bottom;
//
//    // done
//    return result;
//  }
//
//  /**
//   * add callback
//   */
//  public void addLayoutComponent(String name, Component comp) {
//    // reset cached info
//    columns = null;
//  }
//
//  /**
//   * remove callback
//   */
//  public void removeLayoutComponent(Component comp) {
//    // reset cached info
//    columns = null;
//  }
//
//  /**
//   * Set the weight of a component
//   * @param weight the weight (can be null)
//   */
//  public static void setWeight(JComponent comp, Point2D weight) {
//    comp.putClientProperty(ColumnLayout.WEIGHT, weight);
//  }
//
//  /**
//   * Analyze columns
//   */
//  private void analyzeColumns(Container parent) {
//
//    // done?
//    if (analyzed == parent && columns != null)
//      return;
//
//    // calculate width and height of each column
//    columns = new Column[1 + parent.getComponentCount() / columnSize];
//
//    for (int i = 0; i < columns.length; i++) {
//
//      Column column = new Column();
//
//      for (int j = 0; j < columnSize; j++) {
//
//        // get component n
//        int n = i * columnSize + j;
//        if (n >= parent.getComponentCount())
//          break;
//        Component c = parent.getComponent(n);
//
//        // keep it
//        column.comps[j] = c;
//
//        // check its preference
//        Dimension dim = c.getPreferredSize();
//        column.width = Math.max(column.width, dim.width);
//        column.height += dim.height;
//
//        // width information?
//        Point2D weight = getWeight(null, c);
//        if (weight != null) {
//          column.weightx = Math.max(column.weightx, weight.getX());
//          column.weighty += weight.getY();
//        }
//
//        // next
//      }
//
//      columns[i] = column;
//    }
//
//    // done
//  }
//
//  /**
//   * Resolve weight for given component
//   */
//  private Point2D getWeight(Point2D result, Component comp) {
//    
//    // create result?
//    if (result==null)
//      result = new Point2D.Double();
//    
//    // component itself
//    if (comp instanceof JComponent) {
//      Object prop = ((JComponent) comp).getClientProperty(ColumnLayout.WEIGHT);
//      if (prop instanceof Point2D) {
//        Point2D weight = (Point2D) prop;
//        result.setLocation(Math.max(result.getX(), weight.getX()), Math.max(result.getY(), weight.getY()));
//      }
//    }
//
//    // recursive?
//    if (comp instanceof Container) {
//      Container container = (Container) comp;
//      for (int i = 0, j = container.getComponentCount(); i < j; i++)
//        result = getWeight(result, container.getComponent(i));
//    }
//
//    // done
//    return result;
//  }
//
//  /**
//   * Column information
//   */
//  private class Column {
//    private int width = 0;
//    private int height = 0;
//    private Component[] comps = new Component[columnSize];
//    private double weightx = 0;
//    private double weighty = 0;
//  };

} //ColumnLayout