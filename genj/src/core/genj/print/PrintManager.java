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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.swing.JComponent;
import javax.swing.JDialog;

/**
 * A manager for printing */
public class PrintManager {

  /** singleton */
  private static PrintManager instance = null;
  
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
  
//  /**
//   * Set current server//   */
//  /*package*/ void setService(PrintService service) {
//    try {
//      currentJob.setPrintService(service);
//    } catch (PrinterException pe) {
//      throw new RuntimeException("Changing Printer failed");
//    }
//  }
//  
//  /**
//   * Gets the current page characteristics//   */
//  /*package*/ PageFormat getPageFormat() {
//    return currentJob.defaultPage();
//  }
  
  /**
   * Show a print dialog
   */
  public boolean showPrintDialog(JComponent owner) {
    // our own task for printing
    new PrintTask(owner);    
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

    /**
     * Constructor     */
    private PrintTask(JComponent owner) {
      
      // create a job
      job = PrinterJob.getPrinterJob();
      
      // create a widget
      widget = new PrintWidget(this);
      
      // show    
      JDialog dlg = App.getInstance().createDialog("Printing", "print", new Dimension(480,320), owner);
      dlg.getContentPane().add(widget);
      dlg.pack();
      dlg.setModal(true);
      dlg.show();
      
      // print test
      job.setPrintable(new Printable() {
        /**
         * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
         */
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
          
          if (pageIndex>1) return NO_SUCH_PAGE;
          
          Graphics2D g = (Graphics2D)graphics;
          g.setColor(Color.black);
          
          g.draw(new Rectangle2D.Double(
            pageFormat.getImageableX(),
            pageFormat.getImageableY(),
            pageFormat.getImageableWidth(),
            pageFormat.getImageableHeight()
          ));
          
          // 1 cm = 0.393701*inches
          double CM = 0.393701*72;
          
          g.draw(new Rectangle2D.Double(
            0,//pageFormat.getImageableX(),
            0,//pageFormat.getImageableY(),
            21.5D/2*CM,
            28.0D/2*CM
          ));
          
          return PAGE_EXISTS;
        }
      });
      
      try {
        job.print();
      } catch (Throwable t) {
        t.printStackTrace();
      }
      
      // cancel job
      job.cancel();
      
      // done
    }
    
    /**
     * The available services     */
    /*package*/ PrintService[] getPrintServices() {
      return job.lookupPrintServices();
    }
    
    /**
     * Current service     */
    /*package*/ PrintService getCurrentPrintService() {
      return job.getPrintService();
    }
    
    /**
     * Current service     */
    /*package*/ void setCurrentPrintService(PrintService service) {
      try {
        job.setPrintService(service);
      } catch (PrinterException e) {
        Debug.log(Debug.WARNING, this, "Couldn't change printer", e);
      }
    }
  } //WidgetListener  
} //PrintManager
