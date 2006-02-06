/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Fam;
import genj.gedcom.Indi;
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
    public String reportFormats[] = { i18n("IndiPerLine"),
				      i18n("EventPerLine") };

    private final static int
	HTML = 0,
	TEXT = 1,
	TEXT_CSV = 2;
    public int outputFormat=HTML;
    public String outputFormats[] = { i18n("Html")
				      ,i18n("Text")
				      ,"Csv"
    };

    private int columnWidth = 30;
    public int reportMaxGenerations = 999;

    private final static int
	PLACE_LONG = 0,
	PLACE_FIRST = 1,
	PLACE_EXACT = 2;
    public int placeFormat=ONE_LINE;
    public String placeFormats[] = {i18n("Long") ,i18n("First"), i18n("Exact")};
    public int placeJurisdictionIndex = 2;
    private int placeIndex = -placeJurisdictionIndex;

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
    public boolean managePrivacy = true;
    public int privateYears = 100;
    public int privateGen = 999;
    public boolean deadIsPublic=false;
    public String privateEvent=i18n("PrivateEventString");
    public String privateName=i18n("PrivateNameString");
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
	if (placeFormat==PLACE_LONG)
	    placeIndex = 0;
	else if (placeFormat==PLACE_EXACT)
	    placeIndex = placeJurisdictionIndex;
	else 
	    placeIndex = -placeJurisdictionIndex;

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

	output.setPrivacy(managePrivacy,
			  privateYears,
			  deadIsPublic,
			  privateTag);

	nbColumns = 2;
	if (reportPlaceOfBirth ||  reportDateOfBirth) nbColumns++;
	if (reportPlaceOfMarriage || reportDateOfMarriage) nbColumns++;
	if (reportPlaceOfDeath  || reportDateOfDeath) nbColumns++;
	if (reportPlaceOfOccu || reportDateOfOccu) nbColumns++;
	if (reportPlaceOfResi || reportDateOfResi) nbColumns++;

	output.start();
	// iterate into individuals and all its descendants
	for (int i = 0; i < indis.length; i++)
	    iterate(indis[i], 1, new Integer(i+1).toString(), primary);

	output.println(output.h(1,i18n("title.stats")));
	output.println(i18n("nb.fam")+nbFam);
	output.println(i18n("nb.indi")+nbIndi);
	output.println(i18n("nb.living")+nbLiving);
 
	// Done
	output.end();

    }    

    
private void iterate(Indi indi, int level, String num, Map primary) {
    boolean addIndi=true;

    if (level > reportMaxGenerations) return;
    // Here comes the individual
    if (level == 1)
	output.println(output.h(1,i18n("title.descendant",indi.getName())));

    output.println(format(indi, null, num, level));
           
    // And we loop through its families
    Fam[] fams = indi.getFamiliesWhereSpouse();
    for (int f=0;f<fams.length;f++) {
	// .. here's the fam and spouse
	Fam fam = fams[f];
	Indi spouse= fam.getOtherSpouse(indi);
            
	// .. a line for the spouse
	if (fams.length==1)
	    //	    output.println(format(spouse,fam,getIndent(level)+formatString("x",num.length()), level)); 
	    output.println(format(spouse,fam,"x", level)); 
	else 
	    output.println(format(spouse,fam,"x"+(f+1), level)); 
	//	    output.println(format(spouse,fam,getIndent(level)+formatString("x"+(f+1),num.length()), level)); 
	
	Object seeIndi = primary.get(fam.getId());
	if (seeIndi != null){
	    // don't add indi if already seens
	    addIndi = false;
	    output.startIndent();
	    output.startIndent();
	    output.println("====> "+i18n("see")+output.hlink(seeIndi.toString(),seeIndi.toString()));
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
		    iterate(children[c], level+1,num+'.'+(c+1),primary);
                else
		    iterate(children[c], level+1,num+'x'+(f+1)+'.'+(c+1),primary);
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
    private String format(Indi indi, Fam fam, String num,int level) {

	String number;
	String name;
	String birth;
	String death;
	String marriage;
	String occupation;
	String residence;
	String address[];

	String result = new String();

	// Might be null
	if (indi==null) 
	    return "?";

	number = ""+num;
	number = output.anchor(number, number);
	name = output.strong(indi.getName())+" ("+indi.getId()+")";
	birth = Formatter.formatEvent(OPTIONS.getBirthSymbol(), indi, "BIRT", reportDateOfBirth, reportPlaceOfBirth, placeIndex);
	if (fam != null){
	    marriage = Formatter.formatEvent(OPTIONS.getMarriageSymbol(), fam, "MARR", reportDateOfMarriage, reportPlaceOfMarriage, placeIndex);
	} else {
	    marriage = "";
	}
	death = Formatter.formatEvent(OPTIONS.getDeathSymbol(), indi, "DEAT", reportDateOfDeath, reportPlaceOfDeath, placeIndex);
	occupation = Formatter.formatEvent(i18n("Job"), indi, "OCCU", reportDateOfOccu, reportPlaceOfOccu, placeIndex);
	residence = Formatter.formatEvent(i18n("Resi"), indi, "RESI", reportDateOfResi, reportPlaceOfResi, placeIndex);
	address = reportMailingAddress?Formatter.getAddr(indi,null):null;

	if (output.isPrivate(indi,fam,level>privateGen)){
	    name = (privateName.length() != 0)? privateName : name;
	    birth = (birth.length() != 0)? privateEvent:"";
	    marriage = (marriage.length() != 0)? privateEvent:"";
	    death = (death.length() != 0)? privateEvent:"";
	    occupation = (occupation.length() != 0)? privateEvent:"";
	    residence = (residence.length() != 0)? privateEvent:"";
	    address = null;
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
	    if (address != null)
		for (int i = 0; i<address.length; i++){
		    result += output.cell(address[i]);
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

} //ReportMulDesv
