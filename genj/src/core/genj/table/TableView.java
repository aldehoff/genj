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
package genj.table;

import java.awt.*;
import java.awt.event.*;

import awtx.*;
import awtx.table.*;

import genj.util.*;
import genj.gedcom.*;

/**
 * Component for showing entities of a gedcom file in a tabular way
 * NM 19990722 Change extends from Container to awtx.Table
 */
public class TableView extends Table {

  /** members */
  /*package*/ Frame                  frame;
  /*package*/ Gedcom                 gedcom;
  /*package*/ Registry               registry;
  /*package*/ CellRenderer           eHeaderRenderer = new EHeaderRenderer();
  /*package*/ boolean                silent = false;

  /*package*/ final static Resources resources = new Resources("genj.table");

  private static final String defaultPaths [][] = {
    {"INDI","INDI:NAME","INDI:SEX","INDI:BIRT:DATE","INDI:BIRT:PLAC","INDI:FAMS", "INDI:FAMC", "INDI:OBJE:FILE"},
    {"FAM","FAM:MARR:DATE","FAM:MARR:PLAC", "FAM:HUSB", "FAM:WIFE", "FAM:CHIL" },
    {"OBJE","OBJE:BLOB"},
    {"NOTE"},
    {"SOUR"},
    {"SUBM"},
    {"REPO"}
  };

  private TypeView typeViews[];

  private ETableModel model;

  private final static Object[][] views = {
    { Property.getDefaultImage("indi").getImage(), "indis"   ,new Integer(Gedcom.INDIVIDUALS) },
    { Property.getDefaultImage("fam" ).getImage(), "fams"    ,new Integer(Gedcom.FAMILIES   ) },
    { Property.getDefaultImage("obje").getImage(), "medias"  ,new Integer(Gedcom.MULTIMEDIAS) },
    { Property.getDefaultImage("note").getImage(), "notes"   ,new Integer(Gedcom.NOTES      ) },
    { Property.getDefaultImage("sour").getImage(), "sources" ,null                            },
    { Property.getDefaultImage("subm").getImage(), "subms"   ,null                            },
    { Property.getDefaultImage("repo").getImage(), "repos"   ,null                            },
  };

  /**
   * Class for representing table model of entities
   */
  class ETableModel extends AbstractTableModel implements GedcomListener {

    // LCD

    /** members */
    private EntityList entities = new EntityList();
    private int type;

    /**
     * Constructor
     */
    ETableModel(int type) {
      this.type = type;
      entities = gedcom.getEntities()[type];
    }

    /**
     * Adds a listener to this model
     */
    public void addTableModelListener(TableModelListener listener) {
      super.addTableModelListener(listener);
      // (Re)connect to Gedcom if unneeded
      if (listeners.size()==1) {
        gedcom.addListener(this);
        fireNumRowsChanged();
      }
    }

    /**
     * Removes a listener from this model
     */
    public void removeTableModelListener(TableModelListener listener) {
      super.removeTableModelListener(listener);
      // Disconnect from Gedcom if unneeded
      if (listeners.size()==0) {
        gedcom.removeListener(this);
      }
    }

    /**
     * Comparison between rows
     */
    public int compareRows(int rowa,int rowb, int column) {
      Property a = (Property)getObjectAt(rowa,column);
      Property b = (Property)getObjectAt(rowb,column);
      if ((a==null)&&(b==null)) {
        return 0;
      }
      if (a==null) {
        return -1;
      }
      if (b==null) {
        return 1;
      }
      return a.compareTo(b);
    }

    /**
     * Returns type of entities in this model
     */
    public int getType() {
      return type;
    }

    /**
     * Returns number of Columns
     */
    public int getNumColumns() {
      return typeViews[type].columns.length;
    }

    /**
     * Returns number of Rows - that's the number of indis.
     */
    public int getNumRows() {
      return entities.getSize();
    }

    /**
     * Returns the Header Object col - that's one of the columns
     */
    public Object getHeaderAt(int col) {
      return typeViews[type].columns[col];
    }

    /**
     * Returns the Cell Object row,col - that's one of the indis.
     */
    public Object getObjectAt(int row, int col) {

      // Calculate Entity
      Entity e = entities.get(row);
      Column c = typeViews[type].columns[col];

      c.tagPath.setToFirst();
      Property p = e.getProperty().getProperty(c.tagPath,true);

      // Done
      return p;
    }

    /**
     * Returns an entity at given row
     */
    public Entity getEntityAt(int row) {
      return entities.get(row);
    }

    /**
     * Notification that a change in a Gedcom-object took place.
     */
    public void handleChange(final Change change) {

      // Added/Deleted entities ?
      if (change.isChanged(Change.EADD) || change.isChanged(Change.EDEL)) {
        fireNumRowsChanged();
        return;
      }

      // Added/Deleted/Modified properties ?
      if ( (change.isChanged(Change.PADD))
       ||(change.isChanged(Change.PDEL))
       ||(change.isChanged(Change.PMOD)) )  {

        fireRowsChanged(new int[0]);

        return;
      }

      // Done with Changes
    }

    /**
     * Notification that an entity has been selected.
     */
    public void handleSelection(Selection selection) {

      // ignore it?
      if (silent) {
        return;
      }

      // Check if this information is important to us
      Entity which = selection.getEntity();
      if (which.getType()!=type)
        return;

      // Try to find entity and change selection
      Entity entity;
      for (int i=0;i<entities.getSize();i++) {
        entity = entities.get(i);
        if (entity==which) {
          silent = true;
          setSelectedRow(i);
          silent = false;
          break;
        }
      }
    }

    /**
     * Notification that the gedcom is being closed
     */
    public void handleClose(Gedcom which) {
    }

    // EOC
  }

  /**
   * Class for encapsulating a typeview
   */
  private static class TypeView {
    Column[] columns;
    int sortedColumn;
    int sortedDir;
  }

  /**
   * Class for handling a single column
   */
  private static class Column {

    TagPath           tagPath;
    int               width;
    CellRenderer      renderer;

    /**
     * Constructor
     */
    /*package*/ Column(TagPath tagPath,int width) {

      this.tagPath  = tagPath;
      this.width    = width;

      // Create proxy
      String me = getClass().getName(),
      pkg       = me.substring( 0, me.lastIndexOf(".") + 1 ),
      howto     = "Proxy" + Property.calcDefaultProxy(tagPath);

      try {
        renderer = (CellRenderer) Class.forName( pkg + howto ).newInstance();
      } catch (Exception e) {
        renderer = new ProxyUnknown();
      }

      // Done
    }

    // EOC
  }

  /**
   * New implementation of CellRenderer
   */
  public class EHeaderRenderer extends DefaultHeaderCellRenderer {

    /**
     * Render the header
     */
    public void render(Graphics g, Rectangle rect, Object o, FontMetrics fm) {

      Column c = (Column)o;

      // Enough space?
      String s = c.tagPath.toString();

      if (fm.stringWidth(s)+8>rect.width) {
        o = c.tagPath.getLast();
      } else {
        o = s;
      }

      // Delegate
      super.render(g,rect,o,fm);
    }

    // EOC
  }

  /**
   * Constructor
   * @param pGedcom the Gedcom to use
   * @param pRegistry the Registry to get parameters from
   * @param pFrame the frame (can be null) this components resides in
   */
  public TableView(Gedcom pGedcom, Registry pRegistry, Frame pFrame) {

    // Remember
    gedcom   = pGedcom;
    registry = pRegistry;
    frame    = pFrame;

    // Some parameters
    setSortable(true);

    // Prepare buttons in left edge for view switching
    ActionListener alistener = new ActionListener() {
      // LCD
      /** action notification */
      public void actionPerformed(ActionEvent ae) {
        setType(Integer.parseInt(ae.getActionCommand()));
      }
      // EOC
    };

    for (int i=0;i<views.length;i++) {

      Image  img = (Image) views[i][0];
      String key = (String)views[i][1];
      String act = ""+     views[i][2];

      Component c = ComponentProvider.createButton(
        img,
        resources.getString("corner."+key+".text"),
        resources.getString("corner."+key+".tip" ),
        act,
        alistener,
        ComponentProvider.IMAGE_ONLY
      );
      c.setEnabled(views[i][2]!=null);
      add2Edge(c);
    }

    // Restore last state
    try {
      setType(loadParameters(false));
    } catch (Exception e) {
      setType(loadParameters(true));
    }

    // Listening
    awtx.table.TableListener tlistener = new awtx.table.TableListener() {
      // LCD
      /** row selection notification */
      public void rowSelectionChanged(int[] rows) {
        if ((rows.length==0)||(silent)) {
          return;
        }
        silent=true;
        gedcom.fireEntitySelected(null,model.getEntityAt(rows[0]),false);
        silent=false;
        // Done
      }
      /** double click/enter notification */
      public void actionPerformed(int row) {
        gedcom.fireEntitySelected(null,model.getEntityAt(row),true);
      }
      // EOC
    };
    addTableListener(tlistener);

    // Done
  }

  /**
   * Returns number of Columns
   */
  public int getNumColumnsFor(int eType) {
    if (model==null) {
      return 0;
    }
    return typeViews[eType].columns.length;
  }

  /**
   * Returns number of Rows - that's the number of indis.
   */
  public int getNumRows() {
    if (model==null) {
      return 0;
    }
    return model.getNumRows();
  }

  /**
   * Returns all TagPaths for given etype
   */
  public TagPath[] getTagPathsFor(int eType) {

    // Valid model?
    if (model==null) {
      return new TagPath[0];
    }

    // # of TagPaths = # of Columns
    Column[] columns = typeViews[eType].columns;

    TagPath[] result = new TagPath[columns.length];
    for (int c=0;c<result.length;c++) {
      result[c] = columns[c].tagPath;
    }

    // Done
    return result;

  }

  /**
   * Return type of view which is one of the Gedcom.ENTITIES constants
   */
  public int getType() {
    return model.getType();
  }

  /**
   * Method for identifying MouseDoubleClicks
   */
  protected boolean isDoubleClick(MouseEvent e) {
    return super.isDoubleClick(e) || e.isShiftDown();
  }

  /**
   * Helper that loads pararmeters from registry
   */
  private int loadParameters(boolean useDefaults) {

    // Prepare an array of TypeViews
    typeViews = new TypeView[Gedcom.LAST_ETYPE-Gedcom.FIRST_ETYPE+1];

    // .. loop through 'em
    for (int t=Gedcom.FIRST_ETYPE;t<=Gedcom.LAST_ETYPE && t<defaultPaths.length ;t++) {

      // .. create view for type
      typeViews[t] = new TypeView();

      // .. try to get from registry
      int   [] widths;
      String[] paths ;

      String pre =  Gedcom.getPrefixFor(t);

      if (useDefaults) {
        widths = new int[0];
        paths  = defaultPaths[t];
      } else {
        widths = registry.get(pre+".width",new int   [0] );
        paths  = registry.get(pre+".path" ,defaultPaths[t]);
      }

      typeViews[t].sortedColumn = registry.get(pre+".scol",-1);
      typeViews[t].sortedDir    = registry.get(pre+".sdir",-1);

      // .. create space for columns
      Column[] columns = new Column[paths.length];
      typeViews[t].columns = columns;

      // .. create columns
      for (int c=0;c<paths.length;c++) {
        columns[c] = new Column(
          new TagPath(paths[c]),
          (c>=widths.length?50:widths[c])
        );
      }

      // .. next type
    }

    // Done
    return registry.get("view",Gedcom.INDIVIDUALS);
  }

  /**
   * Notification when table isn't used anymore
   */
  public void removeNotify() {

    // Save parameters
    saveParameters();

    // Done
    super.removeNotify();
  }

  /**
   * Helper that saves parameters to registry
   */
  private void saveParameters() {

    setType(-1);

    // Loop through TypeViews
    for (int t=0;t<typeViews.length;t++) {

      // .. get paths&widths
      Column[] columns = typeViews[t].columns;

      String[] paths = new String[columns.length];
      int   [] widths= new int   [columns.length];

      for (int c=0;c<paths.length;c++) {

        Column col = columns[c];

        paths[c] = col.tagPath.toString();
        widths[c]= col.width;

      }

      // .. save
      String pre = Gedcom.getPrefixFor(t);
      registry.put(pre+".path" ,paths );
      registry.put(pre+".width",widths);

      registry.put(pre+".scol",typeViews[t].sortedColumn);
      registry.put(pre+".sdir",typeViews[t].sortedDir);

      // .. next column
    }

    // .. save state
    registry.put("view",model.getType());
    registry.put("scol",getSortedColumn());
    registry.put("sdir",getSortedDir());

    // Done
  }

  /**
   * Sets TagPaths to be used for columns for given eType
   */
  public void setTagPaths(int type, TagPath[] paths) {

    // Prepare/Get new and old column definitions
    Column[] newColumns = new Column[paths.length];
    Column[] oldColumns = typeViews[type].columns;

    // .. Loop through new Columns
    for (int c=0;c<newColumns.length;c++) {

      int w = 50;

      // .. try to find an old width
      for (int o=0;o<oldColumns.length;o++) {
        if (oldColumns[o].tagPath.equals(paths[c])) {
          w = oldColumns[o].width;
          break;
        }
      }

      // .. build new column
      newColumns[c] = new Column(
        paths[c],
        w
      );

      // .. Continue
    }

    // Remember columns
    typeViews[type].columns = newColumns;

    // Show change
    if (model.getType()==type) {
      model = null;
      setType(type);
    }

    // Done
  }

  /**
   * Switch view to type of entity
   */
  public void setType(int type) {

    if (type>Gedcom.LAST_ETYPE) {
      type=Gedcom.FIRST_ETYPE;
    }

    // Valid model to remember parameters for?
    if (model!=null) {

      // .. maybe already there?
      int t = model.getType();
      if (t==type) {
        return;
      }

      // .. Remember column widths
      Column[] columns = typeViews[t].columns;

      int ws[] = getColumnWidths();
      for (int c=0;c<ws.length && c<columns.length;c++) {
        columns[c].width=ws[c];
      }

      // .. And sorting parameters
      typeViews[t].sortedColumn = getSortedColumn();
      typeViews[t].sortedDir    = getSortedDir();

      // .. continue
    }

    // No Model?
    if (type==-1) {
      setModel(null);
      return;
    }

    // Create TableModel
    model = new ETableModel(type);
    setModel(model);

    // Set Renderers for columns
    Column columns[] = typeViews[type].columns;

    int[] widths = new int[columns.length];
    CellRenderer[] crenderers = new CellRenderer[widths.length];
    CellRenderer[] hrenderers = new CellRenderer[widths.length];

    for (int i=0;i<columns.length;i++) {

      Column col = columns[i];

      widths[i] = col.width;
      crenderers[i] = col.renderer;
      hrenderers[i] = eHeaderRenderer;

    }

    setCellRenderers(crenderers);
    setHeaderCellRenderers(hrenderers);
    setColumnWidths (widths);

    setSortedColumn(
      typeViews[type].sortedColumn,
      typeViews[type].sortedDir
    );

    // Done
  }

}
