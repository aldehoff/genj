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
import genj.util.swing.ButtonHelper;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * A PrintDialog */
public class PrintWidget extends JTabbedPane {
  
  /** panels */
  private 
    MainPanel panelMain;
    PreviewPanel panelPreview;
  
  /** task */
  private PrintManager.PrintTask task;
  
  /**
   * Constructor   */
  public PrintWidget(PrintManager.PrintTask tAsk) {
    
    // remember task
    task = tAsk;
    
    // create panels
    panelMain = new MainPanel();
    panelPreview = new PreviewPanel();
    
    // layout
    add("Main", panelMain);
    add("Preview", panelPreview);
    
    // done    
  }
  
  /**
   * Main Panel
   */
  private class MainPanel extends JPanel {

    /**
     * Constructor     */
    private MainPanel() {
       
       add(new JLabel("Printing is under development - This won't work!"));
      // layout
      ButtonHelper bh = new ButtonHelper().setContainer(this);
      bh.create(new PageDlg());
      // done
    }

    /**
     * Open Page Dialog
     */
    private class PageDlg extends ActionDelegate {
      /**
       * Constructor
       */
      private PageDlg() {
        super.setText("Page Properties");
      }
      /**
       * @see genj.util.ActionDelegate#execute()
       */
      protected void execute() {
        task.showPageDialog();
      }
    } //PrintDlg
    
  } //MainPanel
  
  /**
   * A panel for preview
   */
  private class PreviewPanel extends JPanel {
  } //PreviewPanel
  
} //PrintWidget
