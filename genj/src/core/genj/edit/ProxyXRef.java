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
import genj.gedcom.IconValueAvailable;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.util.ActionDelegate;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.view.ViewManager;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A proxy for a property that links entities
 */
class ProxyXRef extends Proxy {

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
    PropertyXRef pxref = (PropertyXRef) property;

    // Valid link ?
    if (pxref.getReferencedEntity()==null) 
      return null;
      
    // Create a link/jump button
    Property p = pxref.getReferencedEntity();
    
    new ButtonHelper().setContainer(in).create(new ActionJump(pxref.getReferencedEntity()));
    
    // Hack to show image for referenced Blob|Image
    ImageIcon img = null;
    if (p instanceof IconValueAvailable) {
      img = ((IconValueAvailable)p).getValueAsIcon();
    }
    JComponent preview;
    if (img!=null) {
      preview = new JLabel(img);
    } else {
      preview = new JTextArea(p.toString());
      preview.setEnabled(false);
    }

    JScrollPane jsp = new JScrollPane(preview);
    jsp.setAlignmentX(0F);
    in.add(jsp);
    
    // done
    return null;
  }

  /**
   * Action - Jump to reference
   */
  private class ActionJump extends ActionDelegate {
    /** the entity to jump to */
    private Entity entity;
    /**
     * Constructor
     */
    private ActionJump(Entity e) {    
      entity = e;
      setText(resources.getString("proxy.jump_to",entity.getId()));
      setImage(e.getImage(false));
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      // get entity
      Entity target = ((PropertyXRef)property).getReferencedEntity();
      if (target!=null) {
        boolean sticky = view.setSticky(false);
        ViewManager.getInstance().setCurrentEntity(target);
        view.setSticky(sticky);
      }
    }
  } //ActionJump
  
} //ProxyXRef
