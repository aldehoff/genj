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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.io.Filter;
import genj.util.swing.SwingFactory;
import genj.view.FilterSupport;
import genj.view.ViewManager;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * A widget for setting save options (export)
 */
/*package*/ class SaveOptionsWidget extends JTabbedPane {
  
  /** components */
  private JCheckBox[] checkEntities = new JCheckBox[Gedcom.NUM_TYPES];
  private JCheckBox[] checkViews;
  private JTextField textTags, textValues;
  
  /** filters */
  private FilterSupport[] filterViews;

  /**
   * Constructor
   */    
  /*package*/ SaveOptionsWidget(Gedcom gedcom) {
    
    SwingFactory create = new SwingFactory();
    
    // entities filter    
    Box types = create.Box(BoxLayout.Y_AXIS);
    for (int t=0; t<Gedcom.NUM_TYPES; t++) {
      checkEntities[t] = create.JCheckBox(Gedcom.getNameFor(t, true), true);
    }
    create.pop();
    
    // property filter
    Box props = create.Box(BoxLayout.Y_AXIS);
    create.JLabel("Exclude Tag (Paths):");
    textTags = create.JTextField("e.g. \"INDI:BIRT:NOTE, ADDR\"", true, 10);
    create.JLabel("Exclude Values containing:");
    textValues = create.JTextField("e.g. \"secret, private\"", true, 10);
    create.pop();
    
    // others filter
    Box others = create.Box(BoxLayout.Y_AXIS);
    filterViews = (FilterSupport[])ViewManager.getInstance().getSupportFor(FilterSupport.class, gedcom);
    checkViews = new JCheckBox[filterViews.length];
    for (int i=0; i<checkViews.length; i++) {
      checkViews[i] = create.JCheckBox(filterViews[i].getFilterName(), false);
    }
    
    // layout
    add("Filter by Entities", types);
    add("by Properties"   , props);
    if (filterViews.length>0) add("by View", others);
    
    // done
  }

  /**
   * The choosen filters
   */
  public Filter[] getFilters() {
    
    // Result
    List result = new ArrayList(10);
    
    // create one for the types
    FilterByType fbt = FilterByType.get(checkEntities);
    if (fbt!=null) result.add(fbt);
    
    // create one for the properties
    FilterProperties fp = FilterProperties.get(textTags.getText(), textValues.getText());
    if (fp!=null) result.add(fp);
    
    // create one for every other
    for (int f=0; f<filterViews.length; f++) {
      if (checkViews[f].isSelected())
    	 result.add(filterViews[f].getFilter());
    }
    
    // done
    return (Filter[])result.toArray(new Filter[result.size()]);
  }
  
  /**
   * Filter property by tag/value
   */
  private static class FilterProperties implements Filter {
    
    /** filter tags */
    private Set tags;
    
    /** filter paths */
    private Set paths;
    
    /** filter values */
    private String[] values;
    
    /**
     * Constructor
     */
    private FilterProperties(Set tags, List values) {
      this.tags = tags;
      this.values = (String[])values.toArray(new String[0]);
      // done
    }
    
    /**
     * Get instance
     */
    protected static FilterProperties get(String sTags, String sValues) {
      
      // calculate tags
      Set tags = new HashSet();
      StringTokenizer tokens = new StringTokenizer(sTags, ",");
      while (tokens.hasMoreTokens()) {
        tags.add(tokens.nextToken().trim());
      }
      // calculate values
      List values = new ArrayList();
      tokens = new StringTokenizer(sValues, ",");
      while (tokens.hasMoreTokens()) {
        values.add(tokens.nextToken().trim());
      }
     
      // done
      return (tags.isEmpty() && values.isEmpty()) ? null : new FilterProperties(tags, values);
    }
    
    /**
     * @see genj.io.Filter#accept(genj.gedcom.Entity)
     */
    public boolean accept(Entity entity) {
      return true;
    }

    /**
     * @see genj.io.Filter#accept(genj.gedcom.Property)
     */
    public boolean accept(Property property) {
      // check if tag is applying
      if (tags.contains(property.getTag())) return false;
      // check if value is applying
      if (property.isMultiLine()!=Property.NO_MULTI) {
        Enumeration lines = property.getLineIterator();
        while (lines.hasMoreElements()) {
          if (!accept(lines.nextElement().toString())) return false;
        }
      }
      // simple
      return accept(property.getValue());
    }
    
    /**
     * Whether we accept a value
     */
    private boolean accept(String value) {
      if (value==null) return true;
      for (int i=0; i<values.length; i++) {
        if (value.indexOf(values[i])>=0) return false;
      }
      return true;
    }

  } //FilterProperty
  
  /**
   * Filter by type
   */
  private static class FilterByType implements Filter {
    
    /** the enabled types */
    private boolean[] types;
    
    /**
     * Constructor
     */
    private FilterByType(boolean[] types) {
      this.types = types;
    }
    
    /**
     * Create an instance
     */
    protected static FilterByType get(JCheckBox[] checks) {
      boolean[] bs = new boolean[Gedcom.NUM_TYPES];
      boolean filter = false;
      for (int t=0; t<checks.length; t++) {
      	bs[t] = checks[t].isSelected();
        if (bs[t]==false) filter = true;
      }
      return filter ? new FilterByType(bs) : null;
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
