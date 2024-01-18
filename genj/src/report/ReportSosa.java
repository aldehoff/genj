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
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.report.Report;
import genj.util.WordBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * GenJ - ReportSosa
 * Types de rapports:
 *   - Tableau d'ascendance avec num sosa: une colonne par type d'evenement
 *   - Tableau d'ascendance Agnatique (uniquement les peres. Si pas de pere, la mere)
 *   - Liste d'ascendance suivant les lignees
 *
 * Format des rapports:
 *   - Une ligne par individu
 *   - Un evt par ligne
 * Type de sortie
 *   - Texte
 *   - Texte, colonnes tronquees
 *   - HTML
 * TODO: 
 * - read gedcom header place format to set placeJurisdictionIndex
 *   in a more comprehensive way.
 * - Tune .property file
 * - Add one event per line for lineage report
 ***** 1. modifier le core pour pouvoir sauvegarde le rapport correctement
 ***** 2. mettre une option pour sortir le rapport en mode texte uniquement (comme avant)
 ***** 3. supprimer la ligne vide dans le rapport en suivant la lignee
 ***** 4. separer les evenements par des virgules dans l'ascendance lignee
 * 5. faire un alignement en cas de debordement dans le rap lignee
 * 6. voir utilisation de la couleur
 ***** 7. modify generation xx formatting (cadre, souligne, ...)
 */
public class ReportSosa extends Report {
    private ReportOut output;
    private String eol= System.getProperty("line.separator");
    private int nbColumns;

    private final static int
	SOSA_REPORT = 0,
	LINEAGE_REPORT = 1,
	AGNATIC_REPORT = 2,
	CSV_REPORT = 3;
    public int reportType = SOSA_REPORT;
    public String reportTypes[] = { i18n("SosaReport"),
				     i18n("LineageReport"),
				    i18n("AgnaticReport"),
				    i18n("CsvReport")};

    private final static int
	ONE_LINE = 0,
	ONE_EVT_PER_LINE = 1;
    public int reportFormat=ONE_LINE;
    public String reportFormats[] = { i18n("IndiPerLine"),
				      i18n("EventPerLine") };

    private final static int
	HTML = 0,
	TEXT = 1,
	TEXT_EXACT = 2;
    public int outputFormat=HTML;
    public String outputFormats[] = { i18n("Html"),
				      i18n("Textfull"),
				      i18n("TextTrunc")};

    public int columnWidth = 30;

    public boolean showGenerations = true;
    public int reportMaxGenerations = 999;

    private final static int
	PLACE_LONG = 0,
	PLACE_FIRST = 1,
	PLACE_EXACT = 2;
    public int placeFormat=PLACE_FIRST;
    public String placeFormats[] = {i18n("Long") ,i18n("First"), i18n("Exact")};
    public int placeJurisdictionIndex = 2;
    private int placeIndex = -placeJurisdictionIndex;


    public boolean reportPlaceOfBirth = true;
    public boolean reportDateOfBirth = true;
    public boolean reportPlaceOfMarriage = true;
    public boolean reportDateOfMarriage = true;
    public boolean reportPlaceOfDeath = true;
    public boolean reportDateOfDeath = true;
    public boolean reportPlaceOfOccu = true;
    public boolean reportDateOfOccu = true;
    public boolean reportPlaceOfResi = true;
    public boolean reportDateOfResi = true;

    /**
     * Main for argument individual
     */
    public void start(Indi indi) {
	// prepare our index
	Map primary = new TreeMap();

	// Init some stuff
	if (placeFormat==PLACE_LONG)
	    placeIndex = 0;
	else if (placeFormat==PLACE_EXACT)
	    placeIndex = placeJurisdictionIndex;
	else 
	    placeIndex = -placeJurisdictionIndex;

	if (outputFormat == HTML && reportType != CSV_REPORT) {
	    output = new ReportOutHtml(this);
	    ((ReportOutHtml) output).
		setStyle("td.report{vertical-align:top;}"+
			 "div.indent {margin-left:30px;"+
			 "}"+
			 "p{margin-top:0;margin-bottom:0;"+
			 "text-indent: - 20px;"+
			 //"padding-left:20px;"+
			 "}"+
			 "h2.report{border-color:black;background-color:#f0f0f0;border-style:solid;border-width:0 0 2 0;text-transform:uppercase;}");
	}else {
	    output = new ReportOutText(this);
	    if (reportType == AGNATIC_REPORT) {
		((ReportOutText) output).setTabStop(new int[] {-7,
							       7+columnWidth,
							       7+columnWidth*2,
							       7+columnWidth*4,
							       7+columnWidth*5,
							       7+columnWidth*6,
							       7+columnWidth*7});
	    } else {
		((ReportOutText) output).setTabStop(new int[] {-7,
							       7+columnWidth,
							       7+columnWidth*2,
							       7+columnWidth*3,
							       7+columnWidth*4,
							       7+columnWidth*5,
							       7+columnWidth*6});
	    }
	    ((ReportOutText) output).setNiceColumns(((outputFormat == TEXT_EXACT) &&
						     (reportFormat != ONE_LINE)));
	}

	nbColumns = 2;
	if (reportPlaceOfBirth ||  reportDateOfBirth) nbColumns++;
	if (reportPlaceOfMarriage || reportDateOfMarriage) nbColumns++;
	if (reportPlaceOfDeath  || reportDateOfDeath) nbColumns++;
	if (reportPlaceOfOccu || reportDateOfOccu) nbColumns++;
	if (reportPlaceOfResi || reportDateOfResi) nbColumns++;

	output.start();
	switch (reportType){
	case AGNATIC_REPORT: 
	    output.println(output.h(1,i18n("title.agnatic",indi.getName())));
	    break;
	case SOSA_REPORT: 
	    output.println(output.h(1,i18n("title.sosa",indi.getName())));
	    break;
	case LINEAGE_REPORT:
	    output.println(output.h(1,i18n("title.lineage",indi.getName())));
	    break;
	case CSV_REPORT:
	    output.println(i18n("CsvHeader"));
	    break;
	default:
	    output.println(output.h(1,i18n("title.report",indi.getName())));
	    break;
	}
	// iterate into individual and all its ascendants
	iterate(indi, null, 1, 1, primary);
        
	// Report is sosa: prints from primary Tree
	if (reportType == SOSA_REPORT) {
	    int gen=0;
	    output.startTable();
	    for (Iterator ps = primary.keySet().iterator(); ps.hasNext(); ) {
		Integer p = (Integer)ps.next();
		if (showGenerations){
		    // skip one line between generations
		    if (1<<gen <= p.intValue()){
			if (gen != 0)
			    output.println();
			output.println(output.row(output.cell(output.h(2,i18n("Generation")+ " " + ++gen),0,nbColumns)));
		    }
		}
		println(primary.get(p));
	    }
	    output.endTable();
	}
	if (reportType == CSV_REPORT) {
	    for (Iterator ps = primary.keySet().iterator(); ps.hasNext(); ) {
		Integer p = (Integer)ps.next();
		println(primary.get(p));
	    }
	}
	// Done
	output.end();
    }
    
    /**
     * Iterates over ascendants
     */
    private void iterate(Indi indi, Fam fam, int level, int sosa, Map primary) {
       	if (level>reportMaxGenerations) return;
        
	String s = format(indi,fam,sosa,level);

	primary.put( new Integer(sosa), s);
	
	// Report is lineage
	if (reportType != SOSA_REPORT && reportType != CSV_REPORT) {
	    output.println(s);
	}
	
        // And we loop through its ascendants
        Fam famc = indi.getFamilyWhereBiologicalChild();
        if (famc==null) {
            return;
        }
	if (reportType == LINEAGE_REPORT) {
	    output.startIndent();
	}
        Indi father = famc.getHusband();
	Indi mother = famc.getWife();
	if (father != null){ iterate(father, famc, level+1, sosa*2, primary);}
	if (!(reportType == AGNATIC_REPORT && father != null)){
	    // With agnatic report, if no father then use mother
	    if (mother != null){ iterate(mother, famc, level+1, sosa*2+1, primary);}
	}
	if (reportType == LINEAGE_REPORT) {
	    output.endIndent();
	}
    }
    
        
    /**
     * resolves the information of one Indi
     */
    private String format(Indi indi, Fam fam, int sosa, int level) {

	String number;
	String name;
	String birth;
	String death;
	String marriage;
	String occupation;
	String residence;

	String result = new String();

	// Might be null
	if (indi==null) 
	    return "?";

	if (reportType == CSV_REPORT) {
	    result = ""+sosa;
	    result += ";"+indi.getName();
	    result += ";"+output.formatEvent(indi, "BIRT",true, true, placeIndex);
	    if (fam != null){
		result += ";"+output.formatEvent(fam, "MARR",true, true, placeIndex);
	    } else {
		result += ";";
	    }
	    result += ";"+output.formatEvent(indi, "DEAT",true, true, placeIndex);
	    result += ";"+output.formatEvent(indi, "OCCU",true, true, placeIndex);
	    result += ";"+output.formatEvent(indi, "RESI",true, true, placeIndex);
	    return result;
	}

	number = ""+sosa;
	name = output.strong(indi.getName())+" ("+indi.getId()+")";
	birth = output.formatEvent(OPTIONS.getBirthSymbol(), indi, "BIRT", reportDateOfBirth, reportPlaceOfBirth, placeIndex);
	if (fam != null){
	    if (reportType == AGNATIC_REPORT){
		marriage = output.formatEvent(OPTIONS.getMarriageSymbol()+" "+fam.getWife().getName(), fam, "MARR", reportDateOfMarriage, reportPlaceOfMarriage, placeIndex);
	    } else {
		marriage = output.formatEvent(OPTIONS.getMarriageSymbol(), fam, "MARR", reportDateOfMarriage, reportPlaceOfMarriage, placeIndex);
	    }
	} else {
	    marriage = "";
	}
	death = output.formatEvent(OPTIONS.getDeathSymbol(), indi, "DEAT", reportDateOfDeath, reportPlaceOfDeath, placeIndex);
	occupation = output.formatEvent(i18n("Job"), indi, "OCCU", reportDateOfOccu, reportPlaceOfOccu, placeIndex);
	residence = output.formatEvent(i18n("Resi"), indi, "RESI", reportDateOfResi, reportPlaceOfResi, placeIndex);

	if (reportType == AGNATIC_REPORT || reportType == SOSA_REPORT) {
	    if (reportFormat == ONE_LINE) {
		result = output.cell(number);
		result += output.cell(name);
		if (birth != null) result += output.cell(birth);
		if (marriage != null) result += output.cell(marriage);
		if (death != null) result += output.cell(death);
		if (occupation != null) result += output.cell(occupation);
		if (residence != null) result += output.cell(residence);
		result = output.row(result);
	    } else {
		result = "";
		if (birth != null) result += output.li(birth);
		if (marriage != null) result += output.li(marriage);
		if (death != null) result += output.li(death);
		if (occupation != null) result += output.li(occupation);
		if (residence != null) result += output.li(residence);

		result = output.cell(number)+
		    output.cell(name)+
		    output.cell(output.ul(result));
		result = output.row(result);
	    } 
	} else if (reportType == LINEAGE_REPORT) {
	    if (reportFormat == ONE_LINE) {
	    String separator=" ";
	    result = number;
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
    

} //ReportSosa
