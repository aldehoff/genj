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

import genj.gedcom.time.PointInTime;
import genj.util.WordBuffer;

import java.text.DecimalFormat;
import java.util.StringTokenizer;

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

  private final static DecimalFormat decimal = new DecimalFormat("00");
  
  private final static String
   CHAN = "CHAN",
   TIME = "TIME",
   DATE = "DATE";
  
  private long time = -1;

  /**
   * @see genj.gedcom.Property#isReadOnly()
   */
  public boolean isReadOnly() {
    return true;
  }
  
  /**
   * Get the last change date
   */
  public String getDateAsString() {
    return time<0 ? "" : PointInTime.getPointInTime(time).toString(new WordBuffer(), true).toString();    
  }
  
  /**
   * Get the last change time
   */
  public String getTimeAsString() {
    if (time<=0)
      return "";

    long
      sec = (time/1000)%60,
      min = (time/1000/60)%60,
      hr  = (time/1000/60/60)%24;
      
    StringBuffer buffer = new StringBuffer();
    buffer.append(decimal.format(hr));
    buffer.append(':');
    buffer.append(decimal.format(min));
    buffer.append(':');
    buffer.append(decimal.format(sec));
    
    return buffer.toString();
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
  Property init(MetaProperty meta, String value) throws GedcomException {
    meta.assertTag(CHAN);
    return super.init(meta,value);
  }

  /**
   * Static update
   */
  /*package*/ static void update(Entity entity, Transaction tx, Change change) {

    // tx isn't rollback?
    if (tx.isRollback())
      return;

    // change itself?
    if (change instanceof Change.PropertyAdd && ((Change.PropertyAdd)change).getAdded() instanceof PropertyChange)
      return;
    if (change instanceof Change.PropertyDel && ((Change.PropertyDel)change).getRemoved() instanceof PropertyChange)
      return;
    if (change instanceof Change.PropertyValue && ((Change.PropertyValue)change).getChanged() instanceof PropertyChange)
      return;
  
    // is allowed?
    MetaProperty meta = entity.getMetaProperty();
    if (!meta.allows(CHAN))
      return;
      
    // update values (tx time is UTC time!)
    PropertyChange prop = (PropertyChange)entity.getProperty(CHAN);
    if (prop==null) {
      prop = (PropertyChange)meta.getNested(CHAN, true).create("");
      prop.setValue(tx.getTime());
      entity.addProperty(prop, Integer.MAX_VALUE);
    } else {
      prop.setValue(tx.getTime());
    }
    
  
    // done
  }
  
  /**
   * @see genj.gedcom.MultiLineProperty#getLineCollector()()
   */
  public Collector getLineCollector() {
    return new DateTimeCollector();
  }
    
  /**
   * @see genj.gedcom.MultiLineProperty#getLineIterator()
   */
  public Iterator getLineIterator() {
    return new DateTimeIterator();
  }
  
  /**
   * Set current value
   */  
  public void setValue(long set) {

    // known?
    if (time==set)
      return;

    String old = getValue();
    
    // keep time before propagate so no endless loop happens
    time = set;
    
    // notify
    propagateChange(old);
    
    // done
  }
  
  /**
   * @see genj.gedcom.Property#setValue(java.lang.String)
   */
  public void setValue(String value) {
    
    String old = getValue();

    // must look like 19 DEC 2003,14:50
    int i = value.indexOf(',');
    if (i<0) 
      return;

    try {
      time = 0;

      // parse time hh:mm:ss
      StringTokenizer tokens = new StringTokenizer(value.substring(i+1), ":");
      if (tokens.hasMoreTokens())
        time += Integer.parseInt(tokens.nextToken()) * 60 * 60 * 1000;
      if (tokens.hasMoreTokens())
        time += Integer.parseInt(tokens.nextToken()) * 60 * 1000;
      if (tokens.hasMoreTokens())
        time += Integer.parseInt(tokens.nextToken()) * 1000;
        
      // parse date
      time += PointInTime.getPointInTime(value.substring(0,i)).getTimeMillis();
      
    } catch (Throwable t) {

      time = -1;
    }
    
    // notify
    propagateChange(old);
    
    // done
  }
  
  /**
   * Gedcom value - this is an intermittend value only that won't be saved (it's not Gedcom compliant but contains a valid gedcom date)
   */
  public String getValue() {
    return time<0 ? "" : PointInTime.getPointInTime(time).getValue() +','+getTimeAsString();
  }
  
  /**
   * A display value - the date/time localized
   */
  public String getDisplayValue() {
    return time<0 ? "" : getDateAsString() +','+getTimeAsString();
  }
  
  /**
   * @see genj.gedcom.Property#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    // safety check
    if (!(o instanceof PropertyChange))
      return super.compareTo(o);
    // compare time
    PropertyChange other = (PropertyChange)o;
    if (time<other.time)
      return -1;
    if (time>other.time)
      return 1;
    return 0;
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
      values = { "", PointInTime.getPointInTime(time).getValue(), getTimeAsString() };
      
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