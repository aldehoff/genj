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
package genj.print;

import java.awt.*;

import genj.gedcom.*;

/**
 * A type for wrapping printing properties
 */
public class PrintProperties {

  public final static int
              LEFTMARGIN       = 1,
              RIGHTMARGIN      = 2,
              TOPMARGIN        = 3,
              BOTTOMMARGIN     = 4,
              PAGEWIDTH        = 5,
              PAGEHEIGHT       = 6,
              INNERPAGEWIDTH   = 7,
              INNERPAGEHEIGHT  = 8,
              DPI              = 9;

  public final static int
              PAGE = 1,
              INNERPAGE = 2;

  public final static float
              INCH = 2.54F;

  private static final float
              defMarginLeft=1.5F,
              defMarginRight=1.5F,
              defMarginTop=1.0F,
              defMarginBottom=1.0F;

  /** print title */
  private String title;

  /** print margins */
  private float marginLeft,marginRight,marginTop,marginBottom;

  /** print job */
  private PrintJob pjob;

  /**
   * Constructor
   */
  public PrintProperties(String title) {
    this(title,defMarginLeft,defMarginRight,defMarginTop,defMarginBottom);
  }

  /**
   * Constructor
   */
  public PrintProperties(String title, float marginLeft, float marginRight, float marginTop, float marginBottom) {

    this.title        = title;
    this.marginLeft   = marginLeft;
    this.marginRight  = marginRight;
    this.marginTop    = marginTop;
    this.marginBottom = marginBottom;
  }

  /**
   * Returns dimension in pixels
   * @param which specifies PAGE or INNERPAGE
   */
  public Dimension getPixelSize(int which) {

    switch (which) {
      case PAGE   :
      return new Dimension(getPixelValue(PAGEWIDTH) ,getPixelValue(PAGEHEIGHT));
      case INNERPAGE :
      return new Dimension(getPixelValue(INNERPAGEWIDTH),getPixelValue(INNERPAGEHEIGHT));
    }

    throw new IllegalArgumentException("Unknown size");
  }

  /**
   * Returns value in pixels
   * @param which specifies PAGEWIDTH, PAGEHEIGHT, INNERPAGEHEIGHT, INNERPAGEWIDTH or (TOP|LEFT|RIGHT|BOTTOM)MARGIN
   */
  public int getPixelValue(int which) {

    // Specials ?
    switch (which) {
      case DPI             :
      return pjob.getPageResolution()      ;
      case PAGEWIDTH       :
      return pjob.getPageDimension().width ;
      case PAGEHEIGHT      :
      return pjob.getPageDimension().height;
      case INNERPAGEWIDTH  :
      return getPixelValue(PAGEWIDTH ) - getPixelValue(LEFTMARGIN) - getPixelValue(RIGHTMARGIN );
      case INNERPAGEHEIGHT :
      return getPixelValue(PAGEHEIGHT) - getPixelValue(TOPMARGIN ) - getPixelValue(BOTTOMMARGIN);
    }

    // Normal
    float result = getValue(which);
    return (int)( result * pjob.getPageResolution() / INCH );

  }

  /**
   * Returns the Title of this properties
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns value in cm/inch
   */
  public float getValue(int which) {

    switch (which) {
      case LEFTMARGIN      :
        return marginLeft  ;
      case RIGHTMARGIN     :
        return marginRight ;
      case TOPMARGIN       :
        return marginTop   ;
      case BOTTOMMARGIN    :
        return marginBottom;
      case PAGEWIDTH       :
        return ((float)pjob.getPageDimension().width ) / pjob.getPageResolution()  * INCH;
      case PAGEHEIGHT      :
        return ((float)pjob.getPageDimension().height) / pjob.getPageResolution() * INCH;
      case INNERPAGEWIDTH  :
        return getValue(PAGEWIDTH ) - getValue(LEFTMARGIN) - getValue(RIGHTMARGIN );
      case INNERPAGEHEIGHT :
        return getValue(PAGEHEIGHT) - getValue(TOPMARGIN ) - getValue(BOTTOMMARGIN);
    }

    throw new IllegalArgumentException("Unknown value");
  }

  /**
   * Tells properties which printjob to use
   */
  public void setPrintJob(PrintJob pjob) {
    this.pjob = pjob;
  }

  /**
   * Sets value in cm/inch
   */
  public void setValue(int which, float value) {

    switch (which) {
      case LEFTMARGIN      :
        marginLeft  = Math.min(getValue(PAGEWIDTH )/3,Math.max(0,value));
        return;
      case RIGHTMARGIN     :
        marginRight = Math.min(getValue(PAGEWIDTH )/3,Math.max(0,value));
        return;
      case TOPMARGIN       :
        marginTop   = Math.min(getValue(PAGEHEIGHT)/3,Math.max(0,value));
        return;
      case BOTTOMMARGIN    :
        marginBottom= Math.min(getValue(PAGEHEIGHT)/3,Math.max(0,value));
        return;
    }

    throw new IllegalArgumentException("Unknown value to set");
  }          
}
