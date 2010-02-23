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

import genj.util.Registry;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A layout that arranges components in nested blocks of rows and columns
 * <pre>
 * <!ELEMENT row (col*|T*)>
 * <!ELEMENT col (row*|T*)>
 * <!ELEMENT table (row*|T*)>
 * <!ELEMENT T>
 * <!ATTLIST col drawer CDATA>
 * <!ATTLIST T wx CDATA>
 * <!ATTLIST T wy CDATA>
 * <!ATTLIST T gx CDATA>
 * <!ATTLIST T gy CDATA>
 * <!ATTLIST T ax CDATA>
 * <!ATTLIST T ay CDATA>
 * </pre>
 * wx,wy are weight arguments - gx,gy are grow arguments
 * 
 * examples:
 * <pre> [col][row][A][B][C][/row][row][D][E][/row]F[/col]
 *  +----------+
 *  | A B C    |
 *  | DDDDDD E |
 *  | FFFFFFFF |
 *  +----------+
 * </pre> 
 *  
 * <pre> [row][A][col][row]BC[/row]EF[/col][/row]
 *  +----------+
 *  | A B C    |
 *  | A EEEE   |
 *  | A FFFFFF |
 *  +----------+
 * </pre> 
 *  
 * <pre> [table][row][A][B][C][/row][row][D][E][/row][row][F][G][H][/row][/table]
 *  +----------+
 *  | A B    C |
 *  | D EEEE   |
 *  | F GG   H |
 *  +----------+
 * </pre> 
 * 
 * Note: table doesn't support the notion of span yet
 */
public class NestedBlockLayout implements LayoutManager2, Cloneable {
  
  private final static SAXException DONE = new SAXException("");
  private final static SAXParser PARSER = getSaxParser();
  
  private final static SAXParser getSaxParser() {
    try {
      return SAXParserFactory.newInstance().newSAXParser();
    } catch (Throwable t) {
      Logger.getLogger("genj.util.swing").log(Level.SEVERE, "Can't initialize SAX parser", t);
      throw new Error("Can't initialize SAX parser", t);
    }
  }
  
  private final static Logger LOG = Logger.getLogger("genj.util");

  /** whether we've been invalidated recently */
  private boolean invalidated = true;
  
  /** one root row is holds all the columns */
  private Block root;
  
  /** padding */
  private int padding = 1;
  
  /**
   * Constructor
   */
  private NestedBlockLayout(Block root) {
    this.root = root;
  }

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
  public Collection<Cell> getCells() {
    return root.getCells(new ArrayList<Cell>(10));
  }
  
  /**
   * Post Constructor Initializer
   */
  private void init(Reader descriptor) throws IOException {
    
    // parse descriptor
    try {
	    PARSER.parse(new InputSource(descriptor), new DescriptorHandler());
    } catch (SAXException sax) {
      if (DONE==sax) {
        return;
      }
      throw new RuntimeException(sax);
    } catch (IOException ioe) {
      throw (IOException)ioe;
    } catch (Exception e) {
      throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
    }
    
    // done
  }
  
  /**
   * Our descriptor parser
   */
  private class DescriptorHandler extends DefaultHandler {
    
    private Stack<Block> stack = new Stack<Block>();
    
    public InputSource resolveEntity(String publicId, String systemId) {
      // 20060601 let's not try to resolve any external entities - in case of GenJ running as an applet and a 
      // webserver returning a custom 404 spmeone might read a layout string from getResourceAsStream()
      // which doesn't return null but returns a custom page that we can't parse
      throw new IllegalArgumentException("Request for resolveEntity "+publicId+"/"+systemId+" not allowed in layout descriptor");
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      
      boolean startsWithSpace = Character.isWhitespace(ch[start]);
      boolean endsWithSpace = Character.isWhitespace(ch[start+length-1]);
      
      // trim
      while (length>0 && Character.isWhitespace(ch[start])) { start++; length--; }
      while (length>0 && Character.isWhitespace(ch[start+length-1])) length--;
      if (length==0)
        return;
      
      // add
      if (startsWithSpace) { start--; length++; }
      if (endsWithSpace) { length++; }
      String s = new String(ch,start,length);
            
      Block parent = (Block)stack.peek();
      parent.add(new Cell(s));
    }
    
    public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) throws org.xml.sax.SAXException {
      // new block!
      Block block = getBlock(qName, attributes);
      // make root or add to parent
      if (stack.isEmpty()) {
        root = block;
      } else {
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
        return new Row(attrs);
      // column?
      if ("col".equals(element))
        return new Column(attrs);
      // table?
      if ("table".equals(element))
        return new Table(attrs);
      // a cell!
      return new Cell(element, attrs, padding);
    }
    
    public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
      
      // check
      if (stack==null||stack.size()==0)
        throw new SAXException("unexpected /element");

      // pop last
      stack.pop();
      
      // are we done?
      if (stack.isEmpty())
        throw DONE;
    }
    
  };

  /**
   * an block in the layout
   */
  private static abstract class Block implements Cloneable {
    
    /** preferred size of column */
    Dimension preferred;

    /** weight/growth */
    Point weight;
    Point grow;
    
    /** subs */
    ArrayList<Block> subs = new ArrayList<Block>(16);
    
    Block(Attributes attributes) {
      
      grow = new Point();
      
      
      // look for grow info
      if (attributes!=null) {
        String gx = attributes.getValue("gx");
        if (gx!=null)
          grow.x = Integer.parseInt(gx)>0 ? 1 : 0;
        String gy = attributes.getValue("gy");
        if (gy!=null)
          grow.y = Integer.parseInt(gy)>0 ? 1 : 0;
      }
    }

    /** copy */
    protected Object clone() {
      try {
        Block clone = (Block)super.clone();
        
        clone.subs = new ArrayList<Block>(subs.size());
        for (int i=0;i<subs.size();i++)
          clone.subs.add( (Block)subs.get(i).clone() );
        return clone;
      } catch (CloneNotSupportedException cnse) {
        throw new Error();
      }
    }
    
    /** remove */
    boolean removeContent(Component component) {

      // look for it
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        if (sub.removeContent(component)) {
          invalidate(false);
          return true;
        }
      }
      
      // not found
      return false;
      
    }
    
    /** add sub */
    Block add(Block block) {
      subs.add(block);
      invalidate(false);
      return block;
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
    abstract Point weight();
    
    /** grow within parent */
    Point grow() {
      return grow;
    }
    
    /** preferred size */
    abstract Dimension preferred();
      
    /** layout */
    abstract void layout(Rectangle in);
    
    /** all cells */
    Collection<Cell> getCells(Collection<Cell> collect) {
      for (int i=0;i<subs.size();i++) 
        ((Block)subs.get(i)).getCells(collect);
      return collect;
    }
    
    /** set cell content */
    Cell setContent(Object key, Component component) {
      // look for it in our subs
      for (Block sub : subs) {
        Cell cell = sub.setContent(key, component);
        if (cell!=null)
          return cell;
      }
        
      return null;
    }

  } //Block
  
  /**
   * a row
   */
  private static class Row extends Block {

    Row(Attributes attr) {
      super(attr);
    }
    
    /** add a sub */
    Block add(Block sub) {
      super.add(sub);
      return sub;
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
    @Override
    Point weight() {
      
      // known?
      if (weight!=null)
        return weight;
      
      // calculate
      weight = new Point();
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        weight.x += sub.weight().x;
        weight.y = Math.max(weight.y, sub.weight().y);
      }      
      
      // done
      return weight;
    }
    
    /** layout */
    @Override
    void layout(Rectangle in) {
      
      // compute spare space horizontally
      double weight = 0;
      int grow = 0;
      int spare = in.width;
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        spare -= sub.preferred().width;
        weight += sub.weight().getX();
        grow += sub.grow().x;
      }
      double weightFactor = weight>0 ? spare/weight : 0;
      int growFactor = weightFactor==0 && grow>0 ? spare/grow : 0;
      
      // layout subs
      Rectangle avail = new Rectangle(in.x, in.y, 0, 0);
      for (int i=0;i<subs.size();i++) {
        
        Block sub = (Block)subs.get(i);
        
        avail.width = sub.preferred().width + (int)(sub.weight().getX() * weightFactor) + (sub.grow().x*growFactor);
        avail.height = in.height;

        sub.layout(avail);
  
        avail.x += avail.width;
      }
      
    }
    
  } //Row
  
  /**
   * a column
   */
  private static class Column extends Block implements MouseListener {
    
    private Handle handle = null;
    
    Column(Attributes attr) {
      super(attr);
    }
    
    /** cloning */
    protected Object clone()  {
      Column clone = (Column)super.clone();
      clone.handle = null;
      return clone;
    }
    
    boolean removeContent(Component component) {
      if (handle==component)
        handle=null;
      return super.removeContent(component);
    }
    
    Cell setContent(Object key, Component component) {
      
      Cell result = super.setContent(key, component);
      
      if (result!=null&&subs.contains(result)&&component instanceof Handle) 
        handle = (Handle)component;
      
      return result;
    }
    
    /** preferred size */
    Dimension preferred() {
      
      if (handle!=null&&handle.isFolded)
        return handle.getPreferredSize();
      
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
    
    Point grow() {
      return handle!=null&&handle.isFolded ? new Point() : super.grow();
    }
    
    /** weight */
    Point weight() {
      
      if (handle!=null&&handle.isFolded)
        return new Point();
      
      // known?
      if (weight!=null)
        return weight;
      
      // calculate
      weight = new Point();
      for (int i=0;i<subs.size();i++) {
        Point sub = ((Block)subs.get(i)).weight();
        weight.x = Math.max(weight.x, sub.x);
        weight.y += sub.y;
      }      
      
      // done
      return weight;
    }
    
    /** layout */
    void layout(Rectangle in) {

      // closed?
      if (handle!=null&&handle.isFolded) {
        for (Block sub : subs) 
          sub.layout(new Rectangle(0,0));
        handle.setBounds(in);
        return;
      }
      
      // compute spare space vertically
      double weight = 0;
      int spare = in.height;
      int grow = 0;
      for (Block sub : subs) {
        spare -= sub.preferred().height;
        weight += sub.weight().getY();
        grow += sub.grow().y;
      }
      double weightFactor = weight>0 ? spare/weight : 0;
      int growFactor = weightFactor==0 && grow>0 ? spare/grow : 0;
      
      // loop over subs
      Rectangle avail = new Rectangle(in.x, in.y, 0, 0);
      for (int i=0;i<subs.size();i++) {
        
        Block sub = (Block)subs.get(i);
        
        avail.width = in.width;
        avail.height = sub.preferred().height + (int)(sub.weight().getY() * weightFactor) + (sub.grow().y*growFactor);
        
        sub.layout(avail);
  
        avail.y += avail.height;
      }
      
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
    
  } //Column
  
  /**
   * Component
   */
  public static class Cell extends Block {
    
    /** a unique element id */
    private String element;
    
    /** attributes */
    private Map<String,String> attrs = new HashMap<String, String>();
    
    /** wrapped component */
    private Component component;
    
    /** padding */
    private int cellPadding;
    
    /** cached weight */
    private Point cellWeight = new Point();
    
    /** cached alignment */
    private Point2D.Double cellAlign = new Point2D.Double(0,0.5);

    /** constructor */
    private Cell(String text) {
      super(null);
      this.element = "text";
      attrs.put("value", text);
    }
    
    /** constructor */
    private Cell(String element, Attributes attributes, int padding) {
      
      super(attributes);
      
      // keep key
      this.element = element;
      this.cellPadding = padding;
      
      for (int i=0,j=attributes.getLength();i<j;i++) 
        attrs.put(attributes.getQName(i), attributes.getValue(i));
      
      // look for weight info
      String wx = getAttribute("wx");
      if (wx!=null) {
        cellWeight.x = Integer.parseInt(wx);
        if (attributes.getValue("gx")==null)
          grow.x = 1;
      }
      String wy = getAttribute("wy");
      if (wy!=null) {
        cellWeight.y = Integer.parseInt(wy);
        if (attributes.getValue("gy")==null)
          grow.y = 1;
      }
      
      // look for alignment info
      String ax = getAttribute("ax");
      if (ax!=null)
        cellAlign.x = Float.parseFloat(ax);
      String ay = getAttribute("ay");
      if (ay!=null)
        cellAlign.y = Float.parseFloat(ay);

      // done
    }
    
    /** cloning */
    protected Object clone()  {
      Cell clone = (Cell)super.clone();
      clone.component = null;
      return clone;
    }
    
    @Override
    void invalidate(boolean arg0) {
      // component info only
      preferred = null;
    }
    
    /** element */
    public String getElement() {
      return element;
    }
    
    /** Access a cell descriptor attribute */
    public boolean isAttribute(String attr) {
      return attrs.containsKey(attr);
    }
    
    /** attribute */
    public String getAttribute(String attr) {
      return (String)attrs.get(attr);
    }
    
    /** remove */
    boolean removeContent(Component component) {
      if (this.component==component) {
        this.component = null;
        invalidate(false);
        return true;
      }
      return false;
    }
    
    /** preferred */
    Dimension preferred() {
      // known?
      if (preferred!=null)
        return preferred;
      // calc
      if (component==null||!component.isVisible())
        preferred = new Dimension();
      else {
	      preferred = new Dimension(component.getPreferredSize());
          Dimension max = component.getMaximumSize();
	      preferred.width = Math.min(max.width,preferred.width) + cellPadding*2;
	      preferred.height = Math.min(max.height,preferred.height) + cellPadding*2;
      }
      return preferred;
    }
    
    /** weight */
    @Override
    Point weight() {
      return component==null ? new Point() : cellWeight;
    }
    
    /** layout */
    void layout(Rectangle in) {
      
      if (component==null)
        return;
      
      // calculate what's available
      Rectangle avail = new Rectangle(in.x+cellPadding, in.y+cellPadding, in.width-cellPadding*2, in.height-cellPadding*2);
      
      // make sure it's not more than maximum
      Dimension max = component.getMaximumSize();
      if (avail.width>max.width) {
        int extraX = avail.width-max.width;
        avail.x += extraX * cellAlign.x;
        avail.width = max.width;
      }
      if (avail.height>max.height) {
        int extraY = avail.height-max.height;
        avail.y += extraY * cellAlign.y;
        avail.height = max.height;
      }

      // set it
      component.setBounds(avail);
    }
    
    /** set cell content*/
    @Override
    Cell setContent(Object key, Component component) {
      if (  (key instanceof Cell&&key!=this)
         || (key instanceof String&&!element.equals(key))
         || (key==null&&this.component!=null))
        return null;
      this.component = component;
      return this;
    }
    
    /** all cells */
    Collection<Cell> getCells(Collection<Cell> collect) {
      collect.add(this);
      return collect;
    }

  } //Cell
  
  /**
   * Component/Layout lifecycle callback
   */
  public void addLayoutComponent(Component comp, Object key) {

    Cell cell = root.setContent(key, comp);
    if (cell!=null)
      return;
    
    // no match
    if (key==null)
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
    root.removeContent(comp);
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
    if (!invalidated) {
      root.invalidate(true);
      invalidated = true;
    }
  }

  /**
   * our preferred layout size
   */
  public Dimension preferredLayoutSize(Container parent) {
    invalidated = false;
    return root.preferred();
  }
  
  /**
   * Component/Layout lifecycle callback
   */
  public void layoutContainer(Container parent) {
    
    // prepare insets
    Insets insets = parent.getInsets();
    Rectangle in = new Rectangle(
      insets.left,
      insets.top,
      parent.getWidth()-insets.left-insets.right,
      parent.getHeight()-insets.top-insets.bottom
    );
    // layout
    root.layout(in);
    // remember
    invalidated = false;
  }
  
  /**
   * Create a private copy
   */
  public NestedBlockLayout copy() {
    try {
      NestedBlockLayout clone = (NestedBlockLayout)super.clone();
      clone.root = (Block)clone.root.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }
  
  /**
   * a table
   */
  private static class Table extends Block {

    private ArrayList<Integer> rowHeights;
    private ArrayList<Integer> colWidths;
    private ArrayList<Integer> rowWeights;
    private ArrayList<Integer> colWeights;
    
    Table(Attributes attrs) {
      super(attrs);
    }
    
    private void calcGrid() {
      
      if (rowHeights!=null)
        return;
      
      rowHeights = new ArrayList<Integer>();
      colWidths = new ArrayList<Integer>();
      rowWeights = new ArrayList<Integer>();
      colWeights = new ArrayList<Integer>();
      
      for (int r=0;r<subs.size();r++) {
        Block row = subs.get(r);
        if (row instanceof Row) {
          for (int c=0;c<row.subs.size();c++) {
            Dimension d = row.subs.get(c).preferred();
            grow(colWidths, c, d.width);
            grow(rowHeights, r, d.height);
            Point w = row.subs.get(c).weight();
            grow(colWeights, c, w.x);
            grow(rowWeights, r, w.y);
          }
        } else {
          Dimension d = row.preferred();
          grow(colWidths, 0, d.width);
          grow(rowHeights, r, d.height);
          Point w = row.weight();
          grow(colWeights, 0, w.x);
          grow(rowWeights, r, w.y);
        }
      }
    }
    
    @Override
    void layout(Rectangle in) {

      // calculate preferred grid & size
      Dimension preferred = preferred();
      calcGrid();
      
      // calculate extras
      float xWeightMultiplier = 0;
      if (in.width>preferred.width) {
        int w = 0;
        for (int i=0;i<colWeights.size();i++)
          w += colWeights.get(i);
        xWeightMultiplier = (in.width-preferred.width)/(float)w;
      }
      float yWeightMultiplier = 0;
      if (in.height>preferred.height) {
        int h = 0;
        for (int i=0;i<rowWeights.size();i++)
          h += rowWeights.get(i);
        yWeightMultiplier = (in.height-preferred.height)/(float)h;
      }
      
      // layout subs
      Rectangle avail = new Rectangle(in.x, in.y, in.width, in.height);
      for (int r=0;r<subs.size();r++) {
        
        Block row = subs.get(r);
        
        if (row instanceof Row) {
          int x = avail.x;
          for (int c=0;c<row.subs.size();c++) {
            int w = avail.width<preferred.width ? avail.width/row.subs.size() : colWidths.get(c) + (int)(colWeights.get(c)*xWeightMultiplier);
            row.subs.get(c).layout(new Rectangle(x, avail.y, w, rowHeights.get(r)));
            x += w;
          }
        } else {
          int w = avail.width<preferred.width ? avail.width : colWidths.get(0) + (int)(colWeights.get(0)*xWeightMultiplier);
          ((Row)row).layout(new Rectangle(avail.x, avail.y, w, rowHeights.get(r)));
        }
        
        // next row
        avail.y += rowHeights.get(r);
      }

      // done
    }
    
    private void grow(ArrayList<Integer> values, int i, Integer value) {
      while (values.size()<i+1)
        values.add(0);
      values.set(i, Math.max(values.get(i), value));
    }
    
    @Override
    Dimension preferred() {
      
      // calculate preferred grid
      calcGrid();
      
      // add it up
      Dimension result = new Dimension(0,0);
      for (int c=0;c<colWidths.size();c++)
        result.width += colWidths.get(c);
      for (int r=0;r<rowHeights.size();r++)
        result.height += rowHeights.get(r);
      
      // done
      return result;
    }

    @Override
    Point weight() {
      if (weight!=null)
        return weight;
      
      // calculate preferred grid
      calcGrid();
      
      // add it up
      Point result = new Point(0,0);
      for (int c=0;c<colWeights.size();c++)
        result.x += colWeights.get(c);
      for (int r=0;r<rowWeights.size();r++)
        result.y += rowWeights.get(r);
      
      // done
      return result;
    }
    
    @Override
    void invalidate(boolean recurse) {
      super.invalidate(recurse);
      
      rowHeights = null;
      colWidths = null;
      rowWeights = null;
      colWeights = null;
    }
  }
  
  /**
   * A widget that operates folders
   */
  public static class Handle extends JLabel {
    
    private final static Icon FOLDED = new Symbol(8, false);
    private final static Icon UNFOLDED = new Symbol(8, true);
    private boolean isFolded = false;
    private Registry registry;
    
    /**
     * Constructor
     */
    public Handle(String label, Registry registry) {
      super(label);
      this.registry = registry;
      this.isFolded = registry.get("fold."+label, false);
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      addMouseListener(new Mouser());
    }

    public Icon getIcon() {
      return isFolded ? FOLDED : UNFOLDED;
    }
    
    private class Mouser extends MouseAdapter {
      @Override
      public void mouseClicked(MouseEvent e) {
        
        isFolded = !isFolded;

        registry.put("fold."+getText(), isFolded);

        Component parent = getParent();
        if (parent instanceof JComponent)
          ((JComponent)parent).revalidate();
        else {
          parent.invalidate();
          parent.validate();
        }
        
        // done
      }
    }
    
    private static class Symbol implements Icon {
      
      private int size;
      private GeneralPath shape = new GeneralPath();

      public Symbol(int size, boolean open) {
          this.size = size;
          if (open) {
            shape.moveTo(0, size/4);
            shape.lineTo(size, size/4);
            shape.lineTo(size/2, size*3/4);
            shape.closePath();
          } else {
            shape.moveTo(size/4, 0);
            shape.lineTo(size/4, size);
            shape.lineTo(size*3/4, size/2);
            shape.closePath();
          }
      }

      public void paintIcon(Component c, Graphics g, int x, int y) {
          Color color = c == null ? Color.GRAY : c.getForeground();
          g.setColor(color);
          int dy = (c.getHeight()-size)/2;
          g.translate(0, dy);
          ((Graphics2D)g).fill(shape);
          g.translate(0, -dy);
      }

      public int getIconWidth() {
        return size;
      }

      public int getIconHeight() {
        return size;
      }
      
    } // Symbol

  }
  
} //ColumnLayout