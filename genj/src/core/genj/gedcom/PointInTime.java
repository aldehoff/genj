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
import java.util.StringTokenizer;

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
   * Returns the month (first month of year is 0=Jan)
   */
  public abstract int getMonth();

  /**
   * Returns the day (first day of month is 0)
   */
  public abstract int getDay();

  /**
   * Accessor to an immutable point in time
   * @param d day (zero based)
   * @param m month (zero based)
   * @param y year
   */
  public static PointInTime getPointInTime(int d, int m, int y) {
    return new Impl(d, m, y);
  }
  
  /**
   * Accessor to an immutable point in time - now
   */
  public static PointInTime getNow() {
    Calendar now = Calendar.getInstance(); // default to current time
    return getPointInTime(
      now.get(Calendar.DATE) - 1,      
      now.get(Calendar.MONTH),      
      now.get(Calendar.YEAR)
    );      
  }  
  
  /**
   * Accessor to an immutable point in time
   * @param string e.g. 25 MAY 1970
   */
  public static PointInTime getPointInTime(String string) {
    Impl result = new Impl(-1,-1,-1);
    result.set(new StringTokenizer(string));
    return result;
  }
  
  /**
   * Setter (implementation dependant)
   */
  public void set(int d, int m, int y) {
    throw new IllegalArgumentException("not supported");
  }
  
  /**
   * Setter
   */
  public boolean set(StringTokenizer tokens) {
    // Number of tokens ?
    switch (tokens.countTokens()) {
      default : // TOO MANY
        return false;
      case 0 : // NONE
        return false;
      case 1 : // YYYY
        try {
          set(-1,-1,Math.max(-1,Integer.parseInt(tokens.nextToken())));
        } catch (NumberFormatException e) {
          break;
        }
        return getYear()>=0;
      case 2 : // MMM YYYY
        try {
          set(-1, getMonth (tokens.nextToken()), Integer.parseInt( tokens.nextToken() ));
        } catch (NumberFormatException e) {
          break;
        }
        return getYear()>=0;
      case 3 : // DD MMM YYYY
        try {
          set(
            Integer.parseInt( tokens.nextToken() ) - 1,
            getMonth ( tokens.nextToken() ),
            Integer.parseInt( tokens.nextToken() )
          );
        } catch (NumberFormatException e) {
          break;
        }
        return getYear()>=0&&getDay()>=0&&getDay()<=31;
    }
    // didn't work
    return false;
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
   * String representation (Gedcom format)
   */
  public String toGedcomString() {
    return toString(new WordBuffer(),false).toString();
  }
    
  /**
   * String representation
   */
  public WordBuffer toString(WordBuffer buffer, boolean localize) {
    
    int 
      day = getDay(),
      month = getMonth(),
      year = getYear();
      
    if (year>0) {
      if (month>=0&&month<MONTHS.length) {
        if (day>=0) buffer.append(new Integer(day+1));
        buffer.append(getMonth(localize, true));
      }    
      buffer.append(new Integer(year));
    }
    
    return buffer;
  }

  /**
   * Returns the localized month as string (either MAY or Mai)
   */
  public String getMonth(boolean localize, boolean abbreviate) {
    int month = getMonth();
    if (month<0||month>=12)
      return "";
    String mmm = MONTHS[month];
    if (localize) mmm = Gedcom.getResources().getString("prop.date.mon."+mmm);
    if (abbreviate&&mmm.length()>3) mmm = mmm.substring(0,3);
    return mmm;
  }
  
  /**
   * Access to (localized) gedcom months 
   */
  public static String[] getMonths(boolean localize, boolean abbreviate) {
    String[] result = new String[12];
    for (int m=0;m<result.length;m++) {
      String mmm = MONTHS[m];
      if (localize) mmm = Gedcom.getResources().getString("prop.date.mon."+mmm);
      if (abbreviate&&mmm.length()>3) mmm = mmm.substring(0,3);
      result[m] = mmm;
    }
    return result;
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
   * Calculate delta of two times years,months,days
   */
  public static int[] getDelta(PointInTime earlier, PointInTime later) {

    // null check
    if (earlier==null||later==null) 
      return null;
           
    // valid?
    if (!earlier.isValid()||!later.isValid())
      return null;
        
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

    // check valid 
    if (ydelta<0||mdelta<0||ddelta<0||(ydelta+mdelta+ddelta==0))  
      return null;

    // done
    return new int[]{ ydelta, mdelta, ddelta };
  }

  /**
   * a default impl
   */
  private static class Impl extends PointInTime {
    
    /** values */
    private int day, month, year;
    
    /**
     * Constructor
     */
    private Impl(int d, int m, int y) {
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
    
    /**
     * @see genj.gedcom.PointInTime#set(int, int, int)
     */
    public void set(int d, int m, int y) {
      day = d;
      month = m;
      year = y;
    }

  } //PIT

} //PointInTime