/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.Dimension2d;
import genj.util.EnvironmentChecker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Dimension2D;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DocumentParser;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * A renderer for entities - blueprint necessary
 */
public class EntityRenderer {

  private final static Logger LOG = Logger.getLogger("genj.renderer");
  
  // this will initialize and load the html32 dtd
  // "/javax/swing/text/html/parser/html32.bdtd"
  static {
    new ParserDelegator();
  }
  
  /** the property image width */
  private static final int 
    PROP_IMAGE_WIDTH  = Indi.IMG_MALE.getIconWidth()+4,
    PROP_IMAGE_HEIGHT = Indi.IMG_MALE.getIconHeight();
  
  /** a no value char array */
  private static final Segment EMPTY_SEGMENT = new Segment(); 
  
  /** the root of our rendering view */
  private RootView root;
  
  /** the document we're looking at */
  private HTMLDocument doc = new MyHTMLDocument();
  
  /** the factory we're using to create views */
  private MyHTMLFactory factory = new MyHTMLFactory();
  
  /** a cached dtd for html */
  private static DTD dtd = null;
  
  /** the entity we're looking at */
  private Entity entity;
  
  /** all PropertyViews we know */
  private List<PropertyView> propViews = new ArrayList<PropertyView>(16);
  
  /** all TableViews we know */
  private List<View> tableViews = new ArrayList<View>(4);
  
  /** whether we have a debug mode */
  private boolean isDebug = false;
  
  /** current graphics context */
  private Graphics2D graphics;
  
  /**
   * Constructor
   */  
  public EntityRenderer(Blueprint bp) {
    this(bp, Options.getInstance().getDefaultFont());
  }
    
  /**
   * Constructor
   */  
  public EntityRenderer(Blueprint bp, Font font) {

    // we wrap the html in html/body
    StringBuffer html = new StringBuffer();
    html.append("<html><head><style type=\"text/css\">");
    if (font!=null) {
      html.append(" body { font-family: \""+font.getFamily()+"\"; font-size: "+font.getSize()+"pt; } "    );
    }
    html.append(" table { border-style: solid;}" );
    html.append(" td  { border-style: solid;  }" );
    html.append("</style></head><body>");
    html.append(bp.getHTML());
    html.append("</body></html>");

    // read and parse the html
    try {
      
      // I started out to use a HTMLEditorkit for reading the html into the document
      //  new HTMLEditorKit().read(new StringReader(html), doc, 0);
      // but this won't allow me to fix a problem with HTMLDocument.HTMLReader 
      // so I'm using ParserDelegator directly allowing me to use MyHTMLReader
      //  new ParserDelegator().parse(new StringReader(html), new HTMLReader(doc), false);
      // but I also want to override the DocumentParser which is instantiated
      // inside ParserDelegator.
      //  new DocumentParser(dtd).parse(r, cb, ignoreCharSet);
      // so I'm reading the dtd myself and creat my own DocumentParser
      
      // .. we need out own html reader (javax.swing.text.html.HTMLDocument.HTMLReader)
      MyHTMLReader reader = new MyHTMLReader(doc);
      
      // .. trigger parsing (we want our own parser here so there's a little bit more magic at this point)
      new MyDocumentParser(DTD.getDTD("html32")).parse(new StringReader(html.toString()), reader, false);
      
      // .. flush reader
      reader.flush();      
      
    } catch (Throwable t) {
      Logger.getLogger("genj.renderer").log(Level.WARNING, "can't parse blueprint "+bp, t);
    }

    // create the root view
    root = new RootView(factory.create(doc.getDefaultRootElement()));

    // done    
  }
  
  /**
   * Render the entity on given context
   */
  public void render(Graphics g, Entity e, Rectangle r) {

    try {
      // keep the entity and graphics
      entity = e;
      graphics = (Graphics2D)g;
      
      // invalidate views 
      for (PropertyView pv : propViews) 
        pv.invalidate();
      
      // and make sure TableView's update their grid
      for (View tv : tableViews) {
        // this will cause invalidateGrid on a javax.swing.text.html.TableView
        tv.replace(0,0,null);
      }
      
      // set the size of root - this triggers a layout of the views
      root.setSize((float)r.getWidth(),(float)r.getHeight());
      
      // clip and paint it
      Rectangle oc = g.getClipBounds();
      g.clipRect(r.x,r.y,r.width,r.height);
      try {
        root.paint(g, r);
      } finally {
        g.setClip(oc.x,oc.y,oc.width,oc.height);
      }

    } catch (Throwable t) {
      LOG.log(Level.WARNING, "can't render", t);
    }
    // done
  }
  
  /**
   * Sets debug mode 
   */
  public void setDebug(boolean set) {
    isDebug = set;
  }

  /**
   * Our own HTMLDocument
   */  
  private class MyHTMLDocument extends HTMLDocument {
    /**
     * @see javax.swing.text.DefaultStyledDocument#getFont(javax.swing.text.AttributeSet)
     */
    public Font getFont(AttributeSet attr) {
      Font font = super.getFont(attr);
      // see http://www.3rd-evolution.de/tkrammer/docs/java_font_size.html
      // While Java assumes 72 dpi screen resolution Windows uses 96 dpi or 120 dpi depending on your font size setting in the display properties. 
      if (!EnvironmentChecker.isMac()) {
        float factor = DPI.get(graphics).vertical()/72F; 
        font = font.deriveFont(factor*font.getSize2D());
      }
      return font;
    }
  } //MyHTMLDocument
  
  /**
   * My own parser that overrides the original's property 
   * <pre>strict=true</pre> - this will make the underlying parser 
   * not skip spaces after close-tags
   */
  private class MyDocumentParser extends DocumentParser {
    /**
     * Constructor
     */
    private MyDocumentParser(DTD dtd) {
      super(dtd);
      // patch strictness
      strict = true;
      // done      
    }
  } //MyDocumentParser

  /**
   * I've created my own subclass of HTMLDocument.HTMLReader a ParserCallback
   * to achieve some special behaviour overriding protected method:
   * @see HTMLDocument.HTMLReader.blockClose(Tag t)
   */
  private static class MyHTMLReader extends HTMLDocument.HTMLReader {
    /** whether we ignore content */
    private boolean skipContent = false;
    /**
     * Constructor
     */
    protected MyHTMLReader(HTMLDocument doc) {
      doc.super(0);
    }
    /**
     * In the original HTMLReader this will add a newline to the end
     * of a block (if there was no newline already). For tables this
     * means that there will be a InlineView (\n) that might flow into
     * a separate *empty* line 
     * <pre>
     *  if(!lastWasNewline) {
     *   addContent(NEWLINE, 0, 1, true);
     *   lastWasNewline = true;
     *  }
     * </pre>
     * 
     * @see javax.swing.text.html.HTMLDocument.HTMLReader#blockClose(javax.swing.text.html.HTML.Tag)
     */
    protected void blockClose(Tag t) {
      // mark that we skip anything that might be added to content 
      // in super.blockClose(). The super class' implementation
      // adds trailing \n's before a block-close. They tend to
      // flow into the next line resulting in empty full-height
      // lines when horizontal space is restricted :(
      skipContent = true;
      // delegate to super
      super.blockClose(t);
      // back to accepting content
      skipContent = false;
    }
    /**
     * @see javax.swing.text.html.HTMLDocument.HTMLReader#addContent(char, int, int, boolean)
     */
    protected void addContent(char[] data, int offs, int length, boolean generateImpliedPIfNecessary) {
      if (!skipContent) super.addContent(data, offs, length, generateImpliedPIfNecessary);
    }
    
  } //MyHTMLReader
  
  /**
   * My own HTMLFactory that extends the default one to support
   * tags <i>prop</i> and <i>i18n</i>
   */
  private class MyHTMLFactory extends HTMLFactory {
    
    /**
     * @see javax.swing.text.ViewFactory#create(Element)
     */
    public View create(Element elem) {
      
      String name = elem.getName();

      // check if the element is "prop"
      if ("prop".equals(name)) {
        PropertyView result = new PropertyView(elem);
        propViews.add(result);
        return result;
        
      }
      
      // maybe its "name" or "i18n"
      if ("name".equals(name)||"i18n".equals(name)) {
        return new I18NView(elem);
      }
        
      // default to super
      View result = super.create(elem);

      // .. keep track of TableViews for later dynamic invalidation
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
    private Dimension2D preferredSpan = null;
    
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
    
    /**
     * @see javax.swing.text.View#getPreferredSpan(int)
     */
    public float getPreferredSpan(int axis) {
      // check cached preferred Span
      if (preferredSpan==null) {
        preferredSpan = getPreferredSpan();
      }
      return (float)(axis==X_AXIS ? preferredSpan.getWidth() : preferredSpan.getHeight());
    }
    
    /**
     * @see javax.swing.text.View#getMaximumSpan(int)
     */
    public float getMaximumSpan(int axis) {
      return getPreferredSpan(axis);
    }

    /**
     * @see javax.swing.text.View#getAlignment(int)
     */
    public float getAlignment(int axis) {
      // horizontal unchanged
      if (X_AXIS==axis) 
        return super.getAlignment(axis);
      // height we prefer
      float height = (float)getPreferredSpan().getHeight();
      // where's first line's baseline
      FontMetrics fm = getGraphics().getFontMetrics();
      float h = fm.getHeight();
      float d = fm.getDescent();
      return (h-d)/height;
    }
    
    @Override
    public Graphics getGraphics() {
      graphics.setFont(getFont());
      return graphics;
    }
    
    /**
     * Get the preferred span
     */
    protected abstract Dimension2D getPreferredSpan();

    /** 
     * Returns the current font
     */
    protected Font getFont() {
      // we cached the font so that it's retrieved only once
      // instead of using this view's attributes we get the
      // document's stylesheet and ask it for this view's
      // attributes
      if (font==null) {
        font = doc.getFont(getAttributes());
      }
      return font;
    }
    
    /** 
     * Returns the current fg color
     */
    protected Color getForeground() {
  
      // we cached the color so that it's retrieved only once
      // instead of using this view's attributes we get the
      // document's stylesheet and ask it for this view's
      // attributes
      if (foreground==null) 
        //foreground = doc.getForeground(getAttributes());
        foreground = doc.getForeground(doc.getStyleSheet().getViewAttributes(this));
      
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
    
    /**
     * we use our kit's view factory
     */
    public ViewFactory getViewFactory() {
      return factory;
    }

  } //MyView

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
    
    @Override
    public Graphics getGraphics() {
      return graphics;
    }
    
    /**
     * the wrapped view needs to be sized
     */    
    public void setSize(float wIdth, float heIght) {
      // remember
      width = wIdth;
      height = heIght;
      // delegate
      try {
        view.setSize(width, height);
      } catch (Throwable t) {
      }
      // done
    }

    /**
     * @see genj.renderer.EntityRenderer.MyView#getPreferredSpan()
     */
    protected Dimension2D getPreferredSpan() {
      return new Dimension2d(
        (int)view.getPreferredSpan(X_AXIS),
        (int)view.getPreferredSpan(Y_AXIS)
      );
    }

  } //RootView

  /**
   * A view for translating text
   */
  private class I18NView extends MyView {
    
    /** the text to paint */
    private String txt = "?";
    
    /**
     * Constructor
     */
    private I18NView(Element elem) {
      super(elem);
      // resolve and localize text .. tag|entity
      Object o = elem.getAttributes().getAttribute("tag");
      if (o!=null) txt = Gedcom.getName(o.toString());
      else {
        o = elem.getAttributes().getAttribute("entity");
        if (o!=null) txt = Gedcom.getName(o.toString());
      }
      // done
    }
    /**
     * @see javax.swing.text.View#paint(java.awt.Graphics, java.awt.Shape)
     */
    public void paint(Graphics g, Shape allocation) {
      Rectangle r = (allocation instanceof Rectangle) ? (Rectangle)allocation : allocation.getBounds();
      g.setFont(getFont());
      g.setColor(getForeground());
      PropertyRenderer.DEFAULT.renderImpl((Graphics2D)g,r,txt,new HashMap<String, String>());
    }
    /**
     * @see genj.renderer.EntityRenderer.MyView#getPreferredSpan()
     */
    protected Dimension2D getPreferredSpan() {
      return PropertyRenderer.DEFAULT.getSizeImpl(null, txt, new HashMap<String, String>(), (Graphics2D)getGraphics());
    }
  } //LocalizeView

  /**
   * A view that wraps a property and its value
   */
  private class PropertyView extends MyView {
    
    // TODO Performance - can we improve property views through some caching of size&alignment?
    
    /** the tag path used */
    private List<TagPath> paths = new ArrayList<TagPath>();
    
    /** the cached property we're displaying */
    private Property cachedProperty = null;
    
    /** the attributes */
    private Map<String,String> attributes;
    
    /** minimum/maximum percentage of the rendering space */
    private int min, max;
    
    /** 
     * Constructor
     */
    PropertyView(Element elem) {
      super(elem);

      // prepare attributes
      attributes = new HashMap<String, String>();
      
      for (Enumeration<?> as = elem.getAttributes().getAttributeNames(); as.hasMoreElements(); ) {
        Object key = as.nextElement();
        if (key instanceof String)
          attributes.put((String)key, (String)elem.getAttributes().getAttribute(key));
      }
      
      // grab path
      Object p = elem.getAttributes().getAttribute("path");
      if (p!=null) for (String path : p.toString().split(",")) try {
        paths.add(new TagPath(path));
      } catch (IllegalArgumentException e) {
        if (LOG.isLoggable(Level.FINER))
          LOG.log(Level.FINER, "got wrong path "+path);
      }
      
      // minimum?
      min = getAttribute("min", 1, 100, 1);
      max = getAttribute("max", 1, 100, 100);
      
      // done
    }
    
    /**
     * Gets an int value from attributes
     */
    private int getAttribute(String key, int min, int max, int def) {
      // grab a value and try to parse
      String val = attributes.get(key);
      if (val!=null) try {
        return Math.max(min, Math.min(max, Integer.parseInt(val)));
      } catch (NumberFormatException e) {
      }
      // not found
      return def;
    }
    
    /**
     * Get Property
     */
    private Property getProperty() {
      // still looking for property?
      if (cachedProperty!=null)
        return cachedProperty;
      
      if (entity==null||paths.isEmpty())
        return null;

      Property result = null;
      
      for (TagPath path : paths) {
        result = entity.getProperty(path);
        if (result!=null)
          break;
      }
      
      if (paths.size()==1)
        cachedProperty = result;
      
      return result;
    }
    
    /** 
     * Get Renderer
     */
    private PropertyRenderer getRenderer(Property prop) {
      // 20030404 if no property is found we cannot cache
      // the renderer derived from path.getLast() - there
      // are defaults for certain tags preset in PropertyRenderer
      // but another call here with a different prop-type
      // might resolve to a different proxy
      
      // derive from property?
      PropertyRenderer result = PropertyRendererFactory.DEFAULT.getRenderer(paths.get(0), prop);

      // check renderer/prop compatibility
      if (prop==null&&!result.isNullRenderer()) 
        return null;
      
      // done
      return result;
    }
    
    
    /**
     * @see javax.swing.text.View#paint(Graphics, Shape)
     */
    public void paint(Graphics g, Shape allocation) {
      Graphics2D graphics = (Graphics2D)g;
      // property and renderer
      Property property = getProperty();
      PropertyRenderer renderer = getRenderer(property);
      // no renderer - no paint
      if (renderer==null) return;
      // setup painting attributes and bounds
      g.setColor(super.getForeground());
      g.setFont(super.getFont());
      Rectangle r = (allocation instanceof Rectangle) ? (Rectangle)allocation : allocation.getBounds();
      // debug?
      if (isDebug) 
        graphics.draw(r);
      // clip and render
      Shape old = graphics.getClip();
      graphics.clip(r);
      renderer.render(graphics, r, property, attributes);
      g.setClip(old);
      // done
    }
    /**
     * @see genj.renderer.EntityRenderer.MyView#getPreferredSpan()
     */
    protected Dimension2D getPreferredSpan() {
      // property and renderer
      Property property = getProperty();
      PropertyRenderer renderer = getRenderer(property);
      // no renderer - no spane
      if (renderer==null)
        return new Dimension(0,0);
      // calc span
      Dimension2D d = renderer.getSize(property, attributes, (Graphics2D)getGraphics());
      // check max
      d = new Dimension2d(Math.min(d.getWidth(), root.width*max/100), d.getHeight());
      return d;
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
     * Invalidates this views current state
     */
    protected void invalidate() {
      // invalidate cached information that's depending
      // on the current entity's properties
      cachedProperty = null;
      
      super.invalidate();
    }
    
  } //PropertyView

} //EntityRenderer
