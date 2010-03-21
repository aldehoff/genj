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
import genj.gedcom.Grammar;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyComparator;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyNote;
import genj.gedcom.TagPath;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * A complex bean displaying events of an individual or family
 */
public class EventsBean extends PropertyBean {
  
  private final static Col[] COLS = {
      new EventCol(),
      new DetailCol(),
      new ValueCol("DATE"),
      new ValueCol("PLAC"),
      new NoteCol(),
      new SourceCol(),
      new EditCol(),
      new DelCol()
  };
  
  private final static ImageIcon 
  SOURCE = Grammar.V551.getMeta(new TagPath("SOUR")).getImage(),
  NOTE = Grammar.V551.getMeta(new TagPath("NOTE")).getImage();

  private JTable table;
  private Object commit;
  
  public EventsBean() {
    
    // prepare a simple table
    table = new JTable(new Events(null), columns());
    table.setPreferredScrollableViewportSize(new Dimension(32,32));
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(table));

  }
  
  private TableColumnModel columns() {
    DefaultTableColumnModel result = new DefaultTableColumnModel();
    for (int i=0; i<COLS.length; i++) {
      TableColumn c = new TableColumn(i);
      c.setMaxWidth(COLS[i].max);
      c.setHeaderValue(COLS[i].getName());
      result.addColumn(c);
    }
    return result;
  }
  
  @Override
  public List<? extends Action> getActions() {
    return Collections.singletonList(new Add());
  }
  
  @Override
  protected void commitImpl(Property property) {
    
    if (commit instanceof String) {
      Property event = property.addProperty((String)commit, "");
      commit = null;
      ((Events)table.getModel()).add(event);
    }
    
    if (commit instanceof Property) {
      Property event = (Property)commit;
      if (event.getParent()!=null)
        event.getParent().delProperty(event);
      ((Events)table.getModel()).del(event);
    }
    
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
    
    commit = null;
    
    table.setModel(new Events(prop));
  }
  
  private class Events extends AbstractTableModel {
    
    private List<Property> rows = new ArrayList<Property>();
    
    Events(Property root) {
      
      // scan for events
      if (root!=null) for (Property child : root.getProperties()) {

        if (!isEvent(child.getMetaProperty()))
          continue;
        
        // keep
        rows.add(child);
      }
      
      Collections.sort(rows, new PropertyComparator(".:DATE"));
      
      // done
    }
    
    void add(Property event) {
      rows.add(0, event);
      fireTableRowsInserted(0, 0);
    }
    
    void del(Property event) {
      for (int i=0;i<rows.size();i++) {
        if (rows.get(i)==event) {
          rows.remove(i);
          fireTableRowsDeleted(i, i);
          return;
        }
      }
      throw new IllegalArgumentException("no such "+event);
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return COLS[columnIndex].getType();
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
      return COLS[column].getName();
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      return COLS[columnIndex].getValue(rows.get(rowIndex));
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
      
      commit = choose.getSelectedTags()[0];
      
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

  private static abstract class Col {
    protected Class<?> type;
    protected int max = Integer.MAX_VALUE;
    Col() {
      type = String.class;
    }
    Class<?> getType() {
      return type;
    }
    String getName() {
      return "";
    }
    abstract Object getValue(Property event);
    int getMax() {
      return max;
    }
  }
  
  private static class EventCol extends Col {
    @Override
    String getName() {
      return Gedcom.getName("EVEN");
    }
    @Override
    Object getValue(Property prop) {
      return prop.getPropertyName();
    }
  }
  
  private static class ValueCol extends Col {
    private String tag, name;
    ValueCol(String tag) {
      this(tag, Gedcom.getName(tag));
    }
    ValueCol(String tag, String name) {
      this.tag = tag;
      this.name = name;
    }
    @Override
    String getName() {
      return name;
    }
    @Override
    Object getValue(Property event) {
      return event.getPropertyDisplayValue(tag);
    }
    
  }
  
  private static class DetailCol extends Col {
    @Override
    String getName() {
      return RESOURCES.getString("even.detail");
    }
    @Override
    Object getValue(Property event) {
      String val = event.getDisplayValue();
      if (val.length()>0)
        return val;
      return event.getPropertyValue("TYPE");
    }
  }
  
  private static class NoteCol extends Col {
    NoteCol() {
      type = Icon.class;
      max = Gedcom.getImage().getIconWidth();
    }
    @Override
    Object getValue(Property event) {
      for (Property note : event.getProperties("NOTE")) {
        if (note instanceof PropertyNote)
          return NOTE;
        if (note.getValue().length()>0)
          return NOTE;
      }
      return NOTE.getGrayedOut();
    }
  }
 
  private static class SourceCol extends Col {
    SourceCol() {
      type = Icon.class;
      max = Gedcom.getImage().getIconWidth();
    }
    @Override
    Object getValue(Property event) {
      for (Property source : event.getProperties("SOUR")) {
        return SOURCE;
      }
      return SOURCE.getGrayedOut();
    }
  }
  private static class EditCol extends Col {
    EditCol() {
      type = Icon.class;
      max = Gedcom.getImage().getIconWidth();
    }
    @Override
    Object getValue(Property event) {
      return Images.imgAdvanced;
    }
  }
  
  private static class DelCol extends Col {
    DelCol() {
      type = Icon.class;
      max = Gedcom.getImage().getIconWidth();
    }
    @Override
    Object getValue(Property event) {
      return Images.imgDel;
    }
  }
}
