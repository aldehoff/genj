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
public abstract class PointInTime implements Comparable {

  /** calendars */
  public final static Calendar
    GREGORIAN = new Calendar("@#DGREGORIAN@", "gregorian", "images/Gregorian.gif"),
    JULIAN    = new Calendar("@#DJULIAN@"   , "julian"   , "images/Julian.gif"),
    HEBREW    = new Calendar("@#DHEBREW@"   , "hebrew"   , "images/Hebrew.gif"),
    FRENCHR   = new Calendar("@#DFRENCH R@" , "french"   , "images/FrenchR.gif");
  
  public final static Calendar[] CALENDARS = { GREGORIAN, JULIAN, HEBREW, FRENCHR };
    
  /** month names */
  private final static String 
    MONTHS[] = { "JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC" },
    MONTHS_HEBR[] = { "TSH","CSH","KSL","TVT","SHV","ADR","ADS","NSN","IYR","SVN","TMZ","AAV","ELL" },
    MONTHS_FREN[] = { "VEND","BRUM","FRIM","NIVO","PLUV","VENT","GERM","FLOR","PRAI","MESS","THER","FRUC","COMP" };

  /** localized months */
  private static Map
    localizedMonthNames = new HashMap(),
    abbreviatedMonthNames = new HashMap(); 

  /**
   * initialize months - loop through month names, remember localized value
   * and calculate abbreviation (either first 3 characters or up to vertical
   * bar marker e.g. juil|let)
   */
  {
    for (int m=0;m<MONTHS.length;m++) {
      String mmm = MONTHS[m];
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
  }
  
  /** calendar */
  private Calendar calendar = GREGORIAN;
  
  /**
   * Returns the calendar
   */
  public Calendar getCalendar() {
    return calendar;
  }

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
  public void set(PointInTime other) {
    set(other.getDay(), other.getMonth(), other.getYear());
  }
  
  /**
   * Parse month
   */
  protected int parseMonth(String mmm) throws NumberFormatException {
    for (int i=0;i<MONTHS.length;i++) {
      if (MONTHS[i].equalsIgnoreCase(mmm)) return i;
    }
    throw new NumberFormatException();
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
        set(-1, parseMonth(first), Integer.parseInt(second));
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
        set( Integer.parseInt(first) - 1, parseMonth(second), Integer.parseInt(third));
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

    // YYYY or MMM YYYY or DD MMMM YYYY
    int year = getYear();
    if (year<0)
      return false;
    int month = getMonth();
    if (month<-1||month>=12)
      return false;
    int day = getDay();
    if ((month<0&&day>=0)||day<-1||day>31)
      return false;
    return true;
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
      if (month>=0&&month<MONTHS.length) {
        if (day>=0) 
          buffer.append(new Integer(day+1));
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
    // what's the numeric value?
    int month = getMonth();
    if (month<0||month>=12)
      return "";
    // calculate text
    String mmm = MONTHS[month];
    if (localize) 
      mmm = abbreviate ? abbreviatedMonthNames.get(mmm).toString() : localizedMonthNames.get(mmm).toString();
    // done
    return mmm;
  }
  
  /**
   * Access to (localized) gedcom months 
   */
  public static String[] getMonths(boolean localize, boolean abbreviate) {
    String[] result = new String[12];
    for (int m=0;m<result.length;m++) {
      String mmm = MONTHS[m];
      if (localize) 
        mmm = abbreviate ? abbreviatedMonthNames.get(mmm).toString() : localizedMonthNames.get(mmm).toString();
      result[m] = mmm;
    }
    return result;
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

  /**
   * Calendars we support
   */
  public static class Calendar {
    
    /** fields */
    private String escape;
    private String name;
    private ImageIcon image;
     
    /** Constructor */
    private Calendar(String esc, String key, String img) {
      escape = esc;
      name = Gedcom.resources.getString("prop.date.cal."+key);
      image = new ImageIcon(Gedcom.class, img);
    }
    
    /** accessor - name */
    public String getName() {
      return name;
    }
    
    /** accessor - image */
    public ImageIcon getImage() {
      return image;
    }
    
    
  } //Calendar

} //PointInTime