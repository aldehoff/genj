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
package genj.gedcom;

import genj.util.ImgIcon;

/**
 * A wrapper for the images that we use in this package
 */
final class Images {

  private static Images instance = new Images();

  /*package*/ static
    ImgIcon
    imgAttribute,
    imgDate,
    imgIndi,
    imgMarriage,
    imgPlus,
    imgBirth,
    imgDeath,
    imgLink2Fam,
    imgMinus,
    imgQuestion,
    imgCause,
    imgError,
    imgLink2Indi,
    imgSource,
    imgEvent,
    imgLinkXFam,
    imgName,
    imgSubmitter,
    imgCode,
    imgFam,
    imgLinkXIndi,
    imgNote,
    imgTime,
    imgCont,
    imgFemale,
    imgMale,
    imgPlace,
    imgType,
    imgMedia,
    imgBarBat,
    imgBurial,
    imgMigration,
    imgFormat,
    imgBlob,
    imgDisk,
    imgOccu,
    imgNati;

  /**
   * Constructor which pre-loads all images
   */
  private Images() {

    imgAttribute= new ImgIcon(this,"images/Attribute.gif");
    imgDate     = new ImgIcon(this,"images/Date.gif");
    imgIndi     = new ImgIcon(this,"images/Indi.gif");
    imgMarriage = new ImgIcon(this,"images/Marriage.gif");
    imgPlus     = new ImgIcon(this,"images/Plus.gif");
    imgBirth    = new ImgIcon(this,"images/Birth.gif");
    imgDeath    = new ImgIcon(this,"images/Death.gif");
    imgLink2Fam = new ImgIcon(this,"images/Link2Fam.gif");
    imgMinus    = new ImgIcon(this,"images/Minus.gif");
    imgQuestion = new ImgIcon(this,"images/Question.gif");
    imgCause    = new ImgIcon(this,"images/Cause.gif");
    imgError    = new ImgIcon(this,"images/Error.gif");
    imgLink2Indi= new ImgIcon(this,"images/Link2Indi.gif");
    imgSource   = new ImgIcon(this,"images/Source.gif");
    imgEvent    = new ImgIcon(this,"images/Event.gif");
    imgLinkXFam = new ImgIcon(this,"images/LinkXFam.gif");
    imgName     = new ImgIcon(this,"images/Name.gif");
    imgSubmitter= new ImgIcon(this,"images/Submitter.gif");
    imgCode     = new ImgIcon(this,"images/Code.gif");
    imgFam      = new ImgIcon(this,"images/Fam.gif");
    imgLinkXIndi= new ImgIcon(this,"images/LinkXIndi.gif");
    imgNote     = new ImgIcon(this,"images/Note.gif");
    imgTime     = new ImgIcon(this,"images/Time.gif");
    imgCont     = new ImgIcon(this,"images/Cont.gif");
    imgFemale   = new ImgIcon(this,"images/Female.gif");
    imgMale     = new ImgIcon(this,"images/Male.gif");
    imgPlace    = new ImgIcon(this,"images/Place.gif");
    imgType     = new ImgIcon(this,"images/Type.gif");
    imgMedia    = new ImgIcon(this,"images/Media.gif");
    imgBarBat   = new ImgIcon(this,"images/BarBat.gif");
    imgBurial   = new ImgIcon(this,"images/Burial.gif");
    imgMigration= new ImgIcon(this,"images/Migration.gif");
    imgFormat   = new ImgIcon(this,"images/Format.gif");
    imgBlob     = new ImgIcon(this,"images/Blob.gif"  );
    imgDisk     = new ImgIcon(this,"images/Disk.gif"  );
    imgOccu     = new ImgIcon(this,"images/Occupation.gif"  );
    imgNati     = new ImgIcon(this,"images/Nationality.gif"  );
  }
}
