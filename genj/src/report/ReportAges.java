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
import genj.report.Options;
import genj.report.Report;

/**
 * GenJ - ReportAges
 * (based on ReportDescendants and ReportGedcomStatistics)
 *
 * @author Daniel P. Kionka
 * @author Carsten Müssig <carsten.muessig@gmx.net>
 * @version 1.3
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
    
    /** program options */
    private static final Options OPTIONS = Options.getInstance();
    
    /** localized strings */
    private final static String AGE = Gedcom.getName("AGE");
    private final static String VERSION = "1.3";
    
    public String getVersion() {
        return VERSION;
    }
    
    /**
     * Author
     */
    public String getAuthor() {
        return "Daniel P. Kionka, Carsten M\u00FCssig <carsten.muessig@gmx.net>";
    }
    
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
        
        int indent = 3;
        
        // check for date under tag
        PropertyDate prop = (PropertyDate) indi.getProperty(new TagPath("INDI:" + tag + ":DATE"));
        if (prop == null)
            return false;
        
        // do the header
        if (header)
            println(getIndent(2) + Gedcom.getName(tag) + ':');
        
        // format and ouput
        String toPrint = "";
        if (printTag) {
            toPrint = "INDI:" + tag + ": ";
            indent = 4;
        }
        println(getIndent(3) + toPrint + prop.toString(true));
        Delta age = indi.getAge(prop.getStart());
        printAge(age, indent);
        
        // done
        return true;
    }
    
    /**
     * Analyze and report ages for given individual
     */
    private void analyzeIndi(Indi indi) {
        
        Delta age = null;
        
        String[] output = {indi.getId(), indi.getName()};
        println(i18n("entity", output));
        
        // print birth date (give up if none)
        PropertyDate birth = indi.getBirthDate();
        if (birth == null) {
            println(OPTIONS.getBirthSymbol()+" "+i18n("noData"));
            return;
        }
        println(OPTIONS.getBirthSymbol()+" " + birth);
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
                for (int i = 0; i < fams.length; i++) {
                    Fam fam = fams[i];
                    output[0] = fam.getId();
                    output[1] = fam.toString();
                    String text = getIndent(2)+OPTIONS.getMarriageSymbol() + " "+i18n("entity", output)+": ";
                    if (fam.getMarriageDate() == null)
                        println(text + i18n("noData"));
                    else {
                        println(text + fam.getMarriageDate());
                        age = indi.getAge(fam.getMarriageDate().getStart());
                        printAge(age,3);
                    }
                }
                println();
            }
        }
        
        if (reportAgeAtDivorce) {
            Fam[] fams = indi.getFamilies();
            if (fams.length > 0) {
                for (int i = 0; i < fams.length; i++) {
                    Fam fam = fams[i];
                    if (fam.getDivorceDate() != null) {
                        output[0] = fam.getId();
                        output[1] = fam.toString();
                        println(getIndent(2)+OPTIONS.getDivorceSymbol() + " "+i18n("entity", output)+": " + fam.getDivorceDate());
                        age = indi.getAge(fam.getDivorceDate().getStart());
                        printAge(age,3);
                    }
                }
            }
        }
        
        if (reportAgeAtChildBirth) {
            Indi[] children = indi.getChildren();
            if (children.length > 0) {
                for (int i = 0; i < children.length; i++) {
                    Indi child = children[i];
                    output[0] = child.getId();
                    output[1] = children[i].getName();
                    String text = getIndent(2) + OPTIONS.getBirthSymbol()+" "+i18n("entity", output)+": ";
                    PropertyDate cbirth = child.getBirthDate();
                    if (cbirth == null)
                        println(text + i18n("noData"));
                    else {
                        println(text + cbirth);
                        age = indi.getAge(cbirth.getStart());
                        printAge(age,3);
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
                println(getIndent(2) + OPTIONS.getDeathSymbol()+" " + death);
                age = indi.getAge(indi.getDeathDate().getStart());
                printAge(age,3);
                println();
            }
        }
        
        if (reportAgeSinceBirth) {
            PointInTime now = PointInTime.getNow();
            println(getIndent(2) + i18n("sinceBirth", now));
            age = indi.getAge(now);
            printAge(age,3);
        }
    }
    
    /**
     * Print a computed age with given indent
     */
    private void printAge(Delta age, int indent) {
        if (age == null)
            println(getIndent(indent) + i18n("error"));
        else
            println(getIndent(indent) + AGE + ": " + age);
    }
    
    /**
     * Return an indented string for given level
     */
    private String getIndent(int level) {
        return super.getIndent(level, OPTIONS.getIndentPerLevel(), null);
    }
    
} //ReportAges
