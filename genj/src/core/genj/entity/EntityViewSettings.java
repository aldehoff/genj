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
package genj.entity;

import genj.gedcom.Gedcom;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintEditor;
import genj.util.ActionDelegate;
import genj.view.ApplyResetSupport;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;


/**
 * The settings editor for the EntityView
 */
public class EntityViewSettings extends JPanel implements ApplyResetSupport {
  
  /** a drop-down for available entities */
  private JComboBox dropEntities = new JComboBox();
  
  /** the entity view */
  private EntityView entityView; 
  
  /** the scheme editor */
  private BlueprintEditor editor;
  
  /**
   * Constructor
   */
  public EntityViewSettings(EntityView view) {
    
    // keep the view
    entityView = view;
    
    // get entities
    for (int i=0;i<Gedcom.NUM_TYPES;i++) {
      dropEntities.addItem(wrap(i));
    }
    dropEntities.setRenderer(new BasicComboBoxRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        value = Gedcom.getNameFor(unwrap(value),true);
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });
    dropEntities.addActionListener((ActionListener)new ActionSelect().as(ActionListener.class));
    
    // prepare a scheme editor
    editor = new BlueprintEditor();
    editor.set(view.gedcom, new Blueprint());
    
    // do the layout
    setLayout(new BorderLayout());
    add(dropEntities, BorderLayout.NORTH);
    add(editor, BorderLayout.CENTER);    
    
    // done
  }
  
  /**
   * Helper that wraps an entity type
   */
  private Integer wrap(int type) {
    return new Integer(type);
  }
  
  /**
   * Helper that unwraps an entity type
   */
  private int unwrap(Object type) {
    return ((Integer)type).intValue();
  }

  /**
   * @see genj.app.ViewSettingsWidget#apply()
   */
  public void apply() {
    // entityView.setHtml(, edi.getText());
  }

  /**
   * @see genj.app.ViewSettingsWidget#reset()
   */
  public void reset() {
    Blueprint s = new Blueprint();
    s.setType(dropEntities.getSelectedIndex());
    s.setHTML(entityView.getHtml(dropEntities.getSelectedIndex()));
    editor.set(entityView.gedcom, s);
    // FIXME textHtml.setText(entityView.getHtml(unwrap(dropEntities.getSelectedItem())));
  }

  /**
   * Action - selection of an entity type
   */
  private class ActionSelect extends ActionDelegate {
    /** @see genj.util.ActionDelegate#execute() */
    protected void execute() {
      reset();
    }
  } //ActionSelect

} //EntityViewSettings
