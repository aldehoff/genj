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

import java.awt.BorderLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * A PrintDialog */
public class PrintWidget extends JTabbedPane {
  
  /** handle to print manager */
  private PrintManager manager = PrintManager.getInstance();
  
  /** panels */
  private MainPanel main;

  /**
   * Constructor   */
  public PrintWidget() {
    
    // create panels
    main = new MainPanel();
    
    // layout
    add("Main", main);
    
    // done    
  }
  
  /**
   * Main Panel
   */
  private class MainPanel extends JPanel {
    
    /** combobox with printservices */
    private JComboBox services = new JComboBox();
  
    /**
     * Constructor     */
    private MainPanel() { 
      // setup combo PrintServices
      services.setModel(new DefaultComboBoxModel(manager.getServices()));
      // layout
      setLayout(new BorderLayout());
      add(services, BorderLayout.NORTH);
      // done
    }
  } //MainPanel
} //PrintWidget
