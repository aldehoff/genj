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
  private PointInTime 
    start = new PointInTime(),
    end = new PointInTime();

  /** the format of the contained date */
  private int format = DATE;

  /** as string */
  private String dateAsString;

  /** formats */
  public static final int
    DATE    = 0,			// unable to parse - use dateAsString
    FROMTO  = 1,
    FROM    = 2,
    TO      = 3,
    BETAND  = 4,
    BEF     = 5,
    AFT     = 6,
    ABT     = 7,
    CAL     = 8,
    EST     = 9,
    LAST_ATTRIB = EST;
    
  /** format definitions */
  private final static FormatDescriptor[] formats = {
    new FormatDescriptor(false, ""    , ""   ), // DATE
    new FormatDescriptor(true , "FROM", "TO" ), // FROM TO
    new FormatDescriptor(false, "FROM", ""   ), // FROM
    new FormatDescriptor(false, "TO"  , ""   ), // TO
    new FormatDescriptor(true , "BET" , "AND"), // BETAND
    new FormatDescriptor(false, "BEF" , ""   ), // BEF
    new FormatDescriptor(false, "AFT" , ""   ), // AFT
    new FormatDescriptor(false, "ABT" , ""   ), // ABT
    new FormatDescriptor(false, "CAL" , ""   ), // CAL
    new FormatDescriptor(false, "EST" , ""   )  // EST
  };

  /** month names */
  private final static String months[] = {"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};

  /**
   * Constructor
   */
  public PropertyDate() {
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
    return start.compareTo(p.start);
  }

  /**
   * Returns starting point
   */
  public PointInTime getStart() {
    return start;
  }

  /**
   * Returns ending point
   */
  public PointInTime getEnd() {
    return end;
  }

  /**
   * Returns the format of this date
   */
  public int getFormat() {
    return format;
  }

  /**
   * Returns label for given format type
   */
  public static String getLabelForFormat(int type) {

    String res=null;

    switch (type) {
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
    return toString(false,false);
  }

  /**
   * Returns wether this date is a range (fromto, betand)
   */
  public boolean isRange() {
    return isRange(format);
  }

  /**
   * Returns wether given format is a range (fromto, betand)
   */
  public static boolean isRange(int format) {
    return formats[format].isRange;
  }

  /**
   * Tells wether this date is valid
   * @return <code>boolean</code> indicating validity
   */
  public boolean isValid() {

    // Still invalid string information ?
    if (dateAsString!=null)
      return false;

    // end valid?
    if (isRange()&&(!end.isValid()))
      return false;

    // start valid?
    if (!start.isValid())
      return false;

    // O.K.
    return true;
  }

  /**
   * Helper that parses date as string for validity
   */
  private boolean parseDate(String string, StringTokenizer tokens) {

    // No words -> no date
    if (tokens.countTokens()==0)
      return false;

    // Look for format token 'FROM', 'AFT', ...
    String token = tokens.nextToken();
    for (format=0;format<formats.length;format++) {

      // .. found modifier (prefix is enough: e.g. ABT or ABT.)
      if ( (formats[format].startModifier.length()>0) && token.startsWith(formats[format].startModifier) ) {

        // ... no range (TO,ABT,CAL,...) -> parse PointInTime from remaining tokens
        if ( !formats[format].isRange ) 
          return start.set(tokens);

        // ... is range (FROM-TO,BET-AND)
        String grab="";
        while (tokens.hasMoreTokens()) {
          // .. TO or AND ? -> parse 2 PointInTimes from grabbed and remaining tokens
          token = tokens.nextToken();
          if ( token.startsWith(formats[format].endModifier) ) {
            return start.set(new StringTokenizer(grab)) && end.set(tokens);
          }
          // .. grab more
          grab += " " + token + " ";
        }
        // ... wasn't so good after all
      }
      // .. try next one
    }

    // ... no valid type found ?
    format = DATE;
    
    // .. look for date from first to last word
    return start.set(new StringTokenizer(string));
  }

  /**
   * Accessor Format
   */
  public void setFormat(int newFormat) {
    
    // Valid format ?
    if ((newFormat<DATE) || (newFormat>EST) )
      throw new IllegalArgumentException("Unknown format '"+newFormat+"'");

    // remember as modified      
    noteModifiedProperty();

    // remember
    if (!isRange()&&isRange(newFormat)) {
      end.set(start);
    }
    format=newFormat;
    
    // Done
  }

  /**
   * Accessor Value
   */
  public boolean setValue(String newValue) {

    noteModifiedProperty();

    // Reset value
    start.set(null,null,null);
    end.set(null,null,null);
    format = DATE;
    dateAsString=null;

    // Empty Date ?
    StringTokenizer tokens = new StringTokenizer(newValue);
    if ( tokens.countTokens() == 0 ) {
      return true;
    }

    // Parsing wrong ?
    if ( parseDate(newValue,tokens) == false ){
      dateAsString=newValue;
      return false;
    }

    // Everything o.k.
    return true;
  }

  /**
   * Returns this date as a string
   */
  public String toString() {
    return toString(false,true);
  }
    
  /**
   * Returns this date as a string
   */
  public String toString(boolean abbreviate, boolean localize) {
    if (dateAsString!=null) return dateAsString;
    WordBuffer result = new WordBuffer(start.toString(abbreviate,localize));
    if (isRange()) {
      if (abbreviate) result.append("-");
      result.append(end.toString(abbreviate,localize));
    }
    return result.toString();
  }

  /** 
   * A point in time 
   */
  public class PointInTime {
    
    /** content */
    protected Integer year,month,day;
    
    /**
     * Returns the year
     */
    public int getYear(int fallback) {
      if (year==null) return fallback;
      return year.intValue();
    }
  
    /**
     * Returns the month
     */
    public int getMonth(int fallback) {
      if (month==null) return fallback;
      return month.intValue();
    }
  
    /**
     * Returns the day
     */
    public int getDay(int fallback) {
      if (day==null) return fallback;
      return day.intValue();
    }
  
    /**
     * Returns the year
     */
    public Integer getYear() {
      return year;
    }
  
    /**
     * Returns the month
     */
    public Integer getMonth() {
      return month;
    }
  
    /**
     * Returns the day
     */
    public Integer getDay() {
      return day;
    }
  
    /**
     * compare to other
     */  
    protected int compareTo(PointInTime other) {
      
      int result;
      
      // Year ?
      if ((result=compare(year, other.year))!=0) return result;
      
      // Month
      if ((result=compare(month, other.month))!=0) return result;
      
      // Day
      if ((result=compare(day, other.day))!=0) return result;
      
      // Equal
      return 0;
    }    
    
    /**
     * helper for comparison
     */
    private int compare(Integer one, Integer two) {
      if ((one==null)&&(two==null)) return 0;
      if (one==null) return -1;
      if (two==null) return  1;
      if (one.intValue()<two.intValue()) return -1;
      if (one.intValue()>two.intValue()) return  1;
      return 0;
    }
    
    /**
     * Checks for validity
     */
    private boolean isValid() {
  
      // YYYY or MMM YYYY or DD MMMM YYYY
      if (year==null)
        return false;
      if ((month!=null)&&(month.intValue()>=1)&&(month.intValue()<=12))
        return true;
      if (day!=null)
        return false;
      return true;
    }
    
    /**
     * Setter
     */
    protected void set(PointInTime other) {
      // Remember change
      noteModifiedProperty();
      // set
      year  = other.year;
      month = other.month;
      day   = other.day;
    }
    
    /**
     * Setter
     */
    public void set(Integer d, Integer m, Integer y) {

      // Remember change
      noteModifiedProperty();

      // Set it
      dateAsString=null;
      
      day   = d;
      if ((m!=null)&&(m.intValue()<1||m.intValue()>12)) {} else month = m;
      year  = y;
  
      // Done
    }
    
    /**
     * Setter
     */
    protected boolean set(StringTokenizer tokens) {
  
      // Number of tokens ?
      switch (tokens.countTokens()) {
        default : // TOO MANY
          return false;
        case 0 : // NONE
          return false;
        case 1 : // YYYY
          try {
            year = new Integer( tokens.nextToken() );
          } catch (NumberFormatException e) {
            return false;
          }
          return true;
        case 2 : // MMM YYYY
          try {
            month = parseMonth ( tokens.nextToken() );
            year  = new Integer( tokens.nextToken() );
          } catch (NumberFormatException e) {
            return false;
          }
          break;
        case 3 : // DD MMM YYYY
          try {
            day   = new Integer( tokens.nextToken() );
            month = parseMonth ( tokens.nextToken() );
            year  = new Integer( tokens.nextToken() );
          } catch (NumberFormatException e) {
            return false;
          }
          break;
      }
  
      // Passed
      return true;
    }
    
    /**
     * String representation
     */
    public String toString() {
      return toString(false,true);
    }
    
    /**
     * String representation
     */
    public String toString(boolean abbreviate, boolean localize) {
      WordBuffer result = new WordBuffer();
      if (!abbreviate) {
        result.append(getModifier(localize));
      }
      return result.append(getDay()).append(getMonth(localize)).append(getYear()).toString();
    }
    
    /**
     * Acessor - the modifier (e.g. 'FROM', 'BEF')
     */
    private String getModifier(boolean localize) {
      String mod = (start == this ? formats[format].startModifier : formats[format].endModifier);
      if ((mod.length()>0)&&localize) mod = Gedcom.getResources().getString("prop.date.mod."+mod);
      return mod;
    }

    /**
     * Accessor - the month
     */
    private String getMonth(boolean localize) {
      if (month==null)
        return "";
      String mmm = months[month.intValue()-1];
      if (localize) mmm = Gedcom.getResources().getString("prop.date.mon."+mmm);
      return mmm;
    }
    
    /**
     * Helper that transforms month to Integer
     */
    private Integer parseMonth(String mmm) throws NumberFormatException {
      for (int i=0;i<months.length;i++) {
        if (months[i].equalsIgnoreCase(mmm)) return new Integer(i+1);
      }
      throw new NumberFormatException();
    }
  
  } // class PointInTime
  
  /**
   * A format definition
   */
  private static class FormatDescriptor {
    protected boolean isRange;
    protected String startModifier, endModifier;
    protected FormatDescriptor(boolean r, String s, String e) {
      isRange=r; startModifier=s; endModifier=e;
    }
  }
}
