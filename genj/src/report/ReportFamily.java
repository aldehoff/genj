/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.gedcom.Gedcom;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Fam;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.report.Report;

/**
 * @author Carsten Müssig <carsten.muessig@gmx.net>
 * @version 1.0
 */

public class ReportFamily extends Report {
    
    /** bullets we use as prefix */
    private static final String EMPTY_PREFIX = "";
    private static final String STANDARD_PREFIX = "  ";
    private static final int SPACES_PER_LEVEL = 5;
    /** this report's version */
    public static final String VERSION = "1.0";    
    
    public String getVersion() {
        return VERSION;
    }
    
    /**
     * Author
     */
    public String getAuthor() {
        return "Carsten M\u00FCssig <carsten.muessig@gmx.net>";
    }
    
    /** Returns the name of this report - should be localized.
     */
    public String getName() {
        return i18n("name");
    }
    
    /**
     * Some information about this report
     * @return Information as String
     */
    public String getInfo() {
        return i18n("info");
    }    
    
    /**
     * @see genj.report.Report#accepts(java.lang.Object)
     */
    public String accepts(Object context) {
        // we accept GEDCOM or families
        return context instanceof Fam || context instanceof Gedcom ? getName() : null;
    }
    
    /**
     * This method actually starts this report
     */
    public void start(Object context) {
        Entity[] fams;
        Fam fam;
        
        // If we were passed a family to start at, use that
        if (context instanceof Fam) {
            fam = (Fam) context;
            analyzeFam(fam);
        } else {
            // Otherwise, ask the user select the root of the tree for analysis
            Gedcom gedcom = (Gedcom) context;
            fams = gedcom.getEntities(Gedcom.FAM,"");
            for(int i=0; i<fams.length; i++)
                analyzeFam((Fam)fams[i]);
        }
        // Done
    }
    
    private String getString(Object o) {
        if(o == null)
            return "";
        return o.toString();
    }
    
    private void analyzeFam(Fam f) {
        
        String temp = "";
        
        if((f.getMarriageDate()!=null) || (f.getProperty(new TagPath("FAM:MARR:PLAC"))!=null))
            temp = " oo "+getString(f.getMarriageDate())+" "+getString(f.getProperty(new TagPath("FAM:MARR:PLAC")));
        
        println("@"+f.getId()+"@ "+f+temp);
        analyzeIndi(f.getHusband(), f);
        analyzeIndi(f.getWife(), f);
        println(getIndent(1, SPACES_PER_LEVEL, STANDARD_PREFIX)+i18n("children"));
        analyzeChildren(f);
        println();
        println("=====");
        println();
    }
    
    private void analyzeIndi(Indi indi, Fam f) {
        
        if(indi==null)
            return;
        
        String temp = "";
        
        println(getIndent(1, SPACES_PER_LEVEL, STANDARD_PREFIX)+indi);
        if(indi.getFamc()!=null) {
            Fam parents = indi.getFamc();
            println(getIndent(2, SPACES_PER_LEVEL, " ")+i18n("parents")+": @"+parents.getId()+"@ "+parents);
        }
        else {
            Indi father = indi.getFather(), mother = indi.getMother();
            if((father!=null) || (mother!=null))
                println(getIndent(2, SPACES_PER_LEVEL, " ")+i18n("parents"));
            if(father!=null)
                println(getIndent(3, SPACES_PER_LEVEL, EMPTY_PREFIX)+": @"+father.getId()+"@ "+father);
            if(mother!=null)
                println(getIndent(3, SPACES_PER_LEVEL, EMPTY_PREFIX)+": @"+mother.getId()+"@ "+mother);
        }
        if( ((indi.getBirthAsString()!=null)&&(indi.getBirthAsString().length()>0)) || ((indi.getProperty(new TagPath("INDI:BIRT:PLAC"))!=null)&&(indi.getProperty(new TagPath("INDI:BIRT:PLAC")).toString().length()>0)) )
           println(getIndent(2, SPACES_PER_LEVEL, EMPTY_PREFIX)+" * "+getString(indi.getBirthAsString())+" "+getString(indi.getProperty(new TagPath("INDI:BIRT:PLAC"))));
        if(indi.getProperty("DEAT")!=null && ( (indi.getDeathAsString()!=null) || (indi.getProperty(new TagPath("INDI:DEAT:PLAC"))!=null) ) )
            println(getIndent(2, SPACES_PER_LEVEL, EMPTY_PREFIX)+" + "+getString(indi.getDeathAsString())+" "+getString(indi.getProperty(new TagPath("INDI:DEAT:PLAC"))));
        Fam[] families = indi.getFamilies();
        if(families.length > 1) {
            println(getIndent(2, SPACES_PER_LEVEL, STANDARD_PREFIX)+i18n("otherSpouses"));
            for(int i=0; i<families.length; i++) {
                if(families[i]!=f) {
                    if((families[i].getMarriageDate()!=null) || (families[i].getProperty(new TagPath("FAM:MARR:PLAC"))!=null))
                        temp = " oo "+getString(families[i].getMarriageDate())+" "+getString(families[i].getProperty(new TagPath("FAM:MARR:PLAC")));
                    println(getIndent(3, SPACES_PER_LEVEL, STANDARD_PREFIX)+"@"+families[i].getId()+"@ "+families[i]+temp);
                }
            }
        }
    }
    
    private void analyzeChildren(Fam f) {
        
        Indi[] children = f.getChildren();
        Indi child;
        Fam family;
        String temp = "";
        
        for(int i=0; i<children.length; i++) {
            child = children[i];
            println(getIndent(2, SPACES_PER_LEVEL, " ")+"@"+child.getId()+"@ "+child);
            if ( ((child.getBirthAsString()!=null)&&(child.getBirthAsString().length()>0)) || ((child.getProperty(new TagPath("INDI:BIRT:PLAC"))!=null)&&(child.getProperty(new TagPath("INDI:BIRT:PLAC")).toString().length()>0)) )
               println(getIndent(3, SPACES_PER_LEVEL, EMPTY_PREFIX)+"* "+getString(child.getBirthAsString())+" "+getString(child.getProperty(new TagPath("INDI:BIRT:PLAC"))));
            analyzeBaptism(child, "BAPM");
            analyzeBaptism(child, "BAPL");
            analyzeBaptism(child, "CHR");
            analyzeBaptism(child, "CHRA");
            if(child.getProperty("DEAT")!=null && ( (child.getDeathAsString()!=null) || (child.getProperty(new TagPath("INDI:DEAT:PLAC"))!=null) ) )
                println(getIndent(3, SPACES_PER_LEVEL, EMPTY_PREFIX)+"+ "+getString(child.getDeathAsString())+" "+getString(child.getProperty(new TagPath("INDI:DEAT:PLAC"))));
            Fam[] families = child.getFamilies();
            if(families.length > 0)
                println(getIndent(3, SPACES_PER_LEVEL, EMPTY_PREFIX)+i18n("spouses"));
            for(int j=0; j<families.length; j++) {
                family = (Fam)families[j];
                println(getIndent(4, SPACES_PER_LEVEL, EMPTY_PREFIX)+"@"+family.getId()+"@ "+family+" oo "+getString(family.getMarriageDate())+" "+getString(family.getProperty(new TagPath("FAM:MARR:PLAC"))));
            }            
        }
    }
    
    private void analyzeBaptism(Indi indi, String tag) {
        
        if(indi.getProperty(tag)!=null && ( (indi.getProperty(new TagPath("INDI:"+tag+":DATE"))!=null) || (indi.getProperty(new TagPath("INDI:"+tag+":PLAC"))!=null) ) )
            println(getIndent(3, SPACES_PER_LEVEL, EMPTY_PREFIX)+i18n("baptism")+" ("+tag+"): "+getString(((PropertyDate)indi.getProperty(new TagPath("INDI:"+tag+":DATE"))).toString())+" "+getString(indi.getProperty(new TagPath("INDI:"+tag+":PLAC"))));
    }    
} //ReportFamily
