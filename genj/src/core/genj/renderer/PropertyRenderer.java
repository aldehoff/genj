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

import genj.gedcom.IconValueAvailable;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.util.Dimension2d;
import genj.util.swing.ImageIcon;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class PropertyRenderer {

  private final static String STARS = "*****";
  
  private final static int IMAGE_GAP = 4;
  
  /** our preferences when drawing properties */
  public final static int
    PREFER_DEFAULT      = 0,
    PREFER_IMAGE        = 1,
    PREFER_TEXT         = 2,
    PREFER_IMAGEANDTEXT = 3;
  
  /** an empty dimension */
  private final static Dimension EMPTY_DIM = new Dimension(0,0);
  
  /** a default PropertyProxy */
  /*package*/ final static PropertyRenderer 
    DEFAULT_PROPERTY_PROXY = new PropertyRenderer(),
    SECRET_PROPERTY_PROXY  = new Secret();

  /** an replacement for a 'broken' image */  
  private final static ImageIcon broken = 
    new ImageIcon(PropertyRenderer.class, "Broken.gif");

  /** cached renderer instances */
  private static Map cache = new HashMap();
  
  static {
    cache.put("FILE", new PropertyRenderer.File());
  }

  /** 
   * static accessor  
   */
  public static PropertyRenderer get(Property prop) {
    
    // check secret
    if (prop.isSecret()) 
      return SECRET_PROPERTY_PROXY;
    
    // continue with tag
    return get(prop.getProxy());
  }

  /** 
   * static accessor  
   */
  public static PropertyRenderer get(String name) {
    
    synchronized (cache) {
      
      // have one?
      PropertyRenderer result = (PropertyRenderer)cache.get(name);
      if (result!=null) return result;
      
      // create one
      try {
        result = (PropertyRenderer)Class.forName(PropertyRenderer.class.getName()+"$"+name).newInstance();
      } catch (Throwable t) {
        //Debug.log(Debug.INFO, PropertyRenderer.class, "Couldn't find renderer for "+name, t);
        result = DEFAULT_PROPERTY_PROXY;
      }
      
      // remember
      cache.put(name, result);
      
      // done
      return result;
      
    }
  }  
  
  /**
   * Calculates the preferred size with given metrics, prop and image/text preferrence
   * @param metrics current font metrics
   * @param prop property 
   * @param preference rendering preference
   * @param dpi resolution or null  
   */
  public Dimension2D getSize(Font font, FontRenderContext context, Property prop, int preference, Point dpi) {
    return getSize(font, context, prop.getImage(false), prop.getDisplayValue(), preference, dpi);
  }
  
  /**
   * Renders the property on g with given bounds and image/text preference 
   * @param g to render on
   * @param bounds to stay in
   * @param prop property 
   * @param preference rendering preference
   * @param dpi resolution or null  
   */
  public void render(Graphics2D g, Rectangle bounds, Property prop, int preference, Point dpi) {
    render(g,bounds,prop.getImage(false),prop.getDisplayValue(),preference,dpi);
  }
  
  /**
   * Calculates the vertical alignment offset - by default the baseline of the current font
   */
  public float getVerticalAlignment(Font font, FontRenderContext context) {  
    LineMetrics lm = font.getLineMetrics("", context);
    float h = lm.getHeight();
    float d = lm.getDescent();
    return (h-d)/h;
  }

  /**
   * Implementation for calculating size with given img/txt
   */
  protected Dimension2D getSize(Font font, FontRenderContext context, ImageIcon img, String txt, int preference, Point dpi) {
    double 
      w = 0,
      h = 0;
    // calculate text size (the default size we use)
    if (isText(preference)) {
      Rectangle2D bounds = font.getStringBounds(txt, context);
      w += bounds.getWidth();
      h = Math.max(h, bounds.getHeight());
    }
    // add image size
    if (isImage(preference)) {
      LineMetrics lm = font.getLineMetrics("", context);
      float max = lm.getHeight();
      float scale = 1F;
      if (max<img.getIconHeight()) 
        scale = max/img.getIconHeight();
      w += (int)Math.ceil(img.getIconWidth()*scale) + IMAGE_GAP;
      h = Math.max(h, lm.getHeight());
    }
    
    // done
    return new Dimension2d(w,h);
  }

  /**
   * Implementation for rendering img/txt 
   */
  protected void render(Graphics2D g, Rectangle bounds, ImageIcon img, String txt, int preference, Point dpi) {
    // image?
    if (isImage(preference)) render(g, bounds, img, dpi);
    // text?
    if (isText(preference)) render(g, bounds, txt);
    // done
  }
  
  /**
   * Implementation for rendering img
   */
  protected void render(Graphics2D g, Rectangle bounds, ImageIcon img, Point dpi) {
    
    // no space?
    if (bounds.getHeight()==0||bounds.getWidth()==0)
      return;
    
    // draw image with maximum height of a character
    int 
      w = img.getIconWidth(),
      max = g.getFontMetrics().getHeight();
    
    AffineTransform at = AffineTransform.getTranslateInstance(bounds.getX(), bounds.getY());
    if (max<img.getIconHeight()) {
      float scale = max/(float)img.getIconHeight();
      at.scale(scale, scale);
      w = (int)Math.ceil(w*scale);
    }
    g.drawImage(img.getImage(), at, null);
    
    // patch bounds for skip
    bounds.x += w+IMAGE_GAP;
    bounds.width -= w+IMAGE_GAP;
  }
  
  /**
   * Implementation for rendering txt
   */
  protected void render(Graphics2D g, Rectangle bounds, String txt) {
    g.drawString(txt, bounds.x, bounds.y+bounds.height-g.getFontMetrics().getDescent());
  }

  /**
   * Check preference for option to draw image
   */
  protected boolean isImage(int preference) {
    return preference==PREFER_IMAGE||preference==PREFER_IMAGEANDTEXT;
  }
  
  /**
   * Check preference for option to draw text
   */
  protected boolean isText(int preference) {
    return preference==PREFER_TEXT||preference==PREFER_IMAGEANDTEXT||preference==PREFER_DEFAULT;
  }
  
  /**
   * Whether this renderer wants to paint NULL
   */
  protected boolean isNullRenderer() {
    return false;
  }

  /**
   * Sex
   */
  /*package*/ static class Sex extends PropertyRenderer {

    /** 
     * size override
     */
    public Dimension2D getSize(Font font, FontRenderContext context, Property prop, int preference, Point dpi) {
      if (preference==PREFER_DEFAULT) preference = PREFER_IMAGE;
      return super.getSize(font, context, prop, preference, dpi);
    }

    /**
     * render override
     */
    public void render( Graphics2D g, Rectangle bounds, Property prop, int preference, Point dpi) {
      if (preference==PREFER_DEFAULT) preference = PREFER_IMAGE;
      super.render(g, bounds, prop, preference, dpi);
    }
  
  } //Sex

  /**
   * MLE
   */
  /*package*/ static class MLE extends PropertyRenderer {
  
    /**
     * size override
     */
    public Dimension2D getSize(Font font, FontRenderContext context, Property prop, int preference, Point dpi) {
      
      //.gotta be multiline
      if (!(prop instanceof MultiLineProperty))
        return super.getSize(font, context, prop, preference, dpi);
      
      // count 'em
      int lines = 0;
      double width = 0;
      double height = 0;
      MultiLineProperty.Iterator line = ((MultiLineProperty)prop).getLineIterator();
      do {
        lines++;
        Rectangle2D bounds = font.getStringBounds(line.getValue(), context);
        width = Math.max(width, bounds.getWidth());
        height += bounds.getHeight();
      } while (line.next());
      
      // done
      return new Dimension2d(width, height);
    }
  
    /**
     * render override
     */
    public void render( Graphics2D g, Rectangle bounds, Property prop, int preference, Point dpi) {
      
      // gotta be multiline
      if (!(prop instanceof MultiLineProperty)) {
        super.render(g, bounds, prop, preference, dpi);
        return;
      }
      
      // get lines
      MultiLineProperty.Iterator line = ((MultiLineProperty)prop).getLineIterator();
      
      // paint
      Graphics2D graphics = (Graphics2D)g;
      Rectangle clip = g.getClipBounds();
      
      Font font = g.getFont();
      FontRenderContext context = graphics.getFontRenderContext();

      float 
        x = (float)bounds.getX(),
        y = (float)bounds.getY();
      
      do {

        // analyze line
        String txt = line.getValue();
        LineMetrics lm = font.getLineMetrics(txt, context);
        y += lm.getHeight();
        
        // draw line
        graphics.drawString(txt, x, y - lm.getDescent());
        
        // .. break if not visible anymore
        if (y>bounds.getMaxY()) 
          break;
        
      } while (line.next());
      // done
    }
    
  } //MLE

  /**
   * File
   */
  /*package*/ static class File extends PropertyRenderer {

    /**
     * size override 
     */
    public Dimension2D getSize(Font font, FontRenderContext context, Property prop, int preference, Point dpi) {
      
      // try to resolve image
      ImageIcon img = getImage(prop, preference);
      if (img==null) 
        return EMPTY_DIM;

      // ask it for size
      return img.getSize(dpi);
        
    }
    
    /**
     * render override
     */
    public void render(Graphics2D g, Rectangle bounds, Property prop, int preference, Point dpi) {
      
      // grab the image
      ImageIcon img = getImage(prop, preference);
      if (img==null) return;
      
      // get unit graphics up
      UnitGraphics ug = new UnitGraphics(g, 1, 1);
      ug.pushTransformation();
      ug.setColor(Color.black);
      ug.translate(bounds.x, bounds.y);
      
      // calculate factor - the image's dpi might be
      // different than that of the rendered surface
      Point idpi = img.getResolution();
      double
       scalex = 1,
       scaley = 1;
      if (idpi!=null) {
       scalex *= (double)dpi.x/idpi.x;
       scaley *= (double)dpi.y/idpi.y;
      }
       
      // check bounds - the image might still be too
      // big - in that case we simply scale down to
      // maximum allowed
      double 
        w = img.getIconWidth ()*scalex,
        h = img.getIconHeight()*scaley;
      if (bounds.width<w||bounds.height<h) {
        double zoom = Math.min(
          bounds.width/w, bounds.height/h
        );
        scalex *= zoom;
        scaley *= zoom;
      }        
        
      // scale and draw
      ug.scale(scalex, scaley);
      ug.draw(img, 0, 0, 0, 0);
      
      // restore graphics
      ug.popTransformation();
         
      // done
    }

    /**
     * place pictures on baseline of current font
     */
    public float getVerticalAlignment(Font font, FontRenderContext context) {
      return 1F;
    }
    
    /**
     * Helper to get the image of PropertyFile
     */
    private ImageIcon getImage(Property prop, int preference) {
      // check file for image
      ImageIcon result = null;
      if (prop instanceof IconValueAvailable) 
        result = ((IconValueAvailable)prop).getValueAsIcon();
      // fallback
      if (result==null&&isImage(preference)) return broken;
      // done
      return result;
    }  
  
    /**
     * @see genj.renderer.PropertyRenderer#isNullRenderer()
     */
    protected boolean isNullRenderer() {
      return true;
    }

  } //File

  /**
   * Entity
   */
  /*package*/ static class Entity extends PropertyRenderer {
  
    /**
     * size override
     */
    public Dimension2D getSize(Font font, FontRenderContext context, Property prop, int preference, Point dpi) {
      return super.getSize(font, context, prop.getImage(false), ((genj.gedcom.Entity)prop).getId(), preference, dpi);
    }
  
    /**
     * render override
     */
    public void render(Graphics2D g, Rectangle bounds, Property prop, int preference, Point dpi) {
      super.render(g, bounds, prop.getImage(false), ((genj.gedcom.Entity)prop).getId(), preference, dpi);
    }
    
  } //Entity

  /**
   * XRef
   */
  /*package*/ static class XRef extends PropertyRenderer {
    
    /** 
     * size override
     */
    public Dimension2D getSize(Font font, FontRenderContext context, Property prop, int preference, Point dpi) {
      if (prop instanceof PropertyXRef) {
        Object e = ((PropertyXRef)prop).getReferencedEntity();
        if (e!=null) 
          return super.getSize(font, context, prop.getImage(false), e.toString(), preference, dpi);
      }
      return super.getSize(font, context, prop, preference, dpi);
    }

    /**
     * render override
     */
    public void render( Graphics2D g, Rectangle bounds, Property prop, int preference, Point dpi) {
      if (prop instanceof PropertyXRef) {
        Object e = ((PropertyXRef)prop).getReferencedEntity();
        if (e!=null) {
          super.render(g, bounds, prop.getImage(false), e.toString(), preference, dpi);
          return;
        }
      }
      super.render(g, bounds, prop, preference, dpi);
    }
  
  } //XRef
      
  /**
   * name
   */
  /*package*/ static class Secret extends PropertyRenderer {
  
    /**
     * size override
     */
    public Dimension2D getSize(Font font, FontRenderContext context, Property prop, int preference, Point dpi) {
      return super.getSize(font, context, prop.getImage(false), STARS, preference, dpi);
    }
  
    /**
     * render override
     */
    public void render( Graphics2D g, Rectangle bounds, Property prop, int preference, Point dpi) {
      super.render(g, bounds, prop.getImage(false), STARS, preference, dpi);
    }
    
  } //Secret
      
} //PropertyProxy
