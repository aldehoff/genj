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
package genj.common;

import genj.gedcom.Property;
import genj.renderer.Options;
import genj.renderer.PropertyRenderer;
import genj.util.Dimension2d;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.SortableTableHeader;
import genj.util.swing.SortableTableHeader.SortableTableModel;
import genj.view.Context;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * A widget that shows entities in rows and columns
 */
public class PropertyTableWidget extends JPanel {

  public final static int
    CONTEXT_PROPAGATION_ON_SINGLE_CLICK = 0,
    CONTEXT_PROPAGATION_ON_DOUBLE_CLICK = 1;
  
  /** a reference to the view manager */
  private ViewManager viewManager;
  
  /** table component */
  private JTable table;
  
  /** context propagation */
  private boolean contextPropagationOnDoubleClick = false;
  
  /**
   * Constructor
   */
  public PropertyTableWidget(PropertyTableModel propertyModel, ViewManager manager) {
    
    viewManager = manager;
    
    // create table comp
    table = new JTable();
    table.addMouseListener(new InteractionHandler());
    table.setDefaultRenderer(Object.class, new PropertyTableCellRenderer());
    table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setTableHeader(new SortableTableHeader());
    table.getColumnModel().setColumnSelectionAllowed(true);
    table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    // setup layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(table));
    
    // set model
    setModel(propertyModel);
    
    // done
  }
  
  /**
   * Setter for current model
   */
  public void setModel(PropertyTableModel set) {
    table.setModel(new Model(set));
    //table.setColumnModel(new ColumnsWrapper(set));
  }
  
  /**
   * Set column resize behavior
   */
  public void setAutoResize(boolean on) {
    table.setAutoResizeMode(on ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);
  }
  
  /**
   * Context Propagation on double click?
   */
  public void setContextPropagation(int mode) {
    contextPropagationOnDoubleClick = mode==CONTEXT_PROPAGATION_ON_DOUBLE_CLICK;
  }
  
  /**
   * Resolve row for property
   */
  public int getRow(Property row) {
    return ((Model)table.getModel()).getRow(row);
  }
  
  /**
   * Resolve col for property
   */
  public int getCol(int row, Property col) {
    return ((Model)table.getModel()).getCol(row, col);
  }
  
  /**
   * Select a cell
   */
  public void select(Property row, Property col) {
    int r = getRow(row);
    int c = getCol(r, col);

    // scroll to visible
    table.scrollRectToVisible(table.getCellRect(r,c,true));

    // change selection
    if (c>=0)
      table.setColumnSelectionInterval(c,c);
    if (r>=0)
      table.setRowSelectionInterval(r,r);
  }
  
  /**
   * Return column widths
   */
  public int[] getColumnWidths() {
    TableColumnModel columns = table.getColumnModel();
    int[] result = new int[columns.getColumnCount()];
    for (int c=0; c<result.length; c++) {
      result[c] = columns.getColumn(c).getWidth();
    }
    return result;
  }
  
  /**
   * Set column widths
   */
  public void setColumnWidths(int[] widths) {
    TableColumnModel columns = table.getColumnModel();
    for (int i=0, j=columns.getColumnCount(); i<widths.length&&i<j; i++) {
      TableColumn col = columns.getColumn(i);
      col.setWidth(widths[i]);
      col.setPreferredWidth(widths[i]);
    }
  }
  
  /**
   * Get sorted column
   */
  public int getSortedColumn() {
    return ((Model)table.getModel()).getSortedColumn();
  }

  /**
   * Set sorted column
   */
  public void setSortedColumn(int set) {
    ((Model)table.getModel()).setSortedColumn(set);
  }
  
  /**
   * Access to table model
   * TODO This is here to allow for TableView to access the information provided by our
   * internal classes for printing. We should really migrate the printing code itself to this class
   * since it then can be used by all users of this widget.
   */
  public TableModel getModel() {
    return table.getModel();
  }

//  /**
//   * Wrapper for swing columns
//   */
//  private class ColumnsWrapper extends DefaultTableColumnModel {
//    
//    PropertyTableModel model;
//    
//    /** constructor */
//    ColumnsWrapper(PropertyTableModel set) {
//      model = set;
//      setColumnSelectionAllowed(true);
//      getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//    }
//    
//    /** */
//    public void addColumn(TableColumn col) {
//    
//      // patch auto generated column
//      Object value = model.getHeader(col.getModelIndex());
//      if (value!=null)
//        col.setHeaderValue(value);
//      
//      // continue
//      super.addColumn(col);
//    }
//  }

  /**
   * Wrapper for swing table
   */
  private class Model extends AbstractTableModel implements SortableTableModel, PropertyTableModelListener {
    
    /** sort */
    private int sortColumn = 0;

    /** our model */
    private PropertyTableModel model;
    
    /** cached table content */
    private Property cells[][];
    
    /** sorted indexes */
    private int row2row[];

    /** constructor */
    private Model(PropertyTableModel set) {
      // setup state
      model = set;
      
      if (model!=null)
        handleRowsChange(model);
      // done
    }
    
    public void handleContentChange(PropertyTableModel model) {
      
      // setup cell state
      int 
        rows = model.getNumRows(), 
        cols = model.getNumCols();
      cells = new Property[rows][cols];

      // tell about it
      fireTableRowsUpdated(0, cells.length);
    }
    
    public void handleRowsChange(PropertyTableModel model) {
      // setup cell state
      int 
        rows = model.getNumRows(), 
        cols = model.getNumCols();
      cells = new Property[rows][cols];
      row2row = new int[rows];
      
      // init default non-sorted
      for (int i=0;i<row2row.length;i++)
        row2row[i]=i;
      
      // sort now
      sort();

      // done
      fireTableDataChanged();
    }
    
    /** someone interested in us */
    public void addTableModelListener(TableModelListener l) {
      super.addTableModelListener(l);
      // start listening ?
      if (model!=null&&getListeners(TableModelListener.class).length==1)
        model.addListener(this);
    }
    
    /** someone lost interest */
    public void removeTableModelListener(TableModelListener l) {
      // stop listening ?
      if (model!=null&&getListeners(TableModelListener.class).length==0)
        model.removeListener(this);
    }
    
    /**
     *  patched column name
     */
    public String getColumnName(int col) {
      Object result = model!=null ? model.getHeader(col) : null;
      return result!=null ? result.toString() : "";
    }
    
    /** num columns */
    public int getColumnCount() {
      return model!=null ? model.getNumCols() : 0;
    }
    
    /** num rows */
    public int getRowCount() {
      return model!=null ? row2row.length : 0;
    }
    
    /** context */
    private Context getContextAt(int row, int col) {
      
      // try to find property already cached
      Property prop = getPropertyAt(row, col);
      if (prop==null) 
        prop = model.getProperty(row2row[row]);
      
      return new Context(prop.getGedcom(), prop.getEntity(), prop);
    }
    
    /** property */
    private Property getPropertyAt(int row, int col) {
      
      row = row2row[row];
      
      Property prop = cells[row][col];
      if (prop==null) {
        prop = model.getProperty(row).getProperty(model.getPath(col));
        cells[row][col] = prop;
      }
      return prop;
    }
    
    /** value */
    public Object getValueAt(int row, int col) {
      return getPropertyAt(row, col);
    }
    
    /** sorted column */
    public int getSortedColumn() {
      return sortColumn;
    }
    
    /** set sorted column */
    public void setSortedColumn(int col) {
      // remember
      sortColumn = col;
      // sort
      sort();
      // tell about it
      fireTableDataChanged();
      // done
    }

    /** compare two rows */
    private int compare(int row1, int row2) {
      
      // split sortColumn
      int col = Math.abs(sortColumn)-1;
      
      // here's the rows
      Property 
        p1 = getPropertyAt(row1, col),
        p2 = getPropertyAt(row2, col);
      
      // both or one null?
      if (p1==p2)
        return  0;
      if (p1==null) 
        return -1 * sortColumn;
      if (p2==null) 
        return  1 * sortColumn;
      
      // let property decide
      return p1.compareTo(p2) * sortColumn;
    }
    
    /** 
     * sort - I'm not using Arrays.sort() because I'm not sure that mergesort
     * used is the better choice over quicksort *and* it doesn't operate on an 
     * array of primitives w/comparator
     */
    private void sort() {
      if (sortColumn!=0&&(Math.abs(sortColumn)-1)<getColumnCount()&&row2row.length>1)
        qsort(0,row2row.length-1);
    }
    
    private void qsort(int from, int to) {
      
      // choose pivot v
      int v = (from+to)/2;
      
      // invariant: <= [v] <= 
      int lo = from, hi = to;
      while (lo<=hi) {
        
        for (;compare(lo, v)<0;lo++);
        for (;compare(hi, v)>0;hi--);

        if (lo>hi)
          break;

        int swap = row2row[lo];
        row2row[lo++] = row2row[hi];
        row2row[hi--] = swap;
      }

      // recurse into halfs
      if (from<hi) qsort(from,hi);
      if (lo<to) qsort(lo, to);
      
      // done
    }
    
    /** 
     * Get row by property
     */
    private int getRow(Property row) {

      if (model==null||row==null)
        return -1;
      
      // find row
      for (int i=0; i<row2row.length; i++) {
        if (model.getProperty(row2row[i])==row) 
          return i;
      }	
     
      // not found
      return -1;
    }

    /** 
     * Get col by property
     */
    private int getCol(int row, Property col) {
      // find col
      for (int i=0, j=getColumnCount(); i<j; i++) {
        if (getPropertyAt(row,i)==col)
          return i;
      }
      // not found
      return -1;
    }
  
  } //ModelWrapper
  
  /**
   * Renderer for properties in cells
   */
  private class PropertyTableCellRenderer extends HeadlessLabel implements TableCellRenderer {
    
    /** current property */
    private Property curProp;
    
    /** table */
    private JTable curTable;
    
    /** attributes */
    private boolean isSelected;
    
    /**
     * constructor
     */
    /*package*/ PropertyTableCellRenderer() {
      setFont(Options.getInstance().getDefaultFont());
    }
    
    /**
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focs, int row, int col) {
      // there's a property here
      curProp = (Property)value;
      curTable = table;
      // and some status
      isSelected = selected;
      // ready
      return this;
    }
    
    /**
     * patched preferred size
     */
    public Dimension getPreferredSize() {
      if (curProp==null)
        return new Dimension(0,0);
      return Dimension2d.getDimension(PropertyRenderer.get(curProp).getSize(getFont(), new FontRenderContext(null, false, false), curProp, PropertyRenderer.PREFER_DEFAULT, viewManager.getDPI()));
    }
    
    /**
     * @see genj.util.swing.HeadlessLabel#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
      Graphics2D graphics = (Graphics2D)g;
      // our bounds
      Rectangle bounds = getBounds();
      bounds.x=0; bounds.y=0;
      // background?
      if (isSelected) {
        g.setColor(curTable.getSelectionBackground());
        g.fillRect(0,0,bounds.width,bounds.height);
        g.setColor(curTable.getSelectionForeground());
      } else {
        g.setColor(curTable.getForeground());
      }
      // no prop and we're done
      if (curProp==null) 
        return;
      // set font
      g.setFont(getFont());
      // get the proxy
      PropertyRenderer proxy = PropertyRenderer.get(curProp);
      // let it render
      proxy.render(graphics, bounds, curProp, PropertyRenderer.PREFER_DEFAULT, viewManager.getDPI());
      // done
    }
    
  } //PropertyTableCellRenderer
  
  
  /**
   * Callback for list selections
   */
  private class InteractionHandler extends MouseAdapter {
    
    /** callback - mouse press */
    public void mousePressed(MouseEvent e) {
      // some platforms send popup trigger on pressed
      if (e.isPopupTrigger())
        mouseReleased(e);
    }
    
    /** callback - mouse release */
    public void mouseReleased(MouseEvent e) {
      
      Point pos = e.getPoint();

      // get context
      int row = table.rowAtPoint(pos);
      int col = table.columnAtPoint(pos);
      if (row<0||col<0) 
        return;
      
      // make sure selection is accurate - JTable does
      // only react to 'first' mouse button not second
      if (!table.isCellSelected(row, col))
        table.changeSelection(row, col, false, false);
      
      // context is either entity or property
      Context context = ((Model)table.getModel()).getContextAt(row, col);
      context.setSource(PropertyTableWidget.this);
      
      // context menu?
      if (e.isPopupTrigger()) {
        viewManager.showContextMenu(context, null, table, pos);
        return;
      }
      
      // context propagation?
      if (contextPropagationOnDoubleClick&&e.getClickCount()<2)
        return;
      viewManager.setContext(context);
      
      // done
    }

      
  } //InteractionHandler

} //PropertyTableWidget
