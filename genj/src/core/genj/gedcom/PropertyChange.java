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

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author nmeier
 */
public class PropertyChange extends PropertySimpleValue {
  
  private final static String
   TAG =  "CHAN",
   SUB_TIME = "TIME",
   SUB_DATE = "DATE";
  
  /** internal flag to break endless loops on change */
  private boolean ignoreChangeNotify = false;
  
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
    Property date = lookup(this, SUB_DATE, false);
    return date!=null ? date.getValue() : "";
  }
  
  /**
   * Get the last change time
   */
  public String getTimeAsString() {
    Property date = lookup(this, SUB_DATE, false);
    if (date==null)
      return EMPTY_STRING;
    Property time = lookup(date, SUB_TIME, false);
    return time!=null ? time.getValue() : EMPTY_STRING;
  }
  
  /**
   * Lookup contained DATE
   */
  private Property lookup(Property parent, String tag, boolean create) {
    Property result = parent.getProperty(tag);
    if (result==null&&create) {
      result = new PropertySimpleReadOnly(tag);
      parent.addProperty(result);
    }
    return result;
  }
  
  /**
   * @see genj.gedcom.Property#getTag()
   */
  public String getTag() {
    return TAG;
  }
  
  /**
   * @see genj.gedcom.PropertySimpleValue#setTag(java.lang.String)
   */
  void setTag(String set) {
    // ignored
  }

  /**
   * Static update
   */
  /*package*/ static void update(Entity entity) {
    
    // get PropertyChange
    PropertyChange change = (PropertyChange)entity.getProperty(TAG);
    if (change==null) {
      change = new PropertyChange();
      entity.addProperty(change);
    }
    change.update();
    
    // done
  }
  
  /**
   * Update change date/time
   */
  private void update() {

    // ignore what's going on?
    if (ignoreChangeNotify||!getGedcom().isTransaction())
      return;
          
    ignoreChangeNotify = true;
        
    // update values
    Property 
      date = lookup(this, SUB_DATE, true),
      time = lookup(date, SUB_TIME, true);
    updateDate(date);
    updateTime(time);
    
    // done
    ignoreChangeNotify = false;
  }
  
  /**
   * Update the date portion
   */
  private void updateDate(Property date) {  
    date.setValue(PointInTime.getNow().toGedcomString());
  }
  
  /**
   * Update the time portion
   */
  private void updateTime(Property time) {  
    time.setValue(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
  }
  
} //PropertyChange