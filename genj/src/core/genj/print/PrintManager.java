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
import genj.util.Debug;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.Trackable;
import genj.util.swing.ProgressDialog;

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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * A manager for printing */
public class PrintManager {
  
  /** singleton */
  private static PrintManager instance = null;
  
  /** registry */
  private static Registry registry = Registry.lookup("genj");
  
  /** resources */
  private Resources resources = Resources.get(PrintManager.class);
  
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
  public void print(JFrame frame, JComponent view) {
    // calculate Printer
    Printer printer = getPrinter(view);
    if (printer==null) return;
    printer.setView(view);
    // our own task for printing
    new PrintTask(frame, printer, view).trigger();    
    // done
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
  /*package*/ class PrintTask extends ActionDelegate implements Printable, Trackable {
    
    /** the base frame */
    private JFrame frame;
    
    /** the owning component */
    private JComponent owner;
    
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
    
    /** current page*/
    private int page = 0;
    
    /** any problem that might occur async */
    private Throwable throwable;
    
    /**
     * Constructor     */
    private PrintTask(JFrame fRame, Printer reNderer, JComponent owNer) {
      
      // remember renderer
      frame = fRame;
      renderer = reNderer;
      owner = owNer;
      
      // setup async
      setAsync(super.ASYNC_SAME_INSTANCE);
     
    }
    
    /**
     * @see genj.util.ActionDelegate#preExecute()
     */
    protected boolean preExecute() {
      
      // create a job
      job = PrinterJob.getPrinterJob();
      if (job==null) {
        Debug.log(Debug.WARNING, this, "Couldn't get PrintJob");
        return false;
      }
      job.setJobName("GenealogyJ");

      // initial page format
      pageFormat = job.defaultPage();
      pageFormat.setOrientation(registry.get("printer.orientation", PageFormat.PORTRAIT));

      // glue to us as the printable     
      job.setPrintable(this, pageFormat);
      
      // show dialog
      widget = new PrintWidget(this, resources);
      
      // show it in dialog
      int ok = App.getInstance().createDialog(
        resources.getString("dlg.title", frame.getTitle()), 
        "print", 
        new Dimension(480,320), 
        owner, 
        widget,
        new String[]{ "Print", UIManager.getString("OptionPane.cancelButtonText")}
      ).packAndShow();
      
      // check choice
      if (ok!=0||getPages().x==0||getPages().y==0) {
        job.cancel();
        return false;
      }

      // setup progress dlg
      new ProgressDialog(frame, resources.getString("progress.title"), "", this, getThread());
      
      // continue
      return true;
    }
    
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      // this is on another thread
      try {
        job.print();
      } catch (PrinterException pe) {
        throwable = pe;
      }
    }
    
    /**
     * @see genj.util.ActionDelegate#postExecute()
     */
    protected void postExecute() {
      if (throwable!=null) {
        Debug.log(Debug.WARNING, this, "print() threw error", throwable);
        JOptionPane.showMessageDialog(owner, throwable.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
      }
    }
    
    /**
     * @see genj.util.Trackable#cancel()
     */
    public void cancel() {
      cancel(true);
    }
    
    /**
     * @see genj.util.Trackable#getProgress()
     */
    public int getProgress() {
      return (int)(page/(float)(getPages().x*getPages().y) * 100);
    }
    
    /**
     * @see genj.util.Trackable#getState()
     */
    public String getState() {
      return resources.getString("progress.page", new String[]{""+(page+1), ""+(getPages().x*getPages().y)} );
    }
    
    /**
     * Show page dialog     */
    /*package*/ void showPrinterDialog() {
      // I wished we could just use job.printDialog here - but
      // Java's printing doesn't update the pageFormat in this
      // method so offering the user to change settings in one
      // of this dialogs is pretty useless - gotta use pageDialog
      // instead :(
      pageFormat = job.pageDialog(pageFormat);
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
     * Compute pages     */
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
    
    /**
     * Computer printer name
     */
    /*package*/ String getPrinter() {
      Object name = "Unknown";
      try {
        name = job.getClass().getMethod("getPrintService", new Class[0]).invoke(job,new Object[0]);
      } catch (Throwable t) {}
      return name.toString();
    }
    
    /**
     * Callback for actual printing  
     * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
  
      // what's the current page
      page = pageIndex;
      int 
        row = pageIndex/getPages().x,
        col = pageIndex%getPages().x;
      if (row>=getPages().x||row>=getPages().y) return NO_SUCH_PAGE;
      
      Point page = new Point(col, row);
  
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
      renderer.renderPage(g, page, getResolution());
      
      // done
      return PAGE_EXISTS;
  
    }
  
  } //PrintTask

} //PrintManager
