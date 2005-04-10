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

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.renderer.Options;
import genj.renderer.PropertyRenderer;
import genj.util.Dimension2d;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.SortableTableHeader;
import genj.util.swing.SortableTableHeader.SortableTableModel;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * A widget that shows entities in rows and columns
 */
public class PropertyTableWidget extends JPanel {
  
  /** a reference to the view manager */
  private ViewManager viewManager;
  
  /**
   * Constructor
   */
  public PropertyTableWidget(PropertyTableModel model, ViewManager manager) {
    
    viewManager = manager;
    
    // setup layout
    setLayout(new BorderLayout());
    JTable table = new JTable(new ModelWrapper(model), new ColumnsWrapper(model));
    table.setDefaultRenderer(Object.class, new PropertyTableCellRenderer());
    table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setCellSelectionEnabled(true);
    table.setTableHeader(new SortableTableHeader());
    add(BorderLayout.CENTER, new JScrollPane(table));
    
    // done
  }
  
  /**
   * Wrapper for swing columns
   */
  private class ColumnsWrapper extends DefaultTableColumnModel {
    
    /** our model */
    private PropertyTableModel model;
    
    /** constructor */
    ColumnsWrapper(PropertyTableModel set) {
      model = set;
      
      for (int i=0,j=model.getNumCols();i<j;i++) {
        TableColumn col = new TableColumn(i);
        TagPath path = model.getPath(i);
        // try to find a reasonable tag to display as text (that's not '.' or '*')
        String tag = path.getLast();
        for (int p=path.length()-2;!Character.isLetter(tag.charAt(0))&&p>=0;p--) 
          tag = path.get(p);
        // keep tag as text
        col.setHeaderValue(Gedcom.getName(tag));
        addColumn(col);
      }
    }
    
  }

  /**
   * Wrapper for swing table
   */
  private class ModelWrapper extends AbstractTableModel implements SortableTableModel, GedcomListener {
    
    /** sort */
    private int sortColumn = 0;

    /** our model */
    private PropertyTableModel model;
    
    /** cached table content */
    private Property cells[][];
    
    /** sorted indexes */
    private int row2row[];

    /** constructor */
    private ModelWrapper(PropertyTableModel set) {
      // setup state
      model = set;
      reset();
      // done
    }
    
    /** reset state */
    private void reset() {
      cells = new Property[model.getNumRows()][model.getNumCols()];
      row2row = new int[model.getNumRows()];
      // init default non-sorted
      for (int i=0;i<row2row.length;i++)
        row2row[i]=i;
      // sort now
      sort();
    }
    
    /** someone interested in us */
    public void addTableModelListener(TableModelListener l) {
      super.addTableModelListener(l);
      // start listening ?
      if (getListeners(TableModelListener.class).length==1)
        model.getGedcom().addGedcomListener(this);
    }
    
    /** someone lost interest */
    public void removeTableModelListener(TableModelListener l) {
      // stop listening ?
      if (getListeners(TableModelListener.class).length==0)
        model.getGedcom().removeGedcomListener(this);
    }
    
    /** underlying gedcom change */
    public void handleChange(Transaction tx) {
      
      // change in number of rows?
      if (model.getNumRows()!=cells.length) {
        reset();
        super.fireTableDataChanged();
        return;
      }
        
      // since properties/cells might have been modified or deleted/added 
      // we simply invalidate all our cached content instead of looking
      // for delate iteratively
      cells = new Property[model.getNumRows()][model.getNumCols()];
      
      fireTableRowsUpdated(0, cells.length);
      
      // done 
      
    }
    
    /** num columns */
    public int getColumnCount() {
      return model.getNumCols();
    }
    
    /** num rows */
    public int getRowCount() {
      return model.getNumRows();
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
      if (sortColumn!=0&&row2row.length>1)
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
}
