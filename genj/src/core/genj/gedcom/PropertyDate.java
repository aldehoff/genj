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
    
    // as string?
    if (dateAsString!=null) 
      return dateAsString;
      
    // what's our format descriptor?
    FormatDescriptor fd = formats[format]; 
      
    // collect information
    WordBuffer result = new WordBuffer();
    result.append(fd.start);  
    start.getValue(result);
    result.append(fd.end);
    if (isRange()) 
      end.getValue(result);

    // done    
    return result.toString();
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

    // parse and keep string if no good
    if (!parseDate(newValue))
      dateAsString=newValue;

    // done
  }

  /**
   * Helper that parses date as string for validity
   */
  private boolean parseDate(String text) {

    // empty string is fine
    StringTokenizer tokens = new StringTokenizer(text);
    if (tokens.countTokens()==0)
      return true;

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
        tokens = new StringTokenizer(text);
        token = tokens.nextToken();
      }
      // .. try next one
    }

    // ... format is a simple date
    format = DATE;
    
    // .. look for date from first to last word
    return start.set(new StringTokenizer(text));
  }

  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  /*package*/ Property init(String tag, String value) throws GedcomException {
    assume(TAG.equals(tag), UNSUPPORTED_TAG);
    return super.init(tag,value);
  }
  
  /**
   * @see genj.gedcom.Property#toString()
   */
  public String toString() {
    return toString(true);
  }

  /**
   * Returns this date as a string
   */
  public String toString(boolean localize) {
    return toString(localize, null);
  }
  
  /**
   * Returns this date as a string
   */
  public String toString(boolean localize, PointInTime.Calendar calendar) {
    
    // as string?
    if (dateAsString!=null) 
      return dateAsString;
      
    // what's our format descriptor?
    FormatDescriptor fd = formats[format]; 
      
    // prepare modifiers
    String
      smod = fd.start,
      emod = fd.end  ;
      
    if (localize) {
      if (smod.length()>0)
        smod = Gedcom.getResources().getString("prop.date.mod."+smod);  
      if (emod.length()>0)
        emod = Gedcom.getResources().getString("prop.date.mod."+emod);  
    }

    // collect information
    try {
      WordBuffer result = new WordBuffer();
      
      // start modifier & point in time
      result.append(smod);
      if (calendar==null||start.getCalendar()==calendar) 
        start.toString(result, localize);
      else 
        start.getPointInTime(calendar).toString(result, localize);
  
      // end modifier & point in time
      if (isRange()) {
        result.append(emod);
        if (calendar==null||end.getCalendar()==calendar) 
          end.toString(result,localize);
        else 
          end.getPointInTime(calendar).toString(result, localize);
      }
  
      // done    
      return result.toString();
      
    } catch (GedcomException e) {
      // done in case of error
      return "";
    }
  }
  
  /**
   * @see genj.gedcom.Property#getInfo()
   */
  public String getInfo() {
    return super.getInfo() + ' ' + toString(true, PointInTime.GREGORIAN);
  }

  /** 
   * A point in time 
   */
  private class PIT extends PointInTime {
    
    /**
     * Setter
     */
    public void set(int d, int m, int y) {
      
      // note change
      modNotify();

      // assume string is not needed anymore
      dateAsString=null;
      
      // set it
      super.set(d,m,y);

      // done
    }
    
    /**
     * Setter
     */
    private void reset() {
      set(-1,-1,-1);
    }
    
  } // class PointInTime
  
  /**
   * A format definition
   */
  private static class FormatDescriptor {
    protected boolean isRange;
    protected String start, end;
    //protected String astart, aend;
    protected FormatDescriptor(boolean r, String s, String e, String as, String ae) {
      isRange= r; 
      start  = s; 
      end    = e;
      //astart = as;
      //aend   = ae;
    }
  } //FormatDescriptor
  
} //PropertyDate
