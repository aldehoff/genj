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
 * Gedcom Property : SOURCE 
 * A property that links to an existing SOURCE entity
 */
public class PropertySource extends PropertyXRef {

  /** applicable target types */
  public final static String[] 
    TARGET_TYPES = { Gedcom.SOUR };

  /**
   * Empty Constructor
   */
  public PropertySource() {
  }
  
  /**
   * Constructor with reference
   * @param target reference of property this property links to
   */
  public PropertySource(PropertyXRef target) {
    super(target);
  }

  /**
   * This will be called once when instantiation has
   * happend - it's our chance to substitute this with
   * a multilinevalue if no reference applicable
   */
  /*package*/ Property init(MetaProperty meta, String value) throws GedcomException {
    // expecting NOTE
    assume("SOUR".equals(meta.getTag()), UNSUPPORTED_TAG);
    // ONLY for @..@!!!
    if (value.startsWith("@")&&value.endsWith("@"))
      return super.init(meta,value);
    // switch to multiline value
    return new PropertyMultilineValue().init(meta, value);
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

    Source source = (Source)getGedcom().getEntity(Gedcom.SOUR, id);
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
  public String[] getTargetTypes() {
    return TARGET_TYPES;
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
} //PropertySource

