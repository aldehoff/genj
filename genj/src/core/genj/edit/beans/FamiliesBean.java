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
package genj.edit.beans;

import genj.common.PropertyTableModel;
import genj.common.PropertyTableWidget;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.Registry;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * A complex bean displaying families of an individual
 */
public class FamiliesBean extends PropertyBean {

  /** indi we're looking at */
  private Indi indi;
  
  /**
   * Finish editing a property through proxy
   */
  public void commit(Transaction tx) {
  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, TagPath setPath, ViewManager setMgr, Registry setReg) {
    super.init(setGedcom, setProp, setPath, setMgr, setReg);

    // we assume we got an indi here
    indi = (Indi)setProp;
    
    // setup layout
    setLayout(new BorderLayout());

    // a label at the top
    add(BorderLayout.NORTH, new JLabel(Gedcom.getName("FAM")));

    // a table for the families
    
    
//    TableAndHeaderModel model = new Model();
//    JTable table = new JTable(model, new TableHeaderColumnModel(model));
//    JScrollPane scroll = new JScrollPane(table);
//    scroll.setPreferredSize(new Dimension(64,64));
    
//    TableView view = new TableView("foo", setGedcom, new Registry(), setMgr);

    PropertyTableModel model = new PropertyTableModel() {
      public int getNumCols() {
        return 4;
      }
      public int getNumRows() {
        return indi.getNoOfFams();
      }
      public TagPath getPath(int col) {
        switch (col) {
          default:
		    	case 0:
		    	  return new TagPath("FAM");
		    	case 1:
		    	  return new TagPath("FAM:HUSB:*"); // FIXME can be wife as well
		    	case 2:
		    	  return Fam.PATH_FAMMARRDATE;
		    	case 3:
		    	  return Fam.PATH_FAMMARRPLAC;
        }
      }
      public Property getProperty(int row) {
        return indi.getFam(row);
      }
    };
    PropertyTableWidget table = new PropertyTableWidget(model, viewManager);
    table.setPreferredSize(new Dimension(64,64));
    add(BorderLayout.CENTER, table);
    
    // done
  }

  /**
   * Our table model for a list of families
   */
  private class Model extends AbstractTableModel implements TableAndHeaderModel {
    
    private int rows;
    
    private Model() {
      rows = indi.getNoOfFams();
    }
      
    public int getColumnCount() {
      return 4;
    }
    
    public int getRowCount() {
      return rows;
    }
    
    public Object getHeaderValue(int col) {

      switch (col) {
        default:
        case 0:
    	    return "";
    	  case 1:
          return "Spouse";
        case 2:
          return "Marriage";
        case 3:
          return "Place";
      }

    }
    
    public Object getValueAt(int row, int col) {

      Fam fam = indi.getFam(row);

      switch (col) {
        default:
      	case 0:
      	  return fam.getId();
      	case 1:
      	  return fam.getOtherSpouse(indi);
      	case 2:
      	  return fam.getProperty(Fam.PATH_FAMMARRDATE);
      	case 3:
      	  return fam.getProperty(Fam.PATH_FAMMARRPLAC);
      }
      
    }
  } //Model
  
  /**
   * An enhanced TableModel that knows about header values
   */
  public interface TableAndHeaderModel extends TableModel {
    public Object getHeaderValue(int col);
  }
  
  /**
   * An enhanced TableColumnModel that knows how to derive columns
   * from a TableAndHeaderModel automatically
   */
  public class TableHeaderColumnModel extends DefaultTableColumnModel {
    
    /** the table model we're looking at */
    private TableAndHeaderModel model;
    
    /**
     * Constructor
     */
    public TableHeaderColumnModel(TableAndHeaderModel model) {

      this.model = model;
      
      setupColumns();
      
      model.addTableModelListener(new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
          setupColumns();
        }
      });
      
    }
    
    /**
     * Setup columns from model
     */
    private void setupColumns() {
      
      // remove all columns
      while (getColumnCount()>0)
        removeColumn(getColumn(0));
      
      // add appropriate columns
	    for (int c=0;c<model.getColumnCount();c++) {
	      TableColumn col = new TableColumn(c);
	      Object val = model.getHeaderValue(c);
	      col.setHeaderValue(val);
	      addColumn(col);
	    }
    }    
    
  } //TableHeaderColumnModel

} //FamiliesBean
