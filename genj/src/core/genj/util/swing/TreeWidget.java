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
package genj.util.swing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Our own version of a JTree 
 */
public class TreeWidget extends JTree {
  
  /** old model */
  private TreeModel old;
  
  /**
   * Constructor
   */
  public TreeWidget() {
  }

  /**
   * Constructor
   */
  public TreeWidget(TreeModel model) {
    super(model);
  }

  /**
   * @see java.awt.Component#removeNotify()
   */
  public void addNotify() {
    // restore an old model?
    if (old!=null) setModel(old);
    // continue
    super.addNotify();
  }

  /**
   * @see genj.util.swing.TreeWidget#removeNotify()
   */
  public void removeNotify() {
    // keep old model around in case we're added
    // somewhere again
    old = getModel();
    // remove from model (otherwise listeners of that
    // model might not disconnect)
    setModel(null);
    // continue    
    super.removeNotify();
    
  }
  
  /** 
   * Expands all rows
   */
  public void expandRows() {
     for (int i=0;i<getRowCount();i++) {
       expandRow(i); 
     } 
  }
  
  /**
   * Convenient abstract model
   */
  public static abstract class AbstractTreeModel implements TreeModel {

    /** listeners */
    private List listeners = new ArrayList();

    
    /**
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void addTreeModelListener(TreeModelListener l) {
      listeners.add(l);
    }

    /**
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public abstract Object getChild(Object parent, int index);

    /**
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public abstract int getChildCount(Object parent);

    /**
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(Object parent, Object child) {
      for (int i=0, j=getChildCount(parent);i<j;i++) {
        if (getChild(parent, i).equals(child))
          return i;
      }
      return -1;
    }

    /**
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    public abstract Object getRoot();

    /**
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    public boolean isLeaf(Object node) {
      return getChildCount(node)==0;
    }

    /**
     * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void removeTreeModelListener(TreeModelListener l) {
      listeners.remove(l);
    }

    /**
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
      // ignored
    }
    
    /**
     * Notification to listeners
     */
    protected void fireStructureChanged() {
      TreeModelEvent e = new TreeModelEvent(this, new Object[]{ this });
      TreeModelListener[] ls = (TreeModelListener[])listeners.toArray(new TreeModelListener[listeners.size()]);
      for (int i=0; i<ls.length; i++) {
        ls[i].treeStructureChanged(e);
      }
    }

  } //AbstractTreeModel
  
} //TreeWidget
