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
    
    imgDelete,
    imgNew;

  /**
   * Constructor which pre-loads all images
   */
  private Images() {
    
    imgView      = new ImageIcon(this,"images/View.gif");

    imgStickOn   = new ImageIcon(this,"images/StickOn.gif");
    imgStickOff  = new ImageIcon(this,"images/StickOff.gif");
    imgReturn    = new ImageIcon(this,"images/Return.gif");
    
    imgDelete    = new ImageIcon(this,"images/entity/Delete.gif");
    imgNew       = new ImageIcon(this,"images/entity/New.gif");
    
  }
}
