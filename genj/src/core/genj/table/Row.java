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
package genj.table;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.TagPath;

/**
 * A row in our table
 */
/*package*/ class Row {

  /** the entity in row */
  private Entity entity;
  
  /** paths into entity */
  private TagPath[] paths;
  
  /** the columns (its properties) */
  private Property[] cols;
  
  /**
   * Constructor
   */
  /*package*/ Row(Entity enTity, TagPath[] paThs) {
    
    // remember entity & paths
    entity = enTity;
    paths = paThs;
    
    // done
  }
    
  /**
   * Accessor - columns
   */
  /*package*/ Property[] getColumns() {
    // not computed yet?
    if (cols==null) {    
      // grab properties
      cols = new Property[paths.length];
      for (int c=0; c<cols.length; c++) {
        cols[c] = entity.getProperty(paths[c]);
      }
    }
    // available
    return cols;    
  }
  
  /**
   * Accessor - property by index
   */
  /*package*/ Property getProperty(int col) {
    return getColumns()[col];
  }

  /**
   * Accessor - entity
   */
  /*package*/ Entity getEntity() {
    return entity;    
  }
  
  /**
   * Invalidate the row
   */
  /*package*/ void invalidate() {
    cols = null;
  }

} //Row