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
      return "Child";
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return getName() + " in " + family.toString(true);
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
      return "Child";
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return getName() + " of " + parent.toString();
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
  
/*  
  public static class ParentOf extends Relationship {
    private Indi child;
  }
  
  public static class SpouseOf extends Relationship {
    private Indi spouse;
  }
  
  public static class SiblingOf extends Relationship {
    private Indi sibling;
  }
*/
    
} //Relationship
