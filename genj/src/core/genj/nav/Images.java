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
package genj.nav;

import genj.util.ImgIcon;

/**
 * Wrapper for package used Images
 */
final public class Images {

  private static Images instance = new Images();

  public static ImgIcon
    imgNavYoungerSiblingOn,
    imgNavYoungerSiblingOff,
    imgNavOlderSiblingOn,
    imgNavOlderSiblingOff,
    imgNavChildOn,
    imgNavChildOff,
    imgNavMotherOn,
    imgNavMotherOff,
    imgNavFatherOn,
    imgNavFatherOff,
    imgNavPartnerOn,
    imgNavPartnerOff;

  /**
   * Constructor which pre-loads all images
   */
  private Images() {
    imgNavYoungerSiblingOn   = new ImgIcon(this,"NavYoungerSiblingOn.gif");
    imgNavYoungerSiblingOff  = new ImgIcon(this,"NavYoungerSiblingOff.gif");
    imgNavOlderSiblingOn  = new ImgIcon(this,"NavOlderSiblingOn.gif");
    imgNavOlderSiblingOff = imgNavYoungerSiblingOff;
    imgNavChildOn         = new ImgIcon(this,"NavChildOn.gif");
    imgNavChildOff        = new ImgIcon(this,"NavChildOff.gif");
    imgNavFatherOn        = new ImgIcon(this,"NavFatherOn.gif");
    imgNavFatherOff       = new ImgIcon(this,"NavFatherOff.gif");
    imgNavMotherOn        = new ImgIcon(this,"NavMotherOn.gif");
    imgNavMotherOff       = imgNavFatherOff;
    imgNavPartnerOn       = new ImgIcon(this,"NavPartnerOn.gif");
    imgNavPartnerOff      = new ImgIcon(this,"NavPartnerOff.gif");
  }
}
