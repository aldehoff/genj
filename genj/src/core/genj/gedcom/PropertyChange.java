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
public class PropertyChange extends Property implements MultiLineProperty {

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
  
  private PointInTime pit = new PointInTime();
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
      // is allowed?
      MetaProperty ment = MetaProperty.get(entity);
      if (!ment.allows(CHAN))
        return;
      // create instance
      change = (PropertyChange)ment.get(CHAN, true).create("");
      entity.addProperty(change);
    }
    
    // update values
    change.pit = PointInTime.getNow();
    change.time = System.currentTimeMillis();

    // done
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#getContinuation()
   */
  public Collector getLineCollector() {
    return new DateTimeCollector();
  }
    
  /**
   * @see genj.gedcom.MultiLineSupport#getLines()
   */
  public Iterator getLineIterator() {
    return new DateTimeIterator();
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#getLinesValue()
   */
  public String getLinesValue() {
    return getValue();
  }
  
  /**
   * @see genj.gedcom.Property#setValue(java.lang.String)
   */
  public void setValue(String value) {
    // must look like 19 DEC 2003,14:50
    int i = value.indexOf(',');
    if (i<0) 
      return;
    // parse pit
    pit = PointInTime.getPointInTime(value.substring(0,i));
    // parse tiem 
    for (int f=0;f<FORMATS.length;f++) {
      try {
        time = FORMATS[f].parse(value.substring(i+1)).getTime();
        break;
      } catch (Throwable t) {
        time = -1;
      }
    }
    // done
  }
  
  /**
   * Will only be used for display - data access will use getLines()
   * @see genj.gedcom.Property#getValue()
   */
  public String getValue() {
    return getDateAsString()+','+getTimeAsString();
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
   * @see genj.gedcom.Property#setPrivate(boolean, boolean)
   */
  public void setPrivate(boolean set, boolean recursively) {
    // ignored
  }

  /**
   * Continuation for handling multiple lines concerning this change
   */
  private class DateTimeCollector implements MultiLineProperty.Collector {

    private String date, time;
    
    /**
     * @see genj.gedcom.MultiLineSupport.Continuation#append(int, java.lang.String, java.lang.String)
     */
    public boolean append(int indent, String tag, String value) {
      
      // DATE
      if (indent==1&&DATE.equals(tag)) {
        date = value; 
        return true;
      }
    
      // TIME
      if (indent==2&&TIME.equals(tag)) {
        time = value;
        return true;
      }
      
      // unknown
      return false;
    }
    
    /**
     * @see genj.gedcom.MultiLineProperty.Collector#getValue()
     */
    public String getValue() {
      return date+','+time;
    }
    
  } //MyContinuation

  /**
   * Iterator for lines wrapped in this change
   */
  private class DateTimeIterator implements MultiLineProperty.Iterator {
    
    /** tracking line index */
    int i = 0;
    
    /** lines */
    private String[] 
      tags = { CHAN, DATE, TIME  },
      values = { EMPTY_STRING, pit.getValue(), getTimeAsString() };
      
    /**
     * @see genj.gedcom.MultiLineProperty.Iterator#setValue(java.lang.String)
     */
    public void setValue(String value) {
      // ignored
    }
      
    /**
     * @see genj.gedcom.MultiLineSupport.LineIterator#getIndent()
     */
    public int getIndent() {
      return i;
    }
      
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
    public boolean next() {
      return ++i!=tags.length;
    }
    
  } //Lines

  
} //PropertyChange