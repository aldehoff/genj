package genj.app;

import genj.gedcom.Change;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Selection;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.util.swing.ImgIconConverter;

import java.awt.Component;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class GedcomTable extends JTable {
  
  /** default column headers */
  private static final Object headers[] = {
    App.resources.getString("cc.column_header.name"),
    ImgIconConverter.get(Gedcom.getImage(Gedcom.INDIVIDUALS)),
    ImgIconConverter.get(Gedcom.getImage(Gedcom.FAMILIES)),
    ImgIconConverter.get(Gedcom.getImage(Gedcom.MULTIMEDIAS)),
    ImgIconConverter.get(Gedcom.getImage(Gedcom.NOTES)),
    ImgIconConverter.get(Gedcom.getImage(Gedcom.SOURCES)),
    ImgIconConverter.get(Gedcom.getImage(Gedcom.SUBMITTERS))
  };

  /** default column widths */
  private static final int defaultWidths[] = {
    48, 24, 24, 24, 24, 24, 24
  };

  /** a registry */
  private Registry registry;
  
  /** a model */
  private Model model;
  
  /**
   * Constructor
   */
  public GedcomTable() {
    
    // Prepare a model
    model = new Model();
    
    // Prepare a column model
    TableColumnModel cm = new DefaultTableColumnModel();
    getTableHeader().setDefaultRenderer(new HeaderCellRenderer());
    
    for (int h=0; h<headers.length; h++) {
      TableColumn col = new TableColumn(h);
      col.setHeaderValue(headers[h]);
      col.setPreferredWidth(defaultWidths[h]);
      cm.addColumn(col);
    }
    setModel(model);
    setColumnModel(cm);

    // change looks    
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    // done
  }
  
  /**
   * Tells us where to look for stored information
   */
  public void setRegistry(Registry registry) {
    // remember
    this.registry=registry;
    // grab the preferred columns
    int[] widths = registry.get("columns",(int[])null);
    for (int c=0; c<widths.length; c++) {
      getColumnModel().getColumn(c).setPreferredWidth(widths[c]);
    }    
    // done
  }
  
  /**
   * Hooking into the tear-down process to store our
   * settings in (set) registry
   */
  public void removeNotify() {
    // remember our layout
    if (registry!=null) {
      int[] widths = new int[headers.length];
      for (int c=0; c<widths.length; c++) {
        widths[c] = getColumnModel().getColumn(c).getWidth();
      }
      registry.put("columns", widths);
    }
    // continue
    super.removeNotify();
  }
  
  /**
   * Accessor for model
   */
  public Vector getAllGedcoms() {
    return model.getAllGedcoms();
  }
  
  /**
   * The selected gedcom
   */
  public Gedcom getSelectedGedcom() {
    return model.getSelectedGedcom();
  }
  
  /**
   * Add a gedcom
   */
  public void addGedcom(Gedcom gedcom) {
    model.addGedcom(gedcom);
  }

  /**
   * A model keeping track of a bunch of Gedcoms
   */
  public class Model implements TableModel, GedcomListener {
    
    /** the Gedcoms we know about */
    private Vector gedcoms = new Vector(10);
    
    /** the listeners */
    private Vector listeners = new Vector(2);
    
    /**
     * Selected Gedcom
     */
    public Gedcom getSelectedGedcom() {
      int row = getSelectedRow();
      if (row==-1) return null;
      return (Gedcom)gedcoms.elementAt(row);
    }
  
    /**
     * All Gedcoms
     */
    public Vector getAllGedcoms() {
      return gedcoms;
    }
  
    /**
     * Add a gedcom
     */
    public void addGedcom(Gedcom gedcom) {
      gedcoms.add(gedcom);
      gedcom.addListener(this);
      fireTableChange(-1);
    }

    /**
     * @see javax.swing.table.TableModel#addTableModelListener(TableModelListener)
     */
    public void addTableModelListener(TableModelListener l) {
      listeners.add(l);
    }
  
    /**
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    public Class getColumnClass(int columnIndex) {
      return String.class;
    }
  
    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
      return headers.length;
    }
  
    /**
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    public String getColumnName(int columnIndex) {
      return ""+columnIndex;
    }
  
    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
      return gedcoms.size();
    }
  
    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
      Gedcom gedcom = (Gedcom)gedcoms.elementAt(row);
      if (col==0) return gedcom.getName();
      return ""+gedcom.getEntities(Gedcom.FIRST_ETYPE+(col-1)).getSize();
    }
  
    /**
     * @see javax.swing.table.TableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }
  
    /**
     * @see javax.swing.table.TableModel#removeTableModelListener(TableModelListener)
     */
    public void removeTableModelListener(TableModelListener l) {
      listeners.remove(l);
    }
  
    /**
     * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      // ignored
    }
    
    /**
     * notification that num
     */
    public void fireTableChange(int row) {
      TableModelEvent ev = (row<0 ? new TableModelEvent(this) : new TableModelEvent(this,row) );
      Enumeration e = listeners.elements();
      while (e.hasMoreElements()) {
        ((TableModelListener)e.nextElement()).tableChanged(ev);
      }
    }
  
    /**
     * @see genj.gedcom.GedcomListener#handleChange(Change)
     */
    public void handleChange(Change change) {
      if (change.isChanged(change.EADD)||change.isChanged(change.EDEL)) {
        Gedcom gedcom = change.getGedcom();
        for (int g=0;g<gedcoms.size(); g++) {
          if (gedcoms.elementAt(g)==gedcom) {
            fireTableChange(g);
            break;
          }
        }
      }
    }

    /**
     * @see genj.gedcom.GedcomListener#handleClose(Gedcom)
     */
    public void handleClose(Gedcom which) {
      gedcoms.remove(which);
      fireTableChange(-1);
    }

    /**
     * @see genj.gedcom.GedcomListener#handleSelection(Selection)
     */
    public void handleSelection(Selection selection) {
    }

  } // Model

  /**
   * Our own TableCellRenderer for the header - because the
   * default one doesn't handle images :(
   */
  private class HeaderCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
                       boolean isSelected, boolean hasFocus, int row, int column) {
      if (table != null) {
        JTableHeader header = table.getTableHeader();
        if (header != null) {
          setForeground(header.getForeground());
          setBackground(header.getBackground());
          setFont(header.getFont());
        }
      }
      setHorizontalAlignment(CENTER);
      if (value instanceof ImageIcon) {
        setIcon((ImageIcon)value);
        value=null;
      } else {
        setIcon(null);
      }
      setText((value == null) ? "" : value.toString());
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      return this;
    }
  } // HeaderCellRenderer

}
