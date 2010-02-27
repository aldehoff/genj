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
import genj.edit.Images;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.TagPath;
import genj.util.swing.Action2;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A complex bean displaying events of an individual or family
 */
public class EventsBean extends PropertyBean {

  private static TagPath[] COLUMNS = {
    new TagPath("."),
    new TagPath("."),
    new TagPath(".:DATE"),
    new TagPath(".:PLAC")
  };
  
  private PropertyTableWidget table;
  
  private List<Action> actions = new ArrayList<Action>();
  
  public EventsBean() {
    
    // prepare a simple table
    table = new PropertyTableWidget() {
      @Override
      protected String getCellValue(Property property, int row, int col) {
        if (col==0)
          return property.getPropertyName();
        if (col==1) {
          String val = property.getDisplayValue();
          if (val.length()==0)
            val = property.getPropertyValue("TYPE");
          return val;
        }
        return super.getCellValue(property, row, col);
      }
    };
    table.setVisibleRowCount(5);
    
    actions.add(new Add());
    actions.add(new Del());
    
    setLayout(new BorderLayout());
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
  
  /**
   * add an event
   */
  private class Add extends Action2 {
    Add() {
      setImage(PropertyEvent.IMG.getOverLayed(Images.imgNew));
      setTip(RESOURCES.getString("even.add"));
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
      valueChanged(null);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
      setEnabled(table.getSelectedRows().length>0);
    }
  } //Del

  @Override
  protected void commitImpl(Property property) {
  }

  @Override
  protected void setPropertyImpl(Property prop) {
    if (prop==null) {
      table.setModel(null);
      return;
    }
    table.setModel(new Model(prop));
    table.setColumnLayout(REGISTRY.get("eventcols",""));
  }
  
  private class Model extends AbstractPropertyTableModel {
    
    private List<Property> events = new ArrayList<Property>();
    
    Model(Property root) {
      
      super(root.getGedcom());
      
      // scan for events
      for (Property child : root.getProperties()) {
        if (child.getMetaProperty().allows("DATE"))
          events.add(child);
      }
      
      // done
    }
    
    @Override
    public String getName(int col) {
      if (col==0)
        return Gedcom.getName("EVEN");
      if (col==1)
        return RESOURCES.getString("even.detail");
      return super.getName(col);
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
    public TagPath getPath(int col) {
      return COLUMNS[col];
    }

    @Override
    public Property getProperty(int row) {
      return events.get(row);
    }
    
  }

}
