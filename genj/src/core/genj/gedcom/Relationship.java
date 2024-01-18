package genj.gedcom;

import javax.swing.ImageIcon;

/**
 * A relationship between entities
 */
public abstract class Relationship {
  
  /**
   * Perform the relationship
   * @return the receiver of focus (preferrably)
   */
  public abstract Entity apply(Entity entity) throws GedcomException ;  
  
  /**
   * An image
   */
  public ImageIcon getImage() {
    return Gedcom.getImage(getTargetType());
  }
  
  /**
   * A name
   */
  public String getName() {
    return Gedcom.getNameFor(getTargetType(), false);    
  }
  
  /**
   * The target type
   */
  public abstract int getTargetType();
  
  /**
   * Checks type
   */
  protected void assume(Object object, Class clazz) throws GedcomException {
    if (!clazz.isAssignableFrom(object.getClass()))
      throw new GedcomException(object+" !instanceof "+clazz);
  }
  
  /**
   * Relationship : Linked by
   */
  public static class LinkedBy extends Relationship {
    
    /** linked by owner */
    private Property owner;
    
    /** its sub */
    private MetaProperty sub;
    
    /** its xref */
    private PropertyXRef xref;
    
    /** Constructor */
    private LinkedBy(Property owner, MetaProperty sub) {

      // keep owner      
      this.owner = owner;
      this.sub = sub;
      
      // create sub xref
      Property p = sub.create("");

      // keep      
      xref = (PropertyXRef)p;
      
      // done
    }
    
    /** access */
    public static LinkedBy getInstance(Property owner, MetaProperty sub) {
      // applicable?
      if (!PropertyXRef.class.isAssignableFrom(sub.getType()))
        return null;
      return new LinkedBy(owner, sub);
    }
    
    /**
     * @see genj.gedcom.Relationship#getTargetType()
     */
    public int getTargetType() {
      return xref.getExpectedReferencedType();
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return "a " + getName() + " for " + owner.getEntity().getId() 
        + "[" + TagPath.get(owner) + "]";
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Entity apply(Entity entity) throws GedcomException {
      // double check
      if (entity.getType()!=xref.getExpectedReferencedType())
        throw new GedcomException("Wrong type for apply()");
      // connect
      owner.addProperty(xref);
      xref.setValue(entity.getId());
      xref.link();
      xref.addDefaultProperties();
      //  focus stays with owner
      return owner.getEntity();
    }

  } // LinkedBy

  /**
   * Relationship : Child in
   */
  public static class ChildIn extends Relationship {
    
    /** child in ... */
    private Fam family;
    
    /** Constructor */
    public ChildIn(Fam famly) {
      family = famly;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      return Gedcom.resources.getString("rel.child");
    }
    
    /**
     * @see genj.gedcom.Relationship#getTargetType()
     */
    public int getTargetType() {
      return Gedcom.INDIVIDUALS;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.child.in", family);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Entity apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      family.addChild((Indi)entity);
      // focus stays with family
      return family;
    }
    
  } // ChildOf

  /**
   * Relationship : Child of
   */
  public static class ChildOf extends Relationship {
    
    /** child of ... */
    private Indi parent;
    
    /** Constructor */
    public ChildOf(Indi parnt) {
      parent = parnt;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      return Gedcom.resources.getString("rel.child");
    }
    
    /**
     * @see genj.gedcom.Relationship#getTargetType()
     */
    public int getTargetType() {
      return Gedcom.INDIVIDUALS;

    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.child.of", parent);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Entity apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      Fam fam = parent.getFam(true);
      fam.addChild((Indi)entity);
      // focus stays with parent
      return parent;
    }
    
  } // ChildOf

  /**
   * Relationship : Parent in
   */
  public static class ParentIn extends Relationship {
    
    /** parent in ... */
    private Fam family;
    
    /** Constructor */
    public ParentIn(Fam famly) {
      family = famly;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      return Gedcom.resources.getString("rel.parent");
    }
    
    /**
     * @see genj.gedcom.Relationship#getTargetType()
     */
    public int getTargetType() {
      return Gedcom.INDIVIDUALS;

    }
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.parent.in", family);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Entity apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      Indi indi = (Indi)entity;
      family.setSpouse(indi);
      // focus stays with family
      return family;
    }
    
  } // ParentIn

  /**
   * Relationship : parent of
   */
  public static class ParentOf extends Relationship {
    
    /** parent of ... */
    private Indi child;
    
    /** Constructor */
    public ParentOf(Indi chil) {
      child = chil;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      return Gedcom.resources.getString("rel.parent");
    }
    
    /**
     * @see genj.gedcom.Relationship#getTargetType()
     */
    public int getTargetType() {
      return Gedcom.INDIVIDUALS;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.parent.of", child);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Entity apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      Fam fam = child.getFamc(true);
      Indi indi = (Indi)entity;
      fam.setSpouse(indi);
      // focus stays with child
      return child;
    }
    
  } // ParentOf
  
  /**
   * Relationship : Spouse of
   */
  public static class SpouseOf extends Relationship {
    
    /** child of ... */
    private Indi spouse;
    
    /** Constructor */
    public SpouseOf(Indi spose) {
      spouse = spose;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      return Gedcom.resources.getString("rel.spouse");
    }
    
    /**
     * @see genj.gedcom.Relationship#getTargetType()
     */
    public int getTargetType() {
      return Gedcom.INDIVIDUALS;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.spouse.of", spouse);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Entity apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      Fam fam = spouse.getFam(true);
      if (fam.getNoOfSpouses()>=2) { 
        fam = spouse.addFam();
      }
      fam.setSpouse((Indi)entity);
      // focus stays with spouse
      return spouse;
    }
    
  } // SpouseOf

  /**
   * Relationship : Sibling Of
   */
  public static class SiblingOf extends Relationship {
    
    /** sibling of ... */
    private Indi sibling;
    
    /** Constructor */
    public SiblingOf(Indi siblng) {
      sibling = siblng;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      return Gedcom.resources.getString("rel.sibling");
    }
    
    /**
     * @see genj.gedcom.Relationship#getTargetType()
     */
    public int getTargetType() {
      return Gedcom.INDIVIDUALS;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.sibling.of", sibling);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Entity apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      Fam fam = sibling.getFamc(true);
      fam.addChild((Indi)entity);
      // focus stays with sibling
      return sibling;
    }
    
  } // SiblingOf
  
//  /**
//   * Relationship : Associated With
//   */
//  public static class AssociatedWith extends Relationship {
//    
//    /** target property */
//    private Property property;
//    
//    /** Constructor */
//    public AssociatedWith(Property prop) {
//      property = prop;
//    }
//    
//    /**
//     * @see genj.gedcom.Relationship.AssociatedWith#getName()
//     */
//    public String getName() {
//      return Gedcom.resources.getString("rel.association");
//    }
//    
//    /**
//     * @see genj.gedcom.Relationship.AssociatedWith#toString()
//     */
//    public String toString() {
//      TagPath path = new TagPath(property.getEntity().getPathTo(property));
//      return Gedcom.resources.getString("rel.association.with", new Object[]{ path, property.getEntity()});
//    }
//    
//    /**
//     * @see genj.gedcom.Relationship.AssociatedWith#apply(genj.gedcom.Entity)
//     */
//    public Entity apply(Entity entity) throws GedcomException {
//      assume(entity, Indi.class);
//      // add association
//      PropertyAssociation pa = (PropertyAssociation)MetaProperty.get(property, "ASSO").create(entity.getId());
//      property.addProperty(pa).addDefaultProperties();
//      pa.link();
//      // focus stays with entity getting the ASSO
//      return property.getEntity();
//    }
//    
//  } //AssociatedWith
  
} //Relationship
