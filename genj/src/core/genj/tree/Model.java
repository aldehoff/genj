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

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import gj.awt.geom.Path;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.util.ModelHelper;

/**
 * Model of our tree
 */
public class Model implements Graph {

  /** arcs */
  private Collection arcs = new ArrayList(100);

  /** nodes */
  private Collection nodes = new ArrayList(100);

  /** bounds */
  private Rectangle2D bounds;  

  /**
   * Constructor
   */
  public Model(Indi root) {
    // parse the tree
    new IndiNode(root);
    // update bounds
    bounds = ModelHelper.getBounds(nodes);
    // done
  }
  
  /**
   * @see gj.model.Graph#getArcs()
   */
  public Collection getArcs() {
    return arcs;
  }

  /**
   * @see gj.model.Graph#getBounds()
   */
  public Rectangle2D getBounds() {
    return bounds;
  }

  /**
   * @see gj.model.Graph#getNodes()
   */
  public Collection getNodes() {
    return nodes;
  }

  /**
   * A node for an entity
   */
  /*package*/ abstract class EntityNode implements Node {

    /** the entity */
    private Entity entity;
    
    /** arcs of this entity */
    private List arcs = new ArrayList(5);
    
    /** position of this entity */
    private Point2D pos = new Point2D.Double();
    
    /** shape */
    private Rectangle2D.Double shape = new Rectangle2D.Double(-1.5D,-1D,3D,2D);

    /**
     * Constructor
     */
    private EntityNode(Entity etity) {
      // remember
      entity = etity;
      // publish
      nodes.add(this);
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
      return entity;
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

  } //EntityNode
  
  /**
   * A node for an individual
   */
  /*package*/ class IndiNode extends EntityNode {
    /**
     * Constructor
     */
    private IndiNode(Indi indi) {
      // delegate
      super(indi);
      // grab the children
      Indi[] children = indi.getChildren();
      for (int c=0; c<children.length; c++) {
        Indi child = children[c];
        new Indi2Indi(this, new IndiNode(child));
      }
      // done
    }
  } //IndiNode

  /**
   * An arc between two individuals
   */
  /*package*/ class Indi2Indi implements Arc {
    /**
     * Constructor
     */
    private Indi2Indi(EntityNode n1, EntityNode n2) {
    }
    /**
     * @see gj.model.Arc#getEnd()
     */
    public Node getEnd() {
      return null;
    }
    /**
     * @see gj.model.Arc#getStart()
     */
    public Node getStart() {
      return null;
    }
    /**
     * @see gj.model.Arc#getPath()
     */
    public Path getPath() {
      return null;
    }
  } //Indi2Indi

} //Model
