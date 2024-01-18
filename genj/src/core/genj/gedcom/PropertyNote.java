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
 * Gedcom Property : NOTE (entity/property)
 * A property that either consists of NOTE information or
 * refers to a NOTE entity
 */
public class PropertyNote extends PropertyXRef implements MultiLineSupport {

  /**
   * Constructor with reference
   * @param entity reference of entity this property links to
   */
  public PropertyNote(PropertyXRef target) {
    super(target);
  }

  /**
   * Constructor with Tag,Value parameters
   * @param tag property's tag
   * @param value property's value
   */
  public PropertyNote() {
    this(null,"");
  }

  /**
   * Constructor with Tag,Value parameters
   * @param tag property's tag
   * @param value property's value
   */
  public PropertyNote(String tag, String value) {
    super(null);
    setValue(value);
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
    return "NOTE";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when processing link would result in inconsistent state
   */
  public void link() throws GedcomException {

    // something to do ?
    if (getReferencedEntity()!=null) return;

    // Look for Note
    String id = getReferencedId();
    if (id.length()==0) return;

    Note note = (Note)getGedcom().getEntity(id, Gedcom.NOTES);
    if (note == null) 
      return;

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    note.getProperty().addProperty(fxref);

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
    return Gedcom.NOTES;
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

 
} //PropertyNote

