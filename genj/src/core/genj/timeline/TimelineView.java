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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeListener;

import genj.gedcom.Gedcom;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.swing.*;
import genj.view.ToolBarSupport;

/**
 * Component for showing entities' events in a timeline view
 */
public class TimelineView extends JPanel implements ToolBarSupport {
  
  /** our model */
  private Model model;
  
  /** our content */
  private Content content;
  
  /** our ruler */
  private Ruler ruler;

  /** our slider for changing the scale */  
  private JSlider sliderCmPerYear;
  
  /** a label for current scale */
  private JLabel labelCmPerYear;

  /** the renderer we use for the ruler */
  private RulerRenderer rulerRenderer = new RulerRenderer();
  
  /** the renderer we use for the content */
  private ContentRenderer contentRenderer = new ContentRenderer();
  
  /** min/max centimeters per year */
  private final static double 
    MIN_CMPERYEAR =  0.1D,
    DEF_CMPERYEAR =  1.0D,
    MAX_CMPERYEAR = 10.0D;
    
  /** centimeters per year */
  private double cmPyear = DEF_CMPERYEAR;
  
  /**
   * Constructor
   */
  public TimelineView(Gedcom gedcom, Registry registry, Frame frame) {
    
    // create our sub-parts
    model = new Model(gedcom);
    content = new Content();
    ruler = new Ruler();
    
    // all that fits in a scrollpane
    JScrollPane scroll = new JScrollPane(new ViewPortAdapter(content));
    scroll.setColumnHeaderView(new ViewPortAdapter(ruler));
    
    // layout
    setLayout(new BorderLayout());
    add(scroll, BorderLayout.CENTER);
    
    // done
  }
  
  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {
    
    // create a slider
    sliderCmPerYear = new JSlider(i(MIN_CMPERYEAR),i(MAX_CMPERYEAR),i(cmPyear));
    sliderCmPerYear.setAlignmentX(0F);
    sliderCmPerYear.setMaximumSize(sliderCmPerYear.getPreferredSize());
    sliderCmPerYear.addChangeListener((ChangeListener)new ActionScale().as(ChangeListener.class));
    bar.add(sliderCmPerYear);
    
    // create maximum label
    labelCmPerYear = new JLabel(MAX_CMPERYEAR+"cm");
    labelCmPerYear.setFont(labelCmPerYear.getFont().deriveFont(9.0F));
    labelCmPerYear.setPreferredSize(labelCmPerYear.getPreferredSize());
    labelCmPerYear.setText(cm2txt(cmPyear));
    bar.add(labelCmPerYear);
    
    // create '/year' label
    JLabel labelPerYear = new JLabel("/year");
    labelPerYear.setFont(labelCmPerYear.getFont());
    bar.add(labelPerYear);
    
    // done
  }
  
  /**
   * Converts double to int
   */
  private int i(double d) {
    return (int)(d*10);
  }
  
  /**
   * Converts int to double
   */
  private double d(int i) {
    return ((double)i)/10;
  }
  
  /**
   * Convert cmPyear into text
   */
  private String cm2txt(double cm) {
    return NumberFormat.getInstance().format(cm)+"cm";
  }
    
  /**
   * The ruler 'at the top'
   */
  private class Ruler extends JComponent {
    
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
      // fill the background
      Rectangle r = getBounds();
      g.setColor(Color.white);
      g.fillRect(0,0,r.width,r.height);
      // let the renderer do its work
      rulerRenderer.cmPyear = cmPyear;
      rulerRenderer.render(g, model);
      // done
    }
  
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      rulerRenderer.cmPyear = cmPyear;
      return rulerRenderer.getDimension(model, getFontMetrics(getFont()));
    }
    
  } //Ruler

  /**
   * The content for displaying the timeline model
   */
  private class Content extends JComponent {
    
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      contentRenderer.cmPyear = cmPyear;
      return contentRenderer.getDimension(model, getFontMetrics(getFont()));
    }
  
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
      // fill the background
      Rectangle r = getBounds();
      g.setColor(Color.white);
      g.fillRect(0,0,r.width,r.height);
      // let the renderer do its work
      contentRenderer.cmPyear = cmPyear;
      contentRenderer.render(g, model);
      // done
    }
  
  } //Content

  /**
   * Action - scale changes
   */
  private class ActionScale extends ActionDelegate {
    /** @see genj.util.ActionDelegate#execute() */
    protected void execute() {
      // get the new value
      cmPyear = d(sliderCmPerYear.getValue());
      // update label
      labelCmPerYear.setText(cm2txt(cmPyear));
      // revalidate views
      ruler.revalidate();
      content.revalidate();
      // done
    }
  } //ActionScale
    
} //TimelineView
