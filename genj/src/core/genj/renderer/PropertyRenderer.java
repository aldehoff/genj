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
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyXRef;
import genj.util.WordBuffer;
import genj.util.swing.ImageIcon;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class PropertyRenderer {

  private final static String STARS = "*****";
  
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
    if (prop.isPrivate()) 
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
  public Dimension getSize(FontMetrics metrics, Property prop, int preference, Point dpi) {
    return getSize(metrics, prop.getImage(false), prop.getValue(), preference, dpi);
  }
  
  /**
   * Renders the property on g with given bounds and image/text preference 
   * @param g to render on
   * @param bounds to stay in
   * @param prop property 
   * @param preference rendering preference
   * @param dpi resolution or null  
   */
  public void render(Graphics g, Rectangle bounds, Property prop, int preference, Point dpi) {
    render(g,bounds,prop.getImage(false),prop.getValue(),preference,dpi);
  }
  
  /**
   * Calculates the vertical alignment offset 
   */
  public float getVerticalAlignment(FontMetrics metrics) {  
    float h = metrics.getHeight();
    float d = metrics.getDescent();
    return (h-d)/h;
  }  
  
  /**
   * Implementation for calculating size with given img/txt
   */
  protected Dimension getSize(FontMetrics metrics, ImageIcon img, String txt, int preference, Point dpi) {
    Dimension result = new Dimension(0,0);
    // text?
    if (isText(preference)) {
      if (null!=txt) {
        result.width += metrics.stringWidth(txt);
        result.height = Math.max(result.height, metrics.getHeight());
      }
    }
    // image?
    if (isImage(preference)) {
      result.width += img.getIconWidth() + metrics.charWidth(' ');
      result.height = Math.max(result.height, img.getIconHeight());
    }
    // done
    return result;
  }

  /**
   * Implementation for rendering img/txt 
   */
  protected void render(Graphics g, Rectangle bounds, ImageIcon img, String txt, int preference, Point dpi) {
    // image?
    if (isImage(preference)) render(g, bounds, img, dpi);
    // text?
    if (isText(preference)) render(g, bounds, txt);
    // done
  }
  
  /**
   * Implementation for rendering img
   */
  protected void render(Graphics g, Rectangle bounds, ImageIcon img, Point dpi) {
    img.paintIcon(g,bounds.x,bounds.y+(bounds.height-img.getIconHeight())/2);
    int skip = img.getIconWidth() + g.getFontMetrics().charWidth(' ');
    bounds.x += skip;
    bounds.width -= skip;
  }

  /**
   * Implementation for rendering txt
   */
  protected void render(Graphics g, Rectangle bounds, String txt) {
    // check whether we'll have to zoom
    FontMetrics fm = g.getFontMetrics();
    // by default we place the texts base at the bottom of bounds
    int y = bounds.y+bounds.height;
    // if bounds is high enough we patch up by fm's descent
    if (bounds.height>=fm.getAscent()) y -= fm.getDescent();
    // and paint
    g.drawString(txt, bounds.x, y);
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
    public Dimension getSize(FontMetrics metrics, Property prop, int preference, Point dpi) {
      if (preference==PREFER_DEFAULT) preference = PREFER_IMAGE;
      return super.getSize(metrics, prop, preference, dpi);
    }

    /**
     * render override
     */
    public void render( Graphics g, Rectangle bounds, Property prop, int preference, Point dpi) {
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
    public Dimension getSize(FontMetrics metrics, Property prop, int preference, Point dpi) {
      //.gotta be multiline
      if (!(prop instanceof MultiLineProperty))
        return super.getSize(metrics, prop, preference, dpi);
      // count 'em
      int lines = 0;
      int width = 0;
      MultiLineProperty.Iterator line = ((MultiLineProperty)prop).getLineIterator();
      do {
        lines++;
        width = Math.max(width, metrics.stringWidth(line.getValue()));
      } while (line.next());
      // done
      return new Dimension(width, metrics.getHeight()*lines);
    }
  
    /**
     * render override
     */
    public void render( Graphics g, Rectangle bounds, Property prop, int preference, Point dpi) {
      //.gotta be multiline
      if (!(prop instanceof MultiLineProperty)) {
        super.render(g, bounds, prop, preference, dpi);
        return;
      }
      // get lines
      MultiLineProperty.Iterator line = ((MultiLineProperty)prop).getLineIterator();
      // paint
      Rectangle clip = g.getClipBounds();
      int 
        h = g.getFontMetrics().getHeight(),
        m = clip.y + clip.height;
      Rectangle r = new Rectangle();
      r.x = bounds.x;
      r.y = bounds.y;
      r.width = bounds.width;
      r.height= h;
      do {
        
        // .. line at a time
        super.render(g, r, line.getValue());
        
        // .. movin' down
        r.y += h;
        
        // .. break if not visible anymore
        if (r.y>m) break;
        
      } while (line.next());
      // done
    }
    
  } //MLE

  /**
   * name
   */
  /*package*/ static class Name extends PropertyRenderer {
  
    /**
     * size override
     */
    public Dimension getSize(FontMetrics metrics, Property prop, int preference, Point dpi) {
      return super.getSize(metrics, prop.getImage(false), getName(prop), preference, dpi);
    }
  
    /**
     * render override
     */
    public void render( Graphics g, Rectangle bounds, Property prop, int preference, Point dpi) {
      super.render(g, bounds, prop.getImage(false), getName(prop), preference, dpi);
    }
    
    /**
     * helper to get the name of PropertyName
     */
    private String getName(Property prop) {
      if (!(prop instanceof PropertyName)||!prop.isValid()) 
        return prop.getValue();
      PropertyName name = (PropertyName)prop;
      WordBuffer b = new WordBuffer().setFiller(", ");
      b.append(name.getLastName());
      b.append(name.getFirstName());
      return b.toString();
    }  
  
  } //Name

  /**
   * File
   */
  /*package*/ static class File extends PropertyRenderer {

    /**
     * size override 
     */
    public Dimension getSize(FontMetrics metrics, Property prop, int preference, Point dpi) {
      
      // try to resolve image
      ImageIcon img = getImage(prop, preference);
      if (img==null) return EMPTY_DIM;

      // ask it for size
      return img.getSize(dpi);
        
    }
    
    /**
     * render override
     */
    public void render(Graphics g, Rectangle bounds, Property prop, int preference, Point dpi) {
      
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
     * 
     */
    public float getVerticalAlignment(FontMetrics metrics) {  
      return 1.0F;
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
    public Dimension getSize(FontMetrics metrics, Property prop, int preference, Point dpi) {
      return super.getSize(metrics, prop.getImage(false), ((genj.gedcom.Entity)prop).getId(), preference, dpi);
    }
  
    /**
     * render override
     */
    public void render(Graphics g, Rectangle bounds, Property prop, int preference, Point dpi) {
      super.render(g, bounds, prop.getImage(false), ((genj.gedcom.Entity)prop).getId(), preference, dpi);
    }
    
  } //Entity

  /**
   * Date
   */
  /*package*/ static class Date extends PropertyRenderer {
  
    /**
     * size override
     */
    public Dimension getSize(FontMetrics metrics, Property prop, int preference, Point dpi) {
      return super.getSize(metrics, prop.getImage(false), getDate(prop), preference, dpi);
    }
  
    /**
     * render override
     */
    public void render(Graphics g, Rectangle bounds, Property prop, int preference, Point dpi) {
      super.render(g, bounds, prop.getImage(false), getDate(prop), preference, dpi);
    }
  
    /**
     * render override (right aligned)
     */
    protected void render(Graphics g, Rectangle bounds, String txt) {
      int w = g.getFontMetrics().stringWidth(txt);
      bounds.x = Math.max(
        bounds.x,
        bounds.x+bounds.width-w
      );
      super.render(g, bounds, txt);
    }
  
    /**
     * Helper to get the date of PropertyDate
     */
    private String getDate(Property prop) {
      if (!(prop instanceof PropertyDate)||!prop.isValid()) 
        return prop.getValue();
      PropertyDate date = (PropertyDate)prop;
      return date.toString(true);
    }  
  
  } //Date

  /**
   * XRef
   */
  /*package*/ static class XRef extends PropertyRenderer {
    
    /** 
     * size override
     */
    public Dimension getSize(FontMetrics metrics, Property prop, int preference, Point dpi) {
      if (prop instanceof PropertyXRef) {
        Object e = ((PropertyXRef)prop).getReferencedEntity();
        if (e!=null) 
          return super.getSize(metrics, prop.getImage(false), e.toString(), preference, dpi);
      }
      return super.getSize(metrics, prop, preference, dpi);
    }

    /**
     * render override
     */
    public void render( Graphics g, Rectangle bounds, Property prop, int preference, Point dpi) {
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
    public Dimension getSize(FontMetrics metrics, Property prop, int preference, Point dpi) {
      return super.getSize(metrics, prop.getImage(false), STARS, preference, dpi);
    }
  
    /**
     * render override
     */
    public void render( Graphics g, Rectangle bounds, Property prop, int preference, Point dpi) {
      super.render(g, bounds, prop.getImage(false), STARS, preference, dpi);
    }
    
  } //Secret
      
} //PropertyProxy
