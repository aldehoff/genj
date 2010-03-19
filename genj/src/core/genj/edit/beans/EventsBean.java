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

import genj.common.AbstractPropertyTableModel;
import genj.common.PropertyTableWidget;
import genj.edit.BeanPanel;
import genj.edit.ChoosePropertyBean;
import genj.edit.Images;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.TagPath;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.view.SelectionSink;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A complex bean displaying events of an individual or family
 */
public class EventsBean extends PropertyBean implements SelectionSink {

  private static TagPath[] COLUMNS = {
    new TagPath("."),
    new TagPath("."),
    new TagPath(".:DATE"),
    new TagPath(".:PLAC")
  };

  private Set<Property> deletes = new HashSet<Property>();
  private Set<Property> adds = new HashSet<Property>();
  
  private Model model;
  private PropertyTableWidget table;
  
  private BeanPanel beans = new BeanPanel() {
    private Dimension minPreferredSize = new Dimension();
    public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      minPreferredSize.width = Math.max(minPreferredSize.width, d.width);
      minPreferredSize.height = Math.max(minPreferredSize.height, d.height);
      return minPreferredSize;
    }
  };
  private List<Action> actions = new ArrayList<Action>();
  
  public EventsBean() {
    
    // prepare a simple table
    table = new PropertyTableWidget();
    table.setVisibleRowCount(5);
    table.setColSelection(-1);
    table.setRowSelection(ListSelectionModel.SINGLE_SELECTION);
    
    beans.setBorder(BorderFactory.createEmptyBorder(8,8,0,0));
    
    actions.add(new Add());
    actions.add(new Del());
    
    setLayout(new BorderLayout());
    add(BorderLayout.SOUTH, beans);
    add(BorderLayout.CENTER, table);

  }
  
  @Override
  public List<Action> getActions() {
    return actions;
  }
  
  @Override
  public void removeNotify() {
    REGISTRY.put("eventcols", table.getColumnLayout());
    super.removeNotify();
  }
  
  @Override
  public void fireSelection(Context context, boolean isActionPerformed) {
    
    // event being selected?
    Property prop = context.getProperty();
    while (prop!=null && prop.getParent()!=getProperty())
      prop = prop.getParent();

    // blank out if not editable
    if (prop!=null) {
      for (PropertyBean bean : session) {
        if (bean.property!=null && prop.contains(bean.property)) {
          prop=null;
          break;
        }
      }
    }    

    // set it
    beans.setRoot(prop);
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

    deletes.clear();
    adds.clear();
    
    model = prop==null ? null : new Model(prop);
    
    table.setModel(model);
    table.setColumnLayout(REGISTRY.get("eventcols",""));
  }
  
  private class Model extends AbstractPropertyTableModel {
    
    private List<Property> events = new ArrayList<Property>();
    
    Model(Property root) {
      
      super(root.getGedcom());
      
      // scan for events
      for (Property child : root.getProperties()) {

        if (!isEvent(child.getMetaProperty()))
          continue;
        
        // keep
        events.add(child);
      }
      
      // done
    }
    
    private List<Property> getEvents(int[] indices) {
      List<Property> result = new ArrayList<Property>(indices.length);
      for (int i=0;i<indices.length;i++)
        result.add(events.get(indices[i]));
      return result;
    }
    
    private void remove(Property event) {
      for (int i=0;i<events.size();i++) {
        if (events.get(i)==event) {
          events.remove(i);
          fireRowsDeleted(i, i);
          return;
        }
      }
      throw new IllegalArgumentException("no such event to remove");
    }
    
    @Override
    public String getColName(int col) {
      if (col==0)
        return Gedcom.getName("EVEN");
      if (col==1)
        return RESOURCES.getString("even.detail");
      return super.getColName(col);
    }
    
    @Override
    public int getNumCols() {
      return COLUMNS.length;
    }

    @Override
    public int getNumRows() {
      return events.size();
    }

    @Override
    public TagPath getColPath(int col) {
      return COLUMNS[col];
    }

    @Override
    public Property getRowRoot(int row) {
      return events.get(row);
    }
    
    @Override
    public int compare(Property valueA, Property valueB, int col) {
      if (col==0) 
        return valueA.getPropertyName().compareTo(valueB.getPropertyName());
      if (col==1)
        return detail(valueA).compareTo(detail(valueB));
      return super.compare(valueA, valueB, col);
    }
    
    private String detail(Property prop) {
      String val = prop.getDisplayValue();
      return val.length()>0 ? val : prop.getPropertyValue("TYPE");
    }
    
    @Override
    public String getCellValue(Property property, int row, int col) {
      if (col==0) 
        return property.getPropertyName();
      if (col==1)
        return detail(property);
      return super.getCellValue(property, row, col);
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
      
      MetaProperty[] metas = getProperty().getNestedMetaProperties(MetaProperty.WHERE_NOT_HIDDEN | MetaProperty.WHERE_CARDINALITY_ALLOWS);
      List<MetaProperty> choices = new ArrayList<MetaProperty>(metas.length);
      for (MetaProperty meta : metas) {
        if (isEvent(meta))
          choices.add(meta);
      }
      ChoosePropertyBean choose = new ChoosePropertyBean(choices.toArray(new MetaProperty[choices.size()]));
      choose.setSingleSelection(true);
      if (0!=DialogHelper.openDialog(getTip(), DialogHelper.QUESTION_MESSAGE, 
          choose, Action2.okCancel(), EventsBean.this))
        return;
      
      // changed
      EventsBean.this.changeSupport.fireChangeEvent();
      
    }
  } //Add

  /**
   * del an event
   */
  private class Del extends Action2 implements ListSelectionListener {
    Del() {
      setImage(PropertyEvent.IMG.getOverLayed(Images.imgDel));
      setTip(RESOURCES.getString("even.del"));
      table.addListSelectionListener(this);
      setEnabled(false);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
      Property row = table.getSelectedRow();
      setEnabled(row!=null);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      
      Property event = table.getSelectedRow();
      
      if (0!=DialogHelper.openDialog(getTip(), DialogHelper.QUESTION_MESSAGE, 
          RESOURCES.getString("even.del.confirm", event),
          Action2.okCancel(), EventsBean.this))
        return;
      
      beans.setRoot(null);
      
      model.remove(event);
      deletes.add(event);
      
      // changed
      EventsBean.this.changeSupport.fireChangeEvent();
      
    }
  } //Del
}
