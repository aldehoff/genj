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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

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
  
  /**
   * Constructor   */
  public PrintWidget(PrintManager.PrintTask tAsk) {
    
    // remember task
    task = tAsk;
    
    // create panels
    MainPanel panelMain = new MainPanel();
    JComponent panelPreview = new JScrollPane(new Preview());
    
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
      // layout
      add(new JLabel("Printing is under development - This won't work!"));
      ButtonHelper bh = new ButtonHelper().setContainer(this);
      bh.create(new PageSetup());
      // done
    }

  } //MainPanel
  
  /**
   * The preview   */
  private class Preview extends JComponent {

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      g.setColor(Color.gray);
      g.fillRect(0,0,getWidth(),getHeight());
      g.setColor(Color.white);
      Point pages = task.getPages(); 
      g.drawString(pages.x+" x "+pages.y,32,32);
      
      Graphics2D g2d = (Graphics2D)g;
      
      //task.getRenderer().renderPage(g2d, new Point(0,0), task.getResolution());
    }

  } //Preview

  /**
   * Show Page Setup
   */
  private class PageSetup extends ActionDelegate {
    /**
     * Constructor
     */
    private PageSetup() {
      super.setText("Page Setup");
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      task.showPageDialog();
    }
  } //PrintDlg
  
  
} //PrintWidget
