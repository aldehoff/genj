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

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.PropertyXRef;
import gj.awt.geom.Path;
import gj.layout.LayoutException;
import gj.layout.tree.NodeOptions;
import gj.layout.tree.Orientation;
import gj.layout.tree.TreeLayout;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Model of our tree
 */
public class Model implements Graph, GedcomListener {
  
  /** possible modes */
  public final static int
    ANCESTORS_AND_DESCENDANTS = 0,
    DESCENDANTS               = 1,
    ANCESTORS                 = 2; 
  
  /** listeners */
  private List listeners = new ArrayList(3);

  /** arcs */
  private Collection arcs = new ArrayList(100);

  /** nodes */
  private Collection nodes = new ArrayList(100);

  /** bounds */
  private Rectangle2D bounds = new Rectangle2D.Double();
  
  /** caching */
  private GridCache cache = null;
  
  /** the layout we use */
  private TreeLayout layout = new TreeLayout();
    
  /** whether we're vertical or not */
  private boolean isVertical = true;
  
  /** whether we model families or not */
  private boolean isFamilies = true;
  
  /** whether we bend arcs or not */
  private boolean isBendArcs = true;
  
  /** the mode we're in */
  private int mode = ANCESTORS_AND_DESCENDANTS;
    
  /** parameters */
  private double 
    padding     = 1.0D,
    widthIndis  = 3.0D,
    widthFams   = 2.8D,
    widthMarrs  = widthIndis/8,
    heightIndis = 2.0D,
    heightFams  = 1.0D,
    heightMarrs = heightIndis/8;

  /** shape of marriage rings */
  private Shape 
    shapeMarrs = calcMarriageRings(),
    shapeIndis  = new Rectangle2D.Double(-widthIndis/2,-heightIndis/2,widthIndis,heightIndis),
    shapeFams   = new Rectangle2D.Double(-widthFams /2,-heightFams /2,widthFams ,heightFams );

  /** padding of nodes */
  private double[] // n, e, s, w
    padMarrsV = new double[]{ 
      (heightIndis+padding)/2 - heightMarrs/2,
      -padding/2, 
      (heightIndis+padding)/2 - heightMarrs/2,
      -padding/2 
    },
    padMarrsH = new double[]{ 
      (widthIndis+padding)/2 - widthMarrs/2,
      -padding/2, 
      (widthIndis+padding)/2 - widthMarrs/2,
      -padding/2
    },
    padMarrs = padMarrsV,
    padFamsD = new double[]{
      -padding*0.4,
      padding*0.1,      
      padding/2,
      padding*0.1     
    },
    padFamsA = new double[]{
      padding/2,
      padding*0.05,      
      -padding*0.40,
      padding*0.05      
    },
    padIndis = new double[] {
      padding/2,
      padding/2,
      padding/2,
      padding/2
    };
    
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
   * Accessor - current root
   */
  public void setRoot(Entity entity) {
    // Indi or Fam plz
    if (!(entity instanceof Indi||entity instanceof Fam)) 
      return;
    // no change?
    if (root==entity) return;
    // keep as root
    root = entity;
    // parse the current information
    parse();
    // done
  }

  /**
   * Accessor - current root
   */
  public Entity getRoot() {
    return root;
  }
    
  /**
   * Accessor - wether we're vertical
   */
  public boolean isVertical() {
    return isVertical;
  }
  
  /**
   * Accessor - wether we're vertical
   */
  public void setVertical(boolean set) {
    isVertical = set;
    parse();
  }
  
  /**
   * Accessor - wether we bend arcs or not
   */
  public boolean isBendArcs() {
    return isBendArcs;
  }
  
  /**
   * Accessor - wether we bend arcs or not
   */
  public void setBendArcs(boolean set) {
    isBendArcs = set;
    parse();
  }
  
  /**
   * Accessor - whether we model families
   */
  public boolean isFamilies() {
    return isFamilies;
  } 
  
  /**
   * Accessor - whether we model families
   */
  public void setFamilies(boolean set) {
    isFamilies = set;
    parse();
  } 
  
  /**
   * Accessor - the mode
   */
  public int getMode() {
    return mode;
  } 
  
  /**
   * Accessor - the mode
   */
  public void setMode(int set) {
    switch (set) {
      case ANCESTORS:
      case DESCENDANTS:
      case ANCESTORS_AND_DESCENDANTS:
        mode = set;
    }
    parse();
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
   * Entities by range
   */
  public List getEntitiesIn(Rectangle2D range) {
    return cache.get(range);
  }
     /**
   * An entity by position
   */
  public Entity getEntityAt(double x, double y) {
    // do we have a cache?
    if (cache==null) return null;
    // get nodes in possible range
    double
      w = Math.max( widthIndis, widthFams),
      h = Math.max(heightIndis,heightFams);
    Rectangle2D range = new Rectangle2D.Double(x-w/2, y-h/2, w, h);
    // loop nodes
    Iterator it = cache.get(range).iterator();
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
   * @see genj.gedcom.GedcomListener#handleChange(Change)
   */
  public void handleChange(Change change) {
    // was any entity deleted?
    List deleted = change.getEntities(change.EDEL);
    if (deleted.size()>0) {
      // root has to change?
      if (deleted.contains(root)) {
        root = null;
        List indis = gedcom.getEntities(Gedcom.INDIVIDUALS);
        if (!indis.isEmpty()) root = (Indi)indis.get(0);
      }  
      // parse now
      parse();
      // done
      return;
    }
    // was a relationship property deleted, added or changed?
    List props = change.getProperties(change.PMOD);
    for (int i=0; i<props.size(); i++) {
      if (props.get(i) instanceof PropertyXRef) {
        parse();
        return;
      }
    }
    // done
  }

  /**
   * Parses the current model starting at root   */
  private void parse() {
    // clear old
    arcs.clear();
    nodes.clear();
    bounds.setFrame(0,0,0,0);
    // something to do?
    if (root==null) return;
    // prepare parsers
    Parser 
      pd = isFamilies ? (Parser)new DescendantsWithFams() : new DescendantsNoFams(),
      pa = isFamilies ? (Parser)new AncestorsWithFams() : new AncestorsNoFams();
    // prepare marr padding
    padMarrs = isVertical ? padMarrsV : padMarrsH;
    // do it
    switch (mode) {
      case ANCESTORS_AND_DESCENDANTS: 
        // parse its descendants
        MyNode node = pd.parse(root, null);
        // keep bounds
        Rectangle2D r = bounds.getFrame();
        Point2D p = node.getPosition();
        // parse its ancestors while preserving position
        node = pa.parse(root, p);    
        // update bounds
        bounds.add(r);
        // done
        break;
      case ANCESTORS:
        pa.parse(root, null);
        break;
      case DESCENDANTS:
        pd.parse(root, null);
        break;
    }
    // create gridcache
    cache = new GridCache(bounds, 4*Math.max(heightIndis+heightFams, widthIndis+widthFams));
    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      MyNode n = (MyNode)it.next();
      Shape s = n.getShape();
      if (s!=null) cache.put(n, s.getBounds2D(), n.getPosition());
    }
    // notify
    fireStructureChanged();
    // done
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
   * Calculates marriage rings
   */
  private Shape calcMarriageRings() {
    Ellipse2D
      a = new Ellipse2D.Double(-widthMarrs/2,-heightMarrs/2,widthMarrs*0.6,heightMarrs),
      b = new Ellipse2D.Double( widthMarrs/2-widthMarrs*0.6,-heightMarrs/2,widthMarrs*0.6,heightMarrs);
    GeneralPath result = new GeneralPath(a);      
    result.append(b,false);
    return result;
  }
  
  /**
   * A node for an entity
   */
  /*private*/ class MyNode implements Node, NodeOptions {
    
    /** the entity */
    protected Entity entity;
    
    /** arcs of this entity */
    private List arcs = new ArrayList(5);
    
    /** position of this entity */
    private Point2D pos = new Point2D.Double();
    
    /** the shape */
    private Shape shape;
    
    /** padding */
    private double[] padding;
    
    /**
     * Constructor
     */
    private MyNode(Entity enTity, Shape sHape, double[] padDing) {
      // remember
      entity = enTity;
      shape = sHape;
      padding = padDing;
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
  } //MyNode

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
   * Parser
   */
  private abstract class Parser {
    
    /**
     * parses a tree starting from entity     * @param entity either fam or indi     */
    public MyNode parse(Entity entity, Point2D at) {
      if (root instanceof Indi)
        return parse((Indi)root, at);
      else
        return parse((Fam )root, at);
    }
    
    /**
     * parses a tree starting from an indi     */
    public abstract MyNode parse(Indi indi, Point2D at);
    
    /**
     * parses a tree starting from a family     */
    public abstract MyNode parse(Fam fam, Point2D at);
    
    /**
     * Helper that applies the layout
     */
    private void layout(MyNode root, boolean isTopDown) {
      // layout
      try {
        layout.setTopDown(isTopDown);
        layout.setBendArcs(isBendArcs);
        layout.setDebug(false);
        layout.setIgnoreUnreachables(true);
        layout.setBalanceChildren(false);
        layout.setRoot(root);
        layout.setVertical(isVertical);
        layout.applyTo(Model.this);
      } catch (LayoutException e) {
        e.printStackTrace();
      }
      // done
    }
    
  } //Parser

  /**
   * Parser - Ancestors without Families   */
  private class AncestorsNoFams extends Parser {
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam, java.awt.geom.Point2D)
     */
    public MyNode parse(Fam fam, Point2D at) {
      MyNode node = new MyNode(fam, shapeFams, padIndis);
      Indi wife = fam.getWife();
      Indi husb = fam.getHusband();
      if (wife!=null) new MyArc(node, iterate(wife), true);
      if (husb!=null) new MyArc(node, iterate(husb), true);
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, false);
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi, java.awt.geom.Point2D)
     */
    public MyNode parse(Indi indi, Point2D at) {
      MyNode node = iterate(indi);
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, false);
      return node;
    }
    /**
     * parse an individual and its ancestors
     */
    private MyNode iterate(Indi indi) {
      // node for indi      
      MyNode node = new MyNode(indi, shapeIndis, padIndis);
      // do we have a family we're child in?
      if (indi!=null) {
        Fam famc = indi.getFamc();
        // grab the family's husband/wife and their ancestors
        if (famc!=null) iterate(famc, node);
      } 
      // done
      return node;
    }
    /**
     * parses a family and its ancestors
     */
    private void iterate(Fam fam, MyNode child) {
      Indi wife = fam.getWife();
      Indi husb = fam.getHusband();
      if (wife!=null) new MyArc(child, iterate(wife), true);
      if (husb!=null) new MyArc(child, iterate(husb), true);
    }
  } //AncestorsNoFams 
   
  /**
   * Parser - Ancestors with Families   */
  private class AncestorsWithFams extends Parser {
    
    private final int
      CENTER = 0,
      LEFT   = 1,
      RIGHT  = 2;
      
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    public MyNode parse(Fam fam, Point2D at) {
      MyNode node = iterate(fam);
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, false);
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi)
     */
    public MyNode parse(Indi indi, Point2D at) {
      MyNode node = iterate(indi, CENTER);
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, false);
      return node;
    }
    /**
     * parse a family and its ancestors
     */
    private MyNode iterate(Fam fam) {
      // node for the fam
      MyNode node = new MyNode(fam, shapeFams, padFamsA);
      // husband & wife
      Indi
        husb = fam.getHusband(),
        wife = fam.getWife();
      new MyArc(node, iterate(wife, hasParents(husb)?LEFT:CENTER), false);
      new MyArc(node, new MyNode(null, shapeMarrs, padMarrs), false);
      new MyArc(node, iterate(husb, hasParents(wife)?RIGHT:CENTER), false);
      // done
      return node;
    }
    /**
     * helper that checks if an individual is child in a family
     */
    private boolean hasParents(Indi indi) {
      if (indi==null) return false;
      return indi.getFamc()!=null;
    }    /**
     * parse an individual and its ancestors     */
    private MyNode iterate(Indi indi, final int alignment) {
      // node for indi      
      MyNode node;
      if (alignment==CENTER) {
        node = new MyNode(indi, shapeIndis, padIndis);
      } else {
        node = new MyNode(indi, shapeIndis, padIndis) { 
          /** patching longitude */
          public double getLongitude(Node node, double minc, double maxc, double mint, double maxt, Orientation o) {
            if (alignment==RIGHT) return mint-padding/2;
            return maxt+padding/2;
          }
        };
      }
      // do we have a family we're child in?
      if (indi!=null) {
        Fam famc = indi.getFamc();
        // grab the family
        if (famc!=null) 
          new MyArc(node, iterate(famc), true);
      } 
      // done
      return node;
    }
  } //AncestorsWithFams

  /**
   * Parser - Descendants no Families   */
  private class DescendantsNoFams extends Parser {
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi)
     */
    public MyNode parse(Indi indi, Point2D at) {
      // parse
      MyNode node = iterate(indi);
      if (at!=null) node.getPosition().setLocation(at);
      // do the layout
      super.layout(node, true);
      // done
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    public MyNode parse(Fam fam, Point2D at) {
      // parse
      MyNode node = new MyNode(fam, shapeFams, padIndis);
      iterate(fam, node);
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, true);
      return node;
    }
    
    /**
     * parses an indi
     * @param indi the indi to parse
     * @return MyNode
     */
    private MyNode iterate(Indi indi) {
      // create node for indi
      MyNode node = new MyNode(indi, shapeIndis, padIndis); 
      // loop through our fams
      Fam[] fams = indi.getFamilies();
      for (int f=0; f<fams.length; f++) {
        iterate(fams[f], node);
      }
      // done
      return node;
    }
    
    /**
     * parses a fam and its descendants
     */
    private void iterate(Fam fam, MyNode parent) {
      // grab the children
      Indi[] children = fam.getChildren();
      for (int c=0; c<children.length; c++) {
      new MyArc(parent, iterate(children[c]), true);       
      }
      // done
    }
  } //DescendantsNoFams

  /**
   * Parser - Descendants with Families 
   */
  private class DescendantsWithFams extends Parser {
    
    /** the alignment offset for an individual above its 1st fam */
    private Point2D.Double alignOffsetIndiAbove1stFam = new Point2D.Double(
      padFamsD[MyNode.WEST]-padIndis[MyNode.WEST] +  widthFams/2 - widthIndis  - ( widthMarrs)/2,
      padIndis[MyNode.WEST]-padFamsD[MyNode.WEST] - heightFams/2 + heightIndis + (heightMarrs)/2
    );
    
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi)
     */
    public MyNode parse(Indi indi, Point2D at) {
      // won't support at
      if (at!=null) throw new IllegalArgumentException("at is not supported");
      // parse under pivot
      MyNode pivot = new MyNode(null, null, null);
      MyNode node = iterate(indi, pivot);
      // do the layout
      super.layout(pivot, true);
      // done
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    public MyNode parse(Fam fam, Point2D at) {
      MyNode node = iterate(fam, null);
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, true);
      return node;
    }
        
    /**
     * parses an indi     * @param indi the indi to parse     * @param pivot all nodes of descendant are added to pivot     * @return MyNode     */
    private MyNode iterate(Indi indi, MyNode pivot) {
      // create node for indi
      MyNode node = new MyNode(indi, shapeIndis, padIndis) { 
        /** patch latitude */
        public double getLongitude(Node node, double minc, double maxc, double mint, double maxt, Orientation o) {
          return minc + o.getLongitude( alignOffsetIndiAbove1stFam );
        }
      };
      new MyArc(pivot, node, pivot.getShape()!=null);
      // loop through our fams
      Fam[] fams = indi.getFamilies();
      MyNode fam1 = null;
      for (int f=0; f<fams.length; f++) {
        // add arc : node-fam
        MyNode fami = iterate(fams[f], fam1);
        if (fam1==null) fam1 = fami;
        new MyArc(node, fami, false);
        // add arcs " pivot-marr, pivot-spouse
        new MyArc(pivot, new MyNode(null, shapeMarrs, padMarrs), false);
        new MyArc(pivot, new MyNode(fams[f].getOtherSpouse(indi), shapeIndis, padIndis), false);
        // next family
      }
      // done
      return node;
    }
    /**
     * parses a fam and its descendants
     * @parm pivot all nodes of descendant are added to pivot     */
    private MyNode iterate(Fam fam, MyNode pivot) {
      // node for fam
      MyNode node = new MyNode(fam, shapeFams, padFamsD);
      // pivot is me if unset
      if (pivot==null) pivot = node;
      // grab the children
      Indi[] children = fam.getChildren();
      for (int c=0; c<children.length; c++) {
        // create an arc from node to node for indi
        iterate(children[c], pivot);       
         // next child
      }
      // done
      return node;
    }
    
  } //DescendantsWithFams
  
} //Model
