package genj.gedcom;

import javax.swing.ImageIcon;

/**
 * A relationship between entities
 */
public abstract class Relationship {
  
  /** Gedcom this applies */
  private Gedcom gedcom;
  
  /**
   * Constructor
   */
  protected Relationship(Gedcom ged) {
    gedcom = ged;
  }
  
  /**
   * Perform the relationship
   * @return the receiver of focus (preferrably)
   */
  public abstract Entity apply(Entity entity) throws GedcomException ;  
  
  /**
   * Accessor - gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
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
   * Relationship : XRef'd by
   */
  public static class XRefBy extends Relationship {
    
    /** linked by owner */
    private Property owner;
    
    /** its sub */
    private MetaProperty sub;
    
    /** its xref */
    private PropertyXRef xref;
    
    /** Constructor */
    public XRefBy(Property owner, PropertyXRef xref) {

      super(owner.getGedcom());

      // keep owner      
      this.owner = owner;
      this.xref = xref;
      
      // done
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

    /**
     * Wether this relationship is applicable for given owner
     */
    public static boolean isApplicable(Class ownerXref) {
      if (!PropertyXRef.class.isAssignableFrom(ownerXref))
        return false;
      if (ownerXref==PropertyFamilySpouse.class) return false;
      if (ownerXref==PropertyFamilyChild.class) return false;
      if (ownerXref==PropertyHusband.class) return false;
      if (ownerXref==PropertyWife.class) return false;
      if (ownerXref==PropertyChild.class) return false;
      return true;
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
      super(famly.getGedcom());
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
      super(parnt.getGedcom());
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
      super(famly.getGedcom());
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
      super(chil.getGedcom());
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
      super(spose.getGedcom());
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
      super(siblng.getGedcom());
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
