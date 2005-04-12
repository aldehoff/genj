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

import genj.gedcom.Gedcom;
import genj.gedcom.TagPath;

/**
 * A default base-type for property models
 */
public abstract class AbstractPropertyTableModel implements PropertyTableModel {

  /**
   * The default header is derived from the path in column col
   */
  public Object getHeader(int col) {
    
    TagPath path = getPath(col);
    
    // try to find a reasonable tag to display as text (that's not '.' or '*')
    String tag = path.getLast();
    for (int p=path.length()-2;!Character.isLetter(tag.charAt(0))&&p>=0;p--) 
      tag = path.get(p);
    
    // as text
    return Gedcom.getName(tag);
  }
  
  /** 
   * By default don't assume any cached state
   */
  public void reset() {
  }
  
}
