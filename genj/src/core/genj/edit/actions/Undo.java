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
package genj.edit.actions;

import genj.edit.Images;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genj.util.swing.Action2;

import java.beans.PropertyChangeListener;

import spin.Spin;

/**
 * Redo on Gedcom
 */  
public class Undo extends Action2 implements GedcomMetaListener {
  
  /** the gedcom */
  private Gedcom gedcom;
  
  /**
   * Constructor
   */
  public Undo(Gedcom gedcom) {
    this(gedcom, gedcom.canUndo());
  }

  /**
   * Constructor
   */
  public Undo(Gedcom gedcom, boolean enabled) {
    
    // setup looks
    setImage(Images.imgUndo);
    setText(AbstractChange.resources.getString("undo"));    
    setTip(getText());
    setEnabled(enabled);
    
    // remember
    this.gedcom = gedcom;
    
  }

  public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    super.addPropertyChangeListener(listener);
    // hook up to events
    if (getPropertyChangeListeners().length==1) {
      gedcom.addGedcomListener((GedcomListener)Spin.over(this));
      setEnabled(gedcom.canUndo());
    }
  }
  
  public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    super.removePropertyChangeListener(listener);
    // unhook from events
    if (getPropertyChangeListeners().length==0)
      gedcom.removeGedcomListener((GedcomListener)Spin.over(this));
  }

  /**
   * Undo changes from last transaction
   */
  protected void execute() {
    if (gedcom.canUndo())
      gedcom.undoUnitOfWork();
  }
  
  public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
    // ignored
  }

  public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
    // ignored
  }

  public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
    // ignored
  }

  public void gedcomPropertyChanged(Gedcom gedcom, Property prop) {
    // ignored
  }

  public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
    // ignored
  }

  public void gedcomHeaderChanged(Gedcom gedcom) {
    // ignored
  }

  public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    // ignored
  }
  
  public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    // ignored
  }

  public void gedcomWriteLockAcquired(Gedcom gedcom) {
    // ignored
  }

  public void gedcomWriteLockReleased(Gedcom gedcom) {
    setEnabled(gedcom.canUndo());
  }
} //Undo

