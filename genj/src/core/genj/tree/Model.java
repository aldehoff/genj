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
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyFamilySpouse;
import genj.gedcom.PropertyHusband;
import genj.gedcom.PropertyWife;
import gj.awt.geom.Path;
import gj.layout.Layout;
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
public class Model implements Graph, GedcomListener {
  
  /** listeners */
  private List listeners = new ArrayList(3);

  /** arcs */
  private Collection arcs = new ArrayList(100);

  /** nodes */
  private Collection nodes = new ArrayList(100);

  /** bounds */
  private Rectangle2D bounds = new Rectangle2D.Double();
  
  /** the layout we use */
  private TreeLayout layout = new TreeLayout();
    
  /** parameters */
  private double 
    padIndis    = 1.0D,
    //padFams     = 1.0D,
    widthIndis  = 3.0D,
    widthFams   = 2.8D,
    widthMarrs  = widthIndis/16,
    heightIndis = 2.0D,
    heightFams  = 1.0D,
    heightMarrs = heightIndis/16;

  /** shape of marriage rings */
  private Shape 
    shapeMarr = calcMarriageRings();

  /** shapes */
  private Rectangle2D.Double 
    shapeIndi = new Rectangle2D.Double(-widthIndis/2,-heightIndis/2,widthIndis,heightIndis),
    shapeFam  = new Rectangle2D.Double(-widthFams /2,-heightFams /2,widthFams ,heightFams );

  /** gedcom we're looking at */
  private Gedcom gedcom;
  
  /** the root we've used */
  private Entity root;

  /**
   * Constructor
   */
  public Model(Gedcom ged) {
    gedcom = ged;
  }
  
  /**
   * Sets the root
   */
  public void setRoot(Entity entity) {
    // Indi or Fam plz
    if (!(entity instanceof Indi||entity instanceof Fam)) 
      throw new IllegalArgumentException("Indi or Fam please");
    // clear old
    arcs.clear();
    nodes.clear();
    bounds.setFrame(0,0,0,0);
    // keep as root
    root = entity;
    // build and layout the tree
    if (root instanceof Indi)
      layout((Indi)root);
    else
      layout((Fam )root);
    // notify
    fireStructureChanged();
    // done
  }
  
  /**
   * Gets the layout used
   */
  /*package*/ Layout getLayout() {
    return layout;
  }
  
  /**
   * Add listener
   */
  public void addListener(ModelListener l) {
    listeners.add(l);
    if (listeners.size()==1) gedcom.addListener(this);
  }
  
  /**
   * Remove listener
   */
  public void removeListener(ModelListener l) {
    listeners.remove(l);
    if (listeners.size()==0) gedcom.removeListener(this);
  }
  
  /**
   * @see genj.gedcom.GedcomListener#handleChange(Change)
   */
  public void handleChange(Change change) {
    // FIXME: this should be more fine-grained
    setRoot(root);
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
   * builds the tree for an individual
   */
  private void layout(Indi indi) {
    // create tree descendants of ancestor
    MyNode dnode = new MyNode(null, null);
    IndiNode inode = new IndiNode(indi);
    new MyArc(dnode, inode, false);
    inode.addDescendants(dnode);
    layout(dnode, true);
    // remember
    Rectangle2D firstHalf = bounds.getFrame();
    Point2D firstPos = inode.getPosition();
    // create tree of ancestors
    inode = new IndiNode(indi);
    inode.getPosition().setLocation(firstPos);
    inode.addAncestors(null);
    layout(inode, false);
    // update bounds
    bounds.add(firstHalf);
    // done
  }
  
  /**
   * builds the tree for a family
   */
  private void layout(Fam fam) {
    // create tree for descendants 
    FamNode fnode = new FamNode(fam);
    layout(fnode.addDescendants(fnode), true);
    Rectangle2D r = bounds.getFrame();
    // create tree for ancestors
    layout(new FamNode(fam).addAncestors(), false);
    // update bounds
    bounds.add(r);
    // done
  }
  
  /**
   * Helper that applies the layout
   */
  private void layout(MyNode root, boolean isTopDown) {
    // layout
    try {
      layout.setTopDown(isTopDown);
      layout.setBendArcs(true);
      layout.setDebug(false);
      layout.setIgnoreUnreachables(true);
      layout.setBalanceChildren(false);
      layout.setRoot(root);
      layout.applyTo(this);
    } catch (LayoutException e) {
      e.printStackTrace();
    }
    // done
  }
  
  /**
   * An entity by position
   */
  public Entity getEntityAt(double x, double y) {
    // loop nodes
    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      MyNode node = (MyNode)it.next();
      Point2D pos = node.getPosition();
      Shape shape = node.getShape();
      if (shape!=null&&shape.getBounds2D().contains(x-pos.getX(),y-pos.getY())&&node.entity!=null) {
        return node.entity;
      }
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
  private class MyNode implements Node, NodeOptions {
    
    /** the entity */
    protected Entity entity;
    
    /** arcs of this entity */
    private List arcs = new ArrayList(5);
    
    /** position of this entity */
    private Point2D pos = new Point2D.Double();
    
    /** the shape */
    private Shape shape;
    
    /**
     * Constructor
     */
    private MyNode(Entity enTity, Shape sHape) {
      // remember
      entity = enTity;
      shape = sHape;
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
    
    /**
     * @see gj.layout.tree.NodeOptions#getLatitude(Node, double, double)
     */
    public double getLatitude(Node node, double min, double max) {
      // default is centered
      return min + (max-min) * 0.5;
    }
    /**
     * @see gj.layout.tree.NodeOptions#getLongitude(Node, double, double, double, double)
     */
    public double getLongitude(Node node, double minc, double maxc, double mint, double maxt) {
      // default is centered
      return minc + (maxc-minc) * 0.5;
    }
    /**
     * @see gj.layout.tree.NodeOptions#getPadding(int)
     */
    public double getPadding(Node node, int dir) {
      return 0;
    }
  } //MyNode


  /**
   * A node for an individual
   */
  /*package*/ class IndiNode extends MyNode {
    
    /** whether we're used as ancestor or descendant */
    private boolean isAncestor;
    
    /** a role we play as an ancestor */
    private Class role;
    
    /**
     * Constructor
     */
    private IndiNode(Indi indi) {
      super(indi, shapeIndi);
    }
    /**
     * Add descendants - spouses and marriages and children
     */
    private void addDescendants(MyNode pivot) {
      // mark as being descendant
      isAncestor = false;
      // we wrap an indi
      Indi indi = (Indi)entity;
      // loop through our fams
      Fam[] fams = indi.getFamilies();
      FamNode first = null;
      for (int f=0; f<fams.length; f++) {
        // add node for fam, marr and spouse
        FamNode  fnode = new FamNode(fams[f]);
        MarrNode mnode = new MarrNode();
        IndiNode snode = new IndiNode(fams[f].getOtherSpouse(indi));
        // pivot takes marr and spouse
        new MyArc(pivot, mnode, false);
        new MyArc(pivot, snode, false);
        new MyArc(this, fnode, false);
        // add fams descendants to first family
        if (first==null) first = fnode;
        fnode.addDescendants(first);
        // next family
      }
      // done
    }
    /**
     * Add ancestors
     */
    private void addAncestors(Class setRole) {
      // mark as being ancestor
      isAncestor = true;
      role = setRole;
      // we wrap an indi (might be a hull though)
      Indi indi = (Indi)entity;
      if (indi==null) return;
      // do we have a family we're child in?
      Fam famc = indi.getFamc();
      if (famc==null) return;
      // add the family
      FamNode fnode = new FamNode(famc);
      fnode.addAncestors();
      new MyArc(this, fnode, true);
      // done
    }
    /**
     * @see genj.tree.Model.MyNode#getPadding(Node, int)
     */
    public double getPadding(Node node, int dir) {
      return padIndis/2;
    }
    /**
     * @see genj.tree.Model.MyNode#getLongitude(Node, double, double, double, double)
     */
    public double getLongitude(Node node, double minc, double maxc, double mint, double maxt) {
      // Ancestor
      if (isAncestor) {
        // with role
        if (role==PropertyHusband.class)
          return mint-padIndis/2;
        if (role==PropertyWife.class)
          return maxt+padIndis/2;
        // without role
        return super.getLongitude(node, minc, maxc, mint, maxt);
      }
      // Descendant
      return minc + widthFams/2 - widthIndis - padIndis/2 - widthMarrs/2;
    }
  } //MyINode
  
  /**
   * A node for a family
   */
  /*package*/ class FamNode extends MyNode {
    /** side we're reducing padding */
    private int sideWithReducedPadding = NORTH;
    /**
     * Constructor
     */
    private FamNode(Fam fam) {
      // delegate
      super(fam, shapeFam);
    }
    /**
     * Add descendants
     */
    private FamNode addDescendants(MyNode pivot) {
      // Looking at the fam
      Fam fam = (Fam)entity;
      // grab the children
      Indi[] children = fam.getChildren();
      for (int c=0; c<children.length; c++) {
        // here's the child
        Indi child = children[c];
        IndiNode node = new IndiNode(child);
        // create a node&arc for it
        new MyArc(pivot, node, true);       
        // and add descendants
        node.addDescendants(pivot);
        // next child
      }
      // remembering side for reduced padding
      sideWithReducedPadding = NORTH;
      // done
      return this;
    }
    /**
     * Add ancestors
     */
    private FamNode addAncestors() {
      // Looking at the fam
      Fam fam = (Fam)entity;
      int noOfSpouses = fam.getNoOfSpouses();
      // husband
      IndiNode hnode = new IndiNode(fam.getHusband());
      hnode.addAncestors(noOfSpouses<2?null:PropertyHusband.class);
      // wife
      IndiNode wnode = new IndiNode(fam.getWife());
      wnode.addAncestors(noOfSpouses<2?null:PropertyWife.class);
      // connect
      new MyArc(this, wnode, false);
      new MyArc(this, new MarrNode(), false);
      new MyArc(this, hnode, false);
      // remembering side for reduced padding
      sideWithReducedPadding = SOUTH;
      // done
      return this;
    }
    /**
     * @see genj.tree.Model.MyNode#getPadding(Node, int)
     */
    public double getPadding(Node node, int dir) {
      if (dir==sideWithReducedPadding) 
        return -padIndis*0.40;
      if (dir==WEST||dir==EAST) return padIndis*0.05;
      return padIndis/2;
    }
  } //MyFNode

  /**
   * A node standing between two partners
   */
  /*package*/ class MarrNode extends MyNode {
    /**
     * Constructor
     */
    private MarrNode() {
      // delegate
      super(null, shapeMarr);
    }
    /**
     * @see genj.tree.Model.MyNode#getPadding(Node, int)
     */
    public double getPadding(Node node, int dir) {
      if (dir==NORTH||dir==SOUTH) {
        return (shapeIndi.getHeight()+padIndis)/2 - shapeMarr.getBounds2D().getHeight()/2;
      }
      return -padIndis/2;
    }

  } //MarrNode
  
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
   * Calculates marriage rings
   */
  private Shape calcMarriageRings() {
    Ellipse2D
      a = new Ellipse2D.Double(-widthMarrs+widthMarrs/4,-heightMarrs/2,widthMarrs,heightMarrs),
      b = new Ellipse2D.Double(           -widthMarrs/4,-heightMarrs/2,widthMarrs,heightMarrs);
    GeneralPath result = new GeneralPath(a);      
    result.append(b,false);
    return result;
  }
  
} //Model
