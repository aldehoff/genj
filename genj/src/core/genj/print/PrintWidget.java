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
import java.awt.print.PageFormat;

import javax.print.PrintService;
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
  
  /** handle to print manager */
  private PrintManager manager = PrintManager.getInstance();
  
  /** panels */
  private MainPanel panelMain;
  
  /** PageFormat */
  private PageFormat pageFormat;
  
  /**
   * Constructor   */
  public PrintWidget(PrintService[] services, PrintService current, PageFormat pageFormAt) {
    
    // remember format
    pageFormat = pageFormAt;
    
    // create panels
    panelMain = new MainPanel(services, current);
    
    // layout
    add("Main", panelMain);
    
    // done    
  }
  
  /**
   * Method setPageFormat.
   * @param pageFormat
   */
  public void setPageFormat(PageFormat pageFormAt) {
    pageFormat = pageFormAt;
    System.out.println(pageFormat.getWidth()+"/"+pageFormat.getHeight());    
    System.out.println(pageFormat.getImageableWidth()+"/"+pageFormat.getImageableHeight());    
  }
  
  /**
   * Main Panel
   */
  private class MainPanel extends JPanel implements ActionListener {
    
    /** combobox with printservices */
    private JComboBox comboServices = new JComboBox();
  
    /**
     * Constructor     */
    private MainPanel(PrintService[] services, PrintService current) {
       
      // setup combo PrintServices
      comboServices.setModel(new DefaultComboBoxModel(services));
      comboServices.addActionListener(this);
      if (current!=null) comboServices.setSelectedItem(current);
      comboServices.setRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          return super.getListCellRendererComponent(list,((PrintService)value).getName(),index, isSelected, cellHasFocus);
        }
      });
      
      // layout
      ButtonHelper bh = new ButtonHelper();
      
      GridBagHelper gh = new GridBagHelper(this);
      gh.add(new JLabel("Printer"), 0, 0);
      gh.add(comboServices        , 1, 0);
      
      // done
    }
    
    /**
     * other service selected
     */
    public void actionPerformed(ActionEvent e) {
      PrintWidget.this.firePropertyChange("service", null, comboServices.getSelectedItem());
    }

  } //MainPanel

} //PrintWidget
