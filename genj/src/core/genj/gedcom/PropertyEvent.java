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
 * Gedcom Property : EVENT
 */
public class PropertyEvent extends Property {
  
  /** our Tag */
  private String tag;
  
  /** whether the event is known to have happened */
  private boolean knownToHaveHappened;

  /**
   * Patching default sub meta properties
   */
  public MetaProperty[] getSubMetaProperties(int filter) {
    
    MetaProperty[] result = super.getSubMetaProperties(filter);
    
    // this RESIdence and default?
    if (filter==MetaProperty.FILTER_DEFAULT&&getTag().equals("RESI")) {
      // patch defaults for RESIdence event
      for (int i = 0; i < result.length; i++) {
        // look for PLAC
        if (!result[i].getTag().equals("PLAC"))
          continue;
        // patch it
        result[i] = getMetaProperty().get("ADDR", false); 
        // done
        break;
      }
      // patched 
    }
    
    // done
    return result;
  }
  
  /**
   * Returns the date of the event
   */
  public PropertyDate getDate() {
    return getDate(true);
  }

  /**
   * Returns the date of the event
   * @param valid specifies wether data has to be valid to be found
   */
  public PropertyDate getDate(boolean valid) {

    // Try to get date-property which is valid
    Property prop = getProperty("DATE",valid);
    if (prop==null) 
      return null;

    // Return as Date
    return (PropertyDate)prop;
  }

  /**
   * Calculate event's date
   */
  public String getDateAsString() {
    Property date = getProperty("DATE");
    return date!=null ? date.getValue() : EMPTY_STRING;
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    return "Event";
  }

  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return tag;
  }

  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  /*package*/ Property init(MetaProperty meta, String value) throws GedcomException {
    // remember tag
    tag = meta.getTag();
    // remember Y
    if (value.toLowerCase().equals("y"))
      knownToHaveHappened = true;
    // continue with super 
    return super.init(meta,value);
  }

  /**
   * Returns the value of this property
   */
  public String getValue() {
    return knownToHaveHappened ? "Y" : EMPTY_STRING;
  }

  /**
   * Sets the value of this property
   */
  public void setValue(String value) {
    setKnownToHaveHappened(value.toLowerCase().equals("y"));
  }

  /**
   * Returns the list of paths which identify PropertyEvents
   */
  public static TagPath[] getTagPaths() {
    return MetaProperty.getPaths(null, PropertyEvent.class);  
  }
  
  /**
   * Access - whether this event is known to have happened
   * @return null if this attribute is not supported, true or false otherwise
   */
  public Boolean isKnownToHaveHappened() {
    // patch - no known for RESIdence and EVEN
    if (getTag().equals("RESI")||getTag().equals("EVEN"))
      return null;
    return new Boolean(knownToHaveHappened);
  }

  /**
   * Access - whether this event is known to have happened
   */
  public void setKnownToHaveHappened(boolean set) {
    String old = getValue();
    knownToHaveHappened = set;
    propagateChange(old);
  }

// Could do an automatic 'y' here but that would pollute
// the gedcom data unnecessary, no?
//  
//  /**
//   * @see genj.gedcom.Property#changeNotify(genj.gedcom.Property, int)
//   */
//  void changeNotify(Property prop, int status) {
//    // continue upwards 
//    super.changeNotify(prop, status);
//    // update known state
//    if (status!=Change.PDEL && prop instanceof PropertyDate) {
//      if (((PropertyDate)prop).isValid()) setKnownToHaveHappened(true);
//    }
//    // done
//  }


} //PropertyEvent
