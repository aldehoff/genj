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
package genj.edit;

import genj.gedcom.Entity;
import genj.gedcom.PropertyXRef;
import genj.renderer.BlueprintManager;
import genj.renderer.EntityRenderer;
import genj.view.ViewManager;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A proxy for a property that links entities
 */
class ProxyXRef extends Proxy {

  /** the entity that is referenced */
  private Entity entity;
  
  /** the blueprint we're using */
  private EntityRenderer renderer;

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {
  }
  
  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return false;
  }

  /**
   * Start editing a property through proxy
   */
  protected JComponent start(JPanel in) {

    // Calculate reference information
    entity = ((PropertyXRef) property).getReferencedEntity();

    // setup content
    if (entity!=null) in.add(new Preview());
    
    // done
    return null;
  }
  
  /**
   * Our content
   */
  private class Preview extends JComponent {
    /**
     * Constructor
     */
    private Preview() {
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      addMouseListener(new MouseAdapter() {
        /**
         * @see genj.edit.ProxyXRef.Preview#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(MouseEvent e) {
          boolean sticky = view.setSticky(false);
          ViewManager.getInstance().setCurrentEntity(entity);
          view.setSticky(sticky);
         }
      });
    }
    /**
     * @see genj.edit.ProxyXRef.Content#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      if (renderer==null) 
        renderer = new EntityRenderer(BlueprintManager.getInstance().getBlueprint(entity.getType(), ""));
      renderer.render(g, entity, new Rectangle(getSize()));
    }
  } //Content

} //ProxyXRef
