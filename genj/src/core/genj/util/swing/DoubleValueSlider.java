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
package genj.util.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A special slider
 */
public class DoubleValueSlider extends JPanel {
  
  /** whether we're exponential or not */
  private boolean isExponential = false;
  
  /** the value range */
  private double min, max;
  
  /** the slider we user */
  private JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
  
  /** the text we show */
  private JLabel label = new JLabel();
  
  /** the layouts we use */
  private BoxLayout[] layouts = {
    new BoxLayout(this, BoxLayout.X_AXIS),
    new BoxLayout(this, BoxLayout.Y_AXIS)
  };
  
  /** the listeners */
  private List listeners = new ArrayList();
  
  /** the text */
  private String text = null;

  /** utils we use */
  private NumberFormat nf = NumberFormat.getInstance();
  private MessageFormat mf = new MessageFormat("");
  
  /**
   * Constructor
   */
  public DoubleValueSlider(double min, double max, double val, boolean exponential) {
    // set fraction digits on number format
    nf.setMaximumFractionDigits(1);
    // add sub-components
    setLayout(layouts[0]);
    setOpaque(false);
    slider.setOpaque(false);
    slider.setAlignmentX(0F);
    label .setAlignmentX(0F);
    add(slider);
    add(label);
    // remember
    this.min = min;
    this.max = max;
    isExponential = exponential;
    setValue(val);
    // glue to slider events
    slider.addChangeListener(new JSliderGlue());
    // done
  }

  /**
   * Sets the value
   */
  public void setValue(double d) {
    int i;
    if (!isExponential) i = (int)(100 * (d-min)/(max-min));
    else i = (int)( 100 - Math.exp( (max - d) / (max-min) * Math.log(100) ) );
    slider.setValue(i);
  }
  
  /**
   * Gets the value
   */
  public double getValue() {
    int i = slider.getValue();
    if (!isExponential) return min + ((double)i)/100 * (max-min);
    return max - Math.log(100-i+1)/Math.log(100) * (max-min);
  }
  
  
  /**
   * Set the text to show
   */
  public void setText(String txt) {
    if (txt.indexOf("{0}")<0) {
      label.setText(txt);
    } else {
      text = txt;
      updateText();
    }
  }
  
  /** 
   * sets the preferred width of the slider   */
  public void setPreferredSliderWidth(int width) {
    slider.setPreferredSize(new Dimension(width, slider.getPreferredSize().height));
  }
  
  /**
   * Updates the showing text   */
  private void updateText() {
    // not if not text available
    if (text==null) return;
    // do it
    mf.applyPattern(text);
    label.setText(mf.format(new Object[]{ nf.format(getValue()) }));
    // done
  }
  
  /**
   * @see javax.swing.JComponent#setFont(Font)
   */
  public void setFont(Font font) {
    // delegate
    super.setFont(font);
    if (slider!=null) slider.setFont(font);
    if (label !=null) label.setFont(font);
    // done
  }
  
  /**
   * @see javax.swing.JComponent#setToolTipText(String)
   */
  public void setToolTipText(String text) {
    slider.setToolTipText(text);
  }

  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    // check if we're in a toolbar
    if (getParent() instanceof JToolBar) {
      int orientation = ((JToolBar)getParent()).getOrientation();
      setLayout(layouts[orientation]);
      slider.setOrientation(orientation);
    }
    slider.setMaximumSize(slider.getPreferredSize());
    // delegate
    super.addNotify();
  }
  
  /**
   * Adds a change listener
   */
  public void addChangeListener(ChangeListener l) {
    listeners.add(l);
  }

  /**
   * Removes a change listener
   */
  public void removeChangeListener(ChangeListener l) {
    listeners.remove(l);
  }
  
  /**
   * JSliderGlue
   */
  private class JSliderGlue implements ChangeListener { 
    /**
     * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
      updateText();
      e = new ChangeEvent(DoubleValueSlider.this);
      for (int l=0; l<listeners.size(); l++) {
        ((ChangeListener)listeners.get(l)).stateChanged(e);
      }
    }
  } //JSliderGlue

} //DoubleValueSlider
