package gj.layout.tree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * An Orientation
 */
/*package*/ abstract class Orientation {
  
  /** orientations */
  private static final Map orientations = new HashMap();
  
  static {
    new truetrue  ();
    new falsetrue ();
    new truefalse ();
    new falsefalse();
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
   * Constructor
   */
  /*package*/ Orientation() {
    orientations.put(getClass().getName(), this);
  }
  
  /**
   * Returns the 2D bounds for a surface with lat/lon's
   */
  /*package*/ abstract Rectangle2D.Double getBounds(Contour c);

  /**
   * Returns the longitude for a 2D position
   */
  /*package*/ abstract double getLongitude(Point2D p);

  /**
   * Returns the latitude for a 2D position
   */
  /*package*/ abstract double getLatitude(Point2D p);

  /**
   * Returns the surface with lat/lon's for given 2D bounds
   */
  /*package*/ abstract Contour getContour(Rectangle2D r);

  /**
   * Returns the 2D position for given latitude/longitude
   */
  /*package*/ abstract Point2D.Double getPoint2D(double lat, double lon);
  
  /**
   * Rotates the orientation
   */
  /*package*/ abstract void rotate(TreeLayout layout, boolean clockwise);
  
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
    /*package*/ double getLongitude(Point2D p) {
      return -p.getY();
    }
    /*package*/ double getLatitude(Point2D p) {
      return p.getX();
    }
    /*package*/ Contour getContour(Rectangle2D r) {
      return new Contour(r.getMinX(), -r.getMaxY(), -r.getMinY(), r.getMaxX());
    }
    /*package*/ Point2D.Double getPoint2D(double lat, double lon) {
      return new Point2D.Double(lat,-lon);
    }
    /*package*/ void rotate(TreeLayout layout, boolean clockwise) {
      layout.isVertical = true;
      layout.isTopDown = clockwise ? true : false;
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
    /*package*/ double getLongitude(Point2D p) {
      return p.getX();
    }
    /*package*/ double getLatitude(Point2D p) {
      return p.getY();
    }
    /*package*/ Contour getContour(Rectangle2D r) {
      return new Contour(r.getMinY(), r.getMinX(), r.getMaxX(), r.getMaxY());
    }
    /*package*/ Point2D.Double getPoint2D(double lat, double lon) {
      return new Point2D.Double(lon,lat);
    }
    /*package*/ void rotate(TreeLayout layout, boolean clockwise) {
      layout.isVertical = false;
      layout.isTopDown = clockwise ? false : true;
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
    /*package*/ double getLatitude(Point2D p) {
      return -p.getX();
    }
    /*package*/ double getLongitude(Point2D p) {
      return p.getY();
    }
    /*package*/ Contour getContour(Rectangle2D r) {
      return new Contour(-r.getMaxX(), r.getMinY(), r.getMaxY(), -r.getMinX());
    }
    /*package*/ Point2D.Double getPoint2D(double lat, double lon) {
      return new Point2D.Double(-lat,lon);
    }
    /*package*/ void rotate(TreeLayout layout, boolean clockwise) {
      layout.isVertical = true;
      layout.isTopDown = clockwise ? false : true;
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
    /*package*/ Contour getContour(Rectangle2D r) {
      return new Contour(-r.getMaxY(), -r.getMaxX(), -r.getMinX(), -r.getMinY());
    }
    /*package*/ double getLatitude(Point2D p) {
      return -p.getY();
    }
    /*package*/ double getLongitude(Point2D p) {
      return -p.getX();
    }
    /*package*/ Point2D.Double getPoint2D(double lat, double lon) {
      return new Point2D.Double(-lon,-lat);
    }
    /*package*/ void rotate(TreeLayout layout, boolean clockwise) {
      layout.isVertical = false;
      layout.isTopDown = clockwise ? true : false;
    }
  } //truefalse

} //Orientation

