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
import genj.util.swing.ImageIcon;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A point in time - either hebrew, roman, frenchr, gregorian or julian
 */
public class PointInTime implements Comparable {

  /** calendars */
  public final static Calendar
    GREGORIAN = new Calendar("@#DGREGORIAN@", "gregorian", "images/Gregorian.gif", new String[]{ "JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC" }),
    JULIAN    = new Calendar("@#DJULIAN@"   , "julian"   , "images/Julian.gif"   , new String[]{ "JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC" }),
    HEBREW    = new Calendar("@#DHEBREW@"   , "hebrew"   , "images/Hebrew.gif"   , new String[]{ "TSH","CSH","KSL","TVT","SHV","ADR","ADS","NSN","IYR","SVN","TMZ","AAV","ELL" }),
    FRENCHR   = new Calendar("@#DFRENCH R@" , "french"   , "images/FrenchR.gif"  , new String[]{ "VEND","BRUM","FRIM","NIVO","PLUV","VENT","GERM","FLOR","PRAI","MESS","THER","FRUC","COMP" });
  
  public final static Calendar[] CALENDARS = { GREGORIAN, JULIAN, HEBREW, FRENCHR };
    
  /** calendar */
  protected Calendar calendar = GREGORIAN;
  
  /** values */
  private int 
    day = -1, 
    month = -1, 
    year = -1;

  /**
   * Constructor
   */
  protected PointInTime() {
  }

  /**
   * Constructor
   */
  protected PointInTime(int d, int m, int y, Calendar cal) {
    day = d;
    month = m;
    year = y;
    calendar = cal;
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
   * Accessor to an immutable point in time
   * @param d day (zero based)
   * @param m month (zero based)
   * @param y year
   */
  public static PointInTime getPointInTime(int d, int m, int y) {
    return getPointInTime(d,m,y,GREGORIAN);
  }
  
  /**
   * Accessor to an immutable point in time
   * @param d day (zero based)
   * @param m month (zero based)
   * @param y year
   */
  public static PointInTime getPointInTime(int d, int m, int y, Calendar calendar) {
    return new PointInTime(d, m, y, calendar);
  }
  
  /**
   * Accessor to an immutable point in time - now
   */
  public static PointInTime getNow() {
    java.util.Calendar now = java.util.Calendar.getInstance(); // default to current time
    return getPointInTime(
      now.get(now.DATE) - 1,      
      now.get(now.MONTH),      
      now.get(now.YEAR)
    );      
  }  
  
  /**
   * Accessor to an immutable point in time
   * @param string e.g. 25 MAY 1970
   */
  public static PointInTime getPointInTime(String string) {
    PointInTime result = new PointInTime(-1,-1,-1, GREGORIAN);
    result.set(new StringTokenizer(string));
    return result;
  }
  
  /**
   * Setter (implementation dependant)
   */
  public void set(int d, int m, int y) {
    day = d;
    month = m;
    year = y;
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
  protected boolean set(StringTokenizer tokens) {

    // no tokens no joy
    if (!tokens.hasMoreTokens())
      return false;

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
        return false;
      first = tokens.nextToken();

    }
    
    // first is YYYY
    if (!tokens.hasMoreTokens()) {
        try {
          set(-1,-1,Math.max(-1,Integer.parseInt(first)));
        } catch (NumberFormatException e) {
          return false;
        }
        return getYear()>=0;
    }
    
    // have second
    String second = tokens.nextToken();
    
    // first and second are MMM YYYY
    if (!tokens.hasMoreTokens()) {
      try {
        set(-1, calendar.parseMonth(first), Integer.parseInt(second));
      } catch (NumberFormatException e) {
        return false;
      }
      return getYear()>=0;
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
      return getYear()>=0&&getDay()>=0&&getDay()<=31;
    }

    // wrong number of tokens
    return false;
  }

  /**
   * Checks for validity
   */
  public boolean isValid() {
    return calendar.isValid(day,month,year);
  }
    
  /**
   * compare to other
   */  
  public int compareTo(Object o) {
    return compareTo((PointInTime)o, 0);
  }    
  
  /**
   * compare to other
   * @param other the pit to compare to
   * @param offset 0 start with year 1 start with month 2 start with day
   */  
  public int compareTo(PointInTime other, int offset) {

    // FIXME comparing PITs with different calendars must be handled
    
    int result;
      
    // Year ?
    if (--offset<0)
      if ((result=getYear()-other.getYear())!=0) return result;
      
    // Month
    if (--offset<0)
      if ((result=getMonth()-other.getMonth())!=0) return result;
      
    // Day
    if (--offset<0)
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
  public WordBuffer toString(WordBuffer buffer, boolean localize) {
    
    int 
      day = getDay(),
      month = getMonth(),
      year = getYear();
      
    if (year>0) {
      if (month>=0) {
        if (day>=0) {
          buffer.append(new Integer(day+1));
        }
        buffer.append(calendar.getMonth(month, localize, true));
      }    
      buffer.append(new Integer(year));
    }
    
    return buffer;
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
      java.util.Calendar c = java.util.Calendar.getInstance();
      c.set(yearlier, mearlier, 1);
      int days = c.getActualMaximum(c.DATE);
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
   * Calendars we support
   */
  public static class Calendar {
    
    /** fields */
    private String escape;
    private String name;
    private ImageIcon image;
    private String[] months;
    private Map
      localizedMonthNames = new HashMap(),
      abbreviatedMonthNames = new HashMap(); 
     
    /** Constructor */
    private Calendar(String esc, String key, String img, String[] mOnths) {
      
      // initialize members
      months = mOnths;
      escape = esc;
      name = Gedcom.resources.getString("prop.date.cal."+key);
      image = new ImageIcon(Gedcom.class, img);
      
      // localize months
      for (int m=0;m<months.length;m++) {
        String mmm = months[m];
        String localized = Gedcom.getResources().getString("prop.date.mon."+mmm);
        String abbreviated;
    
        // calculate abbreviation
        int marker = localized.indexOf('|'); 
        if (marker>0) {
          abbreviated = localized.substring(0, marker);
          localized = abbreviated + localized.substring(marker+1);
        } else {
          abbreviated = localized.length()>3 ? localized.substring(0,3) : localized;
        }
    
        // remember
        localizedMonthNames.put(mmm, localized);
        abbreviatedMonthNames.put(mmm, abbreviated);
    
        // next
      }  

      // done
    }
    
    /** accessor - name */
    public String getName() {
      return name;
    }
    
    /** accessor - image */
    public ImageIcon getImage() {
      return image;
    }
    
    /**
     * Parse month
     */
    protected int parseMonth(String mmm) throws NumberFormatException {
      for (int i=0;i<months.length;i++) {
        if (months[i].equalsIgnoreCase(mmm)) return i;
      }
      throw new NumberFormatException();
    }
  
    /**
     * Access to (localized) gedcom months 
     */
    public String[] getMonths(boolean localize, boolean abbreviate) {
      
      String[] result = new String[months.length];
      for (int m=0;m<result.length;m++) {
        String mmm = months[m];
        if (localize) 
          mmm = abbreviate ? abbreviatedMonthNames.get(mmm).toString() : localizedMonthNames.get(mmm).toString();
        result[m] = mmm;
      }
      return result;
    }

    /**
     * Returns the localized month as string (either MAY or Mai)
     */
    public String getMonth(int month, boolean localize, boolean abbreviate) {
      // what's the numeric value?
      if (month<0||month>=months.length)
        return "";
      // calculate text
      String mmm = months[month];
      if (localize) 
        mmm = abbreviate ? abbreviatedMonthNames.get(mmm).toString() : localizedMonthNames.get(mmm).toString();
      // done
      return mmm;
    }
  
    /**
     * Validity check
     */
    public boolean isValid(int day, int month, int year) {
      // YYYY is needed!
      if (year<0)
        return false;
      // MMM YYYY with month within range?
      if (month<-1||month>=months.length)
        return false;
      // DD MMMM YYYY involves month
      if (month<0&&day>=0)
        return false;
      // FIXME day in range dependent on calendar?
      if (day<-1||day>31)
        return false;
      return true;
    }
    
  } //Calendar

} //PointInTime