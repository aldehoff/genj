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

import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.swing.ImgIconConverter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

/**
 * A component allowing to select a Tag
 */
public class TagSelector extends JComponent {

  /** members */
  private JList   lChoose;
  private List    entries = new ArrayList(64);
  private boolean isChanged = false;

  /**
   * Constructor
   */
  public TagSelector() {

    // create a list
    lChoose = new JList();
    lChoose.setCellRenderer(new EntryRenderer());
    lChoose.addMouseListener(new SelectionListener());

    // Layout
    setLayout(new BorderLayout());
    add(new JScrollPane(lChoose),BorderLayout.CENTER);

    // Done
  }

  /**
   * Returns the preferred size of this component
   */
  public Dimension getPreferredSize() {
    return new Dimension(64,64);
  }

  /**
   * Returns selected tags
   */
  public Set getSelection() {
    Set result = new HashSet(entries.size());
    for (int e=0; e<entries.size(); e++) {
      Entry entry = (Entry)entries.get(e);
      if (entry.selected) result.add(entry.tag);
    }
    return result;
  }

  /**
   * Returns wether some selection change has occured after last setSelectedTags
   */
  public boolean isChanged() {
    return isChanged;
  }

  /**
   * Specifies tags to be selected
   */
  public void setSelection(Set tags) {
    
    // change is reset
    isChanged = false;

    // Loop through available tags
    for (int e=0; e<entries.size(); e++) {
      Entry entry = (Entry)entries.get(e);
      entry.selected = tags.contains(entry.tag);
    }

    // show it
    repaint();
    
    // Done
  }

  /**
   * Sets the tags used by this list
   */
  public void setTags(List tags) {
    // gather the entries
    entries.clear();
    for (int e=0;e<tags.size();e++) {
      entries.add(new Entry(tags.get(e).toString()));
    }
    // set it
    lChoose.setListData(entries.toArray());
    // done
  }

  /**
   * An entry in our list
   */
  private class Entry {
    /** members */
    String tag;
    boolean selected;
    /** constructor */
    Entry(String pTag) { tag = pTag; }
  } //Entry

  /**
   * Tag List Cell Renderer
   */
  private class EntryRenderer implements ListCellRenderer {

    // LCD
    /** members */
    private JPanel    panel = new JPanel();
    private JCheckBox check = new JCheckBox();
    private JLabel    label = new JLabel();

    /** Constructor */
    private EntryRenderer() {
      panel.setLayout(new BorderLayout());
      panel.add(check,"West");
      panel.add(label,"Center");

      check.setOpaque(false);
      label.setOpaque(false);
      panel.setOpaque(false);
    }

    /** callback for component that renders element */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
      // here's the entry
      Entry entry = (Entry)value;
      // prepare its data
      label.setText( entry.tag );
      label.setIcon( ImgIconConverter.get(Property.getDefaultImage(entry.tag)) );
      check.setSelected( entry.selected );
      // done
      return panel;
    }

  } //EntryRenderer

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
      Entry entry = (Entry)lChoose.getModel().getElementAt(pos);
      entry.selected = !entry.selected;
      isChanged = true;
      // Show it 
      lChoose.repaint(lChoose.getCellBounds(pos,pos));
    }
  } //SelectionListener
  
} //TagSelector
