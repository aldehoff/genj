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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A transaction on our Gedcom model
 */
public class Transaction {

  public static final int
    ENTITIES_ADDED     = 0,
    ENTITIES_DELETED   = 1,
    ENTITIES_MODIFIED  = 2,
    PROPERTIES_ADDED   = 3,
    PROPERTIES_DELETED = 4,
    PROPERTIES_MODIFIED= 5,
    NUM                = 6;

  /** gedcom */
  private Gedcom gedcom;

  /** currently affected */
  private Set[] affected;
  
  /** time started */
  private long time;
  
  /** changes */
  private ArrayList changes = new ArrayList(20);
  
  /** whether we're an undo */
  private boolean isRollback = false; 

  /** prev and next transaction */
  /*package*/ Transaction prev, next;
  
  /** change flag before tx */
  /*package*/ boolean hasUnsavedChangesBefore;
  
  /**
   * Constructor
   */
  /*package*/ Transaction(Gedcom ged) {

    // remember    
    gedcom = ged;
    hasUnsavedChangesBefore = ged.hasUnsavedChanges();
    
    time = System.currentTimeMillis();

    // prepare tracking affected
    affected = new Set[NUM];
    for (int i=0;i<NUM;i++) {
      affected[i] = new HashSet(64);
    }
  }
  
  /**
   * Add a change
   */
  /*package*/ void addChange(Change change) {
    changes.add(change);
  }
  
  /*package*/ boolean isRollback() {
    return isRollback;
  }
  
  /*package*/ void setRollback(boolean set) {
    isRollback = set;
  }
  
  /**
   * accessor - time started
   */
  /*package*/ long getTime() {
    return time;
  }
  
  /**
   * Returns list of changes
   */
  public Change[] getChanges() {
    return (Change[])changes.toArray(new Change[changes.size()]);
  }
  
  /**
   * Returns Set
   */
  public Set get(int which) {
    return affected[which];
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
    return !changes.isEmpty();
  }

  /**
   * String representation
   */
  public String toString() {
    StringBuffer result = new StringBuffer(256);
    for (int i=0;i<changes.size();i++)
      result.append(changes.get(i)+"\n");
    return result.toString();
  }

} //Transaction
