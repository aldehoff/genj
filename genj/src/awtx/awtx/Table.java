/**
 * Nils Abstract Window Toolkit
 *
 * Copyright (C) 2000-2002 Nils Meier <nils@meiers.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package awtx;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import awtx.table.*;

/**
 * Fast Table for displaying row/column data
 */
public class Table extends Scrollpane implements TableModelListener {

  private Content         compContent;
  private Header          compHeader;
  private Vector          listeners = new Vector();
  private boolean         isMultiSelectionAllowed = false;

  private TableModel      model       = null;
  private int[]           translation = null;
  private boolean         isSortable  = false;

  private PopupProvider popupProvider   = null;

  private static DefaultCellRenderer dcr = new DefaultCellRenderer();
  private static DefaultHeaderCellRenderer dhr = new DefaultHeaderCellRenderer();

  private int           initialColumnLayout = NONE;

  public final static int
          NONE = 0,
          SIZE_ALL_COLUMNS = 1;

  public final static int
          CLICK  = 0,
          CCLICK = 1,
          DCLICK = 2,
          POPUP  = 3;

  public final static int
          UP     = 1,
          DOWN   = 0;

  private int          sortedColumn = -1;
  private int          sortedDir = DOWN;
  private BitSet       selectedRows = new BitSet(64);
  private int          draggedColumn = -1;
  private int          colWidths[] = new int[0];
  private int          minWidth = 20;
  private Insets       cellInsets = new Insets(1,1,1,1);
  private CellRenderer colRenderer [];
  private CellRenderer headerRenderer[];

  /**
   * Constructor with default ScrollBars
   */
  public Table() {

    setUseSpace(ALIGN_LEFT,ALIGN_TOP);

    // Add content renderer
    compContent = new Content(this);
    compHeader  = new Header (this);

    setQuadrant(CENTER,compContent);
    setQuadrant(NORTH ,compHeader );

    // Listen to clicks
    MouseListener mlistener = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
            compContent.requestFocus();
            int mode = CLICK;
            if (e.isPopupTrigger())
                    mode = POPUP;
            if (isDoubleClick(e))
                    mode = DCLICK;
            if (e.isControlDown())
                    mode = CCLICK;

            mouseClick(
              e.getSource(),
              e.getX(),
              e.getY(),
              mode
            );
      }
      // EOC
    };
    getQuadrant(CENTER).addMouseListener(mlistener);
    getQuadrant(NORTH ).addMouseListener(mlistener);

    // Listens to mouse movement
    MouseMotionListener mmlistener = new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {
            Table.this.mouseDragged(e.getX(),e.getY());
      }
      public void mouseMoved(MouseEvent e) {
            Table.this.mouseMoved(e.getX(),e.getY());
      }
      // EOC
    };
    getQuadrant(NORTH).addMouseMotionListener(mmlistener);

    // Key listening
    KeyListener klistener = new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
            Point old = getScrollPosition();
            int move = 0;
            switch (e.getKeyCode()) {
              case KeyEvent.VK_UP :
                    move = -1;
                    break;
              case KeyEvent.VK_DOWN :
                    move = 1;
                    break;
              case KeyEvent.VK_PAGE_UP:
                    move = -10;
                    break;
              case KeyEvent.VK_PAGE_DOWN:
                    move = 10;
                    break;
            }

            if (move!=0) {
              int newrow;
              if (translation==null)
                    newrow = getSelectedRow()+move;
              else {
                    newrow = Math.min(getNumRows()-1,Math.max(0,translation[getSelectedRow()]+move));
                    newrow = translation[newrow];
              }
              setSelectedRow(newrow);
            }

      }
      // EOC
    };

    compContent.addKeyListener(klistener);
    // Done
  }

  /**
   * Adds a Listener
   */
  public void addTableListener(TableListener l) {
    listeners.addElement(l);
  }

  /**
   * Clears the selection
   * @param row the new selected row
   */
  public void clearSelection() {

    // Model existing?
    if (model==null)
      return;

    // Remember
    selectedRows.and(new BitSet(64));

    // Signal
    fireSelectionChanged();

    // Done
  }

  /**
   * Helper for comparing values in translation table
   * @return 0 if equal, <0 if i<j and >0 if i>j
   */
  private int compareTranslation(int i, int j) {

    int result = (sortedDir==DOWN ? 1 : -1) *
                    model.compareRows(translation[i],translation[j],sortedColumn);

    return result;

  }

  /**
   * Notifies listeners of action performed
   * (in case of Enter or double click)
   */
  protected void fireActionPerformed(int row) {
    // Tell listeners!
    Enumeration e = listeners.elements();
    while (e.hasMoreElements())
      ((TableListener)e.nextElement()).actionPerformed(row);
    // Done
  }

  /**
   * Notifies listeners of change in selection
   */
  protected void fireSelectionChanged() {

    // Show it
    getQuadrant(CENTER).repaint();

    // Tell listeners!
    Enumeration e = listeners.elements();
    while (e.hasMoreElements())
      ((TableListener)e.nextElement()).rowSelectionChanged(getSelectedRows());

    // Done
  }

  /**
   * Returns the cell insets for this table.
   * @see setCellInsets
   */
  public Insets getCellInsets() {
    return cellInsets;
  }

  /**
   * Accessor for header renderers
   */
  public CellRenderer[] getColumnHeaderRenderers() {
        return headerRenderer;
  }

  /**
   * Accessor for column renderers
   */
  public CellRenderer[] getColumnRenderers() {
        return colRenderer;
  }

  /**
   * Returns an array with all column widths
   */
  public int[] getColumnWidths() {
        return colWidths;
  }

  /**
   * Returns the header object of given column of this table
   */
  public Object getHeaderAt(int column) {
        // Model present?
        if (model==null)
          return null;
        // Done
        return model.getHeaderAt(column);
  }

  /**
   * Returns the data of this Table
   */
  public TableModel getModel() {
        return model;
  }

  /**
   * Returns the number of columns of this table
   */
  public int getNumColumns() {
        if (model==null)
          return 0;
        return model.getNumColumns();
  }

  /**
   * Returns the number of rows of this table
   */
  public int getNumRows() {
        if (model==null)
          return 0;
        return model.getNumRows();
  }

  /**
   * Returns a selected row
   * @return row selected last or -1
   */
  public int getSelectedRow() {

    // None selected?
    int rows[] = getSelectedRows();
    if (rows.length==0)
            return -1;

    // First!
    return rows[0];
  }

  /**
   * Returns the selected rows
   */
  public int[] getSelectedRows() {

    if (model==null)
            return new int[0];

    int[] temp = new int[model.getNumRows()];
    int count = 0;
    for (int i=0;i<temp.length;i++) {
            if (selectedRows.get(i))
                    temp[count++]=i;
    }
    int[] result = new int[count];
    System.arraycopy(temp,0,result,0,count);

    return result;
  }

  /**
   * the index of the sorting by column
   */
  public int getSortedColumn() {
    return sortedColumn;
  }

  /**
   * Convenient accessing method
   */
  public int getSortedDir() {
        return sortedDir;
  }

  /**
   * Returns the transformed index of a row in this Table
   */
  public int getTranslatedRow(int row) {

    // Model present?
    if (model==null)
      return row;

    // Translated?
    if (translation!=null)
      return translation[row];

    // Default
    return row;
  }

  /**
   * Event number of rows have changed
   */
  public void handleNumRowsChanged(int newRows) {

    // Forget any sorting
    sortedColumn = -1;
    translation = null;

    // Validate View
    // 20020814 changed from simply doLayout to ...
    invalidate();
    validate();

    // Show it
    repaint();
    
    // notify
    fireSelectionChanged();
  }

  /**
   * Event certain rows have changed
   */
  public void handleRowsChanged(int[] rows) {

    // Show it
    repaint();
  }

  /**
   * Method for identifying MouseDoubleClicks
   */
  protected boolean isDoubleClick(MouseEvent e) {
    return e.getClickCount()>1;
  }

  /**
   * Checks whether a row is selected
   * @param row the candidate to be tested
   */
  public boolean isSelectedRow(int row) {
    if (row<0)
            return false;
    return selectedRows.get(row);
    // Done
  }

  /**
   * Returns wether this table is sortable
   */
  public boolean isSortable() {
    return isSortable;
  }

  /**
   * Helper which calculate column widths
   */
  private void layoutColumns() {
    /*
    // Something to do?
    if ((model==null)||(tableComponent.getSize().width<=0))
      return;

    // Mode?
    if (initialColumnLayout==SIZE_ALL_COLUMNS) {
      int w = tableComponent.getSize().width / colWidths.length;
      for (int i=0;i<colWidths.length;i++)
            colWidths[i] = w;

    }
    */
    // Done
    initialColumnLayout=NONE;
  }

  /**
   * Handle a click on tableComponent
   */
  private void mouseClick(Object source, int xpos, int ypos, int mode) {

    // Valid column?
    int col = compContent.convertPos2Col(xpos);
    if (col<0)
      return;

    // Header?
    if (source==getQuadrant(NORTH)) {
      if ((isSortable)&&(draggedColumn<0)&&(ypos>=0)) {
        setSortedColumn(col, sortedDir==UP?DOWN:UP);
      }
      return;
    }

    // Valid row in content?
    int row = compContent.convertPos2Row(ypos);
    if (row<0) {
      return;
    }

    // Change selection
    if (translation!=null) {
      row = translation[row];
    }

    // DoubleClick or Popup?
    switch (mode) {
      // Set one line selection
      default:
        setSelectedRow(row);
        break;
      // Add to selection
      case CCLICK:
        toggleSelectedRow(row);
        break;
      // Provide a Popup
      case POPUP:
        // Select single row
        setSelectedRow(row);
        // A PopupProvider there?
        if (popupProvider!=null)
                popupProvider.providePopup(getQuadrant(CENTER),xpos,ypos);
        // Done
        break;
      // Signal Double Click
      case DCLICK:
        // Select single row
        setSelectedRow(row);
        fireActionPerformed(row);
        break;
    }

    // Done
  }

  /**
   * MouseDragged
   */
  private void mouseDragged(int x, int y) {

    // No Column being dragged?
    if (draggedColumn<0) {
      return;
    }

    // Calculate new Column Width
    int pos = compContent.convertCol2Pos(draggedColumn);
    int w   = Math.max(minWidth,x-pos);

    // .. size
    colWidths[draggedColumn]=w;

    // Show it
    getQuadrant(CENTER).invalidate();

    // Done
  }

  /**
   * MouseMovement
   */
  private void mouseMoved(int x, int y) {

    int cursor = Cursor.DEFAULT_CURSOR;
    draggedColumn=-1;

    // Resize cursor?
    int col = compContent.convertPos2Col(x);
    if ((col>=0)&&(compContent.convertCol2Pos(col)+colWidths[col]-x<8)) {

      // .. use another cursor
      cursor = Cursor.W_RESIZE_CURSOR;

      // .. remember column
      draggedColumn=col;
    }

    // Set cursor
    getQuadrant(NORTH).setCursor(Cursor.getPredefinedCursor(cursor));

    // Done
  }

  /**
   * Helper for sorting the translation table
   */
  private void qSortTranslation(int lower, int upper) {

    // Nothing to sort?
    if (lower>=upper)
      return;

    // Only Swapping necessary?
    if (lower+1==upper) {
      if (compareTranslation(lower,upper)>0) {
        swapTranslation(lower,upper);
      }
      return;
    }

    // Split range
    int pivot = lower + (upper-lower)/2;

    // Swap places
    int l=lower,
            u=upper;

    while (true) {

      // .. lower < pivot?
      while ( (l<pivot) && (compareTranslation(l,pivot)<= 0) ) {
        l++;
      }

      // .. upper > pivot?
      while ( (u>pivot) && (compareTranslation(pivot,u)< 0) ) {
        u--;
      }

      // Done?
      if (l>=u) {
        break;
      }

      // .. swap lower against upper?
      if ((l<pivot)&&(u>pivot)) {
        swapTranslation(l,u);
        l++;
        u--;
        continue;
      }

      // .. move pivot otherwise
      if (l<pivot) {
        swapTranslation(l,pivot);
        pivot=l;
      } else {
        swapTranslation(pivot,u);
        pivot=u;
      }

      // .. continue
    }

    // Sort lower half
    qSortTranslation(lower,pivot-1);

    // Sort upper half
    qSortTranslation(pivot+1,upper);

    // Done
  }

  /**
   * Notification that this component isn't needed anymore
   */
  public void removeNotify() {
    setModel(null);
    // Delegate
    super.removeNotify();
  }

  /**
   * Removes a Listener
   */
  public void removeTableListener(TableListener l) {
        listeners.removeElement(l);
  }

  /**
   * Selects all
   * @param row the new selected row
   */
  public void selectAll() {

    // Model existing?
    if (model==null)
      return;

    // Remember
    for (int i=0;i<model.getNumRows();i++)
            selectedRows.set(i);

    // Signal
    fireSelectionChanged();

    // Done
  }

  /**
   * Sets cell insets for this table - that is the
   * space around every cell in the rendered table.
   */
  public void setCellInsets(Insets insets) {
    cellInsets = insets;
  }

  /**
   * Sets cell renderers for all columns
   * @param an array of new renderers which can contain nulls
   */
  public void setCellRenderers(CellRenderer renderers[]) {

    // Not used in case of missing model
    if (colRenderer==null)
      return;

    // Remember
    for (int c=0;c<colWidths.length && c<renderers.length;c++) {
      if (renderers[c]!=null)
            colRenderer[c]=renderers[c];
    }

    // Show
    repaint();

    // Done
  }

  /**
   * Sets a column width
   * @param widths an array with widths
   * @return wether all columns were set
   */
  public boolean setColumnWidths(int widths[]) {

    // Remember
    for (int c=0;c<colWidths.length && c<widths.length;c++) {
      colWidths[c]=Math.max(minWidth,widths[c]);
    }

    // Show
    doLayout();

    // Done
    return widths.length==colWidths.length;
  }

  /**
   * Sets header renderers for all columns
   * @param an array of new renderers which can contain nulls
   */
  public void setHeaderCellRenderers(CellRenderer renderers[]) {

    // Not used in case of missing model
    if (colRenderer==null)
      return;

    // Remember
    for (int c=0;c<colWidths.length && c<renderers.length;c++) {
      if (renderers[c]!=null) {
        headerRenderer[c]=renderers[c];
      }
    }

    // Show
    repaint();

    // Done
  }

  /**
   * Sets the behaviour for intial column layout
   */
  public void setInitialColumnLayout(int l) {
    initialColumnLayout=l;
  }

  /**
   * Set Data of this Table
   */
  public void setModel(TableModel pModel) {

    // Old Model?
    if (model!=null) {
      model.removeTableModelListener(this);
    }

    // Remember new
    model = pModel;

    // Skip sorting translation
    translation = null;
    sortedColumn = -1;
    clearSelection();

    // Create initial parameters
    if (model!=null) {

      // .. width of cols
      colWidths = new int[model.getNumColumns()];
      for (int i=0;i<model.getNumColumns();i++) {
        colWidths[i] = 100;
      }

      // .. Renderer
      colRenderer = new CellRenderer[model.getNumColumns()];
      headerRenderer = new CellRenderer[model.getNumColumns()];

      for (int i=0;i<model.getNumColumns();i++) {
        colRenderer[i] = dcr;
        headerRenderer[i] = dhr;
      }

      // Listen
      model.addTableModelListener(this);
    }

    // Validate View
    doLayout();
  }

  /**
   * Changes the behaviour of selecting more than one row at once
   */
  public void setMultiSelectedAllowed(boolean set) {
    isMultiSelectionAllowed = set;
  }

  /**
   * Sets the PopupProvider to be used here
   */
  public void setPopupProvider(PopupProvider provider) {
    popupProvider = provider;
  }

  /**
   * Sets the selection to a single row
   * @param row the new selected row
   */
  public void setSelectedRow(int row) {

    // Model existing?
    if (model==null) {
      return;
    }

    // Bounds
    if ((row<0)||(row>model.getNumRows())) {
      throw new IllegalArgumentException("setSelectedRow out of bounds");
    }

    // Known?
    //if (selectedRows.get(row)&&(getSelectedRows().length==0))
    if (selectedRows.get(row)) {
      return;
    }

    // Remember
    selectedRows = new BitSet(model.getNumRows());
    selectedRows.set(row);

    // Signal
    fireSelectionChanged();

    // Done
  }

  /**
   * Dis/Enables sorting
   */
  public void setSortable(boolean on) {
    isSortable=on;
  }

  /**
   * Sets the column to be sorted by
   * @param col the sorting column (0based)
   * @param dir direction
   */
  public void setSortedColumn(int col, int dir) {

    // Sorting necessary?
    if ((getNumRows()<2)||(col<0)||(col>=getNumColumns())) {

      sortedColumn = -1;
      translation = null;

    } else {

      // Remember
      sortedColumn = col;
      sortedDir    = dir;

      // Sort
      if (translation==null) {
        translation = new int[getNumRows()];
      }

      // .. fill normal translation
      for (int i=0;i<translation.length;i++) {
        translation[i] = translation.length-1-i;
      }

      // .. sort it
      qSortTranslation(0,translation.length-1);

      // Done
    }

    // Show it
    getQuadrant(CENTER).repaint();
    getQuadrant(NORTH ).repaint();
  }

  /**
   * Helper for swapping values in translation table
   */
  private void swapTranslation(int i, int j) {
    int t = translation[i];
    translation[i]=translation[j];
    translation[j]=t;
  }

  /**
   * Adds a row to the selection
   * @param row the new selected row
   */
  public void toggleSelectedRow(int row) {

    // Model existing?
    if (model==null) {
      return;
    }

    // Change selection
    if (selectedRows.get(row))
      selectedRows.clear(row);
    else {
      if (!isMultiSelectionAllowed) {
        selectedRows = new BitSet(model.getNumRows());
      }
      selectedRows.set(row);
    }

    // Signal
    fireSelectionChanged();

    // Done
  }

}

