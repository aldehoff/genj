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
import genj.util.Debug;
import genj.util.Registry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.ResolutionSyntax;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrinterResolution;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * A manager for printing */
public class PrintManager {
  
  // 1 printunit = 1/72 inch = 1/72 * 2.54 cm
  // => cm / 2.54 * 72 = printunit
  //private final static double CM2PRINTUNIT = 1/2.54 * 72;
  
  /** the default resolution in dots per cm */
  private final static Point2D.Double resolution = 
    new Point2D.Double(72/2.54, 72/2.54);

  /** singleton */
  private static PrintManager instance = null;
  
  /** registry */
  private static Registry registry = Registry.lookup("genj");
  
  /**
   * Constructor   */
  private PrintManager() {
  }
  
  /** 
   * Singleton access 
   */
  public static PrintManager getInstance() {
    if (instance==null) instance = new PrintManager();
    return instance;
  }
  
  /**
   * Show a print dialog
   */
  public boolean print(PrintRenderer renderer, JComponent owner) {
    // our own task for printing
    new PrintTask(renderer, owner);    
    // done
    return false;
  }

  /**
   * Our own task for printing   */  
  /*package*/ class PrintTask {
    
    /** our print job */
    private PrinterJob job;
    
    /** the print widget */
    private PrintWidget widget;
    
    /** the current page format */
    private PageFormat pageFormat;
    
    /** the current renderer */
    private PrintRenderer renderer;
    
    /**
     * Constructor     */
    private PrintTask(PrintRenderer reNderer, JComponent owner) {
      
      // remember renderer
      renderer = reNderer;
      
      // create a job
      job = PrinterJob.getPrinterJob();
      
      // restore service?
      setPrintService(registry.get("printer.service",""));
      
      // restore orientation
      setPortrait(registry.get("printer.portrait", true));

      // show dialog
      boolean cont = showDialog(owner);
            
      // cancel if not requested to continue
      if (!cont) {
        job.cancel();
        return;
      }
      
      // remember service
      registry.put("printer.service", getPrintService().getName());

      // remember orientation
      registry.put("printer.portrait", isPortrait());

      // print it
      print();      
      
      // done
    }
    
    /**
     * The available services     */
    /*package*/ PrintService[] getPrintServices() {
      return job.lookupPrintServices();
    }
    
    /**
     * Current service     */
    /*package*/ PrintService getPrintService() {
      return job.getPrintService();
    }
    
    /**
     * Current service     */
    /*package*/ boolean setPrintService(PrintService service) {
      try {
        job.setPrintService(service);
        return true;
      } catch (PrinterException e) {
        Debug.log(Debug.WARNING, this, "Couldn't change printer", e);
        return false;
      }
    }
    
    /**
     * Current service     */
    /*package*/ boolean setPrintService(String name) {
      // look for it
      PrintService[] services = getPrintServices();
      for (int i = 0; i < services.length; i++) {
      	if (services[i].getName().equals(name))
          return setPrintService(services[i]);
      }
      // not found
      return false;
    }
    
    /**
     * Current orientation     */
    /*package*/ void setPortrait(boolean set) {
      if (set)  
        getPageFormat().setOrientation(PageFormat.PORTRAIT);
      else
        getPageFormat().setOrientation(PageFormat.LANDSCAPE);
    }
    
    /**
     * Current orientation
     */
    /*package*/ boolean isPortrait() {
      return getPageFormat().getOrientation()==PageFormat.PORTRAIT;
    }
        
    /**
     * PageFormat     */
    /*package*/ PageFormat getPageFormat() {
      if (pageFormat==null) pageFormat = job.defaultPage();
      return pageFormat;
    }

    /**
     * the actual printing logic     */    
    private void print() {

      // prepare print request
      HashPrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
      if (getPageFormat().getOrientation()==PageFormat.LANDSCAPE)
        set.add(OrientationRequested.LANDSCAPE);
      else
        set.add(OrientationRequested.PORTRAIT);
      set.add(new PrinterResolution(300,300,ResolutionSyntax.DPI));
        
      // FIXME : hack to redirect into file
      set.add(new Destination(new File("d:/temp/tst.ps").toURI()));

      // glue to us as the printable     
      job.setPrintable(new PrintableImpl(getPageFormat(), renderer));
      
      // call
      try {
        job.print(set);
      } catch (Throwable t) {
        t.printStackTrace();
      }

      // done            
    }

    /**
     * Show a print dialog
     */
    private boolean showDialog(JComponent owner) {
            
      // create a print widget
      widget = new PrintWidget(this);
      
      // show it in dialog
      App.Dialog dlg = App.getInstance().createDialog(
        "Printing", 
        "print", 
        new Dimension(480,320), 
        owner, 
        widget,
        new String[]{ "Print", UIManager.getString("OptionPane.cancelButtonText")}
      );
      dlg.pack();
      dlg.show();
      
      // check choice
      return dlg.getChoice()==0;
    }
    
  } //PrintTask

  /**
   * PrintManager   */
  private static class PrintableImpl implements Printable {
    
    /** renderer we use */
    private PrintRenderer renderer;
    
    /** pages */
    private Point[] pageSequence;
    
    /**
     * Constructor     */
    /*package*/ PrintableImpl(PageFormat pageFormat, PrintRenderer rendErer) {

      // remember renderer
      renderer = rendErer;
      
      // calculate pages
      Point pages = renderer.getNumPages(
        new Point2D.Double(pageFormat.getImageableWidth(),pageFormat.getImageableHeight()),
        resolution
      );   

      // safety check
      if (pages.x==0||pages.y==0) 
        throw new IllegalArgumentException("Renderer returned zero pages");
      
      // setup pages
      pageSequence = new Point[pages.x*pages.y];
      int i = 0;
      for (int x=0; x<pages.x; x++) {
      	for (int y=0; y<pages.y; y++) {
          pageSequence[i++] = new Point(x,y);       	
        }
      }

      // ready      
    }
    
    /**
     * Callback for actual printing  - we know that the
     * Graphics object is scaled to 1/72 of an inch.
     * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

      // what's the current page
      if (pageIndex==pageSequence.length) return NO_SUCH_PAGE;
      Point page = pageSequence[pageIndex]; 

      // draw border
      Graphics2D g = (Graphics2D)graphics;
      g.setColor(Color.black);
      
      g.draw(new Rectangle2D.Double(
        pageFormat.getImageableX(),
        pageFormat.getImageableY(),
        pageFormat.getImageableWidth(),
        pageFormat.getImageableHeight()
      ));
      
      // render it
      renderer.renderPage(page, g, resolution);
      
      // done
      return PAGE_EXISTS;

    }

  } //PrintableImpl  

//  /**
//   * TestRenderer//   */
//  private class TestRenderer implements PrintRenderer {
//    /**
//     * @see genj.print.PrintRenderer#getNumPages(java.awt.geom.Point2D)
//     */
//    public Point getNumPages(Point2D pageSize) {
//      System.out.println(pageSize);
//      return new Point(3,2);
//    }
//    /**
//     * @see genj.print.PrintRenderer#renderPage(java.awt.Point, gj.ui.UnitGraphics)
//     */
//    public void renderPage(Point page, UnitGraphics g) {
//
//      Rectangle2D clip = g.getClip();
//      g.translate(
//        clip.getX() + clip.getWidth()/2,
//        clip.getY() + clip.getHeight()/2
//      );
//
//      g.draw(new Rectangle2D.Double(-21.5/4, -28.0/4, 21.5/2, 28.0/2),0,0,false);
//      g.draw("Page x="+page.x, 0, 0, 0.0D);
//      g.draw("Page y="+page.y, 0, 0, 1.0D);
//      
//      int h = g.getFontMetrics().getHeight();
//      
//      g.draw(-21.5/4, 0, 21.5/4, 0, 0, -h/2);
//      g.draw(-21.5/4, 0, 21.5/4, 0, 0, h/2);
//      
//    }
//  } //TestRenderer

} //PrintManager
