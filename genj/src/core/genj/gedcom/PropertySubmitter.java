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
 * Gedcom Property : SUBMITTER (entity/property)
 * A property that either consists of SUBMITTER information or
 * refers to a SUBMITTER entity
 */
public class PropertySubmitter extends PropertyXRef {

  /** the submitter's content */
  private String submitter;

  /**
   * Constructor with reference
   * @param entity reference of entity this property links to
   */
  public PropertySubmitter(PropertyXRef target) {
    super(target);
  }

  /**
   * Constructor with Tag,Value parameters
   * @param tag property's tag
   * @param value property's value
   */
  public PropertySubmitter() {
    this(null,"");
  }

  /**
   * Constructor with Tag,Value parameters
   * @param tag property's tag
   * @param value property's value
   */
  public PropertySubmitter(String tag, String value) {
    super(null);

    // Setup value
    setValue(value);
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {

    // Entity Submitter? Should be Entity but has to be Submitter to be editable :(
    if (this instanceof Entity)
      return "MLE";

    // Property XRef linked to Entity Submitter?
    if (super.getValue().startsWith("@") || submitter==null)
      return "XRef";

    // Seems to be Property Submitter
    return "MLE";
  }

  /**
   * Returns the name of the proxy-object which knows properties looked
   * up by TagPath
   * @return proxy's logical name
   */
  public static String getProxy(TagPath path) {

    // Entity Submitter? Should be Entity but has to be Submitter to be editable :(
    if (path.length()==1)
      return "MLE";

    // Property XRef linked to Entity Submitter - or Property Submitter
    return "XRef";
  }

  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return "SUBM";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when processing link would result in inconsistent state
   */
  public void link() throws GedcomException {

    // No Property Submitter?
    if (submitter!=null) {
      return;
    }

    // Get enclosing entity ?
    Entity entity = getEntity();

    // .. Me Submitter-Property or -Entity?
    if (this==entity) {
      return;  // outa here
    }

    // Something to do ?
    if (getReferencedEntity()!=null) {
      return;
    }

    // Look for Submitter
    String id = getReferencedId();
    if (id.length()==0) {
      return;
    }

    Submitter submitter = getGedcom().getSubmitterFromId(id);
    if (submitter == null) {
        throw new GedcomException(toString()+" not in this gedcom");
    }

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    submitter.addForeignXRef(fxref);

    // ... and point
    setTarget(fxref);

    // Are there any properties that can be deleted ?
    delAllProperties();

    // Done
  }

  /**
   * The expected referenced type
   */
  public int getExpectedReferencedType() {
    return Gedcom.SUBMITTERS;
  }
}

