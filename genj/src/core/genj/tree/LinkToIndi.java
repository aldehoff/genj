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

import java.awt.*;
import genj.gedcom.*;

/**
 * A link represents a box in a tree : Individual
 */
class LinkToIndi extends Link {

  /**
   * Constructor for LinkToIndi
   */
  LinkToIndi(int setDepthInTree,int setPosInGen,Indi setIndi) {
    // Inherited
    super(setDepthInTree,setPosInGen,setIndi);
    // Done
  }

  /**
   * Action for click on PlusMinus
   */
  void clickIn(TreeModel model) {
    model.setActual( this );
  }

  /**
   * Action for doubleclick on PlusMinus
   */
  void dclickIn(TreeModel model) {
    model.setRoot( getEntity() );
  }

  /**
   * Returns proxies for this link
   */
  Proxy[] getProxies(TreeGraphics tg) {
    return tg.indisProxies;
  }

  /**
   * Returns the pixel size in tree
   */
  Dimension getSize(TreeModel model) {
    return model.getSize(Gedcom.INDIVIDUALS);
  }

  /**
   * Overridden shadow test method for INDIs
   */
  boolean isShadow(TreeGraphics tg) {
    return tg.isShadow;
  }
}

