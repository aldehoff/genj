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

import genj.app.TagPathList;
import genj.gedcom.PropertyEvent;
import genj.util.ColorSet;
import genj.util.swing.ColorChooser;
import genj.util.swing.DoubleValueSlider;
import genj.view.Settings;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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
public class TimelineViewSettings extends JTabbedPane implements Settings {
  
  /** keeping track of timeline these settings are for */
  private TimelineView view;
  
  /** a widget for selecting paths to show */
  private TagPathList pathsList = new TagPathList();
  
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
  public TimelineViewSettings() {
    
    // panel for checkbox options    
    Box panelOptions = new Box(BoxLayout.Y_AXIS);
    for (int i=0; i<checkOptions.length; i++) {
      checkOptions[i].setAlignmentX(0F);
      panelOptions.add(checkOptions[i]);
    }
    sliderCmBefEvent = new DoubleValueSlider(TimelineView.MIN_CM_BEF_EVENT, TimelineView.MAX_CM_BEF_EVENT, 0, false);
    sliderCmBefEvent.setAlignmentX(0F);
    sliderCmBefEvent.setToolTipText(TimelineView.resources.getString("info.befevent.tip"));
    sliderCmBefEvent.setText(TimelineView.resources.getString("info.befevent"));
    panelOptions.add(sliderCmBefEvent);
    
    sliderCmAftEvent = new DoubleValueSlider(TimelineView.MIN_CM_AFT_EVENT, TimelineView.MAX_CM_AFT_EVENT, 0, false);
    sliderCmAftEvent.setAlignmentX(0F);
    sliderCmAftEvent.setToolTipText(TimelineView.resources.getString("info.aftevent.tip"));
    sliderCmAftEvent.setText(TimelineView.resources.getString("info.aftevent"));
    panelOptions.add(sliderCmAftEvent);
    
    // panel for main options
    JPanel panelMain = new JPanel(new BorderLayout());
    panelMain.add(pathsList, BorderLayout.CENTER);
    panelMain.add(panelOptions, BorderLayout.SOUTH);
    
    // color chooser
    colorChooser = new ColorChooser();
    
    // add those tabs
    add("Main"  , panelMain);
    add("Colors", colorChooser);

    // done
  }

  /**
   * Tells the ViewInfo to apply made changes
   */
  public void apply() {
    
    // choosen EventTags
    view.getModel().setPaths(pathsList.getSelection());
    
    // checks
    view.setPaintTags(checkOptions[0].isSelected());
    view.setPaintDates(checkOptions[1].isSelected());
    view.setPaintGrid(checkOptions[2].isSelected());
    
    // sliders
    view.setCMPerEvents(sliderCmBefEvent.getValue(), sliderCmAftEvent.getValue());
    
    // colors
    colorChooser.apply();
    
    // Done
  }
  
  /**
   * @see genj.view.Settings#setView(javax.swing.JComponent)
   */
  public void setView(JComponent viEw) {
    // remember
    view = (TimelineView)viEw;
    // characteristics
    colorChooser.setColorSets( new ColorSet[]{
      view.csContent,
      view.csRuler  
    });
  }


  /**
   * Tells the ViewInfo to reset made changes
   */
  public void reset() {
    
    // EventTags to choose from
    pathsList.setPaths(PropertyEvent.getTagPaths());
    pathsList.setSelection(view.getModel().getPaths());
    
    // Checks
    checkOptions[0].setSelected(view.isPaintTags());
    checkOptions[1].setSelected(view.isPaintDates());
    checkOptions[2].setSelected(view.isPaintGrid());

    // sliders
    sliderCmBefEvent.setValue(view.getCmBeforeEvents());
    sliderCmAftEvent.setValue(view.getCmAfterEvents());
    
    // colors
    colorChooser.reset();
    
    // Done
  }
  
  /**
   * @see genj.view.Settings#getEditor()
   */
  public JComponent getEditor() {
    return this;
  }


} //TimelineViewSettings
