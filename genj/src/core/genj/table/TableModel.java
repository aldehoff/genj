package genj.table;

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.EntityList;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Selection;
import genj.gedcom.TagPath;
import genj.util.ImgIcon;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Our model of entites shown in a table
 */
/*package*/ class EntityTableModel extends AbstractTableModel implements GedcomListener {
  
  /** the gedcom we're looking at */
  private Gedcom gedcom;
  
  /** the modes we know about */  
  private Filter[] filters = new Filter[]{
      new Filter(Gedcom.INDIVIDUALS , new String[]{"INDI","INDI:NAME","INDI:SEX","INDI:BIRT:DATE","INDI:BIRT:PLAC","INDI:FAMS", "INDI:FAMC", "INDI:OBJE:FILE"}),
      new Filter(Gedcom.FAMILIES    , new String[]{"FAM","FAM:MARR:DATE","FAM:MARR:PLAC", "FAM:HUSB", "FAM:WIFE", "FAM:CHIL" }),
      new Filter(Gedcom.MULTIMEDIAS , new String[]{"OBJE","OBJE:FILE"}),
      new Filter(Gedcom.NOTES       , new String[]{"NOTE"}),
      new Filter(Gedcom.SOURCES     , new String[]{"SOUR"}),
      new Filter(Gedcom.SUBMITTERS  , new String[]{"SUBM"}),
      new Filter(Gedcom.REPOSITORIES, new String[]{"REPO"})
    };
  
  /** the current mode */
  private Filter filter = filters[0];
  
  /**
   * Constructor
   */
  /*pacakge*/ EntityTableModel(Gedcom gedcom) {
    this.gedcom=gedcom;
    gedcom.addListener(this);
  }

  /**
   * Get paths for given type
   */
  /*package*/ TagPath[] getPaths(int type) {
    return filters[type].paths;
  }
  
  /**
   * Sets paths for given type
   */
  /*package*/ void setPaths(int type, TagPath[] paths) {
    filters[type].paths = paths;
    fireTableStructureChanged();
  }
  
  /**
   * Sets withs for given type
   */
  /*package*/ void setWidths(int type, int[] widths) {
    filters[type].widths = widths;
  }
  
  /**
   * Sets the entity type we're looking at
   */
  /*package*/ void setType(int entity) {
    // remember
    filter = filters[entity];
    // propagate
    fireTableStructureChanged();
  }

  /**
   * Returns the entity type we're looking at
   */
  /*package*/ int getType() {
    return filter.type;
  }
  
  /**
   * Returns the entity at given row index
   */
  /*package*/ Entity getEntity(int row) {
    return gedcom.getEntities(filter.type).get(row);
  }
  
  /**
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount() {
    return filter.paths.length;
  }

  /**
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount() {
    return gedcom.getEntities(filter.type).getSize();
  }

  /**
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    Entity e = gedcom.getEntities(filter.type).get(rowIndex);
    filter.paths[columnIndex].setToFirst();
    return e.getProperty().getProperty(filter.paths[columnIndex], false);
  }

  /**
   * @see genj.gedcom.GedcomListener#handleChange(Change)
   */
  public void handleChange(Change change) {
    //if (change.isChanged(change.EADD)||change.isChanged(change.EDEL))
    fireTableDataChanged();
  }

  /**
   * @see genj.gedcom.GedcomListener#handleClose(Gedcom)
   */
  public void handleClose(Gedcom which) {
    // ignored
  }

  /**
   * @see genj.gedcom.GedcomListener#handleSelection(Selection)
   */
  public void handleSelection(Selection selection) {
    // ignored
  }

  /**
   * Helper that creates a new ColumnModel
   */
  /*package*/ TableColumnModel createTableColumnModel() {
    // create and fill
    TableColumnModel columns = new DefaultTableColumnModel();
    for (int c=0; c<filter.paths.length; c++) {
      TableColumn col = new TableColumn(c);
      col.setHeaderValue(filter.paths[c]);
      col.setPreferredWidth(filter.widths[c]);
      columns.addColumn(col);
    }
    // done
    return columns;
  }

  
  /**
   * A Filter filters the entities we have in the model
   */
  private class Filter {
    /** attributes */
    ImgIcon image;
    int type;
    String[] defaults;
    TagPath[] paths;
    int[] widths;
    /** constructor */
    Filter(int t, String[] d) {
      // remember
      type     = t;
      defaults = d;
      // init
      setPaths(defaults, null);
    }
    /** set paths to use */
    void setPaths(Object[] ps, int[] ws) {
      // build paths
      paths = new TagPath[ps.length];
      widths= new int[ps.length];
      for (int p=0;p<ps.length;p++) {
        paths [p] = new TagPath(ps[p].toString());
        widths[p] = ws != null ? ws[p] : 640/ps.length;
      }
      // done
    }
  } //Filter

} //TableModel
