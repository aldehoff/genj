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
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * A layout that arranges components in nested blocks (rows, columns)
 */
public class NestedBlockLayout implements LayoutManager2 {

  /** one root row is holds all the columns */
  private Block root;
  
  /** level of components */
  private int componentLevel = 1;
  
  /** padding */
  private int padding = 1;
  
  /**
   * Constructor
   */
  public NestedBlockLayout(boolean startWithRow, int componentLevel) {
    
    if (componentLevel<1)
      throw new IllegalArgumentException(componentLevel+"<1");

    root = startWithRow ? (Block)new Row() : (Block)new Column();
    
    this.componentLevel = componentLevel;
    
  }
  
  /**
   * an block in the layout
   */
  private abstract class Block {
    
    /** preferred size of column */
    Dimension preferred;

    /** weight of column */
    Point2D.Double weight;
    
    /** subs */
    ArrayList subs = new ArrayList(16);
    
    /** remove */
    boolean remove(Component component) {

      // look for it
      for (int i=0;i<subs.size();i++) {
        
        Block sub = (Block)subs.get(i);
        if (sub.remove(component)) {
          if (sub.size()==0)
            subs.remove(sub);
          invalidate(false);
          return true;
        }
        
      }
      
      // not found
      return false;
      
    }
    
    /** size */
    int size() {
      return subs.size();
    }
    
    /** add sub */
    void add(Block block, int level) {
      
      if (level==0) {
        if (block==null)
          block = sub();
        subs.add(block);
      } else {
        Block sub;
        if (subs.isEmpty()) {
          sub = sub();
          subs.add(sub);
        } else {
          sub = (Block)subs.get(subs.size()-1);
        }
        sub.add(block, level-1);
      }
      
      invalidate(false);
    }
    
    /** create sub */
    abstract Block sub();
    
    /** invalidate state */
    void invalidate(boolean recurse) {
      
      // clear state
      preferred = null;
      weight = null;

      // recurse
      if (recurse) for (int i=0;i<subs.size();i++) {
        ((Block)subs.get(i)).invalidate(true);
      }
    }
    
    /** weight */
    abstract Point2D weight();
    
    /** preferred size */
    abstract Dimension preferred();
      
    /** layout */
    abstract void layout(Rectangle in);

  } //Area
  
  /**
   * a row
   */
  private class Row extends Block {

    /** create a sub */
    Block sub() {
      return new Column();
    }
    
    /** preferred size */
    Dimension preferred() {
      // known?
      if (preferred!=null)
        return preferred;
    
      // calculate
      preferred = new Dimension();
      for (int i=0;i<subs.size();i++) {
        Dimension sub = ((Block)subs.get(i)).preferred();
        preferred.width += sub.width;
        preferred.height = Math.max(preferred.height, sub.height);
      }
    
      // done
      return preferred;
    }
    
    /** weight */
    Point2D weight() {
      
      // known?
      if (weight!=null)
        return weight;
      
      // calculate
      weight = new Point2D.Double();
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        weight.x += sub.weight().getX();
        weight.y = Math.max(weight.y, sub.weight().getY());
      }      
      
      // done
      return weight;
    }
    
    /** layout */
    void layout(Rectangle in) {
      
      // compute spare space horizontally
      double weight = 0;
      int spare = in.width;
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        spare -= sub.preferred().width;
        weight += sub.weight().getX();
      }
      double spareOverWeight = weight>0 ? spare/weight : 0;
      
      // layout subs
      Rectangle avail = new Rectangle(in.x, in.y, 0, 0);
      for (int i=0;i<subs.size();i++) {
        
        Block sub = (Block)subs.get(i);
        
        avail.width = sub.preferred().width + (int)(sub.weight().getX() * spareOverWeight);
        avail.height = in.height;

        sub.layout(avail);
  
        avail.x += avail.width;
      }
      
    }
    
  } //Row
  
  /**
   * a column
   */
  private class Column extends Block {
    
    /** create a sub */
    Block sub() {
      return new Row();
    }
    
    /** preferred size */
    Dimension preferred() {
      // known?
      if (preferred!=null)
        return preferred;
    
      // calculate
      preferred = new Dimension();
      for (int i=0;i<subs.size();i++) {
        Dimension sub = ((Block)subs.get(i)).preferred();
        preferred.width = Math.max(preferred.width, sub.width);
        preferred.height += sub.height;
      }
    
      // done
      return preferred;
    }
    
    /** weight */
    Point2D weight() {
      
      // known?
      if (weight!=null)
        return weight;
      
      // calculate
      weight = new Point2D.Double();
      for (int i=0;i<subs.size();i++) {
        Point2D sub = ((Block)subs.get(i)).weight();
        weight.x = Math.max(weight.x, sub.getX());
        weight.y += sub.getY();
      }      
      
      // done
      return weight;
    }
    
    /** layout */
    void layout(Rectangle in) {
      
      // compute spare space vertically
      double weight = 0;
      int spare = in.height;
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        spare -= sub.preferred().height;
        weight += sub.weight().getY();
      }
      double spareOverWeight = weight>0 ? spare/weight : 0;
      
      // loop over subs
      Rectangle avail = new Rectangle(in.x, in.y, 0, 0);
      for (int i=0;i<subs.size();i++) {
        
        Block sub = (Block)subs.get(i);
        
        avail.width = in.width;
        avail.height = sub.preferred().height + (int)(sub.weight().getY() * spareOverWeight);
        
        sub.layout(avail);
  
        avail.y += avail.height;
      }
      
    }
    
  } //Column
  
  /**
   * Component
   */
  private class Cell extends Block {

    /** wrapped component */
    private Component component;
    
    /** weight constraints */
    private Point2D constraints;
    
    /** constructor */
    Cell(Component set, Object constraints) {
      component = set;
      
      // remember weight constraints
      this.constraints = constraints instanceof Point2D ? (Point2D)constraints : new Point2D.Double();
    }
    
    /** remove */
    boolean remove(Component component) {
      return this.component==component;
    }
    
    /** add */
    void add(Block block, int level) {
      throw new IllegalArgumentException();
    }
    
    /** sub */
    Block sub() {
      throw new IllegalArgumentException();
    }
    
    /** preferred */
    Dimension preferred() {
      // known?
      if (preferred!=null)
        return preferred;
      // calc
      preferred = new Dimension(component.getPreferredSize());
      preferred.width += padding*2;
      preferred.height += padding*2;
      return preferred;
    }
    
    /** weight */
    Point2D weight() {
      return constraints;
    }
    
    /** layout */
    void layout(Rectangle in) {
      
      Rectangle avail = new Rectangle(in.x+padding, in.y+padding, in.width-padding*2, in.height-padding*2);
      
      Dimension max = component.getMaximumSize();
      if (max.width<avail.width)  
        avail.width = max.width;
      if (max.height<avail.height)  
        avail.height = max.height;
      component.setBounds(avail);
    }
    
  } //Cell
  
  /**
   * Constructor
   */
  private NestedBlockLayout() {
  }
  
  /**
   * Component/Layout lifecycle callback
   * @param comp the added component
   * @param constraints a Point2D for x/y weight (if applicable)
   */
  public void addLayoutComponent(Component comp, Object constraints) {
    root.add(new Cell(comp, constraints), componentLevel-1);
  }

  /**
   * Component/Layout lifecycle callback
   * @param comp the added component
   * @param name name of component is ignored
   */
  public void addLayoutComponent(String name, Component comp) {
    addLayoutComponent(comp, null);
  }

  /**
   * Component/Layout lifecycle callback
   * @param comp the removed component
   */
  public void removeLayoutComponent(Component comp) {
    root.remove(comp);
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
    root.invalidate(true);
  }

  /**
   * Start a new block
   */
  public void createBlock(int level) {
    if (level>=componentLevel)
      throw new IllegalArgumentException(level+">="+componentLevel);
    root.add(null, level);
  }
  
  /**
   * our preferred layout size
   */
  public Dimension preferredLayoutSize(Container parent) {
    return root.preferred();
  }
  
  /**
   * Component/Layout lifecycle callback
   */
  public void layoutContainer(Container parent) {
    Insets insets = parent.getInsets();
    Rectangle in = new Rectangle(
      insets.left,
      insets.top,
      parent.getWidth()-insets.left-insets.right,
      parent.getHeight()-insets.top-insets.bottom
    );
    root.layout(in);
  }

} //ColumnLayout