/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

/**
 * GenJ - ReportAges
 * (based on ReportDescendants and ReportGedcomStatistics)
 *
 * @author Daniel P. Kionka
 * @author Carsten Müssig <carsten.muessig@gmx.net>
 * @version 1.1
 */

public class ReportAges extends Report {

  public boolean reportBaptismAge = true;
  public boolean reportMarriageAge = true;
  public boolean reportAgeAtDivorce = true;
  public boolean reportAgeAtChildBirth = true;
  public boolean reportAgeAtEmigration = true;
  public boolean reportAgeAtImmigration = true;
  public boolean reportAgeAtNaturalization = true;
  public boolean reportAgeAtDeath = true;
  public boolean reportAgeSinceBirth = true;

  /** bullets we use as prefix */
  private final static String[] BULLETS = { " = ", " - ", "   "};

  /** localized strings */
  private final static String 
    AGE = Gedcom.getName("AGE"), 
    BAPM = Gedcom.getName("BAPM"), 
    MARR = Gedcom.getName("MARR"), 
    DIV = Gedcom.getName("DIV"), 
    BIRT = Gedcom.getName("BIRT");

  /**
   * @see genj.report.Report#accepts(java.lang.Object)
   */
  public String accepts(Object context) {
    // we accept GEDCOM or Individuals
    return context instanceof Indi || context instanceof Gedcom ? getName() : null;
  }

  /**
   * This method actually starts this report
   */
  public void start(Object context) {
    Indi indi;

    // If we were passed a person to start at, use that
    if (context instanceof Indi) {
      indi = (Indi) context;
    } else {
      // Otherwise, ask the user select the root of the tree for analysis
      Gedcom gedcom = (Gedcom) context;
      indi = (Indi) getEntityFromUser(i18n("select"), gedcom, Gedcom.INDI);
    }

    if (indi == null)
      return;

    // Display the ages
    analyzeIndi(indi);

    // Done
  }

  /**
   * Analyze an event and report its information, date and age of indi
   */
  private boolean analyzeEvent(boolean header, Indi indi, String tag, boolean printTag) {

    // check for date under tag        
    PropertyDate prop = (PropertyDate) indi.getProperty(new TagPath("INDI:" + tag + ":DATE"));
    if (prop == null)
      return false;

    // do the header
    if (header)
      println(getIndent(0) + Gedcom.getName(tag) + ':');

    // format and ouput  
    String toPrint = "";
    if (printTag)
      toPrint = "INDI:" + tag + ": ";
    println(getIndent(1) + toPrint + prop.toString(true));
    Delta age = indi.getAge(prop.getStart());
    printAge(age);

    // done
    return true;
  }

  /**
   * Analyze and report ages for given individual
   */
  private void analyzeIndi(Indi indi) {

    Delta age = null;

    println("@" + indi.getId() + "@ " + indi.getName());
    println();

    // print birth date (give up if none)
    PropertyDate birth = indi.getBirthDate();
    println(getIndent(0) + BIRT + ':');
    if (birth == null) {
      println(getIndent(1) + i18n("noData"));
      return;
    }
    println(getIndent(1) + birth);
    println();

    if (reportBaptismAge) {
      boolean ok = false;
      ok |= analyzeEvent(!ok, indi, "BAPM", true);
      ok |= analyzeEvent(!ok, indi, "BAPL", true);
      ok |= analyzeEvent(!ok, indi, "CHR", true);
      ok |= analyzeEvent(!ok, indi, "CHRA", true);
      if (ok)
        println();
    }

    if (reportMarriageAge) {
      Fam[] fams = indi.getFamilies();
      if (fams.length > 0) {
        println(getIndent(0) + MARR + ':');
        for (int i = 0; i < fams.length; i++) {
          Fam fam = fams[i];
          String text = "@" + fam.getId() + "@ " + fam.toString() + ": ";
          if (fam.getMarriageDate() == null)
            println(getIndent(1) + text + i18n("noData"));
          else {
            println(getIndent(1) + text + fam.getMarriageDate());
            age = indi.getAge(fam.getMarriageDate().getStart());
            printAge(age);
          }
        }
        println();
      }
    }

    if (reportAgeAtDivorce) {
      Fam[] fams = indi.getFamilies();
      if (fams.length > 0) {
        String header = getIndent(0) + DIV + ':';
        for (int i = 0; i < fams.length; i++) {
          Fam fam = fams[i];
          if (fam.getDivorceDate() != null) {
            if (header != null) {
              println(header);
              header = null;
            }
            String text = "@" + fam.getId() + "@ " + fam.toString() + ": ";
            println(getIndent(1) + text + fam.getDivorceDate());
            age = indi.getAge(fam.getDivorceDate().getStart());
            printAge(age);
          }
        }
        if (header == null)
          println();
      }
    }

    if (reportAgeAtChildBirth) {
      Indi[] children = indi.getChildren();
      if (children.length > 0) {
        println(getIndent(0) + i18n("childBirths"));
        for (int i = 0; i < children.length; i++) {
          Indi child = children[i];
          String text = "@" + child.getId() + "@ " + children[i].getName() + ": ";
          PropertyDate cbirth = child.getBirthDate();
          if (cbirth == null)
            println(getIndent(1) + text + i18n("noData"));
          else {
            println(getIndent(1) + text + cbirth);
            age = indi.getAge(cbirth.getStart());
            printAge(age);
          }
        }
        println();
      }
    }

    if (reportAgeAtEmigration) {
      if (analyzeEvent(true, indi, "EMIG", false))
        println();
    }

    if (reportAgeAtImmigration) {
      if (analyzeEvent(true, indi, "IMMI", false))
        println();
    }

    if (reportAgeAtNaturalization) {
      if (analyzeEvent(true, indi, "NATU", false))
        println();
    }

    if (reportAgeAtDeath) {
      PropertyDate death = indi.getDeathDate();
      if (death != null) {
        println(getIndent(0) + Gedcom.getName("DEAT") + ':');
        println(getIndent(1) + death);
        age = indi.getAge(indi.getDeathDate().getStart());
        printAge(age);
        println();
      }
    }

    if (reportAgeSinceBirth) {
      PointInTime now = PointInTime.getNow();
      println(getIndent(0) + i18n("sinceBirth"));
      println(getIndent(1) + now);
      age = indi.getAge(now);
      printAge(age);
    }
  }

  /**
   * Print a computed age with given indent
   */
  private void printAge(Delta age) {
    if (age == null)
      println(getIndent(2) + i18n("error"));
    else
      println(getIndent(2) + AGE + ": " + age);
  }

  /**
   * Return an indented string including appropriate prefix for given level
   */
  private String getIndent(int level) {
    return super.getIndent(level + 1, 4, BULLETS[level]);
  }

} //ReportAges
