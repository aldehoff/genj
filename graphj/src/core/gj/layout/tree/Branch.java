package gj.layout.tree;
import gj.awt.geom.Geometry;
import gj.awt.geom.Path;
import gj.model.Node;
import gj.util.ArcIterator;
import gj.util.ModelHelper;

import java.awt.geom.Point2D;
import java.util.List;
/**
 * The layouted branch of a tree 
 * <il>
 *  <li>all nodes for descendants of root have a position relative to ancestor (delta) 
 *  <li>all arcs in the branch have a position relative to root (delta) 
 * </il>
 */
public class Branch {
  
  /** the root */
  private Node root;
  
  /** the contour */
  private Contour contour;
  
  /**
   * Constructor
   */
  /*package*/ Branch(Node root, Node parent, Contour contour) {
    
    // remember
    this.root = root;
    this.contour = contour;
    
    // place arcs/children relative to current node
    Point2D delta = Geometry.getNegative(root.getPosition());
    ArcIterator it = new ArcIterator(root);
    while (it.next()) {
      // don't follow back
      if (it.dest==parent) continue;
      // update the arc
      Path path = it.arc.getPath();
      if (path!=null) path.translate(delta);
      // don't go twice or loop
      if (!it.isFirst||it.isLoop) continue;
      // update the node
      ModelHelper.move(it.dest, delta);
    }

    // done      
  }
  
  /**
   * The longitude (west-east centered at origin) 
   */
  public int getLongitude(Orientation o) {
    return o.getLongitude(root.getPosition());
  }
  
  /**
   * The latitude of the branch (northest point)
   */
  public int getLatitude() {
    return contour.north;
  }
  
  /**
   * westmost longitude
   */
  public static int getMinLongitude(Branch[] bs) {
    int result = Integer.MAX_VALUE;
    for (int i=0;i<bs.length;i++)
      result = Math.min(bs[i].contour.west, result);
    return result;
  }
  
  /**
   * eastmost longitude
   */
  public static int getMaxLongitude(Branch[] bs) {
    int result = Integer.MIN_VALUE;
    for (int i=0;i<bs.length;i++)
      result = Math.max(bs[i].contour.east, result);
    return result;
  }

  /**
   * longitude between branches
   */  
  public static int getLongitude(Branch[] bs, double fraction, Orientation o) {
    // calculate center point
    double 
      min = bs[          0].getLongitude(o),
      max = bs[bs.length-1].getLongitude(o);
    return (int)(min + (max-min) * Math.min(1D, Math.max(0D, fraction)));
  }  
  
  /**
   * Places all nodes in the branch at absolute positions
   */
  /*package*/ Contour finalize(int dlat, int dlon, Orientation o) {
    finalizeRecursively(root, null, o.getPoint(dlat,dlon));
    contour.translate(dlat, dlon);
    return contour;
  }
  
  /**
   * Places all nodes under node at absolute positions 
   */
  private void finalizeRecursively(Node node, Node parent, Point2D delta) {

    // change the node's position
    ModelHelper.move(node, delta);

    // propagate via arcs
    ArcIterator it = new ArcIterator(node);
    while (it.next()) {
      // .. only down the tree
      if (it.dest==parent) continue;
      // .. tell the arc's path
      Path path = it.arc.getPath();
      if (path!=null) path.translate(node.getPosition());
      // .. never loop'd
      if (it.isLoop) continue;
      // .. 1st only
      if (!it.isFirst) continue;
      // .. recursion
      finalizeRecursively(it.dest, node, node.getPosition());
    }

    // done
  }
   
  /**
   * Move this branch
   */
  /*package*/ void moveBy(int dlat, int dlon, Orientation o) {
    contour.translate(dlat, dlon);
    ModelHelper.move(root, o.getPoint(dlat, dlon));
  }

  /**
   * Move this branch
   */
  /*package*/ void moveTo(int lat, int lon, Orientation o) {
    Point2D pos = root.getPosition();
    moveBy( lat - o.getLatitude(pos), lon - o.getLongitude(pos), o);
  }

  /**
   * Inserts this branch beside (east) and top-align of others
   */
  /*package*/ void insertEastOf(List others, Orientation o) {
    
    // no placing to do?
    if (!others.isEmpty()) {
      
      // top-align to first 
      moveBy(((Branch)others.get(0)).contour.north - this.contour.north, 0, o);
        
      // and then as close as possible to other (east of)
      moveBy(0, -calcMinimumDistance(others, this), o);
      
    }
      
    // insert
    others.add(this);
    
  }

  /**
   * Calculates the minimum distance of list of branches and other
   * @param branches the list of branches
   * @param other branch
   */
  private static int calcMinimumDistance(List branches, Branch other) {

    // all min distances
    int[] ds = calcMinimumDistances(branches, other);
    
    // find minimum distance
    int result = Integer.MAX_VALUE;
    for (int d=0; d<ds.length; d++) result = Math.min(ds[d], result); 
      
    // done
    return result;
  }

  /**
   * Calculates the distances of a list of branches and other
   * @param branches the list of branches
   * @param other branch
   */
  private static int[] calcMinimumDistances(List branches, Branch other) {
    
    // create a result
    int[] result = new int[branches.size()];
    for (int r=0;r<result.length;r++) result[r] = Integer.MAX_VALUE;

    // we'll iterate west-side of other -> the east
    Contour.Iterator east = other.contour.getIterator(Contour.WEST);
      
    // loop through from east to west
    loop: for (int r=result.length-1;r>=0;r--) {
      
      // the east-side of branch r -> west
      Contour.Iterator west = ((Branch)branches.get(r)).contour.getIterator(Contour.EAST);
      
      // calculate distance
      while (true) {
        
        // skip west segments north of east
        while (west.south<=east.north) if (!west.next()) continue loop;
        // skip east segments north of west
        while (east.south<=west.north) if (!east.next()) break loop;

        // calc distance of segments
        result[r] = Math.min( result[r], east.longitude-west.longitude);

        // skip northern segment
        if (west.south<east.south) {
          if (!west.next()) continue loop;
        } else {
          if (!east.next()) break loop;
        }
      
        // continue with next segment
      }      
      
      // continue further west
    }
    
    // done
    return result;
  }

  /**
   * Get contour-list for merging
   */  
  /*package*/ static Contour[] getCountoursForMerge(Contour parent, Branch[] children) {
    Contour[] result = new Contour[children.length+2];
    int i=0;
    result[i++] = parent;
    for (int c=0; c<children.length; c++) result[i++] = children[c].contour;        
    result[i++] = parent;
    return result;
  }

} //Branch