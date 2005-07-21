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
 * @author Carsten Muessig <carsten.muessig@gmx.net>
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
    
    /** localized strings */
    private final static String AGE = Gedcom.getName("AGE");
    private final static String VERSION = "1.31";
    
    public String getVersion() {
        return VERSION;
    }
    
    private String familyToString(Fam f) {
        Indi husband = f.getHusband(), wife = f.getWife();
        String str = "@"+f.getId()+"@ ";
        if(husband!=null)
            str = str + i18n("entity", new String[] {f.getHusband().getId(), f.getHusband().getName()} );
            if(husband!=null && wife!=null)
                str=str+" + ";
            if(wife!=null)
                str = str + i18n("entity", new String[] {f.getWife().getId(), f.getWife().getName()} );
                return str;
    }
    
    /**
     * Main for argument Indi
     */
    public void start(Indi indi) {
      
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
        if (prop == null || !prop.isValid())
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
        println(getIndent(3) + toPrint + prop.getDisplayValue());
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
        
        println(i18n("entity", new String[] {indi.getId(), indi.getName()} ));
        
        // print birth date (give up if none)
        PropertyDate birth = indi.getBirthDate();
        if (birth == null) {
            println(OPTIONS.getBirthSymbol()+" "+i18n("noData"));
            return;
        }
        println(OPTIONS.getBirthSymbol()+" " + birth);
        println();
        
        if (reportBaptismAge) {
            println(getIndent(2) + i18n("baptism"));
            boolean ok = false;
            ok |= analyzeEvent(!ok, indi, "BAPM", true);
            ok |= analyzeEvent(!ok, indi, "BAPL", true);
            ok |= analyzeEvent(!ok, indi, "CHR", true);
            ok |= analyzeEvent(!ok, indi, "CHRA", true);
            if(!ok)
                println(getIndent(3) + i18n("noData"));
            println();
        }
        
        if (reportMarriageAge) {
            println(getIndent(2) + i18n("marriage"));
            Fam[] fams = indi.getFamiliesWhereSpouse();
            if (fams.length > 0) {
                for (int i = 0; i < fams.length; i++) {
                    Fam fam = fams[i];
                    String text = getIndent(2)+OPTIONS.getMarriageSymbol() + " "+familyToString(fam)+": ";
                    if (fam.getMarriageDate() == null)
                        println(text + i18n("noData"));
                    else {
                        println(text + fam.getMarriageDate());
                        age = indi.getAge(fam.getMarriageDate().getStart());
                        printAge(age,3);
                    }
                }
            }
            else
                println(getIndent(3) + i18n("noData"));
            println();
        }
        
        if (reportAgeAtDivorce) {
            println(getIndent(2) + i18n("divorce"));
            Fam[] fams = indi.getFamiliesWhereSpouse();
            if (fams.length > 0) {
                for (int i = 0; i < fams.length; i++) {
                    Fam fam = fams[i];
                    if (fam.getDivorceDate() != null) {
                        println(getIndent(2)+OPTIONS.getDivorceSymbol() + " "+i18n("entity", new String[] {fam.getId(), fam.toString()}) + ": " + fam.getDivorceDate());
                        age = indi.getAge(fam.getDivorceDate().getStart());
                        printAge(age,3);
                    }
                }
            } else
                println(getIndent(3) + i18n("noData"));
        }
        
        if (reportAgeAtChildBirth) {
            println(getIndent(2) + i18n("childBirths"));
            Indi[] children = indi.getChildren();
            if (children.length > 0) {
                for (int i = 0; i < children.length; i++) {
                    Indi child = children[i];
                    String text = getIndent(2) + OPTIONS.getBirthSymbol()+" "+i18n("entity", new String[] {child.getId(), children[i].getName()})+": ";
                    PropertyDate cbirth = child.getBirthDate();
                    if (cbirth == null)
                        println(text + i18n("noData"));
                    else {
                        println(text + cbirth);
                        age = indi.getAge(cbirth.getStart());
                        printAge(age,3);
                    }
                }
            } else
                println(getIndent(3) + i18n("noData"));
            println();
        }
        
        if (reportAgeAtEmigration) {
            println(getIndent(2) + i18n("emigration"));
            boolean ok = analyzeEvent(true, indi, "EMIG", false);
            if(!ok)
                println(getIndent(3) + i18n("noData"));            
            println();
        }
        
        if (reportAgeAtImmigration) {
            println(getIndent(2) + i18n("immigration"));
            boolean ok = analyzeEvent(true, indi, "IMMI", false);
            if(!ok)
                println(getIndent(3) + i18n("noData"));            
            println();
        }
        
        if (reportAgeAtNaturalization) {
            println(getIndent(2) + i18n("naturalization"));
            boolean ok = analyzeEvent(true, indi, "NATU", false);
            if(!ok)
                println(getIndent(3) + i18n("noData"));
            println();
        }
        
        if (reportAgeAtDeath) {
            println(getIndent(2) + i18n("death"));
            PropertyDate death = indi.getDeathDate();
            if (death != null) {
                println(getIndent(2) + OPTIONS.getDeathSymbol()+" " + death);
                age = indi.getAge(indi.getDeathDate().getStart());
                printAge(age,3);
            } else
                println(getIndent(3) + i18n("noData"));
            println();
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
