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
import genj.util.Debug;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.Trackable;
import genj.window.WindowManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * A manager for printing */
public class PrintManager {
  
  /** singleton */
  private static PrintManager instance = null;
  
  /** registry */
  private static Registry registry;
  
  /** resources */
  private Resources resources = Resources.get(PrintManager.class);
  
  /** window manager */
  private WindowManager winMgr;
  
  /**
   * Constructor   */
  public PrintManager(Registry reGistry, WindowManager winManager) {
    registry = reGistry;
    winMgr = winManager;
  }
  
  /**
   * Prints a view
   */
  public void print(Printer printer, String title, JComponent owner) {
    // our own task for printing
    new PrintTask(printer, title, owner).trigger();    
    // done
  }
  

  /**
   * Our own task for printing   */  
  /*package*/ class PrintTask extends ActionDelegate implements Printable, Trackable {
    
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
    
    /** the title */
    private String title;
    
    /**
     * Constructor     */
    private PrintTask(Printer reNderer, String tiTle, JComponent owNer) {
       
      // remember renderer
      renderer = reNderer;
      owner = owNer;
      title = tiTle;
      
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
      int choice = winMgr.openDialog(
        "print",
        resources.getString("dlg.title", title),
        WindowManager.IMG_QUESTION,
        widget,
        new String[]{ resources.getString("dlg.label.print"), UIManager.getString("OptionPane.cancelButtonText")},
        owner,
        null,
        null
      );
      
      // check choice
      if (choice!=0||getPages().x==0||getPages().y==0) {
        job.cancel();
        return false;
      }

      // setup progress dlg
// FIXME missing      
//      new ProgressDialog(frame, title, "", this, getThread());
      
      // continue
      return true;
    }
    
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      // this is on another thread
      try {
        Debug.log(Debug.INFO, this, "Printing to "+getPrintService()+"("+job+")");
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
     * Callback for actual printing  
     * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
     */
    public int print(Graphics graphics, PageFormat ignore, int pageIndex) throws PrinterException {
  
      // what's the current page
      int 
        row = pageIndex/getPages().x,
        col = pageIndex%getPages().x;
      if (row>=getPages().x||row>=getPages().y) return NO_SUCH_PAGE;

      page = pageIndex;

      // clip to page bounds (seems like the clip already set can't be trusted)
      Rectangle page = getImageable();
      graphics.setClip(page);
      
      // draw border
      graphics.setColor(Color.lightGray);
      graphics.drawRect(page.x,page.y,page.width,page.height);

      // translate for content
      Rectangle imageable = getImageable();
      graphics.translate(imageable.x - col*imageable.width, imageable.y - row*imageable.height);

      // render it
      renderer.renderPage((Graphics2D)graphics, new Point(col, row), getResolution(), false);
      
      // done
      return PAGE_EXISTS;
  
    }

    // I wished we could just use one of both, either
    //    job.printDialog or job.pageDialog
    // but Java's printing 
    //  + doesn't update the pageFormat when going 
    //    through printDialog and 
    //  + doesn't change the PrinterJob's printer that
    //    the data ends up with (stays default) when
    //    going through pageDialog 
    // :(
  
    /**
     * Show page dialog     */
    /*package*/ void showPageDialog() {
      pageFormat = job.pageDialog(pageFormat);
      job.validatePage(pageFormat);
      // preserve page format
      registry.put("printer.orientation", pageFormat.getOrientation());
      // reset pages
      pages = null;
      // done            
    }
    
    /**
     * Show printer dialog
     */
    /*package*/ void showPrinterDialog() {
      // do it
      job.printDialog();
      // reset where possible
      pageFormat = job.validatePage(pageFormat);
      // preserve page format
      registry.put("printer.orientation", pageFormat.getOrientation());
      // reset pages
      pages = null;
      // done            
    }
    
    /**
     * Resolve resolution     */
    /*package*/ Point getResolution() {
      return new Point(72, 72);
    }
    
    /**
     * PageSize in dots
     */
    /*package*/ Rectangle getPage() {
      return new Rectangle(
        0,
        0,
        (int)pageFormat.getWidth(),
        (int)pageFormat.getHeight()
      );
    }
    
    /**
     * ImageableSize in dots
     */
    /*package*/ Rectangle getImageable() {
      return new Rectangle(
        (int)pageFormat.getImageableX(),
        (int)pageFormat.getImageableY(),
        (int)pageFormat.getImageableWidth(),
        (int)pageFormat.getImageableHeight()
      );
    }
    
    /**
     * Compute pages     */
    /*package*/ Point getPages() {

      // already calculated?
      if (pages!=null) return pages;
      
      Dimension content = renderer.calcSize(getResolution());
      Rectangle imageable = getImageable();
      
      pages = new Point(
        (int)Math.ceil( (float)content.width  / imageable.width),
        (int)Math.ceil( (float)content.height / imageable.height)
      );

      // done
      return pages;
    }
    
    /**
     * Computer printer name
     */
    /*package*/ String getPrintService() {
      Object name = "";
      try {
        name = job.getClass().getMethod("getPrintService", new Class[0]).invoke(job,new Object[0]);
      } catch (Throwable t) {
      }
      return name.toString();
    }
    
    /**
     * print renderer
     */
    /*package*/ Printer getRenderer() {
      return renderer;
    }
    
  } //PrintTask

} //PrintManager
