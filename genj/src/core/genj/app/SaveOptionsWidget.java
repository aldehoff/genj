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
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.io.Filter;
import genj.util.Resources;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.TextFieldWidget;
import genj.view.FilterSupport;
import genj.view.ViewManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * A widget for setting save options (export)
 */
/*package*/ class SaveOptionsWidget extends JTabbedPane {
  
  /** components */
  private JCheckBox[] checkEntities = new JCheckBox[Gedcom.ENTITIES.length];
  private JCheckBox[] checkViews;
  private JTextField  textTags, textValues;
  private TextFieldWidget textPassword;
  private JComboBox   comboEncodings;
  private Resources resources = Resources.get(this);
  
  /** filters */
  private FilterSupport[] filterViews;

  /**
   * Constructor
   */    
  /*package*/ SaveOptionsWidget(Gedcom gedcom, ViewManager manager) {
    
    // Options
    Box options = new Box(BoxLayout.Y_AXIS);
    options.add(new JLabel(resources.getString("save.options.encoding")));
    comboEncodings = new ChoiceWidget(Gedcom.ENCODINGS, Gedcom.ANSEL);
    comboEncodings.setEditable(false);
    comboEncodings.setSelectedItem(gedcom.getEncoding());
    options.add(comboEncodings);
    options.add(new JLabel(resources.getString("save.options.password")));
    textPassword = new TextFieldWidget(gedcom.hasPassword() ? gedcom.getPassword() : "", 10);
    textPassword.setEditable(gedcom.getPassword()!=gedcom.PASSWORD_UNKNOWN);
    options.add(textPassword);
    
    // entities filter    
    Box types = new Box(BoxLayout.Y_AXIS);
    for (int t=0; t<Gedcom.ENTITIES.length; t++) {
      checkEntities[t] = new JCheckBox(Gedcom.getEntityName(Gedcom.ENTITIES[t], true), true);
      types.add(checkEntities[t]);
    }
    
    // property filter
    Box props = new Box(BoxLayout.Y_AXIS);
    props.add(new JLabel(resources.getString("save.options.exclude.tags")));
    textTags = new TextFieldWidget(resources.getString("save.options.exclude.tags.eg"), 10).setTemplate(true);
    props.add(textTags);
    props.add(new JLabel(resources.getString("save.options.exclude.values")));
    textValues = new TextFieldWidget(resources.getString("save.options.exclude.values.eg"), 10).setTemplate(true);
    props.add(textValues);
    
    // others filter
    Box others = new Box(BoxLayout.Y_AXIS);
    filterViews = (FilterSupport[])manager.getInstances(FilterSupport.class, gedcom);
    checkViews = new JCheckBox[filterViews.length];
    for (int i=0; i<checkViews.length; i++) {
      checkViews[i] = new JCheckBox(filterViews[i].getFilterName(), false);
      others.add(checkViews[i]);
    }
    
    // layout
    add(resources.getString("save.options"                  ), options);
    add(resources.getString("save.options.filter.entities"  ), types);
    add(resources.getString("save.options.filter.properties"), props);
    add(resources.getString("save.options.filter.views"     ), others);
    
    // done
  }

  /**
   * The choosen password
   */
  public String getPassword() {
    return textPassword.getText();
  }

  /**
   * The choosen encoding
   */
  public String getEncoding() {
    return comboEncodings.getSelectedItem().toString();
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
    private FilterProperties(Set tags, Set paths, List values) {
      this.tags = tags;
      this.paths = paths;
      this.values = (String[])values.toArray(new String[0]);
      // done
    }
    
    /**
     * Get instance
     */
    protected static FilterProperties get(String sTags, String sValues) {
      
      // calculate tags
      Set tags = new HashSet();
      Set paths = new HashSet();
      
      StringTokenizer tokens = new StringTokenizer(sTags, ",");
      while (tokens.hasMoreTokens()) {
        String s = tokens.nextToken().trim();
        if (s.indexOf(':')>0) {
          try {
            paths.add(new TagPath(s));
          } catch (IllegalArgumentException e) { 
          }
        } else {
          tags.add(s);
        }
      }
      // calculate values
      List values = new ArrayList();
      tokens = new StringTokenizer(sValues, ",");
      while (tokens.hasMoreTokens()) {
        values.add(tokens.nextToken().trim());
      }
     
      // done
      return (tags.isEmpty() && paths.isEmpty() && values.isEmpty()) ? null : new FilterProperties(tags, paths, values);
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
      // check if path is applying
      if (paths.contains(property.getPath())) return false;
      // check if value is applying
      if (property instanceof MultiLineProperty) {
        if (!accept(((MultiLineProperty)property).getLinesValue()))
          return false;
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
    private Set types = new HashSet();
    
    /**
     * Create an instance
     */
    protected static FilterByType get(JCheckBox[] checks) {
      
      FilterByType result = new FilterByType();
      
      for (int t=0; t<checks.length; t++) {
      	if (checks[t].isSelected())
          result.types.add(Gedcom.ENTITIES[t]);
      }
      return result.types.size()<Gedcom.ENTITIES.length ? result : null;
    }
    /**
     * accepting only specific entity types
     * @see genj.io.Filter#accept(genj.gedcom.Entity)
     */
    public boolean accept(Entity entity) {
      return types.contains(entity.getTag());
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
