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

import genj.util.ColorSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A 'custom' color chooser
 */
public class ColorChooser extends JPanel {
  
  /** the wrapped swing ColorChooser */
  private JColorChooser colorChooser = new JColorChooser();
  
  /** combo for color sets */
  private JComboBox comboSets = new JComboBox();
  
  /** list for colors */
  private JList listColors = new JList();
  
  /** changed colors */
  private List changes = new ArrayList();
    
  /**
   * Constructor
   */
  public ColorChooser() {
    
    // setup wrapped swing color chooser
    EventGlue glue = new EventGlue();
    colorChooser.getSelectionModel().addChangeListener(glue);
    
    // preview
    JPanel preview = new JPanel(new BorderLayout());
    comboSets.addActionListener(glue);
    preview.add(comboSets, BorderLayout.NORTH);
    
    ColorSetModel csm = new ColorSetModel();
    listColors.setModel(csm);
    listColors.setCellRenderer(glue);
    listColors.addListSelectionListener(glue);
    listColors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    //listColors.setMinimumSize(listColors.getPreferredSize());
    preview.add(listColors, BorderLayout.CENTER);
    
    // layout
    setLayout(new BorderLayout());
    add(colorChooser, BorderLayout.CENTER);
    add(preview, BorderLayout.SOUTH);
    
    // done
  }
  
  /**
   * Sets the color sets to look at
   */
  public void setColorSets(ColorSet[] sets) {
    comboSets.setModel(new DefaultComboBoxModel(sets));
    changes.clear();
  }
  
  /**
   * Applies current settings to the colors
   */
  public void apply() {
    // check ColorSet
    for (int i=0; i<comboSets.getModel().getSize(); i++) {
      ColorSet cs = (ColorSet)comboSets.getModel().getElementAt(i);
      // loop changes
      for (int j=0; j<changes.size(); ) {
      	cs.substitute((Color)changes.get(j++), (Color)changes.get(j++));
      }
      // next color-set
    }
    changes.clear();
    comboSets.getModel();
    // done
  }
  
  /**
   * Resets current settings 
   */
  public void reset() {
    changes.clear();
    repaint();
  }
  
  /** 
   * Current ColorSet
   */
  private ColorSet getColorSet() {
    return (ColorSet)comboSets.getSelectedItem();
  }
  
  /**
   * Current color (may have been changed)
   */
  private Color getColor(Color c) {
    // check changes
    for (int i=0; i<changes.size(); i++) {
      if (changes.get(i++)==c) return (Color)changes.get(i);   	
    }
    // done
    return c;
  }
  
  /**
   * List model for color set
   */
  private class ColorSetModel extends AbstractListModel {
    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
      ColorSet cs = (ColorSet)comboSets.getSelectedItem();
      if (cs==null) return 0;
      return cs.getSize()-1;
    }
    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
      ColorSet cs = (ColorSet)comboSets.getSelectedItem();
      return cs.getName(index+1);
    }
  } //ColorSetModel
  
  /**
   * Renderer knowing how to render picks
   */
  private class EventGlue extends JLabel implements ActionListener, ListCellRenderer, ChangeListener, ListSelectionListener {
    /** the original font */
    private Font oFont = ColorChooser.this.getFont();
    /**
     * set selected from combo box
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      comboSets.setModel(comboSets.getModel());
      listColors.clearSelection();
      listColors.repaint();
    }
    /**
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(JList, Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      // update text
      setText(value.toString());
      // current ColorSet
      ColorSet cs = getColorSet();
      // check bg
      setOpaque(true);
      setBackground(getColor(cs.getColor(0)));
      // check fg
      setForeground(getColor(cs.getColor(index+1)));
      // check font
      setFont(isSelected?oFont.deriveFont(Font.BOLD):oFont);
      // done
      return this;
    }
    /**
     * callback color was choosen
     * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
      // make sure it will be shown
      listColors.repaint();
      // grab current color from color selector
      Color color = getColorSet().getColor(listColors.getSelectedIndex()+1);
      if (!color.equals(colorChooser.getColor())) {
        for (int i=0; i<changes.size(); i++) {
          if (changes.get(i++)==color) {
            changes.set(i, colorChooser.getColor());
            return;
          }
        }
        changes.add(color);
        changes.add(colorChooser.getColor());
      }
      // done
    }
    /**
     * callback list selection changed
     * @see javax.swing.event.ListSelectionListener#valueChanged(ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
      colorChooser.setColor(getColorSet().getColor(listColors.getSelectedIndex()+1));
    }
  } //PickRenderer
    
} //ColourChooser
