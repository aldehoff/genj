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
package genj.common;

import genj.gedcom.GedcomListener;
import genj.gedcom.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * A default base-type for property models
 */
public abstract class AbstractPropertyTableModel implements PropertyTableModel, GedcomListener {
  
  private List listeners = new ArrayList(3);

  /** 
   * Add listener
   */
  public void addListener(PropertyTableModelListener listener) {
    listeners.add(listener);
    if (listeners.size()==1)
      getGedcom().addGedcomListener(this);
  }
  
  /** 
   * Remove listener
   */
  public void removeListener(PropertyTableModelListener listener) {
    listeners.remove(listener);
    if (listeners.isEmpty())
      getGedcom().removeGedcomListener(this);
  }
  
  /**
   * Structure change
   */
  protected void fireRowsChanged() {
    for (int i=0;i<listeners.size();i++)
      ((PropertyTableModelListener)listeners.get(i)).handleRowsChange(this);
  }
  
  /**
   * Structure change
   */
  protected void fireContentChanged() {
    for (int i=0;i<listeners.size();i++)
      ((PropertyTableModelListener)listeners.get(i)).handleContentChange(this);
  }

  /**
   * Gedcom callback
   */
  public void handleChange(Transaction tx) {
    fireRowsChanged();
  }
  
}
