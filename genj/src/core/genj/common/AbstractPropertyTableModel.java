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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.GedcomMetaListener;

import java.util.ArrayList;
import java.util.List;

import spin.Spin;

/**
 * A default base-type for property models
 */
public abstract class AbstractPropertyTableModel implements PropertyTableModel {
  
  private List listeners = new ArrayList(3);
  private Gedcom gedcom = null;
  private GedcomListener callback;
  
  private class Callback extends GedcomListenerAdapter implements GedcomMetaListener {
    
    private boolean bigChange = false;
    
    public void gedcomEntityAdded(Gedcom gedcom, genj.gedcom.Entity entity) {
      bigChange = true;
    }
    
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      bigChange = true;
    }
    
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
      bigChange = false;
    }
    
    public void gedcomWriteLockReleased(Gedcom gedcom) {
      if (bigChange)
        fireStructureChanged();
      else
        fireRowsChanged(0, getNumRows());
    }
  }
  
  /**
   * the gedcom listener to use
   */
  protected GedcomListener getGedcomListener() {
    return new Callback();
  }
  
  /** 
   * Add listener
   */
  public void addListener(PropertyTableModelListener listener) {
    listeners.add(listener);
    if (listeners.size()==1) {
      // cache gedcom now
      if (gedcom==null) gedcom=getGedcom();
      // and start listening (make sure events are spin over to the EDT)
      if (callback==null)
        callback = (GedcomListener)Spin.over(getGedcomListener());
      gedcom.addGedcomListener(callback);
    }
  }
  
  /** 
   * Remove listener
   */
  public void removeListener(PropertyTableModelListener listener) {
    listeners.remove(listener);
    // stop listening
    if (listeners.isEmpty())
      gedcom.removeGedcomListener(callback);
  }
  
  /**
   * Column name
   */
  public String getName(int col) {
    return getPath(col).getName();    
  }
  
  /**
   * Structure change
   */
  protected void fireRowsChanged(int start, int end) {
    for (int i=0;i<listeners.size();i++)
      ((PropertyTableModelListener)listeners.get(i)).handleRowsChanged(this, start, end);
  }
  
  /**
   * Structure change
   */
  protected void fireStructureChanged() {
    for (int i=0;i<listeners.size();i++)
      ((PropertyTableModelListener)listeners.get(i)).handleStructureChanged(this);
  }

}
