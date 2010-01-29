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
import java.util.List;


/**
 * Class for encapsulating multimedia entry in a gedcom file
 */
public class Media extends Entity {
  
  private final static TagPath
    TITLE55 = new TagPath("OBJE:TITL"),
    TITLE551 = new TagPath("OBJE:FILE:TITL");
  
  private TagPath titlepath = TITLE55;

  /**
   * Title ...
   */
  @Override
  protected String getToStringPrefix(boolean showIds) {
    return getTitle();
  }
  
  /**
   * Overriden - special case for file association
   */
  public boolean addFile(File file) {
    List<PropertyBlob> pfiles = getProperties(PropertyBlob.class);
    PropertyBlob pfile;
    if (pfiles.isEmpty()) {
      pfile = (PropertyBlob)addProperty("BLOB", "");
    } else {
      pfile = (PropertyBlob)pfiles.get(0);
    }
    // keep it
    return pfile.addFile(file);
  }

  /**
   * Returns the property file for this OBJE
   */
  public PropertyFile getFile() {
    Property file = getProperty("FILE", true);
    return (file instanceof PropertyFile) ? (PropertyFile)file : null;    
  }
  
  /**
   * Returns the title of this OBJE
   */
  public String getTitle() {
    Property title = getProperty(titlepath);
    return title==null ? "" : title.getValue();
  }
  
  @Override
  void addNotify(Gedcom ged) {
    super.addNotify(ged);
    
    if (getMetaProperty().allows("TITLE"))
      titlepath = TITLE55;
    else
      titlepath = TITLE551;
      
  }

} //Media
