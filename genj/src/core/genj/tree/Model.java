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
import gj.model.Graph;
import gj.model.Node;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  private Map entities2nodes = new HashMap(100);

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
  
  /** the mode we're in */
  private int mode = ANCESTORS_AND_DESCENDANTS;
    
  /** gedcom we're looking at */
  private Gedcom gedcom;
  
  /** the root we've used */
  private Entity root;

  /** metrics */
  private TreeMetrics metrics = new TreeMetrics( 3.0, 2.0, 2.8, 1.0, 1.0 );
  
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
   * Accessor - the metrics   */
  public TreeMetrics getMetrics() {
    return metrics;
  } 
  
  /**
   * Accessor - the metrics
   */
  public void setMetrics(TreeMetrics set) {
    metrics = set;
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
  public Set getEntitiesIn(Rectangle2D range) {
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
      w = Math.max(metrics.wIndis, metrics.wFams),
      h = Math.max(metrics.hIndis, metrics.hFams);
    Rectangle2D range = new Rectangle2D.Double(x-w/2, y-h/2, w, h);
    // loop nodes
    Iterator it = cache.get(range).iterator();
    while (it.hasNext()) {
      TreeNode node = (TreeNode)it.next();
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
   * A node for entity (might be null)
   */
  public Node getNode(Entity e) {
    return (Node)entities2nodes.get(e);
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
    return entities2nodes.values();
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
   * Adds a node   */
  /*package*/ TreeNode add(TreeNode node) {
    Object key = node.entity;
    if (key==null) key = node;
    entities2nodes.put(key, node);
    return node;
  }
  
  /**
   * Adds an arc   */
  /*package*/ TreeArc add(TreeArc arc) {
    arcs.add(arc);
    return arc;
  }

  /**
   * Parses the current model starting at root   */
  private void parse() {
    // clear old
    arcs.clear();
    entities2nodes.clear();
    bounds.setFrame(0,0,0,0);
    // something to do?
    if (root==null) return;
    // do it
    switch (mode) {
      case ANCESTORS_AND_DESCENDANTS: 
        // parse its descendants
        TreeNode node = Parser.getInstance(false, isFamilies, this, metrics).parse(root, null);
        // keep bounds
        Rectangle2D r = bounds.getFrame();
        Point2D p = node.getPosition();
        // parse its ancestors while preserving position
        node = Parser.getInstance(true, isFamilies, this, metrics).parse(root, p);    
        // update bounds
        bounds.add(r);
        // done
        break;
      case ANCESTORS:
        Parser.getInstance(true, isFamilies, this, metrics).parse(root, null);
        break;
      case DESCENDANTS:
        Parser.getInstance(false, isFamilies, this, metrics).parse(root, null);
        break;
    }
    // create gridcache
    cache = new GridCache(bounds, 10.0);
    Iterator it = getNodes().iterator();
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
   * Fire even
   */
  private void fireStructureChanged() {
    for (int l=listeners.size()-1; l>=0; l--) {
      ((ModelListener)listeners.get(l)).structureChanged(this);
    }
  }
  
} //Model
