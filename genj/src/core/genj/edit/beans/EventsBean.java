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
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

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
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, table);
  }

  @Override
  protected void commitImpl(Property property) {
  }

  @Override
  protected void setPropertyImpl(Property prop) {
    if (prop==null)
      table.setModel(null);
    else
      table.setModel(new Model(prop));
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
        return Gedcom.getName("TYPE");
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
