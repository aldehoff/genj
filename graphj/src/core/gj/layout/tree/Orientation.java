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
   * Gets a complement for given NodeLayout
   */
  /*package*/ abstract NodeLayout getComplement(NodeLayout nlayout);
  
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
    /*package*/ NodeLayout getComplement(NodeLayout nlayout) {
      NodeLayout result = nlayout.getClone();
      result.orientn = result.oorientn==this ? truefalse : truetrue;
      return result;
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
    /*package*/ NodeLayout getComplement(NodeLayout nlayout) {
      NodeLayout result = nlayout.getClone();
      result.orientn = result.oorientn==this ? falsetrue : falsefalse;
      return result;
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
    /*package*/ NodeLayout getComplement(NodeLayout nlayout) {
      NodeLayout result = nlayout.getClone();
      result.orientn = result.oorientn==this ? truetrue : truefalse;
      return result;
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
    /*package*/ NodeLayout getComplement(NodeLayout nlayout) {
      NodeLayout result = nlayout.getClone();
      result.orientn = result.oorientn==this ? falsefalse : falsetrue;
      return result;
    }
  } //truefalse

  
//  /**
//   * PatchedNodeOptions
//   */
//  private class PatchedNodeOptions implements NodeOptions {
//    /** the orignal node options */
//    private NodeOptions original;
//    /**
//     * Constructor
//     */
//    private PatchedNodeOptions(NodeOptions originl) {
//      original = originl;
//    }
//    /**
//     * @see gj.layout.tree.NodeOptions#set(Node)
//     */
//    public void set(Node node) {
//      original.set(node);
//    }
//    /**
//     * @see gj.layout.tree.TreeLayout.DefaultNodeOptions#getAlignment(int)
//     */
//    public double getAlignment(int dir) {
//      if (dir==LON) return isComplement ? 1.0D : 0;
//      return original.getAlignment(dir);
//    }
//    /**
//     * @see gj.layout.tree.NodeOptions#getPadding(int)
//     */
//    public double getPadding(int dir) {
//      switch (dir) {
//        case NORTH: default:
//          return original.getPadding(EAST);
//        case WEST:
//          return original.getPadding(NORTH);
//        case EAST:
//          return original.getPadding(SOUTH);
//        case SOUTH:
//          return original.getPadding(WEST);
//      }
//    }
//  } //ComplementNodeOptions

} //Orientation

