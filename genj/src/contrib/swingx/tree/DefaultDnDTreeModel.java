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

import java.util.List;

import javax.swing.tree.*;

public class DefaultDnDTreeModel extends DefaultTreeModel implements DnDTreeModel {

    public DefaultDnDTreeModel(TreeNode root) {
        super(root);
    }

    /**
     * Node should be removed before inserted.
     */
    public boolean removeBeforeInsert() {
        return true;
    }

    public boolean canInsert(List children, Object parent, int index, int action) {
        if (action != MOVE) {
            return false;
        }

        for (int c = 0; c < children.size(); c++) {
            if (isNodeAncestor((MutableTreeNode)children.get(c), (MutableTreeNode)parent)) {
                return false;
            }            
        }
        return true;
    }

    public boolean canRemove(List children) {
        for (int c = 0; c < children.size(); c++) {
            if (((MutableTreeNode)children.get(c)).getParent() == null) {
                return false;
            }            
        }
        return true;
    }

    /**
     * Remove child.
     * 
     * @param child     child to remove
     */
    public void removeFrom(List children) {
        for (int c = children.size() - 1; c >= 0; c--) {
            removeNodeFromParent((MutableTreeNode)children.get(c));
        }
    }

    /**
     * Insert children.
     * 
     * @param children  children to insert
     * @param parent    parent to insert into
     * @param index     index of children to insert
     */
    public void insertInto(List children, Object parent, int index, int action) {
        if (action != MOVE) {
           throw new IllegalArgumentException("action not supported: " + action);
        }

        for (int c = 0; c < children.size(); c++) {
            insertNodeInto((MutableTreeNode)children.get(c), (MutableTreeNode)parent, index + c);
        }
    }

    public boolean isNodeAncestor(TreeNode test, TreeNode node) {
        do {
            if (test == node) {
                return true;
            }        
        } while((node = node.getParent()) != null);

        return false;
    }
}