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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.reflect.Method;

import javax.swing.*;
import javax.swing.event.*;

import genj.gedcom.*;
import genj.util.ImgIcon;
import genj.util.swing.ImgIconConverter;

/**
 * A component that shows a list of TagPaths
 */
public class TagPathList extends JComponent {

  /** members */
  private JList lChoose;
  private Vector vPaths;

  /**
   * Tag List Cell Renderer
   */
  class TagPathRenderer implements ListCellRenderer {
    // LCD
    /** members */
    private JLabel  label = new JLabel();
    private boolean selected;

    /** return component for rendering list element */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {

      TagPath path = (TagPath)value;

      label.setText( path.toString() );
      label.setIcon( ImgIconConverter.get(Property.getDefaultImage(path.getLast())) );

      if (isSelected)
      label.setBackground(lChoose.getSelectionBackground());
      else
      label.setBackground(lChoose.getBackground());
      label.setOpaque(isSelected);

      return label;
    }
    // EOC
  }

  /**
   * Constructor
   */
  public TagPathList() {

    // Layout
    lChoose = new JList();
    lChoose.setCellRenderer(new TagPathRenderer());
    lChoose.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    setLayout(new BorderLayout());
    add(new JScrollPane(lChoose),"Center");

    // Done
  }

  /**
   * Adds a path to this list
   */
  public void addPath(TagPath path) {

    if (vPaths==null)
      vPaths = new Vector();

    vPaths.addElement(path);

    lChoose.setListData(vPaths);
  }

  /**
   * Moves currently selected paths down
   */
  public void down() {

    // Find out which row is selected now
    int row = lChoose.getSelectedIndex();
    if ((row==-1)||(row==vPaths.size()-1))
      return;

    // Move it down
    Object o = vPaths.elementAt(row);
    vPaths.setElementAt(vPaths.elementAt(row+1),row);
    vPaths.setElementAt(o,row+1);

    // Show it
    lChoose.setListData(vPaths);
    lChoose.setSelectedIndex(row+1);
  }

  /**
   * Returns the paths used by this list
   */
  public TagPath[] getPaths() {

    if (vPaths==null)
      return new TagPath[0];

    TagPath[] result = new TagPath[vPaths.size()];
    vPaths.copyInto(result);

    return result;
  }

  /**
   * Returns the preferred size of this component
   */
  public Dimension getPreferredSize() {
    return new Dimension(64,64);
  }

  /**
   * Removes a path from this list
   */
  public void removePath(TagPath path) {

    if (vPaths==null)
      return;

    vPaths.removeElement(path);

    lChoose.setListData(vPaths);
  }

  /**
   * Sets the paths used by this list
   */
  public void setPaths(TagPath[] paths) {

    vPaths = new Vector(paths.length);
    for (int p=0;p<paths.length;p++) {
      vPaths.addElement(paths[p]);
    }

    lChoose.setListData(vPaths);
  }

  /**
   * Moves currently selected paths up
   */
  public void up() {

    // Find out which row is selected now
    int row = lChoose.getSelectedIndex();
    if ((row==-1)||(row==0)) {
      return;
    }

    // Move it up
    Object o = vPaths.elementAt(row);
    vPaths.setElementAt(vPaths.elementAt(row-1),row);
    vPaths.setElementAt(o,row-1);

    // Show it
    lChoose.setListData(vPaths);
    lChoose.setSelectedIndex(row-1);
  }          
}
