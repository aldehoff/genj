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

import java.util.*;

import genj.util.*;

/**
 * Gedcom Property : DATE
 */
public class PropertyDate extends Property {

  /** time values */
  private Integer
    day  []= {null,null},
    month[]= {null,null},
    year []= {null,null};

  /** format flag */
  private int format;

  /** as string */
  private String dateAsString;

  /** format types */
  public static final int
    DATE    = 0,
    FROMTO  = 1,
    FROM    = 2,
    TO      = 3,
    BETAND  = 4,
    BEF     = 5,
    AFT     = 6,
    ABT     = 7,
    CAL     = 8,
    EST     = 9,
    MAX     = 9;

  /** month names */
  private final static String months[] = {"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};

  /** type attributes #1 */
  private final static String attrib0[] = {null,"FROM","FROM","TO","BET","BEF","AFT","ABT","CAL","EST"};

  /** type attributes #2 */
  private final static String attrib1[] = {null,"TO"  ,null  ,null,"AND",null ,null ,null ,null ,null };

  /**
   * Constructor
   */
  public PropertyDate() {

    format   = DATE;
    day  [0] = null;
    month[0] = null;
    year [0] = null;
    day  [1] = null;
    month[1] = null;
    year [1] = null;
  }

  /**
   * Constructor
   */
  public PropertyDate(String tag, String value) {
    setValue(value);
  }

  /**
   * Compares this property to another property
   * @return -1 this < property <BR>
   *          0 this = property <BR>
   *          1 this > property
   */
  public int compareTo(Property p) {

    if (!(p instanceof PropertyDate))
      return 0;

    return compareTo((PropertyDate)p);
  }

  /**
   * Compare two dates
   * @return the value 0 if this date is equal to the argument;
   * a value less than 0 if this date is less then the argument;
   * a value greater than 0 if this date is greater then the argument.
   */
  public int compareTo(PropertyDate p) {

    int result ;

    // Year ?
    if (year[0]==null)
      return (p.year[0]==null ? 0 : -1);
    if (p.year[0]==null)
      return 1;
    if (year[0].intValue()<p.year[0].intValue())
      return -1;
    if (year[0].intValue()>p.year[0].intValue())
      return 1;

    // Month
    if (month[0]==null)
      return (p.month[0]==null ? 0 : -1);
    if (p.month[0]==null)
      return 1;
    if (month[0].intValue()<p.month[0].intValue())
      return -1;
    if (month[0].intValue()>p.month[0].intValue())
      return 1;

    // Day
    if (day[0]==null)
      return (p.day[0]==null ? 0 : -1);
    if (p.day[0]==null)
      return 1;
    if (day[0].intValue()<p.day[0].intValue())
      return -1;
    if (day[0].intValue()>p.day[0].intValue())
      return 1;

    // Equal
    return 0;
  }

  /**
   * Returns a date part-attribute (see attrib0/1)
   */
  public String getAttribute(int which) {
    switch (which) {
      case 0 :
        return attrib0[format];
      case 1 :
        return attrib1[format];
    }
    throw new IllegalArgumentException("Illegal index for modifier (has to be 0 or 1)");
  }

  /**
   * Returns day of date 0/1
   */
  public Integer getDay(int which) {
    return day[which];
  }

  /**
   * Returns day of date 0/1 as int
   */
  public int getDay(int which, int otherwise) {
    if (day[which]==null)
      return otherwise;
    return day[which].intValue();
  }

  /**
   * Default Image
   */
  public static ImgIcon getDefaultImage() {
    return Images.imgDate;
  }

  /**
   * Returns format of date
   */
  public int getFormat() {
    return format;
  }

  /**
   * Returns image
   */
  public ImgIcon getImage(boolean checkValid) {
    if (checkValid&&(!isValid()))
      return Images.imgError;
    return Images.imgDate;
  }

  /**
   * Returns label for given format type
   */
  public static String getLabelForFormat(int which) {

    String res=null;

    switch (which) {
    case DATE:
      res="prop.date.date"  ; break;
    case FROMTO:
      res="prop.date.fromto"; break;
    case FROM:
      res="prop.date.from"  ; break;
    case TO:
      res="prop.date.to"    ; break;
    case BETAND:
      res="prop.date.betand"; break;
    case BEF:
      res="prop.date.bef"   ; break;
    case AFT:
      res="prop.date.aft"   ; break;
    case ABT:
      res="prop.date.abt"   ; break;
    case CAL:
      res="prop.date.cal"   ; break;
    case EST:
      res="prop.date.est"   ; break;
    }

    // Hmmmm
    if (res==null)
      return "";
    return Gedcom.getResources().getString(res);
  }

  /**
   * Returns month of date 0/1
   */
  public Integer getMonth(int which) {
    return month[which];
  }

  /**
   * Returns month of date 0/1 as int
   */
  public int getMonth(int which, int otherwise) {
    if (month[which]==null)
      return otherwise;
    return month[which].intValue();
  }

  /**
   * Returns generic proxy's logical name
   */
  public String getProxy() {
    if (dateAsString!=null) return "Unknown";
    return "Date";
  }

  /**
   * Returns the name of the proxy-object which knows properties looked
   * up by TagPath
   * @return proxy's logical name
   */
  public static String getProxy(TagPath path) {
    return "Date";
  }

  /**
   * Helper which returns given date in gedcom string-format
   */
  public static String getString(Calendar c) {

    return c.get(Calendar.DAY_OF_MONTH)
      + " " + months[c.get(Calendar.MONTH)]
      + " " + c.get(Calendar.YEAR);

  }

  /**
   * Accessor Tag
   */
  public String getTag() {
    return "DATE";
  }

  /**
   * Accessor Value
   */
  public String getValue() {

    return toString();

    /*
    // Generic information ?
    if (dateAsString!=null)
      return dateAsString;

    // Own information
    String v="";
    switch (format) {
      case DATE:
      v = toString(0);
      break;
      case FROMTO:
      v = "FROM " + toString(0)
        + " TO "  + toString(1);
        break;
      case FROM:
      v = "FROM " + toString(0);
      break;
      case TO:
      v = "TO "   + toString(0);
      break;
      case BETAND:
      v = "BET "  + toString(0)
       + " AND " + toString(1);
      break;
      case BEF:
      v = "BEF "  + toString(0);
      break;
      case AFT:
      v = "AFT "  + toString(0);
      break;
      case ABT:
      v = "ABT "  + toString(0);
      break;
      case CAL:
      v = "CAL "  + toString(0);
      break;
      case EST:
      v = "EST "  + toString(0);
      break;
      }

    return v.trim();
    */
  }

  /**
   * Returns year of date 0/1
   */
  public Integer getYear(int which) {
    return year[which];
  }

  /**
   * Returns year of date 0/1 as int
   */
  public int getYear(int which, int otherwise) {
    if (year[which]==null) {
      return otherwise;
    }
    return year[which].intValue();
  }

  /**
   * Helper that transforms Integer/null to month MMM
   */
  private String i2mmm(Integer i) throws NumberFormatException {
    if (i==null)
      return "";
    if (i.intValue()>months.length)
      throw new NumberFormatException();
    return months[i.intValue()-1];
  }

  /**
   * Helper that transforms Integer/null to String
   */
  private String i2s(Integer i) {
    if (i==null)
      return "";
    return String.valueOf(i);
  }

  /**
   * Returns wether this date is a range (fromto, betand)
   */
  public boolean isRange() {
    return attrib1[format]!=null;
  }

  /**
   * Returns wether given format is a range (fromto, betand)
   */
  public static boolean isRange(int which) {
    return attrib1[which]!=null;
  }

  /**
   * Returns wether this PropertyDate consists of two dates
   */
  public boolean isTwoDates() {
    return attrib1[format]!=null;
  }

  /**
   * Tells wether this date is valid
   * @return <code>boolean</code> indicating validity
   */
  public boolean isValid() {

    // Still invalid string information ?
    if (dateAsString!=null)
      return false;

    // Date 2 complete (if required for type) ?
    if ((getAttribute(1)!=null) && (!isValid(1)))
      return false;

    // Date 1 complete ?
    if (!isValid(0))
      return false;

    // O.K.
    return true;
  }

  /**
   * Tells wether part 0/1 of this date is valid
   */
  private boolean isValid(int which) {

    // Minimum is a year
    if (getYear(which)==null)
      return false;
    // Month is enough ?
    if (getMonth(which)!=null)
      return true;
    // Missing Month -> No Day !
    if (getDay(which)!=null)
      return false;
    // O.K.
    return true;
  }

  /**
   * Helper that transforms month to Integer
   */
  private int mmm2i(String mmm) throws NumberFormatException {
    for (int i=0;i<months.length;i++) {
      if (months[i].equalsIgnoreCase(mmm)) return i+1;
    }
    throw new NumberFormatException();
  }

  /**
   * Helper that parses date as string for validity
   */
  private boolean parseDate(String string, StringTokenizer tokens) {

    // No words -> no date
    if (tokens.countTokens()==0)
      return false;

    // Look for starting type information
    String f = tokens.nextToken();

    // ... by going through type list
    int t;
    for (t=0;t<attrib0.length;t++) {

      // .. found modifier
      if ( f.equals(attrib0[t]) ) {

        // ... type is standalone (TO,ABT,CAL,...) ?
        if ( attrib1[t] == null ) {
          // .. remember DATE format
          format = t;
          // .. look for date from second to last word
          return parseDate(tokens,0);
        }

        // ... type is combined (FROM-TO,BET-AND)
        String first="";
        while (tokens.hasMoreTokens()) {
          // .. TO or AND ?
          String next = tokens.nextToken();
          if ( next.equals(attrib1[t]) ) {
            // .. remember DATE format
            format = t;
            // .. calculate both
            return parseDate(new StringTokenizer(first),0) && parseDate(tokens,1);
          }
          // text token !
          first += " " + next + " ";
          // next one
        }
        // ... wasn't so good after all
      }
      // .. try next one
    }

    // ... no valid type found ?
    format = DATE;
    // .. look for date from first to last word
    return parseDate(new StringTokenizer(string),0);
  }

  /**
   * Helper that parses date as string for validity
   */
  private boolean parseDate(StringTokenizer tokens,int which) {

    // Number of tokens ?
    switch (tokens.countTokens()) {
      case 0 : // NONE
        return false;
      case 1 : // YYYY
        try {
          year[which] = new Integer( tokens.nextToken() );
        } catch (NumberFormatException e) {
          return false;
        }
        return true;
      case 2 : // MMM YYYY
        try {
          month[which] = new Integer( mmm2i( tokens.nextToken() ));
          year [which] = new Integer( tokens.nextToken() );
        } catch (NumberFormatException e) {
          return false;
        }
        return true;
      case 3 : // DD MMM YYYY
        try {
          day  [which] = new Integer( tokens.nextToken() );
          month[which] = new Integer( mmm2i( tokens.nextToken() ));
          year [which] = new Integer( tokens.nextToken() );
        } catch (NumberFormatException e) {
          return false;
        }
        return true;
    }

    // Too much tokens
    return false;
  }

  /**
   * Accessor Day
   */
  public void setDay(int which,Integer set) {
    noteModifiedProperty();
    day[which] = set;
  }

  /**
   * Accessor Format
   */
  public void setFormat(int newFormat) {
    // Valid format ?
    if ((newFormat<DATE) || (newFormat>EST) )
      return;
    noteModifiedProperty();

    // new single date format ?
    if (attrib1[newFormat] == null) {
      setValue(1,null,null,null);
    } else {
      // .. double date format with
      if ( (getYear(1) == null) && (getMonth(1) == null) && (getDay(1) == null) ) {
        setValue(1,getDay(0),getMonth(0),getYear(0));
      }
    }
    // Remember format
    format=newFormat;
    // Done
  }

  /**
   * Sets this property's date (0 or 1)
   */
  public void setValue(int which, Integer setDay, Integer setMonth, Integer setYear) {

    // Remember change
    noteModifiedProperty();

    // Set it
    dateAsString=null;
    day  [which] = setDay;
    month[which] = setMonth;
    year [which] = setYear;

    // Done
  }

  /**
   * Accessor Value
   */
  public boolean setValue(String newValue) {

    noteModifiedProperty();

    // Reset value
    day  [0] = null;
    month[0] = null;
    year [0] = null;
    day  [1] = null;
    month[1] = null;
    year [1] = null;
    format = DATE;
    dateAsString=null;;

    // Empty Date ?
    StringTokenizer tokens = new StringTokenizer(newValue);
    if ( tokens.countTokens() == 0 ) {
      return true;
    }

    // Parsing wrong ?
    if ( parseDate(newValue,tokens) == false ){
      day  [0] = null;
      month[0] = null;
      year [0] = null;
      day  [1] = null;
      month[1] = null;
      year [1] = null;
      format = DATE;

      dateAsString=newValue;
      return false;
    }

    // Everything o.k.
    return true;
  }

  /**
   * Accessor Year
   */
  public void setYear(int which,Integer set) {
    noteModifiedProperty();
    year[which] = set;
  }

  /**
   * Returns this date as a string
   */
  public String toString() {
    return toString(0) + ' ' + toString(1);
  }

  /**
   * Returns part of date as a string
   */
  public String toString(int which) {
    return toString(which, false);
  }

  /**
   * Returns part of date as a string
   */
  public String toString(int which, boolean abbreviate) {

    // Still invalid string information ?
    if (dateAsString!=null) {
      return dateAsString;
    }

    // Abbreviation?
    if (abbreviate) {
      return (i2s(getDay(which))+" "+i2s(getMonth(which))+" "+i2s(getYear(which))).trim();
    }

    // Attribute?
    String attrib = getAttribute(which);

    if (attrib==null) {

      // ... not allowed null for '1'
      if (which==1) {
        return "";
      }

      attrib = "";

    } else {

      attrib = attrib+' ';
    }

    // All !
    return attrib + (i2s(getDay(which))+" "+i2mmm(getMonth(which))+" "+i2s(getYear(which))).trim();
  }
}
