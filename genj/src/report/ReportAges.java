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
    
    /** this report's version */
    public static final String VERSION = "1.2";
    
    /**
     * Returns the version of this script
     */
    public String getVersion() {
        return VERSION;
    }
    
    /**
     * Returns the name of this report - should be localized.
     */
    public String getName() {
        return i18n("ages");
    }
    
    /**
     * Some information about this report
     * @return Information as String
     */
    public String getInfo() {
        return i18n("info");
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
            indi = (Indi)context;
        } else {
            // Otherwise, ask the user select the root of the tree for analysis
            Gedcom gedcom=(Gedcom)context;
            indi = (Indi)getEntityFromUser(i18n("select"), gedcom, Gedcom.INDI);
        }
        
        if (indi==null)
            return;
        
        // Display the ages
        reportAges(indi);
        
        // Done
    }
    
    private boolean analyzeTag(Indi indi, String tag, boolean printTag, String errorMessage) {
        
        if((indi.getProperty(new TagPath("INDI:"+tag))!=null) && (indi.getProperty(new TagPath("INDI:"+tag+":DATE"))!=null)) {
            String toPrint = "";
            if(printTag)
                toPrint = "INDI:"+tag+": ";
            PropertyDate prop = (PropertyDate)indi.getProperty(new TagPath("INDI:"+tag+":DATE"));
            println(getIndent(2)+toPrint+prop.toString(true));
            Delta age = indi.getAge(prop.getStart());
            printAge(age, 3, errorMessage);
            return true;
        }
        return false;
    }
    
    private void reportAges(Indi indi) {
        
        Delta age = null;
        String toPrint = "";
        
        println("@"+indi.getId()+"@ "+indi.getName());
        println();
        
        // give up if no birth date
        PropertyDate birth = indi.getBirthDate();
        if (birth == null) {
            println(getIndent(1)+i18n("noBirthDate"));
            return;
        }
        // print birth date
        println(getIndent(1)+i18n("birth"));
        println(getIndent(2)+birth);
        println();
        
        if(reportBaptismAge) {
            boolean baptism[] = new boolean[4];
            println(getIndent(1)+i18n("baptism")+" "+i18n("seeDocumentation")+":");
            baptism[0] = analyzeTag(indi, "BAPM", true, "error");
            baptism[1] = analyzeTag(indi, "BAPL", true, "error");
            baptism[2] = analyzeTag(indi, "CHR", true, "error");
            baptism[3] = analyzeTag(indi, "CHRA", true, "error");
            if((baptism[0]==false) && (baptism[1]==false) && (baptism[2]==false) && (baptism[3]==false))
                println(getIndent(2)+i18n("noData"));
            println();
        }
        
        if(reportMarriageAge) {
            println(getIndent(1)+i18n("marriage"));
            Fam[] fams = indi.getFamilies();
            if(fams.length==0)
                println(getIndent(2)+i18n("noData"));
            else {
                for(int i=0;i<fams.length;i++) {
                    Fam fam = fams[i];
                    toPrint = "@"+fam.getId()+"@ "+fam.toString()+": ";
                    if(fam.getMarriageDate() == null)
                        println(getIndent(2)+toPrint+i18n("noData"));
                    else {
                        println(getIndent(2)+toPrint+fam.getMarriageDate());
                        age = indi.getAge(fam.getMarriageDate().getStart());
                        printAge(age, 3, "error");
                    }
                }
            }
            println();
        }
        
        if(reportAgeAtDivorce) {
            println(getIndent(1)+i18n("divorce"));
            Fam[] fams = indi.getFamilies();
            if(fams.length==0)
                println(getIndent(2)+i18n("noData"));
            else {
                for(int i=0;i<fams.length;i++) {
                    Fam fam = fams[i];
                    toPrint = "@"+fam.getId()+"@ "+fam.toString()+": ";
                    if(fam.getDivorceDate() == null)
                        println(getIndent(2)+toPrint+i18n("noData"));
                    else {
                        println(getIndent(2)+toPrint+fam.getDivorceDate());
                        age = indi.getAge(fam.getDivorceDate().getStart());
                        printAge(age, 3, "error");
                    }
                }
            }
            println();
        }
        
        if(reportAgeAtChildBirth) {
            println(getIndent(1)+i18n("childBirths"));
            Indi[] children = indi.getChildren();
            if(children.length==0)
                println(getIndent(2)+i18n("noData"));
            else {
                for(int i=0;i<children.length;i++) {
                    Indi child = children[i];
                    toPrint = "@"+child.getId()+"@ "+children[i].getName()+": ";
                    PropertyDate cbirth = child.getBirthDate();
                    if(cbirth == null)
                        println(getIndent(2)+toPrint+i18n("noData"));
                    else {
                        println(getIndent(2)+toPrint+cbirth);
                        age = indi.getAge(cbirth.getStart());
                        printAge(age, 3, "error");
                    }
                }
            }
            println();
        }
        
        if(reportAgeAtEmigration) {
            println(getIndent(1)+i18n("emigration"));
            boolean b = analyzeTag(indi, "EMIG", false, "error");
            if(b==false)
                println(getIndent(2)+i18n("noData"));
            println();
        }
        
        if(reportAgeAtImmigration) {
            println(getIndent(1)+i18n("immigration"));
            boolean b = analyzeTag(indi, "IMMI", false, "error");
            if(b==false)
                println(getIndent(2)+i18n("noData"));
            println();
        }
        
        if(reportAgeAtNaturalization) {
            println(getIndent(1)+i18n("naturalization"));
            boolean b = analyzeTag(indi, "NATU", false, "error");
            if(b==false)
                println(getIndent(2)+i18n("noData"));
            println();
        }
        
        if(reportAgeAtDeath) {
            println(getIndent(1)+i18n("death"));
            PropertyDate death = indi.getDeathDate();
            if(death == null)
                println(getIndent(2)+i18n("noData"));
            else {
                println(getIndent(2)+death);
                age = indi.getAge(indi.getDeathDate().getStart());
                printAge(age, 3, "error");
            }
            println();
        }
        
        if(reportAgeSinceBirth) {
            PointInTime now = PointInTime.getNow();
            println(getIndent(1)+i18n("sinceBirth"));
            println(getIndent(2)+now);
            age = indi.getAge(now);
            printAge(age, 3, "error");
        }
    }
    
    private void printAge(Delta age, int indent, String errorMessage) {
        if(age == null)
            println(getIndent(indent)+i18n(errorMessage));
        else
            println(getIndent(indent)+i18n("age")+" "+age);
    }
    
    
    private String getIndent(int level) {
        int l = level;
        StringBuffer buffer = new StringBuffer(256);
        while (--level>0) {
            buffer.append("     ");
        }
        switch(l) {
            case 1:
                buffer.append(" = "); break;
            case 2:
                buffer.append(" * "); break;
            case 3:
                buffer.append(" + "); break;
            case 4:
                buffer.append(" - "); break;
            case 5:
                buffer.append(" . "); break;
        }
        return buffer.toString();
    }
} //ReportAges
