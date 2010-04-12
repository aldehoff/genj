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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.time.PointInTime;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.DateWidget;
import genj.util.swing.NestedBlockLayout;

/**
 * A data-based filter
 */
/*package*/ class DataFilter extends JPanel implements ExportFilter {
  
  private final static Registry REGISTRY = Registry.get(DataFilter.class);
  private final static Resources RESOURCES = Resources.get(DataFilter.class);
  private final static String LAYOUT = 
    "<table pad=\"8\">"+
      "<row><even/><event wx=\"1\"/></row>"+
      "<row><born/><born wx=\"1\"/></row>"+
      "<row><living cols=\"2\"/></row>"+
    "</table>";

  private DateWidget eventsBefore = new DateWidget(PointInTime.getNow()), bornbEFORE = new DateWidget(PointInTime.getNow());
  private JCheckBox living = new JCheckBox(RESOURCES.getString("data.living"), true);

  DataFilter(Gedcom gedcom) {
    
    super(new NestedBlockLayout(LAYOUT));
    
    add(new JLabel(RESOURCES.getString("data.even")));
    add(eventsBefore);
    add(new JLabel(RESOURCES.getString("data.born")));
    add(bornbEFORE);
    add(living);
    
  }
  
  public String name() {
    return RESOURCES.getString("data");
  }
  
  @Override
  public boolean veto(Property property) {
    return false;
  }

  @Override
  public boolean veto(Entity entity) {
    return false;
  }

}
