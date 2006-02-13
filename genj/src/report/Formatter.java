/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
/*
 * todo:
 */
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.time.Delta;
import genj.report.Report;
import genj.util.WordBuffer;

/**
 * GenJ - Formatter
 * Class set to hide presentation logic to report writers
 */
abstract class Formatter  {
    static final int 
	DATE_FORMAT_NONE=0,
	DATE_FORMAT_YEAR=1,
	DATE_FORMAT_LONG=2;
    protected Report parentReport;

    /**
     * Privacy
     */
    boolean managePrivacy = true;
    int privateYears = 100;
    boolean deadIsPublic=false;
    String privateTag="_PRIV";

    abstract void startTable();
    abstract void endTable();
    abstract String b(String s);
    abstract String em(String s);
    abstract String strong(String s);
    abstract String i(String s);
    abstract String underline(String s);
    abstract String row(String s);
    abstract String cell(String s);
    abstract String cell(String s, String attr);
    abstract String cell(String s, int rs, int cs);
    abstract String cell(String s, String attr, int rs, int cs);
    abstract void println(String s);
    abstract void println();
    abstract void print(String s);
    abstract String h(int level,String s);
    abstract String li(String s);
    abstract String ul(String s);
    abstract void startIndent();
    abstract void endIndent();
    abstract void start();
    abstract void end();
    abstract String hlink(String name, String text);
    abstract String anchor(String name, String text);
    abstract String br();
    abstract String page();
    abstract String tablePageBreak();

    /**
     * Constructor
     */
    protected Formatter(Report parent) {
	parentReport = parent;
    }

    /**
     * Privacy management
     */
    void setPrivacy(boolean managed,
	       int nbYears,
	       boolean dead,
	       String tag){

	managePrivacy = managed;
	privateYears = nbYears;
	deadIsPublic=dead;
	privateTag=tag;
    }
    void setPrivacy(boolean managed){
	setPrivacy(managed,100,false,"_PRIV");
    }
    
    boolean isPrivate(Entity indi,Entity fam, boolean genIsPrivate){
	if (!managePrivacy)
	    return false;
	if (deadIsPublic && indi.getProperty("DEAT") != null)
	    return false;
	if (genIsPrivate)
	    return true;

	PropertyDate date = null;
	Property prop = indi.getProperty("BIRT");
	if (prop!=null){
	    date = (PropertyDate) prop.getProperty("DATE");
	}
	if (date == null && fam != null){
	    prop = fam.getProperty("MARR");
	    if (prop!=null){
		date = (PropertyDate) prop.getProperty("DATE");
	    }
	}
	if (date == null){
	    prop = indi.getProperty("DEAT");
	    if (prop!=null){
		date = (PropertyDate) prop.getProperty("DATE");
	    }
	}
	if (date != null) {
	    Delta delta = date.getAnniversary();
	    if (delta != null){
		return (privateYears > delta.getYears());
	    }
	}
	return false;
    }	


    /*****************************
     * Some helper fonctions that are not direcly related to output formatting
     * but are used to extend some parts of the core API thru some conventionel
     * procedure calls
     */

    /**
     * formatEvent : Get a string describing an event with 
     * differents formatting options
     *
     * isDate : affichage de la date
     * isPlace : affichage du lieu
     * placeIndex >0 : jurisdiction at this position
     * placeIndex <0 : First non null jurisdiction from this position
     * placeIndex =0 : All jurisdictions
     */
    static String formatEvent(Property prop, int dateFormat, boolean isPlace, int placeIndex ) {
	// Prop hidden?
	if ((dateFormat == DATE_FORMAT_NONE) && !isPlace) 
	    return "";
      
	if (prop==null)
	    return "";
	
	WordBuffer result = new WordBuffer();
	PropertyDate date = (dateFormat != DATE_FORMAT_NONE) ? (PropertyDate) prop.getProperty("DATE") : null;
	PropertyPlace plac = isPlace ? (PropertyPlace) prop.getProperty("PLAC") : null;
	result.append(prop.getValue());
	if (date != null ) {
	    if (dateFormat == DATE_FORMAT_LONG){
		result.append(date.getDisplayValue());
	    } else {
		result.append(date.getStart().getYear());
	    }
	}
	if (plac != null ) {
	    if (placeIndex > 0)
		result.append(plac.getJurisdiction(placeIndex-1));
	    else if (placeIndex < 0)
		result.append(plac.getFirstAvailableJurisdiction(-placeIndex-1));
	    else
		result.append(" "+plac.getDisplayValue());
	}
	return result.toString();
    }
    static String formatEvent(Entity entity, String tag, int dateFormat, boolean isPlace, int placeIndex ) {
	// prop exists?
	if (entity==null)
	    return "";
	return formatEvent(entity.getProperty(tag), dateFormat, isPlace, placeIndex );
    }

    static String formatEvent(Entity entity, String tag, boolean isDate, boolean isPlace, int placeIndex ) {
	return(formatEvent(entity, tag, isDate?DATE_FORMAT_LONG:DATE_FORMAT_NONE, isPlace, placeIndex ));
    }

    /**
     * return symbol+' '+eventstring if event is not null
     */
    static String formatEvent(String symbol, Entity entity, String tag, boolean isDate, boolean isPlace, int placeIndex ) {
	String result = formatEvent(entity, tag, isDate, isPlace, placeIndex);
	if (result != null && result.length()!=0){
	    result = symbol + " " + result;
	}
	return result;
    }

}

/**
 * subclass to hide html formattig logic for reports
 */
class FormatterHtml extends Formatter {
    private String style = "";
    private int placeFormat;
  /**
   * Constructor
   */
    protected FormatterHtml(Report parent) {
	super(parent);
    }

    private String tag (String tag, String s) {
	return tag(tag,"",s);
    }
    private String tag (String tag, String param, String s) {
	param = (param.length() != 0)? " "+param:param;
	return ("<"+tag+param+">"+s+"</"+tag+">");
    }

    /**
     * Sets style sheet for the page (output in the head section)
     */
    void setStyle(String s){
	style = s;
    }

    void start(){
	parentReport.println("<HTML>");
	parentReport.println("<head><style>"+style+"</style></head>");
	parentReport.println("<body>");

//	parentReport.println("<HTML><head><style>"+style+"</style></head><body>");
  }

    void end(){
	parentReport.println("</body></html>");}
    void startTable(){
	parentReport.println( "<TABLE>");}
    void endTable(){
	parentReport.println( "</TABLE>");}
    String b(String s){
	return (s.length() == 0)? "" : tag("B",s);}
    String i(String s){
	return (s.length() == 0)? "" : tag("I",s);}
    String underline(String s){
	return (s.length() == 0)? "" : tag("U",s);}
    String em(String s){
	return (s.length() == 0)? "" : tag("EM",s);}
    String strong(String s){
	return (s.length() == 0)? "" : tag("STRONG",s);}
    String row(String s){
	return( tag("TR", s));}
    /*    String row(String s){
	  return( tag("TR", "class=\"report\"", s));}*/
    String cell(String s){
	return cell(s,"",0,0);}
    String cell(String s, String attr){
	return cell(s,attr,0,0);}
    String cell(String s, int rs, int cs){
	return cell(s,"",rs,cs);}
    String cell(String s, String attr, int rs, int cs){
	String param = "valign=top";
	param += (rs!=0)?" ROWSPAN="+rs+"":"";
	param += (cs!=0)?" COLSPAN="+cs:"";
	param += (attr.length() !=0)?" CLASS=\""+attr+"\"":"";
	s = s.trim();
	if (s.length() == 0) s = "&nbsp;";
	return tag("TD",param,s);}
    String br(){return ("<BR>");}
    String page(){return ("<span style=\"page-break-before:always\"></span>");}
    String tablePageBreak(){return ("<tr style=\"page-break-before:always\"></tr>");}
    void println(String s){
	parentReport.println(tag("P",s));}
    void print(String s){
	parentReport.println(s);}
    void println(){
	println("");}
    String h(int level,String s){
	return(tag("H"+level, "class=\"report\"",s));}
    String li(String s){
	return((s.length() == 0)? "" : tag("LI",s));}
    String ul(String s){
	return((s.length() == 0)? "" : tag("UL",s));}
    void startIndent(){
	parentReport.println("<div class=\"indent\">");}
    void endIndent(){
	parentReport.println("</div>");}
    String indent(String s){
	return("<div class=\"indent\">"+s+"</div>");}
    String anchor(String name, String text){
	return(tag("A","name="+name,text));
    }
    String hlink(String name, String text){
	return(tag("A","href=#"+name,text));
    }

}

class FormatterText extends Formatter{
    // from Report.java. redefined here as they can't be used here
    private final static int
	ALIGN_LEFT   = 0,
	ALIGN_CENTER = 1,
	ALIGN_RIGHT  = 2;

    private int tabStops[];
    protected int cellIndex = 0;
    private boolean niceColumn = false;
    private int rowLength = 0;
    protected boolean isInTable = false;    
    private int indentLevel=1;
    protected String eol= System.getProperty("line.separator");

  /**
   * Constructor
   */
    protected FormatterText(Report parent) {
	super(parent);
	isInTable = false;
    }
    void startIndent(){
	indentLevel++;}
    void endIndent(){
	indentLevel = (indentLevel > 0)?indentLevel-1:0;}
    String b(String s){
	return s;}
    String em(String s){
	return s;}
    String strong(String s){
	return s;}
    String i(String s){
	return s;}
    String underline(String s){
	return s;}
    String h(int level,String s) {
	return(s.toUpperCase());}
    void setTabStop(int tabs[]){
	tabStops = tabs;
    }
    void setNiceColumns(boolean b){
	niceColumn=b;
    }
    void startTable(){
	cellIndex = 0;
	rowLength = 0;
	isInTable = true;
    }
    void endTable(){
	isInTable = false;
    }
    String row(String s) {
	isInTable = true;
	cellIndex = 0;
	rowLength = 0;
	return(s.replaceAll(" *$",""));
    }
    /* must be modify to handle multilines cells with column exact */
    String cell(String s) {
	isInTable = true;
	String result = "";
	int ts = tabStops[cellIndex];
	String padding = "";

	if (cellIndex == 0){
	    padding = "";
	} else {
	    padding = parentReport.align("",Math.abs(tabStops[cellIndex-1]),ALIGN_LEFT);
	}
	s = s.replaceFirst(eol,"");
	s = s.replaceAll(eol,eol+padding);
	if (ts > 0){
	    result = formatString(s+" ", ts-rowLength);
	} else {
	    result = formatString(s+" ", ts+rowLength); 
	}
	cellIndex++;
	rowLength += result.length();
	return result;

    }
    String cell(String s, String attr) {
	return cell(s);}
    String cell(String s, String attr, int rs, int cs){
	return cell(s,rs,cs);}
    String cell(String s, int rs, int cs){
	isInTable = true;
	cellIndex++;
	return s;
    }
    String br(){ return System.getProperty("line.separator");}
    String page(){ return "";}
    String tablePageBreak(){ return "";}
    void println(String s){
	parentReport.println(parentReport.getIndent(indentLevel)+s);}
    void print(String s){
	println(s);}
    void println(){
	println("");}
    String li(String s){
	return((s.length() == 0)? "" : eol+"- "+s);}
    String ul(String s){
	if (s.length() == 0){
	    return "";
	}
	if (!isInTable){
	    s = s.replaceAll(eol,eol+parentReport.getIndent(indentLevel+1));
	}
	return s;
    }
    void start(){
	isInTable = false;
    }
    void end(){}
    String anchor(String name, String text){
	return(text);
    }
    String hlink(String name, String text){
	return(text);
    }

    private String formatString(String s, int size){
	if (!niceColumn && (Math.abs(size)-s.length())<0)
	    return s+" ";
	if (size > 0){
	    return parentReport.align(s,size-1,ALIGN_LEFT)+" ";
	} else {
	    return parentReport.align(s,-size-1,ALIGN_RIGHT)+" ";
	}
    }
}


class FormatterCsv extends FormatterText{
    private String delimiter="|";

  /**
   * Constructor
   */
    protected FormatterCsv(Report parent) {
	super(parent);
	delimiter="|";
    }
    /* must be modify to handle multilines cells with column exact */
    String cell(String s) {
	isInTable = true;

	s = s.replaceAll(eol," ");
	cellIndex++;
	if (cellIndex == 1){
	    return s;
	} else {
	    return delimiter+s;
	}
    }
    String cell(String s, String attr) {
	return cell(s);}
    String cell(String s, String attr, int rs, int cs){
	return cell(s,rs,cs);}
    String cell(String s, int rs, int cs){
	return cell(s);
    }
    String br(){ return System.getProperty("line.separator");}
    void println(String s){
	parentReport.println(s);}
    void print(String s){
	println(s);}
    void println(){
	println("");}
    String li(String s){
	return(s);}
    String ul(String s){
	return s;
    }
    void start(){
	isInTable = false;
    }
    void end(){}
    String anchor(String name, String text){
	return(text);
    }
    String hlink(String name, String text){
	return(text);
    }
}



