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
    
    /**
     * @param ages all ages added up (unit: days)
     * @param numAges number of persons added up
     * @return double[] with average age
     */
    private double[] calculateAverageAge(double ages, double numAges) {
        
        double[] age = {0.0, 0.0, 0.0};
        
        // only calculate if paramaters != default or unvalid values
        if((numAges>0)&&(ages!=Integer.MAX_VALUE)&&(ages!=Integer.MIN_VALUE)) {
            age[0] = Math.floor(ages/360/numAges);
            ages = ages%(360*numAges);
            age[1] = Math.floor(ages/30/numAges);
            ages = ages%(30*numAges);
            age[2] = ages/numAges;
        }
        return age;
    }
    
    /** Calculates the average PointInTime if parameter is a range.
     *  Otherwise the "normal" point in time is returned.
     * @param d date for calculation
     * @return PointInTime average */
    private PointInTime calculateAveragePointInTime(PropertyDate p) {
        
        if(p.isRange()) {
            String[] months = PointInTime.GREGORIAN.getMonths(false);
            PointInTime a = p.getStart(), b = p.getEnd();
            double[] age = calculateAverageAge(a.getDay()+b.getDay()+a.getMonth()*30+b.getMonth()*30+a.getYear()*360+b.getYear()*360, 2);
            // calculateAverageAge returns int[] = {year, month, day}
            return PointInTime.getPointInTime((int)age[2]+" "+months[(int)age[1]]+" "+(int)age[0]);
        }
        
        return p.getStart();
    }
    
    /** Calculates the age of a individual. Ranges are taken into
     * consideration by PointInTime.getDelta(begin, end)/2.
     * The end point is given by <code>PropertyDate end</code> or
     * <code>PointInTime pit</code>.
     * @param indi individual for the calculation
     * @param end end date for age calculation (PropertyDate or PointInTime)
     * @return String[] : [day, month, year] or null if end < birth or end > death
     */
    private Delta calculateAge(Indi indi, Object end) {
        
        if(end==null)
            return null;
        
        PropertyDate birth = indi.getBirthDate(), death = indi.getDeathDate();
        PointInTime newBirth = calculateAveragePointInTime(birth);
        PointInTime newEnd = null;
        
        if(end instanceof PropertyDate) {
            PropertyDate date = (PropertyDate)end;
            // end date < birth date
            if(date.compareTo(birth)<0)
                return null;
            // end date > death date
            if((death != null) && (date.compareTo(death)>0))
                return null;
            
            // end date == birth date
            newEnd = calculateAveragePointInTime(date);
        }
        
        if(end instanceof PointInTime) {
            PointInTime pit = (PointInTime)end;
            newEnd = pit;
        }
        
        return Delta.get(newBirth, newEnd);
    }
    
    private boolean analyzeTag(Indi indi, String tag, boolean printTag, String errorMessage) {
        
        if((indi.getProperty(new TagPath("INDI:"+tag))!=null) && (indi.getProperty(new TagPath("INDI:"+tag+":DATE"))!=null)) {
            String toPrint = "";
            if(printTag)
                toPrint = "INDI:"+tag+": ";
            PropertyDate prop = (PropertyDate)indi.getProperty(new TagPath("INDI:"+tag+":DATE"));
            println(getIndent(2)+toPrint+prop.toString(true));
            Delta age = calculateAge(indi, prop);
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
            baptism[0] = analyzeTag(indi, "BAPM", true, "notInLifeTime");
            baptism[1] = analyzeTag(indi, "BAPL", true, "notInLifeTime");
            baptism[2] = analyzeTag(indi, "CHR", true, "notInLifeTime");
            baptism[3] = analyzeTag(indi, "CHRA", true, "notInLifeTime");
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
                        age = calculateAge(indi, fam.getMarriageDate());
                        printAge(age, 3, "notInLifeTime");
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
                        age = calculateAge(indi, fam.getDivorceDate());
                        printAge(age, 3, "notInLifeTime");
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
                        age = calculateAge(indi, cbirth);
                        printAge(age, 3, "notInLifeTime");
                    }
                }
            }
            println();
        }
        
        if(reportAgeAtEmigration) {
            println(getIndent(1)+i18n("emigration"));
            boolean b = analyzeTag(indi, "EMIG", false, "notInLifeTime");
            if(b==false)
                println(getIndent(2)+i18n("noData"));
            println();
        }
        
        if(reportAgeAtImmigration) {
            println(getIndent(1)+i18n("immigration"));
            boolean b = analyzeTag(indi, "IMMI", false, "notInLifeTime");
            if(b==false)
                println(getIndent(2)+i18n("noData"));
            println();
        }
        
        if(reportAgeAtNaturalization) {
            println(getIndent(1)+i18n("naturalization"));
            boolean b = analyzeTag(indi, "NATU", false, "notInLifeTime");
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
                age = calculateAge(indi, death);
                printAge(age, 3, "deathBeforeBirth");
            }
            println();
        }
        
        if(reportAgeSinceBirth) {
            PointInTime pit = PointInTime.getNow();
            println(getIndent(1)+i18n("sinceBirth"));
            println(getIndent(2)+pit);
            age = calculateAge(indi, pit);
            printAge(age, 3, "");
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
