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
package genj.print;

import genj.util.ActionDelegate;
import genj.util.Dimension2d;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.UnitGraphics;
import genj.view.Options;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.print.PrintService;
import javax.print.ServiceUI;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A PrintDialog */
public class PrintWidget extends JPanel {
  
  /** task */
  private PrintTask task;
  
  /** resources */
  private Resources resources;

  /** services to choose from */
  private ChoiceWidget services;

  /** a preview */
  private Preview preview;

  /**
   * Constructor   */
  public PrintWidget(PrintTask tAsk, Resources reSources) {
    
    // remember 
    task = tAsk;
    resources = reSources;

    // setup layout
    NestedBlockLayout layout = new NestedBlockLayout(false, 2);
    setLayout(layout);
    
    // 'printer'
    add(new JLabel(resources.getString("printer")));
    
    // choose service
    services = new ChoiceWidget(task.getServices(), task.getService());
    services.setEditable(false);
    services.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        // only selection is interesting
        if (e.getStateChange()!=ItemEvent.SELECTED) 
          // change service
          task.setService((PrintService)services.getSelectedItem());
      }
    });
    add(services, new Point2D.Double(1,0));

    // settings
    add(new ButtonHelper().create(new Settings()));
    
    // next line
    layout.createBlock(0);
    
    // 'preview'
    add(new JLabel(resources.getString("preview")));
    
    // next line
    layout.createBlock(0);
    
    // the actual preview
    preview = new Preview();
    add(new JScrollPane(preview), new Point2D.Double(1,1));
    
    // done    
  }
  
  /**
   * The preview
   */
  private class Preview extends JComponent {
    
    private float 
      padd = 0.1F, // inch
      zoom = 0.25F; // 25%

    private Point dpiScreen = Options.getInstance().getDPI();
    
    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      // calculate
      Dimension pages = task.getPages(); 
      Rectangle2D page = task.getPage(pages.width-1,pages.height-1, padd);
      return new Dimension(
        (int)((page.getMaxX())*dpiScreen.x*zoom),
        (int)((page.getMaxY())*dpiScreen.y*zoom)
      );
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      
      // fill background
      g.setColor(Color.gray);
      g.fillRect(0,0,getWidth(),getHeight());
      g.setColor(Color.white);
      
      // render pages in app's dpi space
      Printer renderer = task.getRenderer();
      Dimension pages = task.getPages(); 
      UnitGraphics ug = new UnitGraphics(g, dpiScreen.x*zoom, dpiScreen.y*zoom);
      for (int y=0;y<pages.height;y++) {
        for (int x=0;x<pages.width;x++) {
          // calculate layout
          Rectangle2D 
            page = task.getPage(x,y, padd), 
            imageable = task.getPrintable(page); 
          // draw page
          ug.setColor(Color.white);
          ug.draw(page, 0, 0, true);
          // draw imageable
          ug.setColor(Color.lightGray);
          ug.draw(imageable, 0, 0, false);
          // draw number
          ug.setColor(Color.gray);
          ug.draw(String.valueOf(x+y*pages.width+1),page.getCenterX(),page.getCenterY(),0.5D,0.5D);
          // draw content
          ug.pushTransformation();
          ug.pushClip(imageable);
          ug.translate(imageable.getMinX(), imageable.getMinY());
          ug.getGraphics().scale(zoom,zoom);
          renderer.renderPage(ug.getGraphics(), new Point(x,y), new Dimension2d(imageable), dpiScreen, true);
          ug.popTransformation();
          ug.popClip();
          // next   
        }
      }
      // done
    }

  } //Preview

  /**
   * Action : printer settings
   */
  private class Settings extends ActionDelegate {

    /** constructor */
    private Settings() {
      super.setText(resources.getString("settings"));
    }

    /** run */
    protected void execute() {
      // show settings
      Point pos = task.getOwner().getLocationOnScreen();
      PrintService choice = ServiceUI.printDialog(null, pos.x, pos.y, task.getServices(), task.getService(), null, task.getAttributes());
      if (choice!=null) {
        services.setSelectedItem(choice);
        task.setService(choice);
      }

      // update preview
      preview.revalidate();
      preview.repaint();
      
    }
    
  } //Settings
  
} //PrintWidget
