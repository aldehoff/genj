package genj.util.swing;

import gj.ui.UnitGraphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * A component that lets the user adjust the resolution
 * by dragging a ruler */
public class ScreenResolutionScale extends JComponent {
  
  /** current dots per cm */
  private Point2D.Double dpc = new Point2D.Double( 
    0.393701D * Toolkit.getDefaultToolkit().getScreenResolution(),
    0.393701D * Toolkit.getDefaultToolkit().getScreenResolution()
  );

  /**
   * Constructor   */
  public ScreenResolutionScale() {
    addMouseMotionListener(new MouseGlue());
  }

  /**
   * Set dotspercentimeters
   */
  public void setDotsPerCm(Point2D set) {
    dpc.x = set.getX();
    dpc.y = set.getY();
    repaint();
  }
  
  /**
   * Get dotspercentimeters   */
  public Point2D getDotsPerCm() {
    return new Point2D.Double(dpc.x, dpc.y);
  }
  
  /**
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  protected void paintComponent(Graphics g) {
    
    // clear background
    g.setColor(Color.lightGray);
    g.fillRect(0,0,getWidth(),getHeight());
    
    // draw ticks   
    UnitGraphics ug = new UnitGraphics(g, dpc.getX(), dpc.getY());
    Rectangle2D clip = ug.getClip();
    FontMetrics fm = g.getFontMetrics(); 
    int fd = fm.getHeight() - fm.getDescent();

    g.setColor(getForeground());
    for (int x=1;x<clip.getMaxX(); x++) {
      // segment
      ug.draw(x,0,x,0.3);
      ug.draw(""+x, x, 0, 0, 2, fd);
      // next
    }

    for (int y=1;y<clip.getMaxY(); y++) {
      // segment
      ug.draw(0,y,0.3,y);
      ug.draw(""+y, 0.3, y, 1.0, 0, fd);
      // next
    }
    
    // draw label
//    String s = "Click and drag until scale matches real world!";
//    g.drawString(s, getWidth()/2 - fm.stringWidth(s)/2, getHeight()/2);

    // done
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension((int)(10D*dpc.x),(int)(10D*dpc.y));
  }
  
  /**
   * @see javax.swing.JComponent#getMinimumSize()
   */
  public Dimension getMinimumSize() {
    return new Dimension(64,64);
  }


  /**
   * Glue for mouse events   */
  private class MouseGlue extends MouseMotionAdapter {
    
    /** the start position of a drag */
    private Point2D.Double startPos = new Point2D.Double();
    
    /** the start dotsPcms of a drag */
    private Point2D.Double startDPC = new Point2D.Double();
    
    /**
     * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {
      startPos.x = e.getPoint().x;
      startPos.y = e.getPoint().y;
      startDPC.x = dpc.x;
      startDPC.y = dpc.y;
    }

    /**
     * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
      Point p = e.getPoint();
      dpc.setLocation(
        Math.max(10, startDPC.x * (p.x/startPos.x) ),     
        Math.max(10, startDPC.y * (p.y/startPos.y) )
      );     
      repaint();
    }
  } //MouseGlue
} //ResolutionRuler
