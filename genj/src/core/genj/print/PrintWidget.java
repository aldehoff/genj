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

import genj.util.GridBagHelper;
import genj.util.swing.ButtonHelper;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.print.PrintService;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
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
    
    /** combobox with printservices */
    private JComboBox comboServices = new JComboBox();
  
    /**
     * Constructor     */
    private MainPanel() {
       
      // setup combo PrintServices
      comboServices.setModel(new DefaultComboBoxModel(task.getPrintServices()));
      comboServices.addActionListener(new ActionListener() {
        /** PrintService selection */
        public void actionPerformed(ActionEvent e) {
          task.setCurrentPrintService((PrintService)comboServices.getSelectedItem());
        }
      });
      comboServices.setSelectedItem(task.getCurrentPrintService());
      comboServices.setRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          return super.getListCellRendererComponent(list,((PrintService)value).getName(),index, isSelected, cellHasFocus);
        }
      });
      
      // layout
      ButtonHelper bh = new ButtonHelper();
      
      GridBagHelper gh = new GridBagHelper(this);
      gh.add(new JLabel("Printer"), 0, 0);
      gh.add(comboServices        , 1, 0, 1, 1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
      gh.add(Box.createGlue()     , 0,99, 2, 1, gh.GROW_BOTH);
      
      // done
    }
    
  } //MainPanel
  
  /**
   * A panel for preview
   */
  private class PreviewPanel extends JPanel {
  } //PreviewPanel
} //PrintWidget
