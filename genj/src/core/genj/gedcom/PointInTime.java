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
  public final static    JulianCalendar JULIAN    = new    JulianCalendar();
  public final static    HebrewCalendar HEBREW    = new    HebrewCalendar();
  public final static   FrenchRCalendar FRENCHR   = new   FrenchRCalendar();
    
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
   * Accessor to a calendar transformed copy 
   */
  public PointInTime getPointInTime(Calendar cal) throws GedcomException {
    PointInTime result = new PointInTime();
    result.set(this);
    result.set(cal);
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
    
    // FIXME what to do about the offset
    
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
      return calendar.toJulianDay(this) - other.calendar.toJulianDay(other);
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
    
    int 
      day = getDay(),
      month = getMonth(),
      year = getYear();
      
    if (year>0) {
      if (month>=0) {
        if (day>=0) {
          buffer.append(new Integer(day+1));
        }
        buffer.append(calendar.getMonth(month, localize));
      }
          
      if (calendar==GREGORIAN||!localize)
        buffer.append(calendar.getYear(year, localize));
      else
        buffer.append(calendar.getYear(year, localize)+calendar.marker);
    }
    
    return buffer;
  }

  /**
   * Calculate delta of two times years,months,days
   */
  public static int[] getDelta(PointInTime earlier, PointInTime later) {

// FIXME how does delta look for different calendars?
//    return earlier.calendar.toJulianDay(later) -
//      later.calendar.toJulianDay(later); 
    
    return null;
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
    protected String marker;
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
      marker = "("+key.charAt(0)+")";
      
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
    public String[] getMonths(boolean localize) {
      
      String[] result = new String[months.length];
      for (int m=0;m<result.length;m++) {
        String mmm = months[m];
        if (localize) 
          mmm = localizedMonthNames.get(mmm).toString();
        result[m] = mmm;
      }
      return result;
    }

    /**
     * Returns the (localized short) month as string (either MAY or Mai)
     */
    public String getMonth(int month, boolean localize) {
      // what's the numeric value?
      if (month<0||month>=months.length)
        return "";
      // calculate text
      String mmm = months[month];
      if (localize) 
        mmm = abbreviatedMonthNames.get(mmm).toString();
      // done
      return mmm;
    }
    
    /**
     * Returns the (localized) year as string
     */
    public String getYear(int year, boolean localize) {
      return ""+year;
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
    protected final int toJulianDay(PointInTime pit) throws GedcomException {
      
      // grab data and correct for missing day/month
      int 
        year  =             pit.getYear () ,
        month = Math.max(0, pit.getMonth()),
        day   = Math.max(0, pit.getDay  ());
      
      
      return toJulianDay(day, month, year);
    }

    /**
     * PIT -> Julian Day
     */
    protected abstract int toJulianDay(int day, int month, int year) throws GedcomException;
    
    /**
     * Julian Day -> PIT
     */
    protected abstract PointInTime toPointInTime(int julianDay) throws GedcomException;
    
  } //Calendar

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
     * @see genj.gedcom.PointInTime.Calendar#toJulianDay(int, int, int)
     */
    protected int toJulianDay(int day, int month, int year) {

      // Communications of the ACM by Henry F. Fliegel and Thomas C. Van Flandern entitled 
      // ``A Machine Algorithm for Processing Calendar Dates''. 
      // CACM, volume 11, number 10, October 1968, p. 657.  
      int
       d = day   + 1,
       m = month + 1,
       y = year     ;      
      
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
     * @see genj.gedcom.PointInTime.GregorianCalendar#toJulianDay(int, int, int)
     */
    protected int toJulianDay(int day, int month, int year) {
      
      int 
        y = year,
        m = month+1,
        d = day+1;
      
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
      AN_I  = GREGORIAN.toJulianDay(22-1, 9-1, 1792),
      UNTIL = GREGORIAN.toJulianDay( 1-1, 1-1, 1806);

    private static final String MONTHS[] 
     = { "VEND","BRUM","FRIM","NIVO","PLUV","VENT","GERM","FLOR","PRAI","MESS","THER","FRUC","COMP" };
    
    private static final int[] LEAP_YEARS
     = { 3,7,11 };
     
    private static final String[] YEARS 
     = { "An I", "An II", "An III", "An IV", "An V", "An VI", "An VII", "An VIII", "An IX", "An X", "An xI", "An XII", "An XIII", "An XIV" };
    
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
      for (int l=0;l<LEAP_YEARS.length;l++)
        if (LEAP_YEARS[l]==year) return true; 
      return false;
    }
    
    /**
     * @see genj.gedcom.PointInTime.Calendar#isValid(int, int, int)
     */
    protected boolean isValid(int day, int month, int year) {
      
      // default checks
      if (!super.isValid(day, month, year))
        return false;
        
      // check range
      try {
        toJulianDay(day, month, year);
      } catch (GedcomException e) {
        return false;
      }
      
      // ok        
      return true;
    }
    
    /**
     * @see genj.gedcom.PointInTime.Calendar#toJulianDay(genj.gedcom.PointInTime)
     */
    protected int toJulianDay(int d, int m, int y) throws GedcomException {
      // calc days
      int jd = AN_I + 365*(y-1) + m*30 + d;
      // check leap years (one less day on julian day)
      for (int l=0;l<LEAP_YEARS.length;l++)
        if (y>LEAP_YEARS[l]) jd++; 
      // check range
      if (jd<AN_I)
        throw new GedcomException("Day lies before French Republican Calendar");
      if (jd>=UNTIL)
        throw new GedcomException("Day lies after French Republican Calendar");
      // sum
      return jd;
    }
    
    /**
     * @see genj.gedcom.PointInTime.Calendar#toPointInTime(int)
     */
    protected PointInTime toPointInTime(int julianDay) throws GedcomException {

      // check range
      if (julianDay<AN_I)
        throw new GedcomException("Day lies before French Republican Calendar");
      if (julianDay>=UNTIL)
        throw new GedcomException("Day lies after French Republican Calendar");
      
      julianDay = julianDay - AN_I;
      
      // calculate years
      int 
        y  = julianDay/365 + 1,
        yr = julianDay%365;
        
      // check leap years (one less day on julian day)
      for (int l=0;l<LEAP_YEARS.length;l++)
        if (y>LEAP_YEARS[l]) yr--; 
        
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
      if (!localize||year<1||year>YEARS.length)
        return ""+year;
      return YEARS[year-1];
    }

  
  } //FrenchRCalendar

  /**
   * Our own hebrew republican
   */
  public static class HebrewCalendar extends Calendar {

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
      super("@#DHEBREW@", "hebrew", "images/Hebrew.gif", MONTHS);
    }
    
    /**
     * Julian Day -> PIT
     */
    protected PointInTime toPointInTime(int julianDay) throws GedcomException {
      // FIXME transformation Julian Day to Hebrew is missing
      throw new GedcomException("Transformation to Hebrew Calendar not implemented yet");
    }
    
    /**
     * @see genj.gedcom.PointInTime.Calendar#isValid(int, int, int)
     */
    protected boolean isValid(int day, int month, int year) {
      // super has something to say about that
      if (!super.isValid(day, month, year))
        return false;
      // make sure year is >=1
      if (year<1)
        return false;
      // make sure Adar R is in leap year
      if (month==5&&!isLeap(year))
        return false;
      // ok
      return true; 
    }

    
    /**
     * d,m,y -> Julian Day
     */
    protected int toJulianDay(int day, int month, int year) throws GedcomException {

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
      return ANNO_MUNDI + jd;
    }

    /**
     * @see genj.gedcom.PointInTime.Calendar#getDays(int, int)
     */
    protected int getDays(int month, int year) {
      
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
  
} //PointInTime