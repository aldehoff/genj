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

import genj.util.WordBuffer;

import java.util.StringTokenizer;

/**
 * Delta
 */
public class Delta implements Comparable {

  /** localizations */
  private final static String
    YEAR  = PointInTime.resources.getString("time.year"  ),
    YEARS = PointInTime.resources.getString("time.years" ),
    MONTH = PointInTime.resources.getString("time.month" ),
    MONTHS= PointInTime.resources.getString("time.months"),
    DAY   = PointInTime.resources.getString("time.day"   ),
    DAYS  = PointInTime.resources.getString("time.days"  );

  /** values */
  private int years, months, days;
  private Calendar calendar;
  
  /**
   * Constructor
   */
  public Delta(int d, int m, int y) {
    this(d,m,y,PointInTime.GREGORIAN);
  }
  
  /**
   * Constructor
   */
  public Delta(int d, int m, int y, Calendar c) {
    years = y;
    months= m;
    days  = d;
    calendar = c;
  }
  
  /**
   * Accessor - years
   */
  public int getYears() {
    return years;
  }
  
  /**
   * Accessor - months
   */
  public int getMonths() {
    return months;
  }
  
  /**
   * Accessor - days
   */
  public int getDays() {
    return days;
  }
  
  /**
   * Accessor - calendar
   */
  public Calendar getCalendar() {
    return calendar;
  }
  
  /**
   * Factory
   * @return Delta or null if n/a
   */
  public static Delta get(PointInTime earlier, PointInTime later) {

    // null check
    if (earlier==null||later==null) 
      return null;
         
    // valid?
    if (!earlier.isValid()||!later.isValid())
      return null;
    
    // same calendar?
    Calendar calendar = earlier.getCalendar();
    if (calendar!=later.getCalendar())
      return null;
      
    // ordering?
    if (earlier.compareTo(later)>0) {
      PointInTime p = earlier;
      earlier = later;
      later = p;
    }

    // grab earlier values  
    int 
      yearlier =           earlier.getYear (),
      mearlier = Delta.fix(earlier.getMonth()),
      dearlier = Delta.fix(earlier.getDay  ());
  
    // age at what point in time?
    int 
      ylater =           later.getYear (),
      mlater = Delta.fix(later.getMonth()),
      dlater = Delta.fix(later.getDay  ());
    
    // make sure years are not empty (could be on all UNKNOWN PIT)
    if (yearlier==PointInTime.UNKNOWN||ylater==PointInTime.UNKNOWN)
      return null;
    
    // calculate deltas
    int years  = ylater - yearlier;
    int months = mlater - mearlier;
    int days = dlater - dearlier;
    
    // check day
    if (days<0) {
      // decrease months
      months --;
      // increase days with days in previous month
      days = dlater + (calendar.getDays(mearlier, yearlier)-dearlier); 
    }
  
    // check month now<then
    if (months<0) {
      // decrease years
      years -=1;
      // increase months
      months += calendar.getMonths();
    } 
    
    // sanity check - 20040316 allowing years==months==days==0 now
    if (years<0||months<0||days<0)  
      return null;      

    // done
    return new Delta(days, months, years, calendar);
  }
  
  private static int fix(int i) {
    return i!=PointInTime.UNKNOWN ? i : 0;
  }
  
  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    Delta other = (Delta)o;
    // compare years
    int delta = years - other.years;
    if (delta != 0)
      return delta;
    // .. months
    delta = months - other.months;
    if (delta != 0)
      return delta;
    // .. days
    delta = days - other.days;
    return delta;
  }

  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    // no delta?
    if (years==0&&months==0&&days==0) {
      return "<1 "+Delta.DAY;
    }

    WordBuffer buffer = new WordBuffer();
    if (years >0) {
      buffer.append(years);
      buffer.append(years==1 ? Delta.YEAR : Delta.YEARS);
    } 
    if (months>0) {
      buffer.append(months);
      buffer.append(months==1 ? Delta.MONTH : Delta.MONTHS);
    } 
    if (days  >0) {
      buffer.append(days);
      buffer.append(days==1 ? Delta.DAY : Delta.DAYS);
    } 
    return buffer.toString();
  }
  
  /**
   * Gedcom value
   */
  public String getValue() {
    WordBuffer buffer = new WordBuffer();
    if (years >0) buffer.append(years+"y");
    if (months>0) buffer.append(months+"m");
    if (days  >0) buffer.append(days +"d");
    return buffer.toString();
  }
  
  /**
   * Gedcom value
   */
  public boolean setValue(String value) {

    // reset
    years = 0;
    months = 0;
    days = 0;

    // try to parse delta string tokens
    StringTokenizer tokens = new StringTokenizer(value);
    while (tokens.hasMoreTokens()) {
        
        String token = tokens.nextToken();
        int len = token.length();
        
        // check 1234x
        if (len<2) return false;
        for (int i=0;i<len-1;i++) {
            if (!Character.isDigit(token.charAt(i))) 
              return false;
        }
        
        int i;
        try {
          i = Integer.parseInt(token.substring(0, token.length()-1));;
        } catch (NumberFormatException e) {
          return false;
        }
        
        // check last
        switch (token.charAt(len-1)) {
            case 'y' : years = i; break;
            case 'm' : months= i; break;
            case 'd' : days  = i; break;
            default  : return false;
        }
    }

    // parsed!
    return true;
  }
  
} // Delta