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
import genj.util.GridBagHelper;
import genj.util.swing.ButtonHelper;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;

import javax.print.PrintService;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
    
    /** checkbox for orientation */
    private JRadioButton 
      radioPortrait   = new JRadioButton("Portrait"), 
      radioLandscape = new JRadioButton("Landscape");
  
    /**
     * Constructor     */
    private MainPanel() {
       
      // setup combo PrintServices
      comboServices.setModel(new DefaultComboBoxModel(task.getPrintServices()));
      comboServices.addActionListener((ActionListener)new SelectService().as(ActionListener.class));
      comboServices.setSelectedItem(task.getPrintService());
      comboServices.setRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          return super.getListCellRendererComponent(list,((PrintService)value).getName(),index, isSelected, cellHasFocus);
        }
      });
 
      // setup radios for orientation
      ButtonGroup bg = new ButtonGroup();bg.add(radioPortrait);bg.add(radioLandscape);
      ActionListener a = (ActionListener)new SelectOrientation().as(ActionListener.class);
      radioPortrait .addActionListener(a);
      radioLandscape.addActionListener(a);
      PageFormat page = task.getPageFormat();
      if (page.getOrientation()==page.PORTRAIT) radioPortrait.setSelected(true);
      else radioLandscape.setSelected(true);
      
      // layout
      ButtonHelper bh = new ButtonHelper();
      
      GridBagHelper gh = new GridBagHelper(this);
      gh.add(new JLabel("Printer")    , 0, 0);
      gh.add(comboServices            , 1, 0, 1, 1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
      gh.add(new JLabel("Orientation"), 0, 1);
      gh.add(radioPortrait            , 1, 1, 1, 1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
      gh.add(radioLandscape           , 1, 2, 1, 1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
      gh.add(Box.createGlue()         , 0,99,99, 1, gh.GROW_BOTH);
      
      // done
    }

    /**
     * Select Print Service
     */
    private class SelectService extends ActionDelegate {
      /**
       * @see genj.util.ActionDelegate#execute()
       */
      protected void execute() {
        task.setPrintService((PrintService)comboServices.getSelectedItem());
      }
    } //SelectService
    
    /**
     * Select Orientation
     */
    private class SelectOrientation extends ActionDelegate {
      /**
       * @see genj.util.ActionDelegate#execute()
       */
      protected void execute() {
        task.getPageFormat().setOrientation(
          radioLandscape.isSelected() ? PageFormat.LANDSCAPE : PageFormat.PORTRAIT
        );
      }
    } //SelectOrientation
    
  } //MainPanel
  
  /**
   * A panel for preview
   */
  private class PreviewPanel extends JPanel {
  } //PreviewPanel
  
} //PrintWidget
