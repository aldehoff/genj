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

import genj.util.swing.ImageIcon;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.ListIterator;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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

  /** an image for options */
  public final static ImageIcon IMAGE = new ImageIcon(OptionsWidget.class, "images/Options.gif");

  /** table we're using */
  private JTable table;
  
  /** model we're using */
  private Model model = new Model();
  
  /** reference to window manager */
  private WindowManager manager;
  
  /**
   * Constructor
   */
  public OptionsWidget(WindowManager manager) {
    this(manager, null);
  }
  
  /**
   * Constructor
   */
  public OptionsWidget(WindowManager manager, List options) {

    this.manager = manager;
        
    // setup
    table = new Table();
     
    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(table));    
    
    // options?
    if (options!=null)
      setOptions(options);

    // done
  }
  
  /**
   * Set options to display
   */
  public void setOptions(List set) {
    
    // check options
    ListIterator it = set.listIterator();
    while (it.hasNext()) {
      Option option = (Option)it.next();
      if (option.getUI(this)==null)
        it.remove();
    }

    // let model know
    Option[] options = (Option[])set.toArray(new Option[set.size()]);
    model.setOptions(options);
    
    // recalc column widths
    int w = 48;
    
    for (int i=0;i<options.length;i++)
      w = Math.max(w, options[i].getUI(this).getComponentRepresentation().getPreferredSize().width);
      
    table.getColumnModel().getColumn(0).setPreferredWidth(Integer.MAX_VALUE);
    table.getColumnModel().getColumn(1).setMinWidth(w);

    // layout
    doLayout();
  }

  /**
   * Access to window manager
   */  
  public WindowManager getWindowManager() {
    return manager;
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
      Option option = getOption(row);
      
      // first column?
      if (col==0)
        return option.getName();
        
        
      // second column!
      if (option instanceof PropertyOption)
        return ((PropertyOption)option).getValue();
        
      // nothing
      return null;
    }
    
  } //Model

  /**
   * our table
   */
  private class Table extends JTable implements TableCellRenderer {

    /** our editor */
    private Editor editor = new Editor();
    
    /** constructor */
    private Table() {
      super(model);

      DefaultTableColumnModel columns = new DefaultTableColumnModel();
      columns.addColumn(new TableColumn(0));
      columns.addColumn(new TableColumn(1));
      super.setColumnModel(columns);
      
      //table.getTableHeader().setReorderingAllowed(false);
      
    }
    
    /**
     * catch remove to commit current editor
     */
    public void removeNotify() {
      if (isEditing())
        editCellAt(-1,-1);
      // continue
      super.removeNotify();
    }

    /** we know how to find the correct editor */
    public TableCellEditor getCellEditor(int row, int col) {
      // easy for first column
      if (col==0)
        return null;
      // ourself for second column
      return editor;
    }
   
    /** we know how to find the correct renderer */
    public TableCellRenderer getCellRenderer(int row, int col) {
      // easy for first column
      if (col==0)
        return new DefaultTableCellRenderer();
      // ourself for second column
      return this;
    }
   
    /** our renderer factory */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      // lookup option and ui
      Option option = model.getOption(row);
      OptionUI ui = option.getUI(OptionsWidget.this);
      // text representation available
      String text = ui.getTextRepresentation();
      if (text!=null)
        return new JLabel(text);
      // use component representation
      return ui.getComponentRepresentation();
    }

    /**
     * Our Editor
     */
    private class Editor extends AbstractCellEditor implements TableCellEditor, ItemListener {  
  
      /** current ui */
      private OptionUI ui;

      /** a component for editing */     
      public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // lookup option and ui
        Option option = model.getOption(row);
        ui = option.getUI(OptionsWidget.this);
        Component result = ui.getComponentRepresentation();
        if (result instanceof ItemSelectable)
          ((ItemSelectable)result).addItemListener(this);
        return result;
      }

      /** callback - no value access through this one though */     
      public Object getCellEditorValue() {
        return null;
      }
    
      /** callback - cancel editing */     
      public void cancelCellEditing() {
        ui = null;
        super.cancelCellEditing();
      }
    
      /** callback - stop editing = commit */     
      public boolean stopCellEditing() {
        if (ui!=null)
          ui.endRepresentation();
        return super.stopCellEditing();
      }

      /** callback - editor component item state changed */      
      public void itemStateChanged(ItemEvent e) {
        stopCellEditing();
      }
  
    } //Editor

  }

} //OptionsWidget
