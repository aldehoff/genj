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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * CHAN is used by Gedcom to keep track of changes done to entity records.
 * We simply fold 
 * <pre>
 *  1 CHAN
 *  2  DATE
 *  3   TIME
 * </pre>
 * into this one property, update the pointintime and time and offer
 * the same three lines on save
 * @author nmeier
 */
public class PropertyChange extends Property implements MultiLineSupport {

  //tried to parse the decimal-fraction of a second, too, but SS is milliseconds
  //new SimpleDateFormat("HH:mm:ss.SS"), 
  
  private final static SimpleDateFormat[] FORMATS = { 
    new SimpleDateFormat("HH:mm:ss"),
    new SimpleDateFormat("HH:mm")
  };
  
  private final static String
   CHAN = "CHAN",
   TIME = "TIME",
   DATE = "DATE";
  
  private PointInTime pit = PointInTime.getPointInTime(-1,-1,-1);
  private long time = -1;

  /**
   * @see genj.gedcom.Property#isReadOnly()
   */
  public boolean isReadOnly() {
    return true;
  }
  
  /**
   * @see genj.gedcom.Property#isSystem()
   */
  public boolean isSystem() {
    return true;
  }

  /**
   * Get the last change date
   */
  public String getDateAsString() {
    return pit.toString(new WordBuffer(), true).toString();    
  }
  
  /**
   * Get the last change time
   */
  public String getTimeAsString() {
    return time<0 ? EMPTY_STRING : FORMATS[0].format(new Date(time));
  }
  
  /**
   * @see genj.gedcom.Property#getTag()
   */
  public String getTag() {
    return CHAN;
  }
  
  /**
   * @see genj.gedcom.PropertySimpleValue#setTag(java.lang.String)
   */
  Property init(String set, String value) throws GedcomException {
    assume(CHAN.equals(set), UNSUPPORTED_TAG);
    return super.init(set,value);
  }

  /**
   * Static update
   */
  /*package*/ static void update(Entity entity) {
    
    // get PropertyChange
    PropertyChange change = (PropertyChange)entity.getProperty(CHAN);
    if (change==null) {
      change = (PropertyChange)MetaProperty.get(entity, CHAN).create("");
      entity.addProperty(change);
    }
    
    // update values
    change.pit = PointInTime.getNow();
    change.time = System.currentTimeMillis();

    // done
  }
  
  /**
   * Callback for properties that we might be able to
   * append - we do TIME and DATE
   * @see genj.gedcom.MultiLineSupport#append(int, java.lang.String, java.lang.String)
   */
  public boolean append(int level, String tag, String value) {
    
    // DATE
    if (level==1&&DATE.equals(tag)) { 
      pit = PointInTime.getPointInTime(value);
      return true;
    }
    
    // TIME
    if (level==2&&TIME.equals(tag)) {
      time = -1;
      
      for (int f=0;f<FORMATS.length;f++) {
        try {
          time = FORMATS[f].parse(value).getTime();
          break;
        } catch (Throwable t) {
        }
      }
      
      return true;
    }
    
    // don't know it
    return false;
  }

  /**
   * @see genj.gedcom.MultiLineSupport#getLines()
   */
  public Line getLines() {
    return new Lines();
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#getLinesValue()
   */
  public String getLinesValue() {
    return EMPTY_STRING;
  }
  
  /**
   * @see genj.gedcom.Property#setValue(java.lang.String)
   */
  public void setValue(String value) {
    // ignored
  }
  
  /**
   * Will only be used for display - data access will use getLines()
   * @see genj.gedcom.Property#getValue()
   */
  public String getValue() {
    return getDateAsString()+' '+getTimeAsString();
  }
  
  /**
   * @see genj.gedcom.Property#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    // safety check
    if (!(o instanceof PropertyChange))
      return super.compareTo(o);
    // compare pit
    PropertyChange other = (PropertyChange)o;
    int result = pit.compareTo(other.pit);
    if (result!=0)
      return result;
    // compare time
    return (int)(time-other.time);
  }

  /**
   * Iterator for lines
   */
  private class Lines implements MultiLineSupport.Line {
    /** tracking line index */
    int i = 0;
    /** lines */
    private String[] 
      tags = { CHAN, DATE, TIME  },
      values = { EMPTY_STRING, pit.toGedcomString(), getTimeAsString() };
    private int[]
      indents = { 1, 2};
    /**
     * @see genj.gedcom.MultiLineSupport.Line#getTag()
     */
    public String getTag() {
      return tags[i];
    }
    /**
     * @see genj.gedcom.MultiLineSupport.Line#getValue()
     */
    public String getValue() {
      return values[i];
    }
    /**
     * @see genj.gedcom.MultiLineSupport.Line#next()
     */
    public int next() {
      return i==indents.length ? 0 : indents[i++];
    }
  } //Lines

  
} //PropertyChange