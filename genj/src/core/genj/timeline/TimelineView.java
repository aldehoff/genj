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
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;

import genj.gedcom.Gedcom;
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
  
  /** centimeters per year */
  private double cmPyear = 0.5D;
  
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
    JSlider slider = new JSlider(JSlider.VERTICAL,0,100,50);
    //slider.setPreferredSize(new Dimension(0,0));
    slider.setPaintTicks(false);
    slider.setPaintLabels(true);
    slider.setPaintTrack(true);
    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(1);
    
    bar.add(slider);
  }

  /**
   * The ruler 'at the top'
   */
  private class Ruler extends JComponent {
    
    /** the renderer we use */
    private RulerRenderer renderer = new RulerRenderer();
    
    /**
     * Constructor
     */
    protected Ruler() {
      renderer.dpi = getToolkit().getScreenResolution();
      renderer.cmPyear = cmPyear;
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
      renderer.render(model, g);
    }
  
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      return renderer.getDimension(model, getFontMetrics(getFont()));
    }
    
  } //Ruler

  /**
   * The content for displaying the timeline model
   */
  private class Content extends JComponent {
    
    /** the renderer we use */
    private ContentRenderer renderer = new ContentRenderer();
    
    /**
     * Content
     */
    protected Content() {
      renderer.dpi = getToolkit().getScreenResolution();
      renderer.cmPyear = cmPyear;
    }
    
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      return renderer.getDimension(model, getFontMetrics(getFont()));
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
      renderer.render(model, g);
    }
  
  } //Content
  
} //TimelineView
