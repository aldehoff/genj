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

import genj.util.ImgIcon;

/**
 * A wrapper to the images we use in the TreeView
 */
final class Images {

  private static Images instance = new Images();

  static ImgIcon
    imgMinus,
    imgPlus,
    imgMore,
    imgStickOn,
    imgStickOff,
    imgIndisFields,
    imgFamsFields,
    imgOverview;

  /**
   * Constructor which pre-loads all images
   */
  private Images() {

    imgMinus       = new ImgIcon(this,"images/Minus.gif"      );
    imgPlus        = new ImgIcon(this,"images/Plus.gif"       );
    imgMore        = new ImgIcon(this,"images/More.gif"       );
    imgStickOn     = new ImgIcon(this,"images/StickOn.gif"    );
    imgStickOff    = new ImgIcon(this,"images/StickOff.gif"   );
    imgIndisFields = new ImgIcon(this,"images/IndisFields.gif");
    imgFamsFields  = new ImgIcon(this,"images/FamsFields.gif" );
    imgOverview    = new ImgIcon(this,"images/Overview.gif"   );
  }
}
