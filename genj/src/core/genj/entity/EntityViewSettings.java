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

import genj.renderer.BlueprintList;
import genj.view.ApplyResetSupport;
import java.awt.BorderLayout;

import javax.swing.JPanel;


/**
 * The settings editor for the EntityView
 */
public class EntityViewSettings extends JPanel implements ApplyResetSupport {
  
  /** the entity view */
  private EntityView entityView; 
  
  /** the scheme editor */
  private BlueprintList blueprintList;
  
  /**
   * Constructor
   */
  public EntityViewSettings(EntityView view) {
    
    // keep the view
    entityView = view;
    
    // prepare a blueprint list    
    blueprintList = new BlueprintList(view.gedcom);
    
    // do the layout
    setLayout(new BorderLayout());
    add(blueprintList, BorderLayout.CENTER);
    
    // reset
    reset();    
    
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
    entityView.setBlueprints(blueprintList.getSelection());
  }

  /**
   * @see genj.app.ViewSettingsWidget#reset()
   */
  public void reset() {
    blueprintList.setSelection(entityView.getBlueprints());
  }

} //EntityViewSettings
