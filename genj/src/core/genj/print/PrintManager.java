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

import java.awt.Dimension;
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
  
  /**
   * Get available services
   */
  public PrintService[] getServices() {
    try {
      PrinterJob job = PrinterJob.getPrinterJob();
      PrintService[] result = job.lookupPrintServices();
      job.cancel();
      return result;
    } catch (Throwable t) {
      return new PrintService[0];
    }
  }
  
  /**
   * Show a print dialog
   */
  public boolean showPrintDialog(JComponent owner) {
    
    PrintWidget widget = new PrintWidget();
    
    JDialog dlg = App.getInstance().createDialog("Printing", "print", new Dimension(480,320), owner);
    dlg.getContentPane().add(widget);
    dlg.pack();
    dlg.setModal(true);
    dlg.show();
    
    return false;
  }  
//      System.out.println("1");
//      PrinterJob pj = PrinterJob.getPrinterJob();
//      
//      try {
//        
//        PrintService[] ps = pj.lookupPrintServices();
//        for (int i = 0; i < ps.length; i++) {
//          System.out.println(ps[i].getName());
//          pj.setPrintService(ps[i]);
//          PageFormat pf = pj.defaultPage();
//          
//          System.out.println(
//            pf.getWidth()/0.393701/72 +"/"+ pf.getWidth()/0.393701/72 
//          );
//        }
//        
//      } catch (PrinterException e) {
//        e.printStackTrace();
//      }
//      
//      pj.pageDialog(new PageFormat()); //native page format
//      pj.pageDialog(new HashPrintRequestAttributeSet());
//      pj.printDialog(new HashPrintRequestAttributeSet());
      
  
} //PrintManager
