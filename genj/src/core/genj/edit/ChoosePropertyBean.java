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

import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.util.GridBagHelper;
import genj.util.Resources;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A bean that allows to choose a property from a list of properties
 */
public class ChoosePropertyBean extends JComponent implements ItemListener, ListSelectionListener {

  private JRadioButton rbChoose,rbNew;
  private JTextField tfNew;
  private JList lChoose;
  private JScrollPane spInfo;
  private JTextPane tpInfo;
  private Property parent;

  /**
   * Constructor
   */
  public ChoosePropertyBean(Property pArent, Resources resources) {
    
    // keep parent and calculate possible properties
    parent = pArent;
    MetaProperty[] defs = parent.getMetaProperties(MetaProperty.FILTER_NOT_HIDDEN);
        
    // Layout
    GridBagHelper gh = new GridBagHelper(this);

    // Checkbox for known props
    rbChoose = new JRadioButton(resources.getString("choose.known"),defs.length>0);
    rbChoose.setEnabled(defs.length>0);
    rbChoose.addItemListener(this);
    rbChoose.setAlignmentX(0);
    gh.add(rbChoose,1,1,2,1, gh.GROWFILL_HORIZONTAL);

    // .. List of tags
    lChoose = new JList(defs);
    lChoose.setVisibleRowCount(4);
    lChoose.setEnabled(defs.length>0);
    lChoose.setCellRenderer(new MetaDefRenderer());
    lChoose.addListSelectionListener(this);
    JScrollPane sp = new JScrollPane(lChoose);
    // 20030527 grrrrrrr why is this necessary
    sp.setMinimumSize(sp.getPreferredSize());
    gh.add(sp,1,2,1,1,gh.GROWFILL_VERTICAL);

    // .. Info field
    tpInfo = new JTextPane();
    tpInfo.setText("");
    tpInfo.setEditable(false);
    spInfo = new JScrollPane(tpInfo);
    gh.add(spInfo,2,2,1,1,gh.GROWFILL_BOTH);

    // RadioButton for new props
    rbNew = new JRadioButton(resources.getString("choose.new"),defs.length==0);
    rbNew.addItemListener(this);
    rbNew.setAlignmentX(0);
    gh.add(rbNew,1,3,2,1, gh.GROWFILL_HORIZONTAL);

    ButtonGroup group = new ButtonGroup();
    group.add(rbChoose);
    group.add(rbNew);

    // Create Lower Part
    tfNew = new JTextField();
    tfNew.setEnabled(defs.length==0);
    tfNew.setAlignmentX(0);
    gh.add(tfNew,1,4,2,1, gh.GROWFILL_HORIZONTAL);

    // Pre select
    if (defs.length>0) {
      lChoose.setSelectedIndex(0);
    }
    
    // Done
  }
  
//  /**
//   * Helper method that adds components to a container with gridbaglayout
//   */
//  private void add(Component c,int x,int y,int w,int h,boolean grow) {
//    add(c);
//
//    GridBagConstraints s = new GridBagConstraints();
//    s.gridx = x;
//    s.gridy = y;
//    s.gridwidth = w;
//    s.gridheight= h;
//    s.weightx = grow ? 1 : 0;
//    s.weighty = grow ? 1 : 0;
//    s.fill = grow ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
//
//    ((GridBagLayout)getLayout()).setConstraints(c,s);
//  }

  /**
   * Returns the resulting properties
   */
  public Property[] getResultingProperties() {

    Property[] result = null;

    // list of selected properties
    if (rbChoose.isSelected() == true) {
      Object[] objs = lChoose.getSelectedValues();
      result = new Property[objs.length];
      for (int i=0;i<objs.length;i++) {
        result[i] = ((MetaProperty)objs[i]).create("");
      }
      return result;
    }
    
    // single entered property
    if (tfNew.getText().equals("")) 
      return new Property[0];

    result = new Property[1];
    result[0] = MetaProperty.get(parent, tfNew.getText()).create("");
    
    // done
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
    MetaProperty meta = (MetaProperty)selection[selection.length-1];
    tpInfo.setText(meta.getInfo());
    if (!rbChoose.isSelected())
      rbChoose.doClick();

    // Done
  }

  /**
   * Tag List Cell Renderer
   */
  class MetaDefRenderer extends DefaultListCellRenderer implements ListCellRenderer {

    /**
     * Return component for rendering list element
     */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
      super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        MetaProperty def = (MetaProperty)value;
        setText(def.getTag());
        setIcon(def.getImage());
      return this;
    }

  } //MetaDefRenderer

} //ChoosePropertyBean

