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

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.List;

import javax.swing.tree.TreeModel;

/**
 * A tree model that offers altering through DnD.
 */
public interface DnDTreeModel extends TreeModel {
    
    public static final int COPY = DnDConstants.ACTION_COPY;
    public static final int MOVE = DnDConstants.ACTION_MOVE;
    public static final int LINK = DnDConstants.ACTION_LINK;

    /**
     * Can the given children be removed from their parents.
     * 
     * @param children  children to test for removal
     * @return          <code>true</code> if children can be removed
     */
    public boolean canRemove(List children);

    /**
     * Remove children from its parent.
     * 
     * @param children   children to remove
     * @param target     new parent if known (when dnd inside same vm)
     */
    public void remove(List children, Object target);

    /**
     * Create a transferable for given children
     */
    public Transferable getTransferable(List children);
    
    /**
     * Can the given children be inserted to the given parent.
     * 
     * @param transferable transferable to test for insertion
     * @param parent       parent of children to insert
     * @return             <code>true</code> if insert is acceptable
     */
    public boolean canInsert(Transferable transferable, Object parent, int index, int action);

    /**
     * Insert children to the given parent.
     * <br>
     * The list of children is garanteed to be ordered from top to bottom.
     * 
     * @param transferable transferable to insert
     * @param parent       parent to insert into
     * @param index        index for children to insert at
     * @return             list of added children
     */
    public List insert(Transferable transferable, Object parent, int index, int action) throws IOException, UnsupportedFlavorException;
}


