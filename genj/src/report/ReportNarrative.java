/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.fo.Document;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * ReportNarrative generates a text document containing the ancestors
 * or descendants of a particular individual.
 * The report itself can be in HTML, tex, rtf, or plain text format,
 * and additional formats can be added.
 */
public class ReportNarrative extends Report {

  public static final int DETAIL_NO_SHOW  = 0;
  public static final int DETAIL_NAME = 1;
  public static final int DETAIL_BRIEF = 2;
  public static final int DETAIL_DATES = 3;
  public static final int DETAIL_BRIEF_WITH_DATES = 2;
  public static final int DETAIL_FULL = 5;
  
  public boolean ancestors = true;
  public boolean nameIndex = true;
  public boolean placeIndex = true;
  // TODO public boolean bibliography = true;
  public boolean showImages = true;
  public String htmlStylesheet = null;

  /**
   * this report only works on the whole Gedcom file
   */
  public String accepts(Object context) {
    return context instanceof Gedcom ? getName() : null;
  }

  /**
   * The result is stored in files
   */
  public boolean usesStandardOut() {
    return false;
  }

  private File createFile(File dir, String name) {
    println(i18n("creating")+" "+name);
    return new File(dir, name);
  }

  public void start(Object context) {

    // assuming Gedcom
    Gedcom gedcom = (Gedcom)context;
    
    String resource = ancestors ? "ancestors.of" : "descendants.of";
    Indi indi = (Indi)getEntityFromUser(i18n(resource), gedcom, Gedcom.INDI); // Remove while testing
    if (indi==null)
      return;
    println("indi = " + indi.getName());

    // 1st pass is not necessary anymore - links between parts of the document are automatically resolved if available
    
    // 2nd pass - fill document content
    String title = (ancestors ? "Ancestors" : "Descendants")  + " of " + indi;
    Document doc = new Document(title);
    
    doc.addText("This report was generated on " + new Date() + " with GenealogyJ - see ");
    doc.addLink("http://genj.sourceforge.net", "http://genj.sourceforge.net");
    doc.addText("!");
    
    Set printed = new HashSet();
    Set gen = new HashSet();
    gen.add(indi);
    Set nextGen;
    int generationNumber = 1;
    do {
      nextGen = printGenerations(doc, generationNumber, gen, printed);
      generationNumber++;
      gen = nextGen;
    } while(gen.size() > 0);

    if (nameIndex) {
      println("Printing name index");
      doc.addIndex("names", "Name Index");
    }
    
    if (placeIndex) {
      println("Printing placename index");
      doc.addIndex("places", "Place Index");
    }

    // TODO if (bibliography) {
      // println("Printing bibliography");
      // doc.writeIndex(IndiWriter.BIBLIOGRAPHY);
    // }
    
    try {
      doc.write(new FileOutputStream(new File("c:/temp/reportout.xml")));
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    println("Finished!");
  }

  private Set printGenerations(Document doc, int n, Set gen0, Set printed) {
    
    println(gen0.size() + " individuals in generation #" + n);
    
    Set nextGen = new LinkedHashSet(); // important: preserves insertion order
    
    doc.addSection("Generation " + n, "");
    
    for (Iterator i = gen0.iterator(); i.hasNext();) {
      
      Indi indi = (Indi) i.next();
      IndiWriter writer = new IndiWriter(indi, doc);

      String sectionTitle = indi.getName();
      Property title = indi.getProperty("TITL");
      if (title != null) {
        if (title.getValue().indexOf(" of ") != -1 || title.getValue().startsWith("of ")) {
          // Sounds better after name
          sectionTitle += ", " +title;
        } else {
          sectionTitle = title + " " + sectionTitle;
        }
      }
      doc.addSection(sectionTitle, indi);

      boolean showKids = indi.getSex() == PropertySex.MALE; // TODO: track families shown, print with first parent
      
      // if indi already listed via different lineage, just write a link with no details.
      if (printed.contains(indi)) {
        doc.addLink("Refer to entry via different lineage", indi);
      } else {
        doc.addIndexEntry("names", indi.getLastName(), indi.getFirstName());
        writer.writeEntry(showKids, DETAIL_FULL, true, /*linkToIndi*/ false, showImages);
      }
      
      doc.endSection();

      addNextGeneration(nextGen, indi);
      
      printed.add(indi); // printed in pass 2
    }
    
    doc.endSection();
    
    println("ReportNarrative.printGenerations - " + nextGen.size() + " indis in next generation");

    return nextGen;
  }

  /** 
   * Add the next generation of relations of 'indi' to 'indis'.
   * The option 'ancestors' determines which direction we go.
   * @param indis Collection of relatives comprising 'next' generation
   * @param indi  Individual to start with
   */
  private void addNextGeneration(Set indis, Indi indi) {
    
    if (ancestors) {
      Indi parent = indi.getBiologicalFather();
      if (parent != null) 
        indis.add(parent);
      parent = indi.getBiologicalMother();
      if (parent != null) 
        indis.add(parent);
    } else {
      Indi[] children = indi.getChildren();
      for (int j = 0; j < children.length; j++) {
        indis.add(children[j]);
      }
    }
  }

  /**
   * IndiWriter writes information about an individual to a Formatter.
   */
  public static class IndiWriter {
    
    private Indi indi;
    private Document doc;
    
    /** Symbolic constant for {@link #writePersonalPronoun} */
    private boolean AS_SUBJECT = true;

//    public static String NAME_INDEX = "Name Index";
//    public static String PLACE_INDEX = "Placename Index";
//    public static final String BIBLIOGRAPHY = "Bibliography";

    public IndiWriter(Indi indi, Document doc) {
      this.indi = indi;
      this.doc = doc;
    }

    // From definition of INDIVIDUAL_ATTRIBUTE_STRUCTURE in Gedcom 5.5 spec
    // at http://homepages.rootsweb.com/~pmcbride/gedcom/55gcch2.htm
    private static final Set INDIVIDUAL_ATTRIBUTES = new HashSet(Arrays.asList(
        new String[] {
        "CAST", // <CASTE_NAME>   {1:1}
        "DSCR", // <PHYSICAL_DESCRIPTION>   {1:1}
        "EDUC", // <SCHOLASTIC_ACHIEVEMENT>   {1:1}
        "IDNO", // <NATIONAL_ID_NUMBER>   {1:1}*
        "NATI", // <NATIONAL_OR_TRIBAL_ORIGIN>   {1:1}
        "NCHI", // <COUNT_OF_CHILDREN>   {1:1}
        "NMR", // <COUNT_OF_MARRIAGES>   {1:1}
        "PROP", // <POSSESSIONS>   {1:1}
        "RELI", // <RELIGIOUS_AFFILIATION>   {1:1}
        "SSN", // <SOCIAL_SECURITY_NUMBER>   {0:1}
        // handled as part of name: "TITL", // <NOBILITY_TYPE_TITLE>  {1:1}
     }
    ));

    public void writeEntry(boolean withChildren, int defaultDetailLevel, boolean withParents, boolean linkToIndi, boolean showImages) {
      
      int detailLevel = defaultDetailLevel;
      try {

        // FIXME
        //doc.addIndexEntry(NAME_INDEX, indi.getName());

//        // TODO: option for image positioning
//        if (showImages && alignImages && detailLevel >= Formatter.DETAIL_FULL) {
//          insertImages();
//        }

        if (linkToIndi)
          doc.addLink(indi);
        else
          doc.addText(indi.toString());
        
        // TODO: alternate names
        // TODO: print REFN and/or ID if desired
        // TODO: Some other 1-level tags in my files:  DESC - almost certainly wrong in usage
        // EDIT (not in standard), EMAIL (not in standard),
        // INFT = informant (not in standard), INTV (not in standard), ORGA (not in standard),
        // ORIG (not in standard), OWNR (not in standard)

        // FILE - only as part of multimedia and in header:
        // MULTIMEDIA_LINK: =
        //
        //  n  OBJE           {1:1}
        //    +1 FORM <MULTIMEDIA_FORMAT>  {1:1}
        //    +1 TITL <DESCRIPTIVE_TITLE>  {0:1}
        //    +1 FILE <MULTIMEDIA_FILE_REFERENCE>  {1:1}
        //    +1 <<NOTE_STRUCTURE>>  {0:M}
        //  ]
        //
        // TODO: ASSO describes an association between individuals, e.g.:
        // 1 ASSO @I2@
        //    2 RELA Godfather
        // TODO: ALIA references another individual who is really the same
        // TODO:  for privacy: RESN <RESTRICTION_NOTICE>

        if (detailLevel >= DETAIL_DATES) {
          String date = getDateString(indi.getBirthDate());
          if (date.length() > 0) doc.addText(", b. " + date); // or "born"?

          // (child of X and Y)
          Indi father = indi.getBiologicalFather(), mother = indi.getBiologicalMother(); 
          if (withParents && (father != null || mother != null)) {
            doc.addText(" (child of ");
            if (father != null) {
              doc.addLink(father);
              if (mother != null) {
                doc.addText(" and ");
                // also link to mother if she has own section
                doc.addLink(mother);
              }
            }
            doc.addText(")");
          }

          Fam[] fams = indi.getFamiliesWhereSpouse();
          for (int i = 0; i < fams.length; i++) {
            Fam fam = fams[i];
            PropertyDate marriage = fam.getMarriageDate();
            doc.addText(", m. ");  // or "married"?
            if (fams.length > 1) doc.addText("(" + (i+1) + ") ");
            if (marriage != null) doc.addText(getDateString(marriage));
            Property age = (indi.getSex() == PropertySex.MALE)
              ? fam.getProperty(new TagPath("FAM:HUSB:AGE"))
              : fam.getProperty(new TagPath("FAM:WIFE:AGE"));
            if (age != null) {
               doc.addText(" at age " + age.getValue());
            }
            Indi spouse = fam.getOtherSpouse(indi);
            if (spouse == null) {
              doc.addText(" (spouse's name unknown)");
            } else {
              if (marriage != null)  doc.addText(" to ");
              doc.addLink(spouse);
            }
            // A MARR event can also have an AGE prop under HUSB and WIFE
          }
          date = getDateString(indi.getDeathDate());
          if (date.length() > 0) doc.addText(", d. " + date); // or "died"?
          doc.addText(".");

          Set tagsProcessed = new HashSet(Arrays.asList(new String[] {
            "REFN", "CHAN", "SEX", "BIRT", "DEAT", "FAMC", "FAMS",
            "NAME", // TODO: print alternative forms
            "OBJE"
          }));
          // TODO: details from FAMS - div, divf, ...
          if (detailLevel >= DETAIL_FULL) {

            Property[] props = indi.getProperties();
            for (int i = 0; i < props.length; i++) {
              Property prop = props[i];
              if (tagsProcessed.contains(prop.getTag())) {
                // ignore
                continue;
              }

              doc.addText(" ");

              if (prop instanceof PropertyEvent) {
                writeEvent(prop);
              } else if (INDIVIDUAL_ATTRIBUTES.contains(prop.getTag())) {
                writeEvent(prop);
              } else if (prop.getTag().equals("BAPT")) { // misspelling of BAPM
                writeEvent(prop);
              } else if (prop.getTag().equals("RESI") || prop.getTag().equals("ADDR")) {
                writeEvent(prop);
              } else if (prop.getTag().equals("OCCU")) {
                doc.addText(" ");
                writePersonalPronoun(AS_SUBJECT);
                boolean past = true;
                if (indi.getDeathDate() == null) {
                  Delta age = indi.getAge(PointInTime.getPointInTime(System.currentTimeMillis()));
                  if (age != null && age.getYears() < 65) past = false;
                }
                if (past) {
                  doc.addText(" was a ");
                } else {
                  doc.addText(" is a ");
                }
                doc.addText(prop.getValue()); // TODO: decapitalize
                doc.addText(".");
              } else if (prop.getTag().equals("NOTE")) {
                if (prop instanceof PropertyXRef) {
                  Entity ref = ((PropertyXRef)prop).getTargetEntity();
                  doc.addText("[Note: " + ref.getValue() + "]");
                  // print SOUR etc of NOTE
                  Property source = ref.getProperty("SOUR");
                  if (source != null) {
                      writeSource((Source) ((PropertySource) source).getTargetEntity());
                  }
                  // TODO: avoid printing same note twice (means referring to a previous note, hmm)
                  // TODO: what else can a NOTE @xx@ have?
                } else {
                  doc.addText("[Note: " + prop.getValue() + "]");
                }
              } else if (prop.getTag().equals("SOUR") && prop instanceof PropertySource) {
                writeSource((Source) ((PropertySource) prop).getTargetEntity());
                // formatter.printText("[Source: " + prop.getValue() + "]");
              } else if (prop.getTag().equals("SOUR")) {
                // One can also record a text description of the source directly
                doc.addText("[Source: " + prop.getValue()  + "]");
              } else {
                doc.addText("[Property " + prop.getTag() + " " + prop.getValue() + "]"); // TODO: finish the useful ones!
              }
            }
          } else if (detailLevel <= DETAIL_BRIEF_WITH_DATES) {
            // TODO: a bit of detail on number of kids, occu?
          }
        }

        if (withChildren ) {
          boolean multipleFamilies = indi.getNoOfFams() > 1;
          Indi[] children = indi.getChildren();
          if (children.length > 0) {
            doc.addList();
              for (int i = 0; i < children.length; i++) {
                doc.addListItem();
                Indi child = children[i];
//                doc.addIndexEntry(NAME_INDEX, child.getName());
                IndiWriter w = new IndiWriter(child, doc);
                // Parents clear from the context, don't print them.
                w.writeEntry(/*withChildren*/ false, DETAIL_DATES,
                    /*withParents*/ multipleFamilies, /*linkToIndi*/ true, false);
              }
            doc.endList();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        doc.addText("***Error here***");
      }

      if (showImages && detailLevel >= DETAIL_FULL) {
        insertImages();
      }
    }

    /* Get date in nicely formatted, localized, human-readable form. */
    private String getDateString(Property prop) {
      if (prop == null || !prop.isValid()) 
        return "";
      return prop.getDisplayValue();
    }

    private void insertImages() {
      // Get images from OBJE tags...align options work in HTML best if the images
      // are printed before the text, but this may vary with other Formatter
      // implementations, should allow them to give a hint.
      // Should also have option where to put the images; even in HTML
      // if could be preferable to
      // GEDCOM Example:
      //1 OBJE
      //2 TITL Pilot Error
      //2 FORM JPG
      //2 FILE meiern.jpg
      //2 NOTE More explanatory text.
        //  TODO: handle references to objects:
        //  n  OBJE @<XREF:OBJE>@  {1:1}
//        Property[] props = indi.getProperties(new TagPath("INDI:OBJE"));
//        for (int i = 0; i < props.length; i++) {
//          Property prop = props[i];
//          if (prop.getProperty("FILE") != null &&
//              (isImagePath(prop.getProperty("FILE")) || isImagePath(prop.getProperty("FORM")))) {
//            if (!alignImages) {
//              formatter.beginParagraph(); // Not ideal formatting, improve
//              formatter.writeImageLink(prop.getProperty("FILE").getValue(),
//                  prop.getProperty("TITL") == null ? "" : prop.getProperty("TITL").getValue(), alignImages);
//              if (prop.getProperty("NOTE") != null) formatter.printText(prop.getProperty("NOTE").getValue());
//              formatter.endParagraph();
//            } else {
//              // HTML malfeature - can't add caption if aligning within a paragraph
//              formatter.writeImageLink(prop.getProperty("FILE").getValue(), null, alignImages);
//              // and in fact, should probably save the text for another place.
//              //if (prop.getProperty("NOTE") != null) formatter.printText(prop.getProperty("NOTE").getValue());
//            }
//          }
//        }

    }

    private boolean isImagePath(Property property) {
      // TODO: better implementation
      if (property == null) return false;
      String path = property.getValue();
      return path.endsWith("jpg")
              ||
          path.endsWith("JPG")
          ||
          path.endsWith("gif")
          ||
          path.endsWith("GIF");
    }

    private void writeSource(Source prop) {
      // TODO: source #, full info at first citation, page #, "ibid" etc,
      // variations depending on whether part of bibliography?

      // Definition from GEDCOM 5.5 standard:
      //    SOURCE_RECORD: =
      //
      //      n  @<XREF:SOUR>@ SOUR  {1:1}
      //        +1 DATA        {0:1}
      //          +2 EVEN <EVENTS_RECORDED>  {0:M}
      //            +3 DATE <DATE_PERIOD>  {0:1}
      //            +3 PLAC <SOURCE_JURISDICTION_PLACE>  {0:1}
      //          +2 AGNC <RESPONSIBLE_AGENCY>  {0:1}
      //          +2 <<NOTE_STRUCTURE>>  {0:M}
      //        +1 AUTH <SOURCE_ORIGINATOR>  {0:1}
      //          +2 [CONT|CONC] <SOURCE_ORIGINATOR>  {0:M}
      //        +1 TITL <SOURCE_DESCRIPTIVE_TITLE>  {0:1}
      //          +2 [CONT|CONC] <SOURCE_DESCRIPTIVE_TITLE>  {0:M}
      //        +1 ABBR <SOURCE_FILED_BY_ENTRY>  {0:1}
      //        +1 PUBL <SOURCE_PUBLICATION_FACTS>  {0:1}
      //          +2 [CONT|CONC] <SOURCE_PUBLICATION_FACTS>  {0:M}
      //        +1 TEXT <TEXT_FROM_SOURCE>  {0:1}
      //          +2 [CONT|CONC] <TEXT_FROM_SOURCE>  {0:M}
      //        +1 <<SOURCE_REPOSITORY_CITATION>>  {0:1}
      //        +1 <<MULTIMEDIA_LINK>>  {0:M}
      //        +1 <<NOTE_STRUCTURE>>  {0:M}
      //        +1 REFN <USER_REFERENCE_NUMBER>  {0:M}
      //          +2 TYPE <USER_REFERENCE_TYPE>  {0:1}
      //        +1 RIN <AUTOMATED_RECORD_ID>  {0:1}
      //        +1 <<CHANGE_DATE>>  {0:1}

      doc.addText("[Source ");
      if (prop.getProperty("REFN") != null) {
        doc.addText(prop.getProperty("REFN").getValue());
      } else {
        doc.addText(prop.getValue());
      }
      writeOptionalProperty(prop, "TYPE", " (", ")");
      if (prop.getProperty("TITL") != null) {
        doc.addText(prop.getProperty("TITL").getValue());
      }
      if (prop.getProperty("AUTH") != null) {
        doc.addText(" by ");
        doc.addText(prop.getProperty("AUTH").getValue());
      }
      if (prop.getProperty("EDIT") != null) { // GEDCOM 5.0 editor
        doc.addText(" edited by ");
        doc.addText(prop.getProperty("EDIT").getValue());
      }
      if (prop.getProperty("INTV") != null) { // GEDCOM 5.0 interviewer
        doc.addText(" as told to ");
        doc.addText(prop.getProperty("INTV").getValue());
        if (prop.getProperty("INFT") != null) { // GEDCOM 5.0 informant
          doc.addText(" by ");
          doc.addText(prop.getProperty("INFT").getValue());
        }
      } else if (prop.getProperty("INFT") != null) { // GEDCOM 5.0 informant
        doc.addText(" as related by ");
        doc.addText(prop.getProperty("INFT").getValue());
      }
      if (prop.getProperty("OWNR") != null) { // GEDCOM 5.0 owner
        doc.addText(" in posession of ");
        doc.addText(prop.getProperty("OWNR").getValue());
      }
      writeOptionalProperty(prop, "DATE", ", ");
      if (prop.getProperty("NOTE") != null) {
        doc.addText(". Note: '");
        doc.addText(prop.getProperty("NOTE").getValue());
        doc.addText("'");
      }
      if (prop.getProperty("TEXT") != null) {
        doc.addText(". Quote from source: '");
        // TODO: convert blank line to paragraph end/begin.  Turn URLs
        // in plaintext into links.
        doc.addText(prop.getProperty("TEXT").getValue());
        doc.addText("'");
      }
      // TODO: DATA, REPO, PUBL, OBJE.  Perhaps also the GEDCOM 5.0 fields
      // TYPE,
      // which I haven't expunged from my data (Bill Kelly).
      // Probably not of interest: ABBR is mainly a sort key ("a short title used
      // for sorting, filing, and retrieving source records"), but depends on
      // popular usage and demand; RIN, CHAN.
      doc.addText("] ");
    }

    private void writeOptionalProperty(Property prop, String tag, String prolog) {
      writeOptionalProperty(prop, tag, prolog, "");
    }

    private void writeOptionalProperty(Property prop, String tag, String prolog, String epilog) {
      if (prop.getProperty(tag) != null) {
        doc.addText(prolog);
        if (tag.equals("DATE")) {
            doc.addText(getDateString(prop));
        } else {
          doc.addText(prop.getProperty(tag).getValue());
        }
        doc.addText(epilog);
      }
    }

    private void writeEvent(Property prop) {
      String verb = prop.getTag(); // useful as fallback
      String placePrep = null;
      String prepForAgency = null;
      if (prop.getTag().equals("CHR")) {
        verb = "was christened";
      } else if (prop.getTag().equals("BURI")) {
        verb = "was buried";
      } else if (prop.getTag().equals("CREM")) {
        verb = "was cremated";
      } else if (prop.getTag().equals("ADOP")) {
        verb = "was adopted";
      } else if (prop.getTag().equals("BAPM")) {
        verb = "was baptized"; // ??
      } else if (prop.getTag().equals("BARM")) {
        verb = "was bar mitzvahed"; // better phrasing :-) was barmy?
      } else if (prop.getTag().equals("BASM")) {
        verb = "was bas mitzvahed"; // better phrasing :-)
      } else if (prop.getTag().equals("BLES")) {
        verb = "was blessed"; // better phrasing :-)
      } else if (prop.getTag().equals("CHRA")) {
        verb = "was christened as an adult";
      } else if (prop.getTag().equals("CONF")) {
        verb = "was confirmed";
      } else if (prop.getTag().equals("FCOM")) {
        verb = "celebrated first communion";
      } else if (prop.getTag().equals("ORDN")) {
        verb = "was ordained";
      } else if (prop.getTag().equals("NATU")) {
        verb = "was naturalized";
      } else if (prop.getTag().equals("RESI")) {
        verb = "resided";
      } else if (prop.getTag().equals("ADDR")) {
        // Officially wrong but FamilyTreeMaker uses it
        verb = "resided";
      } else if (prop.getTag().equals("EMIG")) {
        verb = "emigrated";
        placePrep = "to"; // hmm, I'd have thought from, Nils has destination
      } else if (prop.getTag().equals("IMMI")) {
        verb = "immigrated";
        placePrep = "to";
      } else if (prop.getTag().equals("CENS")) {
        verb = "was recorded in the census";
      } else if (prop.getTag().equals("PROB")) {
        verb = "left a will, which was probated"; // TODO support possessive form - his will was probated
      } else if (prop.getTag().equals("WILL")) {
        verb = "left a will dated";
      } else if (prop.getTag().equals("GRAD")) {
        verb = "graduated";
        prepForAgency = "from";
      } else if (prop.getTag().equals("RETI")) {
        verb = "retired";
      } else if (prop.getTag().equals("EVEN")) {
        // Depends on subordinate TYPE
        if (prop.getProperty("TYPE") == null) {
          verb = "was involved in some kind of event";
        } else {
          verb = "was " + prop.getProperty("TYPE").getValue()+ "-ed";
          String type = prop.getProperty("TYPE").getValue();
          if (type.equals("Resided")) verb = "resided";
        }
      }
      // from ... where? TODO cite spec
      else if (prop.getTag().equals("MARL")) {
        verb = "got a license to marry";
      }
      // below from INDIVIDUAL_ATTRIBUTE_STRUCTURE
      else if (prop.getTag().equals("CAST")) {
        verb = "was of caste " + prop.getValue();
      } else if (prop.getTag().equals("DSCR")) { // <PHYSICAL_DESCRIPTION>   {1:1}
        verb = "was"; // tricky to get right word
      } else if (prop.getTag().equals("EDUC")) { // <SCHOLASTIC_ACHIEVEMENT>   {1:1}
        verb = "was awarded"; // lame attempt
      } else if (prop.getTag().equals("IDNO")) { // <NATIONAL_ID_NUMBER>   {1:1}*
        verb = "had national ID number " + prop.getValue(); // TODO: privacy
      } else if (prop.getTag().equals("NATI")) { // <NATIONAL_OR_TRIBAL_ORIGIN>   {1:1}
        verb = "was"; // was a?
      } else if (prop.getTag().equals("NCHI")) { // <COUNT_OF_CHILDREN>   {1:1}
        verb = "had " + prop.getValue() + " children";
      } else if (prop.getTag().equals("NMR")) { // <COUNT_OF_MARRIAGES>   {1:1}
        verb = "married " + prop.getValue() + " times";
      } else if (prop.getTag().equals("PROP")) { // <POSSESSIONS>   {1:1}
        verb = "owned";
      } else if (prop.getTag().equals("RELI")) { // <RELIGIOUS_AFFILIATION>   {1:1}
        verb = "was affiliated with the"; // the will often be incorrect but usually sounds goodkkkkk
      } else if (prop.getTag().equals("SSN")) { // <SOCIAL_SECURITY_NUMBER>   {0:1}
        verb = "had Social Security number " + prop.getValue(); // TODO: privacy
      }
      writePersonalPronoun(AS_SUBJECT);
      doc.addText(" ");
      doc.addText(verb);
      // TODO // Any event can also have an AGE prop - "age the age of X" ?
      // Values according to GEDCOM 5.5 spec: [ < | > | <NULL>]
      //[ YYy MMm DDDd | YYy | MMm | DDDd |
      //YYy MMm | YYy DDDd | MMm DDDd |
      //CHILD | INFANT | STILLBORN ]
      //]

      if (prop.getProperty("AGNC") != null) {
        if (prepForAgency != null) doc.addText(" " + prepForAgency);
        doc.addText(" " + prop.getProperty("AGNC").getValue());
      }
      writePlace(prop, placePrep);
      String date = "";
      if (prop instanceof PropertyEvent) {
        date = getDateString(prop.getProperty("DATE"));
      }
      if (date.length() > 0) {
        if (date.startsWith("FROM")) { // TODO fix case
          doc.addText(" " + date);
          // TODO if just year, say in
        } else {
          doc.addText(" on " + date);
        }
      }
      doc.addText(". ");
    }

    /**
     * Write a placename in a natural form for humans.
     * @param prop
     */
    private void writePlace(Property prop, String preposition) {
      // TODO: add to placename index in doc
      Object city = null;
      StringBuffer result = new StringBuffer();
      Property addr = prop.getProperty("ADDR");
      if (addr != null) {
        city = addr.getProperty("CITY");
        appendToPlace(result, addr);
        appendToPlace(result, addr.getProperty("ADR1"));
        appendToPlace(result, addr.getProperty("ADR2"));
        appendToPlace(result, addr.getProperty("CITY"));
        // POST is postal code - should have rules to know where it belongs
        appendToPlace(result, addr.getProperty("STAE"));
        appendToPlace(result, addr.getProperty("CTRY"));
      } else if (prop.getProperty("PLAC") != null) {
        String place = prop.getProperty("PLAC").getValue();
        String[] parts = place.split(",\\s*");
        if (parts.length == 0) return; // not possible?
        city = parts[0];
        String lastPart = parts[parts.length-1];
        // County often omitted from US place names
        if (parts.length >= 4 &&
            (lastPart.equalsIgnoreCase("us") || lastPart.equalsIgnoreCase("usa"))) {
          // Assume city, county, state, country
          String county = parts[parts.length-3];
          if (!county.endsWith(" County") && !county.endsWith("Co") && !county.endsWith(" Co.")) {
            parts[parts.length-3] += " Co.";
          }
        }

        // Consider omitting the country if clear from context TODO: strip off known context through new option
        int nParts = parts.length;
        if (lastPart.equals("US") ||
            lastPart.equals("GB")) {
          nParts--;
        } else {
          // Replace ISO codes with more readable names
          parts[parts.length-1] = new Locale("en", lastPart).getDisplayCountry();
        }
        // TODO: drop context when place names printed frequently (more than once?)

        boolean firstWritten = true;
        for (int i = 0; i < nParts; i++) {
          String part = parts[i];
          if (part.length() == 0) continue;
          if (firstWritten) {
            firstWritten = false;
          } else {
            result.append(", ");
          }
          result.append(part);
        }
        // First version
//        place = place.replaceAll(",\\s*,", ","); // collapse
//        place = place.replaceFirst("^[,\\s]*", "");
//        // strip off known context through new option, e.g. ", US"
//        place = place.replaceFirst(", US$", "");
//        place = place.replaceFirst(", GB$", "");
//        place = place.replaceFirst(", DE$", ", Germany");
//        if (place.length() == 0) return;
//        doc.addIndexEntry(PLACE_INDEX, place); // sort key?
      }
      if (preposition == null) {
        preposition = "in";
        if (Character.isDigit(result.charAt(0))) preposition = "at"; // likely street address (crude heuristic)
      }
      if (city!=null) 
        doc.addIndexEntry("places", city.toString(), null);

      doc.addText(" ");
      doc.addText(preposition);
      doc.addText(" ");
      doc.addText(result.toString());
    }

    /** Append , prop if not null. */
    private void appendToPlace(StringBuffer result, Property prop) {
      if (prop != null) {
        if (result.length() > 0) result.append(", ");
        result.append(prop.getValue());
      }
    }

    private void writePersonalPronoun(boolean asSubject) {
      String pronoun;
      if (asSubject) {
        if (indi.getSex() == PropertySex.MALE) pronoun = "He";
        else pronoun = "She";
      } else {
        // need more complex logic for languages with more cases
        if (indi.getSex() == PropertySex.MALE) pronoun = "him";
        else pronoun = "her";
      }
      doc.addText(pronoun);
    }

  }
  
}

