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

import genj.util.swing.ImageIcon;

/**
 * Wrapper for package used Images
 */
final public class Images {

  private static Images instance = new Images();

  public static ImageIcon
  
    imgView,
    
    imgStickOn,
    imgStickOff,
    imgReturn,
    imgFind,
    imgLink,
    imgUp,
    imgDown,
    imgAdd,
    imgRemove,
    imgIndi,
    
    imgDelete,
    imgNewFam,
    imgNewIndi,
    imgNewMedia,
    imgNewNote,
    imgNewRepository,
    imgNewSource,
    imgNewSubmitter;

  /**
   * Constructor which pre-loads all images
   */
  private Images() {
    
    imgView      = new ImageIcon(this,"images/View.gif");

    imgStickOn   = new ImageIcon(this,"images/StickOn.gif");
    imgStickOff  = new ImageIcon(this,"images/StickOff.gif");
    imgFind      = new ImageIcon(this,"images/Find.gif");
    imgLink      = new ImageIcon(this,"images/Link.gif");
    imgReturn    = new ImageIcon(this,"images/Return.gif");
    imgUp        = new ImageIcon(this,"images/Up.gif");
    imgDown      = new ImageIcon(this,"images/Down.gif");
    imgAdd       = new ImageIcon(this,"images/Add.gif");
    imgRemove    = new ImageIcon(this,"images/Remove.gif");
    
    imgDelete        = new ImageIcon(this,"images/entity/Delete.gif");
    imgNewFam        = new ImageIcon(this,"images/entity/NewFam.gif");
    imgNewIndi       = new ImageIcon(this,"images/entity/NewIndi.gif");
    imgNewMedia      = new ImageIcon(this,"images/entity/NewMedia.gif");
    imgNewNote       = new ImageIcon(this,"images/entity/NewNote.gif");
    imgNewRepository = new ImageIcon(this,"images/entity/NewRepository.gif");
    imgNewSource     = new ImageIcon(this,"images/entity/NewSource.gif");
    imgNewSubmitter  = new ImageIcon(this,"images/entity/NewSubmitter.gif");
  }
}
