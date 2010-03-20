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
package genj.edit.beans;

import genj.edit.ChoosePropertyBean;
import genj.edit.Images;
import genj.gedcom.Gedcom;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyComparator;
import genj.gedcom.PropertyEvent;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/**
 * A complex bean displaying events of an individual or family
 */
public class EventsBean extends PropertyBean {

  private JTable table;
  
  public EventsBean() {
    
    // prepare a simple table
    table = new JTable(new Events(null));
    table.setPreferredScrollableViewportSize(new Dimension(32,32));
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(table));

  }
  
  @Override
  public List<? extends Action> getActions() {
    return Collections.singletonList(new Add());
  }
  
  @Override
  protected void commitImpl(Property property) {
  }
  
  private boolean isEvent(MetaProperty meta) {
    
    // crude filter
    if (!PropertyEvent.class.isAssignableFrom(meta.getType())
       && !meta.getTag().equals("RESI") 
       && !meta.getTag().equals("OCCU"))
      return false;
    
    // overedited?
    for (PropertyBean bean : session) {
      if (bean.path.contains(meta.getTag()))
        return false;
    }

    return true;
  }

  @Override
  protected void setPropertyImpl(Property prop) {
    table.setModel(new Events(prop));
  }
  
  private class Events extends AbstractTableModel {
    
    private final String[] COLS = {
      Gedcom.getName("EVEN"), 
      RESOURCES.getString("even.detail"),
      Gedcom.getName("DATE"),
      Gedcom.getName("PLAC"),
      Gedcom.getName("NOTE"),
      Gedcom.getName("SOUR", true)
    };
    
    private List<Property> rows = new ArrayList<Property>();
    
    Events(Property root) {
      
      // scan for events
      if (root!=null) for (Property child : root.getProperties()) {

        if (!isEvent(child.getMetaProperty()))
          continue;
        
        // keep
        rows.add(child);
      }
      
      Collections.sort(rows, new PropertyComparator(".:BIRT"));
      
      // done
    }
    
    @Override
    public int getColumnCount() {
      return COLS.length;
    }
    
    @Override
    public int getRowCount() {
      return rows.size();
    }
    
    @Override
    public String getColumnName(int column) {
      return COLS[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      Property event = rows.get(rowIndex);
      switch (columnIndex) {
        case 0:
          return event.getPropertyName();
        case 1:
          String val = event.getDisplayValue();
          if (val.length()>0)
            return val;
          return event.getPropertyValue("TYPE");
        case 2:
          return event.getPropertyDisplayValue("DATE");
        case 3:
          return event.getPropertyDisplayValue("PLAC");
        case 4:
          return "yes";
        case 5:
          return "no";
      }
      throw new IllegalArgumentException("no such column "+columnIndex);
    }
  }
  
  /**
   * add an event
   */
  private class Add extends Action2 {
    
    Add() {
      setImage(PropertyEvent.IMG.getOverLayed(Images.imgNew));
      setTip(RESOURCES.getString("even.add"));
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
      
      Property root = getProperty();
      
      MetaProperty[] metas = root.getNestedMetaProperties(MetaProperty.WHERE_NOT_HIDDEN | MetaProperty.WHERE_CARDINALITY_ALLOWS);
      List<MetaProperty> choices = new ArrayList<MetaProperty>(metas.length);
      for (MetaProperty meta : metas) {
        if (isEvent(meta))
          choices.add(meta);
      }
      final ChoosePropertyBean choose = new ChoosePropertyBean(choices.toArray(new MetaProperty[choices.size()]));
      choose.setSingleSelection(true);
      if (0!=DialogHelper.openDialog(getTip(), DialogHelper.QUESTION_MESSAGE, 
          choose, Action2.okCancel(), EventsBean.this))
        return;
      
      String add = choose.getSelectedTags()[0];
      
      EventsBean.this.changeSupport.fireChangeEvent(new CommitRequired(EventsBean.this));
            
    }
    
  } //Add

//  /**
//   * del an event
//   */
//  private class Del extends Action2 implements ListSelectionListener {
//    Del() {
//      setImage(PropertyEvent.IMG.getOverLayed(Images.imgDel));
//      setTip(RESOURCES.getString("even.del"));
//      table.addListSelectionListener(this);
//      setEnabled(false);
//    }
//
//    @Override
//    public void valueChanged(ListSelectionEvent e) {
//      Property row = table.getSelectedRow();
//      setEnabled(row!=null);
//    }
//    
//    @Override
//    public void actionPerformed(ActionEvent e) {
//      
//      Property event = table.getSelectedRow();
//      
//      if (0!=DialogHelper.openDialog(getTip(), DialogHelper.QUESTION_MESSAGE, 
//          RESOURCES.getString("even.del.confirm", event),
//          Action2.okCancel(), EventsBean.this))
//        return;
//
//      // hide
//      detail.removeAll();
//      panels.remove(event);
//
//      // remove
//      model.remove(event);
//      deletes.add(event);
//      
//      // changed
//      EventsBean.this.changeSupport.fireChangeEvent();
//      
//    }
//  } //Del
  
}
