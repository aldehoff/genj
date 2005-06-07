/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */


import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.TagPath;
import genj.report.Report;

/**
 * @author Carsten Muessig <carsten.muessig@gmx.net>
 * @version 1.02
 */

public class ReportFamily extends Report {
    
    public boolean reportParents = true;
    public boolean reportOtherSpouses = true;
    public boolean reportDetailedChildrenData = true;
    
    /** this report's version */
    public static final String VERSION = "1.02";
    
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
            for(int i=0; i<fams.length; i++) {
                analyzeFam((Fam)fams[i]);
                println();
                println("=====");
                println();
            }
        }
        // Done
    }
    
    private String trim(Object o) {
        if(o == null)
            return "";
        return o.toString();
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
    
    private void analyzeFam(Fam f) {
        println(familyToString(f));
        if( (trim(f.getMarriageDate()).length()>0) || (trim(f.getProperty(new TagPath("FAM:MARR:PLAC"))).length()>0) )
            println(OPTIONS.getMarriageSymbol()+" "+trim(f.getMarriageDate())+" "+trim(f.getProperty(new TagPath("FAM:MARR:PLAC"))));
        analyzeIndi(f.getHusband(), f);
        analyzeIndi(f.getWife(), f);
        analyzeChildren(f);
    }
    
    private void analyzeIndi(Indi indi, Fam f) {
        
        if(indi==null)
            return;
        
        println(getIndent(2)+i18n("entity", new String[] {indi.getId(), indi.getName()} ));
        
        if(reportParents) {
          Fam fam = indi.getFamilyWhereBiologicalChild();
            if(fam!=null)
                println(getIndent(3)+OPTIONS.getChildOfSymbol()+" "+familyToString(fam));
        }
        
        if( (trim(indi.getBirthAsString()).length()>0) || (trim(indi.getProperty(new TagPath("INDI:BIRT:PLAC"))).length()>0) )
            println(getIndent(3)+OPTIONS.getBirthSymbol()+" "+trim(indi.getBirthAsString())+" "+trim(indi.getProperty(new TagPath("INDI:BIRT:PLAC"))));
        if(indi.getProperty("DEAT")!=null && ( (trim(indi.getDeathAsString()).length()>0) || (trim(indi.getProperty(new TagPath("INDI:DEAT:PLAC"))).length()>0) ) )
            println(getIndent(3)+OPTIONS.getDeathSymbol()+" "+trim(indi.getDeathAsString())+" "+trim(indi.getProperty(new TagPath("INDI:DEAT:PLAC"))));
        if(reportOtherSpouses) {
            Fam[] families = indi.getFamiliesWhereSpouse();
            if(families.length > 1) {
                println(getIndent(3)+i18n("otherSpouses"));
                for(int i=0; i<families.length; i++) {
                    if(families[i]!=f) {
                        String str = "";
                        if((trim(families[i].getMarriageDate()).length()>0) || (trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC"))).length()>0))
                            str = OPTIONS.getMarriageSymbol()+" "+trim(families[i].getMarriageDate())+" "+trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC")))+" ";
                        println(getIndent(4)+str+" "+i18n("entity", new String[] {families[i].getId(), families[i].toString()} ));
                    }
                }
            }
        }
    }
    
    private void analyzeChildren(Fam f) {
        
        Indi[] children = f.getChildren();
        Indi child;
        Fam[] families;
        Fam family;
        
        if(children.length>0)
            println(getIndent(2)+i18n("children"));
        for(int i=0; i<children.length; i++) {
            child = children[i];
            println(getIndent(3)+i18n("entity", new String[] {child.getId(), child.getName()} ));
            if(reportDetailedChildrenData) {
                if ( (trim(child.getBirthAsString()).length()>0) || (trim(child.getProperty(new TagPath("INDI:BIRT:PLAC"))).length()>0) )
                    println(getIndent(4)+OPTIONS.getBirthSymbol()+" "+trim(child.getBirthAsString())+" "+trim(child.getProperty(new TagPath("INDI:BIRT:PLAC"))));
                printBaptism(child, "BAPM");
                printBaptism(child, "BAPL");
                printBaptism(child, "CHR");
                printBaptism(child, "CHRA");
                families = child.getFamiliesWhereSpouse();
                for(int j=0; j<families.length; j++) {
                    family = (Fam)families[j];
                    println(getIndent(4)+OPTIONS.getMarriageSymbol()+" "+i18n("entity", new String[] {family.getId(),  family.toString()} )+" "+trim(family.getMarriageDate())+" "+trim(family.getProperty(new TagPath("FAM:MARR:PLAC"))));
                }
                if(child.getProperty("DEAT")!=null && ( (trim(child.getDeathAsString()).length()>0) || (trim(child.getProperty(new TagPath("INDI:DEAT:PLAC"))).length()>0) ) )
                    println(getIndent(4)+OPTIONS.getDeathSymbol()+" "+trim(child.getDeathAsString())+" "+trim(child.getProperty(new TagPath("INDI:DEAT:PLAC"))));
            }
        }
    }
    
    private void printBaptism(Indi indi, String tag) {
        
        if( (indi.getProperty(tag)!=null) && ( (trim(indi.getProperty(new TagPath("INDI:"+tag+":DATE"))).length()>0) || (trim(indi.getProperty(new TagPath("INDI:"+tag+":PLAC"))).length()>0) ) )
            println(getIndent(4)+OPTIONS.getBaptismSymbol()+" ("+tag+"): "+trim(indi.getProperty(new TagPath("INDI:"+tag+":DATE")))+" "+trim(indi.getProperty(new TagPath("INDI:"+tag+":PLAC"))));
    }
    
    /**
     * Return an indented string for given level
     */
    private String getIndent(int level) {
        return super.getIndent(level, OPTIONS.getIndentPerLevel(), null);
    }
} //ReportFamily
