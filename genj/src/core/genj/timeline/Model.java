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
package genj.timeline;

import java.util.List;

import genj.gedcom.Gedcom;

/**
 * A model that wraps the Gedcom information in a timeline fashion
 */
/*package*/ class Model {

  /** the gedcom we're looking at */
  private Gedcom gedcom;
  
  /** limits */
  private int
    maxYear = 2020,
    minYear = 1901;
  
  /**
   * Constructor
   */
  /*package*/ Model(Gedcom gedcom) {
    
    // remember
    this.gedcom = gedcom;
    
    // done
  }
  
  /**
   * Gather Events
   */
  private void gatherEvents() {
    gatherEvents(gedcom.getEntities(Gedcom.INDIVIDUALS));
    gatherEvents(gedcom.getEntities(Gedcom.FAMILIES   ));
  }
  
  /** 
   * Gather Events for given entities
   */
  private void gatherEvents(List es) {
  }
  
  /**
   * An event in our model
   */
  private class Event {
  } //Event
  
} //TimelineModel 
