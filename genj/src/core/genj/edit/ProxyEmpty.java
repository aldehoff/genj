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

import javax.swing.*;

import genj.gedcom.Property;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : EMPTY
 */
class ProxyEmpty extends Proxy {

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return false;
  }

  /**
   * Start editing a property through proxy
   */
  protected void start(JPanel in, JLabel setLabel, Property setProp, EditView edit) {
  // Done
  }

}
