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
import java.awt.Toolkit;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.BlockView;
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
  
  /** a no value char array */
  private static final Segment EMPTY_SEGMENT = new Segment(); 
  
  /** the root of our rendering view */
  private View root;
  
  /** the kit we're using */
  private HTMLEditorKit kit = new ModifiedHTMLEditorKit();
  
  /** the document we're looking at */
  private HTMLDocument doc = new HTMLDocument();
  
  /** the entity we're looking at */
  private Entity entity;
  
  /** all PropertyViews we know */
  private List propViews = new ArrayList(16);
  
  /** the proxies we know */
  private static Map proxies = new HashMap(10);
  
  /**
   * 
   */  
  public EntityRenderer(String html) {
    
    // we wrap the html in html/body
    html = "<html><body>"+html+"</body></html>";
    
    // read the html
    try {
      kit.read(new StringBufferInputStream(html), doc, 0);
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
  public void render(Graphics g, Dimension d) {
    root.setSize(d.width,d.height);
    root.paint(g, new Rectangle(0,0,d.width,d.height));
  }
  
  /**
   * Setter - entity
   */
  public void setEntity(Entity set) {
    
    // keep it
    entity = set;
    
    // mark views 
    Iterator it = propViews.iterator();
    while (it.hasNext()) {
      View v = (View)it.next();
      v.preferenceChanged(null,true,true);
    }
    
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
  private class ModifiedHTMLEditorKit extends HTMLEditorKit {
    
    /** the view factory we use */
    private ViewFactory factory = new ModifiedHTMLFactory();
  
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
  private class ModifiedHTMLFactory extends HTMLFactory {
    
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
        result = super.create(elem);
      }
      
      // done
      return result;
    }
  
  } //ModifiedHTMLFactory
  
  /**
   * 
   */
  private abstract class BaseView extends View {
    /**
     * Constructor
     */
    BaseView(Element elem) {
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
  } //BaseView

  /**
   * RootView onto a HTML Document
   */
  private class RootView extends BaseView {

    /** the root of the html's view hierarchy */
    private View view;

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
    public void setSize(float width, float height) {
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
  private class PropertyView extends BaseView {
    
    /** our preference when looking at the property */
    private int preference;
    
    /** the proxy used */
    private PropertyProxy proxy = null;
    
    /** the tag path used */
    private TagPath path = null;
    
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
      
      // done
    }
    
    /**
     * Returns the property we're viewing
     */
    private Property getProperty() {
      if (entity==null) return null;
      path.setToFirst();
      return entity.getProperty().getProperty(path, true);
    }
    
    /** 
     * Returns the current metrics
     */
    private FontMetrics getFontMetrics() {
      return Toolkit.getDefaultToolkit().getFontMetrics(getFont());
    }
    
    /** 
     * Returns the current font
     */
    private Font getFont() {
      return doc.getFont(getAttributes());
    }
    
    /** 
     * Returns the current fg color
     */
    private Color getForeground() {
      return doc.getForeground(getAttributes());
    }
    
    /**
     * @see javax.swing.text.View#getPreferredSpan(int)
     */
    public float getPreferredSpan(int axis) {
      if (proxy==null) return 0;
      Property p = getProperty();
      if (p==null) return 0;
      Dimension d = proxy.getSize(getFontMetrics(), p, preference);
      return axis==X_AXIS ? d.width : d.height;
    }

    /**
     * @see javax.swing.text.View#paint(Graphics, Shape)
     */
    public void paint(Graphics g, Shape allocation) {
      // find property
      if (proxy==null) return ;
      Property p = getProperty();
      if (p==null) return;
      // setup painting attributes
      g.setColor(getForeground());
      g.setFont(getFont());
      // render
      Rectangle bounds = (allocation instanceof Rectangle) ? (Rectangle)allocation : allocation.getBounds();
      proxy.render(g, bounds, p, preference);
      // done
    }
    /**
     * @see javax.swing.text.View#getAlignment(int)
     */
    public float getAlignment(int axis) {
      if (proxy==null) return 1;
      if (X_AXIS==axis)
        return super.getAlignment(axis);
      return proxy.getVerticalAlignment(getFontMetrics());
    }
    /**
     * @see javax.swing.text.View#getBreakWeight(int, float, float)
     */
    public int getBreakWeight(int axis, float pos, float len) {
      if (len > getPreferredSpan(axis)) {
          return GoodBreakWeight;
      }
      return BadBreakWeight;
    }  
    /**
     * @see javax.swing.text.View#breakView(int, int, float, float)
     */
    public View breakView(int axis, int offset, float pos, float len) {
      // don't allow a break
      return this;
    }
  } //PropertyView

  
}
