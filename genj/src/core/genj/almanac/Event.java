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
package genj.almanac;

import genj.gedcom.GedcomException;
import genj.gedcom.time.PointInTime;
import genj.util.WordBuffer;

/**
 * A CDay event
 */
public class Event implements Comparable {
  
  private PointInTime pit;
  private String desc;
  private Category cat;
  private long julianDay;
  
  /** constructor */
  public Event(Category setCat, PointInTime setTime, String setText) throws GedcomException {
    pit = setTime;
    cat = setCat;
    desc = cat.format(setText);
    // make sure its julian day is good
    julianDay = pit.getJulianDay();
  }
  
  /** the julian day */
  protected long getJulian() {
    return julianDay;
  }
  
  /** to String */
  public String toString() {
    WordBuffer result = new WordBuffer();
    result.append(pit.toString());
    result.append(desc);
    return result.toString();
  }
  
  /** comparison */
  public int compareTo(Object o) {
    Event that = (Event)o;
    return this.pit.compareTo(that.pit);
  }
  
  /**
   * Accessor
   */
  public PointInTime getTime() {
    return pit;
  }
  
  /**
   * Accessor
   */
  public Category getCategory() {
    return cat;
  }
  
  /**
   * Accessor
   */
  public String getDescription() {
    return desc;
  }
  
} //Event