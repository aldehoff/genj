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
import java.util.Set;

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
import genj.util.Resources;
import genj.util.swing.*;
import genj.view.ToolBarSupport;

/**
 * Component for showing entities' events in a timeline view
 */
public class TimelineView extends JPanel implements ToolBarSupport {
  
  /** resources */
  /*package*/ final static Resources resources = new Resources("genj.timeline");
  
  /** our model */
  private Model model;
  
  /** our content */
  private Content content;
  
  /** our ruler */
  private Ruler ruler;

  /** our sliders */  
  private JSlider sliderCmPerYear, sliderCmPerEvent;
  
  /** our labels */
  private JLabel labelCmPerYear, labelCmPerEvent;
  
  /** our scrollpane */
  private JScrollPane scrollContent;

  /** the renderer we use for the ruler */
  private RulerRenderer rulerRenderer = new RulerRenderer();
  
  /** the renderer we use for the content */
  private ContentRenderer contentRenderer = new ContentRenderer();
  
  /** min/max's */
  private final static double 
    MIN_CMPERYEAR =  0.1D,
    DEF_CMPERYEAR =  1.0D,
    MAX_CMPERYEAR = 10.0D,
    MIN_CMPEREVENT = 1.0D,
    DEF_CMPEREVENT = 3.0D,
    MAX_CMPEREVENT =10.0D;
    
  /** centimeters per year/event */
  private double 
    cmPyear = DEF_CMPERYEAR,
    cmPevent = DEF_CMPEREVENT;
  
  /** settings */
  private boolean 
    isPaintDates = true,
    isPaintGrid = false,
    isPaintTags = true;

  /** registry we keep */
  private Registry regstry;
  
  /** listener we are to the model */
  private Model.Listener modelListener = new ModelListener();
    
  /**
   * Constructor
   */
  public TimelineView(Gedcom gedcom, Registry registry, Frame frame) {

    // read some stuff from registry
    regstry = registry;
    cmPyear = Math.max(MIN_CMPERYEAR, Math.min(MAX_CMPERYEAR, regstry.get("cmpyear", (float)DEF_CMPERYEAR)));
    cmPevent = Math.max(MIN_CMPEREVENT, Math.min(MAX_CMPEREVENT, regstry.get("cmpevent", (float)DEF_CMPEREVENT)));
    isPaintDates = regstry.get("paintdates", true);
    isPaintGrid  = regstry.get("paintgrid" , false);
    isPaintTags  = regstry.get("painttags" , false);
    
    // create/keep our sub-parts
    model = new Model(gedcom, (Set)regstry.get("filter", model.DEFAULT_FILTER), cmPevent/cmPyear);
    content = new Content();
    ruler = new Ruler();
    
    // start listeing
    model.addListener(modelListener);
    
    // all that fits in a scrollpane
    scrollContent = new JScrollPane(new ViewPortAdapter(content));
    scrollContent.setColumnHeaderView(new ViewPortAdapter(ruler));
   
    // layout
    setLayout(new BorderLayout());
    add(scrollContent, BorderLayout.CENTER);
    
    // done
  }

  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // stop listeing
    model.removeListener(modelListener);
    // store stuff in registry
    regstry.put("cmpyear" , (float)cmPyear);
    regstry.put("cmpevent", (float)cmPevent);
    regstry.put("paintdates", isPaintDates);
    regstry.put("paintgrid" , isPaintGrid);
    regstry.put("painttags" , isPaintTags);
    regstry.put("filter"    , model.getFilter());
    // done
    super.removeNotify();
  }
  
  /**
   * Accessor - the model
   */
  public Model getModel() {
    return model;
  }
  
  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {
    
    // create a slider for cmPerYear
    sliderCmPerYear = new JSlider(i(MIN_CMPERYEAR),i(MAX_CMPERYEAR),i(cmPyear));
    sliderCmPerYear.setAlignmentX(0F);
    sliderCmPerYear.setMaximumSize(sliderCmPerYear.getPreferredSize());
    sliderCmPerYear.addChangeListener((ChangeListener)new ActionCmPerYear().as(ChangeListener.class));
    bar.add(sliderCmPerYear);
    
    // create '0.5cm' label
    labelCmPerYear = new JLabel(cm2txt(cmPyear));
    labelCmPerYear.setFont(labelCmPerYear.getFont().deriveFont(9.0F));
    bar.add(labelCmPerYear);

    // create '/year' label
    JLabel labelPerYear = new JLabel("/year");
    labelPerYear.setFont(labelCmPerYear.getFont());
    bar.add(labelPerYear);

    // create a slider for pixelsPerEvent
    sliderCmPerEvent = new JSlider(i(MIN_CMPEREVENT),i(MAX_CMPEREVENT),i(cmPevent));
    sliderCmPerEvent.setAlignmentX(0F);
    sliderCmPerEvent.setMaximumSize(sliderCmPerEvent.getPreferredSize());
    sliderCmPerEvent.addChangeListener((ChangeListener)new ActionCmPerEvent().as(ChangeListener.class));
    bar.add(sliderCmPerEvent);

    // create 'max' label
    JLabel labelMax = new JLabel("max.");
    labelMax.setFont(labelCmPerYear.getFont());
    bar.add(labelMax);

    // create '1.5cm' label
    labelCmPerEvent = new JLabel(cm2txt(cmPevent));
    labelCmPerEvent.setFont(labelCmPerYear.getFont());
    bar.add(labelCmPerEvent);

    // create '/event' label
    JLabel labelPerEvent = new JLabel("/event");
    labelPerEvent.setFont(labelCmPerYear.getFont());
    bar.add(labelPerEvent);

    // done
  }
  
  /**
   * Accessor - paint tags
   */
  public boolean isPaintTags() {
    return isPaintTags;
  }

  /**
   * Accessor - paint tags
   */
  public void setPaintTags(boolean set) {
    isPaintTags = set;
    repaint();
  }

  /**
   * Accessor - paint dates
   */
  public boolean isPaintDates() {
    return isPaintDates;
  }

  /**
   * Accessor - paint dates
   */
  public void setPaintDates(boolean set) {
    isPaintDates = set;
    repaint();
  }

  /**
   * Accessor - paint grid
   */
  public boolean isPaintGrid() {
    return isPaintGrid;
  }

  /**
   * Accessor - paint grid
   */
  public void setPaintGrid(boolean set) {
    isPaintGrid = set;
    repaint();
  }
  
  /**
   * Converts double to int
   */
  private final int i(double d) {
    return (int)(d*10);
  }
  
  /**
   * Converts int to double
   */
  private final double d(int i) {
    return ((double)i)/10;
  }
  
  /**
   * Convert cmPyear into text
   */
  private final String cm2txt(double cm) {
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
      contentRenderer.paintDates = isPaintDates;
      contentRenderer.paintGrid = isPaintGrid;
      contentRenderer.paintTags = isPaintTags;
      contentRenderer.render(g, model);
      // done
    }
  
  } //Content

  /**
   * Action - cm per year
   */
  private class ActionCmPerYear extends ActionDelegate {
    /** @see genj.util.ActionDelegate#execute() */
    protected void execute() {
      // get the new value
      cmPyear = d(sliderCmPerYear.getValue());
      // update label
      labelCmPerYear.setText(cm2txt(cmPyear));
      // update model
      model.setTimePerEvent(cmPevent/cmPyear);
      // done
    }
  } //ActionScale
    
  /**
   * Action - cm per event
   */
  private class ActionCmPerEvent extends ActionDelegate {
    /** @see genj.util.ActionDelegate#execute() */
    protected void execute() {
      // get the new value
      cmPevent = d(sliderCmPerEvent.getValue());
      // update label
      labelCmPerEvent.setText(cm2txt(cmPevent));
      // update model
      model.setTimePerEvent(cmPevent/cmPyear);
      // done
    }
  } //ActionCmPerEvent
  
  /**
   * We're also listening to the model
   */
  private class ModelListener implements Model.Listener {
    /**
     * @see genj.timeline.Model.Listener#dataChanged()
     */
    public void dataChanged() {
      repaint();
    }
    /**
     * @see genj.timeline.Model.Listener#structureChanged()
     */
    public void structureChanged() {
      ruler.revalidate();
      content.revalidate();
      repaint();
    }
  } // ModelListener
    
} //TimelineView
