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

import genj.util.EnvironmentChecker;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A component for choosing a font */
public class FontChooser extends JPanel {
  
  /** list of all font families */
  private static String[] families = null;
  
  /** combo for fonts */
  private JComboBox fonts;
  
  /** text for size */
  private JTextField size;
  
  /** 
   * apparently on some systems there might be a problem with 
   * accessing all fonts (vmcrash reported by dmoyne) when
   * we render each and every of those fonts in the font-selection-list
   */
  private final static boolean isRenderWithFont = 
    null == EnvironmentChecker.getProperty(FontChooser.class, "genj.debug.fontproblems", null, "supress font usage in font-selection-list");
    

  
  /**
   * Constructor   */
  public FontChooser() {
    
    // sub-components
    fonts = new JComboBox(getAllFonts());
    fonts.setEditable(false);
    fonts.setRenderer(new Renderer());
    size = new JTextField(3);
    
    //layout
    setAlignmentX(0F);
    
    setLayout(new BorderLayout());
    add(fonts, BorderLayout.CENTER);
    add(size , BorderLayout.EAST  );
    
    // done
  }
  
  /**
   * Patched max size
   */
  public Dimension getMaximumSize() {
    Dimension result = super.getPreferredSize();
    result.width = Integer.MAX_VALUE;
    return result;
  }

  /**
   * Accessor - selected font   */
  public void setSelectedFont(Font font) {
    String family = font.getFamily();
    Font[] fs = getAllFonts();
    for (int i = 0; i < fs.length; i++) {
      if (fs[i].getFamily().equals(family)) {
        fonts.setSelectedIndex(i);
        break;
      }
      
    }
    size.setText(""+font.getSize());
  }
  
  /**
   * Accessor - selected font   */
  public Font getSelectedFont() {
    Font font = (Font)fonts.getSelectedItem();
    if (font==null)
      font = getFont();
    return font.deriveFont((float)getSelectedFontSize());
  }
  
  /**
   * Calculates current selected size
   */
  private int getSelectedFontSize() {
    int result = 2;
    try {
      result = Integer.parseInt(size.getText());
    } catch (Throwable t) {
    }
    return Math.max(2,result);
  }
  
  /**
   * Calculate all available fonts
   */
  private Font[] getAllFonts() {

    // initialize families
    if (families==null)
      families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    
    // loop
    Font[] values = new Font[families.length];
    for (int i = 0; i < values.length; i++) {
      values[i] = new Font(families[i],0,12); 
    }
    
    // done
    return values;
  }
  
//  /**
//   * Font list
//   */
//  private static class Model extends AbstractListModel implements ComboBoxModel{
//    
//    /** list of all font families */
//    private static String[] families = null;
//
//    /** values */
//    private Object[] values;
//    
//    /** selection */
//    private int selection;
//    
//    /**
//     * Constructor
//     */
//    private Model() {
//      // grab families once
//  System.out.println("1");
//      if (families==null)
//        families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
//  System.out.println("/1");
//      // copy families into values
//      values = new Object[families.length];
//      System.arraycopy(families, 0, values, 0, families.length);
//      
//      // test loop
//      System.out.println("2");
//      for (int i = 0; i < values.length; i++) {
//        values[i] = new Font(values[i].toString(), 0, 12);
//      }
//      System.out.println("/2");
//      // done
//    }
//    
//    /**
//     * Size
//     */
//    public int getSize() {
//      return values.length;
//    }
//    
//    /**
//     * Element
//     */
//    public Object getElementAt(int index) {
//      return values[index];
//    }
//    
//    /**
//     * Selection
//     */
//    public Font getSelectedFont(int size) {
//      Object font = getSelectedItem();
//      // none
//      if (font==null)
//        return null;
//      // Font
//      if (font instanceof Font)
//        return ((Font)font).deriveFont((float)size);
//      // FontFamily      
//      return new Font(font.toString(), 0, size);
//    }
//    
//    /**
//     * Selection
//     */
//    public void setSelectedItem(Object set) {
//      // translate to family
//      if (set instanceof Font)
//        set = ((Font)set).getFamily();
//      // look for it
//      synchronized (values) {
//        for (int i = 0; i < values.length; i++) {
//          Object font = values[i];
//          // font or font family name in model?
//          if ( (font instanceof Font&&((Font)font).getFamily().equals(set)) || font.equals(set))  {
//            selection = i;
//            return;
//          }
//        }
//      }
//      // done
//    }
//
//    /**
//     * Selection
//     */    
//    public Object getSelectedItem() {
//      return selection<0 ? null : values[selection];
//    }
//
//  } // Model
  
  private static class Renderer extends DefaultListCellRenderer {
    
    /**
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value instanceof Font) {
        Font font = (Font)value;
        super.getListCellRendererComponent(list, font.getFamily(), index, isSelected, cellHasFocus);
        if (isRenderWithFont)
          setFont(font);
      } else {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
      return this;
    }
    
  } //Renderer
  
} //FontChooser
