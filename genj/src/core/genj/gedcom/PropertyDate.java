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

import java.util.StringTokenizer;

/**
 * Gedcom Property : DATE
 */
public class PropertyDate extends Property {

  /*package*/ final static String TAG = "DATE";

  /** time values */
  private PIT 
    start = new PIT(),
    end = new PIT();

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
    new FormatDescriptor(false, ""    , ""   , "" , "" ), // DATE
    new FormatDescriptor(true , "FROM", "TO" , "" , "-"), // FROM TO
    new FormatDescriptor(false, "FROM", ""   , "[", "" ), // FROM
    new FormatDescriptor(false, "TO"  , ""   , "]", "" ), // TO
    new FormatDescriptor(true , "BET" , "AND", ">", "<"), // BETAND
    new FormatDescriptor(false, "BEF" , ""   , "<", "" ), // BEF
    new FormatDescriptor(false, "AFT" , ""   , ">", "" ), // AFT
    new FormatDescriptor(false, "ABT" , ""   , "~", "" ), // ABT
    new FormatDescriptor(false, "CAL" , ""   , "~", "" ), // CAL
    new FormatDescriptor(false, "EST" , ""   , "~", "" )  // EST
  };

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(Object o) {
    if (!(o instanceof PropertyDate)) return super.compareTo(o);
    return start.compareTo(((PropertyDate)o).start);
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
      return EMPTY_STRING;
      
    return Gedcom.getResources().getString(res);
  }

  /**
   * Returns generic proxy's logical name
   */
  public String getProxy() {
    if (dateAsString!=null) return super.getProxy();
    return "Date";
  }

  /**
   * Accessor Tag
   */
  public String getTag() {
    return TAG;
  }

  /**
   * Accessor Value
   */
  public String getValue() {
    return toString(false,false);
  }

  /**
   * Returns whether this date is a range (fromto, betand)
   */
  public boolean isRange() {
    return isRange(format);
  }

  /**
   * Returns whether given format is a range (fromto, betand)
   */
  public static boolean isRange(int format) {
    return formats[format].isRange;
  }

  /**
   * Tells whether this date is valid
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
      if ( (formats[format].start.length()>0) && token.startsWith(formats[format].start) ) {

        // ... no range (TO,ABT,CAL,...) -> parse PointInTime from remaining tokens
        if ( !formats[format].isRange ) 
          return start.set(tokens);

        // ... is range (FROM-TO,BET-AND)
        String grab=EMPTY_STRING;
        while (tokens.hasMoreTokens()) {
          // .. TO or AND ? -> parse 2 PointInTimes from grabbed and remaining tokens
          token = tokens.nextToken();
          if ( token.startsWith(formats[format].end) ) {
            return start.set(new StringTokenizer(grab)) && end.set(tokens);
          }
          // .. grab more
          grab += " " + token + " ";
        }
        
        // ... wasn't so good after all
        // NM 20021009 reset data - FROM 1 OCT 2001 will then
        // fallback to FROM even after FROMTO was checked
        tokens = new StringTokenizer(string);
        token = tokens.nextToken();
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
    modNotify();

    // remember
    format=newFormat;
    
    // set end == start?
    if (!isRange()&&isRange(format)) 
      end.set(start);
    
    // Done
  }

  /**
   * Accessor Value
   */
  public void setValue(String newValue) {

    modNotify();

    // Reset value
    start.reset();
    end.reset();
    format = DATE;
    dateAsString=null;

    // Empty Date ?
    StringTokenizer tokens = new StringTokenizer(newValue);
    if ( tokens.countTokens() == 0 ) {
      return;
    }

    // Parsing wrong ?
    if ( parseDate(newValue,tokens) == false ){
      dateAsString=newValue;
      return;
    }

    // Everything o.k.
    return;
  }

  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  /*package*/ void setTag(String tag) throws GedcomException {
    assume(TAG.equals(tag), UNSUPPORTED_TAG);
  }
  
  /**
   * @see genj.gedcom.Property#toString()
   */
  public String toString() {
    return toString(false, true);
  }

  /**
   * Returns this date as a string
   */
  public String toString(boolean abbreviate, boolean localize) {
    
    // as string?
    if (dateAsString!=null) 
      return dateAsString;
      
    // what's our format descriptor?
    FormatDescriptor fd = formats[format]; 
      
    // prepare modifiers
    String
      smod = abbreviate ? fd.astart : fd.start,
      emod = abbreviate ? fd.aend   : fd.end  ;
      
    if (!abbreviate&&localize) {
      if (smod.length()>0)
        smod = Gedcom.getResources().getString("prop.date.mod."+smod);  
      if (emod.length()>0)
        emod = Gedcom.getResources().getString("prop.date.mod."+emod);  
    }
      
    // collect information
    WordBuffer result = new WordBuffer();
    result.append(smod);  
    start.toString(result,localize);
    result.append(emod);
    if (isRange()) end.toString(result,localize);

    // done    
    return result.toString();
  }

  /** 
   * A point in time 
   */
  private class PIT extends PointInTime {
    
    /** content */
    private int 
      year = -1,
      month = -1,
      day = -1;
    
    /**
     * Returns the year
     */
    public int getYear() {
      if (end==this&&!isRange())
        return start.getYear();
      return year;
    }
  
    /**
     * Returns the month
     */
    public int getMonth() {
      if (end==this&&!isRange())
        return start.getMonth();
      return month;
    }

    /**
     * Returns the day
     */
    public int getDay() {
      if (end==this&&!isRange())
        return start.getDay();
      return day;
    }

    /**
     * Setter
     */
    public void set(int d, int m, int y) {
      
      // note change
      modNotify();

      // assume string is not needed anymore
      dateAsString=null;
      
      // set it
      day   = d;
      month = m;
      year  = y;
      
      // done
    }
    
    /**
     * Setter
     */
    private void reset() {
      set(-1,-1,-1);
    }
    
    /**
     * Setter
     */
    private void set(PIT other) {
      set(other.day, other.month, other.year);
    }
    
  } // class PointInTime
  
  /**
   * A format definition
   */
  private static class FormatDescriptor {
    protected boolean isRange;
    protected String start, end;
    protected String astart, aend;
    protected FormatDescriptor(boolean r, String s, String e, String as, String ae) {
      isRange= r; 
      start  = s; 
      end    = e;
      astart = as;
      aend   = ae;
    }
  } //FormatDescriptor
  
} //PropertyDate
