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
public class PropertyChange extends PropertyNoValue {
  
  private final static String
   SUB_TIME = "TIME",
   SUB_DATE = "DATE";
  
  private boolean ignoreChangeNotify = false;
  
  /**
   * Update change date/time
   */
  /*package*/ void update() {

    // ignore what's going on?
    if (ignoreChangeNotify||!getGedcom().isTransaction())
      return;
          
    ignoreChangeNotify = true;
        
    // make sure we got date/time
    Property date = getProperty(SUB_DATE);
    if (date==null) {
      date = new PropertySimpleValue(SUB_DATE);
      addProperty(date);
    }
    Property time = date.getProperty(SUB_TIME);
    if (time==null) {
      time = new PropertySimpleValue(SUB_TIME);
      date.addProperty(time);
    }
    
    // update values
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