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
 * A link represents a box in a tree : Family
 */
class LinkToFam extends Link {

  /** the family's children's links */
  private SetOfLinks children;

  /**
   * Constructor
   */
  /*package*/ LinkToFam(int setDepthInTree,int setPosInGen,Fam setFam) {
    super(setDepthInTree,setPosInGen,setFam);
    children = new SetOfLinks();
  }

  /**
   * Adds a link to child to this link
   */
  /*package*/ void addLinkToChild(Link link) {
    children.addLink(link);
  }

  /**
   * Action for click on PlusMinus
   */
  /*package*/ void clickIn(TreeModel model) {
    model.setActual( this );
  }

  /**
   * Action for doubleclick on PlusMinus
   */
  /*package*/ void dclickIn(TreeModel model) {
    model.setRoot( getEntity() );
  }

  /**
   * Returns the family linked by this
   */
  /*package*/ Fam getFam() {
    return (Fam)getEntity();
  }

  /**
   * Returns proxies for this link
   */
  /*package*/ Proxy[] getProxies(TreeGraphics tg) {
    return tg.famsProxies;
  }

  /**
   * Returns the pixel size in tree
   */
  /*package*/ Dimension getSize(TreeModel model) {
    return model.getSize(Gedcom.FAMILIES);
  }

  /**
   * Draws a visible family in a tree
   */
  /*package*/ boolean paint(TreeGraphics g, boolean clearBG, boolean drawProperties) {

    // Draw Fam's standard box
    boolean visible = super.paint(g,clearBG,drawProperties);

    // Check visibility
    if (!visible) {

      // .. standard box not visible ?
      if (children.getSize()==0) {
        // .. return when no children
        return false;
      }

      // ... calc dimension of box between family + children
      int pig1=java.lang.Math.min( children.getLink(1).getPosInGen()                , getPosInGen() ),
          pig2=java.lang.Math.max( children.getLink(children.getSize()).getPosInGen()  , getPosInGen() ),
          dit1=getDepthInTree(),
          dit2=children.getLink(1).getDepthInTree();

      // ... check if dimension lies in in rectangle
      if (!g.isVisible(dit1,pig1,dit2,pig2)) {
        return false;
      }

      // ... visible !
    }

    // Draw line(s) to children
    g.setColor(Color.black);

    if ( (getFam().getNoOfChildren()>0) && (children.getSize()>0) ) {

      int posInGen1 ,depthInTree1, posInGen2, depthInTree2;
      int stub=8;

      // ... small stub at family
      posInGen1   = getPosInGen()   +0;
      depthInTree1= getDepthInTree()+g.getPlusMinusOffset(Gedcom.FAMILIES);

      posInGen2   = posInGen1;
      depthInTree2= depthInTree1 + stub;

      g.drawLine(depthInTree1,posInGen1,depthInTree2,posInGen2);

      // ... indication of more children
      if (children.getSize() != getFam().getNoOfChildren()) {
        g.drawLine(depthInTree1+stub/2,posInGen1-stub,depthInTree1+stub/2,posInGen2+stub);
      }

      // ... diagonal lines
      depthInTree1= depthInTree2;

      for (int c=1;c<=children.getSize();c++) {
        // .. here it is
        posInGen2   = children.getLink(c).getPosInGen   ();
        depthInTree2= children.getLink(c).getDepthInTree() - g.getPlusMinusOffset(Gedcom.INDIVIDUALS) - stub;

        g.drawLine(depthInTree1,posInGen1,depthInTree2,posInGen2);

        // .. again small stub
        g.drawLine(depthInTree2,posInGen2,depthInTree2+stub,posInGen2);

        // .. next
      }
      // Done with children
    }
    // Done
    return true;
  }
}
