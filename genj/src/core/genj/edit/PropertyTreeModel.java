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

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import genj.gedcom.*;

/**
 *
 */
public class PropertyTreeModel implements TreeModel {

  private Vector listeners = new Vector();
  private Property root;

  /**
   * Constructor
   */
  public PropertyTreeModel(Property root) {
    this.root = root;
  }          
  
  /**
   * Adds a listener to this model
   */
  public void addTreeModelListener(TreeModelListener l) {
    listeners.addElement(l);
  }          
  
  /**
   * Signals to listeners that properties have changed
   */
  public void firePropertiesChanged(Vector props) {

    // Do it for all changed properties
    Enumeration e = props.elements();
    while (e.hasMoreElements()) {
  
      // .. use property
      Property prop = (Property)e.nextElement();
  
      // .. build event
      Object path[] = root.getPathTo(prop);
      if (path==null)
        continue;
  
      TreeModelEvent ev = new TreeModelEvent(this,path);
  
      // .. tell it to all listeners
      Enumeration elisteners = listeners.elements();
      while (elisteners.hasMoreElements()) {
        ((TreeModelListener)elisteners.nextElement()).treeNodesChanged(ev);
      }
  
      // .. next changed property
    }
  }          
  
  /**
   * Signals to listeners that structure has changed
   */
  public void fireStructureChanged() {

    Object path[] = { root };
    TreeModelEvent ev = new TreeModelEvent(this,path);

    // .. tell it to all listeners
    Enumeration elisteners = listeners.elements();
    while (elisteners.hasMoreElements()) {
      ((TreeModelListener)elisteners.nextElement()).treeStructureChanged(ev);
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
   * Tells wether object is a leaf
   */
  public boolean isLeaf(Object node) {
    Property prop = (Property)node;
    return prop.getNoOfProperties()==0;
  }          
  
  /**
   * Removes a Listener from this model
   */
  public void removeTreeModelListener(TreeModelListener l) {
    listeners.removeElement(l);
  }          
  
  /**
   * Changes a object at given path (not used here)
   */
  public void valueForPathChanged(TreePath path, Object newValue) {
  }          
}
