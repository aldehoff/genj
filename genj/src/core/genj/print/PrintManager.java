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
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * A manager for printing */
public class PrintManager {
  
  // 1 printunit = 1/72 inch = 1/72 * 2.54 cm
  // => cm / 2.54 * 72 = printunit
  //private final static double CM2PRINTUNIT = 1/2.54 * 72;
  
  /** the default resolution in dots per cm */
//  private final static Point2D.Double resolution = 
//    new Point2D.Double(72/2.54, 72/2.54);

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
   * Prints a view
   */
  public boolean print(JComponent view) {
    // calculate Printer
    Printer printer = getPrinter(view);
    if (printer==null) return false;
    printer.setView(view);
    // our own task for printing
    new PrintTask(printer, view);    
    // done
    return false;
  }
  
  /**
   * Returns a view's printer
   */
  public static Printer getPrinter(JComponent view) {
    try {
      return (Printer)Class.forName(view.getClass().getName()+"Printer").newInstance();
    } catch (Throwable t) {
    }
    return null;
  }

  /**
   * Resolves whether a view can be printed   */
  public static boolean hasPrinter(JComponent view) {
    try {
      if (Printer.class.isAssignableFrom(Class.forName(view.getClass().getName()+"Printer")))
      return true;
    } catch (Throwable t) {
    }
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
    private Printer renderer;
    
    /** pages */
    private Point pages;
    
    /**
     * Constructor     */
    private PrintTask(Printer reNderer, JComponent owner) {
      
      // remember renderer
      renderer = reNderer;
      
      // create a job
      job = PrinterJob.getPrinterJob();
      
      // initial page format
      pageFormat = job.defaultPage();
      pageFormat.setOrientation(registry.get("printer.orientation", PageFormat.PORTRAIT));
      
      // show dialog
      boolean cont = showDialog(owner);
      if (!cont||getPages().x==0||getPages().y==0) {
        job.cancel();
        return;
      }

      // glue to us as the printable     
      job.setPrintable(new PrintableImpl(getPages(), getResolution(), renderer), pageFormat);
      
      // call
      try {
        job.print();
      } catch (PrinterException pe) {
        Debug.log(Debug.WARNING, this, "print() threw error", pe);
        JOptionPane.showMessageDialog(owner, pe.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
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
    
    /**
     * Show page dialog     */
    /*package*/ void showPageDialog() {
      // let the user change things
      pageFormat = job.pageDialog(getPageFormat());
      // preserve page format
      registry.put("printer.orientation", pageFormat.getOrientation());
      // reset pages
      pages = null;
      // done            
    }
    
    /**
     * PageFormat
     */
    /*package*/ PageFormat getPageFormat() {
      return pageFormat;
    }

    /**
     * Resolve resolution     */
    /*package*/ Point getResolution() {
      return new Point(72, 72);
    }
    
    /**
     * PageSize in dpi
     */
    /*package*/ Dimension getPageSize() {
      return new Dimension(
        (int)pageFormat.getImageableWidth(),
        (int)pageFormat.getImageableHeight()
      );
    }
    
    /**
     * Resolve pages     */
    /*package*/ Point getPages() {

      // already calculated?
      if (pages!=null) return pages;
      
      // FIXME fix resolution if required (e.g. 300)
      Dimension size = renderer.calcSize(getResolution());
      Dimension page = getPageSize();
      
      pages = new Point(
        (int)Math.ceil( ((float)size.width ) / page.width),
        (int)Math.ceil( ((float)size.height) / page.height)
      );
      
      // done
      return pages;
    }
    
  } //PrintTask

  /**
   * PrintManager
   */
  private static class PrintableImpl implements Printable {
    
    /** renderer we use */
    private Printer renderer;
    
    /** the resolution */
    private Point resolution;
    
    /** pages */
    private Point[] pageSequence;
    
    /**
     * Constructor
     */
    /*package*/ PrintableImpl(Point pages, Point resolUtion, Printer rendErer) {
  
      // remember renderer
      renderer = rendErer;
      resolution = resolUtion;
      
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
     * Callback for actual printing  
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
      
      
      // translate for content
      graphics.translate(
        (int)pageFormat.getImageableX(), 
        (int)pageFormat.getImageableY()
      );
      graphics.translate(
        -(int)(page.x*pageFormat.getImageableWidth ()), 
        -(int)(page.y*pageFormat.getImageableHeight())
      );

      // render it
      renderer.renderPage(g, page, resolution);
      
      // done
      return PAGE_EXISTS;
  
    }
  
  } //PrintableImpl  


} //PrintManager
