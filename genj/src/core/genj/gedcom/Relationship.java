package genj.gedcom;

import genj.util.swing.ImageIcon;

/**
 * A relationship between entities
 */
public abstract class Relationship {
  
  /** target type individuals static */
  private final static int[]
    TARGET_INDIVIDUALS = new int[]{Gedcom.INDIVIDUALS};
  
  /** Gedcom this applies */
  protected Gedcom gedcom;
  
  /** the applicable target types */
  protected int[] targetTypes;
  
  /**
   * Constructor
   */
  protected Relationship(Gedcom ged, int[] types) {
    gedcom = ged;
    targetTypes = types;
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
    return Gedcom.getImage(getTargetTypes()[0]);
  }
  
  /**
   * A name
   */
  public abstract String getName(boolean verbose);
  
  /**
   * The target type(s)
   */
  public int[] getTargetTypes() {
    return targetTypes;
  }
  
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
    
    /** the xref we're referencing through */
    private MetaProperty mxref;
    private PropertyXRef xref;
    
    /** Constructor */
    public XRefBy(Property owner, MetaProperty mxref) {
      super(owner.getGedcom(), null);
      // keep owner      
      this.owner = owner;
      this.mxref = mxref;
      this.xref = (PropertyXRef)mxref.create("@@"); //@@ makes sure we'll get a xref for note/submitter
      // update target types
      targetTypes = xref.getTargetTypes();
      // done
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName(boolean verbose) {
      // "Note"|"Media"|"Source"|... or "Something for"
      
      // is there only one target type?
      String name;
      if (targetTypes.length==1)
        name = Gedcom.getNameFor(targetTypes[0], false);
      else // there must be a more generic translation otherwise
        name = Gedcom.resources.getString("rel.xref."+xref.getTag());
      // verbose?
      if (verbose)
        name = Gedcom.resources.getString("rel.xref.for", new String[]{name, owner.toString()});
      // done
      return name;
    }
    
    /**
     * @see genj.gedcom.Relationship.XRefBy#getImage()
     */
    public ImageIcon getImage() {
      return mxref.getImage();
    }

    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Entity apply(Entity entity) throws GedcomException {
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
    public static boolean isApplicable(MetaProperty mxref) {
      // has to be xref
      Class type = mxref.getType();
      if (!PropertyXRef.class.isAssignableFrom(type))
        return false;
      // and not one of the special kinship relationships
      if (type==PropertyFamilySpouse.class) return false;
      if (type==PropertyFamilyChild.class) return false;
      if (type==PropertyHusband.class) return false;
      if (type==PropertyWife.class) return false;
      if (type==PropertyChild.class) return false;
      // o.k.
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
      super(famly.getGedcom(), TARGET_INDIVIDUALS);
      family = famly;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName(boolean verbose ) {
      // "Child in .." or "Child"
      return verbose ? 
        Gedcom.resources.getString("rel.child.in", family) : 
        Gedcom.resources.getString("rel.child");
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
      super(parnt.getGedcom(), TARGET_INDIVIDUALS);
      parent = parnt;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName(boolean verbose) {
      // "Child of Meier, Nils" or "Child"
      return verbose ? 
        Gedcom.resources.getString("rel.child.of", parent) :
        Gedcom.resources.getString("rel.child");
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
      super(famly.getGedcom(), TARGET_INDIVIDUALS);
      family = famly;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName(boolean verbose) {
      // "Parent in .." or "Parent"
      return verbose ? 
        Gedcom.resources.getString("rel.parent.in", family) :
        Gedcom.resources.getString("rel.parent");
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
      super(chil.getGedcom(), TARGET_INDIVIDUALS);
      child = chil;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName(boolean verbose ) {
      // "Parent of Meier, Nils" or "Parent"
      return verbose ? 
        Gedcom.resources.getString("rel.parent.of", child) :
        Gedcom.resources.getString("rel.parent");
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
      super(spose.getGedcom(), TARGET_INDIVIDUALS);
      spouse = spose;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName(boolean verbose) {
      // "Spouse of Meier, Nils" or "Spouse"
      return verbose ?
        Gedcom.resources.getString("rel.spouse.of", spouse) : 
        Gedcom.resources.getString("rel.spouse");
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Entity apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      // look for fam already there
      Fam fam = spouse.getFam(true);
      if (fam.getNoOfSpouses()>=2) {
        // .. create new if necessary 
        fam = spouse.addFam();
      }
      // set its spouse
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
      super(siblng.getGedcom(), TARGET_INDIVIDUALS);
      sibling = siblng;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName(boolean verbose) {
      return verbose ? 
        Gedcom.resources.getString("rel.sibling.of", sibling) :
        Gedcom.resources.getString("rel.sibling");
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
  
} //Relationship
