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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import genj.gedcom.*;
import genj.util.*;

/**
 * A model for timline information
 */
public class TimelineModel implements GedcomListener {

  /** limits */
  private int
    maxYear      = 1998,
    minYear      = 1945,
    minDist      = 5;

  /** the underlying Gedcom */
  private Gedcom gedcom;

  /** members */
  private Vector listeners = new Vector();
  private Vector vLevels;
  private Link actual;
  private Hashtable eventTags;

  /**
   * Interface for iterating through links
   */
  public interface Action {
    // LCD
    public final static int CONTINUE=0, BREAK_LEVEL=1, BREAK=2;

    /**
     * Does action on given link
     * @param on link to do action on
     * @param level level of link
     * @return one of the conditions CONTINUE, BREAK or BREAK_LEVEL
     */
    public int doAction(Link on, boolean newLevel);
  }

  /**
   * Constructor
   */
  public TimelineModel(Gedcom gedcom) {

    // Remember data
    this.gedcom   = gedcom;
    this.eventTags= new Hashtable();

    // Calculate links
    calculateLinks();

    // Done
  }

  /**
   * Add listener
   */
  public void addTimelineModelListener(TimelineModelListener listener) {

    // Attach to gedcom ?
    if (listeners.size()==0) {
      gedcom.addListener(this);
      calculateLinks();
    }

    // Remember
    listeners.addElement(listener);
  }

  /**
   * Calculates link for event
   */
  private void calculateLinkFor(PropertyEvent pevent) {

    // Generate link
    Link link = Link.generateLink(pevent);

    // Not possible (in case of invalid event data) ?
    if (link==null) {
      return;
    }

    // Check min/max Year
    minYear = (int)Math.floor(Math.min(link.fromYear,minYear));
    maxYear = (int)Math.ceil (Math.max(link.toYear  ,maxYear));

    // Find right place in levels
    Enumeration eLevels = vLevels.elements();
    Enumeration eLevel;
    Vector vLevel;
    Link first,last;

    while (eLevels.hasMoreElements()) {

      // Get Enumeration for Level and first link in level
      vLevel=(Vector)eLevels.nextElement();
      eLevel=vLevel.elements();
      first =(Link)eLevel.nextElement();
      last  =first;

      // Before first in level ?
      if (first.fromYear-minDist > link.toYear) {
      vLevel.insertElementAt(link,0);
      return;
      }

      // Between two of level ?
      int pos=1;
      while (eLevel.hasMoreElements()) {

        last = (Link)eLevel.nextElement();

        // .. max < link < min
        if ((first.toYear+minDist < link.fromYear) && (link.toYear < last.fromYear-minDist) ) {
          vLevel.insertElementAt(link,pos);
          return;
        }
        // .. try next
        first = last;
        pos++;
      }

      // Behind last in level ?
      if (last.toYear+minDist < link.fromYear) {
        vLevel.addElement(link);
        return;
      }
      // Try in next level
    }

    // New Level !
    vLevel = new Vector();
    vLevel.addElement(link);
    vLevels.addElement(vLevel);

    // Done
  }

  /**
   * Calculates links
   */
  private void calculateLinks() {

    // Setup links to events
    vLevels = new Vector();

    // Starting point is now
    minYear=1998;
    maxYear=1998;

    // Gather events of entities
    calculateLinksFor(gedcom.getEntities(Gedcom.INDIVIDUALS));

    // Gather events of fams
    calculateLinksFor(gedcom.getEntities(Gedcom.FAMILIES   ));

    // Insert spare years
    minYear-=minDist;
    maxYear+=minDist;

    // Done
  }

  /**
   * Calculates links for given entities
   */
  private void calculateLinksFor(EntityList entities) {

    for (int e=0;e<entities.getSize();e++) {

      // .. get Entity
      Property property = entities.get(e).getProperty();

      // .. go through its properties
      for (int p=0;p<property.getNoOfProperties();p++) {

        // .. An event ?
        Property child = property.getProperty(p);
        if ( (child instanceof PropertyEvent) && eventTags.contains(child.getTag()))
          calculateLinkFor((PropertyEvent)child);

        // .. Next proeprty
      }

      // .. next entity
    }

    // Done
  }

  /**
   * Iterates on links with given Iterator
   * @param iterator Iterator to use
   * @return last link or null
   */
  public Link doForAll(Action action) {

    // Go through links
    Link link=null,next=null;
    Enumeration eLevels = vLevels.elements();
    loopLevels:
    for (int level=1;eLevels.hasMoreElements();level++) {

      // .. level to paint
      Vector vLevel = (Vector)eLevels.nextElement();

      // .. doAction on links
      boolean newLevel = true;

    loopLevel:
      for (int l=vLevel.size()-1;l>=0;l--) {
      link = (Link)vLevel.elementAt(l);

      switch (action.doAction(link, newLevel)) {
      case Action.BREAK_LEVEL :
        break loopLevel;
      case Action.BREAK :
        break loopLevels;
      }

      newLevel=false;
      }

      // Next level
    }

    // Done
    return link;
  }

  /**
   * Fire data changed event
   */
  private void fireDataChanged() {
    Enumeration ls = listeners.elements();
    while (ls.hasMoreElements()) {
      ((TimelineModelListener)ls.nextElement()).handleDataChanged();
    }
  }

  /**
   * Fire layout changed event
   */
  private void fireLayoutChanged() {
    Enumeration ls = listeners.elements();
    while (ls.hasMoreElements()) {
      ((TimelineModelListener)ls.nextElement()).handleLayoutChanged();
    }
  }

  /**
   * Return number of levels that are used
   */
  public int getLevels() {
    return vLevels.size();
  }

  /**
   * Return maximum year
   */
  public int getMaxYear() {
    return maxYear;
  }

  /**
   * Return minimum year
   */
  public int getMinYear() {
    return minYear;
  }

  /**
   * Return years that are covered
   */
  public int getYears() {
    return maxYear-minYear;
  }

  /**
   * Gedcom has been changed
   */
  public void handleChange(Change change) {

    // EADD EDEL PADD PDEL PMOD
    if (change.isChanged(Change.PADD) || change.isChanged(Change.PDEL) ) {
      calculateLinks();
      fireLayoutChanged();
      return;
    }

    // PMOD
    if (change.isChanged(Change.PMOD)) {
      Enumeration props = change.getProperties(Change.PMOD).elements();
      while (props.hasMoreElements()) {
        Property property = ((Property)props.nextElement()).getParent();
        if (property==null) {
          continue;
        }
        if (eventTags.contains(property.getTag())) {
          calculateLinks();
          fireLayoutChanged();
          return;
        }
      }
      fireDataChanged();
    }

    // Done
  }

  /**
   * Gedcom is being closed
   */
  public void handleClose(Gedcom which) {
    // I won't do anything
  }

  /**
   * Entity in Gedcom has been selected
   */
  public void handleSelection(Selection selection) {
    // I won't do anything
  }

  /**
   * Remove listener
   */
  public void removeTimelineModelListener(TimelineModelListener listener) {

    // Forget
    listeners.removeElement(listener);

    // Disconnect from gedcom ?
    if (listeners.size()==0) {
      gedcom.removeListener(this);
    }
  }

  /**
   * Sets the Tags to consider for Events
   */
  public void setEventTags(String[] tags) {

    eventTags = new Hashtable();
    for (int i=0;i<tags.length;i++) {
      eventTags.put(tags[i],tags[i]);
    }
    calculateLinks();
  }          
}
