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

import genj.almanac.Almanac;
import genj.gedcom.MetaProperty;
import genj.gedcom.PropertyEvent;
import genj.gedcom.TagPath;
import genj.util.Resources;
import genj.util.swing.ColorsWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.ListSelectionWidget;
import genj.util.swing.SpinnerWidget;
import genj.view.Settings;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
  
  /** resources we use */
  private Resources resources = Resources.get(this);
  
  /** keeping track of timeline these settings are for */
  private TimelineView view;
  
  /** a widget for selecting paths to show */
  private ListSelectionWidget pathsList = new ListSelectionWidget() {
    protected ImageIcon getIcon(Object choice) {
      TagPath path = (TagPath)choice;
      return MetaProperty.get(path).getImage();
    }
  };
  
  /** a widget for selecting almanac event libraries / categories */
  private ListSelectionWidget 
    almanacEvents = new ListSelectionWidget(),
    almanacCategories = new ListSelectionWidget();
  
  /** Checkbox for options */
  private JCheckBox[] checkOptions = {
    new JCheckBox(resources.getString("info.show.tags" )),
    new JCheckBox(resources.getString("info.show.dates")),
    new JCheckBox(resources.getString("info.show.grid" ))
  };
  
  /** models for spinners */
  private SpinnerWidget.FractionModel
    modCmBefEvent = new SpinnerWidget.FractionModel(TimelineView.MIN_CM_BEF_EVENT, TimelineView.MAX_CM_BEF_EVENT, 1),
    modCmAftEvent = new SpinnerWidget.FractionModel(TimelineView.MIN_CM_AFT_EVENT, TimelineView.MAX_CM_AFT_EVENT, 1);
     
  /** colorchooser for colors */
  private ColorsWidget colorWidget;
    
  /**
   * @see genj.view.Settings#init(genj.view.ViewManager)
   */
  public void init(ViewManager manager) {
    
    // panel for checkbox options    
    Box panelOptions = new Box(BoxLayout.Y_AXIS);
    for (int i=0; i<checkOptions.length; i++) {
      checkOptions[i].setAlignmentX(0F);
      panelOptions.add(checkOptions[i]);
    }
    
    SpinnerWidget spinCmBefEvent = new SpinnerWidget(resources.getString("info.befevent"), 5, modCmBefEvent);
    spinCmBefEvent.setToolTipText(resources.getString("info.befevent.tip"));
    panelOptions.add(spinCmBefEvent);
    
    SpinnerWidget spinCmAftEvent = new SpinnerWidget( resources.getString("info.aftevent"), 5, modCmAftEvent );
    spinCmAftEvent.setToolTipText(resources.getString("info.aftevent.tip"));
    panelOptions.add(spinCmAftEvent);
    
    JLabel labelEvents = new JLabel(resources.getString("info.events"));
    
    // panel for main options
    JPanel panelMain = new JPanel(new BorderLayout());
    panelMain.add(labelEvents , BorderLayout.NORTH);
    panelMain.add(pathsList   , BorderLayout.CENTER);
    panelMain.add(panelOptions, BorderLayout.SOUTH);
    
    // panel for history options
    JPanel panelEvents = new JPanel(new BorderLayout());
    panelEvents.add(almanacCategories, BorderLayout.NORTH);
    panelEvents.add(almanacEvents, BorderLayout.CENTER);
    
    // color chooser
    colorWidget = new ColorsWidget();
    
    // add those tabs
    add(resources.getString("page.main")  , panelMain);
    add(resources.getString("page.colors"), colorWidget);
    add(resources.getString("page.almanac"), panelEvents);

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
    view.setCMPerEvents(modCmBefEvent.getDoubleValue(), modCmAftEvent.getDoubleValue());
    
    // colors
    Iterator colors = view.colors.keySet().iterator();
    while (colors.hasNext()) {
      String key = colors.next().toString();
      view.colors.put(key, colorWidget.getColor(key));
    }
    
    // Done
  }
  
  /**
   * @see genj.view.Settings#setView(javax.swing.JComponent, genj.view.ViewManager)
   */
  public void setView(JComponent viEw) {
    // remember
    view = (TimelineView)viEw;
  }


  /**
   * Tells the ViewInfo to reset made changes
   */
  public void reset() {
    
    // EventTags to choose from
    pathsList.setChoices(PropertyEvent.getTagPaths());
    pathsList.setSelection(view.getModel().getPaths());
    
    // Checks
    checkOptions[0].setSelected(view.isPaintTags());
    checkOptions[1].setSelected(view.isPaintDates());
    checkOptions[2].setSelected(view.isPaintGrid());

    // sliders
    modCmBefEvent.setDoubleValue(view.getCmBeforeEvents());
    modCmAftEvent.setDoubleValue(view.getCmAfterEvents());
    
    // colors
    colorWidget.removeAllColors();
    Iterator keys = view.colors.keySet().iterator();
    while (keys.hasNext()) {
      String key = keys.next().toString();
      String name = resources.getString("color."+key);
      Color color = (Color)view.colors.get(key);
      colorWidget.addColor(key, name, color);
    }
    
    // almanac
    Almanac almanac = Almanac.getInstance();
    List libs = almanac.getLibraries();
    almanacEvents.setChoices(libs);
    almanacEvents.setSelection(new HashSet());
    List cats = almanac.getCategories();
    almanacCategories.setChoices(cats);
    almanacCategories.setSelection(new HashSet());
    
    
    // Done
  }
  
  /**
   * @see genj.view.Settings#getEditor()
   */
  public JComponent getEditor() {
    return this;
  }


} //TimelineViewSettings
