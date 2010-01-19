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
import genj.gedcom.IconValueAvailable;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMultilineValue;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.util.Dimension2d;
import genj.util.swing.ImageIcon;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.util.Map;

/**
 * A property renderer knows how to render a property into a graphics context
 */
public class PropertyRenderer {

  public final static PropertyRenderer DEFAULT = new PropertyRenderer();

  private final static String STARS = "*****";
  
  private final static int IMAGE_GAP = 4;
  
  /** an empty dimension */
  private final static Dimension EMPTY_DIM = new Dimension(0,0);
  
  /** an replacement for a 'broken' image */  
  private final static ImageIcon broken = 
    new ImageIcon(PropertyRenderer.class, "Broken");

  public static final String HINT_KEY_TXT = "txt";
  public static final String HINT_KEY_IMG = "img";
  public static final String HINT_KEY_SHORT = "short";

  public static final String HINT_VALUE_TRUE = "yes";
  public static final String HINT_VALUE_FALSE = "no";
  
  /**
   * acceptable check
   */
  public boolean accepts(TagPath path, Property prop) {
    // we take everything
    return true;
  }

  /**
   * Calculates the preferred size with given metrics, prop and image/text preferrence
   * @param metrics current font metrics
   * @param prop property 
   * @param preference rendering preference
   * @param dpi resolution or null  
   */
  public final Dimension2D getSize(Property prop, Map<String,String> attributes, Graphics2D graphics) {
    return getSizeImpl(prop, attributes, graphics);
  }
  
  protected Dimension2D getSizeImpl(Property prop, Map<String,String> attributes, Graphics2D graphics) {
    return getSizeImpl(prop, prop.getDisplayValue(), attributes, graphics);

  }
  protected Dimension2D getSizeImpl(Property prop, String txt, Map<String,String> attributes, Graphics2D graphics) {
    
    double 
      w = 0,
      h = 0;
    // calculate text size (the default size we use)
    FontMetrics fm = graphics.getFontMetrics();
    if (!HINT_VALUE_FALSE.equals(attributes.get(HINT_KEY_TXT))&&txt.length()>0) {
      w += fm.stringWidth(txt);
      h = Math.max(h, fm.getAscent() + fm.getDescent());
    }
    // add image size
    if (HINT_VALUE_TRUE.equals(attributes.get(HINT_KEY_IMG))) {
      ImageIcon img = prop.getImage(false);
      float max = fm.getHeight();
      float scale = 1F;
      if (max<img.getIconHeight()) 
        scale = max/img.getIconHeight();
      w += (int)Math.ceil(img.getIconWidth()*scale) + IMAGE_GAP;
      h = Math.max(h, fm.getHeight());
    }
    
    // done
    return new Dimension2d(w,h);
  }

  /**
   * Renders the property on g with given bounds and image/text preference 
   * @param g to render on
   * @param bounds to stay in
   * @param prop property 
   * @param preference rendering preference
   * @param dpi resolution or null  
   */
  public final void render(Graphics2D g, Rectangle bounds, Property prop, Map<String,String> attributes) {
    renderImpl(g,bounds,prop,attributes);
  }
  
  protected void renderImpl(Graphics2D g, Rectangle bounds, Property prop, Map<String,String> attributes) {
    renderImpl(g,bounds,prop,prop.getDisplayValue(),attributes);
  }
  
  /**
   * Implementation for rendering img/txt 
   */
  protected void renderImpl(Graphics2D g, Rectangle bounds, Property prop, String txt, Map<String,String> attributes) {
    // image?
    if (HINT_VALUE_TRUE.equals(attributes.get(HINT_KEY_IMG))) 
      renderImpl(g, bounds, prop.getImage(false));
    // text?
    if (!HINT_VALUE_FALSE.equals(attributes.get(HINT_KEY_TXT))) 
      renderImpl(g, bounds, txt, attributes);
    // done
  }
  
  /**
   * Implementation for rendering img
   */
  protected void renderImpl(Graphics2D g, Rectangle bounds, ImageIcon img) {
    
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
  protected void renderImpl(Graphics2D g, Rectangle bounds, String txt, Map<String,String> attributes) {
    
    // check for empty string
    if (txt.length()==0)
      return;
    
    // prepare layout
    TextLayout layout = new TextLayout(txt, g.getFont(), g.getFontRenderContext());
    
    // alignment?
    double x = bounds.getX();
    if ("right".equals(attributes.get("align"))) {
      if (layout.getAdvance()< bounds.getWidth())
        x = bounds.getMaxX() - layout.getAdvance();
    }
    
    // draw it
    layout.draw(g, (float)x, (float)bounds.getY()+layout.getAscent());
  }
  
  /**
   * Whether this renderer wants to paint NULL
   */
  protected boolean isNullRenderer() {
    return false;
  }
  
  /**
   * Place
   */
  /*package*/ static class RenderPlace extends PropertyRenderer {
    
    /** acceptance */
    public boolean accepts(TagPath path, Property prop) {
      return prop instanceof PropertyPlace;
    }

    /** 
     * size override
     */
    public Dimension2D getSizeImpl(Property prop, Map<String,String> attributes, Graphics2D graphics) {
      return super.getSizeImpl(prop, getText(prop, attributes), attributes, graphics);
    }

    /**
     * render override
     */
    public void renderImpl( Graphics2D g, Rectangle bounds, Property prop, Map<String,String> attributes) {
      super.renderImpl(g, bounds, prop, getText(prop, attributes), attributes);
    }
    
    private String getText(Property prop, Map<String,String> attributes) {

      Object j = attributes.get("jurisdiction");
      
      // index?
      if (j!=null) {
        
        // 0 = first available
        if ("0".equals(j))
          return ((PropertyPlace)prop).getFirstAvailableJurisdiction();

        // i>0
        String result = null;
        try {
            result = ((PropertyPlace)prop).getJurisdiction(Integer.parseInt(j.toString()));
        } catch (Throwable t) {
        }
        return result==null ? "" : result;
      }
      
      // all
      return prop.getDisplayValue();
    }
    
  } //Place
  
  /**
   * Sex
   */
  /*package*/ static class RenderSex extends PropertyRenderer {
    
    /** acceptance */
    public boolean accepts(TagPath path, Property prop) {
      return prop instanceof PropertySex;
    }

    /** 
     * size override
     */
    public Dimension2D getSizeImpl(Property prop, Map<String,String> attributes, Graphics2D graphics) {
      patch(attributes);
      return super.getSizeImpl(prop, value(prop, attributes), attributes, graphics);
    }

    /**
     * render override
     */
    public void renderImpl( Graphics2D g, Rectangle bounds, Property prop, Map<String,String> attributes) {
      patch(attributes);
      super.renderImpl(g, bounds, prop, value(prop, attributes) ,attributes);
    }
    
    private String value(Property sex, Map<String,String> attributes) {
      String result = sex.getDisplayValue();
      if (result.length()>0 && HINT_VALUE_TRUE.equals(attributes.get(HINT_KEY_SHORT)))
        result = result.substring(0,1);
      return result;
    }

    private void patch(Map<String,String> attributes) {
      if (!attributes.containsKey(HINT_KEY_TXT))
        attributes.put(HINT_KEY_TXT, HINT_VALUE_FALSE);
      if (!attributes.containsKey(HINT_KEY_IMG))
        attributes.put(HINT_KEY_IMG, HINT_VALUE_TRUE);
    }
  } //Sex

  /**
   * MLE
   */
  /*package*/ static class RenderMLE extends PropertyRenderer {
  
    /** acceptance */
    public boolean accepts(TagPath path, Property prop) {
      return prop instanceof PropertyMultilineValue;
    }

    /**
     * size override
     */
    public Dimension2D getSizeImpl(Property prop, Map<String,String> attributes, Graphics2D graphics) {
      
      //.gotta be multiline
      if (!(prop instanceof MultiLineProperty))
        return super.getSizeImpl(prop, attributes, graphics);
      
      // count 'em
      FontMetrics fm = graphics.getFontMetrics();
      int lines = 0;
      double width = 0;
      double height = 0;
      MultiLineProperty.Iterator line = ((MultiLineProperty)prop).getLineIterator();
      do {
        lines++;
        width = Math.max(width, fm.stringWidth(line.getValue()));
        height += fm.getHeight();
      } while (line.next());
      
      // done
      return new Dimension2d(width, height);
    }
  
    /**
     * render override
     */
    public void renderImpl( Graphics2D g, Rectangle bounds, Property prop, Map<String,String> attributes) {
      
      // gotta be multiline
      if (!(prop instanceof MultiLineProperty)) {
        super.renderImpl(g, bounds, prop, attributes);
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
        
        // .. break if line doesn't fit anymore
        if (y>bounds.getMaxY()) 
          break;
        
      } while (line.next());
      // done
    }
    
  } //MLE

  /**
   * File
   */
  /*package*/ static class RenderFile extends PropertyRenderer {

    /** acceptance */
    public boolean accepts(TagPath path, Property prop) {
      return prop instanceof PropertyFile 
      || prop instanceof PropertyBlob 
      || (path!=null&&path.getLast().equals("FILE"));
    }

    /**
     * size override 
     */
    public Dimension2D getSizeImpl(Property prop, Map<String,String> attributes, Graphics2D graphics) {
      
      // try to resolve image
      ImageIcon img = getImage(prop, attributes);
      if (img==null) 
        return EMPTY_DIM;

      // ask it for size
      return img.getSizeInPoints(EntityRenderer.getDPI(graphics));
        
    }
    
    /**
     * render override
     */
    public void renderImpl(Graphics2D g, Rectangle bounds, Property prop, Map<String,String> attributes) {
      
      // grab the image
      ImageIcon img = getImage(prop, attributes);
      if (img==null) return;
      
      // get unit graphics up
      UnitGraphics ug = new UnitGraphics(g, 1, 1);
      ug.pushTransformation();
      ug.setColor(Color.black);
      ug.translate(bounds.x, bounds.y);
      
      // calculate factor - the image's dpi might be
      // different than that of the rendered surface
      Point dpi = EntityRenderer.getDPI(g);
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
     * Helper to get the image of PropertyFile
     */
    private ImageIcon getImage(Property prop, Map<String,String> attributes) {
      // check file for image
      ImageIcon result = null;
      if (prop instanceof IconValueAvailable) 
        result = ((IconValueAvailable)prop).getValueAsIcon();
      // fallback
      if (result==null&&HINT_VALUE_TRUE.equals(attributes.get(HINT_KEY_IMG))) return broken;
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
  /*package*/ static class RenderEntity extends PropertyRenderer {
  
    /** acceptance */
    public boolean accepts(TagPath path, Property prop) {
      return prop instanceof Entity;
    }

    /**
     * size override
     */
    public Dimension2D getSizeImpl(Property prop, Map<String,String> attributes, Graphics2D graphics) {
      return super.getSizeImpl(prop, ((genj.gedcom.Entity)prop).getId(), attributes, graphics);
    }
  
    /**
     * render override
     */
    public void renderImpl(Graphics2D g, Rectangle bounds, Property prop, Map<String,String> attributes) {
      attributes.put("align", "right");
      super.renderImpl(g, bounds, prop, ((genj.gedcom.Entity)prop).getId(), attributes);
    }
    
  } //Entity

  /**
   * XRef
   */
  /*package*/ static class RenderXRef extends PropertyRenderer {
    
    /** acceptance */
    public boolean accepts(TagPath path, Property prop) {
      return prop instanceof PropertyXRef;
    }

    // 20050416 the same as default - use displayValue
  
  } //XRef
      
  /**
   * name
   */
  /*package*/ static class RenderSecret extends PropertyRenderer {
  
    /** acceptance */
    public boolean accepts(TagPath path, Property prop) {
      return prop!=null && prop.isSecret();
    }

    /**
     * size override
     */
    public Dimension2D getSizeImpl(Property prop, Map<String,String> attributes, Graphics2D graphics) {
      return super.getSizeImpl(prop, STARS, attributes, graphics);
    }
  
    /**
     * render override
     */
    public void renderImpl( Graphics2D g, Rectangle bounds, Property prop, Map<String,String> attributes) {
      super.renderImpl(g, bounds, prop, STARS, attributes);
    }
    
  } //Secret
      
  /**
   * Date
   */
  /*package*/ static class RenderDate extends PropertyRenderer {
    
    /** acceptance */
    public boolean accepts(TagPath path, Property prop) {
      return prop instanceof PropertyDate;
    }

    /**
     * render override - make it right aligned
     */
    public void renderImpl(Graphics2D g, Rectangle bounds, Property prop, Map<String,String> attributes) {
      attributes.put("align", "right");
      super.renderImpl(g, bounds, prop, attributes);
    }
    
  } //Date

} //PropertyProxy
