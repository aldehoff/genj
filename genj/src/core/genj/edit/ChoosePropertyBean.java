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
package genj.edit;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.reflect.Method;

import javax.swing.*;
import javax.swing.event.*;

import genj.gedcom.*;
import genj.util.Debug;
import genj.util.ImgIcon;
import genj.util.Resources;
import genj.util.swing.ImgIconConverter;

/**
 * A bean that allows to choose a property from a list of properties
 */
public class ChoosePropertyBean extends JComponent implements ItemListener, ListSelectionListener {

  private JRadioButton rbChoose,rbNew;
  private JTextField tfNew;
  private JList lChoose;
  private JScrollPane spInfo;
  private JTextPane tpInfo;

  /**
   * Tag List Cell Renderer
   */
  class TagListRenderer extends JLabel implements ListCellRenderer {

    /** whether the tag in the list is selected or not */
    boolean isSelected;

    /**
     * Return component for rendering list element
     */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
      Property prop = (Property)value;
      setText(prop.getTag());
      try {
        // .. get class of property
        ImgIcon img   = prop.getImage(false);
        setIcon(ImgIconConverter.get(img));
      } catch (Exception e) {
        Debug.log(Debug.WARNING, this, "Unexpected error while retrieving image of "+prop, e);
        setIcon(ImgIconConverter.get( genj.gedcom.Images.get("?")));
      }
      this.isSelected = isSelected;
      return this;
    }

    /**
     * paint is subclassed to draw the background correctly.  JLabel
     * currently does not allow backgrounds other than white, and it
     * will also fill behind the icon.  Something that isn't desirable.
     */
    public void paint(Graphics g) {

      Color bColor;

      if (isSelected) {
        bColor = Color.yellow;
      } else {
        bColor = (getParent() != null) ? getParent().getBackground() : getBackground();
      }

      g.setColor(bColor);
      g.fillRect(0 , 0, getWidth()-1, getHeight()-1);

      super.paint(g);
    }

    // EOC
  }

  /**
   * Constructor
   */
  public ChoosePropertyBean(Property[] knownProps, Resources resources) {

    // Layout
    GridBagLayout layout = new GridBagLayout();
    setLayout(layout);

    // Checkbox for known props
    rbChoose = new JRadioButton(resources.getString("choose.known"),knownProps.length>0);
    rbChoose.setEnabled(knownProps.length>0);
    rbChoose.addItemListener(this);
    rbChoose.setAlignmentX(0);
    add(rbChoose,1,1,2,1,false);

    // .. List of tags
    lChoose = new JList(knownProps);
    lChoose.setEnabled(knownProps.length>0);
    lChoose.setCellRenderer(new TagListRenderer());
    if (knownProps.length==0) lChoose.setPrototypeCellValue(new PropertyUnknown("XXXXX",""));
    lChoose.addListSelectionListener(this);
    JScrollPane sp = new JScrollPane(lChoose);
    add(sp,1,2,1,1,true);

    // .. Info field
    tpInfo = new JTextPane();
    tpInfo.setText("");
    tpInfo.setEditable(false);
    spInfo = new JScrollPane(tpInfo,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
      // LCD
      public Dimension getPreferredSize() {
        return new Dimension(256,128);
      }
      // EOC
    };
    add(spInfo,2,2,1,1,true);

    // RadioButton for new props
    rbNew = new JRadioButton(resources.getString("choose.new"),knownProps.length==0);
    rbNew.addItemListener(this);
    rbNew.setAlignmentX(0);
    add(rbNew,1,3,2,1,false);

    ButtonGroup group = new ButtonGroup();
    group.add(rbChoose);
    group.add(rbNew);

    // Create Lower Part
    tfNew = new JTextField();
    tfNew.setEnabled(knownProps.length==0);
    tfNew.setAlignmentX(0);
    add(tfNew,1,4,2,1,false);

    // Focus
    if (knownProps.length==0) {
      tfNew.requestFocus();
    } else {
      lChoose.requestFocus();
    }

    // Done
  }

  /**
   * Helper method that adds components to a container with gridbaglayout
   */
  private void add(Component c,int x,int y,int w,int h,boolean grow) {
    add(c);

    GridBagConstraints s = new GridBagConstraints();
    s.gridx = x;
    s.gridy = y;
    s.gridwidth = w;
    s.gridheight= h;
    s.weightx = grow ? 1 : 0;
    s.weighty = grow ? 1 : 0;
    s.fill = grow ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;

    ((GridBagLayout)getLayout()).setConstraints(c,s);
  }

  /**
   * Returns the resulting properties
   */
  public Property[] getResultingProperties() {

    Property[] result = null;

    // ... prepare list of selected properties
    if (rbChoose.isSelected() == true) {
      Object[] objs = lChoose.getSelectedValues();
      if ( (objs==null) || (objs.length==0) ) {
        result = null;
      } else {
        result = new Property[objs.length];
        for (int i=0;i<objs.length;i++) {
          result[i] = (Property)objs[i];
        }
      }
    } else {
      // ... create a single entry property list
      if (tfNew.getText().equals("")) {
        result = null;
      } else {
        result = new Property[1];
        result[0] = Property.createInstance(tfNew.getText(),"");
      }
    }

    return result;
  }

  /**
   * RadioButtons have been changed
   */
  public void itemStateChanged(ItemEvent e) {
    if (e.getSource() == rbChoose) {
      lChoose.setEnabled(true);
      tfNew.setEnabled(false);
      lChoose.requestFocus();
    }
    if (e.getSource() == rbNew) {
      lChoose.clearSelection();
      lChoose.setEnabled(false);
      tfNew.setEnabled(true);
      tfNew.requestFocus();
    }
  }

  /**
   * One of the tag-items in the item list has been (de-)selected
   */
  public void valueChanged(ListSelectionEvent e) {

    // Check selection
    Object[] selection = lChoose.getSelectedValues();

    // None selected
    if ((selection==null)||(selection.length==0)) {
      tpInfo.setText("");
      return;
    }

    // Show info of last selected
    Property prop=(Property)selection[selection.length-1];
    tpInfo.setText(prop.getInfo());
    if (!rbChoose.isSelected())
      rbChoose.doClick();

    // Done
  }

}

