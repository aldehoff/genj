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

import genj.gedcom.MetaProperty;
import genj.gedcom.TagPath;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * A component that shows a list of TagPaths
 */
public class TagPathList extends JComponent {

  /** list showing tag paths */
  private JList lChoose;
  
  /** the tag paths */
  private List paths = new ArrayList();
  
  /** the selection */
  private Set selection = null;

  /**
   * Constructor
   */
  public TagPathList() {

    // Layout
    lChoose = new JList();
    lChoose.setCellRenderer(new Renderer());
    lChoose.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    lChoose.addMouseListener(new SelectionListener());

    setLayout(new BorderLayout());
    add(new JScrollPane(lChoose),"Center");

    // Done
  }

  /**
   * Returns the preferred size of this component
   */
  public Dimension getPreferredSize() {
    return new Dimension(64,64);
  }

  /**
   * propagates the current state of paths to the list
   */
  private void update() {
    lChoose.setListData(paths.toArray(new Object[paths.size()]));
  }
  
  /**
   * Adds a path to this list
   */
  public void addPath(TagPath path) {
    paths.add(path);
    update();
  }

  /**
   * Removes a path from this list
   */
  public void removePath(TagPath path) {
    paths.remove(path);
    update();
  }

  /**
   * Returns the paths used by this list
   */
  public TagPath[] getPaths() {
    return (TagPath[])paths.toArray(new TagPath[paths.size()]);
  }

  /**
   * Sets the paths used by this list
   */
  public void setPaths(TagPath[] set) {
    paths = Arrays.asList(set);
    update();
  }

  /**
   * Sets the paths used by this list
   */
  public void setPaths(Collection c) {
    paths = new ArrayList(c);
    update();
  }
  
  /**
   * Set selection
   */
  public void setSelection(Set set) {
    selection = new HashSet(set);
  }
  
  /**
   * Return selection
   */
  public Set getSelection() {
    if (selection==null) selection = new HashSet();
    return Collections.unmodifiableSet(selection);
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

    // Move it down
    Object o = paths.get(row);
    paths.set(row, paths.get(row-1));
    paths.set(row-1, o);

    // Show it
    update();
    lChoose.setSelectedIndex(row-1);
  }          

  /**
   * Moves currently selected paths down
   */
  public void down() {

    // Find out which row is selected now
    int row = lChoose.getSelectedIndex();
    if ((row==-1)||(row==paths.size()-1))
      return;

    // Move it down
    Object o = paths.get(row);
    paths.set(row, paths.get(row+1));
    paths.set(row+1, o);

    // Show it
    update();
    lChoose.setSelectedIndex(row+1);
  }

  /**
   * Tag List Cell Renderer
   */
  private class Renderer extends DefaultListCellRenderer{

    /** members */
    private JPanel        panel = new JPanel();
    private JCheckBox     check = new JCheckBox();

    /** Constructor */
    private Renderer() {
      check.setOpaque(false);
      panel.setOpaque(false);
      panel.setLayout(new BorderLayout());
      panel.add(check,"West");
      panel.add(this,"Center");
    }

    /** callback for component that renders element */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
      // here's the path
      TagPath path = (TagPath)value;
      // prepare its data
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      setText( path.toString() );
      setIcon( MetaProperty.get(path).getImage() );
      // with selection or not?
      if (selection==null) return this;
      // update 
      check.setSelected( selection.contains(path) );
      return panel;
    }

  } //Renderer

  /**
   * Listening to mouse input    
   */
  private class SelectionListener extends MouseAdapter {
    /** press */
    public void mousePressed(MouseEvent me) {
      // Check wether some valid position has been clicked on
      int pos = lChoose.locationToIndex(me.getPoint());
      if (pos==-1) return;
      // Get entry and invert selection
      TagPath path = (TagPath)paths.get(pos);
      if (!selection.remove(path)) selection.add(path);
      // Show it 
      lChoose.repaint(lChoose.getCellBounds(pos,pos));
    }
  } //SelectionListener
  
} //TagPathList
