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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A layout that arranges components in nested blocks of rows and columns
 * <!ELEMENT layout (row|col)>
 * <!ELEMENT row (col*|*)>
 * <!ELEMENT col (row*|*)>
 */
public class NestedBlockLayout implements LayoutManager2 {

  /** one root row is holds all the columns */
  private Block root;
  
  /** padding */
  private int padding = 1;
  
  /** mapping key 2 cell */
  private List cells = new ArrayList(16);
  
  /**
   * Constructor
   */
  public NestedBlockLayout(String descriptor) {
    try {
      init(new StringReader(descriptor));
    } catch (IOException e) {
      // can't happen
    }
  }

  /**
   * Constructor
   */
  public NestedBlockLayout(Reader descriptor) throws IOException {
    init(descriptor);
  }
  
  /**
   * Constructor
   */
  public NestedBlockLayout(InputStream descriptor) throws IOException {
    init(new InputStreamReader(descriptor));
  }
  
  /**
   * Accessor to cell definitions
   */
  public Collection getCells() {
    return cells;
  }
  
  /**
   * Post Constructor Initializer
   */
  private void init(Reader descriptor) throws IOException {
    
    // parse descriptor
    try {
	    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
	    parser.parse(new InputSource(descriptor), new DescriptorHandler());
    } catch (IOException ioe) {
      throw (IOException)ioe;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    // done
  }
  
  /**
   * Our descriptor parser
   */
  private class DescriptorHandler extends DefaultHandler {
    
    private Stack stack = new Stack();
    
    public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) throws org.xml.sax.SAXException {
      // new block!
      Block block = getBlock(qName, attributes);
      // add to parent
      if (stack==null) 
        throw new SAXException("unexpected element");
      if (!stack.isEmpty()) {
        Block parent = (Block)stack.peek();
	      parent.add(block);
      }
      // throw on stack
      stack.add(block);
      // done
    }
    
    private Block getBlock(String element, Attributes attrs) {
      // row?
      if ("row".equals(element)) 
        return new Row();
      // column?
      if ("col".equals(element))
        return new Column();
      // a cell!
      Cell cell =  new Cell(element, attrs);
      // remember
      cells.add(cell);
      // done
      return cell;
    }
    
    public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
      
      // check
      if (stack==null||stack.size()==0)
        throw new SAXException("unexpected /element");

      // pop last
      Block block = (Block)stack.pop();
      
      // are we done?
      if (stack.isEmpty()) {
        root = block;
        stack = null;
      }
    }
    
  };

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
    void add(Block block) {
      subs.add(block);
      invalidate(false);
    }
    
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

    /** add a sub */
    void add(Block sub) {
      if (sub instanceof Row)
        throw new IllegalArgumentException("row can't contain row");
      super.add(sub);
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

    /** add a sub */
    void add(Block sub) {
      if (sub instanceof Column)
        throw new IllegalArgumentException("column can't contain column");
      super.add(sub);
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
  public class Cell extends Block {
    
    /** a unique element id */
    private String element;
    
    /** attributes */
    private Map attrs = new HashMap();
    
    /** wrapped component */
    private Component component;
    
    /** weight constraints */
    private Point2D.Float weight = new Point2D.Float();

    /** constructor */
    private Cell(String element, Attributes attributes) {
      
      // keep key
      this.element = element;
      
      for (int i=0,j=attributes.getLength();i<j;i++) 
        attrs.put(attributes.getQName(i), attributes.getValue(i));
      
      // look for weight info
      String wx = getAttribute("wx");
      if (wx!=null)
        weight.x = Float.parseFloat(wx);
      String wy = getAttribute("wy");
      if (wy!=null)
        weight.y = Float.parseFloat(wy);

    }
    
    /** set contained content */
    void setContent(Component component) {
      this.component = component;
    }
    
    /** element */
    public String getElement() {
      return element;
    }
    
    /** attribute */
    public String getAttribute(String attr) {
      return (String)attrs.get(attr);
    }
    
    /** set weight of cell */
    public void setWeight(Point2D weight) {
      if (weight!=null) this.weight.setLocation(weight);
    }
    
    /** remove */
    boolean remove(Component component) {
      if (this.component==component) {
        this.component = null;
        invalidate(false);
        return true;
      }
      return false;
    }
    
    /** add a sub */
    void add(Block sub) {
      throw new IllegalArgumentException("cell can't contain row, column or other cell");
    }
    
    /** preferred */
    Dimension preferred() {
      // known?
      if (preferred!=null)
        return preferred;
      // calc
      if (component==null)
        preferred = new Dimension();
      else {
	      preferred = new Dimension(component.getPreferredSize());
	      preferred.width += padding*2;
	      preferred.height += padding*2;
      }
	    return preferred;
    }
    
    /** weight */
    Point2D weight() {
      return weight;
    }
    
    /** layout */
    void layout(Rectangle in) {
      
      if (component==null)
        return;
      
      // calculate what's available
      Rectangle avail = new Rectangle(in.x+padding, in.y+padding, in.width-padding*2, in.height-padding*2);
      
      // make sure it's not more than maximum
      Dimension max = component.getMaximumSize();

      int extraX = avail.width-max.width;
      if (extraX>0) {
        avail.x += extraX/2;
        avail.width = max.width;
      }
      
      int extraY = avail.height-max.height;
      if (extraY>0) {
        avail.y += extraY/2;
        avail.height = max.height;
      }

      // set it
      component.setBounds(avail);
    }
    
  } //Cell
  
  /**
   * Component/Layout lifecycle callback
   */
  public void addLayoutComponent(Component comp, Object key) {

    // a cell?
    if (key instanceof Cell) {
      ((Cell)key).setContent(comp);
      return;
    }
    
    // lookup cell
    String element = key!=null ? key.toString() : null;
    for (int i=0,j=cells.size();i<j;i++) {
      Cell cell = (Cell)cells.get(i);
      // take next available, if key==null
      // matching element, otherwise
      if ((element==null&&cell.component==null)||(cell.getElement().equals(element))) {
        cell.setContent(comp);
        return;
      }
    }
  
    if (element==null)
      throw new IllegalArgumentException("no available descriptor element - element qualifier required");
    throw new IllegalArgumentException("element qualifier doesn't match any descriptor element");

    // done
  }

  /**
   * Component/Layout lifecycle callback
   */
  public void addLayoutComponent(String element, Component comp) {
    addLayoutComponent(comp, element);
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