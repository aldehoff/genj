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
package genj.tree;

import genj.util.swing.ColorChooser;
import genj.util.swing.DoubleValueSlider;
import genj.view.ApplyResetSupport;

import java.awt.Container;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;

/**
 * The settings component for the Tree View */
public class TreeViewSettings extends JTabbedPane implements ApplyResetSupport {

  /** keeping track of tree these settings are for */
  private TreeView tree;

  /** sliders for box size */
  private DoubleValueSlider 
    sliderCmIndiWidth, 
    sliderCmIndiHeight,
    sliderCmFamWidth,
    sliderCmFamHeight,
    sliderCmPadding;
  
  /** colorchooser for colors */
  private ColorChooser colors;

  /**
   * Constructor   */
  public TreeViewSettings(TreeView view) {
    
    // remember
    tree = view;
    
    // panel for checkbox options    
    TreeMetrics m = view.model.getMetrics();
    Box options = new Box(BoxLayout.Y_AXIS);
    
    sliderCmIndiWidth = createSlider(options, 1.0, 16.0, m.wIndis, "indiwidth" );
    sliderCmIndiHeight= createSlider(options, 1.0, 16.0, m.hIndis, "indiheight");
    sliderCmFamWidth  = createSlider(options, 1.0, 16.0, m.wFams , "famwidth"  );
    sliderCmFamHeight = createSlider(options, 1.0, 16.0, m.hFams , "famheight" );
    sliderCmPadding   = createSlider(options, 0.1,  4.0, m.pad   , "padding"   );
    
    // color chooser
    colors = new ColorChooser();
    colors.addSet(tree.colors);
    
    // add those tabs
    add(tree.resources.getString("page.main")  , options);
    add(tree.resources.getString("page.colors"), colors);
    
    // reset
    reset();
    
    // done
  }
  
  /**
   * Create a slider
   */
  private DoubleValueSlider createSlider(Container c, double min, double max, double val, String key) {
    // create and preset
    DoubleValueSlider result = new DoubleValueSlider(min, max, val, false);
    result.setPreferredSliderWidth(128);
    result.setAlignmentX(0F);
    result.setText(tree.resources.getString("info."+key));
    result.setToolTipText(tree.resources.getString("info."+key+".tip"));
    c.add(result);
  
    // done
    return result;   }
  
  /**
   * @see genj.view.ApplyResetSupport#apply()
   */
  public void apply() {
    // colors
    colors.apply();
    // metrics
    tree.model.setMetrics(new TreeMetrics(
      sliderCmIndiWidth .getValue(),
      sliderCmIndiHeight.getValue(),
      sliderCmFamWidth  .getValue(),
      sliderCmFamHeight .getValue(),
      sliderCmPadding   .getValue()
    ));
    // make sure that shows
    tree.repaint();
    // done
  }

  /**
   * @see genj.view.ApplyResetSupport#reset()
   */
  public void reset() {
    // colors
    colors.reset();
    // metrics
    TreeMetrics m = tree.model.getMetrics();
    sliderCmIndiWidth .setValue(m.wIndis);
    sliderCmIndiHeight.setValue(m.hIndis);
    sliderCmFamWidth  .setValue(m.wFams );
    sliderCmFamHeight .setValue(m.hFams );
    sliderCmPadding   .setValue(m.pad   );
    // done
  }

} //TreeViewSettings
