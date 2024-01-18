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
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import gj.layout.LayoutException;
import gj.layout.tree.TreeLayout;
import gj.model.Node;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Model of our tree
 */
public class Model implements GedcomListener {
  
  /** listeners */
  private List listeners = new ArrayList(3);

  /** arcs */
  private Collection arcs = new ArrayList(100);

  /** nodes */
  private Map entities2nodes = new HashMap(100);
  private Collection nodes = new ArrayList(100);

  /** bounds */
  private Rectangle2D bounds = new Rectangle2D.Double();
  
  /** caching */
  private GridCache cache = null;
  
  /** whether we're vertical or not */
  private boolean isVertical = true;
  
  /** whether we model families or not */
  private boolean isFamilies = true;
  
  /** whether we bend arcs or not */
  private boolean isBendArcs = true;
  
  /** whether we show marriage symbols */
  private boolean isMarrSymbols = true;
  
  /** whether we show toggles for un/folding */
  private boolean isFoldSymbols = true;
  
  /** individuals whose ancestors we're not interested in */
  private Set hideAncestors = new HashSet();

  /** individuals whose descendants we're not interested in */
  private Set hideDescendants = new HashSet();
  
  /** individuals' family */
  private Map indi2fam = new HashMap();

  /** gedcom we're looking at */
  private Gedcom gedcom;
  
  /** the root we've used */
  private Entity root;

  /** metrics */
  private TreeMetrics metrics = new TreeMetrics( 6.0F, 3.0F, 3.0F, 1.5F, 1.0F );
  
  /** bookmarks */
  private LinkedList bookmarks = new LinkedList();
  
  /**
   * Constructor
   */
  public Model(Gedcom ged) {
    gedcom = ged;
  }
  
  /**
   * Accessor - gedcom   */
  public Gedcom getGedcom() {
    return gedcom;
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
    update();
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
    if (isVertical==set) return;
    isVertical = set;
    update();
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
    if (isBendArcs==set) return;
    isBendArcs = set;
    update();
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
    if (isFamilies==set) return;
    isFamilies = set;
    update();
  } 
  
  /**
   * Access - isMarrSymbol
   */
  public boolean isMarrSymbols() {
    return isMarrSymbols;
  }

  /**
   * Access - isShowMarrSymbol
   */
  public void setMarrSymbols(boolean set) {
    if (isMarrSymbols==set) return;
    isMarrSymbols = set;
    update();
  }

  /**
   * Access - isFoldSymbols
   */
  public void setFoldSymbols(boolean set) {
    if (isFoldSymbols==set) return;
    isFoldSymbols = set;
    update();
  }

  /**
   * Access - isToggles
   */
  public boolean isFoldSymbols() {
    return isFoldSymbols;
  }

  /**
   * Accessor - the metrics   */
  public TreeMetrics getMetrics() {
    return metrics;
  } 
  
  /**
   * Accessor - the metrics
   */
  public void setMetrics(TreeMetrics set) {
    if (metrics.equals(set)) return;
    metrics = set;
    update();
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
   * Nodes by range
   */
  public Collection getNodesIn(Rectangle2D range) {
    if (cache==null) return new HashSet();
    return cache.get(range);
  }

  /**
   * Arcs by range
   */
  public Collection getArcsIn(Rectangle2D range) {
    return arcs;
  }

  /**
   * An node by position
   */
  public TreeNode getNodeAt(double x, double y) {
    // do we have a cache?
    if (cache==null) return null;
    // get nodes in possible range
    double
      w = Math.max(metrics.wIndis, metrics.wFams),
      h = Math.max(metrics.hIndis, metrics.hFams);
    Rectangle2D range = new Rectangle2D.Double(x-w/2, y-h/2, w, h);
    // loop nodes
    Iterator it = cache.get(range).iterator();
    while (it.hasNext()) {
      TreeNode node = (TreeNode)it.next();
      Point2D pos = node.getPosition();
      Shape shape = node.getShape();
      if (shape!=null&&shape.getBounds2D().contains(x-pos.getX(),y-pos.getY()))
        return node;
    }
    
    // nothing found
    return null;
  }
     /**
   * Content by position
   */
  public Object getContentAt(double x, double y) {
    TreeNode node = getNodeAt(x, y);
    return node!=null ? node.getContent() : null;
  }

  /**
   * An entity by position
   */
  public Entity getEntityAt(double x, double y) {
    Object content = getContentAt(x, y);
    return content instanceof Entity ? (Entity)content : null;
  }
  
  /**
   * A node for entity (might be null)
   */
  public Node getNode(Entity e) {
    return (Node)entities2nodes.get(e);
  }
  
  /**
   * The models space bounds
   */
  public Rectangle2D getBounds() {
    return bounds;
  }

  /**
   * Add a bookmark
   */
  public void addBookmark(Bookmark b) {
    bookmarks.addFirst(b);
    if (bookmarks.size()>16) bookmarks.removeLast();
    fireBookmarksChanged();
  }
  
  /**
   * Del a bookmark
   */
  public void delBookmark(Bookmark b) {
    bookmarks.remove(b);
    fireBookmarksChanged();
  }
  
  /**
   * Accessor - bookmarks
   */
  public List getBookmarks() {
    return Collections.unmodifiableList(bookmarks);
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
      // bookmarks?
      ListIterator it = bookmarks.listIterator();
      while (it.hasNext()) {
        Bookmark b = (Bookmark)it.next();
        if (deleted.contains(b.getEntity())) it.remove();
      }
      // indi2fam?
      indi2fam.keySet().removeAll(deleted);
      // parse now
      update();
      // done
      return;
    }
    // was a relationship property deleted, added or changed?
    List props = change.getProperties(change.PMOD);
    for (int i=0; i<props.size(); i++) {
      if (props.get(i) instanceof PropertyXRef) {
        update();
        return;
      }
    }
    // was an individual or family created and we're without root
    if (root==null) {
      Iterator es = change.getEntities(change.EADD).iterator();
      while (es.hasNext()) {
        Entity e = (Entity)es.next();
        if (e instanceof Fam || e instanceof Indi) {
          setRoot(e);
          return;
        }
      }
    }
    // was a property changed that we should notify about?
    HashSet nodes = new HashSet();
    for (int i=0; i<props.size(); i++) {
      Node node = getNode(((Property)props.get(i)).getEntity());
      if (node!=null) nodes.add(node);
    }
    if (!nodes.isEmpty()) fireNodesChanged(new ArrayList(nodes));
    
    // done
  }
  
  /**
   * Whether we're hiding descendants of given entity
   */
  /*package*/ boolean isHideDescendants(Indi indi) {
    return hideDescendants.contains(indi);
  }
  
  /**
   * Whether we're hiding ancestors of given entity
   */
  /*package*/ boolean isHideAncestors(Indi indi) {
    return hideAncestors.contains(indi);
  }
  
  /** 
   * The current family of individual
   */
  /*package*/ Fam getFamily(Indi indi, Fam fams[], boolean next) {
    // only one?
    if (fams.length>0) {
      // lookup map
      Fam fam = (Fam)indi2fam.get(indi);
      if (fam==null) fam = fams[0];
      for (int f=0;f<fams.length;f++) {
        if (fams[f]==fam) 
          return fams[(f+(next?1:0))%fams.length];
      }
      // invalid fam
      indi2fam.remove(indi);
    }
    // done
    return fams[0];
  }
  
  /**
   * Adds a node   */
  /*package*/ TreeNode add(TreeNode node) {
    // check content
    Object content = node.getContent();
    if (content instanceof Entity) {
      entities2nodes.put(content, node);
    }
    nodes.add(node);
    return node;
  }
  
  /**
   * Adds an arc   */
  /*package*/ TreeArc add(TreeArc arc) {
    arcs.add(arc);
    return arc;
  }
  
  /**
   * Currently shown entities
   */
  /*package*/ Set getEntities() {
    return entities2nodes.keySet();
  }

  /**
   * Parses the current model starting at root   */
  private void update() {
    
    // clear old
    arcs.clear();
    nodes.clear();
    entities2nodes.clear();
    bounds.setFrame(0,0,0,0);
    
    // nothing to do if no root set
    if (root==null) return;

    // parse and layout    
    try {
      // make sure families only when root is not family
      boolean isFams = isFamilies || root instanceof Fam;
      // parse its descendants
      Parser descendants = Parser.getInstance(false, isFams, this, metrics);
      bounds.add(layout(descendants.parse(root), true));
      // parse its ancestors 
      bounds.add(layout(descendants.align(Parser.getInstance(true, isFams, this, metrics).parse(root)), false));
    } catch (LayoutException e) {
      e.printStackTrace();
      root = null;
      update();
      return;
    }
    
    // create gridcache
    cache = new GridCache(
      bounds, 3*metrics.calcMax()
    );
    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      TreeNode n = (TreeNode)it.next();
      Shape s = n.getShape();
      if (s!=null) cache.put(n, s.getBounds2D(), n.getPosition());
    }
    
    // notify
    fireStructureChanged();
    // done
  }

  /**
   * Helper that runs a TreeLayout
   */
  private Rectangle2D layout(TreeNode root, boolean isTopDown) throws LayoutException {
    // layout
    TreeLayout layout = new TreeLayout();
    layout.setTopDown(isTopDown);
    layout.setBendArcs(isBendArcs);
    layout.setDebug(false);
    layout.setIgnoreUnreachables(true);
    layout.setBalanceChildren(false);
    layout.setRoot(root);
    layout.setVertical(isVertical);
    return layout.layout(root, nodes.size());
    // done
  }
  
  
  /**
   * Fire event
   */
  private void fireStructureChanged() {
    for (int l=listeners.size()-1; l>=0; l--) {
      ((ModelListener)listeners.get(l)).structureChanged(this);
    }
  }
  
  /**
   * Fire event
   */
  private void fireNodesChanged(List nodes) {
    for (int l=listeners.size()-1; l>=0; l--) {
      ((ModelListener)listeners.get(l)).nodesChanged(this, nodes);
    }
  }
  
  /**
   * Fire bookmarks changed
   */
  private void fireBookmarksChanged() {
    for (int l=listeners.size()-1; l>=0; l--) {
      ((ModelListener)listeners.get(l)).bookmarksChanged(this);
    }
  }

  /**
   * NextFamily
   */
  /*package*/ class NextFamily implements Runnable {
    /** indi */
    private Indi indi;
    /** next fams */
    private Fam fam;
    /**
     * constructor
     * @param individual indi to un/fold
     * @param fams number of fams to roll over
     */
    protected NextFamily(Indi individual, Fam[] fams) {
      indi = individual;
      fam = getFamily(indi, fams, true);
    }
    /**
     * perform 
     */
    public void run() {
      indi2fam.put(indi, fam);
      update();
    }
  } //NextFamily
  
  /**
   * FoldUnfold
   */
  /*package*/ class FoldUnfold implements Runnable {
    /** indi */
    private Indi indi;
    /** set to change */
    private Set set;
    /**
     * constructor
     * @param individual indi to un/fold
     * @param ancestors whether to change its ancestors/descendants
     */
    protected FoldUnfold(Indi individual, boolean ancestors) {
      indi = individual;
      set = ancestors ? hideAncestors : hideDescendants; 
    }
    /**
     * perform 
     */
    public void run() {
      if (!set.remove(indi)) set.add(indi);
      update();
    }
  } //FoldUnfold
  
} //Model
