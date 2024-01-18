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
import java.util.List;

/**
 * Gedcom Property : MEDIA (entity)
 * Property wrapping a reference to a multiMedia object
 */
public class PropertyMedia extends PropertyXRef implements IconValueAvailable {

  /**
   * Constructor with reference
   * @param entity reference of entity this property links to
   */
  public PropertyMedia(PropertyXRef target) {
    super(target);
  }

  /**
   * Constructor
   */
  public PropertyMedia() {
    super(null,"");
  }

  /**
   * Constructor with Tag,Value parameters
   * @param tag property's tag
   * @param value property's value
   */
  public PropertyMedia(String tag, String value) {
    super(tag,value);
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    // Entity Media ?
    if (this instanceof Entity) {
      return "Entity";
    }
    // link or inline
    if (getNoOfProperties()>0) return "Empty";
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
    return "OBJE";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when processing link would result in inconsistent state
   */
  public void link() throws GedcomException {

    // Get enclosing entity ?
    Entity entity = getEntity();

    // .. Me Media-Property or -Entity?
    if (this==entity) {
      return;  // outa here
    }

    // Something to do ?
    if (getReferencedEntity()!=null) {
      return;
    }

    // Look for media
    String id = getReferencedId();
    if (id.length()==0) {
      return;
    }

    Media media = (Media)getGedcom().getEntity(id, Gedcom.MULTIMEDIAS);
    if (media==null) {
      throw new GedcomException("Couldn't find entity with ID "+id);
    }

    // Create a back-reference
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    media.getProperty().addProperty(fxref);

    // .. and point to it
    setTarget(fxref);

    // Done

  }
  
  /**
   * @see genj.gedcom.PropertyXRef#isValid()
   */
  public boolean isValid() {
    // always because might be inline instead of referenced target
    return true;
  }


  /**
   * The expected referenced type
   */
  public int getExpectedReferencedType() {
    return Gedcom.MULTIMEDIAS;
  }

  /**
   * Returns an ImgIcon if existing in one of the sub-properties
   */
  public ImageIcon getValueAsIcon() {
    List ps = super.getProperties(IconValueAvailable.class);
    return ps.isEmpty() ? null : ((IconValueAvailable)ps.get(0)).getValueAsIcon();
  }
  
  /**
   * Returns the property file for this OBJE
   */
  public PropertyFile getFile() {
    PropertyMedia target = (PropertyMedia )super.getReferencedEntity();
    if (target!=null) return target.getFile();
    return (PropertyFile)getProperty(new TagPath("OBJE:FILE"), true);    
  }
  
  /**
   * @see genj.gedcom.Property#addDefaultProperties()
   */
  public Property addDefaultProperties() {
    // no props if linked
    if (getTarget()!=null) return this;
    return super.addDefaultProperties();
  }
  
  /**
   * @see genj.gedcom.PropertyXRef#toString()
   */
  public String toString() {
    if (getTarget()!=null) return super.toString();
    return getTitle();
  }

  /**
   * Returns the title of this media
   */
  private String getTitle() {
    Property title = getProperty("OBJE:TITL");
    return title!=null ? title.getValue() : ""; 
  }

} //PropertyMedia
