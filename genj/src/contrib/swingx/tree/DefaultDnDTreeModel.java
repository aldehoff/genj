/*
 * jOrgan - Java Virtual Organ
 * Copyright (C) 2003 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package swingx.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class DefaultDnDTreeModel extends DefaultTreeModel implements DnDTreeModel {

  /** the dataFlavor used for transfers between different JVMs */
  private static DataFlavor localFlavor;

  static {
    try {
      localFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.Object");
    } catch (ClassNotFoundException e) {
      throw new Error(e);
    }
  }

  /** the dataFlavor used for transfers in one JVM. */
  private static DataFlavor serializedFlavor = new DataFlavor(java.io.Serializable.class, "Object");


  /**
   * Constructor
   * @param root the tree root
   */
  public DefaultDnDTreeModel(TreeNode root) {
    super(root);
  }

  public int canDrag(List children) {
    for (int c = 0; c < children.size(); c++) {
      if (((MutableTreeNode) children.get(c)).getParent() == null) {
        return 0;
      }
    }
    return MOVE;
  }

  public boolean canDrop(int action, Transferable transferable, Object parent, int index) {
    if (action!=MOVE)
      return false;
    if (transferable.isDataFlavorSupported(localFlavor))
      return true;
    if (transferable.isDataFlavorSupported(serializedFlavor))
      return true;
    return false;
  }

  /**
   * Get a transferable for given children
   */
  public Transferable getTransferable(List children) {
    return new DnDTreeTransferable(children);
  }

  /**
   * drag operation
   */
  public void drag(int action, List children, Object parent, int index) {
    if (action==MOVE) {
	    for (int c = children.size() - 1; c >= 0; c--) {
	      removeNodeFromParent((MutableTreeNode) children.get(c));
	    }
    }
  }

  /**
   * drop operation
   */
  public List drop(int action, Transferable transferable, Object parent, int index) throws IOException, UnsupportedFlavorException {
    
    if (action != MOVE) 
      throw new IllegalArgumentException("action not supported: " + action);

    List children;
    if (transferable.isDataFlavorSupported(localFlavor))
      children = (List)transferable.getTransferData(localFlavor);
    else if (transferable.isDataFlavorSupported(serializedFlavor))
      children = (List)transferable.getTransferData(serializedFlavor);
    else throw new UnsupportedFlavorException(null);
    
    for (int c = 0; c < children.size(); c++) {
      insertNodeInto((MutableTreeNode) children.get(c), (MutableTreeNode) parent, index + c);
    }
    
    return children;
  }

  public boolean isNodeAncestor(TreeNode test, TreeNode node) {
    do {
      if (test == node) {
        return true;
      }
    } while ((node = node.getParent()) != null);

    return false;
  }

  /**
   * The transferable used to transfer nodes.
   * 
   * @see #toTransferable(java.util.List)
   * @see #toTransferable(java.awt.datatransfer.Transferable)
   */
  private static class DnDTreeTransferable implements Transferable {

    private List flavors;

    private List nodes;

    public DnDTreeTransferable(List nodes) {
      this.nodes = nodes;

      flavors = createFlavors(nodes);
    }

    protected List createFlavors(List nodes) {
      List flavors = new ArrayList();

      flavors.add(localFlavor);
      flavors.add(DataFlavor.stringFlavor);

      boolean serializable = true;
      for (int n = 0; n < nodes.size(); n++) {
        serializable = serializable && (nodes.get(n) instanceof Serializable);
      }
      if (serializable) {
        flavors.add(serializedFlavor);
      }

      return flavors;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
      if (isDataFlavorSupported(flavor)) {
        if (localFlavor.equals(flavor)) {
          return nodes;
        }
        if (serializedFlavor.equals(flavor)) {
          return nodes;
        }
        if (DataFlavor.stringFlavor.equals(flavor)) {
          return toString();
        }
      }
      throw new UnsupportedFlavorException(flavor);
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer();
      for (int n = 0; n < nodes.size(); n++) {
        if (n > 0) {
          buffer.append("\n");
        }
        buffer.append(nodes.get(n));
      }
      return buffer.toString();
    }

    public DataFlavor[] getTransferDataFlavors() {
      return (DataFlavor[]) flavors.toArray(new DataFlavor[flavors.size()]);
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return flavors.contains(flavor);
    }
  }

}