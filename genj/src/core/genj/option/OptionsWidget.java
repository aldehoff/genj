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

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
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
    
    table = new JTable(model, columns) {
      /** we know how to find the correct editor */
      public TableCellEditor getCellEditor(int row, int col) {
        // only editor for 2nd column 
        if (col==0)   
          return null;
        Option option = model.getOption(row);
        // multiple choice option?
        if (option instanceof MultipleChoiceOption) {
          MultipleChoiceOption moption = (MultipleChoiceOption)option;
          return new DefaultCellEditor(new JComboBox(moption.getChoices()));
        }
        // boolean option?
        if (option.getType()==Boolean.TYPE)
          return new DefaultCellEditor(new JCheckBox());
        // all else
        return new DefaultCellEditor(new JTextField(12));
      }
      /** we know how to find the correct renderer */
      public TableCellRenderer getCellRenderer(int row, int col) {
        // easy for first column
        if (col==0)
          return new DefaultTableCellRenderer();
        // check boolean
        Option option = model.getOption(row);
        if (option.getType()==Boolean.TYPE) {
          TableCellRenderer result = getDefaultRenderer(Boolean.class);
          if (result instanceof JCheckBox)
            ((JCheckBox)result).setHorizontalAlignment(JCheckBox.LEFT);
          return result;
        }
        // all else
        return new DefaultTableCellRenderer();
      }
    };
    
    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(table));    

    // done
  }
  
  /**
   * Set options to display
   */
  public void setOptions(Option[] options) {
    // let model know
    model.setOptions(options);
    
    // recalc column widths
    int w = 48;
    for (int i=0;i<options.length;i++) {
      w = Math.max(w,
        table.getCellEditor(i,1).getTableCellEditorComponent(table, null, false,  i, 1)
          .getPreferredSize().width
        );
    }
    table.getColumnModel().getColumn(0).setPreferredWidth(getWidth());
    table.getColumnModel().getColumn(1).setPreferredWidth(w);

    // layout
    doLayout();
  }
  
  /** 
   * Model
   */
  private class Model extends AbstractTableModel {

    /** options we're looking at */
    private Option[] options = new Option[0];

    /**
     * Get options by index
     */
    private Option getOption(int row) {
      return options[row];
    }
    
    /**
     * Set options to display
     */
    private void setOptions(Option[] set) {
      options = set;
      fireTableDataChanged();
    }
    
    /**
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int row, int col) {
      return col==1;
    }
    
    /**
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt(Object value, int row, int col) {
      getOption(row).setValue(value);
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
      return col==0 ? option.getName() : option.getValue();
    }
    
  } //Model

} //OptionsWidget
