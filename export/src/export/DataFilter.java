/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
package export;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.time.PointInTime;
import genj.io.Filter;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.DateWidget;
import genj.util.swing.NestedBlockLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A data-based filter
 */
/*package*/ class DataFilter extends JPanel implements Filter {
  
  private final static Registry REGISTRY = Registry.get(DataFilter.class);
  private final static Resources RESOURCES = Resources.get(DataFilter.class);
  private final static String LAYOUT = 
    "<table pad=\"8\">"+
      "<row><even/><event wx=\"1\"/></row>"+
      "<row><born/><born wx=\"1\"/></row>"+
      "<row><living cols=\"2\"/></row>"+
      "<row><notes/></row>"+
      "<row><medias/></row>"+
      "<row><sources/></row>"+
    "</table>";

  private DateWidget eventsBefore, bornBefore;
  private JCheckBox living;
  private JCheckBox notes, medias, sources;
  
  DataFilter() {
    
    super(new NestedBlockLayout(LAYOUT));

    PointInTime tomorrow = PointInTime.getNow().add(1,0,0);
    eventsBefore = new DateWidget(tomorrow);
    try {
      eventsBefore.setValue(PointInTime.getPointInTime(REGISTRY.get("eventsbefore", (String)null)));
    } catch (Throwable t) {
    }
    bornBefore = new DateWidget(tomorrow);
    try {
      bornBefore.setValue(PointInTime.getPointInTime(REGISTRY.get("bornBefore", (String)null)));
    } catch (Throwable t) {
    }
    living = new JCheckBox(RESOURCES.getString("data.living"), true);
    living.setSelected(REGISTRY.get("living",true));

    notes = new JCheckBox(Gedcom.getName("NOTE", true), true);
    notes.setSelected(REGISTRY.get("notes",true));

    medias = new JCheckBox(Gedcom.getName("OBJE", true), true);
    medias.setSelected(REGISTRY.get("medias",true));
    
    sources = new JCheckBox(Gedcom.getName("SOUR", true), true);
    sources.setSelected(REGISTRY.get("sources",true));

    add(new JLabel(RESOURCES.getString("data.even")));
    add(eventsBefore);
    add(new JLabel(RESOURCES.getString("data.born")));
    add(bornBefore);
    add(living);

    add(notes);
    add(medias);
    add(sources);
  }
  
  @Override
  public void removeNotify() {
    
    REGISTRY.put("eventsbefore", eventsBefore.getValue().getValue());
    REGISTRY.put("bornbefore", bornBefore.getValue().getValue());
    REGISTRY.put("living", living.isSelected());
    REGISTRY.put("notes", notes.isSelected());
    REGISTRY.put("medias", medias.isSelected());
    REGISTRY.put("sources", sources.isSelected());
  
    super.removeNotify();
  }
  
  public String getName() {
    return RESOURCES.getString("data");
  }
  
  @Override
  public boolean veto(Property property) {
    
    // references are fine
    if (property instanceof PropertyXRef)
      return false;

    // no (inline) notes?
    if (!notes.isSelected()&&"NOTE".equals(property.getTag()))
      return true;
    
    // no (inline) sources?
    if (!sources.isSelected()&&"SOUR".equals(property.getTag()))
      return true;

    // property of individual (not NAME)
    Property parent = property.getParent();
    if (!"NAME".equals(property.getTag()) && parent instanceof Indi) {

      Indi indi = (Indi)parent;
      
      // no detail of living 
      if (!living.isSelected()&&!indi.isDeceased())
        return true;

      // no detail of person born after?
      if (!before(indi.getProperty("BIRT"), bornBefore.getValue()))
        return true;
      
      // no detail of events after?
      if (!before(property, eventsBefore.getValue()))
        return true;
      
    }
    
    return false;
  }
  
  private boolean before(Property property, PointInTime when) {
    if (property==null||when==null)
      return true;
    Property date = property.getProperty("DATE", true);
    if (!(date instanceof PropertyDate))
      return true;
    return ((PropertyDate)date).getEnd().compareTo(when)<0;
  }
  
  public boolean veto(Entity entity) {
    
    // no notes?
    if (!notes.isSelected()&&entity instanceof Note) 
      return true;
    
    // no medias?
    if (!medias.isSelected()&&entity instanceof Media) 
      return true;
    
    // no sources?
    if (!sources.isSelected()&&entity instanceof Source) 
      return true;

    return false;
  }

}
