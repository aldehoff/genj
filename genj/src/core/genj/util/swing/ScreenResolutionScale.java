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
import java.text.NumberFormat;

import javax.swing.JComponent;

/**
 * A component that lets the user adjust the resolution
 * by dragging a ruler */
public class ScreenResolutionScale extends JComponent {
  
  /** global dots per cm */
  private static Point2D.Double globalDpc = new Point2D.Double( 
    0.393701D * Toolkit.getDefaultToolkit().getScreenResolution(),
    0.393701D * Toolkit.getDefaultToolkit().getScreenResolution()
  );

  /** current dots per cm */
  private Point2D.Double dpc = new Point2D.Double( 
    globalDpc.x,
    globalDpc.y
  );

  /**
   * Constructor   */
  public ScreenResolutionScale() {
    addMouseMotionListener(new MouseGlue());
  }

  /**
   * Set dotspercentimeters
   */
  public static void setDotsPerCm(Point2D set) {
    globalDpc.x = set.getX();
    globalDpc.y = set.getY();
  }
  
  /**
   * Get dotspercentimeters   */
  public static Point2D getDotsPerCm() {
    return new Point2D.Double(globalDpc.x, globalDpc.y);
  }
  
  /**
   * Apply current settings to global   */
  public void apply() {
    setDotsPerCm(dpc);
  }
  
  /**
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  protected void paintComponent(Graphics g) {
    
    // clear background
    g.setColor(Color.white);
    g.fillRect(0,0,getWidth(),getHeight());
    g.setColor(Color.black);
    g.drawRect(0,0,getWidth()-1,getHeight()-1);
    
    // draw ticks   
    //g.setColor(Color.gray);
    g.setPaintMode();
    UnitGraphics ug = new UnitGraphics(g, dpc.getX(), dpc.getY());
    Rectangle2D clip = ug.getClip();
    FontMetrics fm = g.getFontMetrics(); 
    int
      fh = fm.getHeight(),
      fd = fh - fm.getDescent();

    for (int X=1;X<clip.getMaxX(); X++) {
      // segment
      ug.setColor(Color.gray);
      for (double x=0.1; x<0.9; x+=0.1)
        ug.draw(X-x,0,X-x,0.1,0,1);
      ug.setColor(Color.black);
      ug.draw(X,0,X,0.4,0,1);
      ug.draw(""+X, X, 1, 0, 2, 0);
      // next
    }

    for (int Y=1;Y<clip.getMaxY(); Y++) {
      // segment
      ug.setColor(Color.gray);
      for (double y=0.1; y<0.9; y+=0.1)
        ug.draw(0,Y-y,0.1,Y-y,1,0);
      ug.setColor(Color.black);
      ug.draw(0,Y,0.4,Y,0,1);
      ug.draw(""+Y, 1, Y, 1.0, 0, fd);
      // next
    }

    // draw label
    g.setColor(Color.black);
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(2);
    String[] txt = new String[]{
      ""+nf.format(dpc.getX()),
      "by",
      ""+nf.format(dpc.getY()),
      "pixels/cm"
    };
    for (int i = 0; i < txt.length; i++) {
      g.drawString(
        txt[i], 
        getWidth()/2 - fm.stringWidth(txt[i])/2, 
        getHeight()/2 - txt.length*fh/2 + i*fh + fh
      );
    }

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
