package genj.gedcom;

/**
 * A relationship between entities
 */
public abstract class Relationship {
  
  /**
   * Perform the relationship
   */
  public abstract void apply(Entity entity) throws GedcomException ;  
  
  /**
   * A name
   */
  public abstract String getName();
  
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
    
    /** target type */
    private int target;

    /** linked by ... */
    private Property owner;
    
    /** Constructor */
    public LinkedBy(Property ownr, int targt) {
      owner = ownr;
      target = targt;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      return Gedcom.getNameFor(target, false);
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return "a " + getName() + " for " + owner.getEntity().getId();
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public void apply(Entity entity) throws GedcomException {
      // must be a PropertyXRef
      if (!(entity.getProperty() instanceof PropertyXRef))
        throw new GedcomException("Can apply relationship to non-xref");

      try {   
        PropertyXRef xref = (PropertyXRef)Property.createInstance(entity.getProperty().getTag(), "");     
        owner.addProperty(xref);
        xref.setTarget((PropertyXRef)entity.getProperty());
      } catch (Throwable t) {
        t.printStackTrace();
      }      
/*
    // Create submitter on owner's end
    owner.addProperty(new PropertySubmitter(this));

    // Create source on owner's end
    owner.addProperty(new PropertySource(this));
    
    // Create repository on owner's end
    owner.addProperty(new PropertyRepository(this));

    // Create note on owner's end
    owner.addProperty(new PropertyNote(this));

    // Create media on owner's end
    owner.addProperty(new PropertyMedia(this));
*/    
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
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.child.in", family);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public void apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      family.addChild((Indi)entity);
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
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.child.of", parent);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public void apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      Fam fam = parent.getFam(true);
      fam.addChild((Indi)entity);
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
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.parent.in", family);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public void apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      Indi indi = (Indi)entity;
      family.setSpouse(indi);
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
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.parent.of", child);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public void apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      Fam fam = child.getFamc(true);
      Indi indi = (Indi)entity;
      fam.setSpouse(indi);
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
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.spouse.of", spouse);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public void apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      Fam fam = spouse.getFam(true);
      if (fam.getNoOfSpouses()>=2) { 
        fam = spouse.addFam();
      }
      fam.setSpouse((Indi)entity);
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
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return Gedcom.resources.getString("rel.sibling.of", sibling);
    }
  
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public void apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      Fam fam = sibling.getFamc(true);
      fam.addChild((Indi)entity);
    }
    
  } // SiblingOf
  
} //Relationship
