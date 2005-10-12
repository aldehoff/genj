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
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.report.Report;
import genj.util.WordBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


// a enlecer
import genj.util.DirectAccessTokenizer;


/**
 * GenJ - ReportDescendants
 */
public class ReportSosa extends Report {
    
    private String getFirstAvailableJurisdiction(int skip, PropertyPlace plac) {
      if (skip<0) throw new IllegalArgumentException("negative skip value");
    DirectAccessTokenizer jurisdictions = plac.getJurisdictions();
    String result = jurisdictions.get(skip);
    if (result == null) return ("");
    for (int i=skip+1; result.length()==0 && jurisdictions.get(i)!=null ;i++) 
      result = jurisdictions.get(i);
    return result;
  }

  private final static String SOSA_ORDER = i18n("SosaOrder");
  private final static String LINEAGE_ORDER = i18n("LineageOrder");
  /** options - open file after generation */
    public int outputOrder = 0;
    public String outputOrders[] = { SOSA_ORDER, LINEAGE_ORDER, i18n("AgnaticLineage") };

  
    private final static int
	ONE_LINE = 0,
	ONE_EVT_PER_LINE = 1;

    public int outputFormat=ONE_LINE;
    public String outputFormats[] = { i18n("IndiPerLine"), i18n("EventPerLine") };

    public int reportMaxGenerations = 999;
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
	if (outputOrder == 0) {
	    int curGen=0;
	    int gen=0;
	    for (Iterator ps = primary.keySet().iterator(); ps.hasNext(); ) {
		Integer p = (Integer)ps.next();
		gen = Integer.numberOfTrailingZeros(Integer.highestOneBit(p))+1;
		if (gen != curGen){
		    curGen = gen;
		    println(i18n("Generation") + " " + curGen + " ---");
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
	primary.put( sosa, format(indi,fam,sosa));

	// Output order is lineage
	if (outputOrder == 1) 
	    println("Gen:"+level+getIndent(level)+s);
	if (outputOrder == 2)
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
	if (outputOrder != 2 && mother != null){ iterate(mother, famc, level+1, sosa*2+1, primary);}
            
    }
    
    /**
     * format date and place
     */
    private String formatDateAndPlace(String symbol, Entity entity, String tag, boolean isDate, boolean isPlace) {
      
      // prop exists?
      if (entity==null)
        return "";
      Property prop = entity.getProperty(tag);
      if (prop==null)
        return "";
      
      WordBuffer result = new WordBuffer();
      PropertyDate date = isDate ? (PropertyDate) prop.getProperty("DATE") : null;
      PropertyPlace plac = isPlace ? (PropertyPlace) prop.getProperty("PLAC") : null;
      if (date != null || plac != null) {
        result.append(symbol);
        result.append(date.getDisplayValue());
        result.append(getFirstAvailableJurisdiction(1,plac));
      }
      return result.toString();
    }

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
      if (plac != null ) result.append(getFirstAvailableJurisdiction(1,plac));
      
      if (result.length() >0) 
	  return symbol+" " + result.toString();
      else 
	  return "";
    }
    
    
    /**
     * resolves the information of one Indi
     */
    private String format(Indi indi, Fam fam, int sosa) {

      // Might be null
      if (indi==null) 
          return "?";
      
      String result = new String("");
      result += String.format("% 6d ",(Integer)sosa);
      result += String.format("%s",indi.getName()+"("+indi.getId()+") ");
      result = formatBuffer(result,formatEvent(OPTIONS.getBirthSymbol(), indi, "BIRT", reportDateOfBirth, reportPlaceOfBirth),-43);
     
      result = formatBuffer(result,formatEvent(OPTIONS.getDeathSymbol(), indi, "DEAT", reportDateOfDeath, reportPlaceOfDeath),73);
      result = formatBuffer(result,formatEvent("Métiers:", indi, "OCCU", reportDateOfOccu, reportPlaceOfOccu),103);
      result = formatBuffer(result,formatEvent("Résidences:", indi, "RESI", reportDateOfResi, reportPlaceOfResi),133);
      result = formatBuffer(result,formatEvent(OPTIONS.getMarriageSymbol(), fam, "MARR", reportDateOfMarriage, reportPlaceOfMarriage),163);
      return result;
    }

/**
  * tabPos = <0 means firstpass
  */
    private String formatBuffer(String outBuffer,String event,int tabPos) {
	if (outputOrder == 1)
	    if (event != null && event.length() != 0)
		return (outBuffer+"; "+event);
	    else
		return outBuffer;
	if (outputFormat == ONE_LINE || tabPos < 0){
	    tabPos = Math.abs(tabPos);
	    return(String.format("%-"+tabPos+"s%s",outBuffer,event));
	}
	if (outputFormat == ONE_EVT_PER_LINE)
	    if (event != null && event.length() != 0)
		return(outBuffer+String.format("%n%-43s%s"," ",event));
	    else
		return outBuffer;
	return (outBuffer + event);
    }
} //ReportDescendants
