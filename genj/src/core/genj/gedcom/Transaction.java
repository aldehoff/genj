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

import java.util.HashSet;
import java.util.Set;

/**
 * A transaction on our Gedcom model
 */
public class Transaction {

  public static final int
    EADD    = 0,
    EDEL    = 1,
    EMOD    = 2,
    PADD    = 3,
    PDEL    = 4,
    PMOD    = 5,
    NUM     = 6;

  /** a  set that doesn't retain added content */  
  private static final Set NULL_SET = new HashSet() {
    // ignore any add's
    public boolean add(Object o) { return false; }
  };
  
  /** gedcom */
  private Gedcom gedcom;

  /** current changes */
  private Set[] changes;
  
  /** whether we track changes */
  private boolean isTrackChanges = true;
  
  /** time started */
  private long time;
  
  /**
   * Constructor
   */
  /*package*/ Transaction(Gedcom ged) {

    // remember    
    gedcom = ged;
    
    time = System.currentTimeMillis();

    // prepare tracking changes
    changes = new Set[NUM];
    for (int i=0;i<NUM;i++) {
      changes[i] = new HashSet(64);
    }
  }
  
  /**
   * accessor - time started
   */
  public long getTime() {
    return time;
  }
  
  /**
   * accessor - whether to track changes
   */
  public boolean isTrackChanges() {
    return isTrackChanges;
  }
  
  /**
   * accessor - whether to track changes
   */
  public void setTrackChanges(boolean set) {
    isTrackChanges = set;
  }
  
  /**
   * Returns Set
   */
  public Set getChanges(int which) {
    return isTrackChanges ? changes[which] : NULL_SET;
  }

  /**
   * Changed Gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * whether something was actually changed
   */
  public boolean hasChanges() {
    for (int i=0;i<NUM;i++)
      if (!changes[i].isEmpty()) 
        return true;
    return false;
  }

} //Transaction
