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
package genj.renderer;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;

/**
 * 
 */
public class EntityRenderer {
  
  /** the property image width */
  private static final int 
    PROP_IMAGE_WIDTH  = Property.getDefaultImage("INDI").getIconWidth()+4,
    PROP_IMAGE_HEIGHT = Property.getDefaultImage("INDI").getIconHeight();
  
  /** replace value for no-property */
  private static final Object
    NULL = new Object();
    
  /** a no value char array */
  private static final Segment EMPTY_SEGMENT = new Segment(); 
  
  /** the root of our rendering view */
  private RootView root;
  
  /** the kit we're using */
  private HTMLEditorKit kit = new MyHTMLEditorKit();
  
  /** the document we're looking at */
  private HTMLDocument doc = new HTMLDocument();
  
  /** the entity we're looking at */
  private Entity entity;
  
  /** all PropertyViews we know */
  private List propViews = new ArrayList(16);
  
  /** all TableViews we know */
  private List tableViews = new ArrayList(4);
  
  /** the proxies we know */
  private static Map proxies = new HashMap(10);
  
  /** the graphics we're for */
  private Graphics graphics;
  
  /**
   * 
   */  
  public EntityRenderer(Graphics g, String html) {

    if (g==null||html==null) throw new IllegalArgumentException("Graphics and html must not be null"); 
    
    // remember Graphics
    graphics = g;
    
    // we wrap the html in html/body
    html = "<html><body>"+html+"</body></html>";
    
    // read the html
    try {
      kit.read(new StringReader(html), doc, 0);
    } catch (IOException ioe) {
      // can't happen
    } catch (BadLocationException ble) {
      // can't happen
    }

    // create the root view
    root = new RootView(kit.getViewFactory().create(doc.getDefaultRootElement()));

    // done    
  }

  /**
   * 
   */
  public void render(Graphics g, Rectangle r) {
    
    // invalidate views 
    Iterator pv = propViews.iterator();
    while (pv.hasNext()) {
      ((PropertyView)pv.next()).invalidate();
    }
    
    // and make sure TableView's update their grid
    Iterator tv = tableViews.iterator();
    while (tv.hasNext()) {
      // this will cause invalidateGrid on a javax.swing.text.html.TableView
      ((View)tv.next()).replace(0,0,null);
    }

//      View parent = getParent();
//      while (parent!=null) {
////        try {
////          parent.getClass().getDeclaredMethod("invalidateGrid", new Class[]{}).invoke(parent, new Object[]{});
////          System.out.println("!");
////        } catch (Throwable t) { t.printStackTrace(); }
//        parent.replace(0,0,null);
//        parent = parent.getParent();
//      } 
//      //((javax.swing.text.html.TableView.RowView)getParent()).preferenceChanged(this,true,true);
//      //invalidateGrid
    
    // set the size of root
    root.setSize(r.width,r.height);
    
    // show it
    root.paint(g, r);
  }
  
  /**
   * Setter - entity
   */
  public void setEntity(Entity set) {
    
    // keep it
    entity = set;
    
    // done
  }
  
  /** 
   * Getter - entity
   */
  public Entity getEntity() {
    return entity;
  }
  
  /**
   * 
   */
  private class MyHTMLEditorKit extends HTMLEditorKit {
    
    /** the view factory we use */
    private ViewFactory factory = new MyHTMLFactory();
  
    /**
     * @see javax.swing.text.EditorKit#getViewFactory()
     */
    public ViewFactory getViewFactory() {
      return factory;
    }
  
  } //ModifiedHTMLEditorKit

  /**
   * 
   */
  private class MyHTMLFactory extends HTMLFactory {
    
    /**
     * @see javax.swing.text.ViewFactory#create(Element)
     */
    public View create(Element elem) {

      View result;
      
      // check if the element is "prop"
      if ("prop".equals(elem.getName())) {
        result = new PropertyView(elem);
        propViews.add(result);
      } else {
        
        // .. otherwise default to super
        result = super.create(elem);
        
        // .. keep track of TableViews
        if ("table".equals(elem.getName())) {
          tableViews.add(result);
        }
          
        
      }
      
      // done
      return result;
    }
  
  } //ModifiedHTMLFactory
  
  /**
   * 
   */
  private abstract class MyView extends View {
    /**
     * Constructor
     */
    MyView(Element elem) {
      super(elem);
    }
    /**
     * @see javax.swing.text.View#viewToModel(float, float, Shape, Bias[])
     */
    public int viewToModel(float arg0, float arg1, Shape arg2, Bias[] arg3) {
      throw new RuntimeException("viewToModel() is not supported");
    }
    /**
     * @see javax.swing.text.View#modelToView(int, Shape, Bias)
     */
    public Shape modelToView(int pos, Shape a, Bias b) throws BadLocationException {
      throw new RuntimeException("modelToView() is not supported");
    }
    /**
     * Creates a shallow copy
     */
    protected final Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException cnse) {
        throw new RuntimeException();
      }
    }
  } //BaseView

  /**
   * RootView onto a HTML Document
   */
  private class RootView extends MyView {

    /** the root of the html's view hierarchy */
    private View view;
    
    /** the size of the root view */
    private float width, height;

    /**
     * Constructor
     */
    RootView(View view) {
      
      // block super
      super(null);

      // keep view
      this.view = view;
      view.setParent(this);
      
      // done
    }

    /**
     * we don't have any attributes
     */
    public AttributeSet getAttributes() {
      return null;
    }

    /**
     * we let the wrapped view do the painting
     */
    public void paint(Graphics g, Shape allocation) {
      view.paint(g, allocation);
    }

    /** 
     * our document is the parsed html'
     */
    public Document getDocument() {
      return doc;
    }

    /**
     * the wrapped view needs to be sized
     */    
    public void setSize(float wIdth, float heIght) {
      // remember
      width = wIdth;
      height = heIght;
      // delegate
      view.setSize(width, height);
    }

    /**
     * we use our kit's view factory
     */
    public ViewFactory getViewFactory() {
      return kit.getViewFactory();
    }

    /**
     * @see javax.swing.text.View#getPreferredSpan(int)
     */
    public float getPreferredSpan(int axis) {
      return view.getPreferredSpan(axis);
    }

  } //RootView

  /**
   *
   */
  private class PropertyView extends MyView implements Cloneable {
    
    /** our preference when looking at the property */
    private int preference;
    
    /** the proxy used */
    private PropertyProxy proxy = null;
    
    /** the tag path used */
    private TagPath path = null;
    
    /** the cached property we're displaying */
    private Object property = null;
    
    /** the cached font we're using */
    private Font font = null;
    
    /** the cached foreground we're using */
    private Color foreground = null;
    
    /** the cached preferred span */
    private Dimension preferredSpan = null;
    
    /** minimum percentage of the rendering space */
    private int min;
    
    /** 
     * Constructor
     */
    PropertyView(Element elem) {
      super(elem);
      
      // grab path&proxy
      Object p = elem.getAttributes().getAttribute("path");
      if (p!=null) {
        
        path = new TagPath(p.toString());
      
        // get a 'logical' proxy name
        String name = Property.calcDefaultProxy(path);

        // know it already?
        proxy = (PropertyProxy) proxies.get(name);
        if (proxy==null) {
          proxy = PropertyProxy.get(name);
          proxies.put(name, proxy);
        }

        // proxy is setup
      }      
      
      // check image&text
      preference = PropertyProxy.PREFER_DEFAULT;
      AttributeSet atts = elem.getAttributes();
      if ("yes".equals(atts.getAttribute("img"))) {
        preference = PropertyProxy.PREFER_IMAGEANDTEXT;
        if ("no".equals(atts.getAttribute("txt"))) 
          preference = PropertyProxy.PREFER_IMAGE;
      }
      
      // minimum?
      min = getInt(atts, "min", 1, 100, 1);
      
      // done
    }
    
    /**
     * Gets an int value from attributes     */
    private int getInt(AttributeSet atts, String key, int min, int max, int def) {
      // grab a value and try to parse
      Object val = atts.getAttribute(key);
      if (val!=null) try {
        return Math.max(min, Math.min(max, Integer.parseInt(val.toString())));
      } catch (NumberFormatException e) {
      }
      // not found
      return def;
    }
    
    /**
     * Invalidates this views current state
     */
    private void invalidate() {
      // invalidate cached information that's depending
      // on the current entity's properties
      property = null;
      preferredSpan = null;
      // signal preference change through super
      super.preferenceChanged(this,true,true);
    }
    
    /**
     * Returns the property we're viewing
     */
    private Property getProperty() {
      if (entity==null) return null;
      if (property!=null) return property==NULL ? null : (Property)property;
      path.setToFirst();
      property = entity.getProperty().getProperty(path, true);
      if (property!=null) return (Property)property; 
      property = NULL;
      return null;
    }
    
    /** 
     * Returns the current metrics
     */
    private FontMetrics getFontMetrics() {
      return graphics.getFontMetrics(getFont());
    }
    
    /** 
     * Returns the current font
     */
    private Font getFont() {
      if (font==null) font = doc.getFont(getAttributes());
      return font;
    }
    
    /** 
     * Returns the current fg color
     */
    private Color getForeground() {
      if (foreground==null) foreground = doc.getForeground(getAttributes());
      return foreground;
    }
    
    /**
     * @see javax.swing.text.View#paint(Graphics, Shape)
     */
    public void paint(Graphics g, Shape allocation) {
      // find property
      Property p = getProperty();
      if (p==null) return;
      // setup painting attributes
      g.setColor(getForeground());
      g.setFont(getFont());
      // render
      Rectangle r = (allocation instanceof Rectangle) ? (Rectangle)allocation : allocation.getBounds();
      Rectangle old = g.getClipBounds();
      g.clipRect(r.x, r.y, r.width, r.height);
      proxy.render(g, r, p, preference);
      g.setClip(old.x, old.y, old.width, old.height);
      // done
    }
    /**
     * @see javax.swing.text.View#getAlignment(int)
     */
    public float getAlignment(int axis) {
      if (X_AXIS==axis)
        return super.getAlignment(axis);
      return proxy.getVerticalAlignment(getFontMetrics());
    }
    /**
     * @see javax.swing.text.View#getPreferredSpan(int)
     */
    public float getPreferredSpan(int axis) {
      // get the property
      Property p = getProperty();
      if (p==null) return 0;
      // check cached preferred Spane
      if (preferredSpan==null) {
        preferredSpan = proxy.getSize(getFontMetrics(), p, preference);
      }
      return axis==X_AXIS ? preferredSpan.width : preferredSpan.height;
    }
    /**
     * @see javax.swing.text.View#getMinimumSpan(int)
     */
    public float getMinimumSpan(int axis) {
      float pref = getPreferredSpan(axis);
      if (axis==Y_AXIS) return pref;
      return Math.min(pref, root.width*min/100);
    }
    /**
     * @see javax.swing.text.View#getMaximumSpan(int)
     */
    public float getMaximumSpan(int axis) {
      return getPreferredSpan(axis);
    }
    /**
     * @see javax.swing.text.View#getBreakWeight(int, float, float)
     */
    public int getBreakWeight(int axis, float pos, float len) {
      // not on vertical
      if (axis==Y_AXIS) return BadBreakWeight;
      // horizontal might work after our content
      if (len > getPreferredSpan(X_AXIS)) {
        return GoodBreakWeight;
      }
      return BadBreakWeight;
    }  
    /**
     * @see javax.swing.text.View#breakView(int, int, float, float)
     */
    public View breakView(int axis, int offset, float pos, float len) {
      return this;
    }
  } //PropertyView

  
} //EntityRenderer
