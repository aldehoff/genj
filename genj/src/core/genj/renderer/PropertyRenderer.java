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

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyName;
import genj.util.Debug;
import genj.util.WordBuffer;
import genj.util.swing.ImageIcon;

/**
 * 
 */
public class PropertyRenderer {
  
  /** our preferences when drawing properties */
  public final static int
    PREFER_DEFAULT      = 0,
    PREFER_IMAGE        = 1,
    PREFER_TEXT         = 2,
    PREFER_IMAGEANDTEXT = 3;
  
  /** a default PropertyProxy */
  private final static PropertyRenderer DEFAULT_PROPERTY_PROXY = new PropertyRenderer();

  /** cached renderer instances */
  private static Map cache = new HashMap();
  
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
        Debug.log(Debug.INFO, PropertyRenderer.class, "Couldn't find renderer for "+name, t);
        result = DEFAULT_PROPERTY_PROXY;
      }
      
      // remember
      cache.put(name, result);
      
      // done
      return result;
      
    }
  }  
  
  /**
   * 
   */
  public Dimension getSize(FontMetrics metrics, Property prop, int preference) {
    return getSize(metrics, prop.getImage(false), prop.getValue(), preference);
  }
  
  /**
   * 
   */
  public void render(Graphics g, Rectangle bounds, Property prop, int preference) {
    render(g,bounds,prop.getImage(false),prop.getValue(),preference);
  }
  
  /**
   * 
   */
  public float getVerticalAlignment(FontMetrics metrics) {  
    float h = metrics.getHeight();
    float d = metrics.getDescent();
    return (h-d)/h;
  }  
  
  /**
   * 
   */
  protected Dimension getSize(FontMetrics metrics, ImageIcon img, String txt, int preference) {
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
   * 
   */
  protected void render(Graphics g, Rectangle bounds, ImageIcon img, String txt, int preference) {
    // image?
    if (isImage(preference)) render(g, bounds, img);
    // text?
    if (isText(preference)) render(g, bounds, txt);
    // done
  }
  
  /**
   * 
   */
  protected void render(Graphics g, Rectangle bounds, ImageIcon img) {
    img.paintIcon(g,bounds.x,bounds.y+(bounds.height-img.getIconHeight())/2);
    int skip = img.getIconWidth() + g.getFontMetrics().charWidth(' ');
    bounds.x += skip;
    bounds.width -= skip;
  }

  /**
   * 
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
   * Check whether to draw image or not
   */
  protected boolean isImage(int preference) {
    return preference==PREFER_IMAGE||preference==PREFER_IMAGEANDTEXT;
  }
  
  /**
   * Check whether to draw text or not
   */
  protected boolean isText(int preference) {
    return preference==PREFER_TEXT||preference==PREFER_IMAGEANDTEXT||preference==PREFER_DEFAULT;
  }


  /**
   * Sex
   */
  private static class Sex extends PropertyRenderer {

    /** 
     * size override
     */
    public Dimension getSize(FontMetrics metrics, Property prop, int preference) {
      if (preference==PREFER_DEFAULT) preference = PREFER_IMAGE;
      return super.getSize(metrics, prop, preference);
    }

    /**
     * render override
     */
    public void render( Graphics g, Rectangle bounds, Property prop, int preference) {
      if (preference==PREFER_DEFAULT) preference = PREFER_IMAGE;
      super.render(g, bounds, prop, preference);
    }
  
  } //Sex

  /**
   * MLE
   */
  private static class MLE extends PropertyRenderer {
  
    /**
     * size override
     */
    public Dimension getSize(FontMetrics metrics, Property prop, int preference) {
      // check lines 
      Property.LineIterator it = prop.getLineIterator();
      if (it==null) return new Dimension(0,0);
      // count 'em
      int lines = 0;
      int width = 0;
      while (it.hasMoreValues()) {
        width = Math.max(width, metrics.stringWidth(it.getNextValue()));
        lines++;
      }
      // done
      return new Dimension(width, metrics.getHeight()*lines);
    }
  
    /**
     * render override
     */
    public void render( Graphics g, Rectangle bounds, Property prop, int preference) {
      // get lines
      Property.LineIterator it = prop.getLineIterator();
      if (it==null) return;
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
      while (it.hasMoreValues()) {
        // .. line at a time
        String line = it.getNextValue();
        super.render(g, r, line);
        // .. movin' down
        r.y += h;
        // .. break if not visible anymore
        if (r.y>m) break;
      }
      // done
    }
    
  } //MLE

  /**
   * name
   */
  private static class Name extends PropertyRenderer {
  
    /**
     * size override
     */
    public Dimension getSize(FontMetrics metrics, Property prop, int preference) {
      return super.getSize(metrics, prop.getImage(false), getName(prop), preference);
    }
  
    /**
     * render override
     */
    public void render( Graphics g, Rectangle bounds, Property prop, int preference) {
      super.render(g, bounds, prop.getImage(false), getName(prop), preference);
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

  /** an replacement for a 'broken' image */  
  private final static ImageIcon broken = 
    //new ImageIcon(new javax.swing.text.html.ImageView(null).getNoImageIcon());
    new ImageIcon(Object.class.getResourceAsStream("/javax/swing/text/html/icons/image-failed.gif"));
  
  /**
   * File
   */
  private static class File extends PropertyRenderer {
  
    /**
     * size override 
     */
    public Dimension getSize(FontMetrics metrics, Property prop, int preference) {
      ImageIcon img = getImage(prop);
      return new Dimension(img.getIconWidth(), img.getIconHeight());
    }
  
    /**
     * render override
     */
    public void render(Graphics g, Rectangle bounds, Property prop, int preference) {
      // grab the image
      ImageIcon img = getImage(prop);
      int
        h = img.getIconHeight(),
        w = img.getIconWidth ();
      // check if we should zoom
      double zoom = Math.min(
        Math.min(1.0D, ((double)bounds.width )/w),
        Math.min(1.0D, ((double)bounds.height)/h)
      );
      img.paintIcon(g, bounds.x, bounds.y, zoom);
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
    private ImageIcon getImage(Property prop) {
      ImageIcon result = null;
      if (prop instanceof PropertyFile) { 
        PropertyFile file = (PropertyFile)prop;
        result = file.getValueAsIcon();
      }
      if (result==null) result = broken;
      return result;
    }  
  
  } //File

  /**
   * Entity
   */
  private static class Entity extends PropertyRenderer {
  
    /**
     * size override
     */
    public Dimension getSize(FontMetrics metrics, Property prop, int preference) {
      return super.getSize(metrics, prop.getImage(false), ((genj.gedcom.Entity)prop).getId(), preference);
    }
  
    /**
     * render override
     */
    public void render(Graphics g, Rectangle bounds, Property prop, int preference) {
      super.render(g, bounds, prop.getImage(false), ((genj.gedcom.Entity)prop).getId(), preference);
    }
    
  } //Entity

  /**
   * Date
   */
  private static class Date extends PropertyRenderer {
  
    /**
     * size override
     */
    public Dimension getSize(FontMetrics metrics, Property prop, int preference) {
      return super.getSize(metrics, prop.getImage(false), getDate(prop), preference);
    }
  
    /**
     * render override
     */
    public void render(Graphics g, Rectangle bounds, Property prop, int preference) {
      super.render(g, bounds, prop.getImage(false), getDate(prop), preference);
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
      return date.toString(false, true);
    }  
  
  } //Date
      
} //PropertyProxy
