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
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import genj.util.GridBagHelper;
import genj.gedcom.*;
import awtx.*;
import genj.app.*;

/**
 * The ViewInfo representing settings of a TimelineView
 */
public class TimelineViewSettings extends ViewSettingsWidget {

  /** members */
  private TimelineView timeline;
  private TagSelector  selectorEventTags;
  private Scala        scalaPercentPerYear;
  private JCheckBox[]  checkPaints = {
    new JCheckBox(TimelineView.resources.getString("info.show.tags" )),
    new JCheckBox(TimelineView.resources.getString("info.show.dates")),
    new JCheckBox(TimelineView.resources.getString("info.show.grid" ))
  };

  /**
   * Creates the visual parts of the editor
   */
  public TimelineViewSettings(TimelineView timeline) {
    
    // remember
    this.timeline = timeline;
    
    // Create components
    selectorEventTags = new TagSelector();
    scalaPercentPerYear = new Scala();

    // Layout
    GridBagHelper helper = new GridBagHelper(this);
    int row = 0;

    helper.add(new JLabel(TimelineView.resources.getString("info.events")),0,row++,2,1);
    helper.addFiller(0,row,new Dimension(16,16));
    helper.add(selectorEventTags,1,row++,1,1,helper.GROW_BOTH|helper.FILL_BOTH);

    helper.add(new JLabel(TimelineView.resources.getString("info.spaceperyear")),0,row++,2,1);
    helper.add(scalaPercentPerYear,1,row++,1,1,helper.FILL_HORIZONTAL);

    helper.add(new JLabel(TimelineView.resources.getString("info.display")),0,row++,2,1);
    for (int i=0;i<checkPaints.length;i++) {
      helper.add(checkPaints[i],1,row++,1,1);
    }
    
    // init
    reset();

    // Done
  }

  /**
   * Tells the ViewInfo to apply made changes
   */
  public void apply() {
    // EventTags to choose from
    if (selectorEventTags.isChanged()) {
      timeline.setEventTags(selectorEventTags.getSelectedTags());
    }

    // Space for one year
    timeline.setPercentagePerYear(scalaPercentPerYear.getValue());

    // Checks
    timeline.setPaintTags (checkPaints[0].isSelected());
    timeline.setPaintDates(checkPaints[1].isSelected());
    timeline.setPaintGrid (checkPaints[2].isSelected());

    // Done
  }

  /**
   * Tells the ViewInfo to reset made changes
   */
  public void reset() {
    // EventTags to choose from
    String tags[] = PropertyEvent.getTags();
    selectorEventTags.setTags(tags);
    selectorEventTags.selectTags(timeline.getEventTags());

    // Space for one year
    scalaPercentPerYear.setValue(timeline.getPercentagePerYear());

    // Checks
    checkPaints[0].setSelected(timeline.isPaintTags());
    checkPaints[1].setSelected(timeline.isPaintDates());
    checkPaints[2].setSelected(timeline.isPaintGrid());

    // Done
  }

}
