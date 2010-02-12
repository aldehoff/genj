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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * A renderer of media - it can find suitable media information to render, offers quick size access and best offer
 * scaling results with caching
 */
public class MediaRenderer {

  private final static Logger LOG = Logger.getLogger("genj.renderer");
  
  /**
   * size override 
   */
  public static Dimension2D getSize(Property root, Graphics graphics) {
    try {
      InputStream in = getIn(root);
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
      LOG.log(Level.FINER, "Can't get image dimension for "+root, ioe);
    }

    return new Dimension();
  }
  
  private static InputStream getIn(Property prop) throws IOException {
    
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
  public static void render(Graphics g, Rectangle bounds, Property root) {
    InputStream in = null;
    try {
      in = getIn(root);
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
      LOG.log(Level.FINER, "Can't render image for "+root, ioe);
    } finally {
      if (in!=null) try { in.close(); } catch (IOException e) {}
    }
    
    // done
  }

}