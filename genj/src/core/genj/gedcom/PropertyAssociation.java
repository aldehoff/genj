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

import genj.util.WordBuffer;

/**
 * Gedcom Property : ASSO
 * Property wrapping the condition of a property having an association 
 * to another entity
 */
public class PropertyAssociation extends PropertyXRef {
  
  /** available target types */
  public static final String[] TARGET_TYPES = {
    Gedcom.INDI, Gedcom.FAM, Gedcom.SUBM
  };
  
  /** our target type */
  private String targetType = Gedcom.INDI;
  
  /**
   * Empty Constructor
   */
  public PropertyAssociation() {
  }

  /**
   * Constructor with reference
   */
  public PropertyAssociation(PropertyXRef target) {
    super(target);
  }
  
  /**
   * We're trying to give a bit more information than the
   * default display value (target.getEntity().toString())
   * For example:
   *  Birth Meier, Nils (I008) 25 May 1970 Rendsburg
   * @see genj.gedcom.PropertyXRef#getDisplayValue()
   */
  public String getDisplayValue() {
    
    // find target
    PropertyXRef target = getTarget();
    if (target==null)
      return super.getDisplayValue();
    
    // check its parent
    Property parent = target.getParent();
    if (target==null)
      return super.getDisplayValue();
    
    // collect some info e.g.
    //  Meier, Nils (I008) - Birth - 25 May 1970 - Rendsburg
    WordBuffer result = new WordBuffer(" - ");
    result.append(parent.getEntity());
    
    result.append(Gedcom.getName(parent.getTag()));
    
    Property date = parent.getProperty("DATE");
    if (date!=null)
      result.append(date);
    
    Property place = parent.getProperty("PLAC");
    if (place!=null)
      result.append(place);
    
    // done
    return result.toString();
  }
  
  /**
   * @see genj.gedcom.PropertyXRef#getForeignDisplayValue()
   */
  protected String getForeignDisplayValue() {
    // do we know a relationship?
    Property rela = getProperty("RELA");
    if (rela!=null&&rela.getDisplayValue().length()>0) 
      return rela.getDisplayValue() + ": " + getEntity().toString();
    // fallback
    return super.getForeignDisplayValue();
  }
  
  /**
   * Returns a warning string that describes what happens when this
   * property would be deleted
   * @return warning as <code>String</code>, <code>null</code> when no warning
   */
  public String getDeleteVeto() {
    return resources.getString("prop.asso.veto");
  }

  /**
   * Returns the Gedcom-Tag of this property
   */
  public String getTag() {
    return "ASSO";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when property has no parent property,
   * or a double husband/wife situation would be the result
   */
  public void link() throws GedcomException {

    // linked already?
    if (getReferencedEntity()!=null) 
      return;

    // Try to find entity
    String id = getReferencedId();
    if (id.length()==0)
      return;

    Entity ent = (Entity)getGedcom().getEntity(id);
    if (ent==null) 
      throw new GedcomException("Couldnt't find individual with ID "+id);

    // Create Backlink using RELA
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    try {
      PropertyRelationship rela = (PropertyRelationship)getProperty("RELA");
      ent.getProperty(rela.getAnchor()).addProperty(fxref);
    } catch (Throwable t) {
      ent.addProperty(fxref);
    }

    // ... and point
    setTarget(fxref);

    // .. update type
    Property type = getProperty("TYPE");
    if (type==null) type = addProperty(new PropertySimpleValue("TYPE"));
    type.setValue(ent.getTag());

    // Done
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return targetType;
  }
  
  /**
   * The expected referenced type
   */
  public void setTargetType(String set) {
    targetType = set;
  }
  
} //PropertyAssociation
