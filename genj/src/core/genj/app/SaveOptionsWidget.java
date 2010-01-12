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
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.PointInTime;
import genj.io.Filter;
import genj.util.Resources;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.DateWidget;
import genj.util.swing.TextFieldWidget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
  
  private final static Resources RESOURCES = Resources.get(SaveOptionsWidget.class);
  
  /** components */
  private JCheckBox[] checkEntities = new JCheckBox[Gedcom.ENTITIES.length];
  private JCheckBox[] checkFilters;
  private JTextField  textTags, textMarkers;
  private TextFieldWidget textPassword;
  private JComboBox   comboEncodings;
  private JCheckBox checkFilterEmpties;
  private JCheckBox checkFilterLiving;
  private DateWidget dateEventsAfter, dateBirthsAfter;

  /**
   * Constructor
   */    
  /*package*/ SaveOptionsWidget(Gedcom gedcom) {
    
    // Options
    Box options = new Box(BoxLayout.Y_AXIS);
    options.add(new JLabel(RESOURCES.getString("save.options.encoding")));
    comboEncodings = new ChoiceWidget(Gedcom.ENCODINGS, Gedcom.ANSEL);
    comboEncodings.setEditable(false);
    comboEncodings.setSelectedItem(gedcom.getEncoding());
    options.add(comboEncodings);
    options.add(new JLabel(RESOURCES.getString("save.options.password")));
    textPassword = new TextFieldWidget(gedcom.hasPassword() ? gedcom.getPassword() : "", 10);
    textPassword.setEditable(gedcom.getPassword()!=Gedcom.PASSWORD_UNKNOWN);
    options.add(textPassword);
    
    // entities filter    
    Box types = new Box(BoxLayout.Y_AXIS);
    for (int t=0; t<Gedcom.ENTITIES.length; t++) {
      checkEntities[t] = check(Gedcom.getName(Gedcom.ENTITIES[t], true), false);
      types.add(checkEntities[t]);
    }
    
    // property filter
    Box props = new Box(BoxLayout.Y_AXIS);
    props.add(new JLabel(RESOURCES.getString("save.options.exclude.tags")));
    textTags = new TextFieldWidget(RESOURCES.getString("save.options.exclude.tags.eg"), 10).setTemplate(true);
    props.add(textTags);
    props.add(new JLabel(RESOURCES.getString("save.options.exclude.markers")));
    textMarkers = new TextFieldWidget(RESOURCES.getString("save.options.exclude.markers.eg"), 10).setTemplate(true);
    props.add(textMarkers);
    props.add(new JLabel(RESOURCES.getString("save.options.exclude.events")));
    dateEventsAfter = new DateWidget();
    dateEventsAfter.setOpaque(false);
    props.add(dateEventsAfter);
    props.add(new JLabel(RESOURCES.getString("save.options.exclude.indis")));
    dateBirthsAfter = new DateWidget();
    dateBirthsAfter.setOpaque(false);
    props.add(dateBirthsAfter);
    checkFilterLiving = check(RESOURCES.getString("save.options.exclude.living"),false);
    props.add(checkFilterLiving);
    checkFilterEmpties = check(RESOURCES.getString("save.options.exclude.empties"), false);
    props.add(checkFilterEmpties);
       
    // layout
    add(RESOURCES.getString("save.options"                  ), options);
    add(RESOURCES.getString("save.options.filter.entities"  ), types);
    add(RESOURCES.getString("save.options.filter.properties"), props);
    
    // done
  }
  
  private JCheckBox check(String text, boolean selected) {
    JCheckBox result = new JCheckBox(text, selected);
    result.setOpaque(false);
    return result;
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
    result.add(new FilterByType(checkEntities));
    
    // create one for the properties
    result.add(new FilterProperties(textTags.getText().split(","), textMarkers.getText().split(",")));
    
    // create one for events
    PointInTime eventsAfter = dateEventsAfter.getValue();
    if (eventsAfter!=null&&eventsAfter.isValid())
      result.add(new FilterEventsAfter(eventsAfter));
    
    // create one for births
    PointInTime birthsAfter = dateBirthsAfter.getValue();
    if (birthsAfter!=null&&birthsAfter.isValid())
      result.add(new FilterIndividualsBornAfter(birthsAfter));
    
    // create one for living
    if (checkFilterLiving.isSelected())
      result.add(new FilterLivingIndividuals());
        
    // create one for empties
    if (checkFilterEmpties.isSelected())
      result.add(new FilterEmpties());
    
    // done
    return (Filter[])result.toArray(new Filter[result.size()]);
  }
  
  /**
   * Filtering out empty properties
   */
  private static class FilterEmpties implements Filter {
   
    public boolean checkFilter(Property property) {
      for (int i = 0; i < property.getNoOfProperties(); i++) {
        if (checkFilter(property.getProperty(i)))
            return true;
      }
      return property.getValue().trim().length()>0;
    }
    
    public String getFilterName() {
      return toString();
    }
    
  }

  /**
   * Filter individuals if born after pit
   */
  private static class FilterIndividualsBornAfter implements Filter {
    
    private PointInTime after;
    
    /** constructor */
    private FilterIndividualsBornAfter(PointInTime after) {
      this.after = after;
    }
    
    /** callback */
    public boolean checkFilter(Property property) {
      if (property instanceof Indi) {
        Indi indi = (Indi)property;
        PropertyDate birth = indi.getBirthDate();
        if (birth!=null) return birth.getStart().compareTo(after)<0;
      }
        
      // fine
      return true;
    }
    
    public String getFilterName() {
      return toString();
    }
  }
  
  /**
   * Filter not deceased individuals
   */
  private static class FilterLivingIndividuals implements Filter {

    private FilterLivingIndividuals() {
    }

    /** callback */
    public boolean checkFilter(Property property) {
      if (property instanceof Indi) {
        return ((Indi)property).isDeceased();
      }

     // fine
     return true;
   }

   public String getFilterName() {
     return toString();
   }
 }

  /**
   * Filter properties if concerning events after pit
   */
  private static class FilterEventsAfter implements Filter {
    
    private PointInTime after;
    
    /** constructor */
    private FilterEventsAfter(PointInTime after) {
      this.after = after;
    }
    
    /** callback */
    public boolean checkFilter(Property property) {
      PropertyDate when = property.getWhen();
      return when==null || when.getStart().compareTo(after)<0;
    }
    public String getFilterName() {
      return toString();
    }
  }
  
  /**
   * Filter property by tag/value
   */
  private static class FilterProperties implements Filter {
    
    /** filter tags */
    private String[] tags;
    
    /** filter markers */
    private String[] markers;
    
    /**
     * Constructor
     */
    public FilterProperties(String[] tags, String[] markers) {
      this.tags = tags;
      this.markers = markers;
      // done
    }
        
    /**
     * @see genj.io.Filter#accept(genj.gedcom.Property)
     */
    public boolean checkFilter(Property property) {
      // check if tag is applying
      for (String tag : tags)
        if (tag.equals(property.getTag())) 
          return false;
      // check if marker is applying
      for (String marker : markers)
        if (property.getProperty(marker)!=null)
          return false;
      // in
      return true;
    }
    
    public String getFilterName() {
      return toString();
    }
  } //FilterProperty
  
  /**
   * Filter by type
   */
  private static class FilterByType implements Filter {
    
    /** the excluded types */
    private Set<String> excluded = new HashSet<String>();
    
    /**
     * Constructor
     */
    FilterByType(JCheckBox[] checks) {
      for (int t=0; t<checks.length; t++) {
      	if (checks[t].isSelected())
          excluded.add(Gedcom.ENTITIES[t]);
      }
    }
    /**
     * accepting all properties, limit to entities of parameterized types
     * @see genj.io.Filter#accept(genj.gedcom.Property)
     */
    public boolean checkFilter(Property property) {
      return !(property instanceof Entity) || !excluded.contains(property.getTag());
    }
    public String getFilterName() {
      return toString();
    }
  } //FilterByType

} //SaveOptionsWidget
