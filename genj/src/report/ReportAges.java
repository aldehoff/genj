/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Fam;
import genj.gedcom.PointInTime;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.report.Report;

import java.text.NumberFormat;

/**
 * GenJ - ReportAges
 * (based on ReportDescendants and ReportGedcomStatistics)
 *
 * @author Daniel P. Kionka
 * @author Carsten Müssig <carsten.muessig@gmx.net>
 * @version 1.0
 */

public class ReportAges extends Report {
    
    public boolean reportMarriageAge = true;
    public boolean reportAgeAtChildBirth = true;
    public boolean reportAgeSinceBirth = true;
    
    /** this report's version */
    public static final String VERSION = "1.0";
    
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
            indi = (Indi)getEntityFromUser(
            i18n("select"),   // msg
            gedcom,           // our gedcom instance
            Gedcom.INDI,      // type INDIVIDUALS
            "INDI:NAME"       // sort by name
            );
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
    PointInTime calculateAveragePointInTime(PropertyDate p) {
        
        if(p.isRange()) {
            String[] months = PointInTime.getMonths(false, true);
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
     * @param end end date for age calculation
     * @param pit point of time for the end
     * @return String[] : [day, month, year] or null if <CODE>end</CODE> < <CODE>birth</CODE>
     */
    private String[] calculateAge(Indi indi, PropertyDate end, PointInTime pit) {
        String[] str = {"0","0","0"};
        PropertyDate birth = indi.getBirthDate();
        PointInTime newBirth = calculateAveragePointInTime(birth);
        PointInTime newEnd = null;
        
        if((end==null)&&(pit==null))
            return null;
        
        if(end != null) {
            // end date < birth date
            if(end.compareTo(birth)<0)
                return null;
            // end date == birth date
            if(end.compareTo(birth)==0)
                return str;
            
            newEnd = calculateAveragePointInTime(end);
        }
        
        if(pit != null) {
            // end date < birth date
            if(pit.compareTo(newBirth)<0)
                return null;
            // end date == birth date
            if(pit.compareTo(newBirth)==0)
                return str;
            
            newEnd = pit;
        }
        
        int[] age = PointInTime.getDelta(newBirth, newEnd);
        str[0] = Integer.toString(age[0]);
        str[1] = Integer.toString(age[1]);
        str[2] = Integer.toString(age[2]);
        return str;
    }
    
    
    private void reportAges(Indi indi) {
        
        String[] age = new String[3];
        
        // give up if no birth date
        if (indi.getBirthDate() == null) {
            println("@"+indi.getId()+"@ "+indi.getName());
            println(i18n("noBirthDate"));
            return;
        }
        println("@"+indi.getId()+"@ "+indi.getName()+" *"+indi.getBirthDate());
        println();
        
        // calculate marriage age
        if(reportMarriageAge) {
            println(getIndent(1)+i18n("marriageAge"));
            Fam[] fams = indi.getFamilies();
            if(fams.length==0)
                println(getIndent(2)+i18n("noMarriage"));
            else {
                for(int i=0;i<fams.length;i++) {
                    Fam fam = fams[i];
                    println(getIndent(2)+fam.toString());
                    if(fam.getMarriageDate() == null)
                        println(getIndent(3)+i18n("noMarriageDate"));
                    else {
                        println(getIndent(3)+i18n("marriage", fam.getMarriageDate()));
                        age = calculateAge(indi, fam.getMarriageDate(), null);
                        if(age == null)
                            println(getIndent(4)+i18n("marriageBeforeBirth", fam.getMarriageDate()));
                        else
                            println(getIndent(4)+i18n("age")+" "+i18n("ageDisplay",age));
                    }
                }
            }
            println();
        }
        
        if(reportAgeAtChildBirth) {
            println(getIndent(1)+i18n("ageAtChildBirths"));
            Indi[] children = indi.getChildren();
            if(children.length==0)
                println(getIndent(2)+i18n("noChildren"));
            else {
                for(int i=0;i<children.length;i++) {
                    println(getIndent(2)+i18n("child")+" @"+children[i].getId()+"@ "+children[i].getName());
                    if(children[i].getBirthDate() == null)
                        println(getIndent(3)+i18n("noBirthDate"));
                    else
                        println(getIndent(3)+i18n("birth", children[i].getBirthDate()));
                    age = calculateAge(indi, children[i].getBirthDate(), null);
                    if(age == null)
                        println(getIndent(4)+i18n("childBirthBeforeBirth"));
                    else
                        println(getIndent(4)+i18n("age")+" "+i18n("ageDisplay",age));
                }
            }
            println();
        }
        
        println(getIndent(1)+i18n("deathAge"));
        if(indi.getDeathDate() == null)
            println(getIndent(2)+i18n("noDeathDate"));
        else {
            println(getIndent(2)+i18n("death", indi.getDeathDate()));
            age = calculateAge(indi, indi.getDeathDate(), null);
            if(age == null)
                println(getIndent(3)+i18n("deathBeforeBirth", indi.getDeathDate()));
            else
                println(getIndent(3)+i18n("age")+" "+i18n("ageDisplay",age));
        }
        
        if(reportAgeSinceBirth) {
            println();
            println(getIndent(1)+i18n("ageSinceBirth"));
            age = calculateAge(indi, null, PointInTime.getNow());
            println(getIndent(2)+i18n("ageDisplay",age));
        }
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
