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
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Transaction;
import genj.util.swing.Action2;

/**
 * Redo on Gedcom
 */  
public class Undo extends Action2 implements GedcomListener {
  
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
    setImage(Images.imgUndo);
    setText(AbstractChange.resources.getString("undo"));    
    setEnabled(enabled);
    this.gedcom = gedcom;
  }

  /**
   * Undo changes from last transaction
   */
  protected void execute() {
    if (gedcom.canUndo())
      gedcom.undo();
  }
  
  /**
   * Callback for gedcom events
   */
  public void handleChange(Transaction tx) {
    setEnabled(gedcom.canUndo());
  }
  
} //Undo

