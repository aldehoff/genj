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
package genj.util;

import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Class that represents an improved image that doesn't depend
 * on Swing
 */
public class ImgIcon {

  private Object           swingicon=null;
  private Image            image=null;
  private Dimension        size;
  private static Hashtable zoomed = new Hashtable();

  private final static Component component = new TextField();
  private final static MediaTracker tracker = new MediaTracker(component);
  private final static int CLUSTER = 1024*4;

  /**
   * Constructor #4
   */
  public ImgIcon(Image image) {

    // Remember image
    this.image = image;

    // Calculate dimension
    size = calcDimension(image);

    // Done
  }

  /**
   * Constructor #3
   */
  public ImgIcon(Object from, String name) {

    // Get InputStream
    InputStream in = from.getClass().getResourceAsStream(name);
    if (in==null) 
      throw new RuntimeException("Couldn't read image from resources "+from.getClass().getName()+"/"+name);

    // Load it
    createImageFrom(in);

    // Done
  }

  /**
   * Constructor #2
   */
  public ImgIcon(InputStream in) throws IOException {

    // Load it
    createImageFrom(in);

    // Done
  }

  /**
   * Constructor #1
   */
  public ImgIcon(URL url) throws IOException {

    // Load it
    createImageFrom(url.openStream());

    // Done
  }

  /**
   * Calculates dimension of image (eventually waiting)
   */
  private static Dimension calcDimension(Image image) {

    // Wait till ready
    synchronized (tracker) {
      tracker.addImage(image,0);

      try {
        tracker.waitForID(0,5000);
      } catch (InterruptedException ex) {
        Debug.log(Debug.WARNING, ImgIcon.class, "Interrupted while loading image");
        return new Dimension(0,0);
      } finally {
        tracker.removeImage(image,0);
      }
    }

    // Calc width/height for result
    // 03.02.2000 2nd parameter should be getHeight and not getWidth :)
    return new Dimension(image.getWidth(null),image.getHeight(null));

  }

  /**
   * Helper which actually loads the image
   */
  private void createImageFrom(InputStream in) {

    // First we read that stuff completely
    byte[] imgData;

    try {
      imgData = new ByteArray(in).getBytes();
    } catch (IOException ex) {
      // we ignore the fact
      return;
    }

    // Create image
    image = Toolkit.getDefaultToolkit().createImage(imgData,0,imgData.length);

    // Calculate dimension
    size = calcDimension(image);

    // Done
    imgData=null;
  }

  /**
   * Returns image's height
   */
  public int getIconHeight() {
    return size.height;
  }

  /**
   * Returns image's width
   */
  public int getIconWidth() {
    return size.width;
  }

  /**
   * Returns this image
   */
  public Image getImage() {
    return image;
  }

  /**
   * Paints the image
   */
  public void paintIcon(Component c,Graphics g, int x, int y) {
    g.drawImage(image,x,y,c);
  }

  /**
   * Paints the image
   */
  public void paintIcon(Component c,Graphics g, int x, int y, double zoom) {

    // No real zoom ?
    if (zoom==1.0) {
      paintIcon(c,g,x,y);
      return;
    }

    // Calc w & h
    int w = (int)(size.width*zoom),
        h = (int)(size.height*zoom);

    // Known Image ?
    String key = image+"/"+w+"x"+h;
    Object o = zoomed.get(key);

    Image i;
    if (o!=null) {
      i = (Image)o;
    } else {

      // Create Image
      i = image.getScaledInstance( w, h, Image.SCALE_DEFAULT);
      calcDimension(i);

      // Remember
      zoomed.put(key,i);
    }

    // Paint
    g.drawImage(i,x,y,null);

    // Done
  }

  /**
   * Paints the image
   */
  public void paintIcon(Graphics g, int x, int y) {
    g.drawImage(image,x,y,null);
  }

  /**
   * Paints the image
   */
  public void paintIcon(Graphics g, int x, int y, double zoom) {
    paintIcon(null,g,x,y,zoom);
  }
  
  /**
   * Untyped setter for cached SwingIcon
   */
  public void setSwingIcon(Object o) {
    swingicon = o;
  }
  
  /** 
   * Untyped getter for cached SwingIcon
   */
  public Object getSwingIcon() {
    return swingicon;
  }
}
