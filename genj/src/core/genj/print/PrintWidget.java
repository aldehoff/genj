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

import genj.app.App;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * A PrintDialog */
public class PrintWidget extends JTabbedPane {
  
  /** task */
  private PrintManager.PrintTask task;
  
  /** resources */
  private Resources resources;
  
  /**
   * Constructor   */
  public PrintWidget(PrintManager.PrintTask tAsk, Resources reSources) {
    
    // remember 
    task = tAsk;
    resources = reSources;

    // reset current stuff shown
    reset();
  }
  
  /**
   * Resets the visual representation - brutal but
   * easiest for everything that might have changed
   * after a printer setup dialog ended
   */    
  private void reset() {
    
    // start from scratch
    removeAll();
    
    // new stuff
    add(resources.getString("dlg.tab.settings"), new SettingsPanel());
    add(resources.getString("dlg.tab.preview" ), new JScrollPane(new Preview()));
    
    // done    
  }
  
  /**
   * Main Panel
   */
  private class SettingsPanel extends JPanel {

    /**
     * Constructor     */
    private SettingsPanel() {

      // get some helpers      
      ButtonHelper bh = new ButtonHelper();
      GridBagHelper gh = new GridBagHelper(this).setInsets(new Insets(4,4,4,4));

      // Choose printer
      gh.add(new JLabel(resources.getString("dlg.label.printer")), 0, 0);
      gh.add(bh.create(new PrinterSetup())                       , 1, 0);
      gh.add(new JLabel(task.getPrintService())                  , 2, 0);

      gh.add(new JLabel(resources.getString("dlg.label.page"))   , 0, 1);
      gh.add(bh.create(new PageSetup())                          , 1, 1);
      
      gh.addFiller(99,99);
      
      // done
    }

  } //MainPanel
  
  /**
   * Action - Show Printer Setup
   */
  private class PrinterSetup extends ActionDelegate {
    /**
     * Constructor
     */
    private PrinterSetup() {
      super.setText(resources.getString("dlg.label.setup"));
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      task.showPrinterDialog();
      reset();
    }
  } //PrinterSetup
  
  /**
   * Action - Show Page Setup
   */
  private class PageSetup extends ActionDelegate {
    /**
     * Constructor
     */
    private PageSetup() {
      super.setText(resources.getString("dlg.label.setup"));
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      task.showPageDialog();
      reset();
    }
  } //PrintDlg
  
  /**
   * The preview
   */
  private class Preview extends JComponent {
    
    private double 
      pad  = 1.0D,
      zoom = 0.1D;
    
    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      // calculate
      Rectangle2D page = calcPage(0,0);
      Point pages = task.getPages(); 
      Point dpi = App.getInstance().getDPI();
      double 
        width = (pages.x*(page.getWidth ()+pad) + pad)*dpi.x*zoom,
        height= (pages.y*(page.getHeight()+pad) + pad)*dpi.y*zoom;
      // done
      return new Dimension((int)width, (int)height);
    }

    /**
     * Calculate page in inches
     */
    private Rectangle2D calcPage(int x, int y) {
      Point dpi = task.getResolution();
      Dimension psize = task.getPageSize();
      double 
       w = (double)psize.width /dpi.x,
       h = (double)psize.height/dpi.y;
      return new Rectangle2D.Double(
        pad + x*(w+pad),
        pad + y*(h+pad), 
        w, 
        h
      );
    }
    
    /**
     * Calculate imageable in inches
     */
    private Rectangle2D calcImageable(Rectangle2D page) {
      Point dpi = task.getResolution();
      Rectangle imageable = task.getImageable();
      return new Rectangle2D.Double(
        page.getMinX() + (imageable.getX()/dpi.x), 
        page.getMinY() + (imageable.getY()/dpi.y), 
        imageable.getWidth ()/dpi.x, 
        imageable.getHeight()/dpi.y
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
      Point pages = task.getPages(); 
      Point dpi = App.getInstance().getDPI();
      dpi.x *= zoom;
      dpi.y *= zoom;
      UnitGraphics ug = new UnitGraphics(g, dpi.x, dpi.y);
      for (int y=0;y<pages.y;y++) {
        for (int x=0;x<pages.x;x++) {
          // calculate layout
          Rectangle2D 
            page = calcPage(x,y), 
            imageable = calcImageable(page); 
          // draw page
          ug.setColor(Color.white);
          ug.draw(page, 0, 0, true);
          // draw imageable
          ug.setColor(Color.lightGray);
          ug.draw(imageable, 0, 0, false);
          // draw number
          ug.setColor(Color.gray);
          ug.draw(String.valueOf(x+y*pages.x+1),page.getCenterX(),page.getCenterY(),0.5D,0.5D);
          // draw content
          // FIXME 
//          ug.pushTransformation();
//          ug.pushClip(page);
//          ug.translate(page.getMinX() - (x*isize.getX()), page.getMinY() - (y*isize.getY()));
//          renderer.renderPage(ug.getGraphics(), new Point(x,y), dpi, true);
//          ug.popTransformation();
//          ug.popClip();
          // next   
        }
      }
      // done
    }

  } //Preview

  
} //PrintWidget
