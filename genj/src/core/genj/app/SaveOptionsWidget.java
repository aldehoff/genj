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
package genj.app;

import java.util.ArrayList;
import java.util.List;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.io.Filter;
import genj.view.FilterSupport;
import genj.view.ViewManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;

import sun.security.x509.OtherName;

/**
 * A widget for setting save options (export)
 */
/*package*/ class SaveOptionsWidget extends JTabbedPane {
  
  /** checkboxes */
  private List checkTypes = new ArrayList();
  private List checkOthers = new ArrayList();
  
  /** filters */
  private FilterSupport[] filterSupport;

  /**
   * Constructor
   */    
  /*package*/ SaveOptionsWidget(Gedcom gedcom) {
    
    // entities filter    
    Box types = new Box(BoxLayout.Y_AXIS);
    for (int t=0; t<Gedcom.NUM_TYPES; t++) {
      types.add(createCheck(Gedcom.getNameFor(t, true), true, checkTypes));
    }
    
    // others filter
    Box others = new Box(BoxLayout.Y_AXIS);
    filterSupport = (FilterSupport[])ViewManager.getInstance().getSupportFor(FilterSupport.class, gedcom);
    for (int i=0; i<filterSupport.length; i++) {
      others.add(createCheck(filterSupport[i].getFilterName(), false, checkOthers));
    }
    
    // layout
    add("Types ...", types);
    if (filterSupport.length>0) add("... part of", others);
    
    // done
  }
  
  /**
   * Create a checkbox 
   */
  private JCheckBox createCheck(String title, boolean enabled, List collection) {
    JCheckBox result = new JCheckBox(title, enabled);
    collection.add(result);
    return result;
  }
  
  /**
   * Resolve a checkbox
   */
  private boolean isChecked(List collection, int index) {
    return ((JCheckBox)collection.get(index)).isSelected();
  }
  
  /**
   * The choosen filters
   */
  public Filter[] getFilters() {
    
    // Result
    List result = new ArrayList(10);
    
    // create one for the types
    FilterByType fbt = new FilterByType();
    if (fbt.isActive()) result.add(fbt);
    
    // create one for every other
    for (int f=0; f<filterSupport.length; f++) {
      if (isChecked(checkOthers, f))
    	 result.add(filterSupport[f].getFilter());
    }
    
    // done
    return (Filter[])result.toArray(new Filter[result.size()]);
  }
  
  /**
   * Filter by type
   */
  private class FilterByType implements Filter {
    /** the enabled types */
    private boolean[] types = new boolean[Gedcom.NUM_TYPES];
    /**
     * Constructor
     */
    private FilterByType() {
      for (int t=0; t<types.length; t++) {
      	types[t] = isChecked(checkTypes, t);
      }
    }
    /**
     * whether we actually filter something
     */
    private boolean isActive() {
      for (int t=0; t<types.length; t++) {
        if (types[t]==false) return true;
      }
      return false;
    }
    /**
     * accepting only specific entity types
     * @see genj.io.Filter#accept(genj.gedcom.Entity)
     */
    public boolean accept(Entity entity) {
      return types[entity.getType()];
    }
    /**
     * accepting all properties
     * @see genj.io.Filter#accept(genj.gedcom.Property)
     */
    public boolean accept(Property property) {
      return true;
    }
  } //FilterByType

} //SaveOptionsWidget
