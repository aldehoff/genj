/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2002-2004 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.geom;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import junit.framework.TestCase;

/**
 * Test Geometry type
 */
public class GeometryTest extends TestCase {
  
  private double TwoPi = Math.PI*2;
  
  public void testRadian() {
    
    tst(radian(  0), Geometry.getRadian(p(0,-1000)));
    tst(radian( 45), Geometry.getRadian(p( 7, -7 )));
    tst(radian( 90), Geometry.getRadian(p(100,0  )));
    tst(radian(135), Geometry.getRadian(p( 99,99 )));
    tst(radian(180), Geometry.getRadian(p(0,10   )));
    tst(radian(225), Geometry.getRadian(p(-2,2   )));
    tst(radian(270), Geometry.getRadian(p(-1,0   )));
    tst(radian(315), Geometry.getRadian(p(-1,-1   )));
    
  }

  public void testLineIntersection() {
    
    // | |
    tst( null, Geometry.getLineIntersection(p(0,0), p(0,1), p(1,0), p(1,1)));
    
    //tst( null, Geometry.getIntersection(p(0,0), radian(180), p(1,0), radian(180)));

    // X
    tst( p(0.5,0.5), Geometry.getLineIntersection(p(0,0), p(1,1), p(0,1), p(1,0)));
    tst( p(0.5,0.5), Geometry.getIntersection(p(0,0), radian(135), p(0,1), radian(225)));

    // +
    tst( p(0,0), Geometry.getLineIntersection(p(-1,0), p(1,0), p(0,-1), p(0,1)));
    
    // /
    //  \
    tst( p(0.5,0.5), Geometry.getLineIntersection(p(1,0), p(0,1), p(1,1), p(2,2)));

    // | -
    tst( p(0,0.5), Geometry.getLineIntersection(p(0,0), p(0,1), p(2,0.5), p(3,0.5)));
    
    // 
  }
  
  /**
   * Test maximum of a shape calculations
   */
  public void testShapeMaximum() {
    
    // -----
    //  xxx
    //  x x
    //  xxx
    tst(-0.5, Geometry.getMax( r(0,0), radian(0)).getY() );
    
    //  xxx
    //  x x
    //  xxx
    // -----
    tst(0.5, Geometry.getMax( r(0,0), radian(180)).getY() );
    
    //  xxx|
    //  x x|
    //  xxx|
    tst(0.5, Geometry.getMax( r(0,0), radian(90)).getX() );
    
    //  xxx
    //  x x
    //  xxx/
    //    /
    assertEquals(new Point2D.Double(.5,.5), Geometry.getMax( r(0,0), radian(90+45)));
    
    
    //    x  |
    //   x x |
    //  x   x|
    //   x x |
    //    x  |
  }
  
  /**
   * Test shape distance calculations
   */
  public void testShapeShapeDistance() {
    
    // xxx   
    // xoxo
    // xxxo
    //  ooo
    tst( -0.5, dist(r(0,0), r(0.5,0), radian(90)));

    // ******
    //-*-**-*-
    // ****** 
    tst( 0, dist(r(0,0), r(1,0), radian(90))); 
    
    // *** ***
    //-*-*-*-*-
    // *** *** 
    tst( 1, dist(r(0,0), r(2,0), radian(90)));
    
    //  |
    // ***
    // *|* 
    // ***    
    // ***
    // *|*    
    // ***
    //  |
    tst( 0, dist(r(0,0), r(0,1), radian(180))); 

    //  |
    // ***
    // *|* 
    // ***    
    //  |
    // ***
    // *|*    
    // ***    
    //  |
    tst( 1, dist(r(0,0), r(0,2), radian(180)));
    
    // ***
    //-*-*-
    // ***    
    //
    // ***
    //-*-*-   
    // ***    
    tst( Double.POSITIVE_INFINITY, dist(r(0,0), r(0,2), radian(90)) );
    
    // ***
    //-*-*-***-
    // *** * *
    //     ***
    tst( 4, dist(r(0,0), r(5,0.5), radian(90)));
    
    // ***
    // * *    
    // ***    
    //    ***
    //    * *
    //    ***
    tst( 0, dist(r(0,0), r(1,1), radian(135)));
    
    // ***
    // *\*    
    // *** 
    //    \
    //     ***
    //     *\*
    //     ***
    tst( hypotenuse(4,4), dist(r(0,0), r(5,5), radian(135)));

    // ooo
    //-oxxx-
    // oxox
    //  xxx
    tst( -0.5, dist(r(0,0), r(0.5,0.5), radian(90)));
    
    //
    //  *   *
    //-*-*-*-*-
    //  *   *
    GeneralPath s1 = new GeneralPath(r(0,0));
    s1.transform(AffineTransform.getRotateInstance(radian(45)));
    GeneralPath s2 = new GeneralPath(s1);
    s2.transform(AffineTransform.getTranslateInstance(3,0));
    tst(3-hypotenuse(1,1), dist(s1,s2,radian(90)));
    
    //     ***
    // *** * *
    // * * ***
    // * **
    //  * *
    //  ***
    s1 = new GeneralPath(r(0,0)); 
    s1.append(r(0.5,0.5), true);
    s2 = new GeneralPath(r(3,-0.5));
    tst(1.5, dist(s1,s2,radian(90)));
    s2.transform(AffineTransform.getTranslateInstance(0,-0.1));
    tst(2, dist(s1,s2,radian(90)));
    
    // done
  }
  
  /**
   * equals test
   */
  private void tst(Point2D a, Point2D b) {
    if (a==b)
      return;
    if (a==null) {
      assertNull("expected null but got "+b,b);
      return;
    }
    if (b==null) {
      assertNull("expected null but got "+a,a);
      return;
    }
      
    assertEquals(a.getX(),b.getX(),0.0000001);
    assertEquals(a.getY(),b.getY(),0.0000001);
  }
  
  /**
   * equals test
   */
  private void tst(double a, double b) {
    assertEquals(a,b,0.0000001);
  }
  
  /**
   * calculate radian from degree
   */
  private double radian(double degree) {
    return TwoPi/360*degree;
  }
  
  /**
   * calculate the distance of two shapes
   */
  private double dist(Shape s1, Shape s2, double angle) {
    return Geometry.getDistance(s1,s2,angle);
  }
  
  /** 
   * calculate the hypotenuse for given a and b
   * @return sqrt(a*a+b*b)
   */
  private double hypotenuse(double a, double b) {
    return Math.sqrt(a*a + b*b);
  }

  /**
   * create a rectangle centered around (x,y) with size (1,1)
   */
  private Rectangle2D r(double x, double y) {
    return new Rectangle2D.Double(x-0.5,y-0.5,1,1);
  }
  
  /**
   * create a rectangle centered around (x,y) with size (w,h)
   */
  private Rectangle2D r(double x, double y, double w, double h) {
    return new Rectangle2D.Double(x-w/2,y-h/2,w,h);
  }
  
  private Point2D p(double x, double y) {
    return new Point2D.Double(x,y);
  }
  
}
