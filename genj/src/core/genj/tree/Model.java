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
import java.util.Iterator;
import java.util.List;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import gj.awt.geom.Path;
import gj.layout.LayoutException;
import gj.layout.tree.NodeOptions;
import gj.layout.tree.TreeLayout;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.util.ModelHelper;

/**
 * Model of our tree
 */
public class Model implements Graph {

  /** listeners */
  private List listeners = new ArrayList(3);

  /** arcs */
  private Collection arcs = new ArrayList(100);

  /** nodes */
  private Collection nodes = new ArrayList(100);

  /** bounds */
  private Rectangle2D bounds = ModelHelper.getBounds(nodes);

  /** shapes */
  private Rectangle2D.Double 
    shapeIndi = new Rectangle2D.Double(-1.5D,-1.0D,3.0D,2.0D),
    shapeFam  = new Rectangle2D.Double(-1.5D,-0.5D,3.0D,1.0D),
    shapeFoo  = new Rectangle2D.Double(-0.5D,-1.0D,1.0D,2.0D);

  /**
   * Constructor
   */
  public Model() {
  }
  
  /**
   * Sets the root
   */
  public void setRoot(Entity root) {
    // clear old
    arcs.clear();
    nodes.clear();
    bounds.setFrame(0,0,0,0);
    // parse the tree
    Node node = parse(root);
    // layout
    try {
      TreeLayout tlayout = new TreeLayout();
      tlayout.setRoot(node);
      tlayout.setNodeOptions(new MyOptions());
      tlayout.setBendArcs(true);
      tlayout.applyTo(this);
    } catch (LayoutException e) {
      e.printStackTrace();
    }
    // notify
    fireStructureChanged();
    // done
  }
  
  /**
   * Add listener
   */
  public void addListener(ModelListener l) {
    listeners.add(l);
  }
  
  /**
   * Remove listener
   */
  public void removeListener(ModelListener l) {
    listeners.remove(l);
  }
  
  /**
   * Fire even
   */
  private void fireStructureChanged() {
    for (int l=listeners.size()-1; l>=0; l--) {
      ((ModelListener)listeners.get(l)).structureChanged(this);
    }
  }
  
  /**
   * builds the tree
   */
  private Node parse(Entity entity) {
    if (entity instanceof Indi) return parse((Indi)entity);
    if (entity instanceof Fam ) return parse((Fam )entity);
    throw new IllegalArgumentException("Indi and Fam only");
  }
  
  /**
   * builds the tree for an individual
   */
  private Node parse(Indi indi) {
    return new IndiNode(indi);
  }
  
  /**
   * builds the tree for a family
   */
  private Node parse(Fam fam) {
    return new FamNode(fam);
  }
  
  /**
   * An entity by position
   */
  public Entity getEntity(double x, double y) {
    // loop nodes
    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      MyNode node = (MyNode)it.next();
      Point2D pos = node.getPosition();
      Shape shape = node.getShape();
      if (shape!=null&&shape.getBounds().contains(x-pos.getX(),y-pos.getY())) 
        return node.entity;
    }
    // nothing found
    return null;
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
  private abstract class MyNode implements Node {
    
    /** the entity */
    private Entity entity;
    
    /** arcs of this entity */
    private List arcs = new ArrayList(5);
    
    /** position of this entity */
    private Point2D pos = new Point2D.Double();
    
    /**
     * Constructor
     */
    private MyNode(Entity etity) {
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
    public abstract Shape getShape();
    
    /**
     * Resolve Padding
     */
    protected abstract double getPadding(int side);
    
  } //MyNode
  
  /**
   * A node for an individual
   */
  private class IndiNode extends MyNode {
    /** whether there are partners of the indi */
    private boolean hasPartners = false;
    /**
     * Constructor
     */
    private IndiNode(Indi indi) {
      // delegate
      super(indi);
      // done
    }
    /**
     * Constructor
     */
    private IndiNode(Indi indi, MyNode famc) {
      // delegate
      super(indi);
      // arc from famc to me
      new MyArc(famc, this, true);       
      // loop through our fams
      Fam[] fams = indi.getFamilies();
      for (int f=0; f<fams.length; f++) {
        // our family
        Fam fam = fams[f];
        if (fam.getNoOfSpouses()==1) {
          // below us
          new MyArc(this, new FamNode(fam), true);
        } else {
          // mark partners
          hasPartners = true;
          // beside us
          new MyArc(famc, new MarrNode(fam), false);
          new MyArc(famc, new IndiNode(fam.getOtherSpouse(indi)), false);
        }
        // next family
      }
      // done
    }
    /**
     * @see gj.model.Node#getShape()
     */
    public Shape getShape() {
      return shapeIndi;
    }
    /**
     * @see genj.tree.Model.MyNode#getPadding(int)
     */
    protected double getPadding(int side) {
      if (side==MyOptions.EAST) return hasPartners ? 0.0D : 0.3D;
      return 0.1D;
    }
  } //MyINode
  
  /**
   * A node for a family
   */
  private class FamNode extends MyNode {
    /**
     * Constructor
     */
    private FamNode(Fam fam) {
      // delegate
      super(fam);
      // grab the children
      Indi[] children = fam.getChildren();
      for (int c=0; c<children.length; c++) {
        // here's the child
        Indi child = children[c];
        // add an arc to that
        new IndiNode(child, this);
        // next child
      }
      // done
    }
    /**
     * @see genj.tree.Model.MyNode#getShape()
     */
    public Shape getShape() {
      return shapeFam;
    }
    /**
     * @see genj.tree.Model.MyNode#getPadding(int)
     */
    protected double getPadding(int side) {
      if (side==MyOptions.NORTH) return 0;
      return 0.1;
    }
  } //MyFNode

  /**
   * A dummy node
   */
  private class MarrNode extends MyNode {
    /**
     * Constructor
     */
    private MarrNode(Fam fam) {
      // delegate
      super(null);
      // add node for fam below
      new MyArc(this, new FamNode(fam), false);
    }
    /**
     * @see genj.tree.Model.MyNode#getShape()
     */
    public Shape getShape() {
      return null;
    }
    /**
     * @see genj.tree.Model.MyNode#getPadding(int)
     */
    protected double getPadding(int side) {
      if (side==MyOptions.NORTH||side==MyOptions.SOUTH)
        return shapeIndi.getHeight()/2+0.1;
      return 0.0;
    }

  } //DummyNode
  
  /**
   * An arc between two individuals
   */
  private class MyArc implements Arc {
    /** start */
    private MyNode start; 
    /** end */
    private MyNode end; 
    /** path */
    private Path path;
    /**
     * Constructor
     */
    private MyArc(MyNode n1, MyNode n2, boolean p) {
      // remember
      start = n1;
      end   = n2;
      if (p) path = new Path();
      // register
      n1.arcs.add(this);
      n2.arcs.add(this);
      arcs.add(this);
      // done  
    }
    /**
     * @see gj.model.Arc#getEnd()
     */
    public Node getEnd() {
      return end;
    }
    /**
     * @see gj.model.Arc#getStart()
     */
    public Node getStart() {
      return start;
    }
    /**
     * @see gj.model.Arc#getPath()
     */
    public Path getPath() {
      return path;
    }
  } //Indi2Indi

  /**
   * Customs NodeOptions
   */
  private class MyOptions implements NodeOptions {
    /** our node */
    private MyNode mynode;
    /**
     * @see gj.layout.tree.NodeOptions#set(Node)
     */
    public void set(Node node) {
      mynode = (MyNode)node;
    }
    /**
     * @see gj.layout.tree.NodeOptions#getAlignment(int)
     */
    public double getAlignment(int dir) {
      return 0.5;
    }
    /**
     * @see gj.layout.tree.NodeOptions#getPadding(int)
     */
    public double getPadding(int dir) {
      return mynode.getPadding(dir);
    }
  } //MyNodeOptions
  
} //Model
