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
package genj.option;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 * A widget for displaying options in tabular way
 */
public class OptionsWidget extends JPanel {

  /** table we're using */
  private JTable table;
  
  /** model we're using */
  private Model model;
  
  /**
   * Constructor
   */
  public OptionsWidget() {
    
    // setup
    DefaultTableColumnModel columns = new DefaultTableColumnModel();
    columns.addColumn(new TableColumn(0));
    columns.addColumn(new TableColumn(1));
    model = new Model();
    table = new JTable(model, columns);
    
    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(table));    

    // done
  }
  
  /**
   * Set options to display
   */
  public void setOptions(Option[] options) {
    model.setOptions(options);
  }
  
  /** 
   * Model
   */
  private static class Model extends AbstractTableModel {

    /** options we're looking at */
    private Option[] options = new Option[0];

    /**
     * Set options to display
     */
    private void setOptions(Option[] set) {
      options = set;
      fireTableDataChanged();
    }
  
    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
      return 2;
    }
    
    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
      return options.length;
    }
    
    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
      Option option = options[row];
      return col==0 ? option.getName() : option.toText();
    }
    
  } //Model

} //OptionsWidget
