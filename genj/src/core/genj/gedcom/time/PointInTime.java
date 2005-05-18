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
import genj.util.Resources;
import genj.util.WordBuffer;

import java.util.StringTokenizer;

/**
 * A point in time - either hebrew, roman, frenchr, gregorian or julian
 */
public class PointInTime implements Comparable {
  
  /** resources */
  /*package*/ final static Resources resources = Resources.get(PointInTime.class);

  /** marker for unknown day,month,year */
  public final static int 
    UNKNOWN = Integer.MAX_VALUE;
  
  /** calendars */
  public final static GregorianCalendar GREGORIAN = new GregorianCalendar();
  public final static    JulianCalendar JULIAN    = new    JulianCalendar();
  public final static    HebrewCalendar HEBREW    = new    HebrewCalendar();
  public final static   FrenchRCalendar FRENCHR   = new   FrenchRCalendar();
    
  public final static Calendar[] CALENDARS = { GREGORIAN, JULIAN, HEBREW, FRENCHR };
  
  /** calendar */
  protected Calendar calendar = GREGORIAN;
  
  /** values */
  private int 
    day   = UNKNOWN, 
    month = UNKNOWN, 
    year  = UNKNOWN,
    jd    = UNKNOWN;

  /**
   * Constructor
   */
  public PointInTime() {
  }

  /**
   * Constructor
   */
  public PointInTime(Calendar cal) {
    calendar = cal;
  }
  
  /**
   * Constructor
   */
  public PointInTime(int d, int m, int y) {
    this(d,m,y,GREGORIAN);
  }
  
  /**
   * Constructor
   */
  public PointInTime(int d, int m, int y, Calendar cal) {
    day = d;
    month = m;
    year = y;
    calendar = cal;
    jd = UNKNOWN;
  }
  
  /**
   * Constructor
   */
  public PointInTime(String yyyymmdd) throws GedcomException {
    
    if (yyyymmdd==null||yyyymmdd.length()!=8)
      throw new GedcomException("no valid yyyymmdd: "+yyyymmdd);
    
    // check date
    try {
      year  = Integer.parseInt(yyyymmdd.substring(0, 4));
      month = Integer.parseInt(yyyymmdd.substring(4, 6))-1;
      day   = Integer.parseInt(yyyymmdd.substring(6, 8))-1;
    } catch (NumberFormatException e) {
      throw new GedcomException("no valid yyyymmdd: "+yyyymmdd);
    }

    // done
  }
    
  /**
   * Returns the calendar
   */
  public Calendar getCalendar() {
    return calendar;
  }

  /**
   * Returns the year
   */
  public int getYear() {
    return year;
  }
  
  /**
   * Returns the month (first month of year is 0=Jan)
   */
  public int getMonth() {
    return month;    
  }

  /**
   * Returns the day (first day of month is 0)
   */
  public int getDay() {
    return day;
  }
  
  /**
   * Accessor to an immutable point in time - now
   */
  public static PointInTime getNow() {
    java.util.Calendar now = java.util.Calendar.getInstance(); // default to current time
    return new PointInTime(
      now.get(java.util.Calendar.DATE) - 1,      
      now.get(java.util.Calendar.MONTH),      
      now.get(java.util.Calendar.YEAR)
    );      
  }  
  
  /**
   * Accessor to an immutable point in time
   * @param string e.g. 25 MAY 1970
   */
  public static PointInTime getPointInTime(String string) {
    PointInTime result = new PointInTime(UNKNOWN,UNKNOWN,UNKNOWN,GREGORIAN);
    result.set(new StringTokenizer(string));
    return result;
  }
  
  /**
   * Accessor to a point in time from system time long
   */
  public static PointInTime getPointInTime(long millis) {
    // January 1, 1970 = 0 millis = 2440588 julian
    long julian = 2440588 + (millis / 24 / 60 / 60 / 1000);
    return GREGORIAN.toPointInTime((int)julian);
  }
  
  /**
   * Returns the time millis representation
   */
  public long getTimeMillis() throws GedcomException {
    // January 1, 1970 = 0 millis = 2440588 julian
    return (getJulianDay()-2440588L) * 24*60*60*1000;
  }

  /**
   * Accessor to a calendar transformed copy 
   */
  public PointInTime getPointInTime(Calendar cal) throws GedcomException {
    if (calendar==cal)
      return this;
    PointInTime result = new PointInTime();
    result.set(this);
    result.set(cal);
    return result;
  }
  
  /**
   * Calculate julian day
   */
  public int getJulianDay() throws GedcomException {
    if (jd==UNKNOWN)
      jd = calendar.toJulianDay(this);
    return jd;
  }
  
  /**
   * Setter
   */
  public void reset() {
    set(UNKNOWN,UNKNOWN,UNKNOWN);
  }
  
  /**
   * Calculate day of week
   */
  public String getDayOfWeek(boolean localize) throws GedcomException {
    return calendar.getDayOfWeek(this, localize);
  }
  
  /**
   * Setter
   */
  public void set(Calendar cal) throws GedcomException {
    // is ok for 'empty' pit
    if (day==UNKNOWN&&month==UNKNOWN&&year==UNKNOWN) {
      calendar = cal;
      return;
    }
    // has to be valid
    if (!isValid())
      throw new GedcomException(resources.getString("pit.invalid"));
    // has to be complete
    if (!isComplete())
      throw new GedcomException(resources.getString("pit.incomplete"));
    // convert to julian date
    int jd = getJulianDay();
    // convert to new instance
    set(cal.toPointInTime(jd));
  }  
  
  /**
   * Setter
   */
  public void set(int d, int m, int y) {
    set(d,m,y,calendar);
  }
  
  /**
   * Setter (implementation dependant)
   */
  public void set(int d, int m, int y, Calendar cal) {
    day = d;
    month = m;
    year = y;
    calendar = cal;
    jd = UNKNOWN;
  }
  
  /**
   * Setter 
   */
  public void set(PointInTime other) {
    calendar = other.calendar;
    set(other.getDay(), other.getMonth(), other.getYear());
  }
  
  /**
   * Parse tokens into this PIT
   */
  public boolean set(StringTokenizer tokens) {

    // no tokens - fine - no info
    if (!tokens.hasMoreTokens()) {
      reset();
      return true;
    }

    // first token might be calendar indicator @#....@
    String first = tokens.nextToken();
    
    if (first.startsWith("@#")) {
      
      // .. has to be one of our calendar escapes
      for (int c=0;c<CALENDARS.length;c++) {
        Calendar cal = CALENDARS[c]; 
        if (cal.escape.startsWith(first)) {
          calendar = cal;
          break;
        }
      }

      // since one of the calendar escape contains a space we
      // might have to skip another token (until we find the
      // token ending in "@"       
      while (!first.endsWith("@")&&tokens.hasMoreTokens()) 
        first = tokens.nextToken();
      
      // switch to next 'first'
      if (!tokens.hasMoreTokens())
        return true;  // calendar only is fine - empty pit
      first = tokens.nextToken();

    }
    
    // first is YYYY
    if (!tokens.hasMoreTokens()) {
        try {
          set(UNKNOWN,UNKNOWN,Integer.parseInt(first));
        } catch (NumberFormatException e) {
          return false;
        }
        return getYear()!=UNKNOWN;
    }
    
    // have second
    String second = tokens.nextToken();
    
    // first and second are MMM YYYY
    if (!tokens.hasMoreTokens()) {
      try {
        set(UNKNOWN, calendar.parseMonth(first), Integer.parseInt(second));
      } catch (NumberFormatException e) {
        return false;
      }
      return getYear()!=UNKNOWN&&getMonth()!=UNKNOWN;
    }

    // have third
    String third = tokens.nextToken();
    
    // first, second and third are DD MMM YYYY
    if (!tokens.hasMoreTokens()) {
      try {
        set( Integer.parseInt(first) - 1, calendar.parseMonth(second), Integer.parseInt(third));
      } catch (NumberFormatException e) {
        return false;
      }
      return getYear()!=UNKNOWN&&getMonth()!=UNKNOWN&&getDay()!=UNKNOWN;
    }

    // wrong number of tokens
    return false;
  }
  
  /**
   * Checks for gregorian calendar 
   */
  public boolean isGregorian() {
    return getCalendar()==GREGORIAN;    
  }

  /**
   * Checks for completeness - DD MMM YYYY
   */
  public boolean isComplete() {
    return isValid() && year!=UNKNOWN && month!=UNKNOWN && day!=UNKNOWN;
  }

  /**
   * Checks for validity
   */
  public boolean isValid() {
    
    // ok if known JD    
    if (jd!=UNKNOWN)
      return true;

    // quick empty check 20040301 where lenient here before but lead to problems
    // relying on valid dates that *CAN* be compared reliably
    if (day==UNKNOWN&&month==UNKNOWN&&year==UNKNOWN)
      return false;

    // try calculating JD
    try {
      jd = calendar.toJulianDay(this);
    } catch (GedcomException e) {
    }
    
    // done
    return jd!=UNKNOWN;
  }
    
  /**
   * compare to other
   */  
  public int compareTo(Object o) {
    return compareTo((PointInTime)o);
  }    

  /**
   * compare to other
   * @param other the pit to compare to
   */  
  public int compareTo(PointInTime other) {
    
    // check valid
    boolean
      v1 = isValid(),
      v2 = other.isValid();
    if (!v1&&!v2)
      return 0;
    if (!v2)
      return 1;
    if (!v1)
      return -1; 
    // compare
    try {
      return getJulianDay() - other.getJulianDay();
    } catch (GedcomException e) {
      return 0; // shouldn't really happen
    }
  }

  /**
   * String representation (Gedcom format)
   */
  public String getValue() {
    return getValue(new WordBuffer()).toString();
  }
    
  /**
   * String representation (Gedcom format)
   */
  public WordBuffer getValue(WordBuffer buffer) {
    if (calendar!=GREGORIAN)
      buffer.append(calendar.escape);
    toString(buffer, false);
    return buffer;
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
    
    if (year!=UNKNOWN) {
      if (month!=UNKNOWN) {
        if (day!=UNKNOWN) {
          buffer.append(new Integer(day+1));
        }
        buffer.append(calendar.getMonth(month, localize));
      }
          
      buffer.append(calendar.getYear(year, localize));
      
      if (localize&&calendar==JULIAN)
        buffer.append("(j)");
    }
    
    return buffer;
  }

} //PointInTime