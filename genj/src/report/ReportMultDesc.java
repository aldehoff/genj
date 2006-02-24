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
import genj.gedcom.Indi;
import genj.gedcom.PrivacyPolicy;
import genj.gedcom.Property;
import genj.gedcom.PropertyMultilineValue;
import genj.report.Report;

import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * GenJ - ReportMultDesc 
 * TODO Daniel titles statistics (nb pers distinctes, nbpers vivantes, nb fam, ...)
 * TODO Daniel: Remove bullet with possibly replacement with d'abboville number
 * TODO Daniel: Add table output (for csv)
 * TODO Daniel: reenable global privacy disabling
 */
public class ReportMultDesc extends Report {

  private final static String FORMAT_STRONG = "font-weight=bold";
  private final static String FORMAT_UNDERLINE = "text-decoration=underline";

  private int nbColumns;

  // Statistics
  private int nbIndi = 0;

  private int nbFam = 0;

  private int nbLiving = 0;

  private final static int ONE_LINE = 0, ONE_EVT_PER_LINE = 1;

  public int reportFormat = ONE_LINE;

  public String reportFormats[] = { translate("IndiPerLine"),
      translate("EventPerLine") };

  public int reportMaxGenerations = 999;

  public boolean showAllPlaceJurisdictions = false;

  public boolean reportPlaceOfBirth = true;

  public boolean reportDateOfBirth = true;

  public boolean reportPlaceOfDeath = true;

  public boolean reportDateOfDeath = true;

  public boolean reportPlaceOfMarriage = true;

  public boolean reportDateOfMarriage = true;

  public boolean reportPlaceOfOccu = true;

  public boolean reportDateOfOccu = true;

  public boolean reportPlaceOfResi = true;

  public boolean reportDateOfResi = true;

  public boolean reportMailingAddress = true;

  // Privacy
  public int publicGen = 0;
  
  /**
   * don't need stdout
   */
  public boolean usesStandardOut() {
    return true;
  }
  
  /**
   * use the fo image
   */
  protected ImageIcon getImage() {
    return Report.IMG_FO;
  }

  /**
   * Main for argument individual
   */
  public void start(Indi indi) {
    start( new Indi[] { indi }, translate("title.descendant", indi.getName()));
  }

  /**
   * One of the report's entry point
   */
  public void start(Indi[] indis) {
    start( indis, getName() + " - " + indis[0].getGedcom().getName());
  }
  
  /**
   * Our main private report point
   */
  private void start(Indi[] indis, String title) {
    
    // keep track of who we looked at already
    HashMap done = new HashMap();

    // Init some stuff
    PrivacyPolicy policy = OPTIONS.getPrivacyPolicy();

    nbColumns = 2;
    if (reportPlaceOfBirth || reportDateOfBirth)
      nbColumns++;
    if (reportPlaceOfMarriage || reportDateOfMarriage)
      nbColumns++;
    if (reportPlaceOfDeath || reportDateOfDeath)
      nbColumns++;
    if (reportPlaceOfOccu || reportDateOfOccu)
      nbColumns++;
    if (reportPlaceOfResi || reportDateOfResi)
      nbColumns++;

    Document doc = new Document(title);
    
    // iterate into individuals and all its descendants
    for (int i = 0; i < indis.length; i++) {
      Indi indi = indis[i];
      doc.startSection( translate("title.descendant", indi.getName()) );
      iterate(indi, 1, (new Integer(i+1).toString()), done, policy, doc);
    }

    doc.startSection( translate("title.stats") );
    doc.addText( translate("nb.fam", nbFam) );
    doc.nextParagraph();
    doc.addText( translate("nb.indi", nbIndi) );
    doc.nextParagraph();
    doc.addText( translate("nb.living", nbLiving) );

    // done
    showDocumentToUser(doc);

  }

  /**
   * Generate descendants information for one individual
   */
  private void iterate(Indi indi, int level, String num, HashMap done, PrivacyPolicy policy, Document doc) {
    
    nbIndi++;
    if (indi!=null&&!indi.isDeceased()) nbLiving ++;

    // no more?
    if (level > reportMaxGenerations)
      return;

    // still in a public generation?
    PrivacyPolicy localPolicy = level < publicGen + 1 ? PrivacyPolicy.PUBLIC : policy;

    // format the indi's information
    doc.startList();
    format(indi, (Fam)null, num, localPolicy, doc);

    // And we loop through its families
    Fam[] fams = indi.getFamiliesWhereSpouse();
    for (int f = 0; f < fams.length; f++) {
      
      // .. here's the fam and spouse
      Fam fam = fams[f];

      Indi spouse = fam.getOtherSpouse(indi);

      // output the spouse
        if (fams.length==1)
    	    format(spouse,fam,"x", localPolicy, doc); 
    	else 
    	    format(spouse,fam,"x"+(f+1), localPolicy, doc); 

      // put out a link if we've seen the spouse already
      if (done.containsKey(fam)) {
        doc.nextParagraph();
        doc.addText("====> " + translate("see") +" ");
        doc.addLink((String)done.get(fam), fam);
      } else {

   	    doc.addAnchor(fam);
          done.put(fam,num);
        nbIndi++;
        nbFam++;
        if (spouse!=null&&!spouse.isDeceased()) nbLiving ++;
        
        // .. and all the kids
        Indi[] children = fam.getChildren();
        for (int c = 0; c < children.length; c++) {
          // do the recursive step
          if (fams.length == 1)
            iterate(children[c], level + 1, num+'.'+(c+1), done, policy, doc);
          else
            iterate(children[c], level + 1, num+'x'+(f+1)+'.'+(c+1), done, policy, doc);
          
          // .. next child
        }
        
      }
      // .. next family
    }
    
    // done
    doc.endList();
  }

  /**
   * resolves the information of one Indi
   */
  private void format(Indi indi, Fam fam, String prefix, PrivacyPolicy policy, Document doc) {

    // Might be null
    if (indi == null)
      return;

    // FIXME Nils re-enable anchors for individuals processes
    
    doc.nextParagraph();
	doc.nextListItem("genj:label="+prefix);
	doc.addText(policy.getDisplayValue(indi, "NAME"), FORMAT_STRONG);
    doc.addText(" (" + indi.getId() + ")" );
    
    String birt = format(indi, "BIRT", OPTIONS.getBirthSymbol(), reportDateOfBirth, reportPlaceOfBirth, policy);
    String marr = fam!=null ? format(fam, "MARR", OPTIONS.getMarriageSymbol(), reportDateOfMarriage, reportPlaceOfMarriage, policy) : "";
    String deat = format(indi, "DEAT", OPTIONS.getDeathSymbol(), reportDateOfDeath, reportPlaceOfDeath, policy);
    String occu = format(indi, "OCCU", "{$T}{ $V}", reportDateOfOccu, reportPlaceOfOccu, policy);
    String resi = format(indi, "RESI", "{$T}", reportDateOfResi, reportPlaceOfResi, policy);
    PropertyMultilineValue addr = reportMailingAddress ? indi.getAddress() : null;
    if (addr != null && policy.isPrivate(addr)) addr = null;

//    if (outputFormat == TEXT_CSV) {
//      String separator = " ";
//      result = "";
//      if (birth != null && birth.length() != 0) {
//        result += separator + birth;
//        separator = "; ";
//      }
//      if (marriage != null && marriage.length() != 0) {
//        result += separator + marriage;
//        separator = "; ";
//      }
//      if (death != null && death.length() != 0) {
//        result += separator + death;
//        separator = "; ";
//      }
//      if (occupation != null && occupation.length() != 0) {
//        result += separator + occupation;
//        separator = "; ";
//      }
//      if (residence != null && residence.length() != 0) {
//        result += separator + residence;
//        separator = "; ";
//      }
//      result = output.cell(number) + output.cell(name) + output.cell(result);
//      if (address != null) {
//        String[] lines = address.getLines();
//        for (int i = 0; i < lines.length; i++) {
//          result += output.cell(lines[i]);
//        }
//      }
//      result = output.row(result);
//    } 
    
    // dump the information
    if (reportFormat!=ONE_LINE) 
      doc.startList();
    
    String[] infos = new String[] { birt, marr, deat, occu, resi };
    for (int i=0, j=0; i<infos.length ; i++) {
      if (infos[i].length()==0)
        continue;
      if (++j>1) {
        if (reportFormat==ONE_LINE)  doc.addText(", ");
        else doc.nextListItem();
      }
      doc.addText(infos[i]);
    }
    
    if (reportFormat!=ONE_LINE) 
      doc.endList();
    
    // done
  }
  
  /**
   * convert given prefix, date and place switches into a format string
   */
  private String format(Entity e, String tag, String prefix, boolean date, boolean place, PrivacyPolicy policy) {
    
    Property prop = e.getProperty(tag);
    if (prop == null)
      return "";

    String format = prefix + (date ? "{ $D}" : "")
        + (place && showAllPlaceJurisdictions ? "{ $P}" : "")
        + (place && !showAllPlaceJurisdictions ? "{ $p}" : "");

    return prop.format(format, policy);

  }

} // ReportMulDesv
