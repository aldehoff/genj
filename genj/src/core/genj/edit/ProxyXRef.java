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

import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * A proxy for a property that links entities
 */
class ProxyXRef extends Proxy implements MouseMotionListener, MouseListener {

  /** xref */
  private PropertyXRef xref;
  
  /** bottom preview */
  private Preview preview;

  /**
   * Finish editing a property through proxy
   */
  protected void commit() {
    // not if no entity - noone was listening and preview==null 20030420
    if (xref==null||xref.getReferencedEntity()==null) 
      return;
    // update looks
    setArmed(false);
    view.tree.removeMouseMotionListener(this);
    view.tree.removeMouseListener(this);
    preview.removeMouseMotionListener(this);
    preview.removeMouseListener(this);
  }
  
  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return false;
  }
  
  /**
   * Nothing to edit
   */  
  protected boolean isEditable() {
    return false;
  }

  /**
   * Start editing a property through proxy
   */
  protected Editor getEditor() {

    Editor result = new Editor();
    result.setLayout(new BorderLayout());

    // Calculate reference information
    xref = (PropertyXRef)property;

    // setup content
    if (xref.getReferencedEntity()!=null) {
      preview = new Preview(xref.getReferencedEntity());
      result.add(BorderLayout.CENTER, preview);
      
      view.tree.addMouseMotionListener(this);
      view.tree.addMouseListener(this);
      preview.addMouseMotionListener(this);
      preview.addMouseListener(this);
      
    }
    
    // done
    return result;
  }
  
  /**
   * @see genj.edit.Proxy#isClickAction()
   */
  protected boolean isClickAction() {
    return xref!=null&&xref.getReferencedEntity()!=null;
  }
  
  /**
   * Follow xref
   */
  private void jump() {
    boolean sticky = view.setSticky(false);
    view.manager.setContext(xref.getTarget());
    view.setSticky(sticky);
  }

  /** whether we're armed */  
  private boolean armed = false;
    
  /** 
   * Arm for click
   */
  private void setArmed(boolean set) {
    // set armed and update cursor
    armed = set;
    Cursor cursor = armed ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor();
    view.tree.setCursor(cursor);
    preview.setCursor(cursor);
  }
  
    
  /**
   * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
   */
  public void mouseMoved(MouseEvent e) {
    // check if we should get armed because we're hovering on prop or preview
    if (e.getSource()==preview) setArmed(true);
    else {
      Property hover = view.tree.getProperty(e.getX(), e.getY());
      setArmed(property==hover);
    }
  }
  
  /**
   * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  public void mouseDragged(MouseEvent e) {
    // ignored
  }

  /**
   * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e) {
    if (armed) {
      setArmed(false);
      jump();
    } else {
      mouseMoved(e);
    } 
  }
  
  /**
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e) {
    // ignored
  }

  /**
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e) {
    // ignored
  }

  /**
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e) {
    // ignored
  }

  /**
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e) {
    // ignored
  }

} //ProxyXRef
