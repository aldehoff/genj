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
import genj.util.*;

/**
 * A graphics context for drawing in the realm of a timeline
 */
class TimelineGraphics {

  private Graphics     graphics;
  private int          level   ;
  private FontMetrics  fm      ;
  private int          y       ;
  private int          minYear ,maxYear;
  private int          pixelsPerYear;
  private Rectangle    clip;

  float        leftYear, rightYear;
  boolean      isPaintTags, isPaintDates;

  /**
   * Constructor
   */
  /*package*/ TimelineGraphics(Graphics setGraphics, int setMinYear, int setMaxYear, int setPixelsPerYear, boolean setPaintTags, boolean setPaintDates) {

    // Setup
    graphics      = setGraphics;
    fm            = graphics.getFontMetrics();
    minYear       = setMinYear;
    maxYear       = setMaxYear;
    pixelsPerYear = setPixelsPerYear;
    clip          = setGraphics.getClipBounds();
    leftYear      = pixel2year(clip.x);
    rightYear     = pixel2year(clip.x+clip.width);
    level         = 0;
    isPaintTags   = setPaintTags;
    isPaintDates  = setPaintDates;
    // Done
  }

  /**
   * Checks for visible year-range
   */
  /*package*/ boolean areVisibleYears(float fromYear, float toYear) {
    return ( (fromYear<rightYear) && (toYear>leftYear) );
  }

  /**
   * Helper that draws little cross
   */
  /*package*/ void drawCross(float year) {
    int x = year2pixel(year);
    graphics.drawLine(x-2,y  ,x+2,y  );
    graphics.drawLine(x  ,y-2,x  ,y+2);
  }

  /**
   * Helper that draws end of time
   */
  /*package*/ void drawEnd(float year) {
    int x = year2pixel(year);
    graphics.drawLine(x,y-2,x,y+2);
    graphics.drawLine(x-pixelsPerYear,y,x,y);
  }

  /**
   * Draws an image
   */
  /*package*/ void drawImage(ImgIcon img, float year,  int xoff, int yoff) {
    img.paintIcon(null, graphics, year2pixel(year)+xoff, y+yoff );
  }

  /**
   * Helper that draws range
   */
  /*package*/ void drawRange(float year1, float year2) {
    int x1 = year2pixel(year1);
    int x2 = year2pixel(year2);
    graphics.drawLine(x1,y-2,x1,y+2);
    graphics.drawLine(x2,y-2,x2,y+2);
    graphics.drawLine(x1,y  ,x2,y  );
  }

  /**
   * Helper that draws start of time
   */
  /*package*/ void drawStart(float year) {
    int x = year2pixel(year);
    graphics.drawLine(x,y-2,x,y+2);
    graphics.drawLine(x,y,x+pixelsPerYear,y);
  }

  /**
   * Helper that draws a string
   */
  /*package*/ void drawString(String txt, float year, float maxYear, int xoff, int yoff) {

    // Calculate pixels
    int x    = year2pixel(year)+xoff;

    // Calculate text
    if (maxYear!=0) {

      // Calculate clip
      int mw = year2pixel(maxYear) - x - 4;
      graphics.clipRect(x,0,mw,4*1024);
      // Draw it
      graphics.drawString(txt, x , y+yoff);
      // Restore clip
      graphics.setClip(clip.x,clip.y,clip.width,clip.height);

    } else {
      // Draw it
      graphics.drawString(txt, x , y+yoff);
    }
  }

  /**
   * Increments current level
   */
  /*package*/ void incLevel() {
    level++;
    y = (level*fm.getHeight()*2-fm.getHeight());
  }

  /**
   * Checks for visible current level
   */
  /*package*/ boolean isVisibleLevel() {
    return ( (y>0) && (y-fm.getHeight()<clip.height) );
  }

  /**
   * Transforms a pixel into year
   */
  /*package*/ float pixel2year(int pixel) {

    float y = pixel / (float)pixelsPerYear + minYear;
    return y;
  }

  /**
   * Transforms a year into x coordinate
   */
  /*package*/ int year2pixel(float year) {
    int x = (int)( (year - minYear) * pixelsPerYear );
    return x;
  }
}
