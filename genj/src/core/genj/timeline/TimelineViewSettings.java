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
import genj.view.ApplyResetSupport;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

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

  /**
   * Constructor
   */
  public TimelineViewSettings(TimelineView timelineView) {
    
    // remember
    timeline = timelineView;
    
    // panel for main options
    JPanel panelMain = new JPanel(new BorderLayout());
    
    Box panelOptions = new Box(BoxLayout.Y_AXIS);
    for (int i=0; i<checkOptions.length; i++) {
      panelOptions.add(checkOptions[i]);
    }
    
    panelMain.add(selectorEventTags, BorderLayout.CENTER);
    panelMain.add(panelOptions, BorderLayout.SOUTH);
    
    // panel for colors
    JPanel panelColors = new JPanel(new BorderLayout());
    
    JColorChooser colorChooser = new JColorChooser();
    colorChooser.setPreviewPanel(new JPanel());
    panelColors.add(colorChooser, BorderLayout.CENTER);

    // add those tabs
    add("Main"  , panelMain);
    add("Colors", panelColors);

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
    
    // Done
  }

}
