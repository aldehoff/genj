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

import java.util.Vector;

import genj.gedcom.*;

/**
 * An interface for a listener to a TreeModel
 */
public interface TreeModelListener {

  /**
   * Notification in case actual link has changed
   */
  public void handleActualChanged(Link oldActual, Link newActual);

  /**
   * Notification in case data and not structure of model has changed
   */
  public void handleDataChanged();

  /**
   * Notification in case entities have changed without change in layout
   * A listener can use iterate to do actions on links to that entities.
   */
  public void handleEntitiesChanged(Vector changed);

  /**
   * Notification in case structure of model has changed
   */
  public void handleStructureChanged();

}
