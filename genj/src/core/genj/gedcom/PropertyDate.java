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

import genj.gedcom.time.*;
import genj.util.WordBuffer;

import java.util.StringTokenizer;

/**
 * Gedcom Property : DATE
 */
public class PropertyDate extends Property {

  /*package*/ public final static String TAG = "DATE";

  /** time values */
  private PIT 
    start = new PIT(),
    end = new PIT();

  /** the format of the contained date */
  private Format format = DATE;

  /** as string */
  private String dateAsString;

  /** format definitions */
  public final static Format
    DATE        = new Format(false, ""    , ""   , "" , "" ), // DATE
    FROM_TO     = new Format(true , "FROM", "TO" , "" , "-"), // FROM TO
    FROM        = new Format(false, "FROM", ""   , "[", "" ), // FROM
    TO          = new Format(false, "TO"  , ""   , "]", "" ), // TO
    BETWEEN_AND = new Format(true , "BET" , "AND", ">", "<"), // BETAND
    BEFORE      = new Format(false, "BEF" , ""   , "<", "" ), // BEF
    AFTER       = new Format(false, "AFT" , ""   , ">", "" ), // AFT
    ABOUT       = new Format(false, "ABT" , ""   , "~", "" ), // ABT
    CALCULATED  = new Format(false, "CAL" , ""   , "~", "" ), // CAL
    ESTIMATED   = new Format(false, "EST" , ""   , "~", "" ); // EST
  
  public final static Format[] FORMATS = {
    DATE, FROM_TO, FROM, TO, BETWEEN_AND, BEFORE, AFTER, ABOUT, CALCULATED, ESTIMATED
  };
  
  /**
   * Constructor
   */
  public PropertyDate() {
  }

  /**
   * Constructor
   */
  public PropertyDate(int year) {
    getStart().set(PointInTime.UNKNOWN, PointInTime.UNKNOWN, year);
  }

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
  public Format getFormat() {
    return format;
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
      
    // collect information
    WordBuffer result = new WordBuffer();
    result.append(format.start);  
    start.getValue(result);
    result.append(format.end);
    if (isRange()) 
      end.getValue(result);

    // done    
    return result.toString();
  }

  /**
   * Returns whether this date is a range (fromto, betand)
   */
  public boolean isRange() {
    return format.isRange();
  }

  /**
   * Tells whether this date is valid
   * @return <code>boolean</code> indicating validity
   */
  public boolean isValid() {

    // date kept as string?
    if (dateAsString!=null) {
      // these are still ok
      // (DATE_PHRASE)
      // INT DATE (DATE_PHRASE)
      if ( (dateAsString.startsWith("INT ")||dateAsString.startsWith("(")) &&dateAsString.endsWith(")"))
        return true;
      // not ok
      return false;
    }

    // end valid?
    if (isRange()&&!end.isValid())
      return false;

    // start valid?
    if (!start.isValid())
      return false;

    // O.K.
    return true;
  }
  
  /**
   * Check whether this date can be compared successfully to another
   */
  public boolean isComparable() {

    // date kept as string?
    if (dateAsString!=null) 
      return false;
    
    // end valid?
    if (isRange()&&!end.isValid())
      return false;

    // start valid?
    if (!start.isValid())
      return false;

    // O.K.
    return true;
  }
  
  /**
   * Accessir value
   */
  public void setValue(Format newFormat, PointInTime newStart, PointInTime newEnd) {
    
    String old = getValue();
    
    // keep it
    format = newFormat;
    start.set(newStart);
    if (newEnd!=null)
      end.set(newEnd);
    
    // remember as modified      
    propagateChange(old);

    // Done
  }

  /**
   * Accessor Format
   */
  public void setFormat(Format set) {

    String old = getValue();
    
    // set end == start?
    if (!isRange()&&set.isRange()) 
      end.set(start);
    
    // remember
    format = set;
    
    // remember as modified      
    propagateChange(old);

    // Done
  }

  /**
   * Accessor Value
   */
  public void setValue(String newValue) {

    String old = getValue();

    // Reset value
    start.reset();
    end.reset();
    format = DATE;
    dateAsString=null;

    // parse and keep string if no good
    newValue = newValue.trim();
    if (!parseDate(newValue))
      dateAsString=newValue;

    // remember as modified      
    propagateChange(old);

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
    for (int f=0;f<FORMATS.length;f++) {

      // .. found modifier (prefix is enough: e.g. ABT or ABT.)
      format = FORMATS[f];
      if ( (format.start.length()>0) && token.startsWith(format.start) ) {

        // ... no range (TO,ABT,CAL,...) -> parse PointInTime from remaining tokens
        if ( !format.isRange ) 
          return start.set(tokens);

        // ... is range (FROM-TO,BET-AND)
        String grab = "";
        while (tokens.hasMoreTokens()) {
          // .. TO or AND ? -> parse 2 PointInTimes from grabbed and remaining tokens
          token = tokens.nextToken();
          if ( token.startsWith(format.end) ) {
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
  /*package*/ Property init(MetaProperty meta, String value) throws GedcomException {
    assume(TAG.equals(meta.getTag()), UNSUPPORTED_TAG);
    return super.init(meta, value);
  }

  /**
   * Returns this date as a localized string for display
   */
  public String getDisplayValue() {
    return getDisplayValue(null);
  }
  
  /**
   * Returns this date as a localized string for display
   */
  public String getDisplayValue(Calendar calendar) {
    
    // as string?
    if (dateAsString!=null) 
      return dateAsString;
      
    // prepare modifiers
    String
      smod = format.start,
      emod = format.end  ;
      
    if (smod.length()>0)
      smod = Gedcom.getResources().getString("prop.date.mod."+smod);  
    if (emod.length()>0)
      emod = Gedcom.getResources().getString("prop.date.mod."+emod);  

    // collect information
    try {
      WordBuffer result = new WordBuffer();
      
      // start modifier & point in time
      result.append(smod);
      if (calendar==null||start.getCalendar()==calendar) 
        start.toString(result, true);
      else 
        start.getPointInTime(calendar).toString(result, true);
  
      // end modifier & point in time
      if (isRange()) {
        result.append(emod);
        if (calendar==null||end.getCalendar()==calendar) 
          end.toString(result,true);
        else 
          end.getPointInTime(calendar).toString(result, true);
      }
  
      // done    
      return result.toString();
      
    } catch (GedcomException e) {
      // done in case of error
      return "";
    }
  }
  
  /**
   * @see genj.gedcom.Property#getPropertyInfo()
   */
  public String getPropertyInfo() {
    WordBuffer result = new WordBuffer();
    result.append(super.getPropertyInfo());
    result.append("<br>");
    result.append(getDisplayValue());
    if (!(getStart().isGregorian()&&getEnd().isGregorian())) {
      result.append("<br>");
      result.append(getDisplayValue(PointInTime.GREGORIAN));
      result.append("("+PointInTime.GREGORIAN.getName()+")");
    }
    return result.toString();
  }

  /** 
   * A point in time 
   */
  private class PIT extends PointInTime {
    
    /**
     * Setter
     */
    public void set(int d, int m, int y) {

      String old = getValue();
      
      // assume string is not needed anymore
      dateAsString=null;
      
      // set it
      super.set(d,m,y);

      // note change
      propagateChange(old);

      // done
    }
    
  } // class PointInTime
  
  /**
   * A format definition
   */
  public static class Format {
    
    protected boolean isRange;
    
    protected String start, end;
    
    private Format(boolean r, String s, String e, String as, String ae) {
      isRange= r; 
      start  = s; 
      end    = e;
      //astart = as;
      //aend   = ae;
    }
    
    public boolean isRange() {
      return isRange;
    }

    public String getLabel() {
      String key = (start+end).toLowerCase();
      if (key.length()==0)
        key = "date";
      return resources.getString("prop.date."+key);
    }
    
    public String getLabel1() {
      if (start.length()==0)
        return "";
      return resources.getString("prop.date.mod."+start);
    }
    
    public String getLabel2() {
      if (end.length()==0)
        return "";
      return resources.getString("prop.date.mod."+end);
    }
    
  } //Format
  
} //PropertyDate
