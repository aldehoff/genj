package gj.layout.tree;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * An Orientation
 */
public abstract class Orientation {
  
  /** orientations */
  private static final Map orientations = new HashMap();
  
  private static final Orientation
    truetrue   = new truetrue  (),
    falsetrue  = new falsetrue (),
    truefalse  = new truefalse (),
    falsefalse = new falsefalse();
    
  /**
   * Constructor
   */
  private Orientation() {
    orientations.put(getClass().getName(), this);
  }
  
  /**
   * Resolve an orientation
   * @param vertical vertical or horizontal
   * @param topdown top-down or bottom-up
   * @return an orientation
   */
  protected static Orientation get(boolean vertical, boolean topdown) {
    return (Orientation) orientations.get(Orientation.class.getName()+"$"+vertical+topdown);
  }
  
  /**
   * Returns the 2D bounds for a surface with lat/lon's
   */
  protected Rectangle getBounds(Contour c) {

    Point
      p1 = getPoint(c.north, c.west),
      p2 = getPoint(c.south, c.east);
      
    int
      x1 = Math.min(p1.x, p2.x),
      y1 = Math.min(p1.y, p2.y),
      x2 = Math.max(p1.x, p2.x),
      y2 = Math.max(p1.y, p2.y);
      
    return new Rectangle(x1,y1,x2-x1,y2-y1);
  }

  /**
   * Returns the surface with lat/lon's for given 2D bounds
   */
  protected Contour getContour(Rectangle2D r2d) {
    
    Rectangle r = r2d.getBounds(); 
      
    Point 
      p1 = new Point(r.x, r.y),
      p2 = new Point(r.x+r.width, r.y+r.height);
      
    int 
      lon1 = getLongitude(p1),
      lon2 = getLongitude(p2),
      lat1 = getLatitude (p1),
      lat2 = getLatitude (p2);

    int          
      n = Math.min(lat1, lat2),
      w = Math.min(lon1, lon2),
      e = Math.max(lon1, lon2),
      s = Math.max(lat1, lat2);

    return new Contour(n,w,e,s);    
  }

  /**
   * Returns the longitude for a 2D position
   */
  public abstract int getLongitude(Point2D p);

  /**
   * Returns the latitude for a 2D position
   */
  public abstract int getLatitude(Point2D p);

  /**
   * Returns the 2D position for given latitude/longitude
   */
  protected abstract Point getPoint(int lat, int lon);
  
  /**
   * vertical=false, topdown=true
   * 
   *      +-+
   *      | |
   *    +-+ |
   *    |   |
   *  +-+   |
   *  |     |
   *  +-+   |
   *    |   |
   *    +-+ |
   *      | |
   *      +-+
   */  
  private static class falsetrue extends Orientation {
    public int getLongitude(Point2D p) {
      return (int)(-p.getY());
    }
    public int getLatitude(Point2D p) {
      return (int)(p.getX());
    }
    protected Point getPoint(int lat, int lon) {
      return new Point(lat,-lon);
    }
  } //falsetrue

  /**
   * vertical=true, topdown=true
   * 
   *     +-+
   *     | |
   *   +-+ +-+
   *   |     |
   * +-+     +-+
   * |         |
   * +---------+
   */  
  private static class truetrue extends Orientation {
    public int getLongitude(Point2D p) {
      return (int)(p.getX());
    }
    public int getLatitude(Point2D p) {
      return (int)(p.getY());
    }
    protected Point getPoint(int lat, int lon) {
      return new Point(lon,lat);
    }
  } //truetrue

  /**
   * vertical=false, topdown=false
   * 
   *  +-+
   *  | |
   *  | +-+
   *  |   |
   *  |   +-+
   *  |     |
   *  |   +-+
   *  |   |
   *  | +-+
   *  | |
   *  +-+
   */  
  private static class falsefalse extends Orientation {
    public int getLatitude(Point2D p) {
      return (int)(-p.getX());
    }
    public int getLongitude(Point2D p) {
      return (int)(p.getY());
    }
    protected Point getPoint(int lat, int lon) {
      return new Point(-lat,lon);
    }
  } //falsefalse

  /**
   * vertical=true, topdown=false
   * 
   * +---------+
   * |         |
   * +-+     +-+
   *   |     |
   *   +-+ +-+
   *     | |
   *     +-+
   */  
  private static class truefalse extends Orientation {
    public int getLatitude(Point2D p) {
      return (int)(-p.getY());
    }
    public int getLongitude(Point2D p) {
      return (int)(-p.getX());
    }
    protected Point getPoint(int lat, int lon) {
      return new Point(-lon,-lat);
    }
  } //truefalse

} //Orientation

