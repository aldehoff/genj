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

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.util.swing.ImageIcon;

import java.util.HashMap;
import java.util.Map;


/**
 * Calendars we support
 */
public abstract class Calendar {
  
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
    name = PointInTime.resources.getString("cal."+key);
    image = new ImageIcon(Gedcom.class, img);
    marker = "("+key.charAt(0)+")";
    
    // localize months
    for (int m=0;m<months.length;m++) {
      String mmm = months[m];
      String localized = PointInTime.resources.getString("mon."+mmm);
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
   * Returns the (localized) day
   */
  public String getDay(int day) {
    if (day==PointInTime.UNKNOWN)
      return "";
    return ""+(day+1);
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
    if (year==PointInTime.UNKNOWN)
      return "";
    return ""+year;
  }
  
  /**
   * Returns the year from a string - opportunity for special year designations
   * to be introduced
   */
  public int getYear(String year) throws GedcomException {
    try {
      return Integer.parseInt(year);
    } catch (NumberFormatException e) {
      throw new GedcomException(year+" is not a valid year");
    }
  }

  /**
   * Calculate number of months
   */
  public int getMonths() {
    return months.length;
  }
  
  /**
   * Calculate number of days in given month
   */
  public abstract int getDays(int month, int year);
      
  /**
   * PIT -> Julian Day
   */
  protected final int toJulianDay(PointInTime pit) throws GedcomException {

    // grab data 
    int 
      year  = pit.getYear () ,
      month = pit.getMonth(),
      day   = pit.getDay  ();
      
    // YYYY is always needed - no calendar includes a year 0!
    if (year==PointInTime.UNKNOWN||year==0)
      throw new GedcomException(PointInTime.resources.getString("year.invalid"));
      
    // MM needed if DD!
    if (month==PointInTime.UNKNOWN&&day!=PointInTime.UNKNOWN)
      throw new GedcomException(PointInTime.resources.getString("month.invalid"));
      
    // months have to be within range
    if (month==PointInTime.UNKNOWN)
      month = 0;
    else if (month<0||month>=months.length)
      throw new GedcomException(PointInTime.resources.getString("month.invalid"));

    // day has to be withing range
    if (day==PointInTime.UNKNOWN)
      day = 0;
    else if (day<0||day>=getDays(month,year))
      throw new GedcomException(PointInTime.resources.getString("day.invalid"));

    // try to get julian day
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