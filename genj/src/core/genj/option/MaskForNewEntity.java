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
package genj.option;

import javax.swing.*;
import java.awt.*;

import genj.util.*;
import genj.gedcom.*;

/**
 * Abstract type for all masks that are used when creating an Entity. The
 * mask knows how to create input components that take entity information
 * and the relationship information
 */
abstract class MaskForNewEntity {

  /** the option this mask will be used in */
  protected Option option;

  /**
   * Initiates entity's creation
   */
  abstract boolean createIn(Gedcom gedcom);

  /**
   * Returns the data-page for entity
   */
  abstract JPanel getDataPage();

  /**
   * Returns the data-page for entity
   */
  abstract JPanel getRelationPage(JComponent firstRow);

  /**
   * Initializes the option
   */
  protected void init(Option pOption) {
    option = pOption;
  }

  /**
   * Sets mask's entity
   */
  abstract void handleContextChange(Gedcom gedcom);

  /**
   * Returns a resource string
   */
  protected String getResourceString(String key) {
    return option.getResourceString(key);
  }
}
