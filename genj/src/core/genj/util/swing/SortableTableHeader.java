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
package genj.util.swing;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * A patched JTableHeader with the following advantages
 * <il>
 * <li>the table's header understands ImageIcon
 * <li>the table's header understands the concept of a sorted column
 * </il>
 */
public class SortableTableHeader extends JTableHeader {
  
  /** a cached table cell renderer */
  private TableCellRenderer originalRenderer;
  
  /**
   * Constructor
   */
  public SortableTableHeader() {
    setDefaultRenderer(new PatchedHeaderRenderer());
    addMouseListener(new SortClickMouseListener());
  }  

  /**
   * Intercept ui
   */
  public void setUI(TableHeaderUI ui) {
    // continue
    super.setUI(ui);
    // grab original renderer
    originalRenderer = createDefaultRenderer();
    // restore ours
    setDefaultRenderer(new PatchedHeaderRenderer());
  }


  /**
   * @see javax.swing.table.JTableHeader#setTable(JTable)
   */
  public void setTable(JTable table) {
    super.setTable(table);
    if (table!=null) setColumnModel(table.getColumnModel());
  }

  /**
   * A patched header renderer that uses the default renderer from installed
   * L&F and knows sorting and ImageIcon values
   */
  private class PatchedHeaderRenderer extends JLabel implements TableCellRenderer {
    
    /** arrow design */
    private int[] 
      xs = new int[]{ 0,+3,-3}, 
      ya = new int[]{-4,+3,+3},
      yd = new int[]{+3,-3,-3};
    
    /** keeper of the column */
    private int column;
    
    /** default label */
    private JLabel original;
    
    /**
     * @see TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
      
      // remember column
      column = col;

      // ask super for a label
      original = (JLabel)originalRenderer.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
      
      // setup data
      if (value instanceof ImageIcon) {
        original.setIcon((ImageIcon)value);
        original.setText(null);
        original.setHorizontalAlignment(CENTER);
      } else {
        original.setIcon(null);
        original.setText(value.toString());
        original.setHorizontalAlignment(LEFT);
      }
      
      // done
      return this;
    }
    
    /**
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
      
      int
        w = original.getWidth(),
        h = original.getHeight();
      
      // let original do its work
      original.paint(g);

      // paint sort indication
      TableModel model = getTable().getModel();
      if (model instanceof SortableTableModel) {
        SortableTableModel smodel = (SortableTableModel)model;
        if (smodel.getSortedColumn()==column)
          paintSortIndicator(g,w,h,smodel.isAscending());
      }
      // done
    }

    /**
     * proxy setting of bounds
     */
    public void setBounds(int x, int y, int width, int height) {
      original.setBounds(x, y, width, height);
    }
    
    /**
     * proxy preferred size
     */
    public Dimension getPreferredSize() {
      return original.getPreferredSize();
    }
    
    /**
     * Paints the sort indicator
     */
    private void paintSortIndicator(Graphics g, int w, int h, boolean a) {
      // position to paint at
      g.translate(w-8, h/2);
      // position
      g.setColor(getForeground());
      g.fillPolygon(xs, a?ya:yd, xs.length);
      // done
    }

  } //PatchedHeaderRenderer
  
  /**
   * A TableModel that supports sorting
   */
  public interface SortableTableModel extends TableModel {
    /**
     * Whether sorting is Ascending or Descending
     */
    public boolean isAscending();
    /**
     * Returns the sorted column
     */
    public int getSortedColumn();
    /**
     * Sets the sorted Column
     */
    public void setSortedColumn(int col, boolean ascending);
  } //SortableTableModel

  /**
   * Mouse click support for sorting
   */
  private class SortClickMouseListener extends MouseAdapter {
    /**
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
      // find out which column
      int col = columnAtPoint(e.getPoint());
      if (col<0) return;
      // check rect
      if (Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR).equals(getCursor())
        ||Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR).equals(getCursor()))
        return;
      // model that cares?
      TableModel model = getTable().getModel();
      if (model instanceof SortableTableModel) {
        // tell to model
        SortableTableModel stm = (SortableTableModel)model;
        stm.setSortedColumn(col,!stm.isAscending());
        // we do a repaint, too
        repaint();
      }
      // done       
    }
  } //SortClickMouseListener
  
} //PatchedJTable
