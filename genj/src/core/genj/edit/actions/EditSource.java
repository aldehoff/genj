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
package genj.edit.actions;

import genj.common.SelectEntityWidget;
import genj.edit.BeanPanel;
import genj.edit.Images;
import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.PropertySource;
import genj.gedcom.TagPath;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

/**
 * Edit note for a property
 */
public class EditSource extends Action2 {
  
  private final static Resources RESOURCES = Resources.get(EditSource.class);
  
  public final static ImageIcon 
    EDIT_SOUR = Grammar.V551.getMeta(new TagPath("SOUR")).getImage(),
    NEW_SOUR = EDIT_SOUR.getOverLayed(Images.imgNew);
  
  private Property property;
  
  /**
   * Constructor
   * @param property the property the note is for
   */
  public EditSource(Property property) {
    this.property = property;
    
    boolean has = hasSource(property);
    setImage(has ? EDIT_SOUR : NEW_SOUR);
    setText(RESOURCES.getString(has ? "edit" : "new", Gedcom.getName(Gedcom.SOUR)));
    setTip(getText());
  }
  
  public static boolean hasSource(Property property) {
    for (Property source : property.getProperties(Gedcom.SOUR)) {
      if (source instanceof PropertySource && source.isValid())
        return true;
      if (source.getValue().length()>0)
        return true;
    }
    return false;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    
    final Property source = property.getProperty("SOUR", true);

    JPanel panel = new JPanel(new NestedBlockLayout("<col><entity/><beans gy=\"1\"/></col>"));
    
    final SelectEntityWidget sources = new SelectEntityWidget(property.getGedcom(), Gedcom.SOUR, 
        RESOURCES.getString("new", Gedcom.getName(Gedcom.SOUR)));
    panel.add(sources);
    
    BeanPanel beans = new BeanPanel();
    beans.setRoot(source);
    panel.add(beans);
        
    sources.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      }
    });
          
    if (0!=DialogHelper.openDialog(property.toString() + " - " + getTip(), DialogHelper.QUESTION_MESSAGE, panel, Action2.okCancel(), e))
      return;

    // done
  }

}
