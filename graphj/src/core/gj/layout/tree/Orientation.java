package gj.layout.tree;

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
  /*package*/ static Orientation get(boolean vertical, boolean topdown) {
    return (Orientation) orientations.get(Orientation.class.getName()+"$"+vertical+topdown);
  }
  
  /**
   * Returns the 2D bounds for a surface with lat/lon's
   */
  /*package*/ abstract Rectangle2D.Double getBounds(Contour c);

  /**
   * Returns the longitude for a 2D position
   */
  public abstract double getLongitude(Point2D p);

  /**
   * Returns the latitude for a 2D position
   */
  public abstract double getLatitude(Point2D p);

  /**
   * Returns the surface with lat/lon's for given 2D bounds
   */
  /*package*/ abstract Contour getContour(Rectangle2D r, double[] pad);

  /**
   * Returns the 2D position for given latitude/longitude
   */
  public abstract Point2D.Double getPoint2D(double lat, double lon);
  
  /**
   * Returns a rotated orientation
   */
  /*package*/ abstract Orientation rotate(boolean clockwise);
  
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
    /*package*/ Rectangle2D.Double getBounds(Contour c) {
      return new Rectangle2D.Double(c.north, -c.east, c.south-c.north, c.east-c.west);
    }
    public double getLongitude(Point2D p) {
      return -p.getY();
    }
    public double getLatitude(Point2D p) {
      return p.getX();
    }
    /*package*/ Contour getContour(Rectangle2D r, double[] pad) {
      return new Contour(
          r.getMinX() - pad[0],
         -r.getMaxY() - pad[1], 
         -r.getMinY() + pad[2], 
          r.getMaxX() + pad[3]
      );
    }
    public Point2D.Double getPoint2D(double lat, double lon) {
      return new Point2D.Double(lat,-lon);
    }
    /*package*/ Orientation rotate(boolean clockwise) {
      return clockwise ? truetrue : truefalse;
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
    /*package*/ Rectangle2D.Double getBounds(Contour c) {
      return new Rectangle2D.Double(c.west, c.north, c.east-c.west, c.south-c.north);
    }
    public double getLongitude(Point2D p) {
      return p.getX();
    }
    public double getLatitude(Point2D p) {
      return p.getY();
    }
    /*package*/ Contour getContour(Rectangle2D r, double[] pad) {
      return new Contour(
        r.getMinY() - pad[0], 
        r.getMinX() - pad[1], 
        r.getMaxX() + pad[2], 
        r.getMaxY() + pad[3] 
      );
    }
    public Point2D.Double getPoint2D(double lat, double lon) {
      return new Point2D.Double(lon,lat);
    }
    /*package*/ Orientation rotate(boolean clockwise) {
      return clockwise ? falsefalse : falsetrue;
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
    /*package*/ Rectangle2D.Double getBounds(Contour c) {
      return new Rectangle2D.Double(-c.south, c.west, c.south-c.north, c.east-c.west);
    }
    public double getLatitude(Point2D p) {
      return -p.getX();
    }
    public double getLongitude(Point2D p) {
      return p.getY();
    }
    /*package*/ Contour getContour(Rectangle2D r, double[] pad) {
      return new Contour(
        -r.getMaxX() - pad[0], 
         r.getMinY() - pad[1], 
         r.getMaxY() + pad[2], 
        -r.getMinX() + pad[3]
      );
    }
    public Point2D.Double getPoint2D(double lat, double lon) {
      return new Point2D.Double(-lat,lon);
    }
    /*package*/ Orientation rotate(boolean clockwise) {
      return clockwise ? truefalse : truetrue;
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
    /*package*/ Rectangle2D.Double getBounds(Contour c) {
      return new Rectangle2D.Double(-c.east, -c.south, c.east-c.west, c.south-c.north);
    }
    /*package*/ Contour getContour(Rectangle2D r, double[] pad) {
      return new Contour(
        -r.getMaxY() - pad[0], 
        -r.getMaxX() - pad[1], 
        -r.getMinX() + pad[2], 
        -r.getMinY() + pad[3]
      );
    }
    public double getLatitude(Point2D p) {
      return -p.getY();
    }
    public double getLongitude(Point2D p) {
      return -p.getX();
    }
    public Point2D.Double getPoint2D(double lat, double lon) {
      return new Point2D.Double(-lon,-lat);
    }
    /*package*/ Orientation rotate(boolean clockwise) {
      return clockwise ? falsetrue : falsefalse;
    }
  } //truefalse

} //Orientation

