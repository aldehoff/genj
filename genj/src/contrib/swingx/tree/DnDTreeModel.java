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
     * Can the given children be dragged
     * 
     * @param children  children to test 
     * @return          actions (or)
     */
    public int canDrag(List children);

    /**
     * Can the given children be dropped to the given parent.
     * 
     * @param transferable transferable to test for drop
     * @param parent       parent to drag to
     * @param pivot        index to drag to
     * @return             <code>true</code> if drop ok
     */
    public boolean canDrop(int action, Transferable transferable, Object parent, int index);

    /**
     * Create a transferable for given children
     */
    public Transferable getTransferable(List children);
    
    /**
     * Perform drag
     * 
     * @param mode       copy, link or move
     * @param children   children to remove
     * @param parent     drop parent if known (when dnd inside same vm)
     * @param index      drop index (when dnd inside same vm)
     */
    public void drag(int mode, List children, Object parent, int index);

    /**
     * Perform drop
     * 
     * @param mode         copy, link or move
     * @param transferable transferable to insert
     * @param parent       drop parent
     * @param pivot        drop index
     * @return             drop result
     */
    public List drop(int mode, Transferable transferable, Object parent, int index) throws IOException, UnsupportedFlavorException;
}


