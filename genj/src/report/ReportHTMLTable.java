/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.report.Report;
import genj.util.ReferenceSet;
import genj.window.CloseWindow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * GenJ - Report
 * @author Carsten Muessig <carsten.muessig@gmx.net>
 * @version 1.01
 */
public class ReportHTMLTable extends Report {
	  
	/** an options - style sheet */
	  public String styleSheet = "";
	  
	  /** table for last names? */
	  public boolean lastNameTable = true;
	  /** sort last names by name (or by frequency) ? */
	  public boolean sortLastNamesByName = true;
	  
	  /** table for birth places? */
	  public boolean birthPlaceTable = true;
	  /** sort birtPlaces by name (or by frequency) ? */
	  public boolean sortBirthPlacesByName = true;	  
	  
	  /** table for baptism places? */
	  public boolean baptismPlaceTable = true;
	  /** sort baptism places by name (or by frequency) ? */
	  public boolean sortBaptismPlacesByName = true;	  

	  /** table for marriage places? */
	  public boolean marriagePlaceTable = true;
	  /** sort marriage places by name (or by frequency) ? */
	  public boolean sortMarriagePlacesByName = true;	  

	  /** table for emigration places? */
	  public boolean emigrationPlaceTable = true;
	  /** sort emigration places by name (or by frequency) ? */
	  public boolean sortEmigrationPlacesByName = true;	  	  
	  
	  /** table for immigration places? */
	  public boolean immigrationPlaceTable = true;
	  /** sort immigration places by name (or by frequency) ? */
	  public boolean sortImmigrationPlacesByName = true;	  
	  
	  /** table for naturalization places? */
	  public boolean naturalizationPlaceTable = true;
	  /** sort naturalization places by name (or by frequency) ? */
	  public boolean sortNaturalizationPlacesByName = true;	  
	  
	  /** table for death places? */
	  public boolean deathPlaceTable = true;	  
	  /** sort death places by name (or by frequency) ? */
	  public boolean sortDeathPlacesByName = true;	
	  
	  /** whether to translate between unicode and html or not (slow!) */
	  public boolean isUnicode2HTML = true;
	  
	  /** HTML Coded Character Set (see http://www.w3.org/MarkUp/html-spec/html-spec_13.html)*/
	  private final static String[] codeTable = {
	   "\u00d1" , "&Ntilde;", //    Capital N, tilde
	   "\u00d2" , "&Ograve;", //    Capital O, grave accent
	   "\u00d3" , "&Oacute;", //    Capital O, acute accent
	   "\u00d4" , "&Ocirc;", //     Capital O, circumflex accent
	   "\u00d5" , "&Otilde;", //    Capital O, tilde
	   "\u00d6" , "&Ouml;", //      Capital O, dieresis or umlaut mark
	   "\u00df" , "&szlig;", //     Small sharp s, German (sz ligature)
	   "\u00c0" , "&Agrave;", //     Capital A, grave accent
	   "\u00c1" , "&Aacute;", //    Capital A, acute accent
	   "\u00c2" , "&Acirc;", //     Capital A, circumflex accent
	   "\u00c3" , "&Atilde;", //    Capital A, tilde
	   "\u00c4" , "&Auml;", //      Capital A, dieresis or umlaut mark
	   "\u00c5" , "&Aring;", //     Capital A, ring
	   "\u00c6" , "&AElig;", //     Capital AE dipthong (ligature)
	   "\u00c7" , "&Ccedil;", //    Capital C, cedilla
	   "\u00c8" , "&Egrave;", //    Capital E, grave accent
	   "\u00c9" , "&Eacute;", //    Capital E, acute accent
	   "\u00ca" , "&Ecirc;", //     Capital E, circumflex accent
	   "\u00cb" , "&Euml;", //      Capital E, dieresis or umlaut mark
	   "\u00cc" , "&Igrave;", //    Capital I, grave accent
	   "\u00cd" , "&Iacute;", //    Capital I, acute accent
	   "\u00ce" , "&Icirc;", //     Capital I, circumflex accent
	   "\u00cf" , "&Iuml;", //      Capital I, dieresis or umlaut mark
	   "\u00d9" , "&Ugrave;", //    Capital U, grave accent
	   "\u00da" , "&Uacute;", //    Capital U, acute accent
	   "\u00db" , "&Ucirc;", //     Capital U, circumflex accent
	   "\u00dc" , "&Uuml;", //      Capital U, dieresis or umlaut mark
	   "\u00dd" , "&Yacute;", //    Capital Y, acute accent
	   "\u00e0" , "&agrave;", //    Small a, grave accent
	   "\u00e1" , "&aacute;", //    Small a, acute accent
	   "\u00e2" , "&acirc;", //     Small a, circumflex accent
	   "\u00e3" , "&atilde;", //    Small a, tilde
	   "\u00e4" , "&auml;", //      Small a, dieresis or umlaut mark
	   "\u00e6" , "&aelig;", //     Small ae dipthong (ligature)
	   "\u00e7" , "&ccedil;", //    Small c, cedilla
	   "\u00e8" , "&egrave;", //    Small e, grave accent
	   "\u00e9" , "&eacute;", //    Small e, acute accent
	   "\u00ea" , "&ecirc;", //     Small e, circumflex accent
	   "\u00eb" , "&euml;", //      Small e, dieresis or umlaut mark
	   "\u00ec" , "&igrave;", //    Small i, grave accent
	   "\u00ed" , "&iacute;", //    Small i, acute accent
	   "\u00ee" , "&icirc;", //     Small i, circumflex accent
	   "\u00ef" , "&iuml;", //      Small i, dieresis or umlaut mark
	   "\u00f0" , "&eth;", //       Small eth, Icelandic
	   "\u00f1" , "&ntilde;", //    Small n, tilde
	   "\u00f2" , "&ograve;", //    Small o, grave accent
	   "\u00f3" , "&oacute;", //    Small o, acute accent
	   "\u00f4" , "&ocirc;", //     Small o, circumflex accent
	   "\u00f5" , "&otilde;", //    Small o, tilde
	   "\u00f6" , "&ouml;", //      Small o, dieresis or umlaut mark
	   "\u00f8" , "&oslash;", //    Small o, slash
	   "\u00f9" , "&ugrave;", //    Small u, grave accent
	   "\u00fa" , "&uacute;", //    Small u, acute accent
	   "\u00fb" , "&ucirc;", //     Small u, circumflex accent
	   "\u00fc" , "&uuml;", //      Small u, dieresis or umlaut mark
	   "\u00fd" , "&yacute;", //    Small y, acute accent
	   "\u00fe" , "&thorn;", //     Small thorn, Icelandic
	   "\u00ff" , "&yuml;"  //      Small y, dieresis or umlaut mark
	  };
	  
	  /** the mapping between unicode and html */
	  private static Hashtable unicode2html = initializeUnicodeSupport();
	  
	  /** this report's version */
	  public static final String VERSION = "1.1";
	  
	  /* possible parameters for printEmiImmiNatu() in order
	     to implement this methode generic */
	  private static final int EMIG = 0;
	  private static final int IMMI = 1;
	  private static final int NATU = 2;
	    
	    /** Returns the version of the report
	     */
	    public String getVersion() {
	        return VERSION;
	    }
	    
	    // this report only works on the whole Gedcom file
	    public String accepts(Object context) {
	        if (context instanceof Gedcom)
	            return getName();
	        return null;
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
	     * The result is stored in files
	     */
	    public boolean usesStandardOut() {
	      return false;
            }
            
            private File createFile(File dir, String name) {
                println(i18n("creating")+" "+name);
                return new File(dir, name);
            }
	  
	  /**
	   * Initializes a Hashtable of Unicode 2 HTML code mappings
	   */
	  private static Hashtable initializeUnicodeSupport() {

	    Hashtable result = new Hashtable();

	    // loop and copy
	    for (int c=0;c<codeTable.length;c+=2) {
	      result.put(
	        codeTable[c+0],
	        codeTable[c+1]
	      );
	    }

	    // done
	    return result;
	  }
	  
	  /**
	   * The report's entry point
	   */
	  public void start(Object context) {
	    
	    // assuming Gedcom
	    Gedcom gedcom = (Gedcom)context;
	    
	    // the data to examine
        Entity[] indis = gedcom.getEntities(Gedcom.INDI, "");
        Entity[] fams = gedcom.getEntities(Gedcom.FAM,"");

	    // Get a directory to write to
	    File dir = getDirectoryFromUser(i18n("target.dir"), CloseWindow.TXT_OK);
	    if (dir==null)
	      return;

	    // tell the user about the output directory
	    println(i18n("target.dir")+" = "+dir);
	    
	    // Make sure directory is there
	    if (!dir.exists()&&!dir.mkdirs()) {
	      println("***Couldn't create output directory "+dir);
	      return;
	    }
	    
	    /* these variables are used in multiple cases and
	       therefore defined outside a certain visibility */
	    PrintWriter htmlOut = null, indexOut = null;
            try {
                indexOut =  new PrintWriter(new FileOutputStream(createFile(dir, "index.html")));
                printHTMLStart(indexOut);
			} catch(Exception e) {
		  		println("Can't create index.html: "+e.getMessage());
		  	}
	    ReferenceSet set = null;
	    
	    // examine the last names
		if(lastNameTable) {
			String lastName;
			try {
				// initialize print writer
				htmlOut = new PrintWriter(new FileOutputStream(createFile(dir, "lastNames.html")));
			} catch(Exception e) {
		  		println("Can't create lastName.html: "+e.getMessage());
		  	}
		  	set = new ReferenceSet();
		  	// write HTML header
		  	printHTMLStart(htmlOut);
		  	// write table header
		  	htmlOut.println("<table border=1 cellspacing=1>");
		  	// get Indi's last names and put them into a ReferenceSet
		  	for(int i=0;i<indis.length;i++) {
		  		lastName = ((Indi)indis[i]).getLastName();
		  		if(lastName.length()==0)
		  			lastName = i18n("unknown");
		  		set.add(lastName, indis[i].getId());
		  	}
		  	// write table header
		  	htmlOut.println("<tr bgcolor=\"yellow\"><td>"+i18n("lastNames")+"</td><td>"+i18n("frequency")+"</td></tr>");
		  	// sort the table data
		  	ArrayList sortedLastNames = new ArrayList(set.getKeys(sortLastNamesByName));
		  	// loop over table data and write to the file
		  	for(int i=0;i<sortedLastNames.size();i++) {
		  		lastName = (String)sortedLastNames.get(i);
		  		htmlOut.println("<tr><td>"+wrapText(lastName)+"</td><td>"+set.getReferences(lastName).size()+"</td></tr>");
		  	}
		  	// write table footer
		  	htmlOut.println("</table>");
		  	// write HTML footer
		  	printHTMLEnd(htmlOut);
		  	// close the file
		  	htmlOut.close();
                        indexOut.println("<a href=\"lastNames.html\">"+i18n("lastNames")+"</a><br>");
		  }		  
		
		  /* same program logic used as for last names, but with
		     examination of the birth place property */
		  if(birthPlaceTable) {
		  	try {
			  	htmlOut = new PrintWriter(new FileOutputStream(createFile(dir, "birthPlaces.html")));
		  	} catch(Exception e) {
		  		println("Can't create birthPlaces.html: "+e.getMessage());
		  	}
			  	set = new ReferenceSet();
			  	printHTMLStart(htmlOut);
			  	htmlOut.println("<table border=1 cellspacing=1>");
			  	for(int i=0;i<indis.length;i++) {
			  		Property prop = indis[i].getProperty(new TagPath("INDI:BIRT:PLAC"));
			  		if(prop!=null) {
			  			String birthPlace = prop.toString();
			  			if(birthPlace.length()==0)
			  				birthPlace = i18n("unknown");
			  			set.add(birthPlace, indis[i].getId());
			  		}
			  	}
			  	htmlOut.println("<tr bgcolor=\"yellow\"><td>"+i18n("birthPlaces")+"</td><td>"+i18n("frequency")+"</td></tr>");
			  	ArrayList sortedBirthPlaces = new ArrayList(set.getKeys(sortBirthPlacesByName));
			  	for(int i=0;i<sortedBirthPlaces.size();i++) {
			  		String birthPlace = (String)sortedBirthPlaces.get(i);
			  		htmlOut.println("<tr><td>"+wrapText(birthPlace)+"</td><td>"+set.getReferences(birthPlace).size()+"</td></tr>");
			  	}
				htmlOut.println("</table>");
			  	printHTMLEnd(htmlOut);
			  	htmlOut.close();
                                indexOut.println("<a href=\"birthPlaces.html\">"+i18n("birthPlaces")+"</a><br>");
		  }
          
		  /* same program logic used as for last names, but with
		     examination of all properties which can contain baptism data*/		  
		  if(baptismPlaceTable) {
		  	try {
			  	htmlOut = new PrintWriter(new FileOutputStream(createFile(dir, "baptismPlaces.html")));
		  	} catch(Exception e) {
		  		println("Can't create baptismPlaces.html: "+e.getMessage());
		  	}
			  	set = new ReferenceSet();
			  	printHTMLStart(htmlOut);
			  	htmlOut.println("<table border=1 cellspacing=1>");
			  	for(int i=0;i<indis.length;i++) {
			  		Property prop;
			  		String baptismPlace;
			  		/* individual identifier for each type of baptism
			  		   becaus the underlying HashSet doesn't allow duplicates */
		            prop = indis[i].getProperty(new TagPath("INDI:BAPM:PLAC"));
				  		if(prop!=null) {
				  			baptismPlace = prop.toString();
				  			if(baptismPlace.length()==0)
				  				baptismPlace = i18n("unknown");
				  			set.add(baptismPlace, indis[i].getId()+"bapm");
				  		}
		            prop = indis[i].getProperty(new TagPath("INDI:BAPL:PLAC"));
				  		if(prop!=null) {
				  			baptismPlace = prop.toString();
				  			if(baptismPlace.length()==0)
				  				baptismPlace = i18n("unknown");
				  			set.add(baptismPlace.toString(), indis[i].getId()+"bapl");
				  		}
		            prop = indis[i].getProperty(new TagPath("INDI:CHR:PLAC"));
				  		if(prop!=null) {
				  			baptismPlace = prop.toString();
				  			if(baptismPlace.length()==0)
				  				baptismPlace = i18n("unknown");
				  			set.add(baptismPlace, indis[i].getId()+"chr");
				  		}
		            prop = indis[i].getProperty(new TagPath("INDI:CHRA:PLAC"));
				  		if(prop!=null) {
				  			baptismPlace = prop.toString();
				  			if(baptismPlace.length()==0)
				  				baptismPlace = i18n("unknown");
				  			set.add(baptismPlace, indis[i].getId()+"chra");
				  		}
			  	}
			  	htmlOut.println("<tr bgcolor=\"yellow\"><td>"+i18n("baptismPlaces")+"</td><td>"+i18n("frequency")+"</td></tr>");
			  	ArrayList sortedBaptismPlaces = new ArrayList(set.getKeys(sortBaptismPlacesByName));
			  	for(int i=0;i<sortedBaptismPlaces.size();i++) {
			  		String baptismPlace = (String)sortedBaptismPlaces.get(i);
			  		htmlOut.println("<tr><td>"+wrapText(baptismPlace)+"</td><td>"+set.getReferences(baptismPlace).size()+"</td></tr>");
			  	}
				htmlOut.println("</table>");
			  	printHTMLEnd(htmlOut);
			  	htmlOut.close();
                                indexOut.println("<a href=\"baptismPlaces.html\">"+i18n("baptismPlaces")+"</a><br>");
		  }

		  /* same program logic used as for last names, but with
		     examination of the marriage place property */
		  if(marriagePlaceTable) {
		  	try {
			  	htmlOut = new PrintWriter(new FileOutputStream(createFile(dir, "marriagePlaces.html")));
			  	} catch(Exception e) {
			  		println("Can't create marriagePlaces.html: "+e.getMessage());
			  	}
			  	set = new ReferenceSet();
			  	printHTMLStart(htmlOut);
			  	htmlOut.println("<table border=1 cellspacing=1>");
			  	for(int i=0;i<fams.length;i++) {
			  		Property[] props = fams[i].getProperties(new TagPath("FAM:MARR:PLAC"));
			  		if((props!=null) && (props.length>0)) {
			  			for(int j=0;j<props.length;j++) {
				  			String marriagePlace = props[j].toString();
				  			if(marriagePlace.length()==0)
				  				marriagePlace = i18n("unknown");
			  				set.add(marriagePlace, fams[i].getId()+j);
			  			}
			  		}
			  	}
			  	htmlOut.println("<tr bgcolor=\"yellow\"><td>"+i18n("marriagePlaces")+"</td><td>"+i18n("frequency")+"</td></tr>");
			  	ArrayList sortedMarriagePlaces = new ArrayList(set.getKeys(true));
			  	for(int i=0;i<sortedMarriagePlaces.size();i++) {
			  		String marriagePlace = (String)sortedMarriagePlaces.get(i);
			  		htmlOut.println("<tr><td>"+wrapText(marriagePlace)+"</td><td>"+set.getReferences(marriagePlace).size()+"</td></tr>");
			  	}
				htmlOut.println("</table>");
			  	printHTMLEnd(htmlOut);
			  	htmlOut.close();
                                indexOut.println("<a href=\"marriagePlaces.html\">"+i18n("marriagePlaces")+"</a><br>");
			  }

		  /* using printEmigImmiNatu() to examine and output the data, this body
		     just writes header and footers */
		  if(emigrationPlaceTable) {
		  	try {
			  	htmlOut = new PrintWriter(new FileOutputStream(createFile(dir, "emigrationPlaces.html")));
			  	} catch(Exception e) {
			  		println("Can't create emigrationPlaces.html: "+e.getMessage());
			  	}
			  	printEmigImmiNatu(EMIG, indis, htmlOut);
			  	printHTMLEnd(htmlOut);
			  	htmlOut.close();
                                indexOut.println("<a href=\"emigrationPlaces.html\">"+i18n("emigration")+"</a><br>");
			  }
		  
		  /* using printEmigImmiNatu() to examine and output the data, this body
		     just writes header and footers */		  
		  if(immigrationPlaceTable) {
		  	try {
			  	htmlOut = new PrintWriter(new FileOutputStream(createFile(dir, "immigrationPlaces.html")));
			  	} catch(Exception e) {
			  		println("Can't create immigrationPlaces.html: "+e.getMessage());
			  	}
			  	printEmigImmiNatu(IMMI, indis, htmlOut);
			  	printHTMLEnd(htmlOut);
			  	htmlOut.close();
                                indexOut.println("<a href=\"immigrationPlaces.html\">"+i18n("immigration")+"</a><br>");
		  }
		  
		  /* using printEmigImmiNatu() to examine and output the data, this body
		     just writes header and footers */
		  if(naturalizationPlaceTable) {
		  	try {
			  	htmlOut = new PrintWriter(new FileOutputStream(createFile(dir, "naturalizationPlaces.html")));
			  	} catch(Exception e) {
			  		println("Can't create naturalizationPlaces.html: "+e.getMessage());
			  	}
			  	printEmigImmiNatu(NATU, indis, htmlOut);
			  	printHTMLEnd(htmlOut);
			  	htmlOut.close();
                                indexOut.println("<a href=\"naturalizationPlaces.html\">"+i18n("naturalization")+"</a><br>");
		  }
		  
		  /* same program logic used as for last names, but with
		     examination of the death place property */
		  if(deathPlaceTable) {
		  	try {
			  	htmlOut = new PrintWriter(new FileOutputStream(createFile(dir, "deathPlaces.html")));
			  	} catch(Exception e) {
			  		println("Can't create deathPlaces.html: "+e.getMessage());
			  	}
			  	set = new ReferenceSet();
			  	printHTMLStart(htmlOut);
			  	htmlOut.println("<table border=1 cellspacing=1>");
			  	for(int i=0;i<indis.length;i++) {
			  		Property prop = indis[i].getProperty(new TagPath("INDI:DEAT:PLAC"));
			  		if(prop!=null) {
			  			String deathPlace = prop.toString();
			  			if(deathPlace.length()==0)
			  				deathPlace = i18n("unknown");
		  				set.add(deathPlace, indis[i].getId());
			  		}
			  	}
			  	htmlOut.println("<tr bgcolor=\"yellow\"><td>"+i18n("deathPlaces")+"</td><td>"+i18n("frequency")+"</td></tr>");
			  	ArrayList sortedDeathPlaces = new ArrayList(set.getKeys(sortDeathPlacesByName));
			  	for(int i=0;i<sortedDeathPlaces.size();i++) {
			  		String deathPlace = (String)sortedDeathPlaces.get(i);
			  		htmlOut.println("<tr><td>"+wrapText(deathPlace)+"</td><td>"+set.getReferences(deathPlace).size()+"</td></tr>");
			  	}
				htmlOut.println("</table>");
			  	printHTMLEnd(htmlOut);
			  	htmlOut.close();
                                indexOut.println("<a href=\"deathPlaces.html\">"+i18n("deathPlaces")+"</a><br>");
		  }
            printHTMLEnd(indexOut);
            indexOut.close();
	  }
	  
	  /** examines and writes emigration, immigration and naturalization places
	   * @param which places is examined
	   * @param indis to be examined
	   * @param htmlOut file to write to
	   */
	  private void printEmigImmiNatu(int which, Entity[] indis, PrintWriter htmlOut){
	  	
	  	// variabes to allow the method to be generic
	  	String tag="", title="";
	  	boolean sortByPlace = true;
	  	
	  	// fill the variables
	  	switch(which) {
	  	case EMIG:
	  		tag = "EMIG";
	  		title = i18n("emigration");
	  		sortByPlace = sortEmigrationPlacesByName;
	  		break;
	  	case IMMI:
	  		tag = "IMMI";
	  		title = i18n("immigration");
	  		sortByPlace = sortImmigrationPlacesByName;
	  		break;
	  	case NATU:
	  		tag = "NATU";
	  		title = i18n("naturalization");
	  		sortByPlace = sortNaturalizationPlacesByName;
	  		break;
	  	default: 
	  		break;	  			
	  	};
		
	  	ReferenceSet set = new ReferenceSet();
	  	// write HTML header
		printHTMLStart(htmlOut);
		// write table header
		htmlOut.println("<table border=1 cellspacing=1>");
		// get the data by examinig the properties
		for(int i=0;i<indis.length;i++) {
			Property[] props = indis[i].getProperties(new TagPath("INDI:"+tag+":PLAC"));
			if((props!=null) && (props.length>0)) {
				for(int j=0;j<props.length;j++) {
		  			String place = props[j].toString();
		  			if(place.length()==0)
		  				place = i18n("unknown");
					set.add(place, indis[i].getId()+j);
				}
		  	}
		}
		//write the table header
		htmlOut.println("<tr bgcolor=\"yellow\"><td>"+title+"</td><td>"+i18n("frequency")+"</td></tr>");
		// sort the table data
		ArrayList sortedPlaces = new ArrayList(set.getKeys(sortByPlace));
		// print the table data
		for(int i=0;i<sortedPlaces.size();i++) {
		 	String place = (String)sortedPlaces.get(i);
		 	htmlOut.println("<tr><td>"+wrapText(place)+"</td><td>"+set.getReferences(place).size()+"</td></tr>");
		}
		// write the table footer
		htmlOut.println("</table>");
	  }
	  
	  /** writes the HTML header
	   *  @param out file to write to
	   */
	  private void printHTMLStart(PrintWriter out) {
	  	out.println("<html>");
		out.println("<head>");
	    if (styleSheet.length()>0)
	    	out.println("<link rel=StyleSheet href=\""+styleSheet+"\" type=\"text/css\"></link>");
		out.println("</head>");
		out.println("<body>");
	  }
	  
	  /** writes the HTML footer
	   *  @param out file to write to
	   */	  
	  private void printHTMLEnd(PrintWriter out) {
	  	out.println("</body>");
	  	out.println("</html>");
	  }
	  
	  /**
	   * Helper to make sure that a Unicode text ends up nicely in HTML
	   */
	  private String wrapText(String text) {

	    // no need to transform if !isUnicode2HTML
	    if (!isUnicode2HTML) 
	      return text;

	    // transform characters
	    StringBuffer result = new StringBuffer(256);
	    for (int c=0;c<text.length();c++) {
	      String unicode = text.substring(c,c+1);
	      Object html    = unicode2html.get(unicode);
	      if (html==null) {
	        result.append(unicode);
	      } else {
	        result.append(html);
	      }
	    }

	    // print it
	    return result.toString();
	  }
}