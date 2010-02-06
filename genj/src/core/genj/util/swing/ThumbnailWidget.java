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
package genj.util.swing;

import genj.io.InputSource;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadUpdateListener;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Timer;

/**
 * A widget for showing image thumbnails
 */
public class ThumbnailWidget extends JComponent {
  
  public final static ImageIcon IMG = new ImageIcon(ThumbnailWidget.class, "File.png");
  private final static Logger LOG = Logger.getLogger("genj.util.swing");
  private final static BlockingQueue<Runnable> executorQueue = new LinkedBlockingDeque<Runnable>();
  private final static Executor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, executorQueue);

  private int thumbSize = 64, thumbPadding = 10;
  private Insets thumbBorder = new Insets(4,4,32,4);
  private List<Thumbnail> thumbs = new ArrayList<Thumbnail>();
  private Callback callback = new Callback();
  private Timer repaint = new Timer(100, new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      repaint();
    }
  });
  private Thumbnail selection = null;
  private boolean pendingCenter = false;

  /**
   * Constructor
   */
  public ThumbnailWidget() {
    addMouseWheelListener(callback);
    addMouseListener(callback);
  }
  
  /**
   * Clear all sources
   */
  public void clear() {
    setSources(null);
  }
  
  public void setSource(InputSource source) {
    if (source==null)
      clear();
    else
      setSources(Collections.singletonList(source));
  }
  
  /**
   * Set sources to show
   */
  public void setSources(List<InputSource> sources) {

    // unselect/let folks know about selection
    Thumbnail oldSelection = selection;
    if (oldSelection!=null) {
      selection = null;
      firePropertyChange("selection", oldSelection.getSource(), null);
    }    

    // keep
    int oldSize = thumbs.size();
    thumbs.clear();
    if (sources!=null) for (InputSource source : sources)
      thumbs.add(new Thumbnail(source));
    
    // show
    revalidate();
    repaint();
    
    // signal
    firePropertyChange("thumbnails", oldSize, thumbs.size());
  }
  
  /**
   * callbacks
   */
  private class Callback extends MouseAdapter implements MouseWheelListener {
    public void mouseWheelMoved(MouseWheelEvent e) {
      if (e.isControlDown()) {
        // zoom
        thumbSize = Math.max(64, thumbSize -= e.getWheelRotation() * 32);
        revalidate();
        repaint();
      } else {
        // scroll
        if (getParent() instanceof JViewport) {
          JViewport port = (JViewport)getParent();
          Point v = port.getViewPosition();
          v.y = Math.min(Math.max(0, v.y+port.getSize().height*e.getWheelRotation()), Math.max(0,getHeight()-port.getHeight())); 
          port.setViewPosition(v);
        }
        return;
      }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
      // change selection
      Thumbnail old = selection;
      Thumbnail thumb = getThumb(e.getPoint());
      // none or already selected?
      if (thumb==null||old==thumb)
        return;
      // select and end
      selection = thumb;
      repaint();
      firePropertyChange("selection", old!=null ? old.getSource() : null, selection.getSource());
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
      // selection?
      if (selection==null)
        return;
      // double-click?
      if (e.getClickCount()!=2)
        return;
      // check thumbs
      Thumbnail thumb = getThumb(e.getPoint());
      if (selection==thumb&&selection.size.width>0&&selection.size.height>0&&getParent() instanceof JViewport) {
        final JViewport port = (JViewport)getParent();
        Dimension size = fit(selection.size, port.getSize());
        thumbSize = Math.max(size.width, size.height);
        pendingCenter = true;
      } else {
        thumbSize = 32;
      }
      revalidate();
      repaint();
      // done
    }
  }
  
  private Thumbnail getThumb(Point pos) {
    for (Thumbnail thumb : thumbs) {
      if (thumb.renderDest.contains(pos)) {
        return thumb;
      }
    }
    return null;
  }

  @Override
  public Dimension getPreferredSize() {
    int cols, rows;
    if (thumbs.isEmpty()) {
      cols = 0;
      rows = 0;
    } else {
      cols = (int) Math.ceil(Math.sqrt(thumbs.size()));
      rows = (int) Math.ceil(thumbs.size() / (float) cols);
    }
    return new Dimension(
      cols*(thumbSize+thumbBorder.left+thumbBorder.right+thumbPadding), 
      rows*(thumbSize+thumbBorder.top+thumbBorder.bottom+thumbPadding)
    );
  }

  @Override
  public void paint(Graphics g) {

    Graphics2D g2d = (Graphics2D) g;

    Dimension d = getSize();
    g.setColor(Color.LIGHT_GRAY);
    g.fillRect(0, 0, d.width, d.height);

    int cols = (int) Math.ceil(Math.sqrt(thumbs.size()));
    int rows = (int) Math.ceil(thumbs.size() / (float) cols);

    int x = (d.width - cols * (thumbSize+thumbBorder.left+thumbBorder.right+thumbPadding)) / 2;
    int y = (d.height- rows * (thumbSize+thumbBorder.top+thumbBorder.bottom+thumbPadding)) / 2;

    int row = 0, col = 0;
    for (Thumbnail thumb : thumbs) {
      
      Point p = new Point(
        x + col*(thumbSize+thumbBorder.left+thumbBorder.right+thumbPadding) + thumbPadding/2, 
        y + row*(thumbSize+thumbBorder.top+thumbBorder.bottom+thumbPadding) + thumbPadding/2
      );

      // outline and text
      g.setColor(Color.DARK_GRAY);
      g2d.fill(new Rectangle(p.x+thumbBorder.left + thumbSize + thumbBorder.right, p.y+2, 2, thumbBorder.top  + thumbSize + thumbBorder.bottom));
      g2d.fill(new Rectangle(p.x+2, p.y+thumbBorder.top + thumbSize + thumbBorder.bottom, thumbBorder.left + thumbSize + thumbBorder.right, 2));
      g.setColor(Color.WHITE);
      g2d.fill(new Rectangle(p.x, p.y, thumbBorder.left + thumbSize + thumbBorder.right, thumbBorder.top  + thumbSize + thumbBorder.bottom));
      g.setColor(Color.BLACK);
      GraphicsHelper.render(g2d, thumb.getName(), new Rectangle(
          p.x, p.y + thumbBorder.top+thumbSize, thumbBorder.left+thumbSize+thumbBorder.right, thumbBorder.bottom),
          0.5,0.5
      );
      
      // selection
      if (thumb==selection) {
        g.setColor(Color.BLUE);
        g2d.draw(new Rectangle(p.x, p.y, thumbBorder.left + thumbSize + thumbBorder.right, thumbBorder.top  + thumbSize + thumbBorder.bottom));
      }

      // content
      Rectangle content = new Rectangle(p.x + thumbBorder.left, p.y + thumbBorder.top, thumbSize,thumbSize);
      if (!thumb.render(g2d, content)) {
        // schedule for update
        Validation v = new Validation(thumb);
        if (!executorQueue.contains(v))
          executor.execute(v);
      }

      // next
      col = (++col) % cols;
      if (col == 0)
        row++;
    }
    
    // a pending center?
    if (pendingCenter && selection!=null && getParent() instanceof JViewport) {
      pendingCenter = false;
      JViewport port = (JViewport)getParent();
      Point center = new Point(
          (int)(Math.max(0, selection.renderDest.getCenterX() - port.getSize().width/2)),
          (int)(Math.max(0, selection.renderDest.getCenterY() - port.getSize().height/2))
        );
      port.setViewPosition(center);
    }

  } // paint
  
  class Validation implements Runnable {
    
    private Thumbnail thumb;

    Validation(Thumbnail thumb) {
      this.thumb = thumb;
    }
    
    @Override
    public void run() {
      thumb.validate();
    }
    
    @Override
    public boolean equals(Object obj) {
      return obj instanceof Validation ? ((Validation)obj).thumb == thumb : false;
    }
    
    @Override
    public int hashCode() {
      return thumb.hashCode();
    }
  }
  
  private static Rectangle grow(Rectangle r, int by) {
    return new Rectangle(r.x-by/2, r.y-by/2, r.width+by, r.height+by);
  }

  private static Rectangle center(Rectangle a, Rectangle b) {
    return new Rectangle(b.x + (b.width - a.width) / 2, b.y + (b.height - a.height) / 2, a.width, a.height);
  }

  private static Dimension fit(Dimension a, Dimension b) {
    float scale = Math.min(b.width / (float) a.width, b.height / (float) a.height);
    return new Dimension((int) (a.width * scale), (int) (a.height * scale));
  }

  /**
   * A thumbnail tracking image
   */
  private class Thumbnail implements IIOReadUpdateListener {

    private InputSource source;
    private SoftReference<Image> image = new SoftReference<Image>(null);
    private Dimension size = new Dimension();
    private Dimension imageSize = new Dimension();
    private Rectangle imageView = new Rectangle();
    private Rectangle renderDest = new Rectangle();
    private Rectangle renderSource = new Rectangle();

    public Thumbnail(InputSource source) {
      this.source = source;
    }
    
    String getName() {
      return source.getName();
    }
    
    InputSource getSource() {
      return source;
    }

    private synchronized boolean isValid() {
      // image at all?
      if (image.get() == null)
        return false;
      // bad size
      if ( (imageSize.width<size.width&&imageSize.width<renderDest.width) 
          || (imageSize.height<size.height&&imageSize.height<renderDest.height))
        return false;
      // bad view?
      // TODO
      return true;
    }

    synchronized boolean render(Graphics2D g, Rectangle bounds) {

      // we request as much as we can get
      renderDest.setBounds(bounds);

      // clip saving us?
      if (!g.getClipBounds().intersects(renderDest))
        return true;

      // if we know how big the picture is we can figure out how much to view
      if (size.width <= 0 || size.height <= 0)
        return false;

      // now we can ask for a specific available view
      renderSource.setBounds(0, 0, size.width, size.width);

      // something to render?
      Image img = image.get();
      if (img==null || imageSize.width == 0 || imageSize.height == 0)
        return false;

      // sanitize what's renderer
      bounds = center(new Rectangle(fit(size, bounds.getSize())), bounds);

      // render what we have
      Shape clip = g.getClip();
      g.clip(bounds);
      draw(img, g, bounds.x, bounds.y, bounds.width, bounds.height, 0, 0, imageSize.width, imageSize.height);
      g.setClip(clip);
      
      // done
      return isValid();
    }
    
    void draw(Image img, Graphics2D g, float sx, float sy, float sw, float sh, float dx, float dy, float dw, float dh) {
      g.drawImage(img, (int) sx, (int) sy, (int) (sx + sw), (int) (sy + sh), (int) dx, (int) dy, (int) (dx + dw), (int) (dy + dh), null);
    }

    void validate() {

      if (isValid()) 
        return;

      LOG.finer("Loading " + source.getName());

      // load it
      InputStream in = null;
      ImageReader reader = null;
      try {

        in = source.open();
        ImageInputStream iin = ImageIO.createImageInputStream(in);

        Iterator<ImageReader> iter = ImageIO.getImageReaders(iin);
        if (!iter.hasNext())
          throw new IOException("no suiteable image reader for " + source.getName());

        reader = (ImageReader) iter.next();
        reader.setInput(iin, false, false);
        reader.addIIOReadUpdateListener(this);

        ImageReadParam param = reader.getDefaultReadParam();
        synchronized (this) {
          size.setSize(reader.getWidth(0), reader.getHeight(0));

          // anything requested?
          if (renderSource.width == 0 || renderSource.height == 0)
            return;

          // match requested size
          if (param.canSetSourceRenderSize()) {
            param.setSourceRenderSize(fit(size, renderSource.getSize()));
          } else {
            param.setSourceSubsampling(Math.max(1, (int) Math.floor(size.width / renderDest.width)), Math.max(1, (int) Math.floor(size.height / renderDest.height)), 0, 0);
          }
        }

        Image img = reader.read(0, param);
        
        synchronized (this) {
          image = new SoftReference<Image>(img);
          imageSize.setSize(img.getWidth(null), img.getHeight(null));
        }

      } catch (Throwable t) {
        if (LOG.isLoggable(Level.FINER))
          LOG.log(Level.FINER, "Loading " + source.getName() + " failed", t);
        else
          LOG.log(Level.FINE, "Loading " + source.getName() + " failed");

        // setup fallback
        synchronized (this) {
          image = new SoftReference<Image>(IMG.getImage());
          size.setSize(IMG.getIconWidth(), IMG.getIconHeight());
          imageSize.setSize(size);
          imageView.setBounds(0, 0, size.width, size.height);
        }

      } finally {
        repaint.stop();
        try {
          reader.dispose();
        } catch (Throwable t) {
        }
        try {
          in.close();
        } catch (Throwable t) {
        }

        repaint();
      }

      // done
    }

    public synchronized void imageUpdate(ImageReader source, BufferedImage img, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {
      if (image.get()==null) synchronized (this) {
        image = new SoftReference<Image>(img);
        imageSize.setSize(img.getWidth(null), img.getHeight(null));
        repaint.start();
      }
    }

    public void passComplete(ImageReader source, BufferedImage theImage) {
    }

    public void passStarted(ImageReader source, BufferedImage theImage, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {
    }

    public void thumbnailPassComplete(ImageReader source, BufferedImage theThumbnail) {
    }

    public void thumbnailPassStarted(ImageReader source, BufferedImage theThumbnail, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {
    }

    public void thumbnailUpdate(ImageReader source, BufferedImage theThumbnail, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {
    }

  } // Thumbnail

}
