/**
 * GraphJ
 *
 * Copyright (C) 2002 Nils Meier
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package gj.layout.tree;

import gj.awt.geom.Path;
import gj.layout.ArcLayout;
import gj.model.Arc;
import gj.model.Node;
import gj.util.ArcIterator;

import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 * The ArcLayout for trees
 */
/*package*/ abstract class TreeArcLayout extends ArcLayout {
  
  /**
   * Resolve an instance
   * @param bended whether we bend arcs 
   * @return an ArcLayout
   */
  /*package*/ static TreeArcLayout get(boolean bended) {
    if (bended) return new Bended();
    else return new Straight();
  }
    
  /**
   * apply the layout
   */
  /*package*/ final void layout(Node node, double equator, Orientation orientation, ArcOptions arcop) {
    // Loop through arcs to children (without backtrack)
    ArcIterator it = new ArcIterator(node);
    while (it.next()) {
      // no path no interest
      if (it.arc.getPath()==null) continue;
      // handle loops separate from specialized
      if (it.isLoop) layout(it.arc);
      else layout(it.arc, equator, orientation, arcop);
    }
    // done      
  }
  
  /**
   * layout one arc in the iterator we're working on
   */
  protected abstract void layout(Arc arc, double equator, Orientation orientation, ArcOptions arcop);


  /**
   * Default (direct-line) ArcLayout
   */
  private static class Straight extends TreeArcLayout {
    
    /**
     * layout one arc in the iterator we're working on
     */
    protected void layout(Arc arc, double equator, Orientation o, ArcOptions arcop) {
      
      // grab nodes and their position/shape
      Node
        n1 = arc.getStart(),
        n2 = arc.getEnd  ();
      Point2D 
        p1 = arcop.getPort(arc, n1, o),
        p2 = arcop.getPort(arc, n2, o);
      Shape 
        s1 = n1.getShape(),
        s2 = n2.getShape();

      // calculate south of p1 and north of p2
      p1 = getIntersection(
        p1, o.getPoint2D(o.getLatitude(p2), o.getLongitude(p1)),
        p1, s1
      );
      p2 = getIntersection(
        p2, o.getPoint2D(o.getLatitude(p1), o.getLongitude(p2)),
        p2, s2
      );

      // strike a path
      Path path = arc.getPath();
      path.reset();
      path.moveTo(p1);
      path.lineTo(p2);
      
      // done  
    }
  
  } //DefaultArcLayout
  
  /**
   * Bended ArcLayout
   */
  private static class Bended extends Straight {
    
    /**
     * layout one arc in the iterator we're working on
     */
    protected void layout(Arc arc, double equator, Orientation o, ArcOptions arcop) {
      
      // grab arc's information
      Node
        n1 = arc.getStart(),
        n2 = arc.getEnd();
      
      Point2D
        p1 = arcop.getPort(arc, n1, o),
        p2 = new Point2D.Double(),
        p3 = new Point2D.Double(),
        p4 = arcop.getPort(arc, n2, o);

      // straight line up?
      if (o.getLongitude(p1)==o.getLongitude(p4)) {
        super.layout(arc);
        return;
      }        

      // bending around equator
      if (equator==o.getLatitude(p1)) {
        p2.setLocation(o.getPoint2D(equator, o.getLongitude(p4)));
        p3=p2;
      } else if (equator==o.getLatitude(p4)) {
        p2.setLocation(o.getPoint2D(equator, o.getLongitude(p1)));
        p3=p2;
      } else {
        p2.setLocation(o.getPoint2D(equator, o.getLongitude(p1)));
        p3.setLocation(o.getPoint2D(equator, o.getLongitude(p4)));
      }

      // layout       
      super.layout(
        arc.getPath(),
        new Point2D[]{p1,p2,p3,p4},
        n1.getShape(),
        n2.getShape()
      );
      
      // done
    }
    
  } //BendedArcLayout

}