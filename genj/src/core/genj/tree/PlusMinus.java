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

import genj.util.*;
import genj.gedcom.Entity;

/**
 * A link represents a box in a tree : PlusMinus (Switch)
 */
class PlusMinus extends Link {

  /*package*/ boolean stopped;

  final static Dimension dim = new Dimension(Images.imgPlus.getIconWidth(),Images.imgPlus.getIconWidth());

  /**
   * Constructor for LinkToPlus
   */
  PlusMinus(TreeModel model, int setDepthInTree,int setPosInGen,Entity setEntity, boolean topAligned) {
    // Inherited
    super(setDepthInTree+(topAligned?6:-6),setPosInGen,setEntity);
    stopped = model.isStopper(setEntity);
    // Done
  }

  /**
   * Action for click on PlusMinus
   */
  void clickIn(TreeModel model) {
    Entity e = getEntity();
    if (e==null) {
      return;
    }
    stopped = model.flipStopper(getEntity());
  }

  /**
   * Action for doubleclick on PlusMinus
   */
  void dclickIn(TreeModel model) {
    clickIn(model);
  }

  /**
   * Returns proxies for this link
   */
  Proxy[] getProxies(TreeGraphics tg) {
    return null;
  }

  /**
   * Returns the pixel size in tree
   */
  Dimension getSize(TreeModel model) {
    return dim;
  }

  /**
   * Draws a Plus in a tree
   */
  boolean paint(TreeGraphics g, boolean clearBG, boolean isDetail) {

    // Drawing of folding
    if ((!g.isFolding)||(!isDetail)) {
      return false;
    }

    // Calc some parms
    Dimension s = getSize(g.getModel());

    int pig=getPosInGen()   ,
        dit=getDepthInTree();

    // Check Visibility
    if (!g.isVisible(dit,pig,s)) {
      return false;
    }

    // Draw Plus-Sign
    ImgIcon img;
    if (getEntity()==null) {
      img=Images.imgMore;
    } else {
      img= stopped ? Images.imgPlus : Images.imgMinus;
    }

    g.drawImage(img,dit-dim.height/2,pig-dim.width/2);

    // Done
    return true;

  }          
}
