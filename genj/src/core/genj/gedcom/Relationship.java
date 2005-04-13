package genj.gedcom;

import genj.util.swing.ImageIcon;

/**
 * A relationship between entities
 */
public abstract class Relationship {
  
  public static String
		LABEL_FATHER = Gedcom.resources.getString("rel.father"),
		LABEL_MOTHER = Gedcom.resources.getString("rel.mother"),
		LABEL_PARENTS = Gedcom.resources.getString("rel.parent");

  /** Gedcom this applies */
  protected Gedcom gedcom;
  
  /** the applicable target type */
  protected String targetType;
  
  /**
   * Constructor
   */
  protected Relationship(Gedcom ged, String type) {
    gedcom = ged;
    targetType = type;
  }
  
  /**
   * Perform the relationship
   * @return the receiver of focus (preferrably)
   */
  public abstract Property apply(Entity entity) throws GedcomException ;  
  
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
    return Gedcom.getEntityImage(getTargetType());
  }
  
  /**
   * A warning
   * @return a warning text for this relationship or null
   */
  public String getWarning() {
    return null;
  }
  
  /**
   * A name
   */
  public abstract String getName();
  
  /**
   * A description
   */
  public abstract String getDescription();
  
  /**
   * The target type
   */
  public String getTargetType() {
    return targetType;
  }
  
  /**
   * Checks type
   */
  protected void assume(Object object, Class clazz) throws GedcomException {
    if (!clazz.isAssignableFrom(object.getClass()))
      throw new GedcomException(object+" !instanceof "+clazz);
  }
  
  /**
   * Relationship : Association
   */
  public static class Association extends Relationship {
      
    private Property target;

    /**
     * Constructor
     */  
    public Association(Property target) {
      super(target.getGedcom(), Gedcom.INDI);
      
      this.target = target;
    }
      
    /**
     * Create Association 
     */
    public Property apply(Entity entity) throws GedcomException {
        
      assume(entity, Indi.class);

      // create ASSO in entity
      MetaProperty meta = entity.getMetaProperty().get("ASSO", true);
      PropertyAssociation asso = (PropertyAssociation)meta.create('@'+target.getEntity().getId()+'@');
      entity.addProperty(asso);

      // setup anchor through RELA if applicable
      TagPath anchor = target.getPath();
      Property rela = asso.addProperty(meta.get("RELA", true).create(anchor==null?"":'@'+anchor.toString()));

      // link it
      asso.link();
      
      // done - continue with relationship
      return rela;
    }
    
    /**
     * 'Association'
     */
    public String getName() {
      return Gedcom.resources.getString("rel.association");
    }
    
    /**
     * 
     */
    public String getDescription() {
      return Gedcom.resources.getString("rel.association.with", new String[]{ Gedcom.getName(target.getTag()), target.getEntity().toString() });
    }
    
  } //Assocation
  
  /**
   * Relationship : XRef'd by
   */
  public static class XRefBy extends Relationship {
    
    /** linked by owner */
    private Property owner;
    
    /** the xref we're referencing through */
    private PropertyXRef xref;
    
    /** Constructor */
    public XRefBy(Property owner, PropertyXRef xref) {
      super(owner.getGedcom(), xref.getTargetType());
      // keep owner      
      this.owner = owner;
      this.xref = xref;
      // done
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      return Gedcom.resources.getString("rel.xref", Gedcom.getName(getTargetType()));
    }
    
    /**
     * @see genj.gedcom.Relationship#getDescription()
     */
    public String getDescription() {
      return Gedcom.resources.getString("rel.xref.desc", new String[]{ Gedcom.getName(getTargetType()), owner.getEntity().toString()});
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Property apply(Entity entity) throws GedcomException {
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
      super(famly.getGedcom(), Gedcom.INDI);
      family = famly;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName( ) {
      return Gedcom.resources.getString("rel.child");
    }
    
    /**
     * @see genj.gedcom.Relationship#getDescription()
     */
    public String getDescription() {
      return Gedcom.resources.getString("rel.child.in", family);
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Property apply(Entity entity) throws GedcomException {
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
      super(parnt.getGedcom(), Gedcom.INDI);
      parent = parnt;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      // "Child"
      return Gedcom.resources.getString("rel.child");
    }
    
    /**
     * @see genj.gedcom.Relationship#getDescription()
     */
    public String getDescription() {
      // "Child of Meier, Nils"
      return Gedcom.resources.getString("rel.child.of", parent);
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Property apply(Entity entity) throws GedcomException {
      
      assume(entity, Indi.class);
  
      // lookup family for child    
      Fam fam;
      if (parent.getNoOfFams()>0) {
        fam = parent.getFam(0);
      } else {
        fam = (Fam)getGedcom().createEntity(Gedcom.FAM);
        fam.setSpouse(parent);
        
        // 20040619 adding missing spouse automatically now
        fam.setSpouse((Indi)getGedcom().createEntity(Gedcom.INDI).addDefaultProperties());
      }
      
      // add child
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
      super(famly.getGedcom(), Gedcom.INDI);
      family = famly;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      return Gedcom.resources.getString("rel.parent");
    }
    
    /**
     * @see genj.gedcom.Relationship#getDescription()
     */
    public String getDescription() {
      // "Parent in .." 
      return Gedcom.resources.getString("rel.parent.in", family);
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Property apply(Entity entity) throws GedcomException {
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
      super(chil.getGedcom(), Gedcom.INDI);
      child = chil;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName( ) {
      // "Parent"
      return Gedcom.resources.getString("rel.parent");
    }
    
    /**
     * @see genj.gedcom.Relationship#getDescription()
     */
    public String getDescription() {
      // "Parent of Meier, Nils"
      return Gedcom.resources.getString("rel.parent.of", child);
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Property apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      Indi parent = (Indi)entity;
      
      // get Family
      Fam fam = child.getFamc();
      if (fam==null) {
        fam = (Fam)getGedcom().createEntity(Gedcom.FAM);
        // make sure the new entity is first in family
        fam.setSpouse(parent);
        // add 'new' child
	      fam.addChild(child);
        // add defaults
        fam.addDefaultProperties();
      } else {
        // new entity becomes husb/wife
        fam.setSpouse(parent);
      }

      // 20040619 adding missing spouse automatically now
      // 20050405 whether we created a new family or the family didn't have all parents
      if (fam.getNoOfSpouses()<2) 
	      fam.setSpouse((Indi)getGedcom().createEntity(Gedcom.INDI).addDefaultProperties());

      // focus goes to new parent
      return parent;
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
      super(spose.getGedcom(), Gedcom.INDI);
      spouse = spose;
    }
    
    public String getWarning() {
      int n = spouse.getNoOfFams();
      if (n>0)
        return Gedcom.resources.getString("rel.spouse.warning", new String[]{ spouse.toString(), ""+n });
      return super.getWarning();
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      // "Spouse"
      return Gedcom.resources.getString("rel.spouse");
    }
    
    /**
     * @see genj.gedcom.Relationship#getDescription()
     */
    public String getDescription() {
      // "Spouse of Meier, Nils" or "Spouse"
      return Gedcom.resources.getString("rel.spouse.of", spouse);
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Property apply(Entity entity) throws GedcomException {
      assume(entity, Indi.class);
      
      // lookup family for spouse
      Fam fam = null;
      if (spouse.getNoOfFams()>0)
        fam = spouse.getFam(0);
      if (fam==null||fam.getNoOfSpouses()>=2) {
        fam = (Fam)getGedcom().createEntity(Gedcom.FAM).addDefaultProperties();
        fam.setSpouse(spouse);
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
      super(siblng.getGedcom(), Gedcom.INDI);
      sibling = siblng;
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName() {
      // "Sibling"
      return Gedcom.resources.getString("rel.sibling");
    }
    
    /**
     * @see genj.gedcom.Relationship#getDescription()
     */
    public String getDescription() {
      // "Sibling of Meier, Nils"
      return Gedcom.resources.getString("rel.sibling.of", sibling);
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity)
     */
    public Property apply(Entity entity) throws GedcomException {
      
      assume(entity, Indi.class);

      // get Family where sibling is child
      Fam fam = sibling.getFamc();
      if (fam==null) {
        fam = (Fam)getGedcom().createEntity(Gedcom.FAM);
        // 20040619 adding missing spouse automatically now
        fam.setSpouse((Indi)getGedcom().createEntity(Gedcom.INDI).addDefaultProperties());
        fam.setSpouse((Indi)getGedcom().createEntity(Gedcom.INDI).addDefaultProperties());
        fam.addChild(sibling);
      }

      // add new sibling      
      fam.addChild((Indi)entity);
      
      // focus stays with sibling
      return sibling;
    }
    
  } // SiblingOf
  
} //Relationship
