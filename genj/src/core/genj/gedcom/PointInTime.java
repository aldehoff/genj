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
  public final static GregorianCalendar GREGORIAN = new GregorianCalendar();
  public final static JulianCalendar    JULIAN    = new JulianCalendar();
  public final static Calendar          HEBREW    = new HebrewCalendar();
  public final static FrenchRCalendar   FRENCHR   = new FrenchRCalendar();
    
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
   * Setter
   */
  public void set(Calendar cal) throws GedcomException {
    // has to be valid
    if (!isValid())
      throw new GedcomException("PointInTime not valid - switching calendars n/a");
    // has to be complete
    if (!isComplete())
      throw new GedcomException("PointInTime not complete DD MMM YYYY - switching calendars n/a");
    // convert to julian date
    int jd = calendar.toJulianDay(this);
    // convert to new instance
    set(cal.toPointInTime(jd));
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
   * Checks for completeness - DD MMM YYYY
   */
  public boolean isComplete() {
    return isValid() && year>0 && month>=0 && day>=0;
  }

  /**
   * Checks for validity
   */
  public boolean isValid() {

    // YYYY is always needed!
    if (year<0)
      return false;
    // MM needed if DD!
    if (month<0&&day>=0)
      return false;
    // DD at least not <-1
    if (day<-1)
      return false;

    // rely on calendar specific
    return calendar.isValid(day<0?0:day,month<0?0:month,year);
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

    // FIXME need to use julian day comparison of for different calendars 
    
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
  public static abstract class Calendar {
    
    /** fields */
    protected String escape;
    protected String name;
    protected ImageIcon image;
    protected String[] months;
    protected Map
      localizedMonthNames = new HashMap(),
      abbreviatedMonthNames = new HashMap(); 
     
    /** 
     * Constructor 
     */
    protected Calendar(String esc, String key, String img, String[] mOnths) {
      
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
     * Validity check day,month,year>0
     */
    protected boolean isValid(int day, int month, int year) {

      // months have to be within range
      if (month>=months.length)
        return false;

      // day has to be withing range
      if (day>=getDays(month,year))
        return false;

      // is good
      return true;
    }
    
    /**
     * Calculate number of days in given month
     */
    protected abstract int getDays(int month, int year);
        
    /**
     * PIT -> Julian Day
     */
    protected abstract int toJulianDay(PointInTime pit) throws GedcomException;
    
    /**
     * Julian Day -> PIT
     */
    protected abstract PointInTime toPointInTime(int julianDay) throws GedcomException;
    
  } //Calendar

  // FIXME need calendar for hebrew

  /**
   * Our own gregorian - dunno if java.util.GregorianCalendar would be of much help
   */
  public static class GregorianCalendar extends Calendar {

    protected static final String MONTHS[]
      = { "JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC" };
    private static final int MONTH_LENGTH[]
      = {31,28,31,30,31,30,31,31,30,31,30,31}; // 0-based
    private static final int LEAP_MONTH_LENGTH[]
      = {31,29,31,30,31,30,31,31,30,31,30,31}; // 0-based
  
    /**
     * Constructor
     */
    protected GregorianCalendar() {
      this("@#DGREGORIAN@", "gregorian", "images/Gregorian.gif");
    }
    
    /**
     * Constructor
     */
    protected GregorianCalendar(String esc, String key, String img) {
      super(esc, key, img, MONTHS);
    }

    /**
     * @see genj.gedcom.PointInTime.Calendar#getDays(int, int)
     */
    protected int getDays(int month, int year) {
      int[] length = isLeap(year) ? LEAP_MONTH_LENGTH : MONTH_LENGTH;
      return length[month];
    }
    
    /**
     * Definition of Gregorian leap year
     */
    protected boolean isLeap(int year) {
      return ((year%4 == 0) && ((year%100 != 0) || (year%400 == 0)));
    }
    
    /**
     * @see genj.gedcom.PointInTime.Calendar#getJulianDay(genj.gedcom.PointInTime)
     */
    protected int toJulianDay(PointInTime pit) {

      // Communications of the ACM by Henry F. Fliegel and Thomas C. Van Flandern entitled 
      // ``A Machine Algorithm for Processing Calendar Dates''. 
      // CACM, volume 11, number 10, October 1968, p. 657.  
      int
       d = pit.getDay  ()+1,
       m = pit.getMonth()+1,
       y = pit.getYear ();      
      
      return ( 1461 * ( y + 4800 + ( m - 14 ) / 12 ) ) / 4 +
             ( 367 * ( m - 2 - 12 * ( ( m - 14 ) / 12 ) ) ) / 12 -
             ( 3 * ( ( y + 4900 + ( m - 14 ) / 12 ) / 100 ) ) / 4 +
             d - 32075;
    }
    
    /**
     * @see genj.gedcom.PointInTime.Calendar#getPointInTime(int)
     */
    protected PointInTime toPointInTime(int julianDay) {
     
      // see toJulianDay 
      int l = julianDay + 68569;
      int n = ( 4 * l ) / 146097;
          l = l - ( 146097 * n + 3 ) / 4;
      int i = ( 4000 * ( l + 1 ) ) / 1461001;
          l = l - ( 1461 * i ) / 4 + 31;
      int j = ( 80 * l ) / 2447;
      int d = l - ( 2447 * j ) / 80;
          l = j / 11;
      int m = j + 2 - ( 12 * l );
      int y = 100 * ( n - 49 ) + i + l;
      
      return new PointInTime(d-1,m-1,y,this);
    }
    
  } //GregorianCalendar

  /**
   * Our own julian
   */
  public static class JulianCalendar extends GregorianCalendar {

    /**
     * Constructor
     */
    protected JulianCalendar() {
      super("@#DJULIAN@", "julian", "images/Julian.gif");
    }
    
    /**
     * @see genj.gedcom.PointInTime.GregorianCalendar#isLeap(int)
     */
    protected boolean isLeap(int year) {
      return (year%4 == 0);
    }
    
    /**
     * @see genj.gedcom.PointInTime.GregorianCalendar#getJulianDay(genj.gedcom.PointInTime)
     */
    protected int toJulianDay(PointInTime pit) {
      
      // see http://quasar.as.utexas.edu/BillInfo/JulianDatesG.html
      
      int 
        y = pit.getYear(),
        m = pit.getMonth()+1,
        d = pit.getDay()+1;
      
      if (m<2) {
        y--;
        m+=12;
      }
            
      int
        E = (int)(365.25*(y+4716)),
        F = (int)(30.6001*(m+1)),
        JD= d+E+F-1524;
            
      return JD;
    }
    
    /**
     * @see genj.gedcom.PointInTime.GregorianCalendar#getPointInTime(int)
     */
    protected PointInTime toPointInTime(int julianDay) {

      // see toJulianDay
      
      int
        Z = julianDay,
        B = Z+1524,
        C = (int)((B-122.1)/365.25),
        D = (int)(365.25*C),
        E = (int)((B-D)/30.6001),
        F = (int)(30.6001*E),
        d = B-D-F,
        m = E-1 <= 12 ? E-1 : E-13,
        y = C-(m<3?4715:4716);  
      
      return new PointInTime((int)d-1,(int)m-1,(int)y,this);
    }

  } //JulianCalendar

  /**
   * Our own french republican
   */
  public static class FrenchRCalendar extends Calendar {
    
    /* valid from 22 SEP 1792 to not including 1 JAN 1806 */
    private static final int
      START  = GREGORIAN.toJulianDay(getPointInTime(22-1, 9-1, 1792));

    private static final String MONTHS[] 
     = { "VEND","BRUM","FRIM","NIVO","PLUV","VENT","GERM","FLOR","PRAI","MESS","THER","FRUC","COMP" };
    
    /**
     * Constructor
     */
    protected FrenchRCalendar() {
      super("@#DFRENCH R@" , "french", "images/FrenchR.gif", MONTHS);
    }
    
    /**
     * @see genj.gedcom.PointInTime.Calendar#getDays(int, int)
     */
    protected int getDays(int month, int year) {
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
      return year == 3 || year == 7 || year == 11;
    }
    
    /**
     * @see genj.gedcom.PointInTime.Calendar#isValid(int, int, int)
     */
    protected boolean isValid(int day, int month, int year) {
      
      // default checks
      if (!super.isValid(day, month, year))
        return false;
        
      // ok        
      return true;
    }
    
    /**
     * @see genj.gedcom.PointInTime.Calendar#toJulianDay(genj.gedcom.PointInTime)
     */
    protected int toJulianDay(PointInTime pit) throws GedcomException {
      return START + pit.getDay() + pit.getMonth()*30 + 365*(pit.getYear()-1);
    }
    
    /**
     * @see genj.gedcom.PointInTime.Calendar#toPointInTime(int)
     */
    protected PointInTime toPointInTime(int julianDay) throws GedcomException {
      
      julianDay = julianDay - START;
      
      int 
        y  = julianDay/365 + 1,
        yr = julianDay%365,
        m  = yr/30,
        mr = yr%30,
        d  = mr;
        
      return new PointInTime(d,m,y,this);
    }

  
  } //FrenchRCalendar

  /**
   * Our own hebrew republican
   */
  public static class HebrewCalendar extends Calendar {
    
    private static final String MONTHS[] 
     = { "TSH","CSH","KSL","TVT","SHV","ADR","ADS","NSN","IYR","SVN","TMZ","AAV","ELL" };
  
    /**
     * Constructor
     */
    protected HebrewCalendar() {
      super("@#DHEBREW@", "hebrew", "images/Hebrew.gif", MONTHS);
    }
    
    /**
     * PIT -> Julian Day
     */
    protected int toJulianDay(PointInTime pit) throws GedcomException {
      throw new GedcomException("Transformation from Hebrew Calendar not implemented yet");
    }
      
    /**
     * Julian Day -> PIT
     */
    protected PointInTime toPointInTime(int julianDay) throws GedcomException {
      throw new GedcomException("Transformation to Hebrew Calendar not implemented yet");
    }
    
    /**
     * @see genj.gedcom.PointInTime.Calendar#getDays(int, int)
     */
    protected int getDays(int month, int year) {
      return 30;
    }
    
  } //HebrewCalendar    
  
} //PointInTime