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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;

/**
 * A model that wraps the Gedcom information in a timeline fashion
 */
/*package*/ class Model {

  /** the gedcom we're looking at */
  private Gedcom gedcom;
  
  /** limits */
  private double 
    max = Double.NaN,
    min = Double.NaN;

  /** a filter for events that we're interested in */
  private Set filter;
  
  /** default filter */
  private final static String[] DEFAULT_FILTER = { "BIRT", "MARR", "RESI" };
    
  /** our levels */
  private List layers;
  
  /**
   * Constructor
   */
  /*package*/ Model(Gedcom gedcom) {
    
    // remember
    this.gedcom = gedcom;
    
    // setup default filter
    filter = new HashSet();
    for (int f=0; f<DEFAULT_FILTER.length; f++) {
      filter.add(DEFAULT_FILTER[f]);
    }
    
    // gather events for the 1st time
    insertEvents();
    
    // done
  }
  
  /**
   * Returns the maximum
   * @return double
   */
  /*package*/ double getMaximum() {
    return max;
  }

  /**
   * Returns the minimum
   * @return double
   */
  /*package*/ double getMinimum() {
    return min;
  }
  
  /**
   * Returns max-min
   */
  /*package*/ double getTimespan() {  
    return max-min;
  }
  
  /** 
   * Returns the layers (containing lists of events)
   */
  /*package*/ List getLayers() {
    return layers;
  }

  /**
   * Gather Events
   */
  private void insertEvents() {
    
    // reset
    min = Double.MAX_VALUE;
    max = -Double.MAX_VALUE;
    
    // prepare some space
    layers = new ArrayList(10);
    
    // look for events in INDIs and FAMs
    insertEventsFrom(gedcom.getEntities(Gedcom.INDIVIDUALS));
    insertEventsFrom(gedcom.getEntities(Gedcom.FAMILIES   ));
    
    // done
  }
  
  /** 
   * Gather Events for given entities
   * @param es list of entities to find events in
   */
  private void insertEventsFrom(List es) {
    // loop through entities
    for (int i=0; i<es.size(); i++) {
      Entity e = (Entity)es.get(i);
      List ps = e.getProperty().getProperties(PropertyEvent.class);
      for (int j=0; j<ps.size(); j++) {
        PropertyEvent pe = (PropertyEvent)ps.get(j);
        if (filter.contains(pe.getTag())) insertEventFrom(pe);
      }
    }
    // done
  }
  
  /** 
   * Gather Event for given PropertyEvent
   * @param pe property to use
   */
  private void insertEventFrom(PropertyEvent pe) {
    // we need a valid date for that event
    PropertyDate date = pe.getDate();
    if (date==null) return;
    // get start (has to be valid) and end
    PropertyDate.PointInTime 
      start = date.getStart(),
      end   = date.getEnd();
    if (!start.isValid()) return;
    if (!end  .isValid()) end = start;
    // create the Event
    Event e = new Event(pe, wrap(start), wrap(end));
    // remember min and max
    min = Math.min(Math.floor(e.from), min);
    max = Math.max(Math.ceil (e.to  ), max);
    // keep the event
    insertEvent(e);
    // done
  }
  
  /**
   * Insert the Event into one of our layers
   */
  private void insertEvent(Event e) {
    
    // find a level that suits us
    for (int l=0;l<layers.size();l++) {
      // try to insert in level
      List layer = (List)layers.get(l);
      if (insertEvent(e, layer)) return;
      // continue
    }
    
    // create a new layer
    List layer = new LinkedList();
    layers.add(layer);
    layer.add(e);
    
    // done
  }
  
  /**
   * Insert the Event into a layer
   * @return whether that was successfull
   */
  private boolean insertEvent(Event e, List layer) {
    return false;
  }
  
  /**
   * Helper transforming a point in time into a double value
   */
  private double wrap(PropertyDate.PointInTime p) {
    return (double)p.getYear(0) + ((double)p.getMonth(1)-1)/12 + ((double)p.getDay(0)/12/31);
  }
  
  /**
   * An event in our model
   */
  /*package*/ class Event {
    /** state */
    /*package*/ double from, to;
    /*package*/ PropertyEvent prop;
    /** 
     * Constructor
     */
    Event(PropertyEvent propEvent, double start, double end) {
      prop = propEvent;
      from  = start;
      to  = end;
    }
    /** 
     * String representation
     */
    public String toString() {
      return prop.getTag() + '@' + from + '>' + to;
    }
  } //Event
  
} //TimelineModel 
