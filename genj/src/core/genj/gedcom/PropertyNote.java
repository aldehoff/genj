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

import genj.util.swing.ImageIcon;


/**
 * Gedcom Property : NOTE 
 * A property that links an existing note
 */
public class PropertyNote extends PropertyXRef {

  public static final String TAG = "NOTE";

  /**
   * Empty Constructor
   */
  public PropertyNote() {
  }
  
  /**
   * Constructor with reference
   * @param target reference of property this property links to
   */
  public PropertyNote(PropertyXRef target) {
    super(target);
  }
  
  /**
   * This will be called once when instantiation has
   * happend - it's our chance to substitute this with
   * a multilinevalue if no reference applicable
   */
  /*package*/ Property init(MetaProperty meta, String value) throws GedcomException {
    // expecting NOTE
    assume("NOTE".equals(meta.getTag()), UNSUPPORTED_TAG);
    // ONLY for @..@!!!
    if (value.startsWith("@")&&value.endsWith("@"))
      return super.init(meta, value);
    // switch to multiline value
    return new PropertyMultilineValue().init(meta, value);
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
    if (getReferencedEntity()!=null) 
      return;

    // Look for Note
    String id = getReferencedId();
    if (id.length()==0) return;

    // .. ignore when not found - play inline note
    Note enote = (Note)getGedcom().getEntity(Gedcom.NOTE, id);
    if (enote==null) 
      return;

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    enote.addProperty(fxref);

    // ... and point
    setTarget(fxref);

    // Done
  }
  
  /**
   * A Note's NOTE property
   */
  public Property getTargetValueProperty() {
    Note note = (Note)getReferencedEntity();
    return note!=null ? note.getDelegate() : null;
  }
  
  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.NOTE;
  }
  
  /**
   * @see genj.gedcom.PropertyXRef#overlay(genj.util.swing.ImageIcon)
   */
  protected ImageIcon overlay(ImageIcon img) {
    // used as a reference? go ahead and overlay!
    if (super.getReferencedEntity()!=null)
      return super.overlay(img);
    // used inline! no overlay!
    return img;
  }
 
} //PropertyNote

