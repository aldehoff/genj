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
import genj.util.Debug;

/**
 * A Link represents a box in a tree either to individual or family
 */
abstract class Link {

  /** the linked entity */
  private Entity entity;

  /** the depth in the tree */
  private int depthInTree;

  /** the position in the generation */
  private int posInGen;

  /**
   * Constructor
   */
  /*package*/ Link(int setDepthInTree,int setPosInGen,Entity setEntity) {
    entity=setEntity;
    depthInTree=setDepthInTree;
    posInGen=setPosInGen;
  }

  /**
   * Overridable action for single-click
   */
  /*package*/ abstract void clickIn(TreeModel model);

  /**
   * Overridable action for double-click
   */
  /*package*/ abstract void dclickIn(TreeModel model);

  /**
   * Returns the color for this link
   */
  protected Color getColor(TreeGraphics tg) {
    if (getEntity() == null) {
      return new Color(240,240,240);
    }
    if (tg.isRoot(getEntity())) {
      return Color.red;
    }
    return Color.black;
  }

  /**
   * Returns the logical depth position
   */
  /*package*/ int getDepthInTree() {
    return depthInTree;
  }

  /**
   * Returns the referenced entity
   */
  /*package*/ Entity getEntity() {
    return entity;
  }

  /**
   * Returns the logical position in generation
   */
  /*package*/ int getPosInGen() {
    return posInGen;
  }

  /**
   * Has to be overriden and returns proxies for this link
   */
  /*package*/ abstract Proxy[] getProxies(TreeGraphics g);

  /**
   * Returns the pixel size in tree
   */
  /*package*/ abstract Dimension getSize(TreeModel model);

  /**
   * Returns wether shadow is drawn for this linkg
   */
  /*package*/ boolean isShadow(TreeGraphics tg) {
    return false;
  }

  /**
   * Move link by depth and position in generation
   */
  /*package*/ void moveBy(int depthChange,int posChange) {
    depthInTree+=depthChange;
    posInGen+=posChange;
  }

  /**
   * Draws a visible link in a tree -
   * returns true if drawn in area
   */
  /*package*/ boolean paint(TreeGraphics g, boolean clearBG, boolean drawProps) {

    // Calc some parms
    Dimension s = getSize(g.getModel());

    int pig=getPosInGen(),
        dit=getDepthInTree();

    // Check Visibility
    if (!g.isVisible(dit,pig,s)) {
      return false;
    }

    // Draw Shadow
    if (isShadow(g)) {
      g.setColor(new Color(128,128,128));
      g.drawShadow(dit,pig,s.width,s.height);
    }

    if (isShadow(g)||clearBG) {
      g.setColor(Color.white);
      g.fillRect(dit,pig,s.width,s.height);
    }

    // Is this the root of the tree ?
    g.setColor(getColor(g));

    // Draw Link's box
    g.drawRect(dit, pig, s.width , s.height );

    // Draw Properties
    if ((entity==null)||(!drawProps)) {
      return true;
    }

    Proxy[] proxies = getProxies(g);
    if (proxies==null) {
      return true;
    }

    g.setColor(Color.black);

    Property p;
    for (int i=0;i<proxies.length;i++) {

      p = entity.getProperty().getProperty(proxies[i].getPath(),true);

      if (p==null) {
        continue;
      }

      // Clip graphics
      Rectangle r = proxies[i].getBox();
      r.translate(-s.width/2,-s.height/2);
      g.clip(dit,pig,r);

      // Draw actual property
      try {
        proxies[i].render(p,dit,pig,s.width,s.height,g);
      } catch (ClassCastException e)  {
        Debug.log(Debug.WARNING, this,"Proxy "+proxies[i]+" can't render "+p);
      }

      // Restore clip
      g.restoreClip();

      // Next
    }

    // Done
    return true;
  }

  /**
   * Remembers the given entity
   */
  /*package*/ void setEntity(Entity setEntity) {
    entity=setEntity;
  }

  /**
   * Sets link's position in generation
   */
  /*package*/ void setPosInGen(int pos) {
    posInGen=pos;
  }
}
