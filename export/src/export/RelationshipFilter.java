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

import genj.common.SelectEntityWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.io.Filter;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;

/**
 * A relationship-based filter
 */
/*package*/ class RelationshipFilter extends JPanel implements Filter {
  
  private final static Resources RESOURCES = Resources.get(Plugin.class);
  private final static ImageIcon LEGEND = new ImageIcon(Plugin.class, "legend.png");
  private final static String LAYOUT = 
    "<col>"+
    "<row><root/><root wx=\"1\"/></row>"+
    "<img pad=\"16\"/>"+
    "<table pad=\"8\">"+
    "<row><gen/><gen/></row>"+
    "<row><gen/><gen/></row>"+
    "<row><deg/><deg/></row>"+
    "<row><deg/><deg/></row>"+
    "<inlaws cols=\"2\"/>"+
    "</table>"+
    "</col>";
  
  private JSpinner ancestors = new JSpinner(new SpinnerNumberModel(10, 0, 999, 1));
  private JSpinner descendants = new JSpinner(new SpinnerNumberModel(10, 0, 999, 1));
  private JSpinner kinship = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
  private JSpinner removal = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
  private JCheckBox inlaws = new JCheckBox(RESOURCES.getString("rela.inlaws"), true);
  private SelectEntityWidget root;
  
  private Handler handler = new Handler();

  RelationshipFilter(Gedcom gedcom) {
    
    super(new NestedBlockLayout(LAYOUT));
    
    root = new SelectEntityWidget(gedcom, "INDI", RESOURCES.getString("rela.root.select"));
    
    JLabel legend = new JLabel(LEGEND);
    legend.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    
    add(new JLabel(RESOURCES.getString("rela.root")));
    add(root);
    add(legend);
    add(new JLabel(RESOURCES.getString("rela.ancestors")));  add(ancestors);
    add(new JLabel(RESOURCES.getString("rela.descendants")));add(descendants);
    add(new JLabel(RESOURCES.getString("rela.kinship")));    add(kinship);
    add(new JLabel(RESOURCES.getString("rela.removal")));    add(removal);
    add(inlaws);

    // events
    root.addActionListener(handler);
    
    handler.actionPerformed(null);
    
  }
  
  public String getName() {
    return RESOURCES.getString("rela");
  }
  
  @Override
  public boolean veto(Property property) {
    return false;
  }

  @Override
  public boolean veto(Entity entity) {
    return false;
  }

  private class Handler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      boolean b = root.getSelection()!=null;
      ancestors.setEnabled(b);
      descendants.setEnabled(b);
      kinship.setEnabled(b);
      removal.setEnabled(b);
      inlaws.setEnabled(b);
    }
  }

}
