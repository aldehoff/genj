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

/**
 * Gedcom Property : SOURCE 
 * A property that either consists of SOURCE information or
 * refers to a SOURCE entity
 */
public class PropertySource extends PropertyXRef implements MultiLineSupport {

  /**
   * Empty Constructor
   */
  public PropertySource() {
  }
  
  /**
   * Constructor with reference
   * @param entity reference of entity this property links to
   */
  public PropertySource(PropertyXRef target) {
    super(target);
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    // 20021113 if linked then we stay XRef
    if (super.getReferencedEntity()!=null)
      return super.getProxy();
    // multiline
    return "MLE";    
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

    // something to do ?
    if (getReferencedEntity()!=null) return;


    // Look for Source
    String id = getReferencedId();
    if (id.length()==0) return;

    Source source = (Source)getGedcom().getEntity(id, Gedcom.SOURCES);
    if (source == null)
      return;

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    source.addProperty(fxref);

    // ... and point
    setTarget(fxref);

    // done
  }

  /**
   * The expected referenced type
   */
  public int getExpectedReferencedType() {
    return Gedcom.SOURCES;
  }

  /**
   * @see genj.gedcom.PropertyXRef#isValid()
   */
  public boolean isValid() {
    // always
    return true;
  }
  
  /**
   * @see genj.gedcom.PropertyXRef#toString()
   */
  public String toString() {
    return super.getValue();
  }
  
  /**
   * @see genj.gedcom.PropertyXRef#getValue()
   */
  public String getValue() {
    return PropertyMultilineValue.getFirstLine(super.getValue());
  }

  /**
   * @see genj.gedcom.MultiLineSupport#getLines()
   */
  public Line getLines() {
    return new PropertyMultilineValue.MLLine(getTag(), super.getValue());
  }

  /**
   * @see genj.gedcom.MultiLineSupport#getLinesValue()
   */
  public String getLinesValue() {
    return super.getValue();
  }

  /**
   * @see genj.gedcom.PropertyMedia#getDefaultMetaProperties()
   */
  public MetaProperty[] getDefaultMetaProperties() {
    // no props if NOT linked
    if (getTarget()==null) return new MetaProperty[0];
    // proceed
    return super.getDefaultMetaProperties();
  }
  
  /**
   * @see genj.gedcom.PropertyMedia#getVisibleMetaProperties()
   */
  public MetaProperty[] getVisibleMetaProperties() {
    // no props if NOT linked
    if (getTarget()==null) return new MetaProperty[0];
    // proceed
    return super.getVisibleMetaProperties();
  }

} //PropertySource

