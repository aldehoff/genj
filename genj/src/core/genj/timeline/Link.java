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
package genj.timeline;

import java.awt.*;
import java.util.*;

import genj.gedcom.*;
import genj.util.ImgIcon;

/**
 * A Link represents a marker on the timeline
 */
/*package*/ abstract class Link {

  /** members */
  PropertyEvent pevent;
  PropertyDate  pdate;
  float fromYear,toYear;

  /**
   * Constructor
   */
  Link(PropertyEvent pevent, float fromYear, float toYear) {

    // Remember
    this.pevent  =pevent;
    this.pdate   =pevent.getDate();
    this.fromYear=fromYear;
    this.toYear  =toYear;

    // Done
  }

  /**
   * Paints this link
   * @param g Graphics to draw on
   * @param to Link that shows end of space to draw
   */
  void drawOn(TimelineGraphics g, float maxYear) {

    // Draw marks and lines
    int type = pdate.getFormat();

    switch (type) {
    case PropertyDate.DATE:
    case PropertyDate.ABT:
    case PropertyDate.CAL:
    case PropertyDate.EST:
      g.drawCross(fromYear);
      break;
    case PropertyDate.FROMTO:
    case PropertyDate.BETAND:
      g.drawRange(fromYear,toYear);
      break;
    case PropertyDate.FROM:
    case PropertyDate.AFT:
      g.drawStart(fromYear);
      break;
    case PropertyDate.TO:
    case PropertyDate.BEF:
      g.drawEnd(fromYear);
      break;
    }

    // Draw image
    ImgIcon img = pevent.getImage(false);
    g.drawImage(img, fromYear, 4, -img.getIconHeight() );

    // Draw text
    String txt = getText();

    if (g.isPaintTags) {
      txt = pevent.getTag()+" of "+txt;
    }
    if (g.isPaintDates) {
      txt += " ("+pevent.getDate()+")";
    }

    g.drawString(txt, fromYear, maxYear, 4+img.getIconWidth(),0 );

    // done
  }

  /**
   *  Generates a link for given PropertyDate
   */
  static Link generateLink(PropertyEvent pevent) {

    // Check event's date
    PropertyDate date = pevent.getDate();
    if (date==null)
      return null;

    // Pre-parse date
    float fromYear=0,toYear=0;

    switch (date.getFormat()) {
    case PropertyDate.DATE:
    case PropertyDate.ABT:
    case PropertyDate.CAL:
    case PropertyDate.EST:
    case PropertyDate.FROM:
    case PropertyDate.AFT:
    case PropertyDate.TO:
    case PropertyDate.BEF:
      fromYear = getYearOf(date,0);
      toYear   = fromYear;
      break;
    case PropertyDate.FROMTO:
    case PropertyDate.BETAND:
      fromYear = getYearOf(date,0);
      toYear   = getYearOf(date,1);
      break;
    default:
      return null;
    }

    // Generate Link
    if (pevent.getEntity() instanceof Indi)
      return new LinkOfIndi(pevent, fromYear, toYear);
    if (pevent.getEntity() instanceof Fam)
      return new LinkOfFam (pevent, fromYear, toYear);

    // Error
    throw new RuntimeException("Unsupported Entity");
  }

  /**
   * Returns this link's width in TimelinePane
   */
  int getPixelWidth(FontMetrics fm) {
    return fm.stringWidth(getText());
  }

  /**
   * Entity dependent text for link description
   */
  abstract String getText();

  /**
   * Returns this link's starting position in TimelinePane
   */
  static float getYearOf(PropertyDate pdate, int which) {

    int y = pdate.getStart().getYear (0);
    int m = pdate.getStart().getMonth(0);

    return (float)y + ((float)m)/12;
  }          
}
