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
package genj.app;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import genj.*;
import genj.gedcom.*;
import genj.util.*;
import awtx.table.*;
import awtx.Table;

/**
 * A component showing Gedcoms
 */
public class GedcomTable extends Table {

  /** members */
  private GTableModel model;

  /** default column headers */
  private static final Object defaultHeaders[] = {
    App.resources.getString("cc.column_header.name"),
    Gedcom.getImage(Gedcom.INDIVIDUALS),
    Gedcom.getImage(Gedcom.FAMILIES),
    Gedcom.getImage(Gedcom.MULTIMEDIAS),
    Gedcom.getImage(Gedcom.NOTES),
    Gedcom.getImage(Gedcom.SOURCES),
    Gedcom.getImage(Gedcom.SUBMITTERS)
  };

  /** default column widths */
  private static final int defaultWidths[] = {
    48,
    24,
    24,
    24,
    24,
    24,
    24
  };

  /**
   * Our header renderer
   */
  class GHeaderRenderer extends DefaultHeaderCellRenderer {
    // LCD
    /** rendering */
    public void render(Graphics g, Rectangle rect, Object o, FontMetrics fm) {
      // Text?
      String text;
      if (o instanceof String) {
        text = (String)o;
      } else {
        text = "";
      }
      super.render(g,rect,text,fm);

      // Image?
      if (o instanceof ImgIcon) {
        // .. draw image
        g.drawImage(
          ((ImgIcon)o).getImage(),
          rect.x+4,
          rect.y,
          null
        );
      }
      // Done
    }
    // EOC
  }


  /**
   * Class for encapsulating all Gedcoms in a model for Table
   */
  class GTableModel extends AbstractTableModel implements  GedcomListener {
    // LCD
    /** member */
    private Vector gedcoms = new Vector();
    /** constructor */
    GTableModel() {
    }
    /** constructor */
    GTableModel(Vector pGedcoms) {
      // Register as Listener
      Gedcom gedcom;
      for (int i=0;i<pGedcoms.size();i++) {
        gedcom = (Gedcom)pGedcoms.elementAt(i);
        gedcom.addListener(this);
        gedcoms.addElement(gedcom);
      }
    }
    /** returns number of Rows - that's the number of gedcoms */
    public int getNumRows() {
      return gedcoms.size();
    }
    /** returns number of Columns for initialization */
    public int getNumColumns() {
      return defaultHeaders.length;
    }
    /** compares two given rows */
    public int compareRows(int first, int second, int column) {
      return 0;
    }
    /** returns the Cell Object row,col - that's one of the gedcoms */
    public Object getObjectAt(int row, int col) {
      Gedcom gedcom = (Gedcom)gedcoms.elementAt(row);
      String txt=null;

      switch (col) {
      case 0 :
        txt   = gedcom.getName();
        break;
      case 1 :
      case 2 :
      case 3 :
      case 4:
        txt   = ""+gedcom.getEntities(Gedcom.FIRST_ETYPE+col-1).getSize();
        break;
      case 5 :
        txt   = "0";
        break;
      case 6 :
        txt   = "0";
        break;
      }
      return txt;
    }
    /** returns the Header Object col - that's one of the headline images  */
    public Object getHeaderAt(int col) {
      return defaultHeaders[col];
    }
    /** callback that a change in a Gedcom-object took place. */
    public void handleChange(Change change) {
      if (  (!change.isChanged(Change.EADD))
        &&(!change.isChanged(Change.EDEL)) ) {
        return;
      }
      for (int i=0;i<gedcoms.size();i++) {
        if (gedcoms.elementAt(i)==change.getGedcom()) {
          fireRowsChanged(new int[]{i});
        }
      }
    }
    /**
     * callback that an entity has been selected.
     */
    public void handleSelection(Selection selection) {
    }
    /**
     * callback that the gedcom is being closed
     */
    public void handleClose(Gedcom which) {
      // De-Register as listener
      which.removeListener(this);
      // Forget about it
      gedcoms.removeElement(which);
      // Update GUI
      fireNumRowsChanged();
    }
    /** returns the Gedcom at given index */
    public Gedcom getGedcom(int which) {
      return (Gedcom)gedcoms.elementAt(which);
    }
    /** callback that a new gedcom has been opened */
    public void addGedcom(Gedcom which) {

      // Register as listener
      which.addListener(this);

      // Remember
      gedcoms.addElement(which);

      // Update GUI
      fireNumRowsChanged();

      // Select first
      setSelectedRow(getNumRows()-1);

      // Done
    }
    /** returns the Gedcoms in this model */
    Vector getGedcoms() {
      return gedcoms;
    }
    // EOC
  }

  /**
   * Constructor
   */
  public GedcomTable() {

    // Prepare a model
    model = new GTableModel();
    setModel(model);

    // Prepare rendering
    CellRenderer[] renderers = new CellRenderer[defaultHeaders.length];
    CellRenderer hrenderer = new GHeaderRenderer();
    for (int c=0;c<defaultHeaders.length;c++) {
      renderers[c] = hrenderer;
    }
    setHeaderCellRenderers(renderers);

    // Done
  }

  /**
   * Add a Gedcom to this table
   */
  public void addGedcom(Gedcom which) {
    model.addGedcom(which);
  }

  /**
   * Returns the Gedcom at given index
   */
  public Gedcom getGedcom(int row) {
    return model.getGedcom(row);
  }

  /**
   * Returns the currently kept Gedcoms
   */
  public Vector getGedcoms() {
    return model.getGedcoms();
  }

  /**
   * Returns the actual selected Gedcom - or null if none selected
   */
  public Gedcom getSelectedGedcom() {
    int row = getSelectedRow();
    if (row<0)
      return null;
    return model.getGedcom(row);
  }

  /**
   * Sets a column width for this GedcomTable - null means default
   */
  public boolean setColumnWidths(int widths[]) {

    if (widths==null) {
      widths = this.defaultWidths;
    }

    return super.setColumnWidths(widths);
  }
}
