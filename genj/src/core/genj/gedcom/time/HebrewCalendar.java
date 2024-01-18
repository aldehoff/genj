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
 * Our own hebrew republican
 */
public class HebrewCalendar extends Calendar {

 /* 
   Month    def reg perf  
   Tishri   30  30  30  
   Kheshvan 29  29  30  
   Kislev   29  30  30  
   Tevet    29  29  29   
   Schevat  30  30  30   
  (Adar r   30  30  30) only in leap year
   Adar s   29  29  29  
   Nisan    30  30  30  
   Iyyar    29  29  29  
   Sivan    30  30  30  
   Tammuz   29  29  29  
   Av       30  30  30  
   Elul     29  29  29  

   total   353 354 355 (+30 in leap year)      
 */

  /**
   * the calendar begins at sunset the night before 
   * Monday, October 7, 3761 B.C.E. (Julian calendar)
   * Monday, September 9, 3761 B.C.E (Gregorian calendar)
   * Julian day 347997.5.
   */
  private static final int 
    ANNO_MUNDI = 347997;

  private static final String[] MONTHS 
   = { "TSH","CSH","KSL","TVT","SHV","ADR","ADS","NSN","IYR","SVN","TMZ","AAV","ELL" };

  /**
   * Constructor
   */
  protected HebrewCalendar() {
    super("@#DHEBREW@", "hebrew", "images/Hebrew.gif", HebrewCalendar.MONTHS);
  }
  
  /**
   * Julian Day -> PIT
   */
  protected PointInTime toPointInTime(int julianDay) throws GedcomException {
    // FIXME transformation Julian Day to Hebrew is missing
    throw new GedcomException("Transformation to Hebrew Calendar not implemented yet");
  }
  
  /**
   * d,m,y -> Julian Day
   */
  protected int toJulianDay(int day, int month, int year) throws GedcomException {

    // year ok?
    if (year<1)
      throw new GedcomException("Hebrew calendar has to start with 1");

    // make sure Adar R is in leap year
    if (month==5&&!isLeap(year))
      throw new GedcomException("There's no Adar R in non-leap year "+year);
  
    // get tishri1 for year
    int jd = getTishri1(year);
    
    // add months
    for (int m=0;m<month;m++)
      jd += getDays(m, year);
    
    // add days
    jd += day;
    
    // add 1 because hebrew day starts at 18:00
    jd ++;
    
    // done
    return jd;
  }
  
  /**
   * Calculates Tishri 1 (first day of Tishri) in given year
   * @return tishri1 in julian day
   */
  private int getTishri1(int year) {

    // need tishri one (adjusted) for last, this and next year
    int
      last    = _getTishri1(year-1),
      tishri1 = _getTishri1(year  ),
      next    = _getTishri1(year+1);
   
    // adjust due to length of adjacent years
    if (next-tishri1==356)
      tishri1 += 2;
    else if (tishri1-last==382)
      tishri1 ++;
      
    // done
    return tishri1;   
  }
    
  private int _getTishri1(int year) {
        
    // In general the 1st of Tishri of that year avoids
    // Sunday, Wednesday, and Friday
    
    int months = ((235 * year) - 234) / 19;
    int parts = 12084 + (13753 * months);
    int jd = (months * 29) + parts / 25920;

    if ( (3 * (jd + 1))%7 < 3) {
      jd++;
    }

    // done if first day of calendar is added
    return HebrewCalendar.ANNO_MUNDI + jd;
  }

  /**
   * @see genj.gedcom.PointInTime.Calendar#getDays(int, int)
   */
  public int getDays(int month, int year) {
    
    //  easy for the months fixed to 29
    switch (month) {
      case  3: //TVT
      case  6: //ADS
      case  8: //IYR
      case 10: //TMZ
      case 12: //ELL
        return 29;
      case  1: //CSH - depends on length of year
        if (getDays(year)%10!=5)
          return 29;
        break; 
      case  2: //KSL - depends on length of year
        if (getDays(year)%10==3)
          return 29;
        break;
    }   

    // standard is 30
    return 30;
  }
  
  private int getDays(int year) {
    try {
      return toJulianDay(1,1,year+1) - toJulianDay(1,1,year); 
    } catch (Throwable t) {
      // shouldn't happen
      throw new RuntimeException();
    }
  }
  
  /**
   * whether a given year is a leap year 
   *  0, 3, 6, 8, 11, 14, 17 in metonic cycle (mod 19)
   */
  private boolean isLeap(int year) {
    return (14*7+1)%19<7;    
  }
  
  /**
   * number of months in given year
   */
  private int getMonths(int year) {
    return isLeap(year) ? 13 : 12;
  }
  
} //HebrewCalendar