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

import genj.util.WordBuffer;

import java.util.Calendar;

/**
 * A point in time
 */
public abstract class PointInTime implements Comparable {

  /** month names */
  private final static String MONTHS[] = {"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};

  /**
   * Returns the year
   */
  public abstract int getYear();
  
  /**
   * Returns the month
   */
  public abstract int getMonth();

  /**
   * Returns the day
   */
  public abstract int getDay();

  /**
   * Accessor to an immutable point in time
   */
  public static PointInTime get(int d, int m, int y) {
    return new Immutable(d, m, y);
  }
  
  /**
   * Accessor to an immutable point in time
   */
  public static PointInTime getNow() {
    Calendar now = Calendar.getInstance(); // default to current time
    return get(
      now.get(Calendar.DATE)-1,      
      now.get(Calendar.MONTH),      
      now.get(Calendar.YEAR)
    );      
  }  

  /**
   * Setter (implementation dependant)
   */
  public void set(int d, int m, int y) {
    throw new IllegalArgumentException("not supported");
  }

  /**
   * Checks for validity
   */
  public boolean isValid() {

    // YYYY or MMM YYYY or DD MMMM YYYY
    int year = getYear();
    if (year<0)
      return false;
    int month = getMonth();
    if (month>=12)
      return false;
    int day = getDay();
    if (month<0&&day>=0)
      return false;
    return true;
  }
    
  

  /**
   * compare to other
   */  
  public int compareTo(Object o) {
    
    PointInTime other = (PointInTime)o;

    int result;
      
    // Year ?
    if ((result=getYear()-other.getYear())!=0) return result;
      
    // Month
    if ((result=getMonth()-other.getMonth())!=0) return result;
      
    // Day
    if ((result=getDay()-other.getDay())!=0) return result;
      
    // Equal
    return 0;
  }    

  /**
   * String representation
   */
  public String toString() {
    return toString(new WordBuffer(),true).toString();
  }
    
  /**
   * String representation
   */
  public WordBuffer toString(WordBuffer buffer, boolean localize) {
    int 
      day = getDay(),
      month = getMonth(),
      year = getYear();
      
    if (day>0) buffer.append(new Integer(day));
    
    buffer.append(getMonth(localize));
    
    if (year>0) buffer.append(new Integer(year));
    
    return buffer;
  }

  /**
   * Returns a double representation
   */
  public double toDouble() {

    double result = 0;
      
    int year = getYear();
    if (year>=0) {
      result = year;
      int month = getMonth();
      if (month>=0&&month<12) {
        result += ((double)month)/12;
        int day = getDay();
        if (day>=0&&day<31) {
          result += ((double)day)/12/31;
        } 
      }
    }
       
    return result;
  }
    
  /**
   * Returns the localized month as string (either MAY or Mai)
   */
  public String getMonth(boolean localize) {
    int month = getMonth();
    if (month<0||month>=12)
      return "";
    String mmm = MONTHS[month];
    if (localize) mmm = Gedcom.getResources().getString("prop.date.mon."+mmm);
    return mmm;
  }

  /**
   * Helper that transforms month to Integer
   */
  public static int getMonth(String mmm) throws NumberFormatException {
    for (int i=0;i<MONTHS.length;i++) {
      if (MONTHS[i].equalsIgnoreCase(mmm)) return i;
    }
    throw new NumberFormatException();
  }

  /**
   * Helper which returns given date in gedcom string-format
   */
  public static String getDateString(Calendar c) {

    return c.get(Calendar.DAY_OF_MONTH)
      + " " + MONTHS[c.get(Calendar.MONTH)]
      + " " + c.get(Calendar.YEAR);

  }

  /**
   * Calculate delta (1y 2m 3d)
   * @return delta as string (e.g. 1y 2m 3d) or empty string 
   */
  public String getDelta(PointInTime other) {
    return getDelta(this, other);
  }
  
  /**
   * Calculate delta (1y 2m 3d)
   * @return delta as string (e.g. 1y 2m 3d) or empty string 
   */
  public static String getDelta(PointInTime earlier, PointInTime later) {

    // valid?
    if (!earlier.isValid()||!later.isValid())
      return "";
      
    // ordering?
    if (earlier.compareTo(later)>0) {
      PointInTime p = earlier;
      earlier = later;
      later = p;
    }

    // grab earlier values  
    int 
      yearlier = earlier.getYear(),
      mearlier = Math.max(0,earlier.getMonth()),
      dearlier = Math.max(0,earlier.getDay  ());
  
    // age at what point in time?
    int 
      ylater = later.getYear(),
      mlater = Math.max(0, later.getMonth()),
      dlater = Math.max(0, later.getDay  ());
    
    // calculate deltas
    int 
      ydelta = ylater - yearlier,
      mdelta = mlater - mearlier,
      ddelta = dlater - dearlier;
    
    // check day
    if (ddelta<0) {
      // decrease months
      mdelta -=1;
      // increase days with days in previous month
      Calendar c = Calendar.getInstance();
      c.set(yearlier, mearlier, 1);
      int days = c.getActualMaximum(Calendar.DATE);
      ddelta = dlater + (days-dearlier); 
    }
  
    // check month now<then
    if (mdelta<0) {
      // decrease years
      ydelta -=1;
      // increase months
      mdelta +=12;
    } 
  
    // calculate output
    WordBuffer buffer = new WordBuffer();
    if (ydelta>0) buffer.append(ydelta+"y");
    if (mdelta>0) buffer.append(mdelta+"m");
    if (ddelta>0) buffer.append(ddelta+"d");
    return buffer.toString();    
  }

  
  /**
   * a default impl
   */
  private static class Immutable extends PointInTime {
    /** values */
    private int day,month,year;
    /**
     * Constructor
     */
    private Immutable(int d, int m, int y) {
      day = d;
      month = m;
      year = y;
    }
    /**
     * @see genj.gedcom.PointInTime#getDay()
     */
    public int getDay() {
      return day;
    }

    /**
     * @see genj.gedcom.PointInTime#getMonth()
     */
    public int getMonth() {
      return month;
    }

    /**
     * @see genj.gedcom.PointInTime#getYear()
     */
    public int getYear() {
      return year;
    }

  } //PIT

} //PointInTime