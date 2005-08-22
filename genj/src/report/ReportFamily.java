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
    
    /**
     * Main for argument Gedcom
     */
    public void start(Gedcom gedcom) {
      Entity[] fams = gedcom.getEntities(Gedcom.FAM,"");
      for(int i=0; i<fams.length; i++) {
          analyzeFam((Fam)fams[i]);
          println();
          println("=====");
          println();
      }
    }
    
    /**
     * Main for argument Family
     */
    public void start(Fam fam) {
      analyzeFam(fam);
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
    
} //ReportFamily
