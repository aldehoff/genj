/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.tree;

import gj.layout.tree.NodeOptions;
import gj.layout.tree.Orientation;
import gj.model.Node;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A node in our genealogy tree */
/*package*/ class TreeNode implements Node, NodeOptions {
  
  /** the content */
  /*package*/ Object content;
  
  /** arcs of this entity */
  /*package*/ List arcs = new ArrayList(5);
  
  /** position of this entity */
  /*package*/ Point2D pos = new Point2D.Double();
  
  /** the shape */
  /*package*/ Shape shape;
  
  /** padding */
  /*package*/ double[] padding;
  
  /**
   * Constructor
   */
  /*package*/ TreeNode(Object cOntent, Shape sHape, double[] padDing) {
    // remember
    content = cOntent;
    shape = sHape;
    padding = padDing;
    // done
  }
  
  /**
   * @see gj.model.Node#getArcs()
   */
  public List getArcs() {
    return arcs;
  }

  /**
   * @see gj.model.Node#getContent()
   */
  public Object getContent() {
    return content;
  }

  /**
   * @see gj.model.Node#getPosition()
   */
  public Point2D getPosition() {
    return pos;
  }

  /**
   * @see gj.model.Node#getShape()
   */
  public Shape getShape() {
    return shape;
  }
  
  /**
   * @see gj.layout.tree.NodeOptions#getLatitude(Node, double, double)
   */
  public double getLatitude(Node node, double min, double max, Orientation o) {
    // default is centered
    return min + (max-min) * 0.5;
  }
  
  /**
   * @see gj.layout.tree.NodeOptions#getLongitude(Node, double, double, double, double)
   */
  public double getLongitude(Node node, double minc, double maxc, double mint, double maxt, Orientation o) {
    // default is centered
    return minc + (maxc-minc) * 0.5;
  }
  
  /**
   * @see gj.layout.tree.NodeOptions#getPadding(int)
   */
  public double getPadding(Node node, int dir, Orientation o) {
    if (padding==null) return 0;
    return padding[dir];
  }
  
} //TreeNode

