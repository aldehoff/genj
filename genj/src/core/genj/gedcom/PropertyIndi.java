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

import java.util.Date;

import genj.util.*;

/**
 * Gedcom Property : INDI (entity)
 * Class for encapsulating a person as property
 */
public abstract class PropertyIndi extends Property{

  /**
   * Adds all default properties to this property
   */
  public void addDefaultProperties() {
    addProperty(new PropertyName());
    addProperty(new PropertySex(Gedcom.MALE));
    addProperty(new PropertyEvent("BIRT"));
    addProperty(new PropertyEvent("DEAT"));
  }

  /**
   * Compares this property to another property
   * @return -1 this < property <BR>
   *          0 this = property <BR>
   *          1 this > property
   */
  public int compareTo(Property p) {
    try {
      return
        Integer.parseInt(getEntity().getId().substring(1))
        -
        Integer.parseInt(p.getEntity().getId().substring(1));
    } catch (Exception e) {
    }
    return getEntity().getId().compareTo(p.getEntity().getId());
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    return "Entity";
  }

  /**
   * Returns the name of the proxy-object which knows properties looked
   * up by TagPath
   * @return proxy's logical name
   */
  public static String getProxy(TagPath path) {
    return "Entity";
  }

  /**
   * Accessor for Tag
   */
  public String getTag() {
    return "INDI";
  }

  /**
   * Accessor for Value
   */
  public String getValue() {
    return "";
  }

  /**
   * Accessor for Value
   */
  public boolean setValue(String value) {
    return false;
  }
  
}
