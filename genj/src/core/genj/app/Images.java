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
package genj.app;

import genj.util.ImgIcon;

/**
 * Wrapper for package used Images
 */
final public class Images {

  private static Images instance = new Images();

  public static ImgIcon
    imgDelEntity,
    imgNewFam,
    imgNewSource,
    imgGedcom,
    imgNewIndi,
    imgNewSubmitter,
    imgNewRepository,
    imgUndo,
    imgNewMedia,
    imgNewTable,
    imgNewTimeline,
    imgNewNote,
    imgNewTree,
    imgNewEdit,
    imgNewReport,
    imgNewNavigator,
    imgSettings,
    imgPrint,
    imgHelp;

  /**
   * Constructor which pre-loads all images
   */
  private Images() {

    imgDelEntity    = new ImgIcon(this,"images/DelEntity.gif");
    imgNewFam       = new ImgIcon(this,"images/NewFam.gif");
    imgNewSource    = new ImgIcon(this,"images/NewSource.gif");
    imgGedcom       = new ImgIcon(this,"images/Gedcom.gif");
    imgNewIndi      = new ImgIcon(this,"images/NewIndi.gif");
    imgNewSubmitter = new ImgIcon(this,"images/NewSubmitter.gif");
    imgNewRepository= new ImgIcon(this,"images/NewRepository.gif");
    imgUndo         = new ImgIcon(this,"images/Undo.gif");
    imgNewMedia     = new ImgIcon(this,"images/NewMedia.gif");
    imgNewTable     = new ImgIcon(this,"images/NewTable.gif");
    imgNewTimeline  = new ImgIcon(this,"images/NewTimeline.gif");
    imgNewNote      = new ImgIcon(this,"images/NewNote.gif");
    imgNewTree      = new ImgIcon(this,"images/NewTree.gif");
    imgNewEdit      = new ImgIcon(this,"images/NewEdit.gif");
    imgNewReport    = new ImgIcon(this,"images/NewReport.gif");
    imgNewNavigator = new ImgIcon(this,"images/NewNavigator.gif");
    imgSettings     = new ImgIcon(this,"images/Settings.gif");
    imgPrint        = new ImgIcon(this,"images/Print.gif");
    imgHelp         = new ImgIcon(this,"images/Help.gif");
  }
}
