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
import genj.gedcom.Gedcom;
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
  public EntityRenderer(Graphics g, Blueprint bp) {

    if (g==null||bp==null) throw new IllegalArgumentException("Graphics and blueprint must not be null"); 
    
    // remember Graphics
    graphics = g;
    
    // we wrap the html in html/body
    String html = "<html><body>"+bp.getHTML()+"</body></html>";
    
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
  public void render(Graphics g, Entity e, Rectangle r) {
    
    // keep the entity
    entity = e;
    
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
    
    // set the size of root
    root.setSize((float)r.getWidth(),(float)r.getHeight());
    
    // clip it
    Rectangle oc = g.getClipBounds();
    g.clipRect(r.x,r.y,r.width,r.height);
    
    // show it
    root.paint(g, r);
    
    // restore clip
    g.setClip(oc.x,oc.y,oc.width,oc.height);
    
    // done
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
      
      String name = elem.getName();

      // check if the element is "prop"
      if ("prop".equals(name)) {
        View result = new PropertyView(elem);
        propViews.add(result);
        return result;
        
      }
      
      // maybe its "tag"
      if ("i18n".equals(name)) {
        return new I18NView(elem);
      }
        
      // default to super
      View result = super.create(elem);
      // .. keep track of TableViews
      if ("table".equals(elem.getName())) {
        tableViews.add(result);
      }
      return result;
    }
  
  } //ModifiedHTMLFactory
  
  /**
   * 
   */
  private abstract class MyView extends View {
  
    /** the cached font we're using */
    private Font font = null;
    
    /** the cached foreground we're using */
    private Color foreground = null;
    
    /** the cached preferred span */
    private Dimension preferredSpan = null;
    
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
     * @see javax.swing.text.View#getBreakWeight(int, float, float)
     */
    public int getBreakWeight(int axis, float pos, float len) {
      // not on vertical
      if (axis==Y_AXIS) return BadBreakWeight;
      // horizontal might work after our content
      if (len > getPreferredSpan(X_AXIS)) {
        return ExcellentBreakWeight;
      }
      return BadBreakWeight;
    }  
    /**
     * @see javax.swing.text.View#breakView(int, int, float, float)
     */
    public View breakView(int axis, int offset, float pos, float len) {
      return this;
    }
    
    /**
     * @see javax.swing.text.View#getMaximumSpan(int)
     */
    public float getMaximumSpan(int axis) {
      return getPreferredSpan(axis);
    }
    
    /**
     * @see javax.swing.text.View#getPreferredSpan(int)
     */
    public float getPreferredSpan(int axis) {
      // check cached preferred Span
      if (preferredSpan==null) {
        preferredSpan = getPreferredSpan();
      }
      return axis==X_AXIS ? preferredSpan.width : preferredSpan.height;
    }
    
    /**
     * @see javax.swing.text.View#getAlignment(int)
     */
    public float getAlignment(int axis) {
      if (X_AXIS==axis) return 0.5F;
      return getVerticalAlignment();
    }
    
    /**
     * Get the preferred alignment     */
    protected float getVerticalAlignment() {
      return 0.5F;
    }
    
    /**
     * Get the preferred span     */
    protected abstract Dimension getPreferredSpan();

    /** 
     * Returns the current metrics
     */
    protected FontMetrics getFontMetrics() {
      return graphics.getFontMetrics(getFont());
    }
    
    /** 
     * Returns the current font
     */
    protected Font getFont() {
      if (font==null) font = doc.getFont(getAttributes());
      return font;
    }
    
    /** 
     * Returns the current fg color
     */
    protected Color getForeground() {
      if (foreground==null) foreground = doc.getForeground(getAttributes());
      return foreground;
    }
    
    /**
     * Invalidates this views current state
     */
    protected void invalidate() {
      // invalidate preferred span
      preferredSpan = null;
      // signal preference change through super
      super.preferenceChanged(this,true,true);
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
     * @see genj.renderer.EntityRenderer.MyView#getPreferredSpan()
     */
    protected Dimension getPreferredSpan() {
      return new Dimension(
        (int)view.getPreferredSpan(X_AXIS),
        (int)view.getPreferredSpan(Y_AXIS)
      );
    }

  } //RootView

  /**
   * A view for translating text   */
  private class I18NView extends MyView {
    
    /** the text to paint */
    private String txt = "?";
    
    /**
     * Constructor     */
    private I18NView(Element elem) {
      super(elem);
      // resolve and localize text .. tag
      Object o = elem.getAttributes().getAttribute("tag");
      if (o!=null) txt = Gedcom.getName(o.toString());
      // resolve and localize text .. type
      o = elem.getAttributes().getAttribute("entity");
      if (o!=null) try {
        txt = Gedcom.getNameFor(Gedcom.getTypeFor(o.toString()), false);
      } catch (IllegalArgumentException e) {}
      // done
    }
    /**
     * @see javax.swing.text.View#paint(java.awt.Graphics, java.awt.Shape)
     */
    public void paint(Graphics g, Shape allocation) {
      Rectangle r = allocation.getBounds();
      g.setFont(getFont());
      g.setColor(getForeground());
      PropertyRenderer.DEFAULT_PROPERTY_PROXY.render(g,r,txt);
    }
    /**
     * @see genj.renderer.EntityRenderer.MyView#getPreferredSpan()
     */
    protected Dimension getPreferredSpan() {
      return new Dimension(      
        getFontMetrics().stringWidth(txt),
        getFontMetrics().getHeight()
      );
    }
    
    /**
     * @see genj.renderer.EntityRenderer.MyView#getVerticalAlignment()
     */
    protected float getVerticalAlignment() {
      return PropertyRenderer.DEFAULT_PROPERTY_PROXY.getVerticalAlignment(getFontMetrics());
    }
  } //LocalizeView

  /**
   * A view that wraps a property and its value
   */
  private class PropertyView extends MyView {
    
    /** our preference when looking at the property */
    private int preference;
    
    /** the proxy used */
    private PropertyRenderer proxy = null;
    
    /** the tag path used */
    private TagPath path = null;
    
    /** the cached property we're displaying */
    private Object property = null;
    
    /** minimum percentage of the rendering space */
    private int min;
    
    /** 
     * Constructor
     */
    PropertyView(Element elem) {
      super(elem);
      
      // grab path&proxy
      Object p = elem.getAttributes().getAttribute("path");
      if (p!=null) try {
        
        path = new TagPath(p.toString());
      
        // get a 'logical' proxy name
        String name = Property.calcDefaultProxy(path);

        // know it already?
        proxy = (PropertyRenderer) proxies.get(name);
        if (proxy==null) {
          proxy = PropertyRenderer.get(name);
          proxies.put(name, proxy);
        }

        // proxy is setup
      } catch (IllegalArgumentException e) {
        // ignoring wrong path
      }       
      
      // check image&text
      preference = PropertyRenderer.PREFER_DEFAULT;
      AttributeSet atts = elem.getAttributes();
      if ("yes".equals(atts.getAttribute("img"))) {
        preference = PropertyRenderer.PREFER_IMAGEANDTEXT;
        if ("no".equals(atts.getAttribute("txt"))) 
          preference = PropertyRenderer.PREFER_IMAGE;
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
     * Returns the property we're viewing
     */
    private Property getProperty() {
      if (entity==null||path==null) return null;
      if (property!=null) return property==NULL ? null : (Property)property;
      path.setToFirst();
      property = entity.getProperty().getProperty(path, true);
      if (property!=null) return (Property)property; 
      property = NULL;
      return null;
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
     * @see genj.renderer.EntityRenderer.MyView#getPreferredSpan()
     */
    protected Dimension getPreferredSpan() {
      // get the property
      Property p = getProperty();
      if (p==null) return new Dimension(0,0);
      return proxy.getSize(getFontMetrics(), p, preference);
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
     * @see genj.renderer.EntityRenderer.MyView#getVerticalAlignment()
     */
    protected float getVerticalAlignment() {
      return proxy==null ? 0 : proxy.getVerticalAlignment(getFontMetrics());
    }
    /**
     * Invalidates this views current state
     */
    protected void invalidate() {
      // invalidate cached information that's depending
      // on the current entity's properties
      property = null;
      super.invalidate();
    }
    
  } //PropertyView

  
} //EntityRenderer
