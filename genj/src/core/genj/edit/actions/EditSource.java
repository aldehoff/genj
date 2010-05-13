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
import genj.util.swing.TableWidget;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;

/**
 * Edit note for a property
 */
public class EditSource extends Action2 {
  
  private final static Resources RESOURCES = Resources.get(EditSource.class);
  
  public final static ImageIcon 
    EDIT_SOUR = Grammar.V551.getMeta(new TagPath("SOUR")).getImage(),
    NEW_SOUR = EDIT_SOUR.getOverLayed(Images.imgNew),
    NO_SOUR = EDIT_SOUR.getTransparent(128);
  
  private Property property;
  
  /**
   * Constructor
   * @param property the property the note is for
   */
  public EditSource(Property property) {
    this(property, false);
  }
  
  /**
   * Constructor
   * @param property the property the note is for
   */
  public EditSource(Property property, boolean showNone) {
    
    this.property = property;
    
    boolean has = !getSources(property).isEmpty();
    setImage(has ? EDIT_SOUR : (showNone ? NO_SOUR : NEW_SOUR));
    setText(has ? RESOURCES.getString("edit",Gedcom.getName(Gedcom.SOUR, true)) : RESOURCES.getString("new", Gedcom.getName(Gedcom.SOUR)));
    setTip(getText());
  }
  
  private List<PropertySource> getSources(Property property) {
    List<PropertySource> sources = new ArrayList<PropertySource>();
    for (Property source : property.getProperties(Gedcom.SOUR, true)) {
      if (source instanceof PropertySource)
        sources.add((PropertySource)source);
    }
    return sources;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    
//    final Property source = property.getProperty("SOUR", true);
//
//    JPanel panel = new JPanel(new NestedBlockLayout("<col><entity/><beans gy=\"1\"/></col>"));
//    
//    final SelectEntityWidget sources = new SelectEntityWidget(property.getGedcom(), Gedcom.SOUR, 
//        RESOURCES.getString("new", Gedcom.getName(Gedcom.SOUR)));
//    panel.add(sources);
//    
//    BeanPanel beans = new BeanPanel();
//    beans.setRoot(source);
//    panel.add(beans);
//        
//    sources.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//      }
//    });
    
    TableWidget<PropertySource> sources = new TableWidget<PropertySource>();
    
    sources.new Column(Gedcom.getName("SOUR")) {
      public Object getValue(PropertySource source) {
        return source.getTargetEntity().getId();
      }
    };
    
    sources.new Column(Gedcom.getName("AUTH")) {
      public Object getValue(PropertySource source) {
        return source.getTargetEntity().getPropertyDisplayValue("AUTH");
      }
    };
    
    sources.new Column(Gedcom.getName("TITL")) {
      public Object getValue(PropertySource source) {
        return source.getTargetEntity().getPropertyDisplayValue("TITL");
      }
    };
    
    sources.new Column(Gedcom.getName("PAGE")) {
      public Object getValue(PropertySource source) {
        return source.getPropertyDisplayValue("PAGE");
      }
    };
    
    sources.new Column("", Action2.class) {
      public Object getValue(PropertySource source) {
        return new Edit(source);
      }
    };
    
    sources.setRows(getSources(property));
    
    Action2[] actions = new Action2[]{ new Action2(RESOURCES.getString("link", Gedcom.getName("SOUR"))), Action2.ok() };
          
    if (0!=DialogHelper.openDialog(property.toString() + " - " + getTip(), DialogHelper.QUESTION_MESSAGE, new JScrollPane(sources), actions, e))
      return;

    // done
  }
  
  private class Edit extends Action2 {
    public Edit(PropertySource source) {
      setText(RESOURCES.getString("edit", Gedcom.getName("SOUR")));
      setTip(getText());
      setImage(Images.imgView);
    }
  }

}
