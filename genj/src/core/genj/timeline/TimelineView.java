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

import genj.gedcom.Gedcom;
import genj.util.ColorSet;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.DoubleValueSlider;
import genj.util.swing.ViewPortAdapter;
import genj.view.ToolBarSupport;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



/**
 * Component for showing entities' events in a timeline view
 */
public class TimelineView extends JPanel implements ToolBarSupport {

  /** resources */
  /*package*/ final static Resources resources = new Resources("genj.timeline");
  
  /** keeping track of our colors */
  /*package*/ ColorSet csContent, csRuler;
    
  /** our model */
  private Model model;
  
  /** our content */
  private Content content;
  
  /** our ruler */
  private Ruler ruler;

  /** our slider for cm per year */  
  private DoubleValueSlider sliderCmPerYear;
  
  /** our scrollpane */
  private JScrollPane scrollContent;

  /** the renderer we use for the ruler */
  private RulerRenderer rulerRenderer = new RulerRenderer();
  
  /** the renderer we use for the content */
  private ContentRenderer contentRenderer = new ContentRenderer();
  
  /** min/max's */
  /*package*/ final static double 
    MIN_CM_PER_YEAR =  0.1D,
    DEF_CM_PER_YEAR =  1.0D,
    MAX_CM_PER_YEAR = 10.0D,
    
    MIN_CM_BEF_EVENT = 0.1D,
    DEF_CM_BEF_EVENT = 0.5D,
    MAX_CM_BEF_EVENT = 2.0D,

    MIN_CM_AFT_EVENT = 2.0D,
    DEF_CM_AFT_EVENT = 2.0D,
    MAX_CM_AFT_EVENT = 9.0D;
    
  /** centimeters per year/event */
  /*package*/ double 
    cmPerYear = DEF_CM_PER_YEAR,
    cmBefEvent = DEF_CM_BEF_EVENT,
    cmAftEvent = DEF_CM_AFT_EVENT;
    
  /** the centered year */
  private double centeredYear = 0;
  
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
    cmPerYear = Math.max(MIN_CM_PER_YEAR, Math.min(MAX_CM_PER_YEAR, regstry.get("cmperyear", (float)DEF_CM_PER_YEAR)));
    cmBefEvent = Math.max(MIN_CM_BEF_EVENT, Math.min(MAX_CM_BEF_EVENT, regstry.get("cmbefevent", (float)DEF_CM_BEF_EVENT)));
    cmAftEvent = Math.max(MIN_CM_AFT_EVENT, Math.min(MAX_CM_AFT_EVENT, regstry.get("cmaftevent", (float)DEF_CM_AFT_EVENT)));
    isPaintDates = regstry.get("paintdates", true);
    isPaintGrid  = regstry.get("paintgrid" , false);
    isPaintTags  = regstry.get("painttags" , false);

    csContent = new ColorSet("content", Color.white, resources, regstry);
    csContent.add("text"    , Color.black);
    csContent.add("tag"     , Color.green);
    csContent.add("date"    , Color.gray );
    csContent.add("timespan", Color.blue );
    csContent.add("grid"    , Color.lightGray);
   
    csRuler = new ColorSet("ruler", Color.blue, resources, regstry);
    csRuler.add("text", Color.black);
    csRuler.add("tick", Color.white);
    
    // create/keep our sub-parts
    model = new Model(gedcom, (Set)regstry.get("filter", model.DEFAULT_FILTER));
    model.setTimePerEvent(cmBefEvent/cmPerYear, cmAftEvent/cmPerYear);
    content = new Content();
    content.addMouseListener(new ContentClick());
    ruler = new Ruler();
    
    // start listeing
    model.addListener(modelListener);
    
    // all that fits in a scrollpane
    scrollContent = new JScrollPane(new ViewPortAdapter(content));
    scrollContent.setColumnHeaderView(new ViewPortAdapter(ruler));
    scrollContent.getHorizontalScrollBar().addAdjustmentListener(new ChangeCenteredYear());
   
    // layout
    setLayout(new BorderLayout());
    add(scrollContent, BorderLayout.CENTER);
    
    // scroll to last centered year
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        centeredYear = regstry.get("centeryear", 0F);
        scroll2year(centeredYear);
      }
    });

    // done
  }

  
  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // stop listeing
    model.removeListener(modelListener);
    // store stuff in registry
    regstry.put("cmperyear"  , (float)Math.rint(cmPerYear*10)/10);
    regstry.put("cmbefevent" , (float)cmBefEvent);
    regstry.put("cmaftevent" , (float)cmAftEvent);
    regstry.put("paintdates" , isPaintDates);
    regstry.put("paintgrid"  , isPaintGrid);
    regstry.put("painttags"  , isPaintTags);
    regstry.put("filter"     , model.getFilter());
    regstry.put("centeryear" , (float)centeredYear);
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
    sliderCmPerYear = new DoubleValueSlider(MIN_CM_PER_YEAR, MAX_CM_PER_YEAR, cmPerYear, true);
    sliderCmPerYear.setText(cm2txt(cmPerYear, "view.cm"));
    sliderCmPerYear.setToolTipText(resources.getString("view.peryear.tip"));
    sliderCmPerYear.addChangeListener(new ChangeCmPerYear());
    bar.add(sliderCmPerYear);
    
    // create '/year' label
    JLabel labelPerYear = new JLabel(resources.getString("view.peryear"));
    labelPerYear.setFont(labelPerYear.getFont().deriveFont(9.0F));
    sliderCmPerYear.setFont(labelPerYear.getFont());
    bar.add(labelPerYear);
    
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
   * Sets the time allocated per event
   */
  public void setCMPerEvents(double before, double after) {
    // remember
    cmBefEvent = before;
    cmAftEvent = after;
    // update model
    model.setTimePerEvent(cmBefEvent/cmPerYear, cmAftEvent/cmPerYear);
  }
  
  /** 
   * Calculates a year from given pixel position
   */
  protected double pixel2year(int x) {
    return model.min + contentRenderer.pixels2cm(x)/cmPerYear;
  }

  /** 
   * Scrolls so that given year is centered in view
   */
  protected void scroll2year(double year) {
    int x = contentRenderer.cm2pixels( (year - model.min)*cmPerYear ) - scrollContent.getViewport().getWidth()/2;
    scrollContent.getHorizontalScrollBar().setValue(x);
  }
  
  /**
   * Convert cmPyear into text
   */
  protected final String cm2txt(double cm, String txt) {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(1);
    return resources.getString(txt, nf.format(cm));
  }
  
  /**
   * The ruler 'at the top'
   */
  private class Ruler extends JComponent {
    
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
      // let the renderer do its work
      rulerRenderer.cBackground = csRuler.getColor("ruler");
      rulerRenderer.cText = csRuler.getColor("text");
      rulerRenderer.cTick = csRuler.getColor("tick");
      rulerRenderer.cmPyear = cmPerYear;
      rulerRenderer.render(g, model);
      // done
    }
  
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      rulerRenderer.cmPyear = cmPerYear;
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
      contentRenderer.cmPyear = cmPerYear;
      return contentRenderer.getDimension(model, getFontMetrics(getFont()));
    }
  
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
      // let the renderer do its work
      contentRenderer.cBackground = csContent.getColor("content" );
      contentRenderer.cText       = csContent.getColor("text"    );
      contentRenderer.cDate       = csContent.getColor("date"    );
      contentRenderer.cTag        = csContent.getColor("tag"     );
      contentRenderer.cTimespan   = csContent.getColor("timespan");
      contentRenderer.cGrid       = csContent.getColor("grid"    );
      contentRenderer.cmPyear = cmPerYear;
      contentRenderer.paintDates = isPaintDates;
      contentRenderer.paintGrid = isPaintGrid;
      contentRenderer.paintTags = isPaintTags;
      contentRenderer.render(g, model);
      // done
    }
    
  } //Content
  
  /**
   * Listening to changes on the scrollpane
   */
  private class ChangeCenteredYear implements AdjustmentListener {
    /** @see java.awt.event.AdjustmentListener#adjustmentValueChanged(AdjustmentEvent) */
    public void adjustmentValueChanged(AdjustmentEvent e) {
      // swing's scrollbar doesn't distinguish between user-input
      // scrolling and propagated changes in its model (e.g. because of resize)\
      // we only update the centeredYear if getValueIsAdjusting()==true
      if (scrollContent.getHorizontalScrollBar().getValueIsAdjusting()) {
        // easy : translation and remember
        int x = scrollContent.getHorizontalScrollBar().getValue() + scrollContent.getViewport().getWidth()/2;
        centeredYear = pixel2year(x);
      } else {
        // no adjusting means we scroll back to 'our' remembered center
        // that means scrolling with the bar's buttons will not work!
        scroll2year(centeredYear);
      }
    }
  } //ChangeScroll 
  
  /**
   * Listening to changes on cm per year (slider)
   */
  private class ChangeCmPerYear implements ChangeListener {
    /** @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent) */
    public void stateChanged(ChangeEvent e) {
      // get the new value
      cmPerYear = sliderCmPerYear.getValue();
      // update label&tip
      String s = cm2txt(cmPerYear, "view.cm");
      sliderCmPerYear.setText(s);
      // update model
      model.setTimePerEvent(cmBefEvent/cmPerYear, cmAftEvent/cmPerYear);
      // done
    }
  } //ChangeCmPerYear
    
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
  
  /**
   * ContentClick
   */
  private class ContentClick extends MouseAdapter {
    /** @see java.awt.event.MouseAdapter#mousePressed(MouseEvent) */
    public void mousePressed(MouseEvent e) {
      // find the year
      double year = pixel2year(e.getX());
      System.out.println(year);
    }
  } //ContentClick  
    
} //TimelineView
