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
package genj.gedcom;

import java.io.File;

/**
 * Class for encapsulating multimedia entry in a gedcom file
 */
public class Media extends Entity {

  /**
   * @see genj.gedcom.Entity#toString()
   */
  public String toString() {
    Property title = getProperty("TITL");
    return title!=null ? title.getValue() : super.toString(); 
  }

  /**
   * Returns the property file for this OBJE
   */
  public PropertyFile getFile() {
    Property file = getProperty("FILE", true);
    return (file instanceof PropertyFile) ? (PropertyFile)file : null;    
  }

  /**
   * Update TITL/FORM in sub-properties
   */
  /*package*/ static void updateSubs(Property media, String file) {
    
    // check
    if (media==null) return;
      
    // title?
    Property title = media.getProperty("TITL");
    if (title==null) {
      title = media.addProperty(new PropertySimpleValue());
    }
    title.setValue(new File(file).getName());
      
    // format?
    Property format = media.getProperty("FORM");
    if (format==null) {
      format = media.addProperty(new PropertySimpleValue()); 
    }
    format.setValue(PropertyFile.getSuffix(file));
    
    // done
  }

} //Media
