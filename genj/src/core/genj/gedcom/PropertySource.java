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
 * Gedcom Property : SOURCE (entity/property)
 * A property that either consists of SOURCE information or
 * refers to a SOURCE entity
 */
public class PropertySource extends PropertyXRef {

  /** the source's content */
  private String source;


  /**
   * Constructor with reference
   * @param entity reference of entity this property links to
   */
  public PropertySource(PropertyXRef target) {
    super(target);
  }

  /**
   * Constructor with Tag,Value parameters
   * @param tag property's tag
   * @param value property's value
   */
  public PropertySource() {
    this(null,"");
  }

  /**
   * Constructor with Tag,Value parameters
   * @param tag property's tag
   * @param value property's value
   */
  public PropertySource(String tag, String value) {
    super(null);

    // Setup value
    setValue(value);
  }

  /**
   * Adds all default properties to this property
   */
  public void addDefaultProperties() {

    noteModifiedProperty();

    // Just add 'em
    if (this instanceof Entity) {
	addProperty(new PropertyGenericAttribute("TITL"));
    }
    // Done
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    // Entity Media ?
    if (this instanceof Entity) {
      return "Entity";
    }

    return "XRef";
  }

  /**
   * Returns the name of the proxy-object which knows properties looked
   * up by TagPath
   * @return proxy's logical name
   */
  public static String getProxy(TagPath path) {
    if (path.length()>1) {
      return "XRef";
    }
    return "Entity";
  }


  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return "SOUR";
  }


  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when processing link would result in inconsistent state
   */
  public void link() throws GedcomException {

    // No Property Source?
    if (source!=null) {
      return;
    }

    // Get enclosing entity ?
    Entity entity = getEntity();

    // .. Me Source-Property or -Entity?
    if (this==entity) {
      return;  // outa here
    }

    // Something to do ?
    if (getReferencedEntity()!=null) {
      return;
    }

    // Look for Source
    String id = getReferencedId();
    if (id.length()==0) {
      return;
    }

    Source source = getGedcom().getSourceFromId(id);
    if (source == null) {
      throw new GedcomException("Couldn't find entity with ID "+id);
    }

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    source.addForeignXRef(fxref);

    // ... and point
    setTarget(fxref);

    // don't delete anything because we may have children, like PAGE
  }

  /**
   * The expected referenced type
   */
  public int getExpectedReferencedType() {
    return Gedcom.SOURCES;
  }
}

