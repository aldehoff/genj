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
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.util.swing.ImageIcon;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * A renderer of media
 */
public class MediaRenderer extends PropertyRenderer {

  /** an replacement for a 'broken' image */  
  private final static ImageIcon DEFAULT_IMAGE = 
    new ImageIcon(PropertyRenderer.class, "Broken");

  /** 
   * acceptance test - we accept FILE and BLOB here even though we can render
   * an available media under a given root as well - that has to be initiated 
   * explicitly through the PropertyRendererFactory (<prop renderer=media>)
   */
  public boolean accepts(Property root, TagPath path, Property prop) {
    return prop instanceof PropertyFile 
      || prop instanceof PropertyBlob 
      || (path!=null&&path.getLast().equals("FILE"));
  }

  /**
   * size override 
   */
  public Dimension2D getSize(Property root, TagPath path, Property prop, Map<String,String> attributes, Graphics2D graphics) {
    try {
      InputStream in = getIn(path!=null ? prop : root);
      if (in!=null) {
        ImageInputStream iin = ImageIO.createImageInputStream(in);
        Iterator<ImageReader> iter = ImageIO.getImageReaders(iin);
        if (iter.hasNext()) {
          ImageReader reader = iter.next();
          try {
            reader.setInput(iin, false, false);
            return new Dimension(reader.getWidth(0), reader.getHeight(0));
          } finally {
            reader.dispose();
          }
        }
      }
    } catch (IOException ioe) {
      LOG.log(Level.FINER, "Can't get image dimension for "+prop, ioe);
    }

    return DEFAULT_IMAGE.getSizeInPoints(DPI.get(graphics));
  }
  
  private InputStream getIn(Property prop) throws IOException {
    
    // a file?
    if (prop instanceof PropertyFile) {
      File file = ((PropertyFile)prop).getFile();
      if (file.exists())
        return new FileInputStream(file);
      return null;
    }

    // a blob?
    if (prop instanceof PropertyBlob)
      return new ByteArrayInputStream(((PropertyBlob)prop).getBlobData());
    
    // contained OBJE?
    for (int i=0;i<prop.getNoOfProperties(); i++) {
      Property child = prop.getProperty(i);
      
      // OBJE > OBJE?
      if (child instanceof PropertyXRef) {
        Entity e = ((PropertyXRef)child).getTargetEntity();
        if (e instanceof Media) {
          Media m = (Media)e;
          PropertyBlob BLOB = m.getBlob();
          if (BLOB!=null)
            return new ByteArrayInputStream(BLOB.getBlobData());
          PropertyFile FILE = m.getFile();
          if (FILE!=null&&FILE.getFile()!=null&&FILE.getFile().exists())
            return new FileInputStream(FILE.getFile());
        }
      }
      
      // OBJE|FILE?
      if ("OBJE".equals(child.getTag())) {
        Property file = child.getProperty("FILE");
        if (file instanceof PropertyFile) {
          PropertyFile FILE = ((PropertyFile)file);
          if (FILE!=null&&FILE.getFile()!=null&&FILE.getFile().exists())
            return new FileInputStream(FILE.getFile());
        }
      }
      
    }
    
    // nothing found
    return null;
  }
  
  /**
   * render override
   */
  public void render(Graphics2D g, Rectangle bounds, Property root, TagPath path, Property prop, Map<String,String> attributes) {
    InputStream in = null;
    try {
      in = getIn(path!=null ? prop : root);
      if (in!=null) {
        ImageInputStream iin = ImageIO.createImageInputStream(in);
        Iterator<ImageReader> iter = ImageIO.getImageReaders(iin);
        if (iter.hasNext()) {
          ImageReader reader = iter.next();
          try {
            reader.setInput(iin, false, false);
            int w = reader.getWidth(0);
            int h = reader.getHeight(0);
            ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceSubsampling(Math.max(1, (int) Math.floor(w/bounds.width)), Math.max(1, (int) Math.floor(h/bounds.height)), 0, 0);
            Image img = reader.read(0, param);
            g.drawImage(img,
                bounds.x,bounds.y,bounds.x+bounds.width,bounds.y+bounds.height,
                0,0,img.getWidth(null),img.getHeight(null),
                null
                );
          } finally {
            reader.dispose();
          }
          return;
        }
      }
    } catch (IOException ioe) {
      LOG.log(Level.FINER, "Can't render image for "+prop, ioe);
    } finally {
      if (in!=null) try { in.close(); } catch (IOException e) {}
    }
    
    g.drawImage(DEFAULT_IMAGE.getImage(),
      bounds.x,bounds.y,bounds.x+bounds.width,bounds.y+bounds.height,
      0,0,DEFAULT_IMAGE.getIconWidth(),DEFAULT_IMAGE.getIconHeight(),
      null
    );
        
    // done
  }

}