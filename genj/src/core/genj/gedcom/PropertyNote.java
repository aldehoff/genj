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

    // Setup value
    setValue(value);
  }

  /**
   * Returns a LineIterator which can be used to iterate through
   * several lines of this address
   */
  public LineIterator getLineIterator() {
    return new NoteLineIterator();
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {

    // Entity Note? Should be Entity but has to be Note to be editable :(
    if (this instanceof Entity)
      return "MLE";

    // Property XRef linked to Entity Note?
    if (super.getValue().startsWith("@") || note==null)
      return "XRef";

    // Seems to be Property Note
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
      return "MLE";

    // Property XRef linked to Entity Note - or Property Note
    return "XRef";
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

    // Note?
    if (note!=null) {
      int pos = note.indexOf('\n');
      if (pos<0)
      return note;
      return note.substring(0,pos)+"...";
    }

    if (this instanceof Entity)
      return "";

    // XRef to Note!
    return super.getValue();
  }

  /**
   * This property incorporates several lines with newlines
   */
  public int isMultiLine() {

    // Note?
    if ((note!=null)||(this instanceof Entity))
      return MULTI_NEWLINE;

    // XRef!
    return NO_MULTI;
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

    Note note = getGedcom().getNoteFromId(id);
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

    v = v.trim();

    // Hmm, reference?
    if (!(this instanceof Entity)) {

      if ( (v.length()==0) || (v.startsWith("@")) ) {
        note=null;
        super.setValue(v);
        return true;
      }
    }

    // No reference!
    super.setValue("");
    noteModifiedProperty();
    note=v;

    // Done
    return true;
  }

  /**
   * Returns this property as a string
   */
  public String toString() {

    Entity e = getReferencedEntity();
    if (e==null) {
      return emptyNotNull(note);
    }

    return super.toString();

  }

  /**
   * The expected referenced type
   */
  public int getExpectedReferencedType() {
    return Gedcom.NOTES;
  }
}

