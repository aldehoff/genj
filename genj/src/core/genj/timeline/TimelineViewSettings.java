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

import genj.app.TagSelector;
import genj.gedcom.PropertyEvent;
import genj.util.ActionDelegate;
import genj.util.swing.ColorChooser;
import genj.util.swing.DoubleValueSlider;
import genj.view.ApplyResetSupport;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

/**
 * The ViewInfo representing settings of a TimelineView
 * +remember the event last visible in the middle
 * +colors
 *  ruler 
 *    background
 *    foreground
 *  content 
 *  background
 *  tag color
 *  txt color
 *  date color
 *  line color
 */
public class TimelineViewSettings extends JTabbedPane implements ApplyResetSupport {
  
  /** keeping track of timeline these settings are for */
  private TimelineView timeline;
  
  /** a widget for selecting tags to show */
  private TagSelector selectorEventTags = new TagSelector();
  
  /** Checkbox for options */
  private JCheckBox[] checkOptions = {
    new JCheckBox(TimelineView.resources.getString("info.show.tags" )),
    new JCheckBox(TimelineView.resources.getString("info.show.dates")),
    new JCheckBox(TimelineView.resources.getString("info.show.grid" ))
  };
  
  /** sliders for event size */
  private DoubleValueSlider sliderCmBefEvent, sliderCmAftEvent;
  
  /** colorchooser for colors */
  private ColorChooser colorChooser;
    
  /**
   * Constructor
   */
  public TimelineViewSettings(TimelineView timelineView) {
    
    // remember
    timeline = timelineView;

    // panel for checkbox options    
    Box panelOptions = new Box(BoxLayout.Y_AXIS);
    for (int i=0; i<checkOptions.length; i++) {
      checkOptions[i].setAlignmentX(0F);
      panelOptions.add(checkOptions[i]);
    }
    sliderCmBefEvent = new DoubleValueSlider(timeline.MIN_CM_BEF_EVENT, timeline.MAX_CM_BEF_EVENT, timeline.cmBefEvent, false);
    sliderCmBefEvent.setAlignmentX(0F);
    sliderCmBefEvent.setToolTipText(timeline.resources.getString("info.befevent.tip"));
    sliderCmBefEvent.setText(timeline.resources.getString("info.befevent"));
    panelOptions.add(sliderCmBefEvent);
    
    sliderCmAftEvent = new DoubleValueSlider(timeline.MIN_CM_AFT_EVENT, timeline.MAX_CM_AFT_EVENT, timeline.cmAftEvent, false);
    sliderCmAftEvent.setAlignmentX(0F);
    sliderCmAftEvent.setToolTipText(timeline.resources.getString("info.aftevent.tip"));
    sliderCmAftEvent.setText(timeline.resources.getString("info.aftevent"));
    panelOptions.add(sliderCmAftEvent);
    
    // panel for main options
    JPanel panelMain = new JPanel(new BorderLayout());
    panelMain.add(selectorEventTags, BorderLayout.CENTER);
    panelMain.add(panelOptions, BorderLayout.SOUTH);
    
    // color chooser
    colorChooser = new ColorChooser();
    colorChooser.addSet(timeline.csContent);
    colorChooser.addSet(timeline.csRuler  );
    
    // add those tabs
    add("Main"  , panelMain);
    add("Colors", colorChooser);

    // init
    reset();
    
    // done
  }

  /**
   * Tells the ViewInfo to apply made changes
   */
  public void apply() {
    
    // choosen EventTags
    timeline.getModel().setFilter(selectorEventTags.getSelection());
    
    // checks
    timeline.setPaintTags(checkOptions[0].isSelected());
    timeline.setPaintDates(checkOptions[1].isSelected());
    timeline.setPaintGrid(checkOptions[2].isSelected());
    timeline.setCMPerEvents(sliderCmBefEvent.getValue(), sliderCmAftEvent.getValue());
    
    // colors
    colorChooser.apply();
    
    // Done
  }

  /**
   * Tells the ViewInfo to reset made changes
   */
  public void reset() {
    // EventTags to choose from
    selectorEventTags.setTags(PropertyEvent.getTags());
    selectorEventTags.setSelection(timeline.getModel().getFilter());

    // Checks
    checkOptions[0].setSelected(timeline.isPaintTags());
    checkOptions[1].setSelected(timeline.isPaintDates());
    checkOptions[2].setSelected(timeline.isPaintGrid());
    
    // colors
    colorChooser.reset();
    
    // Done
  }

} //TimelineViewSettings
