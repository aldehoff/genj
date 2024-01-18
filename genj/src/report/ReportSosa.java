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
import genj.gedcom.PrivacyPolicy;
import genj.gedcom.Property;
import genj.report.Report;

import java.util.ArrayList;
import java.util.List;

/**
 * GenJ - ReportSosa Types de rapports: - Tableau d'ascendance avec num sosa:
 * une colonne par type d'evenement - Tableau d'ascendance Agnatique (uniquement
 * les peres. Si pas de pere, la mere) - Liste d'ascendance suivant les lignees
 *
 * Format des rapports: - Une ligne par individu - Un evt par ligne Type de
 * sortie - Texte - Texte, colonnes tronquees - HTML TODO Daniel - read gedcom
 * header place format to set placeJurisdictionIndex in a more comprehensive
 * way. - Tune .property file - Add one event per line for lineage report - Add
 * different colour for male, female and undef - Add header or footer with
 * issuer informations **** 1. modifier le core pour pouvoir sauvegarde le
 * rapport correctement **** 2. mettre une option pour sortir le rapport en mode
 * texte uniquement (comme avant) **** 3. supprimer la ligne vide dans le
 * rapport en suivant la lignee **** 4. separer les evenements par des virgules
 * dans l'ascendance lignee 5. faire un alignement en cas de debordement dans le
 * rap lignee 6. voir utilisation de la couleur **** 7. modify generation xx
 * formatting (cadre, souligne, ...)
 */
/*
 * TODO: mettre une sortie texte uniquement 
 */
public class ReportSosa extends Report {

  /** option - our report types defined, the value and choices */
  private final static int
    SOSA_REPORT = 0,
    LINEAGE_REPORT = 1,
    AGNATIC_REPORT = 2,
    TABLE_REPORT = 3;

  public int reportType = SOSA_REPORT;

  public String reportTypes[] = {
      translate("SosaReport"),
      translate("LineageReport"),
      translate("AgnaticReport"),
      translate("TableReport")
   };

  /** option - individual per line or event per line */
  private final static int
    ONE_LINE = 0,
    ONE_EVT_PER_LINE = 1;

  public int reportFormat = ONE_LINE;
  public String reportFormats[] = {
      translate("IndiPerLine"),
      translate("EventPerLine")
  };

  /** option - simple choices */
  public boolean showGenerations = true;
  public int reportMaxGenerations = 999;
  public boolean showAllPlaceJurisdictions = false;
  public boolean reportPlaceOfBirth = true;
  public boolean reportDateOfBirth = true;
  public boolean reportPlaceOfMarriage = true;
  public boolean reportDateOfMarriage = true;
  public boolean reportPlaceOfDeath = true;
  public boolean reportDateOfDeath = true;
  public boolean reportPlaceOfOccu = true;
  public boolean reportDateOfOccu = true;
  public boolean reportPlaceOfResi = true;
  public boolean reportDateOfResi = true;

  /** option - number of generations from root considered to be private */
  public int privateGen = 0;

  /**
   * Main for argument individual
   */
  public void start(Indi indi) {

    // Init some stuff
    PrivacyPolicy policy = OPTIONS.getPrivacyPolicy();

    // check recursion type
    Recursion recursion;
    switch (reportType) {
      case AGNATIC_REPORT:
        recursion = new Agnatic();
        break;
      case SOSA_REPORT:
        recursion = new Sosa();
        break;
      case LINEAGE_REPORT:
        recursion = new Lineage();
        break;
      case TABLE_REPORT:
        recursion = new Table();
        break;
      default:
        throw new IllegalArgumentException("no such report type");
    }

    // start with a title in a document
    String title = recursion.getTitle(indi);
    Document doc = new Document(title);
    doc.startSection(title);

    // iterate into individual and all its ascendants
    recursion.start(indi, policy, doc);

    // Done
    showDocumentToUser(doc);
  }

  /**
   * Returns the category of this report.
   */
  public Category getCategory() {
      return CATEGORY_PRESENTATION;
  }

  /**
   * base type for our rescursion into ancestors  - either Sosa, Lineage, Agnatic or Table
   */
  abstract class Recursion {

    /** start the recursion */
    abstract void start(Indi indi, PrivacyPolicy policy, Document doc);

    /**
     * title - implement in sub-class
     */
    abstract String getTitle(Indi root);

    /**
     * recursion step for formatting the start of the recursion - implement in sub-classes
     */
    abstract void formatStart(Indi indi, Document doc);

    /**
     * recursion step for formatting an individual - implement in sub-classes
     */
    abstract void formatIndi(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc);

    /**
     * recursion step for formatting the end of the recursion - implement in sub-classes
     */
    abstract void formatEnd(Document doc);

     /**
      * format some information about an entity's property
      */
     String getProperty(Entity entity, String tag, String prefix, boolean date, boolean place, PrivacyPolicy policy) {
       Property prop = entity.getProperty(tag);
       if (prop == null)
         return "";
       String format = prefix + (date ? "{ $D}" : "") + (place && showAllPlaceJurisdictions ? "{ $P}" : "") + (place && !showAllPlaceJurisdictions ? "{ $p}" : "");
       return prop.format(format, policy);
     }

    /**
     * dump individual's name
     */
    String getName(Indi indi, int sosa, PrivacyPolicy privacy) {
      return (sosa>0?sosa+" ":"") + privacy.getDisplayValue(indi, "NAME") + " (" + indi.getId() + ")";
    }

    /**
     * resolve standard set of properties of an individual
     * @param indi the individual to get properties for
     * @param fam the family to consider as THE spousal family
     * @param privacy privacy policy
     * @param usePrefixes whether to user prefixes in info generation
     * @param returnEmpties whether to return or skip empty values
     */
    String[] getProperties(Indi indi, Fam fam, PrivacyPolicy privacy, boolean usePrefixes, boolean returnEmpties) {

      List result = new ArrayList();

      // birth?
      String birt = getProperty(indi, "BIRT", usePrefixes ? OPTIONS.getBirthSymbol() : "", reportDateOfBirth, reportPlaceOfBirth, privacy);
      if (returnEmpties||birt.length()>0)
        result.add(birt);

      // marriage?
      String marr = "";
      if (fam!=null) {
        String prefix = "";
        if (usePrefixes)
          prefix = OPTIONS.getMarriageSymbol() + (fam.getOtherSpouse(indi) != null ? " " + fam.getOtherSpouse(indi).getName() : "");
        marr = getProperty(fam, "MARR", prefix, reportDateOfMarriage, reportPlaceOfMarriage, privacy);
      }
      if (returnEmpties||marr.length()>0)
        result.add(marr);

      // death?
      String deat = getProperty(indi, "DEAT", usePrefixes ? OPTIONS.getDeathSymbol() : "", reportDateOfDeath, reportPlaceOfDeath, privacy);
      if (returnEmpties||deat.length()>0)
        result.add(deat);

      // occupation?
      String occu = getProperty(indi, "OCCU", "{$T} ", reportDateOfOccu, reportPlaceOfOccu, privacy);
      if (returnEmpties||occu.length()>0)
        result.add(occu);

      // residence?
      String resi = getProperty(indi, "RESI", "{$T} ", reportDateOfResi, reportPlaceOfResi, privacy);
      if (returnEmpties||resi.length()>0)
        result.add(resi);

      // done
      return (String[])result.toArray(new String[result.size()]);
    }

  } //Layout

  /**
   * base type for our rescursion into ancestors  - either Sosa, Lineage, Agnatic or Table
   */
  abstract class DepthFirst  extends Recursion {

    /** start */
    void start(Indi indi, PrivacyPolicy policy, Document doc) {
      formatStart(indi, doc);
      recursion(indi, null, 0, 1, policy, doc);
      formatEnd(doc);
    }

    /**
     * each layout iterates over all individuals starting with the root
     * up to the maximum number of generations
     * @param indi the current individual
     * @param fam the family that this individual was pulled out of (null for root)
     * @param gen the current generation
     * @param sosa the sosa index
     * @param policy the privacy policy
     */
    void recursion(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {

      // stop here?
      if (gen > reportMaxGenerations)
        return;

      // let implementation handle individual
      formatIndi(indi, fam, gen, sosa, gen < privateGen ? PrivacyPolicy.PRIVATE : policy, doc);

      // go one generation up to father and mother
      Fam famc = indi.getFamilyWhereBiologicalChild();
      if (famc == null)
        return;

      Indi father = famc.getHusband();
      Indi mother = famc.getWife();

      if (father==null&&mother==null)
        return;

      // recurse into father
      if (father != null)
        recursion(father, famc, gen+1,  sosa*2, policy, doc);

      // recurse into mother
      if (mother != null)
        recursion(mother, famc, gen+1, sosa*2+1, policy, doc);

      // done
    }
  } //DepthFirst

  /**
   * a breadth first recursion
   */
  abstract class BreadthFirst extends Recursion {

    /** start */
    void start(Indi indi, PrivacyPolicy policy, Document doc) {
      formatStart(indi, doc);
      List list = new ArrayList(3);
      list.add(new Integer(1));
      list.add(indi);
      list.add(null);
      recursion(list, 0, policy, doc);
      formatEnd(doc);
    }

   /**
    * recurse over a generation list
    * up to the maximum number of generations
    * @param generation the current generation (sosa,indi,fam)*
    * @param gen the current generation
    * @param policy the privacy policy
    * @param doc the document to fill
    */
   void recursion(List generation, int gen, PrivacyPolicy policy, Document doc) {

     // stop here?
     if (gen > reportMaxGenerations)
       return;

     // format this generation
     formatGeneration(gen, doc);

     // report the whole generation from 'left to right'
     List nextGeneration = new ArrayList();
     for (int i=0; i<generation.size(); ) {

       // next triplet
       int sosa = ((Integer)generation.get(i++)).intValue();
       Indi indi = (Indi)generation.get(i++);
       Fam fam = (Fam)generation.get(i++);

       // grab father and mother
       Fam famc = indi.getFamilyWhereBiologicalChild();
       if (famc!=null)  {
         Indi father = famc.getHusband();
         if (father!=null) {
           nextGeneration.add(new Integer(sosa*2));
           nextGeneration.add(father);
           nextGeneration.add(famc);
         }
         Indi mother = famc.getWife();
         if (mother!=null) {
           nextGeneration.add(new Integer(sosa*2+1));
           nextGeneration.add(mother);
           nextGeneration.add(famc);
         }
       }

       // let implementation handle individual
       formatIndi(indi, fam, gen, sosa, gen < privateGen ? PrivacyPolicy.PRIVATE : policy, doc);
     }

     // recurse into next generation
     if (!nextGeneration.isEmpty())
       recursion(nextGeneration, gen+1, policy, doc);

     // done
   }

   /**
    * formatting the begin of a generation - implement in sub-classes
    */
   abstract void formatGeneration(int gen, Document doc);

  } //BreadthFirst

  /**
   * The pretties report with breadth first
   *
   * GENERATION 1
   * 1 root
   * GENERATION 2
   * 2 father
   * 3 mother
   * GENERATION 3
   * 4 grandfather 1
   * 5 grandmother 1
   * 6 grandfather 2
   * 7 grandfather 2
   * GENERATION 4
   * ...
   */
  class Sosa extends BreadthFirst {

    /** our title - simply the column header values */
    String getTitle(Indi root) {
      return translate("title.sosa", root.getName());
    }

    /** this is called once at the beginning of the recursion - we add our table around it */
    void formatStart(Indi root, Document doc) {
      // open table first
      doc.startTable("width=100%");
      doc.addTableColumn("");
      doc.addTableColumn("");
    }

    /** called at each generation add a generation info row*/
    void formatGeneration(int gen, Document doc) {
      doc.nextTableRow();
      doc.nextTableCell("number-columns-spanned=2,font-size=20pt,background-color=#f0f0f0,border-after-width=0.5pt");
      doc.addText(translate("Generation")+" "+(gen+1));
    }

    /** this is called at each recursion step - output table rows */
    void formatIndi(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {

      // start with a new row
      doc.nextTableRow();

      // a cell with sosa# and name
      doc.addText(getName(indi, sosa, policy)); // [sosa] name (id)

      // then a cell with properies
      String[] props = getProperties(indi, fam, policy, true, false);
      if (props.length>0) {
        doc.nextTableCell();

        if (reportFormat==ONE_LINE) {
          for (int p=0; p<props.length; p++) {
            if (p!=0) doc.addText(", ");
            doc.addText(props[p]);
          }
        } else {
          doc.startList();
          for (int p=0; p<props.length; p++) {
            if (p!=0) doc.nextListItem();
            doc.addText(props[p]);
          }
          doc.endList();
        }
      }
      // done for now
    }

    /** called at the end of the recursion - end our table */
    void formatEnd(Document doc) {
      // close table
      doc.endTable();
    }

  } //Sosa

  /**
   * A Lineage recursion goes depth first and generates a nested tree of ancestors and their properties
   *
   * 1 root
   *  2 father
   *   4 grandfather
   *   5 grandmother
   *  3 mother
   *   6 grandfrather
   *   7 grandmother
   *  ...
   */
  class Lineage extends DepthFirst  {

    /** our title */
    String getTitle(Indi root) {
      return translate("title.lineage", root.getName());
    }

    /** start formatting */
    void formatStart(Indi indi, Document doc) {
      // noop
    }

    /** how we format an individual */
    void formatIndi(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {

      // dump the indi's name
      doc.nextParagraph("space-after=10pt,space-before=10pt,start-indent="+(gen*20)+"pt");
      doc.addText(getName(indi, sosa, policy));

      // dump its properties
      String[] props = getProperties(indi, fam, policy, true, false);
      if (props.length>0) {

        // calculate indent
        String indent = "start-indent="+(gen*20+10)+"pt";

        if (reportFormat==ONE_LINE) {
          for (int p=0; p<props.length; p++) {
            if (p!=0) doc.addText(", ");
            doc.addText(props[p]);
          }
        } else {
          doc.startList(indent);
          for (int p=0; p<props.length; p++) {
            if (p!=0) doc.nextListItem();
            doc.addText(props[p]);
          }
          doc.endList();
        }
      }
      // done
    }

    /** end formatting */
    void formatEnd(Document doc) {
      // noop
    }

  } //Lineage

  /**
   * 1 root
   * 2 father
   * 4 grandfather
   * 8 great grandfather
   * ...
   */
  class Agnatic extends DepthFirst  {

      /** 
       * each layout iterates over all individuals starting with the root
       * up to the maximum number of generations
       * @param indi the current individual
       * @param fam the family that this individual was pulled out of (null for root)
       * @param gen the current generation
       * @param sosa the sosa index
       * @param policy the privacy policy
       */
      void recursion(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {
        
        // stop here?
        if (gen > reportMaxGenerations)
          return;

        // let implementation handle individual
        formatIndi(indi, fam, gen, sosa, gen < privateGen ? PrivacyPolicy.PRIVATE : policy, doc);
        
        // go one generation up to father and mother 
        Fam famc = indi.getFamilyWhereBiologicalChild();
        if (famc == null) 
          return;
        
        Indi father = famc.getHusband();
        
        // recurse into father 
        if (father != null) 
          recursion(father, famc, gen+1,  sosa*2, policy, doc);

        // done
      }

    /** our title */
    String getTitle(Indi root) {
      return translate("title.agnatic", root.getName());
    }

    /** start formatting */
    void formatStart(Indi indi, Document doc) {
      // noop
    }

    /** how we format an individual */
    void formatIndi(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {

      // only consider fathers
      if (fam!=null&&fam.getHusband()!=indi)
        return;

      // dump the indi's name
      doc.nextParagraph("space-after=10pt,space-before=10pt");
      doc.addText(getName(indi, sosa, policy));

      // dump its properties
      String[] props = getProperties(indi, fam, policy, true, false);
      if (props.length>0) {

        // calculate indent
        String indent = "start-indent=10pt";

        if (reportFormat==ONE_LINE) {
          for (int p=0; p<props.length; p++) {
            if (p!=0) doc.addText(", ");
            doc.addText(props[p]);
          }
        } else {
          doc.startList(indent);
          for (int p=0; p<props.length; p++) {
            if (p!=0) doc.nextListItem();
            doc.addText(props[p]);
          }
          doc.endList();
        }
      }
      // done
    }

    /** end formatting */
    void formatEnd(Document doc) {
      // noop
    }

  }

  /**
   * A Sosa Table goes breadth first and generates a sosa-ascending table of properties
   *
   * 1;root;...
   * 2;father;...
   * 3;mother;...
   * 4;grandfather 1;...
   * 5;grandmother 1;...
   * 6;grandfather 2;...
   * 7;grandfather 3;...
   */
  class Table extends BreadthFirst  {

    // number + ";" + name + ";" + birth + ";" + marriage + ";" + death + ";" + occupation + ";" + residence;
    String[] header = { "Sosa", Gedcom.getName("NAME"), Gedcom.getName("BIRT"), Gedcom.getName("MARR"), Gedcom.getName("DEAT"), Gedcom.getName("OCCU"), Gedcom.getName("RESI") };

    /** our title - simply the column header values */
    String getTitle(Indi root) {
      StringBuffer result = new StringBuffer();
      for (int c=0;c<header.length;c++) {
        if (c>0) result.append(";");
        result.append(header[c]);
      }
     return result.toString();
    }

    /** this is called once at the beginning of the recursion - we add our table around it */
    void formatStart(Indi root, Document doc) {
      // open table first
      doc.startTable("genj:csv=true"); // this table is csv compatible
      // add header
      doc.nextTableRow();
      for (int i=0;i<header.length;i++) {
        doc.addTableColumn(""); // define a column
        if (i>0) doc.nextTableCell();
        doc.addText(header[i]);
      }
    }

    /** called at each generation - ignored */
    void formatGeneration(int gen, Document doc) {
      // noop
    }

    /** this is called at each recursion step - output table rows */
    void formatIndi(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {

      // grab properties - no prefixes, but all properties empty or not
      String[] props =  getProperties(indi, fam, policy, false, true);

      // start with a new row, sosa and name
      doc.nextTableRow();
      doc.addText(""+sosa);
      doc.nextTableCell();
      doc.addText(getName(indi, 0, policy)); //pass in 0 as sosa - don't want it as part of name
      doc.nextTableCell();

      // loop over props
      for (int i=0;i<props.length;i++) {
        if (i>0) doc.nextTableCell();
        doc.addText(props[i]);
      }

      // done for now
    }

    /** called at the end of the recursion - end our table */
    void formatEnd(Document doc) {
      // close table
      doc.endTable();
    }

  } //Table

}
