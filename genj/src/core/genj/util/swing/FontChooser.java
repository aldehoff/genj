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

import genj.util.GridBagHelper;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

/**
 * A component for choosing a font */
public class FontChooser extends JPanel {
  
  /** combo for fonts */
  private JComboBox comboFonts;
  
  /** text for size */
  private JTextField textSize;
  
  /** list of all fonts */
  private static Font[] allFonts = null;

  /**
   * Constructor   */
  public FontChooser() {
    // sub-components
    comboFonts = new JComboBox();
    comboFonts.setPrototypeDisplayValue(getFont());
    textSize = new JTextField(2);
    textSize.setText("12");
    // listening
    Glue glue = new Glue();
    comboFonts.setRenderer(glue);
    //layout
    setAlignmentX(0F);
    GridBagHelper gh = new GridBagHelper(this);
    gh.add(comboFonts      , 0, 0, 1, 1, gh.GROW_HORIZONTAL);
    gh.add(textSize        , 1, 0);
    gh.add(Box.createGlue(), 2, 0, 1, 1, gh.GROW_BOTH);
    // done
  }
  
  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    super.addNotify();
    
    // init combobox with fontlist
    comboFonts.setModel(new DefaultComboBoxModel(getAllFonts()));
    
    // done
  }
  
  /**
   * @see javax.swing.JComponent#getMaximumSize()
   */
  public Dimension getMaximumSize() {
    return new Dimension(Integer.MAX_VALUE, comboFonts.getPreferredSize().height);
  }
  
  /**
   * Helper to get fontlist
   */
  private static Font[] getAllFonts() {
    if (allFonts==null) allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    return allFonts;
  }
  
  /**
   * Calculates current selected size   */
  private int getSelectedFontSize() {
    int result = 2;
    try {
      result = Integer.parseInt(textSize.getText());
    } catch (Throwable t) {
    }
    return Math.max(2,result);
  }
  
  /**
   * Accessor - selected font   */
  public void setSelectedFont(Font font) {
    // make sure fonts are prepared
    getAllFonts();
    // look for font
    for (int f=0; f<allFonts.length; f++) {
      if (allFonts[f].getName().equals(font.getName())) {
        comboFonts.setSelectedIndex(f);
        break;
      }
    }
    // set size
    textSize.setText(""+font.getSize());
    // done
  }
  
  /**
   * Accessor - selected font   */
  public Font getSelectedFont() {
    Font f = (Font)comboFonts.getSelectedItem();
    return f.deriveFont((float)getSelectedFontSize());
  }
  
  /**
   * glue
   */
  private class Glue implements ListCellRenderer {
    
    /** a label */
    private HeadlessLabel label = new HeadlessLabel();

    /**
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value instanceof Font) {
        Font font = (Font)value;
        label.setFont(font.deriveFont((float)12));
        label.setText(font.getName());
      } else {
        label.setText("Foo");
      }
      return label;
    }
  } //Glue
} //FontChooser
