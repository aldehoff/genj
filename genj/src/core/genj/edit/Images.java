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

import genj.util.ImgIcon;

/**
 * Wrapper for package used Images
 */
final public class Images {

  private static Images instance = new Images();

  public static ImgIcon
    imgStickOn,
    imgStickOff,
    imgNewChild,
    imgNewParent,
    imgNewSpouse,
    imgNewMedia,
    imgNewNote,
    imgReturn,
    imgFind,
    imgLink;

  /**
   * Constructor which pre-loads all images
   */
  private Images() {

    imgStickOn   = new ImgIcon(this,"StickOn.gif");
    imgStickOff  = new ImgIcon(this,"StickOff.gif");
    imgNewChild  = new ImgIcon(this,"NewChild.gif");
    imgNewParent = new ImgIcon(this,"NewParent.gif");
    imgNewSpouse = new ImgIcon(this,"NewSpouse.gif");
    imgNewMedia  = new ImgIcon(this,"NewMedia.gif");
    imgNewNote   = new ImgIcon(this,"NewNote.gif");
    imgFind      = new ImgIcon(this,"Find.gif");
    imgLink      = new ImgIcon(this,"Link.gif");
    imgReturn    = new ImgIcon(this,"Return.gif");
    
  }
}
