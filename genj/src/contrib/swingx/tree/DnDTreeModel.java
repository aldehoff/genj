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

import java.awt.dnd.DnDConstants;
import java.util.List;

import javax.swing.tree.*;

/**
 * A tree model that offers altering through DnD.
 */
public interface DnDTreeModel extends TreeModel {
    
    public static final int COPY = DnDConstants.ACTION_COPY;
    public static final int MOVE = DnDConstants.ACTION_MOVE;
    public static final int LINK = DnDConstants.ACTION_LINK;

    /**
     * Must nodes of this model be removed before they
     * can be inserted.
     * 
     * @return  <code>true</code> if removal should happen
     *          before insertion
     */
    public boolean removeBeforeInsert();

    /**
     * Can the given children be removed from their parents.
     * 
     * @param children  children to test for removal
     * @return          <code>true</code> if children can be removed
     */
    public boolean canRemove(List children);

    /**
     * Can the given children be inserted to the given parent.
     * 
     * @param children  children to test for insertion
     * @param parent    parent of children to insert
     * @return          <code>true</code> if children can be inserted
     */
    public boolean canInsert(List children, Object parent, int index, int action);

    /**
     * Remove children from its parent.
     * 
     * @param children     children to remove
     */
    public void removeFrom(List children);

    /**
     * Insert children to the given parent.
     * <br>
     * The list of children is garanteed to be ordered from top to bottom.
     * 
     * @param children  children to insert
     * @param parent    parent to insert into
     * @param index     index of children to insert
     */
    public void insertInto(List children, Object parent, int index, int action);
}


