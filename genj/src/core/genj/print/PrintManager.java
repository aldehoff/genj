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

import java.awt.Dimension;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
  private static class PrintTask implements PropertyChangeListener {
    
    /** our print job */
    private PrinterJob job;
    
    /** the print widget */
    private PrintWidget widget;

    /**
     * Constructor     */
    protected PrintTask(JComponent owner) {
      
      // create a job
      job = PrinterJob.getPrinterJob();
      
      // find services
      PrintService[] services = job.lookupPrintServices();
          
      // create a widget
      widget = new PrintWidget(services, services.length>0?services[0]:null, job.defaultPage());
      
      // listen to it
      widget.addPropertyChangeListener(this);
  
      // show    
      JDialog dlg = App.getInstance().createDialog("Printing", "print", new Dimension(480,320), owner);
      dlg.getContentPane().add(widget);
      dlg.pack();
      dlg.setModal(true);
      dlg.show();
      
      // cancel job
      job.cancel();
      
      // done
    }
    
    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
      // service?
      if ("service".equals(evt.getPropertyName())) {
        try {
          job.setPrintService((PrintService)evt.getNewValue());
        } catch (PrinterException e) {
          Debug.log(Debug.WARNING, this, "Couldn't change printer", e);
        }
        widget.setPageFormat(job.defaultPage());
      }
      // nothing we know
    }
  } //WidgetListener  
} //PrintManager
