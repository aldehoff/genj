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

import genj.edit.BeanPanel;
import genj.edit.Images;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.PropertySource;
import genj.gedcom.TagPath;
import genj.gedcom.UnitOfWork;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TableWidget;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
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
    
    List<PropertySource> sources = getSources(property);
    if (!sources.isEmpty()) {
    
      final TableWidget<PropertySource> table = new TableWidget<PropertySource>();
      table.new Column(Gedcom.getName("SOUR")) {
        public Object getValue(PropertySource source) {
          return source.getTargetEntity().getId();
        }
      };
      table.new Column(Gedcom.getName("AUTH")) {
        public Object getValue(PropertySource source) {
          return source.getTargetEntity().getPropertyDisplayValue("AUTH");
        }
      };
      table.new Column(Gedcom.getName("TITL")) {
        public Object getValue(PropertySource source) {
          return source.getTargetEntity().getPropertyDisplayValue("TITL");
        }
      };
      table.new Column(Gedcom.getName("PAGE")) {
        public Object getValue(PropertySource source) {
          return source.getPropertyDisplayValue("PAGE");
        }
      };
      table.new Column("", Action2.class) {
        public Object getValue(PropertySource source) {
          return new Edit(source);
        }
      };
      table.new Column("", Action2.class) {
        public Object getValue(PropertySource source) {
          return new DelProperty(source);
        }
      };
      table.setRows(sources);
      
      GedcomListener update = new GedcomListenerAdapter() {
        @Override
        public void gedcomWriteLockReleased(Gedcom gedcom) {
          table.setRows(getSources(property));
        }
      };
      
      property.getGedcom().addGedcomListener(update);
      
      Action2[] actions = new Action2[]{ Action2.ok(), new Action2(RESOURCES.getString("link", Gedcom.getName("SOUR"))) };
      
      int rc = DialogHelper.openDialog(property.toString() + " - " + getTip(), DialogHelper.QUESTION_MESSAGE, new JScrollPane(table), actions, e);

      property.getGedcom().removeGedcomListener(update);

      if (rc<1)
        return;
      
    }

    // create new source
    CreateXReference create = new CreateXReference(property, "SOUR");
    create.actionPerformed(e);
    PropertySource source = (PropertySource)create.getReference();
    if (source!=null)
      new Edit(source).actionPerformed(e);

  }
  
  private class Edit extends Action2 {
    
    private PropertySource source;
    
    public Edit(PropertySource source) {
      setText(RESOURCES.getString("edit", Gedcom.getName("SOUR")));
      setTip(getText());
      setImage(Images.imgView);
      
      this.source = source;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      
      JPanel panel = new JPanel(new NestedBlockLayout("<col><sour gy=\"1\"/><ref/></col>"));
      
      final BeanPanel entity = new BeanPanel();
      entity.setRoot(source.getTargetEntity());
      panel.add(entity);
      
      final BeanPanel citation = new BeanPanel();
      citation.setRoot(source);
      panel.add(citation);
      
      if (0!=DialogHelper.openDialog(getText(), DialogHelper.QUESTION_MESSAGE, new JScrollPane(panel), Action2.okCancel(), e))
        return;
      
      source.getGedcom().doMuteUnitOfWork(new UnitOfWork() {
        public void perform(Gedcom gedcom) throws GedcomException {
          entity.commit();
          citation.commit();
        }
      });
          
    }
  }

}
