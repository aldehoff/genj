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
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySimpleValue;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.renderer.Options;
import genj.renderer.PropertyRenderer;
import genj.util.Dimension2d;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.LinkWidget;
import genj.util.swing.SortableTableHeader;
import genj.util.swing.SortableTableHeader.SortableTableModel;
import genj.view.Context;
import genj.view.ContextProvider;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * A widget that shows entities in rows and columns
 */
public class PropertyTableWidget extends JPanel {
  
  private Map type2generator = new HashMap();
  
  { 
    type2generator.put(PropertyName.class, new NameSG()); 
    type2generator.put(PropertyDate.class, new DateSG()); 
    type2generator.put(PropertyPlace.class, new ValueSG()); 
    type2generator.put(PropertySimpleValue.class, new ValueSG()); 
  }
  
  /** a reference to the view manager */
  private ViewManager viewManager;
  
  /** table component */
  private JTable table;
  
  /** shortcuts panel */
  private JPanel panelShortcuts;
  
  /** property tabl model */
  private PropertyTableModel propertyModel;
  
  /**
   * Constructor
   */
  public PropertyTableWidget(ViewManager manager) {
    this(null, manager);
  }
  
  /**
   * Constructor
   */
  public PropertyTableWidget(PropertyTableModel propertyModel, ViewManager manager) {
    
    this.viewManager = manager;
    this.propertyModel = propertyModel;
    
    // create table comp
    table = new Content();
    
    // create panel for shortcuts
    panelShortcuts = new JPanel(new GridLayout(0,1));

    // setup layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(table));
    add(BorderLayout.EAST, panelShortcuts);
    
    // done
  }
  
  /**
   * Component lifecycle callback - added
   */
  public void addNotify() {
    // super 
    super.addNotify();
    // (re)set current model
    if (propertyModel!=null)
      table.setModel(new Model(propertyModel));
  }
  
  /**
   * Component lifecycle callback - removed
   */
  public void removeNotify() {
    super.removeNotify();
    // clear table's current model - lifecycle destructor
    // so to say that will disconnect listeners recursively
    table.setModel(new Model(null));
  }
  
  /**
   * Setter for current model
   */
  public void setModel(PropertyTableModel set) {
    // remember
    propertyModel = set;
    // tell table about it
    table.setModel(new Model(set));
  }
  
  /**
   * Set column resize behavior
   */
  public void setAutoResize(boolean on) {
    table.setAutoResizeMode(on ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);
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
    int c = col!=null ? getCol(r, col) : -1;

    // scroll to visible
    Rectangle visible = table.getVisibleRect();
    Rectangle scrollto = table.getCellRect(r,c,true);
    if (c<0) scrollto.x = visible.x;
    table.scrollRectToVisible(scrollto);

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
   */
  public PropertyTableModel getModel() {
    return propertyModel;
  }
  
  /**
   * Create shortcuts for given tag
   */
  private void installShortcuts() {

    // remove old
    panelShortcuts.removeAll();
    panelShortcuts.revalidate();
    
    // anything we can offer? need ascending sorted column and at least 10 rows
    Model model = (Model)table.getModel();
    if (model.getSortedColumn()<=0||model.getSortedColumn()>model.getColumnCount()||model.getRowCount()<10)
      return;
    
    // find a suitable non-null value representative
    int col = model.getSortedColumn()-1;
    for (int row=model.getRowCount()-1;row>0;row--) {
      Property prop = model.getPropertyAt(row, col);
      if (prop!=null) {
        ShortcutGenerator sg = getShortcutGenerator(prop);
        if (sg!=null) 
          sg.generate(col, model, panelShortcuts);
        break;
      }
    }

    // done
  }
  
  /**
   * Find applicable ShortcutGenerator for property representative
   */
  private ShortcutGenerator getShortcutGenerator(Property prop) {

    // type is key
    Class key = prop.getClass();
    while (key!=null) {
      ShortcutGenerator sg = (ShortcutGenerator)type2generator.get(key);
      if (sg!=null) 
        return sg;
      key = key.getSuperclass();
    }
    
    // not found
    return null;
  }

  /**
   * Table Content
   */
  private class Content extends JTable implements ContextProvider, ListSelectionListener  {
    
    /**
     * Constructor
     */
    private Content() {
      
      setDefaultRenderer(Object.class, new PropertyTableCellRenderer());
      getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setTableHeader(new SortableTableHeader());
      getColumnModel().setColumnSelectionAllowed(true);
      getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      
      setRowHeight((int)Math.ceil(Options.getInstance().getDefaultFont().getLineMetrics("", new FontRenderContext(null,false,false)).getHeight())+getRowMargin());
      
      getColumnModel().getSelectionModel().addListSelectionListener(this);
      getSelectionModel().addListSelectionListener(this);
    }
    
    /** 
     * ListSelectionListener - callback
     */
    public void valueChanged(ListSelectionEvent e) {
      
      // let super handle it (strange that JTable implements this as well)
      super.valueChanged(e);
      
      
      // propagate selection change?
      if (!e.getValueIsAdjusting()) {
        int row = getSelectedRow();
        int col = getSelectedColumn();
        if (row>=0&&col>=0) 
          viewManager.fireContextSelected(((Model)getModel()).getContextAt(row, col), this);
      }
      
      // done
    }
    
    /**
     * ContextProvider - callback 
     */
    public Context getContext() {
      
      Model model = (Model)getModel();
      
      // one row one col?
      int row = getSelectedRow();
      int col = getSelectedColumn();
      if (getSelectedRowCount()==1&&getSelectedColumnCount()==1)
        return model.getContextAt(row, col);
      
      // fallback
      Gedcom ged = model.getGedcom();
      return ged!=null ? new Context(ged) : null;
    }
  } //Content

  /**
   * The logical model
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
    
    private Gedcom getGedcom() {
      return model!=null ? model.getGedcom() : null;
    }
    
    public void handleContentChange(PropertyTableModel model) {
      
      // setup cell state
      int 
        rows = model.getNumRows(), 
        cols = model.getNumCols();
      cells = new Property[rows][cols];
      
      // sort again
      sort(Integer.MAX_VALUE);

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
      sort(Integer.MAX_VALUE);

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
      super.removeTableModelListener(l);
      // stop listening ?
      if (model!=null&&getListeners(TableModelListener.class).length==0)
        model.removeListener(this);
    }
    
    /**
     *  patched column name
     */
    public String getColumnName(int col) {
      return model!=null ? model.getPath(col).getName() : "";
    }
    
    /** num columns */
    public int getColumnCount() {
      return model!=null ? model.getNumCols() : 0;
    }
    
    /** num rows */
    public int getRowCount() {
      return model!=null ? row2row.length : 0;
    }
    
    /** path in column */
    private TagPath getPath(int col) {
      return model!=null ? model.getPath(col) : null;
    }
    
    /** context */
    private Context getContextAt(int row, int col) {
      // nothing to do?
      if (model==null)
        return null;
      // try to find property already cached
      Property prop = getPropertyAt(row, col);
      if (prop!=null)
        return new Context(prop);
      Property root = model.getProperty(row2row[row]);
      
      // we're very specific here - we don't want to get a context back thrown
      // at us that has property!=null if the user has selected an empty
      // cell in our view - property==null!!!
      return new Context(root.getGedcom(), root.getEntity(), null);
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
      int old = sortColumn;
      sortColumn = col;
      // sort
      sort(old);
      // tell about it
      fireTableDataChanged();
      // install shortcuts
      SwingUtilities.invokeLater(new Runnable() { 
        public void run() { installShortcuts(); }
      });
      // done
    }
    
    /** calculate where given property would have to be inserted for current sorted column*/
    private int getRowToInsert(Property shortcut) {
      // some safety checks
      if (model==null||sortColumn<=0||getRowCount()==0)
        return -1;
      
      // do a binary search
      int col = sortColumn-1;
      int from = 0, to = getRowCount()-1;
      while (from<to) {

        int pivot = (from+to)/2;
        
        Property prop = getPropertyAt(pivot, col);
        if (prop==null) {
          from = pivot+1;
          continue;
        }
        
        if (prop.compareTo(shortcut) >= 0) {
          to = pivot;
        } else {
          from = pivot + 1;
        }
        
        // next iteration
      }      
      
      // not found
      return from;
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
    private void sort(int old) {
      
      // no sorting necessary?
      if (sortColumn==0|| Math.abs(sortColumn)>getColumnCount() || row2row.length<2) 
        return;

      // a simple inversion (ascending into descending)?
      if (old==-sortColumn) {
        for (int i=0,j=row2row.length-1;i<j;)
          swap(i++, j--);
        return;
      }
      
      // run quicksort
      qsort(0,row2row.length-1);

    }
    
    private void qsort(int from, int to) {
      
      // choose pivot v in the middle (this will work better on already sorted rows that are
      // being resorted on content change) - move it out of the way
      int v = (from+to)/2;
      swap(from, v);
      v = from;
      
      // partition into < and > than v 
      int lo = from+1, hi = to;
      while (true) {
        
        for (;lo<=to&&compare(lo, v)<0;lo++);
        for (;hi>=from&&compare(hi, v)>0;hi--);

        if (lo>hi)
          break;

        swap(lo++, hi--);
      }
      
      // move pivot back in the middle
      swap(v, hi);

      // recurse into halfs
      if (lo<to) 
        qsort(lo,to);
      if (hi>from) 
        qsort(from, hi);
      
      // done
    }
    
    private void swap(int row1, int row2) {
      int swap = row2row[row1];
      row2row[row1] = row2row[row2];
      row2row[row2] = swap;
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
    
    private int RENDERER_PREFERENCE = PropertyRenderer.PREFER_DEFAULT & ~PropertyRenderer.PREFER_SHORT;
    
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
      return Dimension2d.getDimension(PropertyRenderer.get(curProp).getSize(getFont(), new FontRenderContext(null, false, false), curProp, RENDERER_PREFERENCE, viewManager.getDPI()));
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
      // add some space left and right
      bounds.x += 1;
      bounds.width -= 2;
      // let it render
      proxy.render(graphics, bounds, curProp, RENDERER_PREFERENCE, viewManager.getDPI());
      // done
    }
    
  } //PropertyTableCellRenderer
  
  
  /**
   * A shortcut to jump to
   */
  private class Shortcut extends LinkWidget {
    private Property prop;
    private Shortcut(Property prop) {
      super(prop.getDisplayValue(), null);
      this.prop = prop;
      setHorizontalAlignment(SwingConstants.CENTER);
      setBorder(new EmptyBorder(0,1,0,1));
    }
    protected void fireActionPerformed() {
      // ask model
      Model model = (Model)table.getModel();
      int row = model.getRowToInsert(prop);
      if (row<0)
        return;
      int col = Math.abs(model.sortColumn)-1;
      // 20050518 removed the selection change 
      // scroll to visible
      Rectangle rect = table.getCellRect(row,col,true);
      rect.height = table.getParent().getHeight();
      table.scrollRectToVisible(rect);
      // done
    }
  } //Letter

  /**
   * A generator for shortcuts to rows in ascending order
   */
  private abstract class ShortcutGenerator {

    abstract void generate(int col, Model model, Container container);
    
    protected void add(Property shortcut, Container container) {
      container.add(new Shortcut(shortcut));
    }
  }
  
  /**
   * A generator for shortcuts to names
   */
  private class NameSG extends ShortcutGenerator {

    /** collect first letters of names */
    Set getLetters(Model model) {
      
      // check first letter of lastnames
      Set letters = new TreeSet();
      for (Iterator names = PropertyName.getLastNames(model.model.getGedcom(), false).iterator(); names.hasNext(); ) {
        String name = names.next().toString();
        if (name.length()>0) {
          char c = name.charAt(0);
          if (Character.isLetter(c))
            letters.add(String.valueOf(Character.toUpperCase(c)));
        }
      }
      // done
      return letters;
    }
    
    /** generate */
    void generate(int col, Model model, Container container) {

      // create a shortcut for each letter
      Set letters = getLetters(model);
      for (Iterator it = letters.iterator(); it.hasNext(); ) 
        add(new PropertyName("", it.next().toString()), container);
      
      // done
    }
  } //NameShortcutGenerator
  
  /**
   * A generator for shortcuts to simple text values
   */
  private class ValueSG extends ShortcutGenerator {

    /** collect first letters of values */
    Set getLetters(int col, Model model) {
      
      // check first letter of lastnames
      Set letters = new TreeSet();
      int rows = model.getRowCount();
      for (int row=0;row<rows;row++) {
        Property prop = model.getPropertyAt(row, col);
        if (prop!=null) {
          String value = prop.getDisplayValue();
          if (value.length()>0) {
            char c = value.charAt(0);
            if (Character.isLetter(c))
              letters.add(String.valueOf(Character.toUpperCase(c)));
          }
        }
      }
      // done
      return letters;
    }
    
    /** generate */
    void generate(int col, Model model, Container container) {

      // create a shortcut for each letter
      Set letters = getLetters(col, model);
      for (Iterator it = letters.iterator(); it.hasNext(); ) 
        add(new PropertySimpleValue("", it.next().toString()), container);
      
      // done
    }
  } //SimpleShortcutGenerator
  
  /**
   * A generator for shortcuts to years
   */
  private class DateSG extends ShortcutGenerator {
    /** generate */
    void generate(int col, Model model, Container container) {
      
      // how many text lines fit on screen?
      int visibleRows = Math.max(24, container.getHeight() / new LinkWidget("9999",null).getPreferredSize().height);
      int rows = model.getRowCount();
      if (rows>visibleRows) try {
        
        // find all applicable years
        Set years = new TreeSet();
        for (int row=0;row<rows;row++) {
          PropertyDate date = (PropertyDate)model.getPropertyAt(row, col);
          if (date==null || !date.getStart().isValid())
            continue;
          years.add(new Integer(date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear()));
        }
        
        // generate shortcuts for all years
        Object[] ys = years.toArray();
        for (int y=0; y<visibleRows; y++) {
          int index = y<visibleRows-1  ?   (int)( y * ys.length / visibleRows)  : ys.length-1;
          int year = ((Integer)ys[index]).intValue();
          add(new PropertyDate(year), container);
        }

      } catch (Throwable t) {
      }
      // done
    }
  } //DateShortcutGenerator
  
} //PropertyTableWidget
