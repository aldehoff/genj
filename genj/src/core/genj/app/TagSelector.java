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
 * A component allowing to select a Tag
 */
public class TagSelector extends JComponent {

  /** members */
  private JList  lChoose;
  private Vector vTags;
  private boolean isChanged = false;

  /**
   * Tag List Cell Renderer
   */
  class TagSelectorRenderer implements ListCellRenderer {

    // LCD
    /** members */
    private JPanel    panel = new JPanel();
    private JCheckBox check = new JCheckBox();
    private JLabel    label = new JLabel();

    /** Constructor */
    TagSelectorRenderer() {
      panel.setLayout(new BorderLayout());
      panel.add(check,"West");
      panel.add(label,"Center");

      check.setOpaque(false);
      label.setOpaque(false);
      panel.setOpaque(false);
    }

    /** callback for component that renders element */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {

      Tag tag = (Tag)value;

      label.setText( tag.tag );
      label.setIcon( ImgIconConverter.get(Property.getDefaultImage(tag.tag)) );
      check.setSelected( tag.selected );

      return panel;
    }

    // EOC
  }

  /**
   * A tag to choose
   */
  private class Tag {
    // LCD
    /** members */
    String tag;
    boolean selected;
    /** constructor */
    Tag(String pTag) {
      tag = pTag;
    }
    // EOC
  }

  /**
   * Constructor
   */
  public TagSelector() {

    // Layout
    lChoose = new JList();
    lChoose.setCellRenderer(new TagSelectorRenderer());

    // Listening
    MouseAdapter madapter = new MouseAdapter() {
      // LCD
      /** callback for mouse click */
      public void mouseClicked(MouseEvent me) {
        // Check wether some valid position has been clicked on
        int pos = lChoose.locationToIndex(me.getPoint());
        if (pos==-1)
          return;
        // Get Node and invert selection
        Tag tag = (Tag)lChoose.getModel().getElementAt(pos);
        tag.selected = ! tag.selected;
        isChanged = true;
        // Show it ... simple repaint ... but could be done better ... probably
        lChoose.repaint();
      }
      // EOC
    };
    lChoose.addMouseListener(madapter);

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
   * Returns selected tags
   */
  public String[] getSelectedTags() {

    // Loop through available tags
    Vector tags = new Vector(vTags.size());

    for (int i=0;i<vTags.size();i++) {
      Tag tag = (Tag)vTags.elementAt(i);
      if (tag.selected==true)
      tags.addElement(tag.tag);
    }

    String result[] = new String[tags.size()];
    tags.copyInto(result);

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
  public void selectTags(String[] tags) {

    isChanged = false;

    // Loop through available tags
    outer:
    for (int i=0;i<vTags.size();i++) {

      // .. calculate Tag
      Tag tag = (Tag)vTags.elementAt(i);

      // .. check for selection
      for (int j=0;j<tags.length;j++) {
        if (tags[j].equals(tag.tag)) {
          tag.selected=true;
          continue outer;
        }
      }
      tag.selected=false;

      // .. next
    }

    // Done
    repaint();
  }

  /**
   * Sets the tags used by this list
   */
  public void setTags(String[] tags) {

    vTags = new Vector(tags.length);
    for (int t=0;t<tags.length;t++) {
      vTags.addElement(new Tag(tags[t]));
    }

    lChoose.setListData(vTags);
  }

}
