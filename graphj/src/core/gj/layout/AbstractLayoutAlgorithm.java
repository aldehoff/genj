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
package gj.layout;

import gj.util.EdgeLayoutHelper;

import java.awt.geom.Point2D;

/**
 * Base for Layouts (optional)
 */
public abstract class AbstractLayoutAlgorithm implements LayoutAlgorithm {
  
  /** an arc layout for convenience */
  protected EdgeLayoutHelper arcLayout = new EdgeLayoutHelper();

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String s = getClass().getName();
    return s.substring(s.lastIndexOf('.')+1);
  }
  
  /**
   * Helper to move point by delta
   */
  protected void move(Layout2D layout, Object vertex, double dx, double dy) {
    Point2D pos = layout.getPositionOfVertex(vertex);
    pos.setLocation(pos.getX() + dx, pos.getY() + dy);
    layout.setPositionOfVertex(vertex, pos);
  }

} //AbstractLayout
