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
package genj.gedcom.time;

import genj.gedcom.GedcomException;


/**
 * Our own french republican
 */
public class FrenchRCalendar extends Calendar {
  
  /* valid from 22 SEP 1792 to not including 1 JAN 1806 */
  private static final int
    AN_I  = PointInTime.GREGORIAN.toJulianDay(22-1, 9-1, 1792),
    UNTIL = PointInTime.GREGORIAN.toJulianDay( 1-1, 1-1, 1806);

  private static final String MONTHS[] 
   = { "VEND","BRUM","FRIM","NIVO","PLUV","VENT","GERM","FLOR","PRAI","MESS","THER","FRUC","COMP" };
  
  private static final int[] LEAP_YEARS
   = { 3,7,11 };
   
  private static final String YEARS_PREFIX = "An ";
   
  private static final String[] YEARS 
   = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV" };
  
  /**
   * Constructor
   */
  protected FrenchRCalendar() {
    super("@#DFRENCH R@" , "french", "images/FrenchR.gif", FrenchRCalendar.MONTHS);
  }
  
  /**
   * @see genj.gedcom.PointInTime.Calendar#getDays(int, int)
   */
  public int getDays(int month, int year) {
    // standard month has 30 days
    if (month<12)
      return 30;
      
    // 5/6 jours complémentaires
    return isLeap(year) ? 6 : 5;
    
    // noop
  }
  
  /**
   * Leap year test
   */
  private boolean isLeap(int year) {
    for (int l=0;l<FrenchRCalendar.LEAP_YEARS.length;l++)
      if (FrenchRCalendar.LEAP_YEARS[l]==year) return true; 
    return false;
  }
  
  /**
   * @see genj.gedcom.PointInTime.Calendar#toJulianDay(genj.gedcom.PointInTime)
   */
  protected int toJulianDay(int d, int m, int y) throws GedcomException {
    // calc days
    int jd = FrenchRCalendar.AN_I + 365*(y-1) + m*30 + d;
    // check leap years (one less day on julian day)
    for (int l=0;l<FrenchRCalendar.LEAP_YEARS.length;l++)
      if (y>FrenchRCalendar.LEAP_YEARS[l]) jd++; 
    // check range
    if (jd<FrenchRCalendar.AN_I)
      throw new GedcomException(PointInTime.resources.getString("frenchr.bef"));
    if (jd>=FrenchRCalendar.UNTIL)
      throw new GedcomException(PointInTime.resources.getString("frenchr.aft"));
    // sum
    return jd;
  }
  
  /**
   * @see genj.gedcom.PointInTime.Calendar#toPointInTime(int)
   */
  protected PointInTime toPointInTime(int julianDay) throws GedcomException {

    // check range
    if (julianDay<FrenchRCalendar.AN_I)
      throw new GedcomException(PointInTime.resources.getString("frenchr.bef"));
    if (julianDay>=FrenchRCalendar.UNTIL)
      throw new GedcomException(PointInTime.resources.getString("frenchr.aft"));
    
    julianDay = julianDay - FrenchRCalendar.AN_I;
    
    // calculate years
    int 
      y  = julianDay/365 + 1,
      yr = julianDay%365;
      
    // check leap years (one less day on julian day)
    for (int l=0;l<FrenchRCalendar.LEAP_YEARS.length;l++)
      if (y>FrenchRCalendar.LEAP_YEARS[l]) yr--; 
      
    // calc month
    int
      m  = yr/30,
      mr = yr%30,
      d  = mr;
      
    // done
    return new PointInTime(d,m,y,this);
  }
  
  /**
   * @see genj.gedcom.PointInTime.Calendar#getYear(int, boolean)
   */
  public String getYear(int year, boolean localize) {
    if (!localize||year<1||year>FrenchRCalendar.YEARS.length)
      return super.getYear(year, localize);
    return YEARS_PREFIX+FrenchRCalendar.YEARS[year-1];
  }

  /**
   * Getting into hook to parse a valid year - check for our years
   * @see genj.gedcom.time.Calendar#getYear(java.lang.String)
   */
  public int getYear(String year) throws GedcomException {
    // strip any 'An '
    if (year.startsWith(YEARS_PREFIX))
      year = year.substring(YEARS_PREFIX.length());
    // look for years
    for (int y=0;y<YEARS.length;y++)
      if (YEARS[y].equals(year))
        return y+1;
    // let super do it
    return super.getYear(year);
  }

} //FrenchRCalendar