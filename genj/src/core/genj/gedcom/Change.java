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

import java.util.Set;


/**
 * Class that encapsulated changes after writelock
 */
public class Change {

  public static final int
    EADD    = 0,
    EDEL    = 1,
    EMOD    = 2,
    PADD    = 3,
    PDEL    = 4,
    PMOD    = 5,
    NUM      = 6;

  private Gedcom gedcom;

  private Set[] sets;

  /**
   * Constructor
   */
  public Change(Gedcom geDcom, Set[] seTs) {

    // Remember
    gedcom = geDcom;
    sets   = seTs;
    
  }

  /**
   * Returns Set
   */
  public Set getChanges(int which) {
    return sets[which];
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
  public boolean isEmpty() {
    for (int i=0;i<NUM;i++)
      if (!sets[i].isEmpty()) return false;
    return true;
  }

} //Change
