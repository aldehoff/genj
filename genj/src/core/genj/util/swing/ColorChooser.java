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

import genj.util.ColorAttribute;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Event;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A 'custom' color chooser
 */
public class ColorChooser extends JColorChooser {
  
  /** a list of color attributes */
  private JList list;
  
  /** radio buttons */
  private JRadioButton 
    rbForeground, 
    rbBackground;
    
  /** new colors */
  private Color[] newForeground, newBackground;

  /**
   * Constructor
   */
  public ColorChooser(List colors) {
    
    // initialize space for new colors
    newForeground = new Color[colors.size()];
    newBackground = new Color[colors.size()];
    
    // connecting to events through glue
    EventGlue glue = new EventGlue();
    getSelectionModel().addChangeListener(glue);
    
    // preview
    JPanel preview = new JPanel(new BorderLayout());
    
    Box bgorfg = new Box(BoxLayout.X_AXIS);
    rbForeground = new JRadioButton("Foreground", true);
    rbBackground = new JRadioButton("Background");
    ButtonGroup bg = new ButtonGroup();
    bg.add(rbForeground);
    bg.add(rbBackground);
    bgorfg.add(rbForeground);
    bgorfg.add(rbBackground);
    preview.add(bgorfg, BorderLayout.NORTH);
    
    list = new JList(colors.toArray());
    list.setCellRenderer(glue);
    list.addListSelectionListener(glue);
    preview.add(list, BorderLayout.CENTER);

    setPreviewPanel(preview);
    
    // set to first
    if (colors.size()>0) list.setSelectedIndex(0);
    
    // done
  }
  
  /**
   * Applies current settings to the colors
   */
  public void apply() {
    // iterate over colors
    ListModel model = list.getModel();
    for (int c=0; c<model.getSize(); c++) {
      ColorAttribute ca = (ColorAttribute)model.getElementAt(c);
      if (newForeground[c]!=null) ca.setForeground(newForeground[c]);
      if (newBackground[c]!=null) ca.setBackground(newBackground[c]);      
    }
    // done
  }
  
  /**
   * Resets current settings 
   */
  public void reset() {
    newForeground = new Color[newForeground.length];
    newBackground = new Color[newBackground.length];
    repaint();
  }
  
  /**
   * Helper to get color attr
   */
  private ColorAttribute getColorAttribute(int i) {
    return (ColorAttribute)list.getModel().getElementAt(i);
  }
  
  /**
   * Helper to get current color
   */
  private Color getColor(int i, boolean fg) {
    // is that a changed value?
    Color result = fg ? newForeground[i] : newBackground[i];
    // if not take original
    if (result==null) {
      ColorAttribute attr = getColorAttribute(i);
      result = fg?attr.getForeground():attr.getBackground();
    }
    // done
    return result;
  }
  
  /**
   * Renderer knowing how to render picks
   */
  private class EventGlue extends JLabel implements ListCellRenderer, ChangeListener, ListSelectionListener {
    /** the original font */
    private Font oFont = ColorChooser.this.getFont();
    /**
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(JList, Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      // make to attr
      ColorAttribute attr = (ColorAttribute)value;
      // update text
      setText(attr.getName());
      // check bg
      Color bg = getColor(index, false);
      if (bg!=null) {
        setOpaque(true);
        setBackground(bg);
      } else {
        setOpaque(false);
      }
      // check fg
      Color fg = getColor(index, true);
      if (fg!=null) {
        setForeground(fg);
      } else {
        setForeground(Color.black);
      }
      // check font
      setFont(isSelected?oFont.deriveFont(Font.BOLD):oFont);
      // done
      return this;
    }
    /**
     * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
      // what's picked
      int i = list.getSelectedIndex();
      if (i<0) return;
      // set its color
      if (rbForeground.isSelected()) newForeground[i] = getColor();
      if (rbBackground.isSelected()) newBackground[i] = getColor();
      // show it
      list.repaint();
      // done
    }
    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
      // what's picked
      ColorAttribute attr = (ColorAttribute)list.getSelectedValue();
      if (attr==null) return;
      // check its changeability
      Color fg = attr.getForeground();
      Color bg = attr.getBackground();
      rbBackground.setEnabled(bg!=null);
      rbForeground.setEnabled(fg!=null);
      rbForeground.setSelected(bg==null);
      rbBackground.setSelected(fg==null);
      // set color
      setColor(getColor(list.getSelectedIndex(),rbForeground.isSelected()));
      // done
    }
  } //PickRenderer

  /**
   * A color model
   */
    
} //ColourChooser
