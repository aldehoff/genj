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
package genj.edit;

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A wrapper for properties as a TreeModel
 */
public class PropertyTreeModel implements TreeModel, GedcomListener {

  /** listeners */
  private List listeners = new ArrayList();
  
  /** root of tree */
  private Property root;

  /** history stack */
  private Stack history = new Stack();
  
  /** the gedcom we're looking at */
  private Gedcom gedcom;

  /**
   * Constructor
   */
  public PropertyTreeModel(Gedcom gedcom) {
    this.gedcom=gedcom;
    gedcom.addListener(this);
  }          

  /**
   * Destructor
   */
  /*package*/ void destructor() {
    gedcom.removeListener(this);
  }
  
  /**
   * Set the root
   */
  public void setEntity(Entity entity) {
    // remember history
    if (root!=null) {
      history.push(root.getEntity());
      if (history.size()>16) history.removeElementAt(0);
    }
    // change
    root = entity!=null?entity.getProperty():null;
    fireStructureChanged();
  }
  
  /**
   * Sets the root to the previous one
   */
  public void setPrevious() {
    // is there one?
    if (history.isEmpty()) return;
    // don't want current to end up on the stack
    root = null;
    // set it
    setEntity((Entity)history.pop());
    // done
  }
  
  /**
   * Adds a listener to this model
   */
  public void addTreeModelListener(TreeModelListener l) {
    listeners.add(l);
  }          
  
  /**
   * Removes a Listener from this model
   */
  public void removeTreeModelListener(TreeModelListener l) {
    listeners.remove(l);
  }          
  
  /**
   * Signals to listeners that properties have changed
   */
  public void firePropertiesChanged(List props) {

    // Do it for all changed properties
    Iterator e = props.iterator();
    while (e.hasNext()) {
  
      // .. use property
      Property prop = (Property)e.next();
  
      // .. build event
      Object path[] = root.getPathTo(prop);
      if (path==null)
        continue;
  
      TreeModelEvent ev = new TreeModelEvent(this,path);
  
      // .. tell it to all listeners
      Iterator elisteners = listeners.iterator();
      while (elisteners.hasNext()) {
        ((TreeModelListener)elisteners.next()).treeNodesChanged(ev);
      }
  
      // .. next changed property
    }
  }          
  
  /**
   * Signals to listeners that structure has changed
   */
  public void fireStructureChanged() {

    Object[] path = new Object[]{ root!=null ? (Object)root : ""};
    TreeModelEvent ev = new TreeModelEvent(this,path);

    // .. tell it to all listeners
    Iterator elisteners = listeners.iterator();
    while (elisteners.hasNext()) {
      ((TreeModelListener)elisteners.next()).treeStructureChanged(ev);
    }
  }          
  
  /**
   * Returns child by index of parent
   */
  public Object getChild(Object parent, int index) {
    Property prop = (Property)parent;
    return prop.getProperty(index);
  }          
  
  /**
   * Returns child count of parent
   */
  public int getChildCount(Object parent) {
    Property prop = (Property)parent;
    return prop.getNoOfProperties();
  }          
  
  /**
   * Returns index of given child from parent
   */
  public int getIndexOfChild(Object parent, Object child) {

    // Calculate index by fiven parent property
    int index = ((Property)parent).getIndexOf((Property)child);
  
    // This is zero-based
    return index-1;
    
  }          
  
  /**
   * Returns root of tree
   */
  public Object getRoot() {
    return root;
  }          
  
  /** 
   * Returns root of tree
   */
  public Entity getEntity() {
    if (root==null) return null;
    return root.getEntity();
  }
  
  /**
   * Tells wether object is a leaf
   */
  public boolean isLeaf(Object node) {
    Property prop = (Property)node;
    return prop.getNoOfProperties()==0;
  }          
  
  /**
   * Changes a object at given path (not used here)
   */
  public void valueForPathChanged(TreePath path, Object newValue) {
  } 
  
  /**
   * @see genj.gedcom.GedcomListener#handleChange(Change)
   */
  public void handleChange(Change change) {

    // Could we be affected at all?
    if (root==null) return;

    // Entity deleted ?
    if ( change.isChanged(Change.EDEL) ) {
      // Loop through known entity ?
      boolean affected = false;
      Iterator ents = change.getEntities(Change.EDEL).iterator();
      while (ents.hasNext()) {
        // the entity deleted
        Entity entity = (Entity)ents.next();
        // ... a removed entity has to be removed from stack
        while (history.removeElement(entity)) {};
        // ... and might affect the current edit view
        affected |= (entity.getProperty()==root);
      }
      // Is this a show stopper at this point?
      if (affected==true) {
        root=null;
        fireStructureChanged();
        return;
      }
      // continue
    }

    // Property added/removed ?
    if ( change.isChanged(Change.PADD)||change.isChanged(Change.PDEL)) {
      // reset
      fireStructureChanged();
      // done
      return;
    }

    // Property modified ?
    if ( change.isChanged(change.PMOD) ) {
      if ( change.getEntities(Change.EMOD).contains(root.getEntity())) {
        firePropertiesChanged(change.getProperties(Change.PMOD));
        return;
      }
    }

    // Done
  }
  
} //PropertyTreeModel
