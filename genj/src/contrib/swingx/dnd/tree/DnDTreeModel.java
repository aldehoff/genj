/*
 * swingx - Swing eXtensions
 * Copyright (C) 2004 Sven Meier
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
package swingx.dnd.tree;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;

import javax.swing.tree.*;

/**
 * A tree model that offers altering through DnD.
 */
public interface DnDTreeModel extends TreeModel {
    
    public static final int COPY = DnDConstants.ACTION_COPY;
    public static final int MOVE = DnDConstants.ACTION_MOVE;
    public static final int LINK = DnDConstants.ACTION_LINK;

    public Transferable createTransferable(Object[] children);

    public int getDragActions(Transferable transferable);
    
    public int getDropActions(Transferable transferable, Object parent, int index);
    
    public void drag(Transferable transferable, int action) throws UnsupportedFlavorException, IOException;
    
    public void drop(Transferable transferable, Object parent, int index, int action) throws UnsupportedFlavorException, IOException;
    
    public void releaseTransferable(Transferable transferable);
}