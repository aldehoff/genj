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

import java.util.*;
import genj.util.*;

/**
 * Gedcom Property : ?
 * A property that is not supported by GenJ out of the box
 */
public class PropertyUnknown extends Property {

  /** the unknown tag */
  String tag;

  /** the value */
  String value;

  /**
   * Constructor of unknown Gedcom-line
    */
  public PropertyUnknown(String tag, String value) {
    setValue(value);
    this.tag = tag;
  }

  /**
   * Returns some explanationary information about this property.
   * Here not known.
   */
  public String getInfo() {
    return "";
  }

  /**
   * Accessor for tag
   */
  public String getTag() {
    return tag;
  }

  /**
   * Accessor for value
   */
  public String getValue() {
    return value;
  }

  /**
   * Accessor for value
   */
  public boolean setValue(String value) {
    noteModifiedProperty();
    this.value=value;
    // Done
    return true;
  }
}
