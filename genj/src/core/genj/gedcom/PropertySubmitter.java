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

import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * Gedcom Property : SUBMITTER (entity/property)
 * A property that either consists of SUBMITTER information or
 * refers to a SUBMITTER entity
 */
public class PropertySubmitter extends PropertyXRef {

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
    setValue(value);
  }
  
  /**
   * Returns a LineIterator which can be used to iterate through
   * several lines of this submitter
   */
  public Enumeration getLineIterator() {
    // iterate
    return new StringTokenizer(super.getValue(), "\n");
  }

  /**
   * Returns this property's value cut to a first line in
   * case someone actually asks us
   */
  public String getValue() {
    String result = super.getValue();    
    int pos = result.indexOf('\n');
    if (pos>=0) result = result.substring(0,pos)+"...";
    return result;
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    // 20021113 if linked then we stay XRef
    if (super.getReferencedEntity()!=null)
      return "XRef";
    // multiline
    return "MLE";    
  }

  /**
   * Returns the name of the proxy-object which knows properties looked
   * up by TagPath
   * @return proxy's logical name
   */
  public static String getProxy(TagPath path) {

    // Entity Note? Should be Entity but has to be Note to be editable :(
    if (path.length()==1)
      return "Entity";

    // Could be XRef or MLE
    return "MLE";
  }

  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return "SUBM";
  }

  /**
   * This property incorporates several lines with newlines
   */
  public int isMultiLine() {
    // not if this is a valid linke
    if (getReferencedEntity()!=null) return NO_MULTI;
    // sure bring it on!
    return MULTI_NEWLINE;
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when processing link would result in inconsistent state
   */
  public void link() throws GedcomException {

    // something to do ?
    if (getReferencedEntity()!=null) return;

    // Look for SUBM
    String id = getReferencedId();
    if (id.length()==0) return;

    Submitter subm = (Submitter)getGedcom().getEntity(id, Gedcom.SUBMITTERS);
    if (subm == null) 
      return;

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    subm.getProperty().addProperty(fxref);

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
  
  /**
   * @see genj.gedcom.PropertyXRef#isValid()
   */
  public boolean isValid() {
    // always
    return true;
  }

} //PropertySubmitter

