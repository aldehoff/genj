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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.plaf.LabelUI;
import javax.swing.table.DefaultTableCellRenderer;
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

  /** the ui used by the 'original' TableCellRender for header */ 
  private JLabel cachedLabel;
  private TableCellRenderer cachedRenderer;
    
  /**
   * Constructor
   */
  public SortableTableHeader() {
    setDefaultRenderer(new PatchedHeaderRenderer());
    addMouseListener(new SortClickMouseListener());
  }  

  /**
   * @see javax.swing.table.JTableHeader#setDefaultRenderer(TableCellRenderer)
   */
  public void setDefaultRenderer(TableCellRenderer defaultRenderer) {
    if (defaultRenderer instanceof PatchedHeaderRenderer) {
      super.setDefaultRenderer(defaultRenderer);
    } else {
      cachedRenderer = defaultRenderer;
      cachedLabel = null;
    }
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
    
    /** keeper of the column */
    private int column;
    
    /**
     * @see TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
      // compute the cached ui we'll use later
      if (cachedLabel==null) {
        if (cachedRenderer==null) cachedRenderer = new DefaultTableCellRenderer();
        cachedLabel = (JLabel)cachedRenderer.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,col);
      }
      // grab the information we need
      if (value instanceof ImageIcon) {
        cachedLabel.setText(null);
        cachedLabel.setIcon((ImageIcon)value);
      } else {
        cachedLabel.setText(value.toString());
        cachedLabel.setIcon(null);
      }
      column = col;
      // done
      return this;
    }
    
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g) {
      int w = getWidth(), h = getHeight();
      // let default renderer do its job
      cachedLabel.setBounds(0,0,w,h);
      cachedLabel.paint(g);
      // HACK: skinlf uses the same JLabel for all TableHeaders in the VM
      cachedLabel.setText(null);
      cachedLabel.setIcon(null);
      // paint sort indication
      TableModel model = getTable().getModel();
      if (model instanceof SortableTableModel) {
        SortableTableModel smodel = (SortableTableModel)model;
        if (smodel.getSortedColumn()==column)
          paintSortIndicator(g,w,h);
      }
      // done
    }
    
    /**
     * Paints the sort indicator
     */
    private void paintSortIndicator(Graphics g, int w, int h) {
      int 
        x = w-8,
        y = h/2;
      g.setColor(cachedLabel.getForeground());
      g.fillPolygon(new int[]{ x+0, x+3, x-3}, new int[]{ y-4, y+4, y+4}, 3);
    }

    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      return cachedLabel.getPreferredSize();
    }

    /**
     * @see java.awt.Component#setBounds(int, int, int, int)
     */
    public void setBounds(int x, int y, int w, int h) {
      super.setBounds(x,y,w,h);
      cachedLabel.setBounds(x,y,w,h);
    }

  } //PatchedHeaderRenderer
  
  /**
   * A TableModel that supports sorting
   */
  public interface SortableTableModel extends TableModel {
    public int getSortedColumn();
  } //SortableTableModel

  /**
   * Mouse click support for sorting
   */
  private class SortClickMouseListener extends MouseAdapter {
    /**
     * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
    }
  } //SortClickMouseListener
  
} //PatchedJTable
