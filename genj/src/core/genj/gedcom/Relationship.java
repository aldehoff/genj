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
   * Setup the captured relationship with given entity
   * @param isNew whether the entity was created new specifically for this relationship
   * @return the receiver of focus (preferrably)
   */
  public abstract Property apply(Entity entity, boolean isNew) throws GedcomException ;  
  
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
   * A warning that explains the dire consequences of applying this relationship
   * @param entity existing entity that this relationship is applied to (if applicable)
   * @return a warning text for this relationship or null
   */
  public String getWarning(Entity entity) {
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
    public Property apply(Entity entity, boolean isNew) throws GedcomException {
        
      Indi indi = (Indi)entity;

      // create ASSO in entity
      MetaProperty meta = entity.getMetaProperty().getNested("ASSO", true);
      PropertyAssociation asso = (PropertyAssociation)meta.create('@'+target.getEntity().getId()+'@');
      entity.addProperty(asso);

      // setup anchor through RELA if applicable
      TagPath anchor = target.getPath();
      Property rela = asso.addProperty(meta.getNested("RELA", true).create(anchor==null?"":'@'+anchor.toString()));

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
     * @see genj.gedcom.Relationship#apply(Entity, boolean)
     */
    public Property apply(Entity entity, boolean isNew) throws GedcomException {
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
     * Warn if existing entity already has parents
     */
    public String getWarning(Entity entity) {
      // new entity is fine
      if (entity==null) 
        return null;
      
      // check biological parents
      Fam fam = ((Indi)entity).getFamilyWhereBiologicalChild();
      if (fam!=null)
        return Gedcom.resources.getString("error.already.child", new Object[]{ entity, fam } );
      
      // no prob
      return null;
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity, boolean)
     */
    public Property apply(Entity entity, boolean isNew) throws GedcomException {
      
      Indi child = (Indi)entity;
      
      // add child
      family.addChild(child);
      
      // set name if new person
      if (isNew) {
        Indi parent  = family.getHusband();
        if (parent==null) parent = family.getWife();
        if (parent!=null)
          child.setName("", parent.getLastName());
      }
      
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
     * Warn if existing entity already has parents
     */
    public String getWarning(Entity entity) {
      // new entity is fine
      if (entity==null) 
        return null;
      
      // check biological parents
      Fam fam = ((Indi)entity).getFamilyWhereBiologicalChild();
      if (fam!=null)
        return Gedcom.resources.getString("error.already.child", new Object[]{ entity, fam } );
      
      // no prob
      return null;
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity, boolean)
     */
    public Property apply(Entity entity, boolean isNew) throws GedcomException {
      
      Indi child = (Indi)entity;
  
      // lookup family for child    
      Fam[] fams = parent.getFamiliesWhereSpouse();
      Fam fam;
      if (fams.length>0) {
        // add child to first fam
        fam = fams[0];
        fam.addChild(child);
      } else {
        
        // create a new fam
        fam = (Fam)getGedcom().createEntity(Gedcom.FAM);

        try {
          fam.addChild(child);
        } catch (GedcomException e) {
          getGedcom().deleteEntity(fam);
          throw e;
        }
        
        // set spouse
        fam.setSpouse(parent);
        
        // 20040619 adding missing spouse automatically now
        fam.setSpouse((Indi)getGedcom().createEntity(Gedcom.INDI).addDefaultProperties());
      }
      
      // set it's name if new
      if (isNew) {
        Indi parent  = fam.getHusband();
        if (parent==null) parent = fam.getWife();
        if (parent!=null)
          child.setName("", parent.getLastName());
      }
      
      
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
     * @see genj.gedcom.Relationship#apply(Entity, boolean)
     */
    public Property apply(Entity entity, boolean isNew) throws GedcomException {
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
    
    /** predetermined family that needs a spouse */
    private Fam familyWithMissingSpouse;
    
    /** Constructor */
    public ParentOf(Indi chil) {
      super(chil.getGedcom(), Gedcom.INDI);
      
      // remember child
      child = chil;
      
      // check if there's a family without spouse
      Fam[] fams = child.getFamiliesWhereChild();
      for (int f = 0; f < fams.length; f++) {
        if (fams[f].getNoOfSpouses()<2) {
          familyWithMissingSpouse = fams[f];
          break;
        }
      }
      
    }
    
    /**
     * @see genj.gedcom.Relationship#getName()
     */
    public String getName( ) {
      // "Parent"
      return Gedcom.resources.getString("rel.parent");
    }
    
    /**
     * Warn if individual already has parents
     */
    public String getWarning(Entity entity) {
      // a family with missing spouse already identified?
      if (familyWithMissingSpouse!=null)
        return null;
      // check if we got biological parents already
      Fam fam =child.getFamilyWhereBiologicalChild();
      if (fam!=null)
        return Gedcom.resources.getString("error.already.child", new Object[]{ child, fam } );
      // no problem
      return null;
    }
    
    /**
     * @see genj.gedcom.Relationship#getDescription()
     */
    public String getDescription() {
      // "Parent of Meier, Nils"
      return Gedcom.resources.getString("rel.parent.of", child);
    }
    
    /**
     * @see genj.gedcom.Relationship#apply(Entity, boolean)
     */
    public Property apply(Entity entity, boolean isNew) throws GedcomException {
      Indi parent = (Indi)entity;
      
      // create new if necessary
      if (familyWithMissingSpouse==null) {
        familyWithMissingSpouse = (Fam)getGedcom().createEntity(Gedcom.FAM);
        // add 'new' child
        familyWithMissingSpouse.addChild(child);
        // add defaults
        familyWithMissingSpouse.addDefaultProperties();
      } 
      
      // set spouse
      familyWithMissingSpouse.setSpouse(parent);

      // 20040619 adding missing spouse automatically now
      // 20050405 whether we created a new family or the family didn't have all parents
      if (familyWithMissingSpouse.getNoOfSpouses()<2) 
        familyWithMissingSpouse.setSpouse((Indi)getGedcom().createEntity(Gedcom.INDI).addDefaultProperties());

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
    
    public String getWarning(Entity entity) {
      int n = spouse.getNoOfFams();
      if (n>0)
        return Gedcom.resources.getString("rel.spouse.warning", new String[]{ spouse.toString(), ""+n });
      return null;
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
     * @see genj.gedcom.Relationship#apply(Entity, boolean)
     */
    public Property apply(Entity entity, boolean isNew) throws GedcomException {
      
      Indi indi = (Indi)entity;
      
      // lookup family for spouse
      Fam[] fams = spouse.getFamiliesWhereSpouse();
      Fam fam = null;
      if (spouse.getNoOfFams()>0)
        fam = fams[0];
      if (fam==null||fam.getNoOfSpouses()>=2) {
        fam = (Fam)getGedcom().createEntity(Gedcom.FAM).addDefaultProperties();
        fam.setSpouse(spouse);
      }

      // set its spouse
      fam.setSpouse(indi);
      
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
     * @see genj.gedcom.Relationship#apply(Entity, boolean)
     */
    public Property apply(Entity entity, boolean isNew) throws GedcomException {
      
      Indi newSibling = (Indi)entity;
      
      // get Family where sibling is child
      Fam fam;
      Fam[] fams = sibling.getFamiliesWhereChild();
      if (fams.length>0) {
        fam = fams[0];
      } else {
        fam = (Fam)getGedcom().createEntity(Gedcom.FAM);
        // 20040619 adding missing spouse automatically now
        fam.setSpouse((Indi)getGedcom().createEntity(Gedcom.INDI).addDefaultProperties());
        fam.setSpouse((Indi)getGedcom().createEntity(Gedcom.INDI).addDefaultProperties());
        fam.addChild(sibling);
      }

      // add new sibling      
      fam.addChild(newSibling);
      
      // set it's name if new
      if (isNew) 
        newSibling.setName("", sibling.getLastName());        
      
      // focus stays with sibling
      return sibling;
    }
    
  } // SiblingOf
  
} //Relationship
