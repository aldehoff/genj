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
 * TODO: read gedcom header place format to set placeJurisdictionIndex
 * in a more comprehensive way.
 */
public class ReportSosa extends Report {
    
  private String SOSA_ORDER = i18n("SosaOrder");
  private String LINEAGE_ORDER = i18n("LineageOrder");
  /** options - open file after generation */
    private final static int
	SOSA_REPORT = 0,
	LINEAGE_REPORT = 1,
	AGNATIC_REPORT = 2;

    public int outputOrder = SOSA_REPORT;
    public String outputOrders[] = { SOSA_ORDER, LINEAGE_ORDER, i18n("AgnaticLineage") };

  
    private final static int
	ONE_LINE = 0,
	ONE_EVT_PER_LINE = 1;

    public int outputFormat=ONE_LINE;
    public String outputFormats[] = { i18n("IndiPerLine"), i18n("EventPerLine") };

    public int reportMaxGenerations = 999;

    private final static int
	PLACE_LONG = 0,
	PLACE_FIRST = 1,
	PLACE_EXACT = 2;
    public int placeFormat=ONE_LINE;
    public String placeFormats[] = {i18n("Long") ,i18n("First"), i18n("Exact")};
    public int placeJurisdictionIndex = 2;    
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

    /**
     * Main for argument individual
     */
    public void start(Indi indi) {
	// prepare our index
	Map primary = new TreeMap();
    
	// iterate into individual and all its ascendants
	iterate(indi, null, 1, 1, primary);
        
	// Output order is sosa
	if (outputOrder == SOSA_REPORT) {
	    int gen=0;
	    for (Iterator ps = primary.keySet().iterator(); ps.hasNext(); ) {
		Integer p = (Integer)ps.next();
		if (1<<gen <= p.intValue()){
		    println("\n--- "+i18n("Generation")+ " " + ++gen + " ---");
		}
		println(primary.get(p));
	    }
	}
	// Done
    }
    
    /**
     * Iterates over ascendants
     */
	private void iterate(Indi indi, Fam fam, int level, int sosa, Map primary) {
       	if (level>reportMaxGenerations) return;
        
        // Here comes the individual
	String s = format(indi,fam,sosa);
	primary.put( new Integer(sosa), format(indi,fam,sosa));

	// Output order is lineage
	if (outputOrder == LINEAGE_REPORT) 
	    println(i18n("GenerationShort")+level+getIndent(level)+s);
	if (outputOrder == AGNATIC_REPORT)
	    println(s);

        // And we loop through its ascendants
        Fam famc = indi.getFamilyWhereBiologicalChild();
        
        if (famc==null) {
	    //  println("no Famc "+ format(indi));
            return;
        }
        
        Indi father = famc.getHusband();
	Indi mother = famc.getWife();
	if (father != null){ iterate(father, famc, level+1, sosa*2, primary);}
	if (outputOrder != AGNATIC_REPORT && mother != null){ iterate(mother, famc, level+1, sosa*2+1, primary);}
            
    }
    
    /**
     * format date and place
     */
    private String formatEvent(String symbol, Entity entity, String tag, boolean isDate, boolean isPlace) {
      
      // prop exists?
      if (entity==null)
        return "";
      Property prop = entity.getProperty(tag);
      if (prop==null)
        return "";
      
      WordBuffer result = new WordBuffer();
      PropertyDate date = isDate ? (PropertyDate) prop.getProperty("DATE") : null;
      PropertyPlace plac = isPlace ? (PropertyPlace) prop.getProperty("PLAC") : null;
      result.append(prop.getValue());
      if (date != null ) result.append(date.getDisplayValue());
      if (plac != null ) {
	  if (placeFormat == PLACE_LONG) 
	      result.append(plac.getDisplayValue());
	  else if (placeFormat == PLACE_FIRST)
	      result.append(plac.getFirstAvailableJurisdiction(placeJurisdictionIndex-1));
	  else
	      result.append(plac.getJurisdiction(placeJurisdictionIndex-1));
      }
      
      if (result.length() >0) 
	  return symbol+" " + result.toString();
      else 
	  return "";
    }
    
    
    /**
     * resolves the information of one Indi
     */
    private String format(Indi indi, Fam fam, int sosa) {

	int tabStop = 50;
      // Might be null
      if (indi==null) 
          return "?";
      
      String result = new String("");
      result += formatString(new Integer(sosa).toString(),7);
      result += " "+indi.getName()+" ("+indi.getId()+")";
      result = formatBuffer(result,formatEvent(OPTIONS.getBirthSymbol(), indi, "BIRT", reportDateOfBirth, reportPlaceOfBirth),-tabStop);
      tabStop += 30;
      if (fam != null)
	  if (outputOrder == AGNATIC_REPORT){
	      result = formatBuffer(result,formatEvent(OPTIONS.getMarriageSymbol()+" "+fam.getWife().getName(), fam, "MARR", reportDateOfMarriage, reportPlaceOfMarriage),tabStop);
	      tabStop += 70;
	  } else {
	      result = formatBuffer(result,formatEvent(OPTIONS.getMarriageSymbol(), fam, "MARR", reportDateOfMarriage, reportPlaceOfMarriage),tabStop);
	      tabStop += 30;
	  }
      result = formatBuffer(result,formatEvent(OPTIONS.getDeathSymbol(), indi, "DEAT", reportDateOfDeath, reportPlaceOfDeath),tabStop);
      tabStop += 30;
      if (reportDateOfOccu || reportPlaceOfOccu){
	  result = formatBuffer(result,formatEvent(i18n("Job"), indi, "OCCU", reportDateOfOccu, reportPlaceOfOccu),tabStop);
	  tabStop += 30;
      }
      if (reportDateOfResi || reportPlaceOfResi){
	  result = formatBuffer(result,formatEvent(i18n("Resi"), indi, "RESI", reportDateOfResi, reportPlaceOfResi),tabStop);
	  tabStop += 30;
      }
      return result;
    }

/**
  * tabPos = <0 means firstpass
  */
    private String formatBuffer(String outBuffer,String event,int tabPos) {
	if (outputOrder == LINEAGE_REPORT)
	    if (event != null && event.length() != 0)
		return (outBuffer+"; "+event);
	    else
		return outBuffer;
	if (outputFormat == ONE_LINE || tabPos < 0){
	    tabPos = Math.abs(tabPos);
	    return(formatString(outBuffer+" ",-tabPos)+event);
	}
	if (outputFormat == ONE_EVT_PER_LINE)
	    if (event != null && event.length() != 0)
		return(outBuffer+"\n"+formatString(" ",50)+event);
	    else
		return outBuffer;
	return (outBuffer + " "+event);
    }
    private String formatString(String s, int size){

	int width=Math.abs(size)-s.length();
	if (width <= 0)
	    return s;

	StringBuffer sbuf = new StringBuffer(width);
	for (int i = 0; i < width; ++i)
	    sbuf.append(' ');
    
	if (size > 0){
	    return sbuf+s;
	} else {
	    return s+sbuf;
	}
    }
} //ReportSosa
