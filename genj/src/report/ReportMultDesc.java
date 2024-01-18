/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.PrivacyPolicy;
import genj.gedcom.Property;
import genj.gedcom.PropertyMultilineValue;
import genj.report.Report;

import java.util.Map;
import java.util.TreeMap;

/**
 * GenJ - ReportMultDesc
 * TODO: 
 *   titles
 *   statistics (nb pers distinctes, nb pers vivantes, nb fam, ...)
 */
public class ReportMultDesc extends Report {
  
    private Formatter output;
    //    private String eol= System.getProperty("line.separator");
    private int nbColumns;
    // Statistics
    private int nbIndi=0;
    private int nbFam=0;
    private int nbLiving=0;

    private final static int
	ONE_LINE = 0,
	ONE_EVT_PER_LINE = 1;
    public int reportFormat=ONE_LINE;
    public String reportFormats[] = { translate("IndiPerLine"),
				      translate("EventPerLine") };

    private final static int
	HTML = 0,
	TEXT = 1,
	TEXT_CSV = 2;
    public int outputFormat=HTML;
    public String outputFormats[] = { translate("Html")
				      ,translate("Text")
				      ,"Csv"
    };

    private int columnWidth = 30;
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
    public int privateYears = 0;
    public int publicGen = 0;
    public boolean deadIsPublic=true;
    public String privateTag="_PRIV";


    /**
     * Main for argument individual
     */
    public void start(Indi indi) {
	Indi[] indis = {indi};
	start(indis);
    }
  /**
   * One of the report's entry point
   */
    public void start(Indi[] indis) {
	// prepare our index
	Map primary = new TreeMap();

	// Init some stuff

	if (outputFormat == HTML) {
	    output = new FormatterHtml(this);
	    ((FormatterHtml) output).
		setStyle("td.report{vertical-align:top;}"+
			 "div.indent {margin-left:30px;"+
			 "}"+
			 "p{margin-top:0;margin-bottom:0;"+
			 "text-indent: - 20px;"+
			 //"padding-left:20px;"+
			 "}"+
			 "h2.report{border-color:black;background-color:#f0f0f0;border-style:solid;border-width:0 0 2 0;text-transform:uppercase;}");
	}else if (outputFormat == TEXT){
	    output = new FormatterText(this);
	    ((FormatterText) output).setTabStop(new int[] {-7,
							   7+columnWidth,
							   7+columnWidth*2,
							   7+columnWidth*3,
							   7+columnWidth*4,
							   7+columnWidth*5,
							   7+columnWidth*6});
	    ((FormatterText) output).setNiceColumns(false);
	} else if (outputFormat == TEXT_CSV){
	    output = new FormatterCsv(this);
	}

    PrivacyPolicy policy = new PrivacyPolicy(deadIsPublic, privateYears, privateTag);

	nbColumns = 2;
	if (reportPlaceOfBirth ||  reportDateOfBirth) nbColumns++;
	if (reportPlaceOfMarriage || reportDateOfMarriage) nbColumns++;
	if (reportPlaceOfDeath  || reportDateOfDeath) nbColumns++;
	if (reportPlaceOfOccu || reportDateOfOccu) nbColumns++;
	if (reportPlaceOfResi || reportDateOfResi) nbColumns++;

	output.start();
	// iterate into individuals and all its descendants
	for (int i = 0; i < indis.length; i++)
	    iterate(indis[i], 1, new Integer(i+1).toString(), primary, policy);

	output.println(output.h(1,translate("title.stats")));
	output.println(translate("nb.fam", nbFam));
	output.println(translate("nb.indi", nbIndi));
	output.println(translate("nb.living", nbLiving));
 
	// Done
	output.end();

    }    

    
private void iterate(Indi indi, int level, String num, Map primary, PrivacyPolicy policy) {
    boolean addIndi=true;

    if (level > reportMaxGenerations) return;
    
    // still in a public generation?
    PrivacyPolicy localPolicy = level<publicGen+1 ? PrivacyPolicy.PUBLIC : policy;
    
    // Here comes the individual
    if (level == 1)
	output.println(output.h(1,translate("title.descendant",indi.getName())));

    output.println(format(indi, null, num, localPolicy));
           
    // And we loop through its families
    Fam[] fams = indi.getFamiliesWhereSpouse();
    for (int f=0;f<fams.length;f++) {
	// .. here's the fam and spouse
	Fam fam = fams[f];
	Indi spouse= fam.getOtherSpouse(indi);
            
	// .. a line for the spouse
	if (fams.length==1)
	    //	    output.println(format(spouse,fam,getIndent(level)+formatString("x",num.length()), level)); 
	    output.println(format(spouse,fam,"x", localPolicy)); 
	else 
	    output.println(format(spouse,fam,"x"+(f+1), localPolicy)); 
	//	    output.println(format(spouse,fam,getIndent(level)+formatString("x"+(f+1),num.length()), level)); 
	
	Object seeIndi = primary.get(fam.getId());
	if (seeIndi != null){
	    // don't add indi if already seens
	    addIndi = false;
	    output.startIndent();
	    output.startIndent();
	    output.println("====> "+translate("see")+output.hlink(seeIndi.toString(),seeIndi.toString()));
	    output.endIndent();
	    output.endIndent();
	} else {
	    nbFam++;
	    if (spouse != null){
		nbIndi++;
		nbLiving += (spouse.getProperty("DEAT") == null)? 1 : 0;
	    }
	    primary.put( fam.getId(),num);
            // .. and all the kids
            Indi[] children = fam.getChildren();
	    if (children.length !=0){
		output.startIndent();
	    }
            for (int c = 0; c < children.length; c++) {
		// do the recursive step
		if (fams.length==1)
		    iterate(children[c], level+1,num+'.'+(c+1),primary, policy);
                else
		    iterate(children[c], level+1,num+'x'+(f+1)+'.'+(c+1),primary, policy);
                // .. next child
            }
	    if (children.length !=0){
		output.endIndent();
	    }
	}
     	// .. next family
    }
    if (addIndi){
	nbIndi++;
	nbLiving += (indi.getProperty("DEAT") == null)? 1 : 0;
    }
}
    
    /**
     * resolves the information of one Indi
     */
  private String format(Indi indi, Fam fam, String num, PrivacyPolicy policy) {

	String number;
	String name;
	String birth;
	String death;
	String marriage;
	String occupation;
	String residence;
	PropertyMultilineValue address = null;

	String result = new String();

	// Might be null
	if (indi==null) 
	    return "?";
  
	number = ""+num;
	number = output.anchor(number, number);
	name = output.strong( policy.getDisplayValue(indi, "NAME") )+" ("+indi.getId()+")";
	birth = format(indi, "BIRT", OPTIONS.getBirthSymbol(), reportDateOfBirth, reportPlaceOfBirth, policy);
	if (fam != null){
	    marriage = format(fam, "MARR", OPTIONS.getMarriageSymbol(),reportDateOfMarriage, reportPlaceOfMarriage, policy);
	} else {
	    marriage = "";
	}
	death = format(indi, "DEAT", OPTIONS.getDeathSymbol(), reportDateOfDeath, reportPlaceOfDeath, policy);
	occupation = format(indi, "OCCU", "{$T}{ $V}", reportDateOfOccu, reportPlaceOfOccu, policy);
	residence = format(indi, "RESI", "{$T}", reportDateOfResi, reportPlaceOfResi, policy);
	if (reportMailingAddress) {
	  address = indi.getAddress();
      if (address!=null&&policy.isPrivate(address)) address = null;
    }

	if (outputFormat == TEXT_CSV) {
	    String separator=" ";
	    result = "";
	    if (birth != null && birth.length() != 0){ 
		result += separator + birth;
		separator = "; ";
	    }
	    if (marriage != null && marriage.length() != 0){
		result += separator + marriage;
		separator = "; ";
	    }
	    if (death != null && death.length() != 0){
		result += separator + death;
		separator = "; ";
	    }
	    if (occupation != null && occupation.length() != 0){
		result += separator + occupation;
		separator = "; ";
	    }
	    if (residence != null && residence.length() != 0){
		result += separator + residence;
		separator = "; ";
	    }
	    result = output.cell(number)
		+output.cell(name)
		+output.cell(result);
	    if (address != null) {
	        String[] lines = address.getLines();
    		for (int i = 0; i<lines.length; i++){
    		    result += output.cell(lines[i]);
    		}
        }
	    result = output.row(result);
	} else {
	    if (reportFormat == ONE_LINE) {
		String separator=" ";
		result = output.underline(number);
		result += " " + name;
		if (birth != null && birth.length() != 0){ 
		    result += separator + birth;
		    separator = "; ";
		}
		if (marriage != null && marriage.length() != 0){
		    result += separator + marriage;
		    separator = "; ";
		}
		if (death != null && death.length() != 0){
		    result += separator + death;
		    separator = "; ";
		}
		if (occupation != null && occupation.length() != 0){
		    result += separator + occupation;
		    separator = "; ";
		}
		if (residence != null && residence.length() != 0){
		    result += separator + residence;
		    separator = "; ";
		}
	    } else {
		result = "";
		if (birth != null) result += output.li(birth);
		if (marriage != null) result += output.li(marriage);
		if (death != null) result += output.li(death);
		if (occupation != null) result += output.li(occupation);
		if (residence != null) result += output.li(residence);
		
		result = number+" "+name+output.ul(result);
	    }
	}
    	return result;
    }

  /** 
   * convert given prefix, date and place switches into a format string
   */
  private String format(Entity e, String tag, String prefix, boolean date, boolean place, PrivacyPolicy policy) {
    Property prop = e.getProperty(tag);
    if (prop==null)
      return "";
    
    String format = prefix + (date?"{ $D}":"")
      +(place&&showAllPlaceJurisdictions ? "{ $P}" : "")
      +(place&&!showAllPlaceJurisdictions ? "{ $p}" : "");
    
    return prop.format(format, policy);

  }
  

} //ReportMulDesv
