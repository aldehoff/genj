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
import gj.layout.PathHelper;
import gj.model.Arc;
import gj.model.Node;
import gj.util.ArcIterator;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 * The ArcLayout
 */
/*package*/ abstract class ArcLayout {
  
  /**
   * Resolve an orientation
   * @param bended whether we bend arcs 
   * @return an ArcLayout
   */
  /*package*/ static ArcLayout get(boolean bended) {
    if (bended) return new BendedArcLayout();
    else return new DefaultArcLayout();
  }
  
  
  /**
   * apply the layout
   */
  /*package*/ final void applyTo(Node root, Arc backtrack, double equator, Orientation orientation) {
    // Loop through arcs to children (without backtrack)
    ArcIterator it = new ArcIterator(root);
    while (it.next()) {
      if (!it.isDup(backtrack)&&it.arc.getPath()!=null) layout(root, it, equator, orientation);
    }
    // done      
  }
  
  /**
   * layout one arc in the iterator we're working on
   */
  /*package*/ abstract void layout(Node root, ArcIterator it, double equator, Orientation orientation);


  /**
   * Default (direct-line) ArcLayout
   */
  private static class DefaultArcLayout extends ArcLayout {
    
    /**
     * layout one arc in the iterator we're working on
     */
    /*package*/ void layout(Node root, ArcIterator it, double equator, Orientation orientation) {
    
      // grab nodes and their position/shape
      Node
        n1 = it.arc.getStart(),
        n2 = it.arc.getEnd();
      Point2D 
        p1 = n1.getPosition(),
        p2 = n2.getPosition();
      Shape 
        s1 = n1.getShape(),
        s2 = n2.getShape();

      // calculate south of p1 and north of p2
      p1 = PathHelper.calculateProjection(
        p1, orientation.getPoint2D(orientation.getLatitude(p2), orientation.getLongitude(p1)),
        p1, s1
      );
      p2 = PathHelper.calculateProjection(
        p2, orientation.getPoint2D(orientation.getLatitude(p1), orientation.getLongitude(p2)),
        p2, s2
      );

      // strike a path
      Path path = it.arc.getPath();
      path.reset();
      path.moveTo(p1);
      path.lineTo(p2);
      
      // done  
    }
  
  } //DefaultArcLayout
  
  /**
   * Bended ArcLayout
   */
  private static class BendedArcLayout extends DefaultArcLayout {
    
    /**
     * layout one arc in the iterator we're working on
     */
    /*package*/ void layout(Node root, ArcIterator it, double equator, Orientation orientation) {
      
      // we don't do anything special with loops
      if (it.isLoop) {
        super.layout(root,it,equator,orientation);
        return;
      }
  
      Node
        start = it.arc.getStart(),
        end   = it.arc.getEnd(),
        other = ModelHelper.getOther(it.arc,root);
      
      // bend the arc
      Point2D
        p1 = root.getPosition(),
        p2 = new Point2D.Double(),
        p3 = new Point2D.Double(),
        p4 = other.getPosition();
        
      if (p1.getX()==p4.getX()||p1.getY()==p4.getY()) {
        super.layout(root,it,equator,orientation);
        return;
      }
  
      if (Double.isNaN(equator)) {
        p2.setLocation(orientation.getPoint2D(orientation.getLatitude(p1), orientation.getLongitude(p4)));
        p3=p2;        
      } else {
        p2.setLocation(orientation.getPoint2D(equator, orientation.getLongitude(p1)));
        p3.setLocation(orientation.getPoint2D(equator, orientation.getLongitude(p4)));
      }
  
      PathHelper.update(
        it.arc.getPath(),
        root==start ? new Point2D[]{p1,p2,p3,p4} : new Point2D[]{p4,p3,p2,p1},
        start.getShape(),
        end.getShape()
      );
      
      // done
    }
    
  } //BendedArcLayout

}