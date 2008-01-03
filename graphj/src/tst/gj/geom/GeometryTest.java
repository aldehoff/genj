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
import java.awt.geom.Rectangle2D;

import junit.framework.TestCase;

/**
 * Test Geometry type
 */
public class GeometryTest extends TestCase {
  
  private double TwoPi = Math.PI*2;

  /**
   * Test shape distance calculations
   */
  public void testShapeShapeDistance() {
    
    // xxx   
    // xoxo
    // xxxo
    //  ooo
    tst( -0.5, dist(r(0,0), r(0.5,0), deg(90)));

    // ******
    //-*-**-*-
    // ****** 
    tst( 0, dist(r(0,0), r(1,0), deg(90)));
    
    // *** ***
    //-*-*-*-*-
    // *** *** 
    tst( 1, dist(r(0,0), r(2,0), deg(90)));
    
    //  |
    // ***
    // *|* 
    // ***    
    // ***
    // *|*    
    // ***
    //  |
    tst( 0, dist(r(0,0), r(0,1), deg(180))); 

    //  |
    // ***
    // *|* 
    // ***    
    //  |
    // ***
    // *|*    
    // ***    
    //  |
    tst( 1, dist(r(0,0), r(0,2), deg(180)));
    
    // ***
    //-*-*-
    // ***    
    //
    // ***
    //-*-*-   
    // ***    
    tst( Double.POSITIVE_INFINITY, dist(r(0,0), r(0,2), deg(90)) );
    
    // ***
    //-*-*-***-
    // *** * *
    //     ***
    tst( 4, dist(r(0,0), r(5,0.5), deg(90)));
    
    // ***
    // * *    
    // ***    
    //    ***
    //    * *
    //    ***
    tst( 0, dist(r(0,0), r(1,1), deg(135)));
    
    // ***
    // *\*    
    // *** 
    //    \
    //     ***
    //     *\*
    //     ***
    tst( hypotenuse(4,4), dist(r(0,0), r(5,5), deg(135)));

    // ooo
    //-oxxx-
    // oxox
    //  xxx
    tst( -0.5, dist(r(0,0), r(0.5,0.5), deg(90)));
    
    //
    //  *   *
    //-*-*-*-*-
    //  *   *
    GeneralPath s1 = new GeneralPath(r(0,0));
    s1.transform(AffineTransform.getRotateInstance(deg(45)));
    GeneralPath s2 = new GeneralPath(s1);
    s2.transform(AffineTransform.getTranslateInstance(3,0));
    tst(3-hypotenuse(1,1), dist(s1,s2,deg(90)));
    
    //     ***
    // *** * *
    // * * ***
    // * **
    //  * *
    //  ***
    s1 = new GeneralPath(r(0,0)); 
    s1.append(r(0.5,0.5), true);
    s2 = new GeneralPath(r(3,-0.5));
    tst(1.5, dist(s1,s2,deg(90)));
    s2.transform(AffineTransform.getTranslateInstance(0,-0.1));
    tst(2, dist(s1,s2,deg(90)));
    
    // done
  }
  
  /**
   * equals test
   */
  private void tst(double a, double b) {
    assertEquals(a,b,0.0000001);
  }
  
  /**
   * calculate bogenmass from degree
   */
  private double deg(double degree) {
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
  
}
