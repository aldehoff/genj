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
package genj.util;

import java.util.ArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A boolean state with change notifications
 */
public class ObservableBoolean implements DocumentListener {

  /** state */
  private boolean state;
  
  /** listeners */
  private ArrayList listeners = new ArrayList();

  /**
   * Accessor - Status
   */
  public boolean get() {
    return state;
  }

  /**
   * Accessor - Status
   */  
  public void set(boolean set) {
    state = set;
    broadcast();
  }
  /**
   * Trigger broadcast of state to listeners
   */
  public void broadcast() {
    ChangeEvent e = new ChangeEvent(this);
    ChangeListener[] array = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
    for (int i = 0; i < array.length; i++) {
      array[i].stateChanged(e);
    }   
  }
  
  /**
   * Add listener
   */
  public void addChangeListener(ChangeListener l) {
    listeners.add(l);
  }
  
  /**
   * Remove listener
   */
  public void removeChangeListener(ChangeListener l) {
    listeners.remove(l);
  }

  /**
   * Change notification
   */
  public void changedUpdate(DocumentEvent e) {
    set(true);
  }

  /**
   * Document event - insert
   */
  public void insertUpdate(DocumentEvent e) {
    set(true);
  }

  /**
   * Document event - remove
   */
  public void removeUpdate(DocumentEvent e) {
    set(true);
  }

} //ObservableBoolean
