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

import awtx.*;
import genj.gedcom.*;
import genj.option.*;
import genj.util.*;

/**
 * Component for showing entities' events in a timeline view
 */
public class TimelineView extends Scrollpane implements TimelineModelListener, ActionListener {

  /** events we look for */
  private final static String[] defaultEventTags = {
    "BIRT" ,"DEAT", "MARR"
  };

  /** pixels per year limits */
  private final static int
    MIN_PPY =  2,
    MAX_PPY = 64;

  /** members */
  private Gedcom gedcom;
  private Registry registry;
  private TimelineModel model;
  private Scala scala;
  private boolean isPaintTags = true;
  private boolean isPaintDates = true;
  private boolean isPaintGrid = false;
  private String[] eventTags = {};

  /*package*/ final static Resources resources = new Resources("genj.timeline");

  /**
   * Constructor
   */
  public TimelineView(Gedcom gedcom, Registry registry, Frame frame) {

    // Prepare a model
    model = new TimelineModel(gedcom);

    // Prepare renderer
    setQuadrant(CENTER, new TimelineContent(model,this));
    setQuadrant(SOUTH , new TimelineScala  (model,this));

    setUseSpace(CENTER,CENTER);

    // Listen
    model.addTimelineModelListener(this);

    // Make additional scala for pixelsPerYear
    scala = new Scala();
    scala.setActionCommand("PPY");
    scala.addActionListener(this);
    add2Edge(scala);

    //  Remember
    this.gedcom  =gedcom;
    this.registry=registry;

    // Load parameters
    loadParameters();

    // Done
  }

  /**
   * ActionCommand handling
   */
  public void actionPerformed(ActionEvent ev) {
    // Read from scala for PixelsPerYear
    if (ev.getActionCommand().equals("PPY")) {
      setPercentagePerYear(scala.getValue());
    }
    // Done
  }

  /**
   * Returns an array of EventTags of this view
   */
  public String[] getEventTags() {
    return eventTags;
  }

  /**
   * Returns the percentage of space for one year
   */
  public float getPercentagePerYear() {
    return scala.getValue();
  }

  /**
   * Returns the number of pixels per Year
   */
  public int getPixelsPerYear() {
    return (int)(MIN_PPY+scala.getValue()*(MAX_PPY-MIN_PPY));
  }

  /**
   * Notification from model that some of the data has changed
   */
  public void handleDataChanged() {
    // .. and repaint
    repaint();
  }

  /**
   * Notification from model that layout of data has changed
   */
  public void handleLayoutChanged() {
    // Do the layout
    doLayout();

    // .. and repaint
    repaint();
  }

  /**
   * Returns wether painting of Dates occurs
   */
  public boolean isPaintDates() {
    return isPaintDates;
  }

  /**
   * Queries grid state
   */
  public boolean isPaintGrid() {
    return isPaintGrid;
  }

  /**
   * Returns wether painting of TAGs occurs
   */
  public boolean isPaintTags() {
    return isPaintTags;
  }

  /**
   * Loads parameters from registry
   */
  private void loadParameters() {
    setPercentagePerYear(registry.get("ppy"   , 0.5F));
    setPaintTags        (registry.get("ptags" , true));
    setPaintDates       (registry.get("pdates", true));
    setEventTags        (registry.get("tags"  , defaultEventTags));
    setPaintGrid        (registry.get("pgrid" , true));
  }

  /**
   * Notification in case component isn't used anymore
   */
  public void removeNotify() {

    // Save current state to Registry
    saveParameters();

    // Remove ourself from Model
    model.removeTimelineModelListener(this);

    // Continue
    super.removeNotify();
  }

  /**
   * Saves parameters to registry
   */
  private void saveParameters() {
    registry.put("ppy"    , getPercentagePerYear());
    registry.put("ptags"  , isPaintTags());
    registry.put("pdates" , isPaintDates());
    registry.put("tags"   , getEventTags());
    registry.put("pgrid"  , isPaintGrid());
  }

  /**
   * Sets the EventTags to identify Event with
   */
  public void setEventTags(String[] tags) {
    eventTags = tags;
    model.setEventTags(eventTags);
  }

  /**
   * En/disables painting of Dates
   */
  public void setPaintDates(boolean on) {
    isPaintDates=on;
    repaint();
  }

  /**
   * En/Disables grid painting
   */
  public void setPaintGrid(boolean on) {
    isPaintGrid=on;
  }

  /**
   * En/disables painting of TAGs
   */
  public void setPaintTags(boolean on) {
    isPaintTags=on;
    repaint();
  }

  /**
   * Sets the percentage of space for one year
   */
  public void setPercentagePerYear(float value) {
    scala.setValue(value);
    doLayout();
  }
}
