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
  private ColorChooser colorChooser;

  /**
   * Constructor   */
  public TreeViewSettings(TreeView view) {
    
    // remember
    tree = view;
    
    // panel for checkbox options    
    Box panelOptions = new Box(BoxLayout.Y_AXIS);
    
    sliderCmIndiWidth = new DoubleValueSlider(0.1, 10.0, 1.0, false);
    sliderCmIndiWidth.setPreferredSliderWidth(64);
    sliderCmIndiWidth.setAlignmentX(0F);
    sliderCmIndiWidth.setToolTipText(tree.resources.getString("info.indiwidth.tip"));
    sliderCmIndiWidth.setText(tree.resources.getString("info.indiwidth"));
    panelOptions.add(sliderCmIndiWidth);

    sliderCmIndiHeight = new DoubleValueSlider(0.1, 10.0, 1.0, false);
    sliderCmIndiHeight.setPreferredSliderWidth(64);
    sliderCmIndiHeight.setAlignmentX(0F);
    sliderCmIndiHeight.setToolTipText(tree.resources.getString("info.indiheight.tip"));
    sliderCmIndiHeight.setText(tree.resources.getString("info.indiheight"));
    panelOptions.add(sliderCmIndiHeight);
    
    sliderCmFamWidth = new DoubleValueSlider(0.1, 10.0, 1.0, false);
    sliderCmFamWidth.setPreferredSliderWidth(64);
    sliderCmFamWidth.setAlignmentX(0F);
    sliderCmFamWidth.setToolTipText(tree.resources.getString("info.famwidth.tip"));
    sliderCmFamWidth.setText(tree.resources.getString("info.famwidth"));
    panelOptions.add(sliderCmFamWidth);

    sliderCmFamHeight = new DoubleValueSlider(0.1, 10.0, 1.0, false);
    sliderCmFamHeight.setPreferredSliderWidth(64);
    sliderCmFamHeight.setAlignmentX(0F);
    sliderCmFamHeight.setToolTipText(tree.resources.getString("info.famheight.tip"));
    sliderCmFamHeight.setText(tree.resources.getString("info.famheight"));
    panelOptions.add(sliderCmFamHeight);
    
    sliderCmPadding = new DoubleValueSlider(0.1, 10.0, 1.0, false);
    sliderCmPadding.setPreferredSliderWidth(64);
    sliderCmPadding.setAlignmentX(0F);
    sliderCmPadding.setToolTipText(tree.resources.getString("info.padding.tip"));
    sliderCmPadding.setText(tree.resources.getString("info.padding"));
    panelOptions.add(sliderCmPadding);
    
    // color chooser
    colorChooser = new ColorChooser();
    colorChooser.addSet(tree.colors);
    
    // add those tabs
    add(tree.resources.getString("page.main")  , panelOptions);
    add(tree.resources.getString("page.colors"), colorChooser);
    
    // reset
    reset();
    
    // done
  }

  /**
   * @see genj.view.ApplyResetSupport#apply()
   */
  public void apply() {
    // colors
    colorChooser.apply();
    // make sure that shows
    tree.repaint();
    // done
  }

  /**
   * @see genj.view.ApplyResetSupport#reset()
   */
  public void reset() {
    // colors
    colorChooser.reset();
    // colors
    colorChooser.reset();
    // done
  }

} //TreeViewSettings
