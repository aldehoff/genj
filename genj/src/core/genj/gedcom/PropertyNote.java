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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Gedcom Property : NOTE (entity/property)
 * A property that either consists of NOTE information or
 * refers to a NOTE entity
 */
public class PropertyNote extends PropertyXRef {

  /** the note's content */
  private String note;

  /**
   * Member class for iterating through note's lines
   */
  private class NoteLineIterator implements Property.LineIterator {

    private StringTokenizer tokens;

    /**
     * Constructor which inits tokens
     */
    NoteLineIterator() {
      tokens = new StringTokenizer(note != null ? note : "","\n");
    }

    /**
     * Returns wether this iterator has more lines
     */
    public boolean hasMoreValues() {
      return tokens.hasMoreTokens();
    }

    /**
     * Returns the next line of this iterator
     */
    public String getNextValue() throws NoSuchElementException {
      return tokens.nextToken();
    }

    // EOC
  }

  /**
   * Constructor with reference
   * @param entity reference of entity this property links to
   */
  public PropertyNote(PropertyXRef target) {
    super(target);
    setValue("");
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
   * Returns a LineIterator which can be used to iterate through
   * several lines of this address
   */
  public LineIterator getLineIterator() {
    // iterate
    return new NoteLineIterator();
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    // if entity stay entity
    if (this instanceof Entity) return "Entity";
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
   * Returns this property's value
   */
  public String getValue() {

    // entity?
    if (this instanceof Entity) return super.getValue();
    
    // valid xref?
    if (getReferencedEntity()!=null) return super.getValue();
    
    // Note!
    if (note==null) return "";
    
    int pos = note.indexOf('\n');
    if (pos<0) return note;
    return note.substring(0,pos)+"...";
    
  }

  /**
   * This property incorporates several lines with newlines
   */
  public int isMultiLine() {
    // not if we're an enity
    if (this instanceof Entity) return NO_MULTI;
    // sure bring it on!
    return MULTI_NEWLINE;
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when processing link would result in inconsistent state
   */
  public void link() throws GedcomException {

    // No Property Note?
    if (note!=null) {
      return;
    }

    // Get enclosing entity ?
    Entity entity = getEntity();

    // .. Me Note-Property or -Entity?
    if (this==entity) {
      return;  // outa here
    }

    // Something to do ?
    if (getReferencedEntity()!=null) {
      return;
    }

    // Look for Note
    String id = getReferencedId();
    if (id.length()==0) {
      return;
    }

    Note note = (Note)getGedcom().getEntity(id, Gedcom.NOTES);
    if (note == null) {
        throw new GedcomException(toString()+" not in this gedcom");
    }

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    note.addForeignXRef(fxref);

    // ... and point
    setTarget(fxref);

    // Are there any properties that can be deleted ?
    delAllProperties();

    // Done
  }

  /**
   * Set's this property's value
   */
  public boolean setValue(String v) {

    // trim it
    v = v.trim();

    // we're no entity -> can keep value as is
    if (!(this instanceof Entity)) {
      // might be a reference
      if ( v.startsWith("@") && v.endsWith("@") ) {
        note=null;
        super.setValue(v);
        return true;
      }
      // keep value
      note = v;
      return true;
    }

    // 20021113 we're an entity -> add it as sub-property
    if (v.length()>0)
      getSubNote(true).setValue(v);

    // mark modified
    noteModifiedProperty();

    // Done
    return true;
  }

  /**
   * Returns this property as a string
   */
  public String toString() {

    // no entity? 
    if (!(this instanceof Entity)) {
      if (getReferencedEntity()!=null)
        return getReferencedEntity().toString();
      return note;
    }

    // we're an entity
    PropertyNote sub = getSubNote(false);
    if (sub!=null) return sub.toString();
    
    return "";
  }
  
  /**
   * Get the first attached not   */
  private PropertyNote getSubNote(boolean create) {
    for (int i=0;i<getNoOfProperties();i++) {
      Property child = getProperty(i);
      if (child instanceof PropertyNote) {
        return (PropertyNote)child;
      }
    }
    PropertyNote result = null; 
    if (create) {
      result = new PropertyNote(null, ""); 
      addProperty(result);
    }
    return result;
  }

  /**
   * Adds default properties to this property
   */
  public Property addDefaultProperties() {
    // need a sub note for entities
    if (this instanceof Entity) 
      getSubNote(true);
    return this;
  }

  /**
   * The expected referenced type
   */
  public int getExpectedReferencedType() {
    return Gedcom.NOTES;
  }
}

